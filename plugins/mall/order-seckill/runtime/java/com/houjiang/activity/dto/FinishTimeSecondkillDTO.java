package com.houjiang.activity.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class FinishTimeSecondkillDTO extends PageHelper {

    /**
     * 限时编号
     */
    private int []  seckillIds;
    /**
     * 活动名称
     */
    private String seckillName;
    /**
     * 活动名称
     */
    private String shopName;

    /**
     * 状态:0-未开始;1-已开始;2-已结束；3-已失效
     */
    private Integer seckillState;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 店铺id
     */
    private Integer shopId;
}
