package com.houjiang.userms.controller;


import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName UserFavoritesGoodsController
 * @Description 收藏的商品
 * @Author Amos
 * @Date 2023-05-26 16:24:16
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/favoritesGoods")
public class UserFavoritesGoodsController extends BaseController {
    /*
/**
     * 查看商品收藏与否
     */
    @PostMapping("/check")
    public ResponseResult check(@RequestBody Map<String, String> paraMap) {
        ResponseResult result =userFavoritesGoodsService.check(paraMap);
        return result;
    }
    /**
     * 收藏商品
     */
    @PostMapping("/add")
    public ResponseResult add(@RequestBody Map<String, String> paraMap) {
        ResponseResult result =userFavoritesGoodsService.add(paraMap);
        return result;
    }

    /**
     * 我的收藏商品列表
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/get")
    public ResponseResult get(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = userFavoritesGoodsService.get(paraMap);
        return result;
    }

    /**
     * 删除我的收藏商品
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult delete(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = userFavoritesGoodsService.delete(paraMap);
        return result;

    }


    /**
     * 查询收藏商品
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/query")
    public ResponseResult query(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = userFavoritesGoodsService.query(paraMap);
        return result;
    }
}
