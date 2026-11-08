package com.houjiang.thirdparty.entity.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 极光推送历史记录实体
 *
 * @author Amos
 * @since 2025-07-02
 */
@Data
@TableName("hou_message_jpush_history")
public class JPushHistoryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 组织ID */
    private String orgId;

    /** 推送类型：single/group/tag/broadcast */
    private String pushType;

    /** 推送标题 */
    private String title;

    /** 推送内容 */
    private String content;

    /** 目标类型：registration_id/alias/tag/all */
    private String targetType;

    /** 目标值（JSON数组） */
    private String targetValue;

    /** 推送平台：android/ios/all */
    private String platform;

    /** 扩展字段(JSON) */
    private String extras;

    /** 消息类型：notification/message */
    private String msgType;

    /** 发送状态：0-待发送 1-成功 2-失败 3-部分成功 */
    private Integer sendStatus;

    /** 发送时间 */
    private LocalDateTime sendTime;

    /** 目标设备数 */
    private Integer targetCount;

    /** 成功送达数 */
    private Integer successCount;

    /** 失败数 */
    private Integer failCount;

    /** 点击数 */
    private Integer clickCount;

    /** 极光返回的发送ID */
    private String jpushSendId;

    /** 错误信息 */
    private String errorMsg;

    /** 操作人ID */
    private Integer operatorId;

    /** 操作人姓名 */
    private String operatorName;

    private LocalDateTime createTime;
}
