package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class GdbSeckillBaseVO {

    /**
     * 秒杀id
     */
    private Integer seckillId;

    /**
     * 活动名称
     */
    private String seckillName;

    /**
     * 活动标题
     */
    private String seckillTitle;

    /**
     * 活动说明
     */
    private String seckillExplain;

    /**
     * 套餐编号
     */
    private Integer comboId;
    /**
     * 活动开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date seckillStartTime;
    /**
     * 活动结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date seckillEndTime;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 用户昵称
     */
    private String userNickName;

    /**
     * 店铺名
     */
    private String shopName;
    /**
     * 购买下限，1为不限制
     */
    private Integer seckillLowerLimit;

    /**
     * 状态:0-未开始;1-已开始;2-已结束；3-已失效
     */
    private Integer seckillState;

    /**
     * 场次:1-08：00;2-12：00;3-16：00;4-20：00;5-00：00;
     */
    private Integer seckillHour;

    /**
     * 限购数量
     */
    private Integer seckillUpperLimit;

    /**
     * 参与次数限制
     */
    private Integer seckillJoinLimit;

}
