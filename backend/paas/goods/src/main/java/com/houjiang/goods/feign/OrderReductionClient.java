package com.houjiang.goods.feign;

import com.houjiang.commons.dto.goods.ManjianzengzheGoodsRuleQueryDTO;
import com.houjiang.commons.vo.activity.ManjianzengzheGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.ManjianzengzhePromotionContentQueryResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 满减只读查询：打到 order 宿主兼容桥（activity 服务已拆为 order-reduction 插件）。
 */
@FeignClient(name = "order", contextId = "orderReductionClient", url = "${feign.order.url:}")
public interface OrderReductionClient {

    @PostMapping("/activity/reductionPolicyBase/selectByGoodsId")
    ManjianzengzheGoodsQueryResultVO selectByGoodsId(@RequestParam("commonId") Integer commonId);

    @PostMapping("/activity/reductionPolicyBase/selectList")
    List<ManjianzengzheGoodsRuleQueryDTO> selectList(@RequestParam("promotionId") Integer promotionId);

    @PostMapping("/activity/reductionPolicyBase/selectContent")
    List<ManjianzengzhePromotionContentQueryResultVO> selectContent(@RequestParam("commonId") Integer commonId);
}
