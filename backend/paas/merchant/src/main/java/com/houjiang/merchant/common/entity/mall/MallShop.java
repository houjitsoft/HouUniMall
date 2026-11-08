package com.houjiang.merchant.common.entity.mall;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallShop implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    /** 库表尚未补齐，禁止参与 SELECT/UPDATE */
    @TableField(exist = false)
    @Schema(description = "店主平台用户ID")
    private String ownerUserId;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "修改时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "logo")
    private String logo;

    @TableField(exist = false)
    @Schema(description = "店铺banner")
    private String banner;

    @Schema(description = "店铺简介")
    @TableField(value = "`desc`")
    private String descStr;

    @Schema(description = "经营类目")
    private Integer shopCategoryId;

    @Schema(description = "类型 0自营 1入驻")
    private Integer type;

    @Schema(description = "店铺状态 1正常 0冻结")
    private Integer shopStatus;

    @Schema(description = "经营状态 1营业中 0暂停营业")
    private Integer businessStatus;

    @TableField(exist = false)
    @Schema(description = "有效期截止时间(秒)")
    private Long shopEndTime;

    @TableField(exist = false)
    @Schema(description = "结算周期(天)")
    private Integer settlementCycle;

    @TableField(exist = false)
    @Schema(description = "免运费额度")
    private BigDecimal freeShipping;

    @TableField(exist = false)
    @Schema(description = "发票开关 0关1开")
    private Integer invoiceSwitchStatus;

    @TableField(exist = false)
    @Schema(description = "发票审核状态")
    private Integer invoiceAuditStatus;

    /**
     * 质保金余额。
     * <p>
     * 原 business 的 hou_shop_base.retention_money_balance 随 business 下线一起消失，
     * 2026-09-19 迁移脚本 20260919-merchant-internal-closure.sql 已把该列补回
     * hou_merchant_mall_shop，因此这里不能再标 exist=false，否则 payment 的质保金充值无法入账。
     */
    @Schema(description = "质保金余额")
    private BigDecimal retentionMoneyBalance;

    @Schema(description = "质保金额度")
    @TableField(exist = false)
    private BigDecimal retentionMoneyLimit;

    @TableField(exist = false)
    @Schema(description = "质保金状态")
    private Integer retentionStatus;

    @TableField(exist = false)
    @Schema(description = "店铺等级ID")
    private Integer shopLevelId;

    @TableField(exist = false)
    @Schema(description = "店铺印章")
    private String shopStamp;

    @TableField(exist = false)
    @Schema(description = "印章备注")
    private String shopStampMsg;

    @TableField(exist = false)
    @Schema(description = "兼容旧类型 1卖家2供应商3品牌")
    private Integer legacyShopType;

    @Schema(description = "联系人")
    private String contact;

    @Schema(description = "联系手机")
    private String mobile;

    @Schema(description = "省")
    private Integer provinceId;

    @Schema(description = "市")
    private Integer cityId;

    @Schema(description = "区")
    private Integer districtId;

    @Schema(description = "详细地址")
    private String addressInfo;

    @Schema(description = "纬度")
    private String lng;

    @Schema(description = "经度")
    private String lat;

    @Schema(description = "退货联系人")
    private String returnContact;

    @Schema(description = "退货联系手机")
    private String returnMobile;

    @Schema(description = "退货省份")
    private Integer returnProvinceId;

    @Schema(description = "退货城市")
    private Integer returnCityId;

    @Schema(description = "退货区县")
    private Integer returnDistrictId;

    @Schema(description = "退货详细地址")
    private String returnAddressInfo;

    @Schema(description = "精选店铺 1是 0否")
    private Integer homeRecommend;

    @Schema(description = "产品审核 1需要审核 0无需审核")
    private Integer goodsAudit;

    @Schema(description = "营业执照")
    private String businessLicense;

    @Schema(description = "其他执照")
    private String otherLicense;

    @Schema(description = "服务费百分比")
    private BigDecimal serviceRatio;

    @Schema(description = "店铺评分")
    private BigDecimal score;

    @Schema(description = "店铺星级")
    private BigDecimal star;

    @Schema(description = "进店人数")
    private Integer visitNum;

    @Schema(description = "店铺余额")
    private BigDecimal money;

    @Schema(description = "营业额")
    private Integer saleMoney;

    @Schema(description = "成交订单")
    private Integer saleOrders;

    @Schema(description = "在售商品数")
    private Integer saleOnlineGoods;

    @Schema(description = "采集余额")
    private Integer gatherMoney;

}