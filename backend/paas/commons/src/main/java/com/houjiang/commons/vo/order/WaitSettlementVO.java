package com.houjiang.commons.vo.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName WaitSettlementVO
 * @Description
 * @Author Amos
 * @Date 2023/7/18 10:20
 * @Version 1.0
 * @author Amos
 */
@Data
public class WaitSettlementVO {

    /**
     * 平台应付金额详情
     */
    private String osAmountStr;

    /**
     * 上次结算时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beformDate;

    /**
     * 平台应付金额
     */
    private String osAmount;

    /**
     * 起止时间
     */
    private String date;

    /**
     * 上次结算时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date osDatetime;

    /**
     * 平台应付金额
     */
    private String osId;

    /**
     * 平台应付金额
     */
    private String osState;
}
