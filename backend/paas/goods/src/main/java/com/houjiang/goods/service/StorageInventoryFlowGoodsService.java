package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.goods.StorageInventoryFlowDTO;
import com.houjiang.commons.dto.goods.StorageInventoryFlowGoodsDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.vo.goods.PurchaseOrderVO;
import com.houjiang.commons.vo.goods.StorageInventoryFlowGoodsVO;
import com.houjiang.goods.entity.StorageInventoryFlowGoodsEntity;
import com.houjiang.goods.vo.InventoryFlowVO;
import com.houjiang.goods.vo.StorageGoodsStockVO;
import com.houjiang.goods.vo.StorageInventoryFlowVO;

import java.util.List;

/**
 * 仓库库存流水商品表(StorageInventoryFlowGoods)表服务接口
 *
 * @author Amos
 * @since 2024-09-20 16:51:54
 */
public interface StorageInventoryFlowGoodsService extends IService<StorageInventoryFlowGoodsEntity> {

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
    ResponseResult<PageUtils<StorageInventoryFlowGoodsVO>> queryStorageInventoryFlowGoods(StorageInventoryFlowGoodsDTO param);

    /**
     * 查询采购订单商品信息
     *
     * @param param 请求体，包含查询所需的参数
     * @return 返回采购订单的VO对象封装的响应结果
     */
    ResponseResult<StorageInventoryFlowVO> queryTransferOrderGoods(StorageInventoryFlowGoodsDTO param);

    /**
     * 查询采购订单商品信息
     *
     * @param param 请求体，包含查询所需的参数
     * @return 返回采购订单的VO对象封装的响应结果
     */
    ResponseResult<PurchaseOrderVO> queryPurchaseOrderGoods(StorageInventoryFlowGoodsDTO param);

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
    ResponseResult<List<StorageGoodsStockVO>> queryStorageGoodsStock(StorageInventoryFlowGoodsDTO param);

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
    ResponseResult<PageUtils<InventoryFlowVO>> queryInventoryFlowList(StorageInventoryFlowDTO param);

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
    ResponseResult<String> exportInventoryFlowList(StorageInventoryFlowDTO param);

}
