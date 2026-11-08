package com.houjiang.payment.dto.mch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 平台商户状态变更请求 DTO
 * 对应动作：审核通过/驳回/冻结/解冻/注销
 * @author Amos
 */
@Data
public class MerchantStatusChangeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "商户号不能为空")
    private String mchNo;

    /** 目标状态：0-待审核 1-审核中 2-已生效 3-已冻结 4-已注销 5-审核失败 */
    @NotNull(message = "目标状态不能为空")
    private Integer status;

    /** 备注（状态变更说明） */
    private String remark;
}
