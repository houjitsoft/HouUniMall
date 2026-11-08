package com.houjiang.goods.controller;


import com.houjiang.commons.group.Select;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.goods.StorageVO;
import com.houjiang.goods.dto.StorageGoodsBaseDTO;
import com.houjiang.goods.service.StorageGoodsBaseService;
import com.houjiang.goods.vo.StorageGoodsBaseVO;
import com.houjiang.goods.vo.StorageGoodsVO;
import com.houjiang.goods.vo.StorageInfoVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 仓库中商品信息表(StorageGoodsBase)表控制层
 *
 * @author Amos
 * @since 2024-09-19 14:06:57
 */
@RestController
@RequestMapping("storageGoodsBase")
public class StorageGoodsBaseController {

    /**
     * 服务对象
     */
    @Resource
    private StorageGoodsBaseService storageGoodsBaseService;

    /**
     * 查询库存列表
     *
     * @param param 查询参数，包含与查询相关的必要条件 {@link StorageGoodsBaseDTO}
     * @return 返回库存商品的基础信息列表 {@link List<StorageGoodsBaseVO>}
     *
     * 该方法通过接收查询参数，调用服务层的方法来获取库存列表
     * 使用PostMapping注解指定请求的URL和HTTP方法，确保客户端可以通过POST请求访问这个端点
     * 通过RequestBody注解，表示该方法期望从请求体中获取参数
     * 使用Validated注解，并指定校验组为Select，确保传入的参数符合预期的校验规则
     * 实际的业务逻辑被委托给了storageGoodsBaseService中的queryStorageList方法
     */
    @PostMapping("/queryStorageList")
    public ResponseResult<List<StorageGoodsBaseVO>> queryStorageList(@Validated({Select.class}) @RequestBody StorageGoodsBaseDTO param) {
        return storageGoodsBaseService.queryStorageList(param);
    }

    /**
     * 查询库存列表
     *
     * @param param 查询参数，包含与查询相关的必要条件 {@link StorageGoodsBaseDTO}
     * @return 返回库存商品的基础信息列表 {@link List<StorageGoodsBaseVO>}
     *
     * 该方法通过接收查询参数，调用服务层的方法来获取库存列表
     * 使用PostMapping注解指定请求的URL和HTTP方法，确保客户端可以通过POST请求访问这个端点
     * 通过RequestBody注解，表示该方法期望从请求体中获取参数
     * 使用Validated注解，并指定校验组为Select，确保传入的参数符合预期的校验规则
     * 实际的业务逻辑被委托给了storageGoodsBaseService中的queryStorageList方法
     */
    @PostMapping("/queryGoodsStorageList")
    public ResponseResult<List<StorageGoodsVO>> queryGoodsStorageList(@Validated({Select.selectGoods.class}) @RequestBody StorageGoodsBaseDTO param) {
        return storageGoodsBaseService.queryGoodsStorageList(param);
    }


    /**
     * 根据商品id 查询库存信息 feign
     *
     * 该方法用于根据商品的唯一标识符（goodsId）来查询该商品的库存信息
     * 主要用于需要快速获取某个特定商品库存信息的场景，例如商品详情页、库存管理系统等
     *
     * @param goodsId 商品的唯一标识符，用于定位到具体的商品
     * @return 返回包含商品库存信息的列表，列表中的每个元素对应一个库存地点的库存信息
     */
    @PostMapping("/queryGoodsStorageByGoodsId")
    public ResponseResult<List<StorageVO>> queryGoodsStorageByGoodsId(@RequestParam("goodsId") Integer goodsId) {
        return storageGoodsBaseService.queryGoodsStorageByGoodsId(goodsId);
    }
}

