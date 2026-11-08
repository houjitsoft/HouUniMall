package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class QueryShopVoucherTempResultVO {
    /**
     * 代金券模版编号
     */
    private Integer voucherTId;
    /**
     * 代金券模版名称
     */
    private String voucherTTitle;
    /**
     * 代金券模版描述
     */
    private String voucherTDesc;

    /**
     * 店铺分类id
     */
    private Integer shopClassId;

    /**
     * 代金券模版有效期开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date voucherTStartDate;
    /**
     * 代金券模版有效期结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date voucherTEndDate;
    /**
     * 代金券模版面额
     */
    private Double voucherTPrice;
    /**
     * 代金券使用时的订单限额
     */
    private String voucherTLimit;

    /**
     *店铺id
     */
    private Integer shopId;

    /**
     * 店铺名
     */
    private String shopName;
    /**
     * 代金券模版的创建者id
     */
    private String voucherTCreatorId;
    /**
     * 代金券模版状态(1-有效,2-失效)
     */
    private Integer voucherTState;
    /**
     * 模版可发放的代金券总数
     */
    private Integer voucherTTotal;
    /**
     * 模版已发放的代金券数量
     */
    private Integer voucherTGiveout;
    /**
     * 模版已经使用过的代金券
     */
    private Integer voucherTUsed;
    /**
     * 模版的创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date voucherTAddDate;
    /**
     * 模版的最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date voucherTUpdateDate;
    /**
     * 套餐编号
     */
    private Integer comboId;
    /**
     * 兑换所需积分
     */
    private Integer voucherTPoints;
    /**
     * 每人限领张数
     */
    private Integer voucherTEachlimit;
    /**
     * 样式模版图片
     */
    private String voucherTStyleimg;
    /**
     * 自定义代金券模板图片
     */
    private String voucherTCustomimg;
    /**
     * 代金券领取方式，1-积分兑换(默认)，2-卡密兑换，3-免费领取
     */
    private Integer voucherTAccessMethod;
    /**
     * 推荐状态，0-为不推荐，1-推荐
     */
    private Integer voucherTRecommend;
    /**
     * 领取代金券的用户等级限制
     */
    private Integer voucherTUserGradeLimit;
    /**
     * 代金券模版表
     */
    private Integer receiveOrNot;

    /**
     * 店铺logo
     */
    private String shopLogo;

    /**
     * 代金券类型：1 是店铺优惠券 2是商品优惠券
     */
    private Integer isShopVoucher;

}
