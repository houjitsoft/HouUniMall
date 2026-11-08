package com.houjiang.goods.service;


import com.houjiang.commons.dto.goods.GoodsListQueryEsDTO;
import com.houjiang.commons.dto.goods.ListPageQueryParamDTO;
import com.houjiang.commons.dto.goods.ShopListQueryDTO;
import com.houjiang.commons.dto.goods.WebGoodsListQueryDTO;
import com.houjiang.commons.dto.sensitive.BaseSensitiveDTO;
import com.houjiang.commons.dto.shop.ArticleShopDTO;
import com.houjiang.commons.dto.shop.WebShopListQueryDTO;
import com.houjiang.commons.dto.shop.WebShopListQueryEsDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.sensitive.BaseSensitiveVO;
import com.houjiang.commons.vo.goods.SearchVO;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;

/**
 * @author Amos
 */
public interface ElasticSearchService {
    /**
     * commonId
     * @param commonId
     * @return
     */
    GoodsListQueryEsDTO selectByCommonId(Integer commonId);
    /**
     * 根据goodsId查询商品信息
     * @param goodsId
     * @return
     */
    GoodsListQueryEsDTO selectByGoodsId(Integer goodsId);

    /**
     * 查询店铺id
     * @param commonId
     * @return
     */
    GoodsListQueryEsDTO selectShopIdByCommonId(Integer commonId);

    /**
     * 查询所有店铺
     * @return
     */
    List<ArticleShopDTO> selectAllShop();

    /**
     * 根据shopId查询店铺信息
     * @param shopId
     * @return
     */
    List<WebShopListQueryEsDTO> selectByShopId(Integer shopId);

    /**
     * 查询店铺配置信息
     * @param shopId
     * @return
     */
    ShopListQueryDTO selectByH5ShopId(Integer shopId);

    /**
     * 商品搜索
     */
    ResponseResult<SearchVO> searchGoodsData(ListPageQueryParamDTO webGoodsListQueryParam) throws IOException;

    /**
     * pc的商品搜索
     *
     * @param webGoodsListQueryParam
     * @return
     * @throws Exception
     */
    ResponseResult pcSeachGoodsData(WebGoodsListQueryDTO webGoodsListQueryParam);

    /**
     * pc的店铺搜索
     *
     * @param webGoodsListQueryParam
     * @return
     * @throws Exception
     */
    ResponseResult pcSeachShopData(@RequestBody WebShopListQueryDTO webGoodsListQueryParam);

    /**
     * 创建商品索引库
     */
    ResponseResult createGoodsIndex();

    /**
     * 创建店铺索引库
     */
    ResponseResult createShopIndex();

    /**
     * 创建敏感词索引库
     * @return
     */
    ResponseResult createSensitiveIndex();

    /**
     * 删除商品索引库
     */
    ResponseResult deleteGoodsIndex();

    /**
     * 删除敏感词索引库
     * @return
     */
    ResponseResult deleteSensitiveIndex();

    /**
     * 删除店铺索引库
     */
    ResponseResult deleteShopIndex();

    /**
     * 导入商品数据
     */
    ResponseResult importData();

    /**
     * 导入店铺信息
     */
    ResponseResult importShopData();

    /**
     * 导入敏感词信息
     * @return
     */
    ResponseResult importSensitiveData();

    /**
     * 删除所有商品数据
     */
    ResponseResult deleteGoodsData();

    /**
     * 删除所有店铺数据
     */
    ResponseResult deleteShopData();

    /**
     * 删除敏感词数据
     * @return
     */
    ResponseResult deleteSensitiveData();

    /**
     * 批量  添加 / 修改 敏感词 feign
     * @param list
     * @return
     */
    ResponseResult addSensitiveList(List<BaseSensitiveVO> list);

    /**
     * 批量  删除 敏感词 feign
     * @param list
     * @return
     */
    ResponseResult deletSensitiveList(List<Integer> list);

    /**
     * h5的商品搜索
     *
     * @param webGoodsListQueryParam
     * @return
     * @throws Exception
     */
    ResponseResult h5SeachGoodsData(WebGoodsListQueryDTO webGoodsListQueryParam);

    /**
     * h5的店铺搜索
     *
     * @param shopListQueryParam
     * @return
     * @throws Exception
     */
    ResponseResult h5SeachShopData(ShopListQueryDTO shopListQueryParam);

    /**
     * pc的商品搜索 查询品牌 商品属性等
     * @param webGoodsListQueryParam
     * @return
     * @throws Exception
     */
    ResponseResult querySeachBrandAttributeData(WebGoodsListQueryDTO webGoodsListQueryParam);

    /**
     * 敏感词分词搜索
     * @param baseSensitiveDTO
     * @return
     */
    ResponseResult esSeachSensitive(BaseSensitiveDTO baseSensitiveDTO);

}
