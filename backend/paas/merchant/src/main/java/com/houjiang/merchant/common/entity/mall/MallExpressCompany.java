package com.houjiang.merchant.common.entity.mall;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallExpressCompany implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "修改时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

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