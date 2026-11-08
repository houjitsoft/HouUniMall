package com.houjiang.merchant.controller.area;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.cache.AreaCache;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.entity.Area;
import com.houjiang.merchant.service.area.IAreaService;
import com.houjiang.merchant.validate.area.RegionSearchValidate;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/area")
public class AreaController {

    @Resource
    IAreaService iAreaService;

    @GetMapping("/lists")
    @Operation(summary="地区列表")
    public AjaxResult<List<Area>> lists(@RequestParam(value = "parentId", required = false) Integer parentId) {
        List<Area> list = AreaCache.getAreaCacheList(parentId);
        return AjaxResult.success(list);
    }

    @GetMapping("/lists_all")
    @Operation(summary="获取所有地区数据")
    public AjaxResult<JSONArray> listAll() {
        JSONArray detail = iAreaService.listAll();
        return AjaxResult.success(detail);
    }


    @PostMapping("/tencent_search")
    @Operation(summary="腾讯地区搜索")
    public AjaxResult<JSONObject> tencentSearch(@Validated @RequestBody RegionSearchValidate regionSearchValidate) {
        JSONObject regionDetail = iAreaService.regionSearch(regionSearchValidate);
        return AjaxResult.success(regionDetail);
    }
}
