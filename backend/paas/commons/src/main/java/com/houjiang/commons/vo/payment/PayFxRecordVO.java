package com.houjiang.commons.vo.payment;


import com.houjiang.commons.util.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayFxRecordVO {
    private String recordId;

    private String userId;

    private String userName;

    private BigDecimal recordAmount;

    private BigDecimal beforeRecordAmount;

    private BigDecimal afterRecordAmount;

    private Integer recordStatus;

    private Integer tradeTypeId;

    private Integer recordType;

    private Integer plusOrMinus;

    private Integer payCode;

    private Integer payFrom;

    private String aliUserId;

    private String aliUserName;

    private String wxOpenId;

    private String wxUserName;

    private String recordName;

    private String recordStatusName;

    private String recordDate;

    private String createTime;

    private String updateTime;

    public PayFxRecordVO(String recordId, Integer recordStatus, String recordStatusName) {
        this.recordId = recordId;
        this.recordStatus = recordStatus;
        this.recordStatusName = recordStatusName;
        this.updateTime = DateUtils.getNowDateToString();
    }

}