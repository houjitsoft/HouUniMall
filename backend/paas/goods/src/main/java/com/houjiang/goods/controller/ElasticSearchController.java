package com.houjiang.goods.controller;

import com.houjiang.commons.dto.goods.ListPageQueryParamDTO;
import com.houjiang.commons.dto.goods.ShopListQueryDTO;
import com.houjiang.commons.dto.goods.WebGoodsListQueryDTO;
import com.houjiang.commons.dto.sensitive.BaseSensitiveDTO;
import com.houjiang.commons.dto.shop.WebShopListQueryDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.sensitive.BaseSensitiveVO;
import com.houjiang.commons.vo.goods.SearchVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/es")
public class ElasticSearchController extends BaseController {


    /**
     * 创建商品索引库
     */
    @RequestMapping("/createGoodsIndex")
    public ResponseResult createGoodsIndex() {
        ResponseResult goodsIndex = elasticSearchService.createGoodsIndex();
        return goodsIndex;
    }

    /**
     * 创建店铺索引库
     */
    @RequestMapping("/createShopIndex")
    public ResponseResult createShopIndex() {
        ResponseResult shopIndex = elasticSearchService.createShopIndex();
        return shopIndex;
    }
    /**
     * 创建敏感词索引库
     */
    @RequestMapping("/createSensitiveIndex")
    public ResponseResult createSensitiveIndex() {
        ResponseResult result = elasticSearchService.createSensitiveIndex();
        return result;
    }
    /**
     * 删除商品索引库
     */
    @RequestMapping("/deleteGoodsIndex")
    public ResponseResult deleteGoodsIndex() {
        ResponseResult result = elasticSearchService.deleteGoodsIndex();
        return result;
    }

    /**
     * 删除店铺索引库
     */
    @RequestMapping("/deleteShopIndex")
    public ResponseResult deleteShopIndex() {
        ResponseResult result = elasticSearchService.deleteShopIndex();
        return result;
    }
    /**
     * 删除敏感词索引库
     */
    @RequestMapping("/deleteSensitiveIndex")
    public ResponseResult deleteSensitiveIndex() {
        ResponseResult result = elasticSearchService.deleteSensitiveIndex();
        return result;
    }
    /**
     * 导入商品数据
     */
    @RequestMapping("/importGoodsData")
    public ResponseResult importData() {
        ResponseResult result = elasticSearchService.importData();
        return result;
    }

    /**
     * 导入店铺信息
     */
    @RequestMapping("/importShopData")
    public ResponseResult importShopData() {
        ResponseResult result = elasticSearchService.importShopData();
        return result;
    }
    /**
     * 导入敏感词信息
     */
    @RequestMapping("/importSensitiveData")
    public ResponseResult importSensitiveData() {
        ResponseResult result = elasticSearchService.importSensitiveData();
        return result;
    }

    /**
     * 删除所有商品数据
     */
    @RequestMapping("/deleteGoodsData")
    public ResponseResult deleteGoodsData() {
        ResponseResult result = elasticSearchService.deleteGoodsData();
        return result;
    }

    /**
     * 删除所有店铺数据
     */
    @RequestMapping("/deleteShopData")
    public ResponseResult deleteShopData() {
        ResponseResult result = elasticSearchService.deleteShopData();
        return result;
    }
    /**
     * 删除所有敏感词数据
     */
    @RequestMapping("/deleteSensitiveData")
    public ResponseResult deleteSensitiveData() {
        ResponseResult result = elasticSearchService.deleteSensitiveData();
        return result;
    }
    /**
     * 批量  添加 / 修改 敏感词 feign
     */
    @RequestMapping("/addSensitiveList")
    public ResponseResult addSensitiveList(@RequestBody List<BaseSensitiveVO> list) {
        ResponseResult result = elasticSearchService.addSensitiveList(list);
        return result;
    }
    /**
     * 批量  删除 敏感词 feign
     */
    @RequestMapping("/deletSensitiveList")
    public ResponseResult deletSensitiveList(@RequestBody List<Integer> list) {
        ResponseResult result = elasticSearchService.deletSensitiveList(list);
        return result;
    }
    /**
     * pc的商品搜索 查询品牌 商品属性等
     * @param webGoodsListQueryParam
     * @return
     * @throws Exception
     */
    @RequestMapping("/querySeachBrandAttributeData")
    public ResponseResult querySeachBrandAttributeData(@RequestBody WebGoodsListQueryDTO webGoodsListQueryParam) {
        ResponseResult result = elasticSearchService.querySeachBrandAttributeData(webGoodsListQueryParam);
        return result;
    }

    /**
     * pc的商品搜索
     *
     * @param webGoodsListQueryParam
     * @return
     * @throws Exception
     */
    @RequestMapping("/pcSeachGoodsData")
    public ResponseResult pcSeachGoodsData(@RequestBody WebGoodsListQueryDTO webGoodsListQueryParam) {
        ResponseResult result = elasticSearchService.pcSeachGoodsData(webGoodsListQueryParam);
        return result;
    }

    /**
     * 商品搜索
     *
     * @return 商品列表
     * @throws IOException 异常
     */
    @PostMapping("/searchGoodsData")
    public ResponseResult<SearchVO> searchGoodsData(@RequestBody ListPageQueryParamDTO webGoodsListQueryParam) throws IOException {
        return elasticSearchService.searchGoodsData(webGoodsListQueryParam);
    }

    /**
     * pc的店铺搜索
     *
     * @param webGoodsListQueryParam
     * @return
     * @throws Exception
     */
    @RequestMapping("/pcSeachShopData")
    public ResponseResult pcSeachShopData(@RequestBody WebShopListQueryDTO webGoodsListQueryParam) {
        ResponseResult result = elasticSearchService.pcSeachShopData(webGoodsListQueryParam);
        return result;
    }
    /**
     * h5的商品搜索
     *
     * @param webGoodsListQueryParam
     * @return
     * @throws Exception
     */
    @RequestMapping("/h5SeachGoodsData")
    public ResponseResult h5SeachGoodsData(@RequestBody WebGoodsListQueryDTO webGoodsListQueryParam) {
        ResponseResult result = elasticSearchService.h5SeachGoodsData(webGoodsListQueryParam);
        return result;
    }

    /**
     * h5的店铺搜索
     *
     * @param shopListQueryParam
     * @return
     * @throws Exception
     */
    @RequestMapping("/h5SeachShopData")
    public ResponseResult h5SeachShopData(@RequestBody ShopListQueryDTO shopListQueryParam) {
        ResponseResult result = elasticSearchService.h5SeachShopData(shopListQueryParam);
        return result;
    }
    /**
     * 敏感词分词搜索
     *
     * @param baseSensitiveDTO
     * @return
     * @throws Exception
     */
    @RequestMapping("/esSeachSensitive")
    public ResponseResult esSeachSensitive(@RequestBody BaseSensitiveDTO baseSensitiveDTO) throws Exception {
        ResponseResult result = elasticSearchService.esSeachSensitive(baseSensitiveDTO);
        return result;
    }
}
