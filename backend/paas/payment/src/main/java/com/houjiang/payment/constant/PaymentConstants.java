package com.houjiang.payment.constant;

/**
 * 支付常量定义
 * 
 * @author Amos
 */
public class PaymentConstants {
    
    /**
     * 金额单位转换：元转分的倍数
     */
    public static final int AMOUNT_YUAN_TO_FEN = 100;
    
    /**
     * 成功返回码
     */
    public static final String SUCCESS_CODE = "0";
    
    /**
     * 成功返回信息
     */
    public static final String SUCCESS_MSG = "成功";
    
    /**
     * 返回码定义
     */
    public static class ResponseCode {
        /** 请求成功 */
        public static final String SUCCESS = "0";
        /** 自定义业务异常 */
        public static final String BIZ_ERROR = "9999";
        /** 用户名/密码错误 */
        public static final String AUTH_ERROR = "4000";
        /** 系统异常 */
        public static final String SYSTEM_ERROR = "10";
    }
    
    /**
     * 签名类型
     */
    public static class SignType {
        public static final String MD5 = "MD5";
        public static final String RSA = "RSA";
        public static final String SM2 = "SM2";
    }
    
    /**
     * 订单状态
     */
    public static class OrderStatus {
        /** 0-订单生成 */
        public static final Integer ORDER_CREATE = 0;
        /** 1-进行中 */
        public static final Integer IN_PROGRESS = 1;
        /** 2-支付成功 */
        public static final Integer PAY_SUCCESS = 2;
        /** 3-支付失败 */
        public static final Integer PAY_FAIL = 3;
        /** 4-已撤销 */
        public static final Integer CANCELLED = 4;
        /** 5-已退款 */
        public static final Integer REFUNDED = 5;
        /** 6-订单关闭 */
        public static final Integer CLOSED = 6;
        /** 7-预消费支付成功 */
        public static final Integer PRE_PAY_SUCCESS = 7;
    }
    
    /**
     * 退款状态
     */
    public static class RefundStatus {
        /** 0-订单生成 */
        public static final Integer ORDER_CREATE = 0;
        /** 1-退款中 */
        public static final Integer REFUNDING = 1;
        /** 2-退款成功 */
        public static final Integer SUCCESS = 2;
        /** 3-退款失败 */
        public static final Integer FAIL = 3;
        /** 4-退款关闭 */
        public static final Integer CLOSED = 4;
        /** 6-预消费退款 */
        public static final Integer PRE_REFUND = 6;
    }
    
    /**
     * 支付方式（wayCode）
     * 通企付支持的支付方式编码
     */
    public static class WayCode {
        /** 微信小程序支付 */
        public static final String WX_LITE = "WX_LITE";
        /** 微信主扫 */
        public static final String WX_NATIVE = "WX_NATIVE";
        /** 微信公众号支付 */
        public static final String WX_JSAPI = "WX_JSAPI";
        /** 通联汇入金 */
        public static final String SFT_HRJ = "SFT_HRJ";
        /** 新网汇入金 */
        public static final String XW_HRJ = "XW_HRJ";
        /** 支付宝JS支付 */
        public static final String ALI_JSAPI = "ALI_JSAPI";
        /** 支付宝主扫 */
        public static final String ALI_QR = "ALI_QR";
        /** 收银宝B2B订单 */
        public static final String B2B_ORDER = "B2B_ORDER";
        /** B2B订单付款 */
        public static final String B2B_ORDER_PAY = "B2B_ORDER_PAY";
        /** PC网关 */
        public static final String GATEWAY_PC = "GATEWAY_PC";
        /** 花呗分期 */
        public static final String ALI_CREDIT = "ALI_CREDIT";
        /** 白条分期 */
        public static final String JDBT_PAY = "JDBT_PAY";
        /** 微信小程序收银台支付 */
        public static final String SYB_B2C_WECHAT = "SYB_B2C_WECHAT";
        /** 微信预消费 */
        public static final String WX_TRANS = "WX_TRANS";
        /** 微信B2B门店助手 */
        public static final String WX_B2B = "WX_B2B";
        /** 富民e贷 */
        public static final String FME_CPAY = "FME_CPAY";
        /** 快捷支付 */
        public static final String QUICK_PAY = "QUICK_PAY";
        /** 云微支付 */
        public static final String YW_PAY = "YW_PAY";
        /** 云梯支付 */
        public static final String YT_PAY = "YT_PAY";
        /** H5收银台 */
        public static final String H5_CASHIER = "H5_CASHIER";
        /** 微企付 */
        public static final String YUNST2_WQF_ZZHK = "YUNST2_WQF_ZZHK";
        /** 付款码支付 */
        public static final String AUTO_BAR = "AUTO_BAR";
    }
    
    /**
     * 账户类型
     */
    public static class AcctType {
        /** 借记卡 */
        public static final String DEBIT_CARD = "00";
        /** 信用卡 */
        public static final String CREDIT_CARD = "02";
        /** 其他（花呗/余额等） */
        public static final String OTHER = "99";
    }
    
