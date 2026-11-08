package com.houjiang.payment.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消费提款
 * @author Amos
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ConsumeWithdrawDTO extends UserTokenBeanDTO {

    /**
     * id
     */
    private Integer id;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 结算单号
     */
    private String statementNo;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * null 查询全部 0 待审核 1 进行中 2 打款中 3 通过 4 不通过
     */
    private Integer type;

}
