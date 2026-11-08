package com.houjiang.order.vo;

import lombok.Data;

import java.util.Date;

/**
 * 站内信
 * @author Amos
 */
@Data
public class PlatformMessageVO {
    private Long id;
    /**
     * 接受者id
     */
    private String userId;
    /**
     * 接受者名称
     */
    private String messageUserName;
    /**
     * 消息标题
     */
    private String messageTitle;
    /**
     * 消息类型
     */
    private Integer messageType;
    /**
     * 消息内容
     */
    private String messageContent;
    /**
     * 0:未读；1:已读
     */
    private Integer isReader;
    /**
     * 创建者
     */
    private String creater;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新人
     */
    private String updater;
    /**
     * 更新时间
     */
    private Date updateTime;

}
