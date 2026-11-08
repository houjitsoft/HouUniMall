package com.houjiang.merchant.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.service.IIndexService;
import com.houjiang.merchant.service.mall.IMallShopAdminMenuService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@Slf4j
@RestController
@Tag(name = "主页数据管理")
@RequestMapping("/merchant/index")
public class IndexController {
    @Resource
    IIndexService iIndexService;
    @Resource
    IMallShopAdminMenuService iMallShopAdminMenuService;

/**
 * @GetMapping("/index")
 * @Operation(summary="控制台")
 * public AjaxResult<Map<String, Object>> index() {
 * Map<String, Object> map = iIndexService.index();
 * return AjaxResult.success(map);
 * }
 */


    @GetMapping("/mySelf")
    @Operation(summary="获取当前管理员信息")
    public AjaxResult<JSONObject> mySelf() {
        JSONObject mySelf =  iIndexService.mySelf();
        return AjaxResult.success(mySelf);
    }

    @GetMapping("/menu_list")
    @Operation(summary = "当前管理员菜单列表")
    public AjaxResult<JSONArray> menuList() {
        return AjaxResult.success(
                iMallShopAdminMenuService.getShopAdminMenuList(
                        MerchantThreadLocal.getAdminId()));
    }
}
