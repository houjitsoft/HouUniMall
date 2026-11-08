package com.houjiang.merchant.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class ArticleCateVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "文章数量")
    private Long articleCount;

    @Schema(description = "排序编号")
    private Integer sort;

    @Schema(description = "是否显示")
    private Integer isShow;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;

}
