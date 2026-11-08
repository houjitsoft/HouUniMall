package com.houjiang.goods.controller;


import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName GoodsBrandCatController
 * @Description  商品品牌分类表
 * @Author Amos
 * @Date 2023-06-26 13:50:10
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("goods/BrandCat")
public class GoodsBrandCatController extends BaseController{


    /**
     * 查询品牌分类关联信息
     */
    @PostMapping("/query/list")
    public ResponseResult queryBrandCatList(@RequestBody Map<String,Object> map){
        ResponseResult result = goodsBrandCatService.selectByBrandCat(map);
        return result;
    }


}
