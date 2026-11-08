package com.houjiang.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 红包模版表
 * @TableName activity_redpacket_template
 * @author Amos
 */
@TableName(value ="activity_redpacket_template")
@Data
public class ActivityRedpacketTemplateEntity implements Serializable {
    /**
     * 红包模版编号
     */
    @TableId(type = IdType.AUTO)
    private Integer redpacketTId;

    /**
     * 红包类型，1-新人注册红包，2-普通红包，3-多品红包 4-店铺红包 5-单品多品红包 6-专属红包默认2
     */
    private Integer redpacketTType;

    /**
     * 店铺 ID。红包模板属于店铺维度，和其它活动实体保持一致。
     */
    private Integer shopId;

    /**
     * 红包模版名称
     */
    private String redpacketTTitle;

    /**
     * 红包模版描述
     */
    private String redpacketTDesc;

    /**
     * 红包模版有效期开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date redpacketTStartDate;

    /**
     * 红包模版有效期结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date redpacketTEndDate;

    /**
     * 红包模版面额
     */
    private Integer redpacketTPrice;

    /**
     * 红包使用时的消费限额
     */
    private BigDecimal redpacketTOrderlimit;

    /**
     * 红包模版状态(1-有效,2-失效,3-删除)
     */
    private Byte redpacketTState;

    /**
     * 模版可发放的红包总数  0 不限制
     */
    private Integer redpacketTTotal;

    /**
     * 模版已发放的红包数量
     */
    private Integer redpacketTGiveout;

    /**
     * 模版的创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date redpacketTAddDate;

    /**
     * 模版的最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date redpacketTUpdateDate;

    /**
     * 每人限领张数 0 不限制
     */
    private Integer redpacketTEachlimit;


    /**
     * 操作人
     */
    private String orderOperator;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderOperatorTime;
    /**
     * 审批
     */
    private String redpacketRemark;

    /**
     * 红包是否倒计时1-否2-是
     */
    private Byte redpacketIsBooking;

    /**
     * 抢红包开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date robRedpacketStartDate;

    /**
     * 抢红包结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date robRedpacketEndDate;

    /**
     * 自红包发放起，有效天数
     */
    private Integer redTermofvalidity;

    @TableField(exist = false)
    private List<String> usePlatformStringList;

