package com.houjiang.commons.dto.goods;


import com.houjiang.commons.group.Select;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 仓库库存流水商品表(StorageInventoryFlowGoods)表实体类
 *
 * @author Amos
 * @since 2024-09-20 16:51:54
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorageInventoryFlowGoodsDTO extends PageHelper implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 流水商品id
     */
    private Integer inventoryFlowGoodsId;
    
    /**
     * 商品id
     */
    private Integer goodsId;
    
    /**
     * 店铺id
     */
    private Integer shopId;
    
    /**
     * 入库数量/出库数量/调拨数量
     */
    private Integer goodsNum;
    
    /**
     * 采购数量
     */
    private Integer buyNum;

    /**
     * 商品类型 1普通商品   2供应商商品
     */
    @NotNull(message = "商品类型不能为空", groups = {Select.class})
    private Integer goodsType;

    /**
     * 库存流水id
     */
    @NotEmpty(message = "调拨单号不能为空", groups = {Select.selectGoods.class})
    private String inventoryFlowId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 入库仓库id
     */
    private Integer putStorageId;

    /**
     * 出库仓库id
     */
    private Integer outStorageId;

    /**
     * 商品id集合
     */
    @NotEmpty(message = "商品id集合不能为空", groups = {Select.goodsIdList.class})
    private List<Integer> goodsIdList;

    /**
     * 库存流水id
     */
    @NotEmpty(message = "订单号不能为空", groups = {Select.orderId.class})
    private String orderId;

    public StorageInventoryFlowGoodsDTO(Integer outStorageId, Integer putStorageId) {
        this.outStorageId = outStorageId;
        this.putStorageId = putStorageId;
    }
}
