package com.houjiang.payment.dto.mch;

import com.houjiang.payment.entity.PayMerchantEntity;
import com.houjiang.payment.entity.PayMerchantFeeEntity;
import com.houjiang.payment.entity.PayMerchantProductEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 平台商户详情聚合 VO
 * 包含主表信息 + 已开通产品 + 费率列表
 * @author Amos
 */
@Data
public class MerchantDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private PayMerchantEntity merchant;

    private List<PayMerchantProductEntity> products;

    private List<PayMerchantFeeEntity> fees;
}