    @TableField(exist = false)
    private List<String> GetPlatformStringList;

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
        ActivityRedpacketTemplateEntity other = (ActivityRedpacketTemplateEntity) that;
        return (this.getRedpacketTId() == null ? other.getRedpacketTId() == null : this.getRedpacketTId().equals(other.getRedpacketTId()))
            && (this.getShopId() == null ? other.getShopId() == null : this.getShopId().equals(other.getShopId()))
            && (this.getRedpacketTType() == null ? other.getRedpacketTType() == null : this.getRedpacketTType().equals(other.getRedpacketTType()))
            && (this.getRedpacketTTitle() == null ? other.getRedpacketTTitle() == null : this.getRedpacketTTitle().equals(other.getRedpacketTTitle()))
            && (this.getRedpacketTDesc() == null ? other.getRedpacketTDesc() == null : this.getRedpacketTDesc().equals(other.getRedpacketTDesc()))
            && (this.getRedpacketTStartDate() == null ? other.getRedpacketTStartDate() == null : this.getRedpacketTStartDate().equals(other.getRedpacketTStartDate()))
            && (this.getRedpacketTEndDate() == null ? other.getRedpacketTEndDate() == null : this.getRedpacketTEndDate().equals(other.getRedpacketTEndDate()))
            && (this.getRedpacketTPrice() == null ? other.getRedpacketTPrice() == null : this.getRedpacketTPrice().equals(other.getRedpacketTPrice()))
            && (this.getRedpacketTOrderlimit() == null ? other.getRedpacketTOrderlimit() == null : this.getRedpacketTOrderlimit().equals(other.getRedpacketTOrderlimit()))
            && (this.getRedpacketTState() == null ? other.getRedpacketTState() == null : this.getRedpacketTState().equals(other.getRedpacketTState()))
            && (this.getRedpacketTTotal() == null ? other.getRedpacketTTotal() == null : this.getRedpacketTTotal().equals(other.getRedpacketTTotal()))
            && (this.getRedpacketTGiveout() == null ? other.getRedpacketTGiveout() == null : this.getRedpacketTGiveout().equals(other.getRedpacketTGiveout()))
            && (this.getRedpacketTAddDate() == null ? other.getRedpacketTAddDate() == null : this.getRedpacketTAddDate().equals(other.getRedpacketTAddDate()))
            && (this.getRedpacketTUpdateDate() == null ? other.getRedpacketTUpdateDate() == null : this.getRedpacketTUpdateDate().equals(other.getRedpacketTUpdateDate()))
            && (this.getRedpacketTEachlimit() == null ? other.getRedpacketTEachlimit() == null : this.getRedpacketTEachlimit().equals(other.getRedpacketTEachlimit()))
            && (this.getOrderOperator() == null ? other.getOrderOperator() == null : this.getOrderOperator().equals(other.getOrderOperator()))
            && (this.getOrderOperatorTime() == null ? other.getOrderOperatorTime() == null : this.getOrderOperatorTime().equals(other.getOrderOperatorTime()))
            && (this.getRedpacketRemark() == null ? other.getRedpacketRemark() == null : this.getRedpacketRemark().equals(other.getRedpacketRemark()))
            && (this.getRedpacketIsBooking() == null ? other.getRedpacketIsBooking() == null : this.getRedpacketIsBooking().equals(other.getRedpacketIsBooking()))
            && (this.getRobRedpacketStartDate() == null ? other.getRobRedpacketStartDate() == null : this.getRobRedpacketStartDate().equals(other.getRobRedpacketStartDate()))
            && (this.getRobRedpacketEndDate() == null ? other.getRobRedpacketEndDate() == null : this.getRobRedpacketEndDate().equals(other.getRobRedpacketEndDate()))
            && (this.getRedTermofvalidity() == null ? other.getRedTermofvalidity() == null : this.getRedTermofvalidity().equals(other.getRedTermofvalidity()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getRedpacketTId() == null) ? 0 : getRedpacketTId().hashCode());
        result = prime * result + ((getShopId() == null) ? 0 : getShopId().hashCode());
        result = prime * result + ((getRedpacketTType() == null) ? 0 : getRedpacketTType().hashCode());
        result = prime * result + ((getRedpacketTTitle() == null) ? 0 : getRedpacketTTitle().hashCode());
        result = prime * result + ((getRedpacketTDesc() == null) ? 0 : getRedpacketTDesc().hashCode());
        result = prime * result + ((getRedpacketTStartDate() == null) ? 0 : getRedpacketTStartDate().hashCode());
        result = prime * result + ((getRedpacketTEndDate() == null) ? 0 : getRedpacketTEndDate().hashCode());
        result = prime * result + ((getRedpacketTPrice() == null) ? 0 : getRedpacketTPrice().hashCode());
        result = prime * result + ((getRedpacketTOrderlimit() == null) ? 0 : getRedpacketTOrderlimit().hashCode());
        result = prime * result + ((getRedpacketTState() == null) ? 0 : getRedpacketTState().hashCode());
        result = prime * result + ((getRedpacketTTotal() == null) ? 0 : getRedpacketTTotal().hashCode());
        result = prime * result + ((getRedpacketTGiveout() == null) ? 0 : getRedpacketTGiveout().hashCode());
        result = prime * result + ((getRedpacketTAddDate() == null) ? 0 : getRedpacketTAddDate().hashCode());
        result = prime * result + ((getRedpacketTUpdateDate() == null) ? 0 : getRedpacketTUpdateDate().hashCode());
        result = prime * result + ((getRedpacketTEachlimit() == null) ? 0 : getRedpacketTEachlimit().hashCode());
        result = prime * result + ((getOrderOperator() == null) ? 0 : getOrderOperator().hashCode());
        result = prime * result + ((getOrderOperatorTime() == null) ? 0 : getOrderOperatorTime().hashCode());
        result = prime * result + ((getRedpacketRemark() == null) ? 0 : getRedpacketRemark().hashCode());
        result = prime * result + ((getRedpacketIsBooking() == null) ? 0 : getRedpacketIsBooking().hashCode());
        result = prime * result + ((getRobRedpacketStartDate() == null) ? 0 : getRobRedpacketStartDate().hashCode());
        result = prime * result + ((getRobRedpacketEndDate() == null) ? 0 : getRobRedpacketEndDate().hashCode());
        result = prime * result + ((getRedTermofvalidity() == null) ? 0 : getRedTermofvalidity().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", redpacketTId=").append(redpacketTId);
        sb.append(", shopId=").append(shopId);
        sb.append(", redpacketTType=").append(redpacketTType);
        sb.append(", redpacketTTitle=").append(redpacketTTitle);
        sb.append(", redpacketTDesc=").append(redpacketTDesc);
        sb.append(", redpacketTStartDate=").append(redpacketTStartDate);
        sb.append(", redpacketTEndDate=").append(redpacketTEndDate);
        sb.append(", redpacketTPrice=").append(redpacketTPrice);
        sb.append(", redpacketTOrderlimit=").append(redpacketTOrderlimit);
        sb.append(", redpacketTState=").append(redpacketTState);
        sb.append(", redpacketTTotal=").append(redpacketTTotal);
        sb.append(", redpacketTGiveout=").append(redpacketTGiveout);
        sb.append(", redpacketTAddDate=").append(redpacketTAddDate);
        sb.append(", redpacketTUpdateDate=").append(redpacketTUpdateDate);
        sb.append(", redpacketTEachlimit=").append(redpacketTEachlimit);
        sb.append(", orderOperator=").append(orderOperator);
        sb.append(", orderOperatorTime=").append(orderOperatorTime);
        sb.append(", redpacketRemark=").append(redpacketRemark);
        sb.append(", redpacketIsBooking=").append(redpacketIsBooking);
        sb.append(", robRedpacketStartDate=").append(robRedpacketStartDate);
        sb.append(", robRedpacketEndDate=").append(robRedpacketEndDate);
        sb.append(", redTermofvalidity=").append(redTermofvalidity);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }


}
