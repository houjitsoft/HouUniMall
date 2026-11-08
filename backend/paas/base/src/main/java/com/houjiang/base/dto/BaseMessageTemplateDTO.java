package com.houjiang.base.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.group.Select;
import com.houjiang.commons.group.Update;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 消息模板表(BaseMessageTemplate)表实体类
 *
 * @author Amos
 * @since 2024-08-22 10:32:02
 */
@Data
public class BaseMessageTemplateDTO extends PageHelper implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @NotNull(message = "请输入id", groups = {Update.class})
    private Integer id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 站内信状态 0关闭 1开启
     */
    private Integer mailStatus;

    /**
     * 站内信内容
     */
    private String mailTemplateContent;

    /**
     * 短信状态 0关闭 1开启
     */
    private Integer smsStatus;

    /**
     * 短信内容
     */
    private String smsTemplateContent;

    /**
     * 短信模板id
     */
    private String smsTemplateId;

    /**
     * 微信通知状态 0关闭 1开启
     */
    private Integer wxStatus;

    /**
     * 微信通知内容
     */
    private String wxTemplateContent;

    /**
     * 微信模板id
     */
    private String wxTemplateId;
    /**
     * 邮箱状态 0关闭 1开启
     */
    private Integer emailStatus;
    /**
     * 邮箱内容
     */
    private String emailTemplateContent;
    /**
     * 模板类型 1会员消息 2商家消息 3平台消息 4 邮件消息
     */
    @NotNull(message = "请输入模板类型", groups = {Select.class})
    @Range(message = "模板类型为 {min} 到 {max} 岁之间", min = 1, max = 4, groups = {Select.class})
    private Integer templateType;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
