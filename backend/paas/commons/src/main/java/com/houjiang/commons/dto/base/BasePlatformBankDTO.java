package com.houjiang.commons.dto.base;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 平台银行卡配置
 * @author Amos
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BasePlatformBankDTO extends PageHelper {

    /**
     * 会员等级ID
     */
    private Integer id;

    /**
     * 银行卡卡号
     */
    private String bankCardId;

    /**
     * 开户人姓名
     */
    private String bankCardName;

    /**
     * 开户银行名称
     */
    private String bankName;

    /**
     * 备注
     */
    private String brankNote;

    /**
     * 银联号
     */
    private String brankUnionpayGood;

    /**
     * 支付宝收款码
     */
    private String zfbUrl;

    /**
     * 质保金
     */
    private BigDecimal warrantyMoney;

}
