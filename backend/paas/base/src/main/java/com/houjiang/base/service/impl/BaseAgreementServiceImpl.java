package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.entity.BaseAgreementEntity;
import com.houjiang.base.entity.BaseAgreementVersionEntity;
import com.houjiang.base.entity.BaseSysConfigsEntity;
import com.houjiang.base.mapper.BaseAgreementMapper;
import com.houjiang.base.mapper.BaseAgreementVersionMapper;
import com.houjiang.base.mapper.BaseSysConfigsMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.houjiang.base.service.BaseAgreementService;
import com.houjiang.base.vo.AgreementVO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 协议定义与版本管理ServiceImpl。
 *
 * @author Amos
 */
@Slf4j
@Service
public class BaseAgreementServiceImpl extends ServiceImpl<BaseAgreementMapper, BaseAgreementEntity>
        implements BaseAgreementService {

    /** 版本状态：草稿 */
    private static final int VERSION_DRAFT = 0;
    /** 版本状态：已发布 */
    private static final int VERSION_PUBLISHED = 1;
    /** 版本状态：已下线 */
    private static final int VERSION_OFFLINE = 2;

    @Autowired
    private BaseAgreementVersionMapper versionMapper;

    @Autowired
    private BaseSysConfigsMapper baseSysConfigsMapper;

    // ================= 注册侧 =================

    @Override
    public ResponseResult listByScene(Map<String, Object> param) {
        try {
            String scene = asStr(param, "scene", "register");
            List<BaseAgreementEntity> agreements = listEnabledByScene(scene);
            if (agreements.isEmpty()) {
                return ResponseResult.success(Collections.emptyList());
            }
            List<Integer> ids = agreements.stream().map(BaseAgreementEntity::getId).collect(Collectors.toList());
            Map<Integer, BaseAgreementVersionEntity> latest = latestPublishedByAgreementIds(ids);

            List<AgreementVO> result = new ArrayList<>(agreements.size());
            for (BaseAgreementEntity a : agreements) {
                result.add(toVO(a, latest.get(a.getId())));
            }
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("查询场景协议失败, param=" + param, e);
            return ResponseResult.error("查询场景协议失败");
        }
    }

    @Override
    public ResponseResult latestVersions(Map<String, Object> param) {
        try {
            List<String> codes = splitCodes(asStr(param, "codes", ""));
            if (codes.isEmpty()) {
                return ResponseResult.success(Collections.emptyMap());
            }
            String scene = asStr(param, "scene", "");
            LambdaQueryWrapper<BaseAgreementEntity> aq = new LambdaQueryWrapper<>();
            aq.in(BaseAgreementEntity::getCode, codes);
            if (!scene.isEmpty()) {
                aq.eq(BaseAgreementEntity::getScene, scene);
            }
            List<BaseAgreementEntity> agreements = this.list(aq);
            if (agreements.isEmpty()) {
                return ResponseResult.success(Collections.emptyMap());
            }
            List<Integer> ids = agreements.stream().map(BaseAgreementEntity::getId).collect(Collectors.toList());
            Map<Integer, BaseAgreementVersionEntity> latest = latestPublishedByAgreementIds(ids);

            Map<String, Object> result = new LinkedHashMap<>();
            for (BaseAgreementEntity a : agreements) {
                BaseAgreementVersionEntity v = latest.get(a.getId());
                Map<String, Object> item = new HashMap<>(6);
                item.put("code", a.getCode());
                item.put("name", a.getName());
                item.put("scene", a.getScene());
                item.put("required", a.getRequired());
                item.put("status", a.getStatus());
                if (v != null) {
                    item.put("versionNo", v.getVersionNo());
                    item.put("versionId", v.getId());
                }
                result.put(a.getCode(), item);
            }
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("查询协议生效版本失败, param=" + param, e);
            return ResponseResult.error("查询协议生效版本失败");
        }
    }

    // ================= 后台：协议定义 =================

    @Override
    public ResponseResult adminPage(Map<String, Object> param) {
        try {
            int page = asInt(param, "page", 1);
            int limit = asInt(param, "limit", 10);
            LambdaQueryWrapper<BaseAgreementEntity> qw = new LambdaQueryWrapper<>();
            String code = asStr(param, "code", "");
            String name = asStr(param, "name", "");
            String scene = asStr(param, "scene", "");
            if (!code.isEmpty()) {
                qw.like(BaseAgreementEntity::getCode, code);
            }
            if (!name.isEmpty()) {
                qw.like(BaseAgreementEntity::getName, name);
            }
            if (!scene.isEmpty()) {
                qw.eq(BaseAgreementEntity::getScene, scene);
            }
            Object status = param == null ? null : param.get("status");
            if (status != null && !String.valueOf(status).isEmpty()) {
                qw.eq(BaseAgreementEntity::getStatus, asInt(param, "status", 1));
            }
            qw.orderByAsc(BaseAgreementEntity::getSort).orderByAsc(BaseAgreementEntity::getId);

            Page<BaseAgreementEntity> pageObj = this.page(new Page<>(page, limit), qw);
            return ResponseResult.success(PageUtils.getPage(pageObj));
        } catch (Exception e) {
            log.error("分页查询协议定义失败, param=" + param, e);
            return ResponseResult.error("分页查询协议定义失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult adminSave(Map<String, Object> param) {
        try {
            if (param == null) {
                return ResponseResult.error("参数不能为空");
            }
            String code = asStr(param, "code", "");
            String name = asStr(param, "name", "");
            if (code.isEmpty()) {
                return ResponseResult.error("协议编码不能为空");
            }
            if (name.isEmpty()) {
                return ResponseResult.error("协议名称不能为空");
            }
            String scene = asStr(param, "scene", "register");

            BaseAgreementEntity entity = new BaseAgreementEntity();
            Integer id = asIntOrNull(param, "id");
            // 同一场景内编码唯一
            LambdaQueryWrapper<BaseAgreementEntity> dup = new LambdaQueryWrapper<>();
            dup.eq(BaseAgreementEntity::getCode, code).eq(BaseAgreementEntity::getScene, scene);
            if (id != null) {
                dup.ne(BaseAgreementEntity::getId, id);
            }
            if (this.count(dup) > 0) {
                return ResponseResult.error("同场景下协议编码已存在：" + code);
            }
            if (id != null) {
                entity.setId(id);
            }
            entity.setCode(code);
            entity.setName(name);
            entity.setScene(scene);
            entity.setRequired(asInt(param, "required", 1));
            entity.setDefaultChecked(asInt(param, "defaultChecked", 0));
            entity.setSort(asInt(param, "sort", 0));
            entity.setStatus(asInt(param, "status", 1));
            entity.setRemark(asStr(param, "remark", ""));
            entity.setUpdateTime(new Date());

            if (entity.getId() == null) {
                entity.setCreateTime(new Date());
                this.save(entity);
            } else {
                this.updateById(entity);
            }
            return ResponseResult.success(entity.getId());
        } catch (Exception e) {
            log.error("保存协议定义失败, param=" + param, e);
            return ResponseResult.error("保存协议定义失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult adminDelete(Map<String, Object> param) {
        try {
            Integer id = asIntOrNull(param, "id");
            if (id == null) {
                return ResponseResult.error("ID不能为空");
            }
            BaseAgreementEntity entity = this.getById(id);
            if (entity == null) {
                return ResponseResult.error("协议不存在");
            }
            LambdaQueryWrapper<BaseAgreementVersionEntity> vq = new LambdaQueryWrapper<>();
            vq.eq(BaseAgreementVersionEntity::getAgreementId, id);
            versionMapper.delete(vq);
            this.removeById(id);
            return ResponseResult.success("协议【" + entity.getName() + "】及其全部版本已删除，历史同意记录保留");
        } catch (Exception e) {
            log.error("删除协议定义失败, param=" + param, e);
            return ResponseResult.error("删除协议定义失败");
        }
    }

    // ================= 后台：协议版本 =================

    @Override
    public ResponseResult versionList(Map<String, Object> param) {
        try {
            LambdaQueryWrapper<BaseAgreementVersionEntity> qw = new LambdaQueryWrapper<>();
            Integer agreementId = asIntOrNull(param, "agreementId");
            if (agreementId != null) {
                qw.eq(BaseAgreementVersionEntity::getAgreementId, agreementId);
            } else {
                String code = asStr(param, "code", "");
                if (code.isEmpty()) {
                    return ResponseResult.error("协议ID或编码不能为空");
                }
                qw.eq(BaseAgreementVersionEntity::getAgreementCode, code);
            }
            qw.orderByDesc(BaseAgreementVersionEntity::getEffectiveTime)
                    .orderByDesc(BaseAgreementVersionEntity::getId);
            return ResponseResult.success(versionMapper.selectList(qw));
        } catch (Exception e) {
            log.error("查询协议版本失败, param=" + param, e);
            return ResponseResult.error("查询协议版本失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult versionSave(Map<String, Object> param) {
        try {
            if (param == null) {
                return ResponseResult.error("参数不能为空");
            }
            Integer agreementId = asIntOrNull(param, "agreementId");
            String versionNo = asStr(param, "versionNo", "");
            String content = asStr(param, "content", "");
            if (agreementId == null) {
                return ResponseResult.error("所属协议不能为空");
            }
            if (versionNo.isEmpty()) {
                return ResponseResult.error("版本号不能为空");
            }
            if (content.isEmpty()) {
                return ResponseResult.error("协议正文不能为空");
            }
            BaseAgreementEntity agreement = this.getById(agreementId);
            if (agreement == null) {
                return ResponseResult.error("所属协议不存在");
            }

            Integer id = asIntOrNull(param, "id");
            LambdaQueryWrapper<BaseAgreementVersionEntity> dup = new LambdaQueryWrapper<>();
            dup.eq(BaseAgreementVersionEntity::getAgreementId, agreementId)
                    .eq(BaseAgreementVersionEntity::getVersionNo, versionNo);
            if (id != null) {
                dup.ne(BaseAgreementVersionEntity::getId, id);
            }
            if (versionMapper.selectCount(dup) > 0) {
                return ResponseResult.error("该协议下版本号已存在：" + versionNo);
            }

            BaseAgreementVersionEntity entity = new BaseAgreementVersionEntity();
            if (id != null) {
                entity.setId(id);
                BaseAgreementVersionEntity old = versionMapper.selectById(id);
                if (old != null && Integer.valueOf(VERSION_PUBLISHED).equals(old.getStatus())) {
                    return ResponseResult.error("已发布版本不可直接修改，请先下线或新增版本");
                }
            }
            entity.setAgreementId(agreementId);
            entity.setAgreementCode(agreement.getCode());
            entity.setVersionNo(versionNo);
            entity.setTitle(asStr(param, "title", agreement.getName()));
            entity.setContent(content);
            entity.setChangeNote(asStr(param, "changeNote", ""));
            entity.setEffectiveTime(parseDate(param.get("effectiveTime")));
            if (entity.getEffectiveTime() == null) {
                entity.setEffectiveTime(new Date());
            }
            if (id == null) {
                entity.setStatus(VERSION_DRAFT);
                entity.setCreateBy(asStr(param, "createBy", "admin"));
                entity.setCreateTime(new Date());
                versionMapper.insert(entity);
            } else {
                versionMapper.updateById(entity);
            }
            return ResponseResult.success(entity.getId());
        } catch (Exception e) {
            log.error("保存协议版本失败, param=" + param, e);
            return ResponseResult.error("保存协议版本失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult versionPublish(Map<String, Object> param) {
        try {
            Integer id = asIntOrNull(param, "id");
            if (id == null) {
                return ResponseResult.error("ID不能为空");
            }
            BaseAgreementVersionEntity entity = versionMapper.selectById(id);
            if (entity == null) {
                return ResponseResult.error("协议版本不存在");
            }
            if (entity.getContent() == null || entity.getContent().trim().isEmpty()) {
                return ResponseResult.error("协议正文为空，不可发布");
            }
            Date now = new Date();
            entity.setStatus(VERSION_PUBLISHED);
            entity.setPublishTime(now);
            // 发布即生效：草稿可能建于数日前，若沿用当时的 effective_time，
            // 会比「上一次发布」的生效时间更早，导致「最新版本」判定取到旧版本。
            // 仅在未来时间（预发布）时保留管理员显式设置的生效时间。
            if (entity.getEffectiveTime() == null || entity.getEffectiveTime().before(now)) {
                entity.setEffectiveTime(now);
            }
            versionMapper.updateById(entity);
            return ResponseResult.success("版本 " + entity.getVersionNo() + " 已发布，未同意该版本的用户将需要重新同意");
        } catch (Exception e) {
            log.error("发布协议版本失败, param=" + param, e);
            return ResponseResult.error("发布协议版本失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult versionOffline(Map<String, Object> param) {
        try {
            Integer id = asIntOrNull(param, "id");
            if (id == null) {
                return ResponseResult.error("ID不能为空");
            }
            BaseAgreementVersionEntity entity = versionMapper.selectById(id);
            if (entity == null) {
                return ResponseResult.error("协议版本不存在");
            }
            entity.setStatus(VERSION_OFFLINE);
            versionMapper.updateById(entity);
            return ResponseResult.success("版本 " + entity.getVersionNo() + " 已下线");
        } catch (Exception e) {
            log.error("下线协议版本失败, param=" + param, e);
            return ResponseResult.error("下线协议版本失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult versionDelete(Map<String, Object> param) {
        try {
            Integer id = asIntOrNull(param, "id");
            if (id == null) {
                return ResponseResult.error("ID不能为空");
            }
            BaseAgreementVersionEntity entity = versionMapper.selectById(id);
            if (entity == null) {
                return ResponseResult.error("协议版本不存在");
            }
            if (Integer.valueOf(VERSION_PUBLISHED).equals(entity.getStatus())) {
                return ResponseResult.error("已发布版本不可删除，请先下线");
            }
            versionMapper.deleteById(id);
            return ResponseResult.success("删除成功");
        } catch (Exception e) {
            log.error("删除协议版本失败, param=" + param, e);
            return ResponseResult.error("删除协议版本失败");
        }
    }

    @Override
    public ResponseResult persistSiteEula(Map<String, Object> param) {
        try {
            String version = asStr(param, "acceptedEulaVersion", "");
            String time = asStr(param, "acceptedEulaTime", "");
            if (version.isEmpty()) {
                return ResponseResult.error("acceptedEulaVersion 不能为空");
            }
            if (time.isEmpty()) {
                time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            }
            upsertSysConfig("accepted_eula_version", version, "本站点已接受的开源 EULA 版本号");
            upsertSysConfig("accepted_eula_time", time, "本站点接受开源 EULA 的时间");
            Map<String, Object> data = new LinkedHashMap<>(4);
            data.put("acceptedEulaVersion", version);
            data.put("acceptedEulaTime", time);
            return ResponseResult.success(data);
        } catch (Exception e) {
            log.error("写入站点 EULA 标记失败, param=" + param, e);
            return ResponseResult.error("写入站点 EULA 标记失败");
        }
    }

    private void upsertSysConfig(String key, String value, String comment) {
        BaseSysConfigsEntity existing = baseSysConfigsMapper.selectOne(
                new QueryWrapper<BaseSysConfigsEntity>().eq("config_key", key));
        if (existing == null) {
            BaseSysConfigsEntity config = new BaseSysConfigsEntity();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigComment(comment);
            config.setConfigType("1");
            config.setConfigEnable(1);
            config.setOpType("");
            config.setOpValue("");
            baseSysConfigsMapper.insert(config);
        } else {
            baseSysConfigsMapper.update(null,
                    new UpdateWrapper<BaseSysConfigsEntity>()
                            .eq("config_key", key)
                            .set("config_value", value));
        }
    }

    // ================= 内部工具 =================

    /**
     * 查询场景下启用中的协议定义
     */
    private List<BaseAgreementEntity> listEnabledByScene(String scene) {
        LambdaQueryWrapper<BaseAgreementEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(BaseAgreementEntity::getScene, scene)
                .eq(BaseAgreementEntity::getStatus, 1)
                .orderByAsc(BaseAgreementEntity::getSort)
                .orderByAsc(BaseAgreementEntity::getId);
        return this.list(qw);
    }

    /**
     * 批量取每个协议的最新已发布版本。
     *
     * <p>生效判定：status=1（已发布），按 effective_time 倒序、id 倒序取第一条。
     * 已下线（status=2）与草稿（status=0）不参与生效。</p>
     */
    private Map<Integer, BaseAgreementVersionEntity> latestPublishedByAgreementIds(List<Integer> agreementIds) {
        Map<Integer, BaseAgreementVersionEntity> result = new LinkedHashMap<>();
        if (agreementIds == null || agreementIds.isEmpty()) {
            return result;
        }
        LambdaQueryWrapper<BaseAgreementVersionEntity> qw = new LambdaQueryWrapper<>();
        qw.in(BaseAgreementVersionEntity::getAgreementId, agreementIds)
                .eq(BaseAgreementVersionEntity::getStatus, VERSION_PUBLISHED)
                .orderByDesc(BaseAgreementVersionEntity::getEffectiveTime)
                .orderByDesc(BaseAgreementVersionEntity::getId);
        List<BaseAgreementVersionEntity> list = versionMapper.selectList(qw);
        for (BaseAgreementVersionEntity v : list) {
            result.putIfAbsent(v.getAgreementId(), v);
        }
        return result;
    }

    private AgreementVO toVO(BaseAgreementEntity a, BaseAgreementVersionEntity v) {
        AgreementVO vo = new AgreementVO();
        vo.setId(a.getId());
        vo.setCode(a.getCode());
        vo.setName(a.getName());
        vo.setScene(a.getScene());
        vo.setRequired(a.getRequired());
        vo.setDefaultChecked(a.getDefaultChecked());
        vo.setSort(a.getSort());
        vo.setStatus(a.getStatus());
        if (v != null) {
            vo.setVersionId(v.getId());
            vo.setVersionNo(v.getVersionNo());
            vo.setTitle(v.getTitle());
            vo.setContent(v.getContent());
            vo.setChangeNote(v.getChangeNote());
            vo.setEffectiveTime(v.getEffectiveTime());
        }
        return vo;
    }

    private List<String> splitCodes(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> codes = new ArrayList<>();
        for (String s : raw.split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) {
                codes.add(t);
            }
        }
        return codes;
    }

    private String asStr(Map<String, Object> param, String key, String defaultValue) {
        if (param == null) {
            return defaultValue;
        }
        Object v = param.get(key);
        if (v == null) {
            return defaultValue;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? defaultValue : s;
    }

    private int asInt(Map<String, Object> param, String key, int defaultValue) {
        Integer v = asIntOrNull(param, key);
        return v == null ? defaultValue : v;
    }

    private Integer asIntOrNull(Map<String, Object> param, String key) {
        if (param == null) {
            return null;
        }
        Object v = param.get(key);
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
            return null;
        }
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析时间：支持 Date 与 yyyy-MM-dd HH:mm:ss 字符串
     */
    private Date parseDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
            return null;
        }
        String[] patterns = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd"};
        for (String p : patterns) {
            try {
                return new SimpleDateFormat(p).parse(s);
            } catch (Exception ignored) {
                // 继续尝试下一个格式
            }
        }
        return null;
    }
}
