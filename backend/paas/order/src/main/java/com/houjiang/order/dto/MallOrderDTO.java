package com.houjiang.order.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @ClassName MallOrderDTO
 * @Description 商家中心分销业绩查询
 * @Author Amos
 * @Date 2023/8/9 19:13
 * @Version 1.0
 * @author Amos
 */
@Data
public class MallOrderDTO extends PageHelper {

    /**
     * token
     */
    private String token;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 购买用户名称
     */
    private String buyUserName;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 结算状态
     */
    private Integer settleState;

    /**
     * 分享用户名称
     */
    private String shareUserName;

    /**
     * 分享用户手机号
     */
    private String shareUserMobile;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

}
