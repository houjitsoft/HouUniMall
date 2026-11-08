package com.houjiang.commons.vo.base;

import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.DealerApplyDTO;
import com.houjiang.commons.util.TimeStampUtil;
import com.houjiang.commons.util.querypage.PageHelper;
import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DealerApplyInfoVO extends PageHelper {

    /**
     * ID
     */
    private Integer id;

    /**
     * 供应商店铺ID
     */
    private Integer supplierShopId;

    /**
     * 供应商店铺名称
     */
    private String supplierShopName;

    /**
     * 供应商用户ID
     */
    private String supplierUserId;

    /**
     * 供应商用户名
     */
    private String supplierUserName;

    /**
     * 经销商店铺ID
     */
    private Integer dealerShopId;

    /**
     * 经销商店铺名称
     */
    private String dealerShopName;

    /**
     * 经销商用户ID
     */
    private String dealerUserId;

    /**
     * 经销商用户名
     */
    private String dealerUserName;

    /**
     * 经销商用户电话
     */
    private String dealerUserPhone;

    /**
     * 经销商状态
     */
    private Integer dealerStatus;

    /**
     * 未经销商状态
     */
    private Integer unDealerStatus;

    /**
     * 经销商备注
     */
    private String dealerRemark;

    /**
     * 经销商等级ID
     */
    private Integer dealerLevelId;

    /**
     * 经销商等级名称
     */
    private String dealerLevelName;

    /**
     * 经销商折扣
     */
    private Integer dealerDiscount;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 类别状态
     */
    private Integer catStatus;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 操作类型
     */
    private Integer operateType;

    /**
     * 公共ID
     */
    private Integer commonId;

    /**
     * 经销商用户昵称
     */
    private String dealerUserNickname;

    public DealerApplyInfoVO(UserInfoVO userInfo, DealerApplyVO dealerApply, UserInfoVO supplyUserInfo) {
        this.supplierShopId = dealerApply.getSupplierShopId();
        this.supplierShopName = dealerApply.getSupplierShopName();
        this.supplierUserId = supplyUserInfo.getUserId();
        this.supplierUserName = supplyUserInfo.getUserName();
        this.dealerShopId = dealerApply.getDealerShopId();
        this.dealerShopName = dealerApply.getDealerShopName();
        this.dealerUserId = userInfo.getUserId();
        this.dealerUserName = userInfo.getUserName();
        this.dealerUserPhone = userInfo.getUserMobile();
        this.dealerStatus = CommonType.supplierGoodsStatus.WAIT_AUTH.getCode();
        this.createTime = TimeStampUtil.getNowDate();
    }
    public DealerApplyInfoVO(UserInfoVO userInfo, DealerApplyDTO dealerApply, UserInfoVO supplyUserInfo) {
        this.supplierShopId = dealerApply.getSupplierShopId();
        this.supplierShopName = dealerApply.getSupplierShopName();
        this.supplierUserId = supplyUserInfo.getUserId();
        this.supplierUserName = supplyUserInfo.getUserName();
        this.dealerShopId = dealerApply.getDealerShopId();
        this.dealerShopName = dealerApply.getDealerShopName();
        this.dealerUserId = userInfo.getUserId();
        this.dealerUserName = userInfo.getUserName();
        this.dealerUserPhone = userInfo.getUserMobile();
        this.dealerStatus = CommonType.supplierGoodsStatus.WAIT_AUTH.getCode();
        this.createTime = TimeStampUtil.getNowDate();
    }
}
