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
public class MallOrderAfterSaleLog implements Serializable {

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

    @Schema(description = "售后id")
    private Integer afterSaleId;

    @Schema(description = "操作人 0 系统 1 管理员  2 商家 3用户")
    private Integer operatorType;

    @Schema(description = "操作人id")
    private Integer operatorId;

    @Schema(description = "操作内容")
    private String content;

}