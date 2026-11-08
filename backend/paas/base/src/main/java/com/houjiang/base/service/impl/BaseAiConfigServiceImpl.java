package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.houjiang.base.dto.BaseAiConfigDTO;
import com.houjiang.base.dto.BaseAiProviderCredentialDTO;
import com.houjiang.base.entity.BaseAiConfigEntity;
import com.houjiang.base.mapper.BaseAiConfigMapper;
import com.houjiang.base.service.BaseAiConfigService;
import com.houjiang.base.vo.BaseAiConfigVO;
import com.houjiang.base.client.DifyApiClient;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.util.querypage.PageUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * AI服务配置Service实现类
 *
 * @author Amos
 * @since 2026-01-07
 */
@Service
public class BaseAiConfigServiceImpl extends ServiceImpl<BaseAiConfigMapper, BaseAiConfigEntity>
        implements BaseAiConfigService {

    /** AI 配置属于低频变更的系统配置，进程内常驻全量快照避免每次页面加载访问数据库。 */
    private volatile List<BaseAiConfigVO> aiConfigSnapshot;
    private final Object aiConfigSnapshotLock = new Object();

    @org.springframework.beans.factory.annotation.Autowired
    private DifyApiClient difyApiClient;

    @Override
    public ResponseResult<PageUtils<BaseAiConfigVO>> queryAiConfigList(BaseAiConfigDTO param) {
        try {
            BaseAiConfigDTO query = param == null ? new BaseAiConfigDTO() : param;
            List<BaseAiConfigVO> filtered = new ArrayList<>();
            for (BaseAiConfigVO config : getAiConfigSnapshot()) {
                if (!matches(config, query)) continue;
                filtered.add(config);
            }
            int page = normalizePage(query.getPage());
            int limit = normalizeLimit(query.getLimit());
            int start = Math.min((page - 1) * limit, filtered.size());
            int end = Math.min(start + limit, filtered.size());
            return ResponseResult.success(new PageUtils<>(
                    filtered.subList(start, end), filtered.size(), limit, page));
        } catch (Exception e) {
            log.error("查询AI配置列表失败", e);
            return ResponseResult.fail(BizCodeEnume.QUERY_ERROR);
        }
    }

    @Override
    public ResponseResult<List<BaseAiConfigVO>> queryEnabledConfigs() {
        try {
            List<BaseAiConfigVO> list = new ArrayList<>();
            for (BaseAiConfigVO config : getAiConfigSnapshot()) {
                if (Integer.valueOf(1).equals(config.getIsEnabled())
                        && Integer.valueOf(1).equals(config.getStatus())) {
                    list.add(config);
                }
            }
            list.sort(priorityComparator());
            return ResponseResult.success(list);
        } catch (Exception e) {
            log.error("查询启用的AI配置失败", e);
            return ResponseResult.fail(BizCodeEnume.QUERY_ERROR);
        }
    }

    @Override
    public ResponseResult<BaseAiConfigVO> getDefaultConfig() {
        try {
            // 现在支持“每种功能类型各保留一个默认”，可能存在多条 is_default=1，
            // 对外仍返回单条：优先返回文本对话类默认，其次第一条
            List<BaseAiConfigVO> defaults = new ArrayList<>();
            for (BaseAiConfigVO config : getAiConfigSnapshot()) {
                if (Integer.valueOf(1).equals(config.getIsDefault())
                        && Integer.valueOf(1).equals(config.getIsEnabled())
                        && Integer.valueOf(1).equals(config.getStatus())) {
                    defaults.add(config);
                }
            }
            defaults.sort(priorityComparator());
            BaseAiConfigVO config = pickPrimaryDefault(defaults);
            if (config != null) {
                return ResponseResult.success(config);
            }
            return ResponseResult.fail(BizCodeEnume.QUERY_ERROR.getCode(), "未找到默认AI配置");
        } catch (Exception e) {
            log.error("获取默认AI配置失败", e);
            return ResponseResult.fail(BizCodeEnume.QUERY_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult saveOrUpdateConfig(BaseAiConfigDTO param) {
        try {
            if (StringUtils.isEmpty(param.getProviderCode()) || StringUtils.isEmpty(param.getModelName())) {
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER.getCode(), "提供商代码和模型名称不能为空");
            }

            BaseAiConfigEntity existing = param.getId() == null ? null : this.getById(param.getId());
            if (param.getId() != null && existing == null) {
                return ResponseResult.fail(BizCodeEnume.QUERY_ERROR.getCode(), "配置不存在");
            }
            param.setConfigJson(mergeChatLicense(existing == null ? null : existing.getConfigJson(), param.getConfigJson()));

            BaseAiConfigEntity entity = new BaseAiConfigEntity();
            BeanUtils.copyProperties(param, entity);

            boolean success;
            if (param.getId() != null) {
                success = this.updateById(entity);
            } else {
                success = this.save(entity);
            }

            if (success) {
                refreshAiConfigSnapshot();
                return ResponseResult.success(param.getId() != null ? "更新成功" : "添加成功");
            }
            return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
        } catch (Exception e) {
            log.error("保存AI配置失败", e);
            return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult saveProviderCredential(BaseAiProviderCredentialDTO param) {
        try {
            if (param == null || StringUtils.isEmpty(param.getProviderCode())
                    || param.getModels() == null || param.getModels().isEmpty()) {
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER.getCode(), "提供商代码和模型列表不能为空");
            }

            int created = 0;
            int updated = 0;

            // 品牌共享密钥回退：入参未带密钥时，继承同一品牌卡（同 providerCode + 同端点）已有的密钥，
            // 实现“一次配置、全部模型共享”（仅用于新建行，不覆盖已有行的原密钥）
            String fallbackApiKey = param.getApiKey();
            String fallbackApiSecret = param.getApiSecret();
            if (StringUtils.isEmpty(fallbackApiKey)) {
                QueryWrapper<BaseAiConfigEntity> siblingQw = new QueryWrapper<BaseAiConfigEntity>()
                        .eq("provider_code", param.getProviderCode())
                        .isNotNull("api_key")
                        .ne("api_key", "");
                if (!StringUtils.isEmpty(param.getApiEndpoint())) {
                    siblingQw.eq("api_endpoint", param.getApiEndpoint());
                }
                siblingQw.last("LIMIT 1");
                BaseAiConfigEntity sibling = this.getOne(siblingQw);
                if (sibling != null) {
                    fallbackApiKey = sibling.getApiKey();
                    if (StringUtils.isEmpty(fallbackApiSecret) && !StringUtils.isEmpty(sibling.getApiSecret())) {
                        fallbackApiSecret = sibling.getApiSecret();
                    }
                }
            }

            for (BaseAiConfigDTO model : param.getModels()) {
                String providerCode = StringUtils.isEmpty(model.getProviderCode()) ? param.getProviderCode() : model.getProviderCode();
                String modelName = model.getModelName();
                if (StringUtils.isEmpty(providerCode) || StringUtils.isEmpty(modelName)) {
                    continue;
                }

                BaseAiConfigEntity existing = this.getOne(new QueryWrapper<BaseAiConfigEntity>()
                        .eq("provider_code", providerCode)
                        .eq("model_name", modelName)
                        .last("LIMIT 1"));

                BaseAiConfigEntity entity = existing != null ? existing : new BaseAiConfigEntity();

                // 模型模板参数
                entity.setProviderCode(providerCode);
                if (!StringUtils.isEmpty(model.getProviderName())) entity.setProviderName(model.getProviderName());
                entity.setModelName(modelName);
                if (!StringUtils.isEmpty(model.getModelVersion())) entity.setModelVersion(model.getModelVersion());
                if (!StringUtils.isEmpty(model.getDescription())) entity.setDescription(model.getDescription());
                if (model.getMaxTokens() != null) entity.setMaxTokens(model.getMaxTokens());
                if (model.getTemperature() != null) entity.setTemperature(model.getTemperature());
                if (model.getTopP() != null) entity.setTopP(model.getTopP());
                if (model.getTimeout() != null) entity.setTimeout(model.getTimeout());
                if (model.getRetryTimes() != null) entity.setRetryTimes(model.getRetryTimes());
                if (model.getSupportStream() != null) entity.setSupportStream(model.getSupportStream());
                if (model.getSupportFunction() != null) entity.setSupportFunction(model.getSupportFunction());
                if (model.getSupportVision() != null) entity.setSupportVision(model.getSupportVision());
                if (model.getCostPer1kInput() != null) entity.setCostPer1kInput(model.getCostPer1kInput());
                if (model.getCostPer1kOutput() != null) entity.setCostPer1kOutput(model.getCostPer1kOutput());
                if (model.getPriority() != null) entity.setPriority(model.getPriority());
                if (model.getSortOrder() != null) entity.setSortOrder(model.getSortOrder());

                // 端点：品牌端点优先（同一品牌卡下所有模型共享同一端点），品牌端点为空时回退模型自带端点
                String endpoint = !StringUtils.isEmpty(param.getApiEndpoint()) ? param.getApiEndpoint() : model.getApiEndpoint();
                if (!StringUtils.isEmpty(endpoint)) entity.setApiEndpoint(endpoint);

                // 品牌级共享字段
                if (!StringUtils.isEmpty(param.getRegion())) entity.setRegion(param.getRegion());
                if (!StringUtils.isEmpty(param.getAppId())) entity.setAppId(param.getAppId());

                // 密钥：入参优先；入参为空时，新建行继承同品牌共享密钥，已存在行保留原值
                if (!StringUtils.isEmpty(param.getApiKey())) {
                    entity.setApiKey(param.getApiKey());
                } else if (existing == null && !StringUtils.isEmpty(fallbackApiKey)) {
                    entity.setApiKey(fallbackApiKey);
                }
                if (!StringUtils.isEmpty(param.getApiSecret())) {
                    entity.setApiSecret(param.getApiSecret());
                } else if (existing == null && !StringUtils.isEmpty(fallbackApiSecret)) {
                    entity.setApiSecret(fallbackApiSecret);
                }

                // 启用状态按品牌值统一写入
                if (param.getIsEnabled() != null) {
                    entity.setIsEnabled(param.getIsEnabled());
                    entity.setStatus(param.getIsEnabled());
                } else if (existing == null) {
                    entity.setIsEnabled(1);
                    entity.setStatus(1);
                }

                // configJson：品牌级优先，其次模型模板；保护 chatLicense；均为空则保留原值
                String incomingConfigJson = !StringUtils.isEmpty(param.getConfigJson()) ? param.getConfigJson() : model.getConfigJson();
                if (!StringUtils.isEmpty(incomingConfigJson)) {
                    entity.setConfigJson(mergeChatLicense(existing == null ? null : existing.getConfigJson(), incomingConfigJson));
                }

                if (existing != null) {
                    this.updateById(entity);
                    updated++;
                } else {
                    this.save(entity);
                    created++;
                }
            }

            JSONObject result = new JSONObject();
            result.put("created", created);
            result.put("updated", updated);
            refreshAiConfigSnapshot();
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("批量下发品牌AI凭证失败", e);
            return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult deleteConfig(Integer id) {
        try {
            if (id == null) {
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
            }

            boolean success = this.removeById(id);
            if (success) {
                refreshAiConfigSnapshot();
                return ResponseResult.success("删除成功");
            }
            return ResponseResult.fail(BizCodeEnume.DELETE_ERROR);
        } catch (Exception e) {
            log.error("删除AI配置失败", e);
            return ResponseResult.fail(BizCodeEnume.DELETE_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult toggleEnabled(Integer id, Integer isEnabled) {
        try {
            if (id == null || isEnabled == null) {
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
            }

            UpdateWrapper<BaseAiConfigEntity> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", id).set("is_enabled", isEnabled);
            
            boolean success = this.update(wrapper);
            if (success) {
                refreshAiConfigSnapshot();
                return ResponseResult.success(isEnabled == 1 ? "已启用" : "已禁用");
            }
            return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
        } catch (Exception e) {
            log.error("切换AI配置状态失败", e);
            return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult setAsDefault(Integer id) {
        try {
            if (id == null) {
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
            }

            BaseAiConfigEntity target = this.getById(id);
            if (target == null) {
                return ResponseResult.fail(BizCodeEnume.QUERY_ERROR.getCode(), "配置不存在");
            }

            // “每种功能类型各保留一个默认”：仅清除与目标同类型的其它默认，
            // 不影响其它功能类型（数字人/语音/图像等）的默认选择
            String targetCategory = inferModelCategory(target.getModelName(), target.getDescription());
            List<BaseAiConfigEntity> currentDefaults = this.list(new QueryWrapper<BaseAiConfigEntity>()
                    .eq("is_default", 1));
            List<Integer> sameCategoryIds = new ArrayList<>();
            for (BaseAiConfigEntity cfg : currentDefaults) {
                if (cfg.getId() != null && cfg.getId().equals(id)) {
                    continue;
                }
                if (targetCategory.equals(inferModelCategory(cfg.getModelName(), cfg.getDescription()))) {
                    sameCategoryIds.add(cfg.getId());
                }
            }
            if (!sameCategoryIds.isEmpty()) {
                UpdateWrapper<BaseAiConfigEntity> clearWrapper = new UpdateWrapper<>();
                clearWrapper.in("id", sameCategoryIds).set("is_default", 0);
                this.update(clearWrapper);
            }

            // 设置新的默认配置
            UpdateWrapper<BaseAiConfigEntity> setWrapper = new UpdateWrapper<>();
            setWrapper.eq("id", id).set("is_default", 1);
            
            boolean success = this.update(setWrapper);
            if (success) {
                refreshAiConfigSnapshot();
                return ResponseResult.success("已设置为默认配置");
            }
            return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
        } catch (Exception e) {
            log.error("设置默认AI配置失败", e);
            return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
        }
    }

    @Override
    public ResponseResult testConnection(Integer id) {
        try {
            if (id == null) {
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
            }

            BaseAiConfigEntity entity = this.getById(id);
            if (entity == null) {
                return ResponseResult.fail(BizCodeEnume.QUERY_ERROR.getCode(), "配置不存在");
            }

            BaseAiConfigVO vo = new BaseAiConfigVO();
            org.springframework.beans.BeanUtils.copyProperties(entity, vo);
            if (difyApiClient.isDifyConfig(vo)) {
                if (difyApiClient.testConnection(vo)) {
                    return ResponseResult.success("Dify 连接测试成功");
                }
                return ResponseResult.fail("Dify 连接测试失败，请检查 API 地址与 API Key");
            }

            return ResponseResult.success("连接测试成功");
        } catch (Exception e) {
            log.error("测试AI配置连接失败", e);
            return ResponseResult.fail("连接测试失败：" + e.getMessage());
        }
    }

    private List<BaseAiConfigVO> getAiConfigSnapshot() {
        List<BaseAiConfigVO> snapshot = aiConfigSnapshot;
        if (snapshot != null) return snapshot;
        synchronized (aiConfigSnapshotLock) {
            snapshot = aiConfigSnapshot;
            if (snapshot == null) {
                snapshot = loadAiConfigSnapshot();
                aiConfigSnapshot = snapshot;
            }
            return snapshot;
        }
    }

    /** 写操作成功后同步重建，避免下一次页面请求再次击穿数据库。 */
    private void refreshAiConfigSnapshot() {
        synchronized (aiConfigSnapshotLock) {
            aiConfigSnapshot = null;
            try {
                aiConfigSnapshot = loadAiConfigSnapshot();
            } catch (Exception e) {
                log.error("重建AI配置缓存失败，后续读取将重试数据库", e);
            }
        }
    }

    private List<BaseAiConfigVO> loadAiConfigSnapshot() {
        QueryWrapper<BaseAiConfigEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("sort_order").orderByDesc("create_time");
        List<BaseAiConfigEntity> entities = this.list(wrapper);
        List<BaseAiConfigVO> result = new ArrayList<>(entities.size());
        for (BaseAiConfigEntity entity : entities) {
            BaseAiConfigVO config = new BaseAiConfigVO();
            BeanUtils.copyProperties(entity, config);
            config.setApiKeyConfigured(!StringUtils.isEmpty(entity.getApiKey()));
            config.setApiSecretConfigured(!StringUtils.isEmpty(entity.getApiSecret()));
            hideChatLicense(config);
            result.add(config);
        }
        return List.copyOf(result);
    }

    private static boolean matches(BaseAiConfigVO config, BaseAiConfigDTO query) {
        if (!StringUtils.isEmpty(query.getKeyword())) {
            String keyword = query.getKeyword().toLowerCase();
            String providerName = config.getProviderName() == null ? "" : config.getProviderName().toLowerCase();
            String modelName = config.getModelName() == null ? "" : config.getModelName().toLowerCase();
            if (!providerName.contains(keyword) && !modelName.contains(keyword)) return false;
        }
        if (!StringUtils.isEmpty(query.getProviderCode())
                && !query.getProviderCode().equals(config.getProviderCode())) return false;
        if (query.getIsEnabled() != null && !query.getIsEnabled().equals(config.getIsEnabled())) return false;
        return query.getStatus() == null || query.getStatus().equals(config.getStatus());
    }

    private static int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private static int normalizeLimit(Integer limit) {
        return limit == null || limit < 1 ? 20 : limit;
    }

    private static Comparator<BaseAiConfigVO> priorityComparator() {
        return Comparator.comparingInt((BaseAiConfigVO config) -> value(config.getPriority())).reversed()
                .thenComparing(Comparator.comparingInt((BaseAiConfigVO config) -> value(config.getSortOrder())).reversed())
                .thenComparing(Comparator.comparingInt((BaseAiConfigVO config) -> value(config.getId())).reversed());
    }

    private static int value(Integer value) {
        return value == null ? 0 : value;
    }

    private void hideChatLicense(BaseAiConfigVO config) {
        config.setChatLicenseConfigured(hasChatLicense(config.getConfigJson()));
        config.setConfigJson(withoutChatLicense(config.getConfigJson()));
    }

    static boolean hasChatLicense(String configJson) {
        return !license(parseOptions(configJson)).isBlank();
    }

    static String withoutChatLicense(String configJson) {
        JSONObject options = parseOptions(configJson);
        if (options == null) return configJson;
        options.remove("chatLicense");
        return JSON.toJSONString(options);
    }

    static String mergeChatLicense(String storedConfigJson, String incomingConfigJson) {
        JSONObject incoming = parseOptions(incomingConfigJson);
        if (incoming == null) return incomingConfigJson;
        String supplied = license(incoming);
        if (supplied.isBlank()) {
            String stored = license(parseOptions(storedConfigJson));
            if (stored.isBlank()) incoming.remove("chatLicense");
            else incoming.put("chatLicense", stored);
        }
        return JSON.toJSONString(incoming);
    }

    private static JSONObject parseOptions(String configJson) {
        if (StringUtils.isEmpty(configJson)) return new JSONObject();
        try { return JSON.parseObject(configJson); }
        catch (Exception ignored) { return null; }
    }

    private static String license(JSONObject options) {
        if (options == null || options.get("chatLicense") == null) return "";
        return String.valueOf(options.get("chatLicense")).trim();
    }

    /**
     * 从多个默认配置中选出对外的主默认：优先文本对话类（text），否则取第一条
     * @param defaults 当前 is_default=1 且启用的配置列表
     * @return 主默认配置，为空列表时返回 null
     */
    private static BaseAiConfigVO pickPrimaryDefault(List<BaseAiConfigVO> defaults) {
        if (defaults == null || defaults.isEmpty()) return null;
        for (BaseAiConfigVO cfg : defaults) {
            if ("text".equals(inferModelCategory(cfg.getModelName(), cfg.getDescription()))) {
                return cfg;
            }
        }
        return defaults.get(0);
    }

    /**
     * 推断模型能力分类（与前端 aiProviderTemplates.js 的 inferModelCategory 保持一致的正则与顺序）
     * @param modelName 模型名称
     * @param description 模型描述
     * @return 分类标识：avatar/3d/video/asr/tts/ocr/image/embedding/risk/industry/vision/text
     */
    static String inferModelCategory(String modelName, String description) {
        String name = modelName == null ? "" : modelName.toLowerCase();
        String desc = description == null ? "" : description.toLowerCase();
        String text = name + " " + desc;

        if (matches(text, "digital[ -]?human|avatar|virtual[ -]?human|\u6570\u5b57\u4eba|\u6570\u667a\u4eba")) return "avatar";
        if (matches(text, "3d|tripo|mesh|\u4e09\u7ef4|3-d")) return "3d";
        if (matches(text, "remotion|hyperframes|\u6210\u7247\u6e32\u67d3|render.?service")) return "video";
        if (matches(text, "video|\u56fe\u751f\u89c6\u9891|cogvideo|kling|sora|gen-?3|animate|i2v|t2v|\u89c6\u9891\u751f\u6210")) return "video";
        if (matches(text, "asr|whisper|paraformer|speech.?recog|\u8bed\u97f3\u8bc6\u522b|\u542c\u5199|speech-to-text")) return "asr";
        if (matches(text, "tts|cosyvoice|speech.?synth|\u8bed\u97f3\u5408\u6210|text-to-speech|\u97f3\u9891\u5408\u6210")) return "tts";
        if (matches(text, "ocr|document|doc-|\u6587\u6863\u5904\u7406|\u6587\u5b57\u8bc6\u522b|pdf")) return "ocr";
        if (matches(text, "wanx|t2i|i2i|dall-?e|stable.?diff|\u6587\u751f\u56fe|\u56fe\u50cf\u751f\u6210|image.?gen|\u56fe\u751f\u56fe")) return "image";
        if (matches(text, "embed|embedding|rerank|\u5411\u91cf|\u91cd\u6392|text-embedding")) return "embedding";
        if (matches(text, "risk|moderation|content.?safe|\u98ce\u63a7|\u5185\u5bb9\u5ba1\u6838|\u5b89\u5168")) return "risk";
        if (matches(text, "industry|\u91d1\u878d|\u533b\u7597|\u6cd5\u5f8b|\u884c\u4e1a\u5927\u6a21\u578b|vertical")) return "industry";
        if (matches(text, "vl-|qwen-vl|glm-4v|gpt-4o|gpt-5(?:\\.\\d+)?[- ]?(?:sol|terra|luna)?|vision|multimodal|\u89c6\u89c9\u7406\u89e3|\u56fe\u50cf\u7406\u89e3|\u89c6\u89c9\u6a21\u578b")) return "vision";
        return "text";
    }

    private static boolean matches(String text, String regex) {
        return Pattern.compile(regex).matcher(text).find();
    }
}
