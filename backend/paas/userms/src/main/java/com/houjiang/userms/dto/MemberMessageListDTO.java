package com.houjiang.userms.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户信息集合查询参数
 * @author Amos
 */
@Data
public class MemberMessageListDTO extends PageHelper {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;


    private String outShopName;
    /**
     * 用户等级，1-一级决策者；2-二级扫码者
     */
    private Integer userLevel;

    /**
     * 用户等级，1-一级决策者；2-二级扫码者
     */
    private Integer customerTypeName;

    /**
     * 用户状态，0-禁用；1-启用
     */
    private Integer userState;

    /**
     * 是否黑名单，0-不是；1-是（黑名单的用户登录不了518小程序）
     */
    private Integer isBlack;


    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String finishTime;

    /**
     * 电话
     */
    private String userMobile;

    /**
     * 生日
     */
    private String birthDate;

    /**
     * 上级店铺ID
     */
    private String userParentShopId;

    /**
     * 订单金额
     */
    private Integer orderCount;

    /**
     * 支付总额
     */
    private BigDecimal sumAmount;

    /**
     * 会员等级id
     */
    private Integer memberClassId;

    /**
     * 是否是企业
     */
    private String isBusiness;

    /**
     *店铺id
     */
    private Integer shopId;

    /**
     *套餐类型
     */
    private Integer thaliType;

    /**
     * 1商家  3:供应商  4  品牌店铺
     */
    private Integer shopBusiness;
    /**
     * 是否自营
     */
    private Boolean shopSelfSupport;
}

