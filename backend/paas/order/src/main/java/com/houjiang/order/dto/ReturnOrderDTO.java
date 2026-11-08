package com.houjiang.order.dto;

import com.houjiang.commons.group.Update;
import com.houjiang.commons.util.querypage.PageHelper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReturnOrderDTO extends PageHelper {

    /**
     * 退货状态
     */
    private String returnState;

    /**
     * 退货开始时间
     */
    private String returnAddTime;

    /**
     * 退货结束时间
     */
    private String returnFinishTime;

    /**
     * 订单编号
     */
    private String orderId;

    /**
     * 退货单编号
     */
    @NotEmpty(message = "退单编号不能为空", groups = {Update.approveBarter.class})
    private String returnCode;

    /**
     * 平台介入状态
     */
    private Integer platformIntervention;

    /**
     * 时间类型
     */
    private Integer orderTime;

    /**
     * 店铺ID
     */
    private Integer shopId;

    /**
     * 退货类型
     */
    private Integer returnType;

    /**
     * 店铺类型
     */
    private Integer shopType;

    /**
     * 收货地址id
     */
    private Integer returnAddrId;

    /**
     * 收货地址
     */
    private String returnAddrName;

    /**
     * 收货地址详情
     */
    private String returnAddr;

    /**
     * 退货物流Id
     */
    private String returnShippingCode;

    /**
     * 仓库Id
     */
    private Integer storageId;

    /**
     * 类型
     */
    @NotNull(message = "类型不能为空", groups = {Update.approveBarter.class})
    @Min(value = 0, message = "类型错误", groups = {Update.approveBarter.class})
    @Max(value = 1, message = "类型错误", groups = {Update.approveBarter.class})
    private Integer type;

    /**
     * 商家备注
     */
    private String returnShopMessage;

    /**
     * 邮编
     */
    private Integer returnPostCode;

    /**
     * 联系手机
     */
    private String returnMobile;

    /**
     * 售后状态（1,待卖家审核2,卖家拒绝退款：3,退款成功：4,卖家拒绝退货退款：5,待买家退货：6,买家已退货，待卖家收货：7,买家已退货，卖家拒绝收货：8,卖家已收货,待确认退款：9,退货退款成功：10,卖家拒绝换货：11,买家已退货，待卖家换货：12,卖家已换货，待买家收货：13,换货成功：14,已关闭：15,卖家同意退款,16：卖家同意退款, 17：卖家拒绝退款，18：退款成功）
     */
    private Integer returnStatus;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 买家账号
     */
    private String buyerUserAccount;

    /**
     * 售后开始时间
     */
    private String returnStartTime;

    /**
     * 换货物流ID列表
     */
    private List<Integer> orderBarterIdList;

    /**
     * 平台留言
     */
    private String returnPlatformMessage;

    /**
     * 是否已寄回
     */
    private Integer isSendBack;

    /**
     * 寄回说明
     */
    private String sendBackNotes;

    /**
     * 订单ID列表
     */
    @NotEmpty(message = "订单ID列表不能为空", groups = {Update.class})
    private List<String> orderIdList;

    /**
     * 是否为定时任务
     */
    private Boolean isJob;

    /**
     *待发店铺名
     */
    private String deliverShopName;

    /**
     * 是否已代发
     */
    private Integer dropShipping;
    /**
     * 操作人
     */
    private String orderOperator;
    /**
     * 收货人联系方式
     */
    private String returnNumber;
    /**
     * 收货人
     */
    private String returnRealName;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 换货公司名称
     */
    private String barterPostName;
}
