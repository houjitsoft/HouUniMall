package com.houjiang.aiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("hou_ai_generation_record")
public class AiGenerationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** text_generate / summary / translate / correct / image_recognize / data_analyze … */
    private String scene;

    private String modelName;

    private String prompt;

    private String result;

    /** 1 成功 0 失败 */
    private Integer status;

    private String errorMsg;

    private Integer tokens;

    private BigDecimal costAmount;

    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
