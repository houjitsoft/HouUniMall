package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName DiscountBaseDetailResultVO
 * @Author Amos
 * @Date 2023/7/3 17:02
 * @Version 1.0
 * @author Amos
 */
@Data
public class DiscountBaseDetailResultVO {
    /**
     * 限时编号
     */
    private Integer discountId;
    /**
     * 活动名称
     */
    private String discountName;
    /**
     * 活动标题
     */
    private String discountTitle;
    /**
     * 活动说明
     */
    private String discountExplain;
    /**
     * 活动开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date discountStartTime;
    /**
     * 活动结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date discountEndTime;
    /**
     * 购买下限，1为不限制
     */
    private Integer discountLowerLimit;
    /**
     * 状态， 1-正常/2-结束/3-管理员关闭
     */
    private Integer discountState;
    /**
     * 购买上限，1为不限制
     */
    private Integer discountUpperLimit;
    /**
     * 推荐标志 0-未推荐 1-已推荐
     */
    private Integer discountGoodsRecommend;
    /**
     * 购买上限
     */
    private Integer goodsUpperLimit;
    /**
     * 已购买的个数
     */
    private Integer goodsBuyCount;
    /**
     * 活动预热时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date preStartTime;
    /**
     * 预热时间
     */
    private Integer preStartHour;
    /**
     *每种商品限购 限购天数
     */
    private Integer discountUpperTime;
}
