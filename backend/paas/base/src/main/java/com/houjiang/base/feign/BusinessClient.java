package com.houjiang.base.feign;


import com.houjiang.commons.vo.goods.GoodsCatVO;
import com.houjiang.commons.vo.shop.ExpressQueryByShopDpexpressVO;
import com.houjiang.commons.vo.shop.GdsShopDetailResultVO;
import com.houjiang.commons.vo.shop.PcConfigShopVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/**
 * @author Amos
 * @date 2023/3/27 11:48
 */
@FeignClient("business")
public interface BusinessClient {

    /**
     * 查找店铺信息
     * @param shopId
     * @return
     */
    @PostMapping("shopBase/info/queryShopInfo")
    GdsShopDetailResultVO queryShopinfoByShopId(@RequestParam("shopId") Integer shopId);


    /**
     * 查询店铺默认快递公司
     * @param shopId
     * @return
     */
    @GetMapping("/shop/express/feign/newQueryByShopDpexpress")
    List<ExpressQueryByShopDpexpressVO> newQueryByShopDpexpress(@RequestParam("shopId") Integer shopId);


    /**
     * 根据商店ID查询商店名称
     *
     * @param shopId 商店ID
     * @return 商店名称字符串
     */
    @GetMapping("/shopBase/info/feign/selectByShopName")
    String selectByShopName(@RequestParam("shopId")  Integer shopId);

    /**
     * 查询店铺信息
     * @param id
     * @return
     */
    @GetMapping("/shopBase/info/feign/pcConfigShop")
    PcConfigShopVO pcConfigShop(@RequestParam("id")Integer id);

    /**
     * 查询店铺自定义的分类
     * @param catId
     * @param shopId
     * @return
     */
    @GetMapping("/shop/self/cat/feign/selectByShopId1")
    List<GoodsCatVO> selectByShopId1(@RequestParam("catId")Integer catId, @RequestParam("shopId")Integer shopId);
    /**
     * 查询店铺自定义的分类
     * @param catId
     * @param shopId
     * @return
     */
    @GetMapping("/shop/self/cat/feign/selectByShopId2")
    List<GoodsCatVO> selectByShopId2(@RequestParam("catId")Integer catId, @RequestParam("shopId")Integer shopId);
}
