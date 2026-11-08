package com.houjiang.merchant.vo.pt;

import com.houjiang.merchant.vo.mallorder.MallOrderSimpleGoodsVo;
import com.houjiang.merchant.vo.user.UserVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class PtOpenVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

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

    @Schema(description = "参团人数详细")
    List<PtOpenJoinListedVo> joinList;
}
