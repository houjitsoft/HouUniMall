package com.houjiang.goods.controller;


import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.dto.CartGuessLikeGoodsQueryDTO;
import com.houjiang.goods.dto.WapUserFootPrintDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName GoodsUserFootprintController
 * @Description  商品访问足迹表
 * @Author Amos
 * @Date 2023-06-08 10:01:59
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("goods/userFootprint")
public class GoodsUserFootprintController extends BaseController {

    /**
     * 查询购物车底部猜你喜欢
     * @return
     */
    @PostMapping("/cart/likeGoods/query")
    public ResponseResult queryCartGuessLikeGoods(@RequestBody CartGuessLikeGoodsQueryDTO param){
        ResponseResult result = goodsUserFootprintService.queryCartGuessLikeGoods(param);
        return result;
    }

    /**
     * 查询足迹
     *
     * @return
     */
    @PostMapping("/query")
    public ResponseResult queryFootPrint2(@RequestBody WapUserFootPrintDTO param) {
        ResponseResult result = goodsUserFootprintService.queryUserFoot(param);
        return result;
    }

    /**
     * im 查询用户足迹
     * 该接口用于查询用户的足迹信息，以便在用户界面中展示用户曾经浏览过的商品或页面
     * 使用POST请求方式，并以JSON形式接收参数
     *
     * @param param 包含查询用户足迹所需信息的WapUserFootPrintDTO对象，包括用户ID等信息
     * @return 返回查询结果，包括用户足迹列表和响应状态
     */
    @PostMapping("im/queryFootPrint")
    public ResponseResult imQueryFootPrint(@RequestBody WapUserFootPrintDTO param) {
        ResponseResult result = goodsUserFootprintService.imQueryFootPrint(param);
        return result;
    }


    /**
     * 删除所有足迹
     *
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult deleteFootPrint(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsUserFootprintService.deleteFootPrint(map);
        return result;
    }


    /**
     * 根据用户商品足迹 查询对应分类下的商品
     *
     * @return
     */
    @PostMapping("/queryUserRandomGoods")
    public ResponseResult queryUserRandomGoods(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsUserFootprintService.queryUserRandomGoods(map);
        return result;
    }
}
