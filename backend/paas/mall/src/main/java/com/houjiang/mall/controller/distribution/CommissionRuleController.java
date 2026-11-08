package com.houjiang.mall.controller.distribution;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.distribution.CommissionRule;
import com.houjiang.mall.service.distribution.CommissionRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 佣金规则控制器
 * 提供佣金规则的列表、详情、创建、更新、删除等功能
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/distribution/rule")
@RequiredArgsConstructor
public class CommissionRuleController {

    private final CommissionRuleService commissionRuleService;

    /**
     * 分页查询佣金规则列表
     *
     * @param params 查询参数（可包含 shopId）
     * @return 规则列表
     */
    @PostMapping("/list")
    public ResponseResult<List<CommissionRule>> list(@RequestBody Map<String, Object> params) {
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        if (shopId != null) {
            return ResponseResult.success(commissionRuleService.listByShop(shopId));
        }
        return ResponseResult.success(commissionRuleService.list());
    }

    /**
     * 获取佣金规则详情
     *
     * @param params 参数（包含 id）
     * @return 规则详情
     */
    @PostMapping("/detail")
    public ResponseResult<CommissionRule> detail(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return ResponseResult.success(commissionRuleService.getById(id));
    }

    /**
     * 创建佣金规则
     *
     * @param rule 规则信息
     * @return 是否成功
     */
    @PostMapping("/create")
    public ResponseResult<Boolean> create(@RequestBody CommissionRule rule) {
        return ResponseResult.success(commissionRuleService.createRule(rule));
    }

    /**
     * 更新佣金规则
     *
     * @param rule 规则信息
     * @return 是否成功
     */
    @PostMapping("/update")
    public ResponseResult<Boolean> update(@RequestBody CommissionRule rule) {
        return ResponseResult.success(commissionRuleService.updateById(rule));
    }

    /**
     * 删除佣金规则
     *
     * @param params 参数（包含 id）
     * @return 是否成功
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return ResponseResult.success(commissionRuleService.removeById(id));
    }

    /**
     * 获取适用的佣金规则
     *
     * @param params 参数（包含 shopId, categoryId, goodsId）
     * @return 最优规则
     */
    @PostMapping("/applicable")
    public ResponseResult<CommissionRule> getApplicable(@RequestBody Map<String, Object> params) {
        Long shopId = Long.parseLong(params.get("shopId").toString());
        Long categoryId = params.get("categoryId") != null ? Long.parseLong(params.get("categoryId").toString()) : null;
        Long goodsId = params.get("goodsId") != null ? Long.parseLong(params.get("goodsId").toString()) : null;
        return ResponseResult.success(commissionRuleService.getApplicableRule(shopId, categoryId, goodsId));
    }
}
