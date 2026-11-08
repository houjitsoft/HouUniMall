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
 * @Description  敏感词词库
 * @Author Amos
 * @Date 2024-01-30 16:45:08
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_base_sensitive")
@Data
public class BaseSensitiveEntity  implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 敏感词
     */
    private String sensitiveName;
    /**
     * 敏感词分类id
     */
    private Integer sensitiveClassificationId;
    /**
     * 添加时间
     */
    private Date sensitiveAddTime;
    /**
     * 状态 1启用  0 不启用
     */
    private Integer sensitiveState;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 敏感词分类名称
     */
    @TableField(exist = false)
    private String sensitiveClassificationName;
}
