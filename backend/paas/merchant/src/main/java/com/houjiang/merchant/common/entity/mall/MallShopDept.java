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
public class MallShopDept implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;
    @Schema(description = "店铺id")
    private Integer shopId;
    @Schema(description = "上级部门")
    private Integer pid;

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "负责人名")
    private String contact;

    @Schema(description = "联系电话")
    private String mobile;

    @Schema(description = "排序编号")
    private Integer sort;

    @Schema(description = "是否禁用: [0=否, 1=是]")
    private Integer status;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

}
