package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallExpressCompanyListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "图标")
    private String image;

    @Schema(description = "快递编码")
    private String codeKd;

    @Schema(description = "快递100编码")
    private String codeKd100;

    @Schema(description = "快递鸟编码")
    private String codeKdniao;

    @Schema(description = "排序")
    private Integer sort;

}
