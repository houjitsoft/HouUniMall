package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 定时任务执行日志
 * @author Amos
 */
@Data
@TableName("hou_job_execution_log")
public class JobExecutionLogEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long jobId;

    private String jobName;

    private String invokeTarget;

    /** running/success/failed */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    private Long durationMs;

    private String errorMsg;
}
