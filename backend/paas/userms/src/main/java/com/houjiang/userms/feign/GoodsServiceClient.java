package com.houjiang.userms.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author Amos
 * @date 2023/3/21 11:17
 */
@FeignClient("goods")
public interface GoodsServiceClient {

    //商品收藏量更新
    /**
     * TODO 补充方法说明
     * @param paraMap TODO
     * @return TODO
     */
    @PostMapping(value = "/goods/common/update/goods/collect")
    ResponseResult updateGoodsCollect(@RequestBody Map<String, String> paraMap);

    /**
     * 查询店铺首页
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/goods/common/shopIndex/query")
    ResponseResult getDetailByShopId(@RequestBody Map<String, String> paraMap);


    /**
     * 根据commonid获取商品详情
     *
     * @param commonId
     * @return
     */
    @GetMapping("/goods/common/getDetailByCommonId")
    ResponseResult getDetailByCommonId(@RequestParam("commonId") Integer commonId);


    /**
     * 根据商品id修改收藏数
     *
     * @param goodId
     * @return
     */
    @GetMapping("/goods/base/updateGoodsCollect")
    ResponseResult updateGoodsCollect(@RequestParam("goodId") Integer goodId);


    /**
     * 根据商品id修改收藏数
     *
     * @param commonId
     * @return
     */
    @GetMapping("/goods/common/updateCommonCollect")
    ResponseResult updateCommonCollect(@RequestParam("goodId") String commonId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @GetMapping("/goods/cat/fx/queryCatList")
    ResponseResult queryFxCatList(@RequestParam("catId") Integer catId);
}
