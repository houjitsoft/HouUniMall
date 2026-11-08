package com.houjiang.activity.controller;


import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivityGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName ActivityGoodsController
 * @Description 限时折扣商品表
 * @Author Amos
 * @Date 2023-12-06 14:13:08
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("goods")
public class ActivityGoodsController {
    @Autowired
    protected ActivityGoodsService activityGoodsService;

    /**
     * 查看 限时折扣、是否到购买上限
     * @param param
     * @return
     */
    @PostMapping("/check/limit")
    public ResponseResult checkDiscountLimit(@RequestBody Map<String,Object> param) {

        return activityGoodsService.checkGoodsLimit(param);
    }

}

