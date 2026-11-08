package com.houjiang.userms.controller;


import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.util.ResponseResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName UserFavoritesShopController
 * @Description 收藏的店铺
 * @Author Amos
 * @Date 2023-05-26 16:35:36
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/favoritesShop")
public class UserFavoritesShopController extends BaseController {

    /*
/**
     * 查看店铺收藏与否
     */
    @PostMapping("/check")
    public ResponseResult check(@RequestBody Map<String, String> paraMap) {
        return userFavoritesShopService.check(paraMap);
    }

    /**
     * 收藏店铺
     */
    @PostMapping("/add")
    public ResponseResult add(@RequestBody Map<String, String> paraMap){
        return userFavoritesShopService.add(paraMap);
    }

    /**
     * 我收藏的店铺
     *
     * @param paraMap,token,page,limit
     * @return
     */
    @PostMapping("/get")
    public ResponseResult get(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = userFavoritesShopService.get(paraMap);
        return result;
    }

    /**
     * 删除我收藏的店铺
     *
     * @param paraMap,token,page,limit
     * @return
     */
    @PostMapping("/delete")
    @SysLog("删除我收藏的店铺")
    public ResponseResult delete(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = userFavoritesShopService.delete(paraMap);
        return result;
    }


    /**
     * 删除收藏的店铺
     *
     * @param shopId
     * @return 删除结果
     */
    @PostMapping("/delByShopId")
    @SysLog("删除收藏的店铺")
    public ResponseResult delFavoritesByShopId(@RequestParam("shopId") Integer shopId) {
        ResponseResult result = userFavoritesShopService.delFavoritesByShopId(shopId);
        return result;
    }

    /**
     * 删除收藏的店铺
     * @param shopId
     */
    @GetMapping("/feign/deleteUserFavoritesShop")
    void deleteUserFavoritesShop(@RequestParam("shopId") String shopId){
        userFavoritesShopService.deleteUserFavoritesShop(shopId);
    }
}
