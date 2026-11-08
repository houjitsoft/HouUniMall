package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.commons.vo.goods.StorageVO;
import com.houjiang.goods.dto.MerchantGoodsStockDTO;
import com.houjiang.goods.dto.StorageGoodsBaseDTO;
import com.houjiang.goods.entity.StorageGoodsBaseEntity;
import com.houjiang.goods.vo.StorageGoodsBaseVO;
import com.houjiang.goods.vo.StorageGoodsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 仓库中商品信息表(StorageGoodsBase)表数据库访问层
 *
 * @author Amos
 * @since 2024-09-19 14:06:57
 */
@Mapper
public interface StorageGoodsBaseMapper extends BaseMapper<StorageGoodsBaseEntity> {

    /**
     * 通过商品ID查询总库存
     * @param goodsId 商品ID
     * @return 商品总库存
     */
    Integer selectTotalStorageStock(@Param("goodsId") Integer goodsId);

    /**
     * 查询商品的仓库库存列表
     * @param param 查询参数
     * @return 商品库存列表
     */
    List<StorageGoodsBaseVO> queryStorageGoodsList(@Param("param") MerchantGoodsStockDTO param);

    /**
     * 获取库存商品列表
     * @param param 查询参数
     * @return 库存商品列表
     */
    List<StorageGoodsBaseVO> queryStorageList(@Param("param") StorageGoodsBaseDTO param);

    /**
     * 获取库存商品列表
     * @param param 查询参数
     * @return 库存商品列表
     */
    List<StorageGoodsVO> queryGoodsStorageList(@Param("param") StorageGoodsBaseDTO param);

    /**
     * 获取特定仓库的商品详情列表
     * @param param 查询参数
     * @return 仓库商品详情列表
     */
    List<StorageGoodsBaseVO> queryStorageGoodsBaseList(@Param("param")  StorageGoodsBaseDTO param);

    /**
     * 查询仓库中商品的库存数量
     * @param goodsId 商品ID
     * @param storageId 仓库ID
     * @return 库存数量
     */
    Integer selectStorageGoodsStock(@Param("goodsId") Integer goodsId,@Param("storageId") Integer storageId);

    /**
     * 获取所有规格的商品库存总数
     * @param commonId 商品spuID
     * @return 商品库存总数
     */
    Integer getAllSpecGoodsStorageStock(@Param("commonId") Integer commonId);

    /**
     * 获取商品库存总数
     * @param goodsId 商品skuID
     * @return 商品库存总数
     */
    Integer getGoodsStorageStock(@Param("goodsId") Integer goodsId);
    /**
     * 根据商品ID查询商品库存信息
     * 此方法用于根据特定商品的ID来检索该商品的库存情况
     * 它返回一个StorageGoodsVO列表，包含了所有与指定商品ID相关的库存信息
     *
     * @param goodsId 商品的ID，用于定位特定的商品
     * @return 返回一个列表，包含所有与指定商品ID相关的库存信息如果找不到相关库存，则返回空列表
     */
    List<StorageVO> queryGoodsStorageByGoodsId(@Param("goodsId") Integer goodsId);
}
