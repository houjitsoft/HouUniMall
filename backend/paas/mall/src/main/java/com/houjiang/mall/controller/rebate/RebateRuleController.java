package com.houjiang.mall.controller.rebate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.rebate.RebateRule;
import com.houjiang.mall.service.rebate.RebateRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 返利规则控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/rebate/rule")
@RequiredArgsConstructor
public class RebateRuleController {

    private final RebateRuleService rebateRuleService;

    /**
     * 分页查询返利规则
     * 
     * @param params 查询参数
     * @return 分页数据
     */
    @PostMapping("/list")
    public ResponseResult<IPage<RebateRule>> list(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(rebateRuleService.pageList(params));
    }

    /**
     * 获取返利规则详情
     * 
     * @param id 规则ID
     * @return 规则详情
     */
    @GetMapping("/detail/{id}")
    public ResponseResult<RebateRule> detail(@PathVariable Long id) {
        return ResponseResult.success(rebateRuleService.getById(id));
    }

    /**
     * 创建返利规则
     * 
     * @param rule 规则信息
     * @return 操作结果
     */
    @PostMapping("/create")
    public ResponseResult<Boolean> create(@RequestBody RebateRule rule) {
        return ResponseResult.success(rebateRuleService.createRule(rule));
    }

    /**
     * 更新返利规则
     * 
     * @param rule 规则信息
     * @return 操作结果
     */
    @PostMapping("/update")
    public ResponseResult<Boolean> update(@RequestBody RebateRule rule) {
        return ResponseResult.success(rebateRuleService.updateRule(rule));
    }

    /**
     * 更新规则状态
     * 
     * @param params 参数（id, status）
     * @return 操作结果
     */
    @PostMapping("/status")
    public ResponseResult<Boolean> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        return ResponseResult.success(rebateRuleService.updateStatus(id, status));
    }

    /**
     * 删除返利规则
     * 
     * @param id 规则ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(rebateRuleService.removeById(id));
    }
}
