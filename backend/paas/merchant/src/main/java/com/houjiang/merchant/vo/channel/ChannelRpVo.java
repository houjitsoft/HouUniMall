package com.houjiang.merchant.vo.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class ChannelRpVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Integer id;

    @Schema(description = "规则名")
    private String name;

    @Schema(description = "回复内容")
    private String content;

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "匹配方式: [1=全匹配, 2=模糊匹配]")
    private Integer matchingType;

    @Schema(description = "内容类型: [1=文本]")
    private Integer contentType;

    @Schema(description = "排序编号")
    private Integer sort;

    @Schema(description = "启动状态: [1=启动, 0=关闭]")
    private Integer status;

}
