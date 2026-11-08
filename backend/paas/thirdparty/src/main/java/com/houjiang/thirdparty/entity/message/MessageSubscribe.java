package com.houjiang.thirdparty.entity.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户消息订阅设置实体类
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Data
@TableName("hou_message_subscribe")
public class MessageSubscribe implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 站内信: 0关闭 1开启
     */
    private Integer channelInbox;

    /**
     * 邮件: 0关闭 1开启
     */
    private Integer channelEmail;

    /**
     * 短信: 0关闭 1开启
     */
    private Integer channelSms;

    /**
     * 钉钉: 0关闭 1开启
     */
    private Integer channelDingtalk;

    /**
     * 企微: 0关闭 1开启
     */
    private Integer channelWecom;

    /**
     * 微信: 0关闭 1开启
     */
    private Integer channelWechat;

    /**
     * 飞书: 0关闭 1开启
     */
    private Integer channelFeishu;

    /**
     * 组织ID
     */
    private String orgId;

    /**
     * 创建时间
     */
    private Integer createTime;

    /**
     * 更新时间
     */
    private Integer updateTime;
}
