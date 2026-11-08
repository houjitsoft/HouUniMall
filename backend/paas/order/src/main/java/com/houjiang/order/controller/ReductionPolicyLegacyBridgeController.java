package com.houjiang.order.controller;

import com.houjiang.commons.dto.goods.ManjianzengzheGoodsRuleQueryDTO;
import com.houjiang.commons.vo.activity.ManjianzengzheGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.ManjianzengzhePromotionContentQueryResultVO;
import com.houjiang.order.service.ReductionPolicyQueryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 兼容旧 activity Feign 路径的满减只读桥。
 * goods / 遗留调用方仍打 {@code /activity/reductionPolicyBase/*}，
 * 由本控制器落地到 order 宿主查询（activity 服务已拆插件）。
 */
@RestController
@RequestMapping("/activity/reductionPolicyBase")
public class ReductionPolicyLegacyBridgeController {

    private final ReductionPolicyQueryService reductionPolicyQueryService;

    public ReductionPolicyLegacyBridgeController(ReductionPolicyQueryService reductionPolicyQueryService) {
        this.reductionPolicyQueryService = reductionPolicyQueryService;
    }

    @PostMapping("/selectByGoodsId")
    public ManjianzengzheGoodsQueryResultVO selectByGoodsId(@RequestParam("commonId") Integer commonId) {
        return reductionPolicyQueryService.selectByGoodsId(commonId);
    }

    @PostMapping("/selectList")
    public List<ManjianzengzheGoodsRuleQueryDTO> selectList(@RequestParam("promotionId") Integer promotionId) {
        return reductionPolicyQueryService.selectList(promotionId);
    }

    @PostMapping("/selectContent")
    public List<ManjianzengzhePromotionContentQueryResultVO> selectContent(@RequestParam("commonId") Integer commonId) {
        return reductionPolicyQueryService.selectContent(commonId);
    }
}
