package com.houjiang.thirdparty.entity.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 消息模板实体类
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Data
@TableName("hou_base_message_template")
public class MessageTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 模板编码(唯一)
     */
    private String templateCode;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 业务类型: PROJECT/TASK/ORDER/WORKFLOW等
     */
    private String bizType;

    /**
     * 标题模板
     */
    private String titleTemplate;

    /**
     * 内容模板(支持变量如${projectName})
     */
    private String contentTemplate;

    /**
     * 支持渠道(逗号分隔): INBOX,EMAIL,SMS
     */
    private String channels;

    /**
     * 站内信模板CODE
     */
    private String inboxTemplateCode;

    /**
     * 邮件模板CODE
     */
    private String emailTemplateCode;

    /**
     * 短信模板CODE
     */
    private String smsTemplateCode;

    /**
     * 钉钉模板CODE
     */
    private String dingtalkTemplateCode;

    /**
     * 企业微信模板CODE
     */
    private String wecomTemplateCode;

    /**
     * 微信公众号模板CODE
     */
    private String wechatTemplateCode;

    /**
     * 飞书模板CODE
     */
    private String feishuTemplateCode;

    /**
     * 状态: 0禁用 1启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 组织ID
     */
    private String orgId;

    /**
     * 创建时间
     */
    private Integer createTime;

    /**
     * 更新时间
     */
    private Integer updateTime;

    /**
     * 删除时间
     */
    private Integer deleteTime;
}
