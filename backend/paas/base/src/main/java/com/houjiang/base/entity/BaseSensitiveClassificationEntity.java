package com.houjiang.base.entity;


import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableField;
/**
 * @ClassName
 * @Description  敏感词分类
 * @Author Amos
 * @Date 2024-01-30 16:48:04
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_base_sensitive_classification")
@Data
public class BaseSensitiveClassificationEntity  implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer classificationId;
    /**
     * 分类名称
     */
    private String classificationName;
    /**
     * 添加时间
     */
    private Date classificationAddTime;
    /**
     * 状态 1启用  0 不启用
     */
    private Integer classificationState;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
