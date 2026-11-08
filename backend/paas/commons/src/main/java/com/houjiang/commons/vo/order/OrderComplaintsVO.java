package com.houjiang.commons.vo.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

//订单投诉

/**
 * @author Amos
 */
@Data
public class OrderComplaintsVO {

    /**
     * 投诉单ID
     */
    private String complaintsId;

    /**
     * 投诉类型
     */
    private Integer complaintsType;

    /**
     * ID
     */
    private Integer id;

    /**
     * 投诉原因
     */
    private String complaintsReason;

    /**
     * 投诉凭证
     */
    private String complaintsVoucher;

    /**
     * 投诉凭证列表
     */
    private List complaintsVouchers;

    /**
     * 投诉联系方式
     */
    private String complaintsContact;

    /**
     * 投诉商品ID
     */
    private String complaintsGoodsid;

    /**
     * 投诉商品ID列表
     */
    private List complaintsGoodsids;

    /**
     * 关联订单ID
     */
    private String orderId;

    /**
     * 商家凭证
     */
    private String sellerVoucher;

    /**
     * 商家凭证列表
     */
    private List sellerVouchers;

    /**
     * 商家回复原因
     */
    private String sellerReason;

    /**
     * 投诉状态
     */
    private Integer complaintsStatus;

    /**
     * 赔偿状态
     */
    private Integer compensationStatus;

    /**
     * 撤销次数
     */
    private Integer revokeNum;

    /**
     * 资金去向
     */
    private Integer fundDestination;

    /**
     * 赔偿金额
     */
    private BigDecimal compensationAmount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date failureTime;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 店铺ID
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 关联的投诉单
     */
    OrderComplaintsVO correlationComplaint;

    /**
     * 商品详情列表
     */
    List<GoodsDetailsListVO> goods;

    /**
     * 卖家用户ID
     */
    private String sellUserId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 保留金额余额
     */
    private BigDecimal retentionMoneyBalance;

    /**
     * 保留金额限额
     */
    private BigDecimal retentionMoneyLimit;
}
