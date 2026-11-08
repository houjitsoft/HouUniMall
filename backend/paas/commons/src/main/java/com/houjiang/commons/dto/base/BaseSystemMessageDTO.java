package com.houjiang.commons.dto.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName BaseSystemMessageEntity
 * @Description 系统消息
 * @Author Amos
 * @Date 2023/7/19 15:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class BaseSystemMessageDTO implements Serializable {

    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Integer messageId;

    /**
     * 消息所属用户ID
     */
    private String messageUserId;

    /**
     * 消息所属用户名
     */
    private String messageUserName;

    /**
     * 消息类型
     */
    private Integer messageType;

    /**
     * 消息标题
     */
    private String messageTitle;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 消息是否已查看，0为未查看，1为已查看
     */
    private Integer messageIslook;

    /**
     * 消息创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date messageCreateTime;

    /**
     * 消息是否被删除，0为未删除，1为已删除
     */
    private Integer messageIsdel;

    /**
     * 消息模式
     */
    private Integer messageMold;

    /**
     * 商铺ID
     */
    private String shopId;

    /**
     * 创建者
     */
    private String createUser;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新者
     */
    private String updateUser;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 是否删除，0为未删除，1为已删除
     */
    private Integer isDelete;

    /**
     * 系统版本
     */
    private Integer sysVersion;

    /**
     * 时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date ts;

    /**
     * 消息所属订单ID
     */
    private String messageOrderId;

    /**
     * 消息处理状态
     */
    private Integer messageProcessingStatus;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
