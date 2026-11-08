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
 * @Description  敏感词命中记录
 * @Author Amos
 * @Date 2024-01-30 16:48:44
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_base_sensitive_record")
@Data
public class BaseSensitiveRecordEntity  implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 文本内容
     */
    private String sensitiveTxt;
    /**
     * 敏感词
     */
    private String sensitiveName;
    /**
     * 敏感词分类id
     */
    private Integer classificationId;
    /**
     * 命中时间
     */
    private Date recordAddTime;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 用户名称
     */
    private String userName;
    /**
     * 用户类别 1 普通用户 2 平台用户
     */
    private Integer userType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
