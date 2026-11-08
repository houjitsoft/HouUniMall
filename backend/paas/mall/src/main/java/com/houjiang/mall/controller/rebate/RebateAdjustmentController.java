package com.houjiang.mall.controller.rebate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.rebate.RebateAdjustment;
import com.houjiang.mall.service.rebate.RebateAdjustmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 返利调整控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/rebate/adjustment")
@RequiredArgsConstructor
public class RebateAdjustmentController {

    private final RebateAdjustmentService rebateAdjustmentService;

    /**
     * 分页查询返利调整
     * 
     * @param params 查询参数
     * @return 分页数据
     */
    @PostMapping("/list")
    public ResponseResult<IPage<RebateAdjustment>> list(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(rebateAdjustmentService.pageList(params));
    }

    /**
     * 获取返利调整详情
     * 
     * @param id 调整ID
     * @return 调整详情
     */
    @GetMapping("/detail/{id}")
    public ResponseResult<RebateAdjustment> detail(@PathVariable Long id) {
        return ResponseResult.success(rebateAdjustmentService.getById(id));
    }

    /**
     * 创建返利调整
     * 
     * @param adjustment 调整信息
     * @return 操作结果
     */
    @PostMapping("/create")
    public ResponseResult<Boolean> create(@RequestBody RebateAdjustment adjustment) {
        return ResponseResult.success(rebateAdjustmentService.createAdjustment(adjustment));
    }

    /**
     * 审核返利调整
     * 
     * @param params 参数（id, auditStatus, auditorId, auditorName, auditRemark）
     * @return 操作结果
     */
    @PostMapping("/audit")
    public ResponseResult<Boolean> audit(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Integer auditStatus = Integer.parseInt(params.get("auditStatus").toString());
        Long auditorId = Long.parseLong(params.get("auditorId").toString());
        String auditorName = (String) params.getOrDefault("auditorName", "");
        String auditRemark = (String) params.getOrDefault("auditRemark", "");
        return ResponseResult.success(rebateAdjustmentService.audit(id, auditStatus, auditorId, auditorName, auditRemark));
    }

    /**
     * 删除返利调整
     * 
     * @param id 调整ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(rebateAdjustmentService.removeById(id));
    }
}
