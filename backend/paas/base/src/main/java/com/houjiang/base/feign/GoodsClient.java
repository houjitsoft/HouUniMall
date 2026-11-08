package com.houjiang.base.feign;


import com.houjiang.base.vo.GoodsIdsQueryVO;
import com.houjiang.commons.dto.goods.CatIdsDTO;
import com.houjiang.commons.dto.goods.ModularizationdGoodsDataDTO;
import com.houjiang.commons.dto.goods.WebGoodsCatQueryDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.base.BaseWxLiveGoodsVO;
import com.houjiang.commons.vo.goods.GoodsIdsVO;
import com.houjiang.commons.vo.goods.ModularizationdGoodsVO;
import com.houjiang.commons.vo.goods.SecordNameListVO;
import com.houjiang.commons.vo.goodscat.WebCatMenuVO;
import com.houjiang.commons.vo.sensitive.BaseSensitiveVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Amos
 */
@FeignClient("goods")
public interface GoodsClient {


    /**
     * 批量  添加 / 修改 敏感词 feign
     */
    @RequestMapping("/es/addSensitiveList")
    ResponseResult addSensitiveList(@RequestBody List<BaseSensitiveVO> list);
    /**
     * 批量  删除 敏感词 feign
     */
    @RequestMapping("/es/deletSensitiveList")
    ResponseResult deletSensitiveList(@RequestBody List<Integer> list);


    /**
     * 查询商品信息
     * @param modularizationdGoodsData
     * @return
     */
    @PostMapping("/goods/base/feign/queryGoodsInfo")
    ModularizationdGoodsDataDTO queryGoodsInfo(@RequestBody ModularizationdGoodsDataDTO modularizationdGoodsData);

    /**
     * 查询商品信息
     * @param goodsId
     * @return
     */
    @GetMapping("/goods/base/feign/queryModularizationdGoodsInfo")
    ModularizationdGoodsVO queryModularizationdGoodsInfo(@RequestParam("goodsId") String goodsId);


    /**
     * 查询商品信息
     * @param goodsId
     * @return
     */
    @GetMapping("/goods/base/feign/queryModularizationdGoodsInfo2")
    ModularizationdGoodsVO queryModularizationdGoodsInfo2(@RequestParam("goodsId") String goodsId);


    /**
     * 根据通用ID查询通用名称
     *
     * @param commonId 通用ID
     * @return 通用名称字符串
     */
    @GetMapping("/goods/common/feign/selectCommonName")
    String selectCommonName(@RequestParam("commonId") Integer commonId);

    /**
     * 根据类别ID查询类别名称
     *
     * @param catId 类别ID
     * @return 类别名称字符串
     */
    @GetMapping("/goods/cat/feign/selectByCatNameString")
    String selectByCatNameString(@RequestParam("catId") Integer catId);


    /**
     * 查询商品详情
     * @param commonId
     * @return
     */
    @GetMapping("/goods/common/feign/queryCommonDetail")
    BaseWxLiveGoodsVO queryCommonDetail(@RequestParam("commonId") Integer commonId);


    /**
     * 查询商品信息
     * @param goodsId
     * @return
     */
    @GetMapping("/goods/base/feign/newSelectWebFloorMobConfig")
    GoodsIdsVO newSelectWebFloorMobConfig(@RequestParam("goodsId")Integer goodsId);

    /**
     * 查询所有子分类
     * @return
     */
    @PostMapping("/goods/cat/feign/selectAllCat2")
    List<WebCatMenuVO> selectAllCat2();

    /**
     * 通过参数查询
     *
     * @param param
     * @return
     */
    @PostMapping("/goods/cat/selectByParam")
    ResponseResult selectByParam(@RequestBody WebGoodsCatQueryDTO param);

    /**
     * 查询分类
     * @param catId
     * @return
     */
    @GetMapping("/goods/cat/feign/selectByCatParentId")
    List<SecordNameListVO> selectByCatParentId(@RequestParam("catParentId")Integer catId);

    /**
     * 查询分类信息
     * @param
     * @param catIdsDTO
     * @return
     */
    @PostMapping("/goods/base/feign/selectByCatIds")
    List<GoodsIdsQueryVO> selectByCatIds(@RequestBody CatIdsDTO catIdsDTO);

    /**
     * 根据商品名称查询分类
     * @param commonName
     * @return
     */
    @GetMapping("/goods/common/feign/selectCatIdByCommonName")
    Integer selectCatIdByCommonName(@RequestParam("commonName")String commonName);
}
