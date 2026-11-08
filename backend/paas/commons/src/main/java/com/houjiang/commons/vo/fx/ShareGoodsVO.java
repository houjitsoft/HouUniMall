package com.houjiang.commons.vo.fx;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Amos
 * @date 2021-06-0517:17
 * @Description: 分享商品购买记录
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareGoodsVO {
    private String buyUserId;
    private String shareUserId;
    private Integer shopId;
    private BigDecimal orderPrice;
    private Integer goodsNum;
    private Integer goodsId;
    private BigDecimal tgCommission;
    private BigDecimal fxCommission;
    private Integer shareNum;
    private Integer inviteNum;
    private String orderId;
    /**
     * 邀请类型 1申请微客邀请   2首次分享购买邀请
     */
    private Integer inviteType;
    /** 分享 1 推广 2 */
    private Integer fxTg;
    /** 微客上级id */
    private String vkShareUserId;
}
