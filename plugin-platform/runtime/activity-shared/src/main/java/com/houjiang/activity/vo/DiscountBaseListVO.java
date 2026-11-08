package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName DiscountBaseListVO
 * @Description 限时活动详情
 * @Author Amos
 * @Date 2023/7/3 15:12
 * @Version 1.0
 * @author Amos
 */
@Data
public class DiscountBaseListVO {
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


    private String shopName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date discountCreateTime;
}
