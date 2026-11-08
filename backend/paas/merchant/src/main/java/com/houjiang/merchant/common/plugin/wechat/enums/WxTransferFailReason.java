package com.houjiang.merchant.common.plugin.wechat.enums;

/**
 * 微信转账失败原因枚举
 *
 * @author Amos
 */
public enum WxTransferFailReason {
    
    /** TODO 补充说明 */
    ACCOUNT_FROZEN("ACCOUNT_FROZEN", "该用户账户被冻结"),
    /** TODO 补充说明 */
    ACCOUNT_NOT_EXIST("ACCOUNT_NOT_EXIST", "该用户账户不存在"),
    /** TODO 补充说明 */
    BANK_CARD_ACCOUNT_ABNORMAL("BANK_CARD_ACCOUNT_ABNORMAL", "银行卡已被销户、冻结、作废、挂失等致无法入账"),
    /** TODO 补充说明 */
    BANK_CARD_BANK_INFO_WRONG("BANK_CARD_BANK_INFO_WRONG", "登记的银行名称或分支行信息有误"),
    /** TODO 补充说明 */
    BANK_CARD_CARD_INFO_WRONG("BANK_CARD_CARD_INFO_WRONG", "银行卡户名或卡号有误"),
    /** TODO 补充说明 */
    BANK_CARD_COLLECTIONS_ABOVE_QUOTA("BANK_CARD_COLLECTIONS_ABOVE_QUOTA", "银行卡属二/三类卡，达到收款限额无法入账"),
    /** TODO 补充说明 */
    BANK_CARD_PARAM_ERROR("BANK_CARD_PARAM_ERROR", "用户收款卡错误，请核实信息"),
    /** TODO 补充说明 */
    BANK_CARD_STATUS_ABNORMAL("BANK_CARD_STATUS_ABNORMAL", "银行卡状态异常，无法入账"),
    /** TODO 补充说明 */
    BLOCK_B2C_USERLIMITAMOUNT_BSRULE_MONTH("BLOCK_B2C_USERLIMITAMOUNT_BSRULE_MONTH", "超出用户月转账收款限额，本月不支持继续向该用户付款"),
    /** TODO 补充说明 */
    BLOCK_B2C_USERLIMITAMOUNT_MONTH("BLOCK_B2C_USERLIMITAMOUNT_MONTH", "用户账户存在风险收款受限，本月不支持继续向该用户付款"),
    /** TODO 补充说明 */
    DAY_RECEIVED_COUNT_EXCEED("DAY_RECEIVED_COUNT_EXCEED", "超过用户日收款次数，核实产品设置是否准确"),
    /** TODO 补充说明 */
    DAY_RECEIVED_QUOTA_EXCEED("DAY_RECEIVED_QUOTA_EXCEED", "超过用户日收款额度，核实产品设置是否准确"),
    /** TODO 补充说明 */
    EXCEEDED_ESTIMATED_AMOUNT("EXCEEDED_ESTIMATED_AMOUNT", "转账金额超过预约金额范围，请检查"),
    /** TODO 补充说明 */
    ID_CARD_NOT_CORRECT("ID_CARD_NOT_CORRECT", "收款人身份证校验不通过，请核实信息"),
    /** TODO 补充说明 */
    MCH_CANCEL("MCH_CANCEL", "商户撤销付款"),
    /** TODO 补充说明 */
    MERCHANT_REJECT("MERCHANT_REJECT", "商户员工（转账验密人）已驳回转账"),
    /** TODO 补充说明 */
    MERCHANT_NOT_CONFIRM("MERCHANT_NOT_CONFIRM", "商户员工（转账验密人）超时未验密"),
    /** TODO 补充说明 */
    NAME_NOT_CORRECT("NAME_NOT_CORRECT", "收款人姓名校验不通过，请核实信息"),
    /** TODO 补充说明 */
    OPENID_INVALID("OPENID_INVALID", "OpenID格式错误或者不属于商家公众账号"),
    /** TODO 补充说明 */
    OTHER_FAIL_REASON_TYPE("OTHER_FAIL_REASON_TYPE", "其它失败原因"),
    /** TODO 补充说明 */
    OVERDUE_CLOSE("OVERDUE_CLOSE", "超过系统重试期，系统自动关闭"),
    /** TODO 补充说明 */
    PAYEE_ACCOUNT_ABNORMAL("PAYEE_ACCOUNT_ABNORMAL", "用户账户收款异常，请联系用户完善其在微信支付的身份信息以继续收款"),
    /** TODO 补充说明 */
    PAYER_ACCOUNT_ABNORMAL("PAYER_ACCOUNT_ABNORMAL", "商户账户付款受限，可前往商户平台获取解除功能限制指引"),
    /** TODO 补充说明 */
    PRODUCT_AUTH_CHECK_FAIL("PRODUCT_AUTH_CHECK_FAIL", "未开通该权限或权限被冻结，请核实产品权限状态"),
    /** TODO 补充说明 */
    REALNAME_ACCOUNT_RECEIVED_QUOTA_EXCEED("REALNAME_ACCOUNT_RECEIVED_QUOTA_EXCEED", "用户账户收款受限，请引导用户在微信支付查看详情"),
    /** TODO 补充说明 */
    REAL_NAME_CHECK_FAIL("REAL_NAME_CHECK_FAIL", "收款人未实名认证，需要用户完成微信实名认证"),
    /** TODO 补充说明 */
    RECEIVE_ACCOUNT_NOT_CONFIGURE("RECEIVE_ACCOUNT_NOT_CONFIGURE", "请前往商户平台-商家转账-前往功能-转账场景中添加"),
    /** TODO 补充说明 */
    RESERVATION_INFO_NOT_MATCH("RESERVATION_INFO_NOT_MATCH", "转账信息，如用户OpenID等参数，与预约时传入的信息不一致，请检查"),
    /** TODO 补充说明 */
    RESERVATION_SCENE_NOT_MATCH("RESERVATION_SCENE_NOT_MATCH", "该预约单的转账场景与发起转账时传入的不同，请检查"),
    /** TODO 补充说明 */
    RESERVATION_STATE_INVALID("RESERVATION_STATE_INVALID", "预约转账单状态异常，请检查"),
    /** TODO 补充说明 */
    TRANSFER_QUOTA_EXCEED("TRANSFER_QUOTA_EXCEED", "超过用户单笔收款额度，核实产品设置是否准确"),
    /** TODO 补充说明 */
    TRANSFER_REMARK_SET_FAIL("TRANSFER_REMARK_SET_FAIL", "转账备注设置失败， 请调整后重新再试"),
    /** TODO 补充说明 */
    TRANSFER_RISK("TRANSFER_RISK", "该笔转账可能存在风险，已被微信拦截"),
    /** TODO 补充说明 */
    TRANSFER_SCENE_INVALID("TRANSFER_SCENE_INVALID", "你尚未获取该转账场景，请确认转账场景ID是否正确"),
    /** TODO 补充说明 */
    TRANSFER_SCENE_UNAVAILABLE("TRANSFER_SCENE_UNAVAILABLE", "该转账场景暂不可用，请确认转账场景ID是否正确");
    
    private final String code;
    private final String message;
    
    WxTransferFailReason(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    /**
     * 根据失败原因代码获取对应的中文描述
     *
     * @param failReasonCode 失败原因代码
     * @return 中文描述，如果找不到对应的代码则返回原代码
     */
    public static String getMessageByCode(String failReasonCode) {
        if (failReasonCode == null || failReasonCode.trim().isEmpty()) {
            return "未知错误";
        }
        
        for (WxTransferFailReason reason : values()) {
            if (reason.getCode().equals(failReasonCode)) {
                return reason.getMessage();
            }
        }
        
        // 如果找不到对应的枚举值，返回原始错误代码
        return failReasonCode;
    }
}