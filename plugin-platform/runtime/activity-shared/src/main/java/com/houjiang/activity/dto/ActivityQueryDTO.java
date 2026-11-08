package com.houjiang.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName ActivityQueryDTO
 * @Description 活动查询DTO
 * @Author Amos
 * @Date 2024-12-29
 * @Version 1.0
 * @author Amos
 */
@Data
public class ActivityQueryDTO implements Serializable {

    @JsonAlias("pageNum")
    private Integer page = 1;

    @JsonAlias("pageSize")
    private Integer limit = 20;

    private String keyword;

    private String type;

    private String status;

    private Integer shopId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTimeBegin;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTimeEnd;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTimeBegin;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTimeEnd;

    private Integer recommend;

    private String sortField = "create_time";

    private String sortOrder = "desc";

    private static final long serialVersionUID = 1L;
}
