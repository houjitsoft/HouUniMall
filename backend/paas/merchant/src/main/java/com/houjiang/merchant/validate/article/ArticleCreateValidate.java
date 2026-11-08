package com.houjiang.merchant.validate.article;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class ArticleCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "cid参数必传且需大于0")
    @Schema(description = "分类ID", required = true)
    private Integer cid;

    @NotEmpty(message = "文章标题不能为空")
    @Length(min = 1, max = 200, message = "文章标题不能大于200个字符")
    @Schema(description = "文章标题", required = true)
    private String title;

    @Length(max = 200, message = "简介不能超出200个字符")
    @Schema(description = "文章简介")
    @JsonProperty("abstract")
    private String abstractFied = "";

    @Length(max = 200, message = "图片链接过长不能超200个字符")
    @Schema(description = "封面图片")
    private String image = "";

    @Length(max = 32, message = "作者名称不能超32个字符")
    @Schema(description = "作者姓名")
    private String author = "";

    @NotNull(message = "排序号不能为空")
    @DecimalMin(value = "0", message = "排序号值不能少于0")
    @Schema(description = "排序", required = true)
    private Integer sort;

    @NotNull(message = "缺少isShow参数")
    @IntegerContains(values = {0, 1}, message = "isShow不是合法值")
    @Schema(description = "是否显示", required = true)
    private Integer isShow;

    @Schema(description = "文章内容")
    private String content = "";

    @Schema(description = "文章描述")
    private String desc = "";

    @DecimalMin(value = "0", message = "初始浏览量不能少于0")
    @Schema(description = "初始浏览量")
    private Integer click_virtual = 0;

}
