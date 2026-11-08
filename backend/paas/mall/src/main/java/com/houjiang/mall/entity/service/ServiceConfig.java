package com.houjiang.mall.entity.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客服配置实体类
 * 对应数据库表：hou_mall_service_config
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_service_config")
public class ServiceConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 店铺 ID（唯一索引）
     */
    private Long shopId;

    /**
     * 客服 QQ
     */
    private String serviceQq;

    /**
     * 客服微信
     */
    private String serviceWechat;

    /**
     * 客服电话
     */
    private String servicePhone;

    /**
     * 客服邮箱
     */
    private String serviceEmail;

    /**
     * 服务时间
     */
    private String serviceHours;

    /**
     * 自动回复内容
     */
    private String autoReply;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
