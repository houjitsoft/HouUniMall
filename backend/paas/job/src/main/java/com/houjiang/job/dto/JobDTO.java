package com.houjiang.job.dto;



import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.io.Serializable;

/**
 * 定时任务调度信息 job
 *
 * @author Amos
 */
@Data
public class JobDTO extends PageHelper implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    private Long jobId;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务组名
     */
    private String jobGroup;

    /**
     * 调用目标字符串
     */
    private String invokeTarget;

    /**
     * cron执行表达式
     */
    private String cronExpression;

    /**
     * 任务状态  0正常 1暂停
     */
    private String status;

    /**
     * 操作人
     */
    private String createBy;

    /**
     * 操作时间
     */
    private String createTime;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * token
     */
    private String token;

    /**
     * 超时时间配置 （例如：查询超过30分钟的数据）
     */
    private String timeOutPeriod;

    /**
     * 分
     */
    private String min;

    /**
     * 时
     */
    private String hour;

    /**
     * 天
     */
    private String day;

    /**
     * 月
     */
    private String mouth;

    /**
     * 周
     */
    private String week;


}
