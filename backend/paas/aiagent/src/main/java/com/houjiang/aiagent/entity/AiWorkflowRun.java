package com.houjiang.aiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 中台 - 工作流运行记录。
 *
 * <p>每次「试运行」或正式调用都落一条：入参、出参、每个节点的耗时与中间结果（traces）、
 * 失败原因。后台据此回放一次运行，排查是哪个节点慢或哪一步答错。
 */
@Data
@TableName("hou_ai_workflow_run")
public class AiWorkflowRun {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 运行编号，如 WFR20261029083000123 */
    private String runNo;

    private String workflowCode;

    /** 运行时的名称快照（工作流改名后仍能对上） */
    private String workflowName;

    private String terminal;

    /** running 执行中 / success 成功 / failed 失败 */
    private String status;

    /** 入参 JSON */
    private String inputs;

    /** 出参 JSON */
    private String outputs;

    /** 节点轨迹 JSON 数组：[{nodeId,name,type,status,costMs,input,output,error}] */
    private String traces;

    private String errorMsg;

    /** 总耗时（毫秒） */
    private Integer costMs;

    /** 累计 token */
    private Integer tokenUsed;

    private LocalDateTime createTime;

    private LocalDateTime finishTime;
}
