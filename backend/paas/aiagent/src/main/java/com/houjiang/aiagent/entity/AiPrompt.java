package com.houjiang.aiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hou_ai_prompt")
public class AiPrompt {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long categoryId;

    /** 提示词正文，支持 {input} 占位 */
    private String content;

    private String description;

    /** 逗号分隔 */
    private String tags;

    private Integer status;

    private Long usageCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic(value = "0", delval = "1")
    private Integer deleteFlag;
}
