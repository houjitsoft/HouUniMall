package com.houjiang.goods.controller;


import com.houjiang.commons.dto.goods.StorageInventoryFlowDTO;
import com.houjiang.commons.dto.goods.StorageInventoryFlowGoodsDTO;
import com.houjiang.commons.group.Select;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.vo.goods.PurchaseOrderVO;
import com.houjiang.commons.vo.goods.StorageInventoryFlowGoodsVO;
import com.houjiang.goods.service.StorageInventoryFlowGoodsService;
import com.houjiang.goods.vo.InventoryFlowVO;
import com.houjiang.goods.vo.StorageGoodsStockVO;
import com.houjiang.goods.vo.StorageInventoryFlowVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 仓库库存流水商品表(StorageInventoryFlowGoods)表控制层
 *
 * @author Amos
 * @since 2024-09-20 16:51:54
 */
@RestController
@RequestMapping("storageInventoryFlowGoods")
public class StorageInventoryFlowGoodsController {

    /**
     * 服务对象
     */
    @Resource
    private StorageInventoryFlowGoodsService storageInventoryFlowGoodsService;

    /**
     * 查询库存流水货物信息
     * <p>
     * 通过POST请求接收客户端查询请求，根据提供的查询参数（StorageInventoryFlowGoodsDTO对象），调用库存流水货物信息的服务，
     * 来获取库存流水货物的分页数据。返回的数据结构包括总记录数、当前页码、总页数以及数据列表。
     *
     * @param param 查询参数，遵循Select组的校验规则，用于指定查询条件和分页信息
     * @return 包含分页数据的ResponseResult对象，其中数据类型为PageUtils<StorageInventoryFlowGoodsVO>，
     *         包含总记录数、当前页码、总页数以及数据列表
     */
    @PostMapping("/query")
    public ResponseResult<PageUtils<StorageInventoryFlowGoodsVO>> queryStorageInventoryFlowGoods(@Validated({Select.class}) @RequestBody StorageInventoryFlowGoodsDTO param) {
        return storageInventoryFlowGoodsService.queryStorageInventoryFlowGoods(param);
    }

    /**
     * 查询调拨单订单商品信息
     * <p>
     * 通过POST请求接收客户端发送的商品查询参数，以查询相应的转单订单商品信息
     * 该方法委托给storageInventoryFlowGoodsService中的queryTransferOrderGoods方法执行实际的查询逻辑
     *
     * @param param 请求体，包含查询所需的参数，符合Select.selectGoods校验组的规则
     * @return 返回包含库存流动信息的ResponseResult对象
     */
    @PostMapping("/queryTransferOrderGoods")
    public ResponseResult<StorageInventoryFlowVO> queryTransferOrderGoods(@Validated({Select.selectGoods.class}) @RequestBody StorageInventoryFlowGoodsDTO param) {
        return storageInventoryFlowGoodsService.queryTransferOrderGoods(param);
    }

    /**
     * 查询采购订单商品信息
     *
     * @param param 请求体，包含查询所需的参数
     * @return 返回采购订单的VO对象封装的响应结果
     */
    @PostMapping("/queryPurchaseOrderGoods")
    public ResponseResult<PurchaseOrderVO> queryPurchaseOrderGoods(@Validated({Select.orderId.class}) @RequestBody StorageInventoryFlowGoodsDTO param) {
        return storageInventoryFlowGoodsService.queryPurchaseOrderGoods(param);
    }

    /**
     * 查询仓库商品库存信息
     * <p>
     * 通过POST请求接收一个StorageInventoryFlowGoodsDTO参数对象，用于查询特定商品的库存信息
     * 该方法利用了Spring MVC的@Validated注解来校验参数对象是否合法，确保了请求数据的正确性
     * 同时，@RequestBody注解使得请求体中的数据能够绑定到方法参数上
     * 最终，该方法调用storageInventoryFlowGoodsService中的queryStorageGoodsStock方法，
     * 返回一个封装了商品库存信息列表的ResponseResult对象
     *
     * @param param 包含查询条件的StorageInventoryFlowGoodsDTO对象
     * @return 包含商品库存信息列表的ResponseResult对象
     */
    @PostMapping("/queryStorageGoodsStock")
    public ResponseResult<List<StorageGoodsStockVO>> queryStorageGoodsStock(@Validated({Select.goodsIdList.class}) @RequestBody StorageInventoryFlowGoodsDTO param) {
        return storageInventoryFlowGoodsService.queryStorageGoodsStock(param);
    }

    /**
     * 查询库存流水列表信息
     * <p>
     * 通过POST请求接收客户端查询请求，根据提供的查询参数（StorageInventoryFlowDTO），调用库存流水货物信息的服务，
     * 来获取库存流水货物的分页数据。返回的数据结构包括总记录数、当前页码、总页数以及数据列表。
     *
     * @param param 查询参数，遵循Select组的校验规则，用于指定查询条件和分页信息
     * @return 包含分页数据的ResponseResult对象，其中数据类型为PageUtils<InventoryFlowVO>，
     *         包含总记录数、当前页码、总页数以及数据列表
     */
    @PostMapping("/queryInventoryFlowList")
    public ResponseResult<PageUtils<InventoryFlowVO>> queryInventoryFlowList(@RequestBody StorageInventoryFlowDTO param) {
        return storageInventoryFlowGoodsService.queryInventoryFlowList(param);
    }

    /**
     * 导出库存流水列表信息
     * <p>
     * 通过POST请求接收客户端查询请求，根据提供的查询参数（StorageInventoryFlowDTO），调用库存流水货物信息的服务，
     * 来获取库存流水货物的分页数据。返回的数据结构包括总记录数、当前页码、总页数以及数据列表。
     *
     * @param param 查询参数，遵循Select组的校验规则，用于指定查询条件和分页信息
     * @return 包含分页数据的ResponseResult对象，其中数据类型为PageUtils<InventoryFlowVO>，
     *         包含总记录数、当前页码、总页数以及数据列表
     */
    @PostMapping("/exportInventoryFlowList")
    public ResponseResult<String> exportInventoryFlowList(@RequestBody StorageInventoryFlowDTO param) {
        return storageInventoryFlowGoodsService.exportInventoryFlowList(param);
    }

}

