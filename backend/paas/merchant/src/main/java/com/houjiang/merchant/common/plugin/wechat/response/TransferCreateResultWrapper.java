package com.houjiang.merchant.common.plugin.wechat.response;

import com.github.binarywang.wxpay.bean.merchanttransfer.TransferCreateResult;
import lombok.Data;

import java.io.Serializable;

/**
 * 转账创建结果包装类
 * 包含原有的TransferCreateResult和完整的响应JSON
 * @author Amos
 */
@Data
public class TransferCreateResultWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 原有的转账创建结果 */
    private TransferCreateResult transferCreateResult;

    /** 完整的响应JSON字符串 */
    private String responseBody;

    public TransferCreateResultWrapper() {}

    public TransferCreateResultWrapper(TransferCreateResult transferCreateResult, String responseBody) {
        this.transferCreateResult = transferCreateResult;
        this.responseBody = responseBody;
    }

    /**
     * 为了向后兼容，提供一些常用方法的代理
     */
    public String getOutBatchNo() {
        return transferCreateResult != null ? transferCreateResult.getOutBatchNo() : null;
    }

    public String getBatchId() {
        return transferCreateResult != null ? transferCreateResult.getBatchId() : null;
    }

    public String getCreateTime() {
        return transferCreateResult != null ? transferCreateResult.getCreateTime() : null;
    }

    public String getBatchStatus() {
        return transferCreateResult != null ? transferCreateResult.getBatchStatus() : null;
    }
}