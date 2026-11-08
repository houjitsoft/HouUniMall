package com.houjiang.commons.dto.order;

import com.houjiang.commons.group.Select;
import com.houjiang.commons.vo.goods.GoodsSpecValueVO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderGoodsBarterDTO {

    /**
     * 订单id
     */
    @NotEmpty(message = "订单id不能为空", groups = {Select.class})
    private String orderId;

    /**
     * 商品id
     */
    @NotNull(message = "商品id不能为空", groups = {Select.class})
    private Integer goodsId;

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    /**
     * 版本id
     */
    private Integer verId;

    /**
     * 规格值
     */
    private List<GoodsSpecValueVO> specValues;
}
