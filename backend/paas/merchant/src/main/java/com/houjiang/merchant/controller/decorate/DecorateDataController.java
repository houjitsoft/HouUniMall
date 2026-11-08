package com.houjiang.merchant.controller.decorate;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.service.IDecorateDataService;
import com.houjiang.merchant.vo.decorate.DecorateDataArticleVo;
import com.houjiang.merchant.common.core.AjaxResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @author Amos
 */
@RestController
@Tag(name = "装修数据管理")
@RequestMapping("/adminapi/decorate.data")
public class DecorateDataController {

    @Resource
    IDecorateDataService iDecorateDataService;

    @GetMapping("/article")
    @Operation(summary="获取文章数据")
    public AjaxResult<List<DecorateDataArticleVo>> article(@RequestParam(defaultValue = "10") Integer limit) {
        List<DecorateDataArticleVo> list = iDecorateDataService.article(limit);
        return AjaxResult.success(list);
    }


    @GetMapping("/pc")
    @Operation(summary="获取pc装修数据")
    public AjaxResult<JSONObject> pc() {
        JSONObject pc = iDecorateDataService.pc();
        return AjaxResult.success(pc);
    }

}
