package com.houjiang.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName ActivityUpdateDTO
 * @Description 活动更新DTO
 * @Author Amos
 * @Date 2024-12-29
 * @Version 1.0
 * @author Amos
 */
@Data
public class ActivityUpdateDTO implements Serializable {

    @NotNull(message = "活动ID不能为空")
    private Integer activityId;

    @NotNull(message = "活动类型不能为空")
    private String activityType;

    private String name;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private Integer status;

    private String configParams;

    private Integer priority;

    private Integer recommend;

    private static final long serialVersionUID = 1L;
}