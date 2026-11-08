package com.houjiang.logging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.logging.dto.LogAlertQueryDTO;
import com.houjiang.logging.entity.LogAlertEntity;
import com.houjiang.logging.entity.LogErrorEntity;
import com.houjiang.logging.mapper.LogAlertMapper;
import com.houjiang.logging.service.LogAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 运行错误告警 Service 实现类
 *
 * <p>核心是「指纹聚合」：错误日志是流式的，同一个 Bug 被 100 个用户触发就会产生 100 条记录，
 * 直接推给后台等于刷屏。这里把 module + 异常类 + 归一化后的错误消息首行算成一个指纹，
 * 同一个指纹只占一条告警，累计 hitCount 并刷新最近一次的上下文（谁、哪个接口、堆栈）。</p>
 *
 * <p>归一化会把消息里的数字、UUID、十六进制串换成占位符，避免「订单 12345 不存在」和
 * 「订单 67890 不存在」被算成两个问题。</p>
 *
 * <p>告警被标记为已处理后若再次出现，会重新打开为未处理 —— 修复没生效必须能被发现。</p>
 *
 * @author Amos
 * @since 2026-09-25
 */
@Slf4j
@Service
public class LogAlertServiceImpl extends ServiceImpl<LogAlertMapper, LogAlertEntity>
        implements LogAlertService {

    /** 错误消息摘要最大长度 */
    private static final int MESSAGE_MAX = 900;

    /** 堆栈最大长度（够定位问题即可，避免把表撑爆） */
    private static final int STACK_MAX = 8000;

    /** 指纹计算截取的消息首行长度 */
    private static final int FINGERPRINT_MESSAGE_MAX = 200;

    /**
     * 分页查询告警
     *
     * @param dto     查询条件 DTO（status 为空时默认只查未处理）
     * @param current 当前页码
     * @param size    每页记录数
     * @return 分页结果
     */
    @Override
    public IPage<LogAlertEntity> queryPage(LogAlertQueryDTO dto, long current, long size) {
        QueryWrapper<LogAlertEntity> wrapper = new QueryWrapper<>();
        // 后台告警中心的主视角是「还有什么没修」，不传状态就只看未处理
        wrapper.eq("status", dto.getStatus() != null ? dto.getStatus() : 0);
        if (StringUtils.hasText(dto.getModule())) {
            wrapper.eq("module", dto.getModule());
        }
        if (StringUtils.hasText(dto.getErrorType())) {
            wrapper.like("error_type", dto.getErrorType());
        }
        if (dto.getStartTime() != null) {
            wrapper.ge("last_time", dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            wrapper.le("last_time", dto.getEndTime());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like("error_message", dto.getKeyword())
                    .or().like("error_type", dto.getKeyword())
                    .or().like("request_url", dto.getKeyword()));
        }
        wrapper.orderByDesc("last_time");
        return page(new Page<>(current, size), wrapper);
    }

    /**
     * 收到一条错误日志后聚合成告警
     *
     * @param errorLog 已落库的错误日志
     */
    @Override
    public void onError(LogErrorEntity errorLog) {
        if (errorLog == null) {
            return;
        }
        try {
            String fingerprint = buildFingerprint(errorLog);
            String message = trim(errorLog.getErrorMessage(), MESSAGE_MAX);
            String stack = trim(errorLog.getStackTrace(), STACK_MAX);
            String url = trim(errorLog.getRequestUrl(), 500);
            String userId = trim(errorLog.getUserId(), 64);
            String username = trim(errorLog.getUsername(), 100);
            String ip = trim(errorLog.getIp(), 50);
            String method = trim(errorLog.getRequestMethod(), 10);
            LocalDateTime now = LocalDateTime.now();

            UpdateWrapper<LogAlertEntity> update = new UpdateWrapper<>();
            update.eq("fingerprint", fingerprint)
                    .setSql("hit_count = hit_count + 1")
                    .set("last_time", now)
                    .set("error_message", message)
                    .set("stack_trace", stack)
                    .set("request_url", url)
                    .set("request_method", method)
                    .set("user_id", userId)
                    .set("username", username)
                    .set("ip", ip)
                    // 已处理的告警再次出现说明没修好，重新打开
                    .set("status", 0)
                    .set("handle_time", null)
                    .set("handle_remark", "");
            boolean updated = update(update);

            if (!updated) {
                LogAlertEntity alert = new LogAlertEntity();
                alert.setFingerprint(fingerprint);
                alert.setModule(trim(errorLog.getModule(), 50));
                alert.setErrorType(trim(errorLog.getErrorType(), 200));
                alert.setErrorMessage(message);
                alert.setStackTrace(stack);
                alert.setRequestUrl(url);
                alert.setRequestMethod(method);
                alert.setUserId(userId);
                alert.setUsername(username);
                alert.setIp(ip);
                alert.setHitCount(1);
                alert.setStatus(0);
                alert.setFirstTime(now);
                alert.setLastTime(now);
                try {
                    save(alert);
                } catch (DuplicateKeyException e) {
                    // 并发下指纹已由别的实例插入，退化为累加
                    update(update);
                }
            }
        } catch (Exception e) {
            // 告警是旁路能力，绝不能因为告警失败影响错误日志本身
            log.error("[LogAlertService] 生成运行告警失败，errorType={}，原因={}",
                    errorLog == null ? null : errorLog.getErrorType(), e.getMessage());
        }
    }

    /**
     * 统计未处理告警数量（后台红点用）
     *
     * @return 未处理（status=0）告警条数
     */
    @Override
    public long countUnhandled() {
        return count(new QueryWrapper<LogAlertEntity>().eq("status", 0));
    }

    /**
     * 标记告警为已处理
     *
     * @param id     告警ID
     * @param remark 处理备注（修复原因）
     * @return 是否更新成功
     */
    @Override
    public boolean markHandled(Long id, String remark) {
        return markStatus(id, 1, remark);
    }

    /**
     * 标记告警为已忽略
     *
     * @param id     告警ID
     * @param remark 忽略原因
     * @return 是否更新成功
     */
    @Override
    public boolean markIgnored(Long id, String remark) {
        return markStatus(id, 2, remark);
    }

    /**
     * 清理已处理/已忽略且长时间未复现的告警
     *
     * @param days 保留天数
     */
    @Override
    public void cleanExpiredAlerts(int days) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        QueryWrapper<LogAlertEntity> wrapper = new QueryWrapper<>();
        wrapper.in("status", 1, 2).lt("last_time", expireTime);
        int count = baseMapper.delete(wrapper);
        log.info("[LogAlertService] 清理已闭环告警 {} 条（{}天内未复现）", count, days);
    }

    /**
     * 更新告警状态
     *
     * @param id     告警ID
     * @param status 目标状态（1已处理 2已忽略）
     * @param remark 备注
     * @return 是否更新成功
     */
    private boolean markStatus(Long id, Integer status, String remark) {
        if (id == null) {
            return false;
        }
        UpdateWrapper<LogAlertEntity> update = new UpdateWrapper<>();
        update.eq("id", id)
                .set("status", status)
                .set("handle_time", LocalDateTime.now())
                .set("handle_remark", trim(remark, 500));
        return update(update);
    }

    /**
     * 生成错误指纹
     *
     * <p>module + 异常类 + 归一化消息首行，归一化时把数字、UUID、十六进制串替换为占位符。</p>
     *
     * @param errorLog 错误日志
     * @return 指纹（32位小写MD5）
     */
    private String buildFingerprint(LogErrorEntity errorLog) {
        String raw = trim(errorLog.getModule(), 50) + "|"
                + trim(errorLog.getErrorType(), 200) + "|"
                + normalize(trim(errorLog.getErrorMessage(), FINGERPRINT_MESSAGE_MAX));
        return md5(raw);
    }

    /**
     * 归一化错误消息：把易变片段（数字、UUID、十六进制、引号内的长串）替换为占位符
     *
     * @param message 原始消息
     * @return 归一化后的消息
     */
    private String normalize(String message) {
        if (!StringUtils.hasText(message)) {
            return "";
        }
        String text = message.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
        text = text.replaceAll("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "{uuid}");
        text = text.replaceAll("\\b[0-9a-f]{16,}\\b", "{hash}");
        text = text.replaceAll("\\d+", "{n}");
        return text;
    }

    /**
     * 截断字符串，避免超长内容写库失败
     *
     * @param value 原值
     * @param max   最大长度
     * @return 截断后的字符串（原值为空返回空串）
     */
    private String trim(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    /**
     * 计算 MD5
     *
     * @param input 原始字符串
     * @return 32位小写十六进制
     */
    private String md5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(Character.forDigit((b >> 4) & 0xf, 16));
                sb.append(Character.forDigit(b & 0xf, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            // 极端环境下 MD5 不可用时退化为原文哈希，保证指纹仍然稳定
            return String.valueOf(input.hashCode());
        }
    }
}
