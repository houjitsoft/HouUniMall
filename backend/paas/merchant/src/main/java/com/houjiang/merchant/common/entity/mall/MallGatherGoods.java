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
public class MallGatherGoods implements Serializable {

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

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "采集id")
    private Integer gatherId;

    @Schema(description = "状态 1成功 0失败")
    private Integer status;

    @Schema(description = "已处理 1是 0否")
    private Integer deal;

    @Schema(description = "链接")
    private String link;

    @Schema(description = "商品内容")
    private String content;

    @Schema(description = "采集记录")
    private String gather;

    @Schema(description = "渠道")
    private Integer channel;

}