package com.houjiang.activity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
 /**
 * @ClassName
 * @Description  限时折扣活动表
 * @Author Amos
 * @Date 2023-05-30 18:05:20
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_addon_activity_discount_base")
@Data
public class ActivityDiscountBaseEntity  implements Serializable {
    /**
     * 限时编号
     */
    @TableId(type = IdType.AUTO)
    private Integer discountId;
    /**
     * 活动名称
     */
    private String discountName;
    /**
     * 活动开始时间
     */
    private Date discountStartTime;
    /**
     * 活动结束时间
     */
    private Date discountEndTime;
    /**
     * 用户编号
     */
    private String userId;
    /**
     * 店铺编号
     */
    private Integer shopId;
    /**
     * 用户名
     */
    private String userNickName;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 购买下限，1为不限制
     */
    private Integer discountLowerLimit;
    /**
     * 状态， 1-已开始；2-已结束；3-已失效；0-未开始；4-预热中
     */
    private Integer discountState;
    /**
     * 每种商品限购 限购次数
     */
    private Integer discountUpperLimit;
    /**
     * 活动创建时间
     */
    private Date discountCreateTime;
    /**
     * 预热时间
     */
    private Integer preStartHour;
    /**
     * 每种商品限购 限购天数
     */
    private Integer discountUpperTime;
    /**
     * 预热开始时间
     */
    private Date preStartTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
