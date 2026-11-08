package com.houjiang.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.goods.GdsListQueryResultVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 代金券模版表
 * @TableName activity_voucher_template
 * @author Amos
 */
@TableName(value = "hou_addon_activity_voucher_template")
@Data
public class ActivityVoucherTemplateEntity implements Serializable {
    /**
     * 代金券模版编号
     */
    @TableId(type = IdType.AUTO)
    private Integer voucherTId;

    /**
     * 代金券模版名称
     */
    private String voucherTTitle;


    /**
     * 代金券模版有效期开始时间
     */
    private Date voucherTStartDate;

    /**
     * 代金券模版有效期结束时间
     */
    private Date voucherTEndDate;

    /**
     * 代金券模版面额
     */
    private BigDecimal voucherTPrice;

    /**
     * 代金券使用时的订单限额
     */
    private BigDecimal voucherTLimit;

    /**
     * 代金券模版的店铺id
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 代金券模版的创建者id
     */
    private String voucherTCreatorId;

    /**
     * 代金券模版状态(1-有效,2-失效)
     */
    private Byte voucherTState;

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
    private Date voucherTAddDate;

    /**
     * 模版的最后修改时间
     */
    private Date voucherTUpdateDate;

    /**
     * 每人限领张数
     */
    private Integer voucherTEachlimit;


    /**
     * 自定义代金券模板图片
     */
    private String voucherTCustomimg;
    /**
     * 参加平台活动审核失败原因
     */
    private String voucherTRemark;

    /**
     * 优惠券报名开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date voucherBStartDate;

    /**
     * 优惠券报名结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date voucherBEndDate;

    /**
     * 代金券类型：1 是店铺优惠券 2是商品优惠券 3 商品兑换券
     */
    private Integer isShopVoucher;