    /**
     * 通知返回结果
     */
    public static class NotifyResult {
        /** 成功（必须小写） */
        public static final String SUCCESS = "success";
        /** 失败（必须小写） */
        public static final String FAIL = "fail";
    }
    
    /**
     * 文件类型
     */
    public static class FileType {
        /** CSV文件 */
        public static final String CSV = "CSV";
        /** EXCEL文件 */
        public static final String EXCEL = "EXCEL";
    }
    
    /**
     * 账单类型
     */
    public static class BillType {
        /** 支付交易对账单 */
        public static final Integer PAY = 1;
        /** 分账交易对账单 */
        public static final Integer DIVISION = 2;
        /** 预消费交易对账单 */
        public static final Integer PRE_CONSUME = 3;
    }
    
    /**
     * 渠道编码
     */
    public static class ChannelCode {
        /** 收银宝 */
        public static final String ALLINPAY = "allinpay";
        /** 云商通特色结算 */
        public static final String YUNST2ISV = "yunst2isv";
        /** 云商通渠道 */
        public static final String YUNST = "yunst";
        /** 云商通银行账户 */
        public static final String YUNST2YHZH = "yunst2yhzh";
    }
    
    /**
     * 账户类型
     */
    public static class AccountType {
        /** 待结算户（yunst2isv） */
        public static final String PENDING_SETTLE_ISV = "08";
        /** 免密余额户（yunst2yhzh） */
        public static final String NO_PWD = "NO_PWD";
        /** 待结算户（yunst2yhzh） */
        public static final String PENDING_SETTLE = "PENDING_SETTLE";
    }
    
    /**
     * 提现状态
     */
    public static class WithdrawStatus {
        /** 0-订单生成 */
        public static final Integer ORDER_CREATE = 0;
        /** 1-提现中 */
        public static final Integer WITHDRAWING = 1;
        /** 2-提现成功 */
        public static final Integer SUCCESS = 2;
        /** 3-提现失败 */
        public static final Integer FAIL = 3;
        /** 6-订单关闭 */
        public static final Integer CLOSED = 6;
    }
    
    /**
     * 提现方式
     */
    public static class WithdrawType {
        /** D+0到账 */
        public static final String D0 = "D0";
        /** D+1到账 */
        public static final String D1 = "D1";
        /** T+1到账 */
        public static final String T1 = "T1";
    }
    
    /**
     * 银行卡属性
     */
    public static class BankCardType {
        /** 个人银行卡 */
        public static final Integer PERSONAL = 0;
        /** 企业对公账户 */
        public static final Integer ENTERPRISE = 1;
    }
    
    /**
     * 分账状态
     */
    public static class DivisionStatus {
        /** 1-进行中 */
        public static final Integer PROCESSING = 1;
        /** 2-分账成功 */
        public static final Integer SUCCESS = 2;
        /** 3-分账失败 */
        public static final Integer FAIL = 3;
    }
    
    /**
     * 分账撤销状态
     */
    public static class RefundAssureStatus {
        /** 0-订单生成 */
        public static final Integer ORDER_CREATE = 0;
        /** 1-撤销中 */
        public static final Integer REFUNDING = 1;
        /** 2-撤销成功 */
        public static final Integer SUCCESS = 2;
        /** 3-撤销失败 */
        public static final Integer FAIL = 3;
    }
    
    /**
     * 币种
     */
    public static final String CURRENCY_CNY = "CNY";
    
    /**
     * 转账状态
     */
    public static class TransferStatus {
        /** 0-订单生成 */
        public static final Integer ORDER_CREATE = 0;
        /** 1-转账中 */
        public static final Integer TRANSFERRING = 1;
        /** 2-转账成功 */
        public static final Integer SUCCESS = 2;
        /** 3-转账失败 */
        public static final Integer FAIL = 3;
    }
    
    /**
     * 入账方式
     */
    public static class EntryType {
        /** 收银宝余额 */
        public static final String SYB_CASH = "SYB_CASH";
    }
    
    /**
     * 小程序链接关闭状态
     */
    public static class AppletCloseState {
        /** 0-链接关闭失败 */
        public static final Integer FAIL = 0;
        /** 1-链接关闭成功 */
        public static final Integer SUCCESS = 1;
    }
    
    /**
     * 快捷支付签约类型
     */
    public static class QuickPayAgreeType {
        /** 1-签约申请 */
        public static final String APPLY = "1";
        /** 2-签约确认 */
        public static final String CONFIRM = "2";
    }
    
    /**
     * 证件类型
     */
    public static class IdentityType {
        /** 0-身份证 */
        public static final String ID_CARD = "0";
        /** 2-护照 */
        public static final String PASSPORT = "2";
        /** 5-港澳通行证 */
        public static final String HK_MACAU = "5";
        /** 6-台湾通行证 */
        public static final String TAIWAN = "6";
    }
}
