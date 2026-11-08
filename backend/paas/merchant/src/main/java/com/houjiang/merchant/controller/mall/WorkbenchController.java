package com.houjiang.merchant.controller.mall;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.service.IIndexService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * 店铺控制器
 * Class MallShopController
 * @author Amos
 */
@RestController
@RequestMapping({"merchant/mall.workbench"})
@Tag(name = "工作台")
public class WorkbenchController {
    @Resource
    IIndexService iIndexService;

    @GetMapping("/index")
    @Operation(summary="控制台")
    public AjaxResult<Map<String, Object>> index() {
        Map<String, Object> map = iIndexService.index();
        return AjaxResult.success(map);
    }
}
