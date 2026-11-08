package com.houjiang.job.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据大屏用户表
 * @TableName ${database.prefix.datav}hou_datav_user_info
 * @author Amos
 */

@Data
public class DcenterUserInfoVO implements Serializable {
    /**
     * 用户id
     */

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


}