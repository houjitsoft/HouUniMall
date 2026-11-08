package com.houjiang.bffweb.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 满减只读查询（order 宿主兼容桥）。
 */
@FeignClient(name = "order", contextId = "bffReductionQueryFeign", url = "${feign.order.url:}")
public interface ReductionQueryFeign {

    @PostMapping("/activity/reductionPolicyBase/selectByGoodsId")
    Map<String, Object> selectByGoodsId(@RequestParam("commonId") Integer commonId);

    @PostMapping("/activity/reductionPolicyBase/selectList")
    List<Map<String, Object>> selectList(@RequestParam("promotionId") Integer promotionId);

    @PostMapping("/activity/reductionPolicyBase/selectContent")
    List<Map<String, Object>> selectContent(@RequestParam("commonId") Integer commonId);
}
