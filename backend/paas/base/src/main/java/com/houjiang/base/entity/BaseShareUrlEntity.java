package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 分享地址
 * @TableName base_share_url
 * @author Amos
 */
@TableName(value ="hou_base_share_url")
@Data
public class BaseShareUrlEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 描述
     */
    private String shareDesc;

    /**
     * 分享路径
     */
    private String shareUrl;

    /**
     * 1自由拼团分享路径
     */
    private Byte type;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}