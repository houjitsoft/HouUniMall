package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据大屏用户访问表
 * @TableName ${database.prefix.datav}hou_datav_user_access
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_user_access")
@Data
public class DcenterUserAccessEntity implements Serializable {

    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 商品id
     */
    private String goodsId;

    /**
     * 添加时间
     */
    private String addTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}