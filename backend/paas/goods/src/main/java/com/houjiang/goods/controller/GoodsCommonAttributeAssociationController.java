package com.houjiang.goods.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName GoodsCommonAttributeAssociationController
 * @Description
 * @Author Amos
 * @Date 2023/7/5 14:56
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/goods/attribute/association")
public class GoodsCommonAttributeAssociationController extends BaseController{

    /*
     *
     * 删除时判断商品属性是否已发布
     * */
    @PostMapping("/delete")
    public ResponseResult attribuDelete(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsCommonAttributeAssociationService.selectAttribute(map);
        return result;
    }
}
