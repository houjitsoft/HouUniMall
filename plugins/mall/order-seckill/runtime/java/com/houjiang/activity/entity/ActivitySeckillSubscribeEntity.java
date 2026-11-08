package com.houjiang.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 限时秒杀开抢预约表
 *
 * <p>用户针对某场次内某个秒杀商品预约，开抢时由 job 服务读取本表发站内信 + 短信提醒。
 *
 * @author Amos
 */
@TableName("hou_addon_activity_seckill_subscribe")
@Data
public class ActivitySeckillSubscribeEntity implements Serializable {

    /** 预约编号 */
    @TableId(type = IdType.AUTO)
    private Integer subscribeId;

    /** 限时秒杀场次编号 */
    private Integer seckillId;

    /** 秒杀商品编号（规格级 goods_id） */
    private Integer goodsId;

    /** 商品 SPU 编号 */
    private Integer commonId;

    /** 店铺编号 */
    private Integer shopId;

    /** 预约用户编号 */
    private String userId;

    /** 预约时快照的手机号（供开抢短信提醒） */
    private String userMobile;

    /** 商品名称快照 */
    private String goodsName;

    /** 场次开抢时间快照（供定时提醒调度） */
    private Date seckillStartTime;

    /** 预约时间 */
    private Date subscribeTime;

    /** 提醒状态：0-未提醒 1-已提醒 2-提醒失败 */
    private Integer remindStatus;

    /** 提醒发送时间 */
    private Date remindTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
