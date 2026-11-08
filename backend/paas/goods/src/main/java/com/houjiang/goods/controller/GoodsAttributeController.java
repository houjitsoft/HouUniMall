package com.houjiang.goods.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName GoodsAttributeController
 * @Description
 * @Author Amos
 * @Date 2023/7/5 15:16
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/goods/attribute")
public class GoodsAttributeController extends BaseController{

    /*
/**
     *
     * 查询分类关联下的属性品牌回显
     *
     */
    @PostMapping("/selectCatAttribute")
    public ResponseResult selectCatAttribute(@RequestBody Map<String, Object> map) {
        ResponseResult result=goodsAttributeService.selectCatAttribute(map);
        return result;
    }


    /*
     * 通过catId商品属性表查找属性
     * */
    @PostMapping("/query/byCatId")
    public ResponseResult queryBrandCatAttributeList(@RequestBody Map<String,Object> map){
        ResponseResult result=goodsAttributeService.queryBrandCatAttributeList(map);
        return result;
    }
}
