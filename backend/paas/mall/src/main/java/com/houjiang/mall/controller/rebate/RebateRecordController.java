package com.houjiang.mall.controller.rebate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.rebate.RebateRecord;
import com.houjiang.mall.service.rebate.RebateRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 返利记录控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/rebate/record")
@RequiredArgsConstructor
public class RebateRecordController {

    private final RebateRecordService rebateRecordService;

    /**
     * 分页查询返利记录
     * 
     * @param params 查询参数
     * @return 分页数据
     */
    @PostMapping("/list")
    public ResponseResult<IPage<RebateRecord>> list(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(rebateRecordService.pageList(params));
    }

    /**
     * 获取返利记录详情
     * 
     * @param id 记录ID
     * @return 记录详情
     */
    @GetMapping("/detail/{id}")
    public ResponseResult<RebateRecord> detail(@PathVariable Long id) {
        return ResponseResult.success(rebateRecordService.getById(id));
    }

    /**
     * 创建返利记录
     * 
     * @param record 记录信息
     * @return 操作结果
     */
    @PostMapping("/create")
    public ResponseResult<Boolean> create(@RequestBody RebateRecord record) {
        return ResponseResult.success(rebateRecordService.createRecord(record));
    }

    /**
     * 结算返利记录
     * 
     * @param id 记录ID
     * @return 操作结果
     */
    @PostMapping("/settle/{id}")
    public ResponseResult<Boolean> settle(@PathVariable Long id) {
        return ResponseResult.success(rebateRecordService.settle(id));
    }

    /**
     * 批量结算返利记录
     * 
     * @param params 参数（ids列表）
     * @return 操作结果
     */
    @PostMapping("/batchSettle")
    public ResponseResult<Boolean> batchSettle(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) params.get("ids");
        return ResponseResult.success(rebateRecordService.batchSettle(ids));
    }

    /**
     * 取消返利记录
     * 
     * @param params 参数（id, reason）
     * @return 操作结果
     */
    @PostMapping("/cancel")
    public ResponseResult<Boolean> cancel(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        String reason = (String) params.getOrDefault("reason", "");
        return ResponseResult.success(rebateRecordService.cancel(id, reason));
    }
}
