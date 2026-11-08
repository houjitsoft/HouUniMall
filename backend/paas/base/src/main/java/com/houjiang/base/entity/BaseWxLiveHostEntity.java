package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName BaseWxLiveHostEntity
 * @Description 微信直播主播申请表
 * @Author Amos
 * @Date 2023/7/24 13:34
 * @Version 1.0
 * @author Amos
 */
@TableName(value ="hou_base_wx_live_host")
@Data
public class BaseWxLiveHostEntity {

    /**
     * ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 商铺ID
     */
    private Integer shopId;

    /**
     * 微信ID
     */
    private String wechatId;

    /**
     * 头像
     */
    private String headPortrait;

    /**
     * 创建时间
     */
    private Date creataTime;

    /**
     * 操作
     */
    private Integer operation;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 原因
     */
    private String cause;

    /**
     * 主机状态
     */
    private Integer hoststate;
}
