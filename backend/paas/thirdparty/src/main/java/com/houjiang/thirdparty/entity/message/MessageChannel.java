package com.houjiang.thirdparty.entity.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 消息渠道配置实体类
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Data
@TableName("hou_message_channel")
public class MessageChannel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 渠道编码: INBOX/EMAIL/SMS/DINGTALK/WECOM/WECHAT/FEISHU
     */
    private String channelCode;

    /**
     * 渠道名称
     */
    private String channelName;

    /**
     * 渠道配置JSON(AppKey/Secret等)
     */
    private String config;

    /**
     * 状态: 0禁用 1启用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

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
