package com.houjiang.payment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.payment.dto.merchantQual.MerchantQualQueryRequest;
import com.houjiang.payment.dto.merchantQual.MerchantQualSaveRequest;
import com.houjiang.payment.dto.merchantQual.MerchantQualVO;
import com.houjiang.payment.entity.PayMerchantQualEntity;

/**
 * 商户资质 Service
 *
 * @author Amos
 * @since 2026-05-13
 */
public interface PayMerchantQualService {

    /**
     * 分页查询
     *
     * @param request 查询请求
     * @return 分页 VO
     */
    IPage<MerchantQualVO> page(MerchantQualQueryRequest request);

    /**
     * 详情
     *
     * @param id              主键
     * @param plainSensitive  是否解密敏感字段返回明文（编辑场景=true，列表场景=false）
     * @return VO
     */
    MerchantQualVO detail(Long id, boolean plainSensitive);

    /**
     * 新增/编辑（id 为空=新增；非空=编辑）
     *
     * @param request 表单
     * @return 主键
     */
    Long save(MerchantQualSaveRequest request);

    /** 智能入库专用：仅新增禁用资质，且使用服务端确定的租户和创建人。 */
    Long importDisabled(MerchantQualSaveRequest request, Long tenantId, String creatorId);

    /**
     * 启用/禁用
     */
    boolean toggleStatus(Long id, Integer status);

    /**
     * 逻辑删除
     */
    boolean delete(Long id);

    /**
     * 支付 Handler 调用：按 terminal/channel/env 拿启用中的资质（解密敏感字段返回明文）
     *
     * @param terminal 终端
     * @param channel  渠道
     * @param env      环境（S/P；为空默认 P）
     * @return 资质实体（敏感字段已解密；找不到返回 null）
     */
    PayMerchantQualEntity getByTerminalChannel(String terminal, String channel, String env);
}
