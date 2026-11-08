package com.houjiang.aiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话消息。除支撑多轮对话外，它还是合规留痕的真相源，
 * 因此不做物理删除（delete_flag 仅用于「清空会话」的软标记）。
 */
@Data
@TableName("hou_ai_chat_message")
public class AiChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    /** user / assistant / system / tool */
    private String role;

    private String content;

    private String modelName;

    private Integer tokens;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic(value = "0", delval = "1")
    private Integer deleteFlag;
}
