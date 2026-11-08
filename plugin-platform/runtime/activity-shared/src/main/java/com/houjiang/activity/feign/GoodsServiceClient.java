package com.houjiang.activity.feign;

import com.houjiang.commons.dto.goods.CommonIsXianUpdateDTO;
import com.houjiang.commons.vo.activity.ActivityCombinationGoodsVO;
import com.houjiang.commons.vo.goods.GdsDetailsCommonQueryResultVO;
import com.houjiang.commons.vo.goods.GoodsIdsQueryResultVO;
import com.houjiang.commons.vo.goods.ProductVoucherGoodsVO;
import com.houjiang.commons.vo.goods.SelfSupportIndexGoodsVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @ClassName
 * @Description
 * @Author Amos
 * @Date 2023-06-13 09:05:20
 * @Version 1.0
 * @author Amos
 */
@FeignClient(name = "goods", contextId = "activityGoodsServiceClient", configuration = ActivityFeignConfig.class)
public interface GoodsServiceClient {


    /**
     * 修改商品是否加入促销
     * @param commonIdList
     */
    @PostMapping("/goods/common/update/is/xian")
    void updateCommonIsXian2(@RequestBody List<Integer> commonIdList);


    /**
     * 修改商品是否加入促销 feign
     * @param param
     */
    @PostMapping("/goods/common/update/is/xian/byCommonId")
    void updateCommonIsXian(@RequestBody Map<String,Object> param);

    /**
     * 查询 兑换券商品信息
     *
     * @param productVoucherGoodsVOS
     * @return
     */
    @PostMapping("/goods/common/selecProductVoucherGoods")
    List<ProductVoucherGoodsVO> selecProductVoucherGoods(@RequestBody List<ProductVoucherGoodsVO> productVoucherGoodsVOS );
    /**
     * 根据商品名称模糊查询商品id feign
     * @param commonName
     * @return
     */
    @PostMapping("/goods/common/queryLikeGoodsName")
    List<Integer> queryLikeGoodsName(@RequestParam("commonName") String commonName);


    /**
     * 根据Id获取商品
     * @param goodsId 商品Id
     * @return 商品
     */
    @GetMapping("/goods/base/feign/selectGoodsById")
    ActivityCombinationGoodsVO selectGoodsById(@RequestParam("goodsId") Integer goodsId);


    /**
     * 查询商品信息
     * @param commonId
     * @return
     */
    @GetMapping("/goods/common/feign/SelfSupportIndexGoodsVO")
    SelfSupportIndexGoodsVO selectByCommonId(@RequestParam("commonId") Integer commonId);


    /**
     * 修改商品活动状态
     * @param param
     */
    @PostMapping("/goods/common/feign/updateCommonIsXian3")
    void updateCommonIsXian3(@RequestBody CommonIsXianUpdateDTO param);


    /**
     * 查询商品信息
     * @param commonId
     * @return
     */
    @GetMapping("/goods/common/feign/selectManjianzengzheMessage")
    Map<String, Object> selectManjianzengzheMessage(@RequestParam("commonId") Integer commonId);


    /**
     * 查询商品信息
     * @param commonId
     * @return
     */
    @GetMapping("/goods/common/feign/selectByCommonId")
    GdsDetailsCommonQueryResultVO selectByCommonIdFeign(@RequestParam("commonId") Integer commonId);

    /**
     * 商品是否是经销商品
     * @param goodsId
     * @return
     */
    @GetMapping("/goods/base/feign/selectGoodsIdList")
    GoodsIdsQueryResultVO selectGoodsIdList(@RequestParam("goodsId")Integer goodsId);


}
