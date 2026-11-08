package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.goods.StorageVO;
import com.houjiang.goods.dto.StorageGoodsBaseDTO;
import com.houjiang.goods.entity.StorageGoodsBaseEntity;
import com.houjiang.goods.vo.StorageGoodsBaseVO;
import com.houjiang.goods.vo.StorageGoodsVO;

import java.util.List;

/**
 * 仓库中商品信息表(StorageGoodsBase)表服务接口
 *
 * @author Amos
 * @since 2024-09-19 14:06:57
 */
public interface StorageGoodsBaseService extends IService<StorageGoodsBaseEntity> {

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
    ResponseResult<List<StorageGoodsBaseVO>> queryStorageList(StorageGoodsBaseDTO param);

    /**
     * 查询库存商品列表
     *
     * @param param 查询参数，包含商户ID等信息
     * @return 返回库存商品列表的响应结果
     */
    ResponseResult<List<StorageGoodsVO>> queryGoodsStorageList(StorageGoodsBaseDTO param);

    /**
     * 根据商品ID查询商品库存
     *
     * 本函数旨在通过商品ID获取商品的详细库存信息它返回指定商品的所有库存项目，
     * 包括但不限于库存数量、位置等信息这对于库存管理、订单处理等业务场景至关重要
     *
     * @param goodsId 商品ID，用于唯一标识一个商品
     * @return 返回一个ResponseResult对象，其中包含本次查询的结果状态（成功或失败）、
     *         状态码、提示信息以及可能的返回数据（商品库存列表）
     */
    ResponseResult<List<StorageVO>> queryGoodsStorageByGoodsId(Integer goodsId);
}
