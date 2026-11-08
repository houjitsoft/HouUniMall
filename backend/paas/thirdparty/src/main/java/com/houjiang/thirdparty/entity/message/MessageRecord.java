package com.houjiang.thirdparty.entity.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 消息发送记录实体类
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Data
@TableName("hou_message_record")
public class MessageRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String idempotencyKey;

    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 业务ID
     */
    private String bizId;

    private String eventType;

    /**
     * 发送渠道
     */
    private String channel;

    /**
     * 接收人ID
     */
    private String receiverId;

    /**
     * 接收人姓名
     */
    private String receiverName;

    /**
     * 接收人联系方式(手机/邮箱)
     */
    private String receiverContact;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 发送状态: 0待发送 1发送中 2成功 3失败
     */
    private Integer sendStatus;

    /**
     * 发送时间
     */
    private Integer sendTime;

    /**
     * 失败原因
     */
    private String failReason;

    private String errorCode;

    private String providerMessageId;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 组织ID
     */
    private String orgId;

    /**
     * 创建时间
     */
    private Integer createTime;
}
