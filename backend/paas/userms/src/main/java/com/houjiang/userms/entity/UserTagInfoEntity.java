package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName user_tag_info
 * @author Amos
 */
@TableName(value ="hou_user_tag_info")
@Data
public class UserTagInfoEntity implements Serializable {
    /**
     * 会员标签id
     */
    @TableId(type = IdType.AUTO)
    private Integer tagId;

    /**
     * 会员标签name
     */
    private String tagName;

    /**
     * 会员标签权重
     */
    private Integer tagWeight;

    /**
     * 会员标签图片
     */
    private String tagImage;

    /**
     * 会员标签描述
     */
    private String tagDescribe;

    /**
     * 会员标签新增时间
     */
    private Date tagAddData;

    /**
     * 会员标签00删除 01正常，默认正常
     */
    private Integer tagIsactive;

    /**
     * 会员标签更新时间
     */
    private Date tagUpdData;

    /**
     * 
     */
    private Date date;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}