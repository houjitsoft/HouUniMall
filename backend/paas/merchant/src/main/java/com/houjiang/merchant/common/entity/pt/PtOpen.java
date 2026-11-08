package com.houjiang.merchant.common.entity.pt;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class PtOpen implements Serializable {

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

    @Schema(description = "拼团活动id")
    private Integer ptId;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "失效时间")
    private Integer timeout;

    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "订单商品id")
    private Integer orderGoodsId;

    @Schema(description = "状态 0拼团中 1拼团成功 2拼团失败")
    private Integer status;

    @Schema(description = "开团人数")
    private Integer userNum;

    @Schema(description = "参团人数")
    private Integer joinNum;

}