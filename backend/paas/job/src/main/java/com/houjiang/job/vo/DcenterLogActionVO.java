package com.houjiang.job.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据大屏用户行为日志表
 * @TableName ${database.prefix.datav}hou_datav_log_action
 * @author Amos
 */

@Data
public class DcenterLogActionVO implements Serializable {
    /**
     * 用户id
     */

    private String userId;

    /**
     * 用户昵称 不唯一
     */
    private String userName;

    /**
     * 记录时间
     */
    private Date logTime;

    /**
     * ip地址
     */
    private String logIp;


}