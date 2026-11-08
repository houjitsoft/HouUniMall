package com.houjiang.order.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName RiderEvaluationDTO
 * @Description 骑手评价入参
 * @Author Amos
 * @Date 2024/4/29 9:33
 * @Version 1.0
 * @author Amos
 */
@Data
public class RiderEvaluationDTO extends PageHelper {

    /**
     * 订单编号
     */
    private String orderId;

    /**
     * 评分1-5分
     */
    private Integer scores;

    /**
     * 骑手id
     */
    private String riderId;
    /**
     * 店铺id
     */
    private String shopId;
    /**
     * 评论内容
     */
    private String content;
    /**
     * 用户id
     */
    private String userId;
}
