package com.houjiang.commons.vo.payment;

import com.houjiang.commons.util.TimeStampUtil;
import com.houjiang.commons.vo.order.CommissionSettleVO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class PayFxAccountVO {

    private Integer id;

    private String userId;

    private String userName;

    private String userMobile;

    private BigDecimal totalAccount;

    private BigDecimal vkAccount;

    private BigDecimal ygAccount;

    private BigDecimal withdrawFreezeAccount;

    private String aliUserId;

    private String aliUserName;

    private String miniWxOpenId;

    private String miniWxUserName;

    private String miniWxUnionId;

    private String wxOpenId;

    private String wxUserName;

    private String wxUnionId;

    private String createTime;

    private String updateTime;

    /**
     * 是否是员工  1是 2否
     */
    private Integer isYgUser;
    public PayFxAccountVO() {
    }

    public PayFxAccountVO(String userId, String userName, String userMobile) {
        this.userId = userId;
        this.userName = userName;
        this.userMobile = userMobile;
        this.createTime = TimeStampUtil.getNowDate();
        this.updateTime = TimeStampUtil.getNowDate();
    }

    public PayFxAccountVO(String userId, BigDecimal freezeAccount, BigDecimal vkAccount) {
        this.updateTime = TimeStampUtil.getNowDate();
        this.userId = userId;
        this.withdrawFreezeAccount = freezeAccount;
        this.vkAccount = vkAccount;
    }

    public PayFxAccountVO(String userId, BigDecimal vkAccount) {
        this.userId = userId;
        this.vkAccount = vkAccount;
        this.updateTime = TimeStampUtil.getNowDate();
    }

    public PayFxAccountVO(Integer id, String userId, String miniWxOpenId, String miniWxUserName, String miniWxUnionId) {
        this.id = id;
        this.userId = userId;
        this.miniWxOpenId = miniWxOpenId;
        this.miniWxUserName = miniWxUserName;
        this.miniWxUnionId = miniWxUnionId;
        this.updateTime = TimeStampUtil.getNowDate();
    }

    public PayFxAccountVO(Integer id, String userId, String aliUserId, String aliUserName, String wxOpenId, String wxUserName, String wxUnionId) {
        this.id = id;
        this.userId = userId;
        this.aliUserId = aliUserId;
        this.aliUserName = aliUserName;
        this.wxOpenId = wxOpenId;
        this.wxUserName = wxUserName;
        this.wxUnionId = wxUnionId;
        this.updateTime = TimeStampUtil.getNowDate();
    }

    public PayFxAccountVO(String userId) {
        this.userId = userId;
        this.totalAccount = BigDecimal.ZERO;
        this.vkAccount = BigDecimal.ZERO;
        this.ygAccount = BigDecimal.ZERO;
    }

    public PayFxAccountVO(CommissionSettleVO commissionSettle) {
        this.userId = commissionSettle.getShareUserId();
        if(commissionSettle.getCommissionType().intValue() == 1) {
            this.vkAccount = commissionSettle.getCommissionMoney();
        }else{
            this.ygAccount= commissionSettle.getCommissionMoney();
        }
        this.updateTime = TimeStampUtil.getNowDate();
    }
}
