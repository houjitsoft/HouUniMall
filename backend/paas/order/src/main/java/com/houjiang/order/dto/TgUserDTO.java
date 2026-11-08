package com.houjiang.order.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @ClassName TgUserDTO
 * @Description 推广客户
 * @Author Amos
 * @Date 2023/8/22 11:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class TgUserDTO extends PageHelper {
    /**
     * 用户授权的token
     */
    private String token;

    /**
     * 购买用户名
     */
    private String buyUserName;

    /**
     * 购买用户的ID
     */
    private String buyUserId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 1查询今日数据 2全部
     */
    private Integer todayState;

    /**
     * 退款 4
     */
    private Integer orderStatus;

    /**
     * 1绑定 2未绑定  3全部
     */
    private Integer bindType;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 支付时间
     */
    private String payTime;


    /**
     *  1降序  2升序
     */
    private Integer sortType;

    /**
     * 1默认  2绑定时间  3交易时间  4订单数量  5交易金额
     */
    private Integer orderType;
}
