package com.houjiang.merchant.common.plugin.wechat.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class TransferQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 终端 */
    private Integer terminal;

    /** 商家批次单号 */
    private String outBatchNo;

    /** 商家明细单号 */
    private String outDetailNo;

}