    @TableField(exist = false)
    private List<GdsListQueryResultVO> goodsList;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        ActivityVoucherTemplateEntity other = (ActivityVoucherTemplateEntity) that;
        return (this.getVoucherTId() == null ? other.getVoucherTId() == null : this.getVoucherTId().equals(other.getVoucherTId()))
            && (this.getVoucherTTitle() == null ? other.getVoucherTTitle() == null : this.getVoucherTTitle().equals(other.getVoucherTTitle()))
            && (this.getVoucherTStartDate() == null ? other.getVoucherTStartDate() == null : this.getVoucherTStartDate().equals(other.getVoucherTStartDate()))
            && (this.getVoucherTEndDate() == null ? other.getVoucherTEndDate() == null : this.getVoucherTEndDate().equals(other.getVoucherTEndDate()))
            && (this.getVoucherTPrice() == null ? other.getVoucherTPrice() == null : this.getVoucherTPrice().equals(other.getVoucherTPrice()))
            && (this.getVoucherTLimit() == null ? other.getVoucherTLimit() == null : this.getVoucherTLimit().equals(other.getVoucherTLimit()))
            && (this.getShopId() == null ? other.getShopId() == null : this.getShopId().equals(other.getShopId()))
            && (this.getShopName() == null ? other.getShopName() == null : this.getShopName().equals(other.getShopName()))
            && (this.getVoucherTCreatorId() == null ? other.getVoucherTCreatorId() == null : this.getVoucherTCreatorId().equals(other.getVoucherTCreatorId()))
            && (this.getVoucherTState() == null ? other.getVoucherTState() == null : this.getVoucherTState().equals(other.getVoucherTState()))
            && (this.getVoucherTTotal() == null ? other.getVoucherTTotal() == null : this.getVoucherTTotal().equals(other.getVoucherTTotal()))
            && (this.getVoucherTGiveout() == null ? other.getVoucherTGiveout() == null : this.getVoucherTGiveout().equals(other.getVoucherTGiveout()))
            && (this.getVoucherTUsed() == null ? other.getVoucherTUsed() == null : this.getVoucherTUsed().equals(other.getVoucherTUsed()))
            && (this.getVoucherTAddDate() == null ? other.getVoucherTAddDate() == null : this.getVoucherTAddDate().equals(other.getVoucherTAddDate()))
            && (this.getVoucherTUpdateDate() == null ? other.getVoucherTUpdateDate() == null : this.getVoucherTUpdateDate().equals(other.getVoucherTUpdateDate()))
            && (this.getVoucherTEachlimit() == null ? other.getVoucherTEachlimit() == null : this.getVoucherTEachlimit().equals(other.getVoucherTEachlimit()))
            && (this.getVoucherTCustomimg() == null ? other.getVoucherTCustomimg() == null : this.getVoucherTCustomimg().equals(other.getVoucherTCustomimg()))
            && (this.getVoucherTRemark() == null ? other.getVoucherTRemark() == null : this.getVoucherTRemark().equals(other.getVoucherTRemark()))
            && (this.getVoucherBStartDate() == null ? other.getVoucherBStartDate() == null : this.getVoucherBStartDate().equals(other.getVoucherBStartDate()))
            && (this.getVoucherBEndDate() == null ? other.getVoucherBEndDate() == null : this.getVoucherBEndDate().equals(other.getVoucherBEndDate()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getVoucherTId() == null) ? 0 : getVoucherTId().hashCode());
        result = prime * result + ((getVoucherTTitle() == null) ? 0 : getVoucherTTitle().hashCode());
        result = prime * result + ((getVoucherTStartDate() == null) ? 0 : getVoucherTStartDate().hashCode());
        result = prime * result + ((getVoucherTEndDate() == null) ? 0 : getVoucherTEndDate().hashCode());
        result = prime * result + ((getVoucherTPrice() == null) ? 0 : getVoucherTPrice().hashCode());
        result = prime * result + ((getVoucherTLimit() == null) ? 0 : getVoucherTLimit().hashCode());
        result = prime * result + ((getShopId() == null) ? 0 : getShopId().hashCode());
        result = prime * result + ((getShopName() == null) ? 0 : getShopName().hashCode());
        result = prime * result + ((getVoucherTCreatorId() == null) ? 0 : getVoucherTCreatorId().hashCode());
        result = prime * result + ((getVoucherTState() == null) ? 0 : getVoucherTState().hashCode());
        result = prime * result + ((getVoucherTTotal() == null) ? 0 : getVoucherTTotal().hashCode());
        result = prime * result + ((getVoucherTGiveout() == null) ? 0 : getVoucherTGiveout().hashCode());
        result = prime * result + ((getVoucherTUsed() == null) ? 0 : getVoucherTUsed().hashCode());
        result = prime * result + ((getVoucherTAddDate() == null) ? 0 : getVoucherTAddDate().hashCode());
        result = prime * result + ((getVoucherTUpdateDate() == null) ? 0 : getVoucherTUpdateDate().hashCode());
        result = prime * result + ((getVoucherTEachlimit() == null) ? 0 : getVoucherTEachlimit().hashCode());
        result = prime * result + ((getVoucherTCustomimg() == null) ? 0 : getVoucherTCustomimg().hashCode());
        result = prime * result + ((getVoucherTRemark() == null) ? 0 : getVoucherTRemark().hashCode());
        result = prime * result + ((getVoucherBStartDate() == null) ? 0 : getVoucherBStartDate().hashCode());
        result = prime * result + ((getVoucherBEndDate() == null) ? 0 : getVoucherBEndDate().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", voucherTId=").append(voucherTId);
        sb.append(", voucherTTitle=").append(voucherTTitle);
        sb.append(", voucherTStartDate=").append(voucherTStartDate);
        sb.append(", voucherTEndDate=").append(voucherTEndDate);
        sb.append(", voucherTPrice=").append(voucherTPrice);
        sb.append(", voucherTLimit=").append(voucherTLimit);
        sb.append(", shopId=").append(shopId);
        sb.append(", shopName=").append(shopName);
        sb.append(", voucherTCreatorId=").append(voucherTCreatorId);
        sb.append(", voucherTState=").append(voucherTState);
        sb.append(", voucherTTotal=").append(voucherTTotal);
        sb.append(", voucherTGiveout=").append(voucherTGiveout);
        sb.append(", voucherTUsed=").append(voucherTUsed);
        sb.append(", voucherTAddDate=").append(voucherTAddDate);
        sb.append(", voucherTUpdateDate=").append(voucherTUpdateDate);
        sb.append(", voucherTEachlimit=").append(voucherTEachlimit);
        sb.append(", voucherTCustomimg=").append(voucherTCustomimg);
        sb.append(", voucherTRemark=").append(voucherTRemark);
        sb.append(", voucherBStartDate=").append(voucherBStartDate);
        sb.append(", voucherBEndDate=").append(voucherBEndDate);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
