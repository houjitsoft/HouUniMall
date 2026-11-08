package com.houjiang.job.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据大屏用户详细信息表
 * @TableName ${database.prefix.datav}hou_datav_user_info_detail
 * @author Amos
 */

@Data
public class DcenterUserInfoDetailVO implements Serializable {
    /**
     * 用户id
     */

    private String userId;

    /**
     * 用户头像
     */
    private String userAvatar;
    /**
     * 注册用户来源 1.pc;2.h5;3.小程序; 4:app 
     */
    private Integer userPlatform;

    /**
     * 省份id
     */
    private Integer userProvinceId;


}