package com.houjiang.order.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName FxOrderListDTO
 * @Description 结算查询
 * @Author Amos
 * @Date 2023/8/21 18:32
 * @Version 1.0
 * @author Amos
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserFxOrderListDTO extends PageHelper {

    /**
     * 买家昵称
     */
    private String userName;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 状态 null全部 1待支付  2未入账  3已入账  4已退款  5已取消 6已冻结
     */
    private Integer orderStatus;

    /**
     * 下单开始时间
     */
    private String orderStartTime;

    /**
     * 下单开始时间
     */
    private String orderEndTime;

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 店铺Id
     */
    private String shopId;
    /**
     * 商品名称
     */
    private String commonName;
    /**
     * 排序规则
     */
    private Integer collation;
    /**
     * 状态 null全部 1直接订单 2间接订单
     */
    private Integer orderType;
    /**
     * 订单时间 null全部 1今天 2七天 3一个月 4三个月 5一年
     */
    private Integer orderTime;
    /**
     * 购买用户id
     */
    private String buyUserId;

}
