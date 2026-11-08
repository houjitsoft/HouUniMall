package com.houjiang.payment.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.util.PayCredentialCryptoUtil;
import com.houjiang.payment.entity.PayChannelEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.mapper.PayChannelMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 自研渠道策略抽象基类（W5 新增）
 * <p>
 * 统一处理凭证加载/解密：
 * <ul>
 *   <li>按 channelCode + merchantNo 查 hou_pay_channel</li>
 *   <li>未指定商户号则取全局配置（merchant_no IS NULL）</li>
 *   <li>解密 config_json，必填字段缺失则抛 CHANNEL_NOT_CONFIGURED(503)</li>
 * </ul>
 *
 * @author Amos
 */
@Slf4j
public abstract class AbstractChannelPayStrategy implements ChannelPayStrategy {

    /** 错误码常量 */
    protected static final String ERR_CHANNEL_NOT_CONFIGURED = "CHANNEL_NOT_CONFIGURED";

    @Autowired
    protected PayChannelMapper payChannelMapper;

    /**
     * 子类声明本渠道必填字段。返回空数组表示无强制必填项。
     */
    protected abstract String[] requiredFields();

    /**
     * 加载并解密凭证；缺失必填项抛 CHANNEL_NOT_CONFIGURED
     *
     * @param mchNo 商户号（NULL 取全局）
     * @return 解密后的 configMap（不可变视图，供调用方读取凭证）
     */
    protected Map<String, Object> loadCredential(String mchNo) {
        String channelCode = getChannel();
        // 先按商户号查
        PayChannelEntity entity = null;
        if (mchNo != null && !mchNo.isEmpty()) {
            entity = payChannelMapper.selectOne(new LambdaQueryWrapper<PayChannelEntity>()
                    .eq(PayChannelEntity::getChannelCode, channelCode)
                    .eq(PayChannelEntity::getMerchantNo, mchNo)
                    .eq(PayChannelEntity::getStatus, 1)
                    .last("LIMIT 1"));
        }
        // 未配置则回退全局
        if (entity == null) {
            entity = payChannelMapper.selectOne(new LambdaQueryWrapper<PayChannelEntity>()
                    .eq(PayChannelEntity::getChannelCode, channelCode)
                    .isNull(PayChannelEntity::getMerchantNo)
                    .eq(PayChannelEntity::getStatus, 1)
                    .last("LIMIT 1"));
        }
        if (entity == null || entity.getConfigJson() == null || entity.getConfigJson().isEmpty()) {
            throw PaymentException.bizError(ERR_CHANNEL_NOT_CONFIGURED + ": 渠道 " + channelCode + " 未配置凭证（mchNo=" + mchNo + "）");
        }
        Map<String, Object> cfg = PayCredentialCryptoUtil.decryptConfigJson(entity.getConfigJson());
        // 校验必填字段
        for (String key : requiredFields()) {
            Object v = cfg.get(key);
            if (v == null || String.valueOf(v).isEmpty()) {
                throw PaymentException.bizError(ERR_CHANNEL_NOT_CONFIGURED + ": 渠道 " + channelCode + " 缺少必填项: " + key);
            }
        }
        log.debug("[{}/loadCredential] mchNo={}, fields={}", channelCode, mchNo, cfg.keySet());
        return cfg;
    }

    /**
     * 工具：构造 mock 响应（无真实 SDK 时使用），调用方可在 raw 中追加凭证标识
     */
    protected Map<String, Object> mockResponse(String channelOrderNo, String prepayInfoOrUrl, Map<String, Object> cfgUsed) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("channelOrderNo", channelOrderNo);
        resp.put("prepayInfo", prepayInfoOrUrl);
        resp.put("payUrl", prepayInfoOrUrl);
        resp.put("status", 0);
        resp.put("raw", "MOCK_PENDING_REAL_SDK");
        if (cfgUsed != null && !cfgUsed.isEmpty()) {
            resp.put("configReady", Boolean.TRUE);
            resp.put("configKeys", cfgUsed.keySet());
        } else {
            resp.put("configReady", Boolean.FALSE);
        }
        return Collections.unmodifiableMap(resp);
    }
}
