package com.houjiang.merchant.common.plugin.wechat.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class TransferRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 支付终端 */
    private Integer terminal;

    /** 商家批次单号 */
    private String outBatchNo;

    /** 转账金额单位为“分” 转账总金额必须与批次内所有明细转账金额之和保持一致，否则无法发起转账操作 */
    private Integer totalAmount;

    /** 一个转账批次单最多发起三千笔转账。转账总笔数必须与批次内所有明细之和保持一致，否则无法发起转账操作 */
    private Integer totalNum;

    @Schema(description = "转账明细列表")
    private List<detailList> detailList;

    @Data
    public static class detailList {
        /** 用户openid*/
        private String openid;

        /** 收款用户姓名 (超过2000元，此字段必须) */
        private String userName;

        /** 商户系统内部区分转账批次单下不同转账明细单的唯一标识 */
        private String outDetailNo;

        /** 单条转账备注（微信用户会收到该备注），UTF8编码，最多允许32个字符 */
        private String transferRemark;

        /** 转账金额单位为分 */
        private Integer transferAmount;
    }
}
