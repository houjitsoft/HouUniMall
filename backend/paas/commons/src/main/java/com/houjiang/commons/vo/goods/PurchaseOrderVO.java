package com.houjiang.commons.vo.goods;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 仓库库存流水表(StorageInventoryFlow)表实体类
 *
 * @author Amos
 * @since 2024-09-20 16:51:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 订单id
     */
    private String orderId;

    /**
     * 进货编号
     */
    private String purchaseNumber;
    
    /**
     * 店铺id
     */
    private Integer shopId;
    
    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 操作商品数量
     */
    private Integer inventoryFlowNum;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 商品集合
     */
    private List<StorageInventoryFlowGoodsVO> goodsList;

}
