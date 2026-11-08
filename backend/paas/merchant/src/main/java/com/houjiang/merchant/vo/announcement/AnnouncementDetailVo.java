package com.houjiang.merchant.vo.announcement;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class AnnouncementDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Integer id;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "优先级 1紧急 2重要 3提示")
    private Integer priority;
    @Schema(description = "优先级 1紧急 2重要 3提示")
    @JsonProperty("priority_name")
    private String priorityName;

    @Schema(description = "创建时间")
    @JsonProperty("create_time")
    private String createTime;

    @Schema(description = "更新时间")
    @JsonProperty("update_time")
    private String updateTime;


}
