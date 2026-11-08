package com.houjiang.mall.controller.distribution;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.distribution.DistributionWithdraw;
import com.houjiang.mall.service.distribution.DistributionWithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 分销提现控制器
 *
 * 挂在 /mall/distribution/withdraw 下，经 BFF 商城模块直通通道访问，白名单已覆盖。
 * C 端分销中心提交提现申请，后台审核（通过→待打款 / 驳回→余额退回 / 已打款→归档）。
 *
 * @author Amos
 */
@RestController
@RequestMapping("/mall/distribution/withdraw")
@RequiredArgsConstructor
public class DistributionWithdrawController {

    private final DistributionWithdrawService distributionWithdrawService;

    /**
     * 分页查询提现单
     */
    @PostMapping("/page")
    public ResponseResult<?> page(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(distributionWithdrawService.pageList(params == null ? Map.of() : params));
    }

    /**
     * 提交提现申请（校验分销开关、提现门槛、可提现余额，通过后冻结余额）
     */
    @PostMapping("/apply")
    public ResponseResult<DistributionWithdraw> apply(@RequestBody DistributionWithdraw withdraw) {
        return ResponseResult.success(distributionWithdrawService.apply(withdraw));
    }

    /**
     * 审核提现单：status 1=通过待打款 2=驳回（退回余额） 3=已打款（归档）
     */
    @PostMapping("/audit")
    public ResponseResult<Boolean> audit(@RequestBody Map<String, Object> params) {
        Long id = params.get("id") != null ? Long.parseLong(params.get("id").toString()) : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        String auditor = params.get("auditor") != null ? params.get("auditor").toString() : null;
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;
        if (id == null || status == null) {
            return ResponseResult.success(false);
        }
        return ResponseResult.success(distributionWithdrawService.audit(id, status, auditor, remark));
    }
}
