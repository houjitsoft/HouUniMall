package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据大屏用户表
 * @TableName ${database.prefix.datav}hou_datav_user_info
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_user_info")
@Data
public class DcenterUserInfoEntity implements Serializable {
    /**
     * 用户id
     */
    @TableId
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户积分
     */
    private Integer userIntegral;
    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}