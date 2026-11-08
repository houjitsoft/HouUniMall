package com.houjiang.mall.service.rebate;

import com.houjiang.mall.entity.rebate.RebatePayment;

/**
 * 返利发放通道。
 *
 * <p>为什么要有这层：返利涉及真金白银出账，早期实现里 {@code execute()} 只是把状态直接改成了
 * 「已发放」并拼了一个 {@code TXN+时间戳} 当交易号 —— 钱根本没打出去，账面却是成功的，
 * 财务对账必然对不上。现在把「实际打款」收敛到通道里，通道没接好就必须返回失败，
 * 由服务层落「发放失败」并记原因，绝不允许伪造成功。
 *
 * <p>接入真实通道时新增一个实现类并在 {@link RebatePaymentChannelRouter} 注册即可，
 * 服务层不用改。
 *
 * @author Amos
 */
public interface RebatePaymentChannel {

    /** 通道编码，对应配置 {@code houcloud.rebate.pay.gateway}。 */
    String code();

    /**
     * 当前是否可用。不可用的通道不会被路由选中。
     *
     * @return true 表示可承接发放
     */
    boolean available();

    /**
     * 执行一笔发放。
     *
     * <p>实现要求：<b>只有资金确实付出去了才允许返回成功</b>，且成功必须带上游真实流水号；
     * 拿不到流水号就按失败返回，由服务层置失败状态并记录原因。
     *
     * @param payment 发放单（状态已置为发放中）
     * @return 发放结果
     */
    Result pay(RebatePayment payment);

    /**
     * 发放结果。
     *
     * <p>{@code success=true} 时 {@code transactionNo} 必须有值：调用方据此判定能否置「已发放」，
     * 空流水号一律按失败处理，避免产生无凭据的成功记录。
     */
    class Result {

        private final boolean success;

        private final String transactionNo;

        private final String message;

        private Result(boolean success, String transactionNo, String message) {
            this.success = success;
            this.transactionNo = transactionNo;
            this.message = message;
        }

        public static Result success(String transactionNo) {
            return new Result(true, transactionNo, null);
        }

        public static Result fail(String message) {
            return new Result(false, null, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getTransactionNo() {
            return transactionNo;
        }

        public String getMessage() {
            return message;
        }
    }
}
