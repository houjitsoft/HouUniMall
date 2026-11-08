package com.houjiang.activity.dto;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class TimeSecondkillDTO extends PageHelper {
    /**
     * 限时编号
     */
    private int  seckillId;
    /**
     *活动名称
     */
    private String seckillName;
    /**
     *活动标题
     */
    private String seckillTitle;
    /**
     *活动说明
     */
    private String seckillExplain;
    /**
     *活动开始时间
     */
    private String seckillStartTime;
    /**
     *活动结束时间
     */
    private String seckillEndTime;
    /**
     *用户ID
     */
    private int userId;
    /**
     *店铺id
     */
    private int shopId;
    /**
     *店铺名称
     */
    private String shopName;
    /**
     *购买下限，1为不限制',
     */
    private String seckillLowerLimit;
    /**
     *'状态， 1-正常/2-结束/3-管理员关闭'
     */
    private String seckillState;
    /**
     *场次
     */
    private int seckillHour;
    /**
     *最高购买数量
     */
    private int seckillUpperLimit;
    /**
     *参与次数限制
     */
    private int seckillJoinLimit;
    /**
     *活动创建时间
     */
    private String seckillCreateTime;


}
