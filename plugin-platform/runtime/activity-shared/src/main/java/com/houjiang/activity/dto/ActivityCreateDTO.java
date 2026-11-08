package com.houjiang.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName ActivityCreateDTO
 * @Description 活动创建DTO
 * @Author Amos
 * @Date 2024-12-29
 * @Version 1.0
 * @author Amos
 */
@Data
public class ActivityCreateDTO implements Serializable {

    @NotBlank(message = "活动名称不能为空")
    private String name;

    @NotBlank(message = "活动类型不能为空")
    private String type;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "活动开始时间不能为空")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "活动结束时间不能为空")
    private Date endTime;

    private Integer shopId;

    private String configParams;

    private Integer priority = 0;

    private Integer recommend = 0;

    private static final long serialVersionUID = 1L;
}