package com.houjiang.merchant.vo.article;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class ArticleListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "分类")
    private String cateName;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "图片")
    private String image;

    @Schema(description = "作者")
    private String author;

    @Schema(description = "简介")
    @TableField(value = "`desc`")
    private String desc;

    @Schema(description = "摘要")
    @JsonProperty("abstract")
    private String abstractField;

    @Schema(description = "访问")
    private Integer click;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "虚拟浏览量")
    private Integer clickVirtual;

    @Schema(description = "实际浏览量")
    private Integer clickActual;

    @Schema(description = "是否显示")
    private Integer isShow;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;

}
