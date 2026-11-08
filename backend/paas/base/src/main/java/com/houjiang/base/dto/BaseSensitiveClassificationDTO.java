package com.houjiang.base.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName
 * @Description  敏感词分类
 * @Author Amos
 * @Date 2024-01-30 16:48:04
 * @Version 1.0
 * @author Amos
 */
@Data
public class BaseSensitiveClassificationDTO extends PageHelper {
    /**
     * id
     */
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

}
