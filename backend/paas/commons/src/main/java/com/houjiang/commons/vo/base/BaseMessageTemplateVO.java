package com.houjiang.commons.vo.base;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName
 * @Description  消息模板表
 * @Author Amos
 * @Date 2024-09-11 14:19:51
 * @Version 1.0
 * @author Amos
 */
@Data
public class BaseMessageTemplateVO {
    /**
     * 模板名称
     */
    private String templateName;
    /**
     * 模板编号
     */
    private String templateCode;
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
    private Integer templateType;
    /**
     * 创建时间
     */
    private Date createTime;

}
