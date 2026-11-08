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
 * @Description  限时秒杀活动表
 * @Author Amos
 * @Date 2023-07-04 17:26:45
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_addon_activity_seckill_base")
@Data
public class ActivitySeckillBaseEntity  implements Serializable {
    /**
     * 限时编号
     */
    @TableId(type = IdType.AUTO)
    private Integer seckillId;
    /**
     * 活动名称
     */
    private String seckillName;
    /**
     * 活动开始时间
     */
    private Date seckillStartTime;
    /**
     * 活动结束时间
     */
    private Date seckillEndTime;
    /**
     * 用户编号
     */
    private Object userId;
    /**
     * 店铺编号
     */
    private Object shopId;
    /**
     * 用户名
     */
    private String userNickName;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 状态:0-未开始;1-已开始;2-已结束；3-已失效
     */
    private String seckillState;
    /**
     * 场次:
     *  1-08：00;
     *  2-12：00;
     *  3-16：00;
     *  4-20：00;
     *  5-00：00;
     */
    private Integer seckillHour;
    /**
     * 活动创建时间
     */
    private Date seckillCreateTime;
    /**
     * 活动开始日期
     */
    private Date seckillStartDate;
    /**
     * 订单取消时间(分钟)
     */
    private Integer cancelTime;
    /**
     * 推荐标志 0-未推荐 1-已推荐
     */
    private Integer seckillDoodsRecommend;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
