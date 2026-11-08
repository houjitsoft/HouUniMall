package com.houjiang.payment.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.payment.dto.channel.ChannelListRequest;
import com.houjiang.payment.dto.channel.ChannelSaveRequest;
import com.houjiang.payment.entity.PayChannelEntity;

import java.util.List;
import java.util.Map;

/**
 * 支付渠道配置服务
 * @author Amos
 */
public interface ChannelService {

    /**
     * @param req 请求
     * @param operatorId operator主键
     * @return 处理结果
     */
    Long save(ChannelSaveRequest req, String operatorId);

    /**
     * @param id 主键
     * @param status 状态
     * @param operatorId operator主键
     * @return 处理结果
     */
    boolean changeStatus(Long id, Integer status, String operatorId);

    /**
     * @param id 主键
     * @return 处理结果
     */
    boolean delete(Long id);

    /**
     * @param req 请求
     * @return 处理结果
     */
    IPage<PayChannelEntity> page(ChannelListRequest req);

    /**
     * @param id 主键
     * @return 处理结果
     */
    PayChannelEntity detail(Long id);

    /**
     * @param channelCode 渠道编码
     * @return 处理结果
     */
    PayChannelEntity getByCode(String channelCode);

    /**
     * 保存凭证：configMap 中的敏感字段会自动 AES 加密后落库
     */
    Long saveCredential(ChannelSaveRequest req, Map<String, Object> configMap, String operatorId);

    /**
     * 查询凭证（解密返回明文，仅供管理员编辑）
     */
    Map<String, Object> getCredentialDecrypted(Long id);

    /**
     * 列表（敏感字段统一替换为 ***）
     */
    IPage<PayChannelEntity> pageDesensitized(ChannelListRequest req);

    /**
     * 测试连通：依据 channelCode 路由 Adapter，无凭证返回 CHANNEL_NOT_CONFIGURED
     */
    Map<String, Object> testConnect(Long id);

    /**
     * 公开版渠道配置：仅返回 status=1 的启用渠道，且 configJson 仅暴露公开字段
     * （appId/mchId/certSerialNo/merchId/partnerId/sandbox/notifyUrl/jumpUrl 等），
     * 敏感字段（privateKey/apiV3Key/appSecret/signKey/alipayPublicKey 等）一律剔除。
     * 供 C 端 uniapp 收银台初始化各支付渠道使用。
     */
    List<Map<String, Object>> listEnabledPublic(String payMethod);
}
