package com.houjiang.mall.controller.rebate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.rebate.RebatePayment;
import com.houjiang.mall.service.rebate.RebatePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 返利发放控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/rebate/payment")
@RequiredArgsConstructor
public class RebatePaymentController {

    private final RebatePaymentService rebatePaymentService;

    /**
     * 分页查询返利发放
     * 
     * @param params 查询参数
     * @return 分页数据
     */
    @PostMapping("/list")
    public ResponseResult<IPage<RebatePayment>> list(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(rebatePaymentService.pageList(params));
    }

    /**
     * 获取返利发放详情
     * 
     * @param id 发放ID
     * @return 发放详情
     */
    @GetMapping("/detail/{id}")
    public ResponseResult<RebatePayment> detail(@PathVariable Long id) {
        return ResponseResult.success(rebatePaymentService.getById(id));
    }

    /**
     * 创建返利发放
     * 
     * @param payment 发放信息
     * @return 操作结果
     */
    @PostMapping("/create")
    public ResponseResult<Boolean> create(@RequestBody RebatePayment payment) {
        return ResponseResult.success(rebatePaymentService.createPayment(payment));
    }

    /**
     * 执行发放
     * 
     * @param id 发放ID
     * @return 操作结果
     */
    @PostMapping("/execute/{id}")
    public ResponseResult<Boolean> execute(@PathVariable Long id) {
        return ResponseResult.success(rebatePaymentService.execute(id));
    }

    /**
     * 标记发放失败
     * 
     * @param params 参数（id, failReason）
     * @return 操作结果
     */
    @PostMapping("/fail")
    public ResponseResult<Boolean> markFailed(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        String failReason = (String) params.getOrDefault("failReason", "");
        return ResponseResult.success(rebatePaymentService.markFailed(id, failReason));
    }

    /**
     * 删除返利发放
     * 
     * @param id 发放ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(rebatePaymentService.removeById(id));
    }
}
