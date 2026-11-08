package com.houjiang.commons.dto.sensitive;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName
 * @Description  敏感词命中记录
 * @Author Amos
 * @Date 2024-01-30 16:48:44
 * @Version 1.0
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BaseSensitiveRecordDTO extends PageHelper {
    /**
     * id
     */
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
     * 命中开始时间
     */
    private Date recordStartTime;
    /**
     * 命中结束时间
     */
    private Date recordEndTime;
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

}
