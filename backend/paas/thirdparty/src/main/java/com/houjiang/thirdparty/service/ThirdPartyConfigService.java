package com.houjiang.thirdparty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.thirdparty.entity.ThirdPartyConfigEntity;

import java.util.List;
import java.util.Map;

/**
 * 第三方集成配置Service接口
 * 
 * @author Amos
 * @since 2025-01-07
 */
public interface ThirdPartyConfigService extends IService<ThirdPartyConfigEntity> {

    /**
     * 归属「统一登录（UniLogin）」的第三方平台键。
     * 只有这些平台的配置允许在无登录态时被 auth 服务读取（见 LoginChannelConfigController）。
     * 电子签 / 语雀等与登录无关的平台一律不在其列，避免匿名接口扩大凭据读取面。
     */
    List<String> LOGIN_CHANNEL_PLATFORMS = List.of(
            "wechat-login", "wechat-work", "dingtalk", "feishu", "qq-login"
    );

    /**
     * 根据平台获取配置
     * 
     * @param platform 平台名称
     * @return 配置信息
     */
    ThirdPartyConfigDTO getConfigByPlatform(String platform);

    /** 按组织读取（OA 考勤同步等内部调用须传 orgId，避免无登录上下文读错租户） */
    /**
     * TODO 补充方法说明
     * @param platform TODO
     * @param orgId TODO
     * @return TODO
     */
    ThirdPartyConfigDTO getConfigByPlatform(String platform, String orgId);

    /**
     * 一次查询当前组织下全部第三方平台配置（设置页批量加载，避免多次 Feign/DB 往返）
     */
    Map<String, ThirdPartyConfigDTO> getAllConfigs(String orgId);

    /**
     * 解析当前组织的企业微信凭证；无法确定组织或组织未配置时返回 null。
     *
     * @param orgId 组织 ID，可为空
     * @return 已配置凭证，未找到时返回 null
     */
    ThirdPartyConfigDTO.WechatWorkConfig resolveWechatWorkConfig(String orgId);

    /**
     * 读取登录渠道凭据（统一登录 UniLogin 专用）。
     *
     * <p>与 {@link #getConfigByPlatform(String)} 的差别：登录发生在<b>未登录态</b>，
     * 请求里没有组织上下文，因此这里不依赖 orgId，而是直接取该平台下
     * 「最近更新且凭据填写完整」的那条记录（与 {@code findAnyConfiguredWechatWork} 同思路）。
     * 单站点部署即单租户，这样才不会出现「设置页填了、登录页却说未开通」。</p>
     *
     * @param platform 平台键，仅限登录类平台（wechat-login / wechat-work / dingtalk / feishu / qq-login）
     * @return 配置；平台不属于登录类、或没有任何已填凭据的记录时返回 null
     */
    ThirdPartyConfigDTO getLoginChannelConfig(String platform);

    /**
     * 保存平台配置
     * 
     * @param platform 平台名称
     * @param config 配置信息
     * @return 是否成功
     */
    boolean saveConfig(String platform, ThirdPartyConfigDTO config);

    /**
     * 测试平台连接
     * 
     * @param platform 平台名称
     * @param config 配置信息
     * @return 是否成功
     */
    boolean testConnection(String platform, ThirdPartyConfigDTO config);

    /**
     * 删除平台配置
     * 
     * @param platform 平台名称
     * @return 是否成功
     */
    boolean deleteConfig(String platform);

    /**
     * 读取平台原始 JSON 配置
     */
    String getConfigJsonByPlatform(String platform);

    /**
     * TODO 补充方法说明
     * @param platform TODO
     * @param orgId TODO
     * @return TODO
     */
    String getConfigJsonByPlatform(String platform, String orgId);

    /**
     * 保存平台原始 JSON 配置
     */
    boolean saveJsonConfig(String platform, String configName, Boolean enabled, String description, String configJson);
}
