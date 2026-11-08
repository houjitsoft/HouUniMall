package com.houjiang.mall.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 调用 goods 服务 Mall 专用内部接口（/mall/goods/*），由 mall 网关层鉴权，不传递前台 JWT。
 * @author Amos
 */
@FeignClient(name = "goods", url = "${feign.goods.url:}", fallback = GoodsPaasFeignFallback.class)
public interface GoodsPaasFeign {

    /**
     * TODO 补充方法说明
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/mall/goods/lists")
    ResponseResult goodsLists(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @GetMapping("/mall/goods/detail")
    ResponseResult goodsDetail(@RequestParam("id") Integer id);

    /**
     * TODO 补充方法说明
     * @param "name" TODO
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/mall/goods/category/lists")
    ResponseResult categoryLists(@RequestParam(value = "name", required = false) String name);
}
