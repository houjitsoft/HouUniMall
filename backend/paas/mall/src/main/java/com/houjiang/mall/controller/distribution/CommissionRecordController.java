package com.houjiang.mall.controller.distribution;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.distribution.CommissionRecord;
import com.houjiang.mall.service.distribution.CommissionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 佣金记录控制器
 * 提供佣金记录的列表查询、详情、结算、批量结算、统计等功能
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/distribution/commission")
@RequiredArgsConstructor
public class CommissionRecordController {

    private final CommissionRecordService commissionRecordService;

    /**
     * 分页查询佣金记录
     *
     * @param params 查询参数
     * @return 分页结果
     */
    @PostMapping("/list")
    public ResponseResult<IPage<CommissionRecord>> list(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(commissionRecordService.pageList(params));
    }

    /**
     * 获取佣金记录详情
     *
     * @param params 参数（包含 id）
     * @return 佣金记录详情
     */
    @PostMapping("/detail")
    public ResponseResult<CommissionRecord> detail(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return ResponseResult.success(commissionRecordService.getById(id));
    }

    /**
     * 创建佣金记录
     *
     * @param record 佣金记录信息
     * @return 是否成功
     */
    @PostMapping("/create")
    public ResponseResult<Boolean> create(@RequestBody CommissionRecord record) {
        return ResponseResult.success(commissionRecordService.createRecord(record));
    }

    /**
     * 结算佣金
     *
     * @param params 参数（包含 id）
     * @return 是否成功
     */
    @PostMapping("/settle")
    public ResponseResult<Boolean> settle(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return ResponseResult.success(commissionRecordService.settle(id));
    }

    /**
     * 批量结算佣金
     *
     * @param params 参数（包含 ids 列表）
     * @return 是否成功
     */
    @PostMapping("/batchSettle")
    public ResponseResult<Boolean> batchSettle(@RequestBody Map<String, Object> params) {
        Object raw = params.get("ids");
        if (!(raw instanceof java.util.List)) {
            return ResponseResult.success(false);
        }
        // Jackson 把 JSON 数字数组还原成 List<Integer>，强转 List<Long> 会抛 ClassCastException；
        // 这里按 Number.longValue() 逐个转换，兼容整数、长整型与字符串三种传参。
        java.util.List<Long> ids = new java.util.ArrayList<>();
        for (Object value : (java.util.List<?>) raw) {
            if (value instanceof Number) {
                ids.add(((Number) value).longValue());
            } else if (value != null) {
                ids.add(Long.parseLong(value.toString()));
            }
        }
        return ResponseResult.success(commissionRecordService.batchSettle(ids));
    }

    /**
     * 统计分销员佣金
     *
     * @param params 参数（包含 distributorId）
     * @return 佣金统计数据
     */
    @PostMapping("/stats")
    public ResponseResult<Map<String, BigDecimal>> stats(@RequestBody Map<String, Object> params) {
        Long distributorId = Long.parseLong(params.get("distributorId").toString());
        return ResponseResult.success(commissionRecordService.statsCommission(distributorId));
    }
}
