package com.houjiang.commons.vo.sensitive;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName
 * @Description  敏感词词库
 * @Author Amos
 * @Date 2024-01-30 16:45:08
 * @Version 1.0
 * @author Amos
 */
@Data
public class BaseSensitiveVO {
    /**
     * id
     */
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

    /**
     * 敏感词分类名称
     */
    private String classificationName;

    /**
     * 敏感词分类 状态 1启用  0 不启用
     */
    private Integer classificationState;
}
