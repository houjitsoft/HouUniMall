package com.houjiang.thirdparty.entity.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户消息收件箱实体类
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Data
@TableName("hou_message_inbox")
public class MessageInbox implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String idempotencyKey;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型: 1系统通知 2业务提醒 3待办事项
     */
    private Integer msgType;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 业务ID(点击跳转用)
     */
    private String bizId;

    /**
     * 是否已读: 0未读 1已读
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private Integer readTime;

    /**
     * 组织ID
     */
    private String orgId;

    /**
     * 创建时间
     */
    private Integer createTime;

    /**
     * 删除时间
     */
    private Integer deleteTime;
}
