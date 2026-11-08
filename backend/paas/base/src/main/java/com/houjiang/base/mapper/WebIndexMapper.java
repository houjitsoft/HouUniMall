package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.base.vo.GoodsIdsQueryListVO;
import com.houjiang.base.vo.GoodsIdsQueryVO;
import com.houjiang.base.vo.WebShopListQueryResultVO;
import com.houjiang.base.vo.index.PcConfigGoodsVO;
import com.houjiang.commons.dto.goods.WebGoodsListQueryDTO;
import com.houjiang.commons.dto.shop.WebShopListQueryDTO;
import com.houjiang.commons.vo.goods.GdsListQueryResultVO;
import com.houjiang.commons.vo.goods.GoodsCatVO;
import com.houjiang.commons.vo.goods.GoodsIdsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName WebIndexMapper
 * @Description pc首页装修
 * @Author Amos
 * @Date 2023/5/24 9:19
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface WebIndexMapper {

    /**
     * 查询商品
     *
     * @param goodIds
     * @return
     */
    List<GoodsIdsQueryListVO> selectByGoodIds(@Param("goodIds") List<Integer> goodIds);


    /**
     * 查询商品 店铺信息
     * @param commonId
     * @return
     */
    PcConfigGoodsVO pcConfigGoods(@Param("id")Integer commonId);


    /**
     * 查询商品信息
     * @param goodsIds
     * @return
     */
    List<GoodsIdsQueryVO> selectGoodsByGoodIds(@Param("goodIds")List<Integer> goodsIds);


    /**
     * 查询商品信息
     * @param param
     * @return
     */
    List<GoodsIdsQueryVO> selectRecGoods(@Param("param")WebGoodsListQueryDTO param);


    /**
     * 供应商商品查询
     * @param goodsIdList2
     * @return
     */
    List<GoodsIdsQueryListVO> selectByGoodIds2(@Param("goodIds")List<Integer> goodsIdList2);

    /**
     * 供应商商品经销查询
     * @param goodIds
     * @return
     */
    List<GoodsIdsVO> selectDelayByGoodIds(@Param("goodIds") List<Integer> goodIds);

    /**
     * 查询供应商商品列表
     * @param page
     * @param param
     * @return
     */
    IPage<GdsListQueryResultVO> queryBrandList(IPage<GdsListQueryResultVO> page, @Param("param")WebGoodsListQueryDTO param);

    /**
     * 供应商店铺列表
     * @param page
     * @param param
     * @return
     */
    IPage<WebShopListQueryResultVO> selectShopListFilter(IPage<WebShopListQueryResultVO> page, @Param("param")WebShopListQueryDTO param);




}
