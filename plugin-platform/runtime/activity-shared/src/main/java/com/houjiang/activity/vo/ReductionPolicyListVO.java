package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class ReductionPolicyListVO {
    /**
     * 活动ID
     */
    private Integer promotionId;
    /**
     *  活动名称
     */
    private String promotionName;
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    /**
     * 活动状态:1-正常;0-已结束;2-管理员已关闭
     */
    private Integer promotionStatus;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdTime;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 店铺ID
     */
    private Integer shopId;
    /**
     * 有无赠品
     */
    private Integer hasGiveaway;
    /**
     * 活动内容
     */
    private String promotionContent;


}
