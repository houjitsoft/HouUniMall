package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统队列表
 * @author Amos
 */
@Data
@TableName("hou_sys_queue")
public class SysQueueEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String module;

    /** pending/running/completed/failed/cancelled */
    private String status;

    /** high/medium/low */
    private String priority;

    private Integer progress;

    private Integer retryCount;

    private Integer maxRetry;

    private String payload;

    private String errorMsg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date finishedAt;
}
