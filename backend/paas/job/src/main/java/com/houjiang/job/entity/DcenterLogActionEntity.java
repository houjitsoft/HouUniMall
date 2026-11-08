package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据大屏用户行为日志表
 * @TableName ${database.prefix.datav}hou_datav_log_action
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_log_action")
@Data
public class DcenterLogActionEntity implements Serializable {
    /**
     * 用户id
     */
    @TableId
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}