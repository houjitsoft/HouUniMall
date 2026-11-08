package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据大屏用户详细信息表
 * @TableName ${database.prefix.datav}hou_datav_user_info_detail
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_user_info_detail")
@Data
public class DcenterUserInfoDetailEntity implements Serializable {
    /**
     * 用户id
     */
    @TableId
    private String userId;

    /**
     * 用户头像
     */
    private String userAvatar;
    /**
     * 注册用户来源 1.pc;2.h5;3.小程序; 4:app 
     */
    private Integer userPlatform;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}