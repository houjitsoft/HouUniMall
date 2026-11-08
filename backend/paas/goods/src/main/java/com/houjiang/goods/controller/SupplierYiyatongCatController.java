package com.houjiang.goods.controller;

import com.houjiang.commons.dto.yiyatong.CategoryDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.service.SupplierYiyatongCatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName SupplierYiyatongCatController
 * @Description
 * @Author Amos
 * @Date 2024/11/20 17:31
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/supplier")
public class SupplierYiyatongCatController {

    @Autowired
    private SupplierYiyatongCatService supplierYiyatongCatService;

    /**
     * 同步分类信息
     * @param categoryDTO
     */
    @PostMapping("/synchronousCategorys")
    public ResponseResult synchronousCategorys(@RequestBody CategoryDTO categoryDTO){
        ResponseResult result=supplierYiyatongCatService.synchronousCategorys(categoryDTO);
        return result;


    }
}
