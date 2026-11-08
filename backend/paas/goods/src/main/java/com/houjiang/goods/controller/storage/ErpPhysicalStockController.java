package com.houjiang.goods.controller.storage;

import com.houjiang.goods.service.storage.ErpPhysicalStockService;
import com.houjiang.goods.vo.stock.ErpStockSyncRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ERP 物理仓同步接收 API。
 *
 * <h3>安全约束</h3>
 * 只接受 ERP 服务调用，不对外暴露给前端。
 * {@code X-Idempotent-Key} 头与请求体中的 erpFlowNo 必须一致，防止被伪造重放。
 *
 * <h3>三条铁律（与 ERP 侧对称）</h3>
 * <ol>
 *   <li>单向：本服务不回调 ERP</li>
 *   <li>幂等：按 erpFlowNo 去重</li>
 *   <li>缺行可补：ERP 有货而商城无库存行时自动补建</li>
 * </ol>
 *
 * @author Amos
 * @since 2026-10-16
 */
@RestController
@RequestMapping("/goods/storage/stock/erp")
@RequiredArgsConstructor
@Tag(name = "ERP 物理仓同步", description = "ERP 出入库过账后回写商城可售库存（内部接口）")
public class ErpPhysicalStockController {

    private final ErpPhysicalStockService service;

    /**
     * 物理出库：ERP 出库过账 → 回补可售、释放锁定。
     * <p>
     * 修复「ERP 出了货商城还显示有货可卖」的超卖缺陷。
     */
    @PostMapping("/physical-out")
    @Operation(summary = "ERP 物理出库同步")
    public ErpStockSyncRequest.Result physicalOut(
            @RequestHeader(value = "X-Idempotent-Key", required = false) String idempotentKey,
            @RequestBody ErpStockSyncRequest request) {
        return service.physicalOut(verify(request, idempotentKey));
    }

    /**
     * 物理入库：ERP 入库过账 → 增加可售。
     */
    @PostMapping("/physical-in")
    @Operation(summary = "ERP 物理入库同步")
    public ErpStockSyncRequest.Result physicalIn(
            @RequestHeader(value = "X-Idempotent-Key", required = false) String idempotentKey,
            @RequestBody ErpStockSyncRequest request) {
        return service.physicalIn(verify(request, idempotentKey));
    }

    /**
     * 调拨出库：转出仓扣减可售。
     */
    @PostMapping("/transfer-out")
    @Operation(summary = "ERP 调拨出库同步")
    public ErpStockSyncRequest.Result transferOut(
            @RequestHeader(value = "X-Idempotent-Key", required = false) String idempotentKey,
            @RequestBody ErpStockSyncRequest request) {
        return service.transferOut(verify(request, idempotentKey));
    }

    /**
     * 校验幂等键一致性。
     * <p>
     * 头与体不一致说明调用方实现有问题（或被中间人篡改），
     * 直接拒绝而不是二选一 —— 二选一会让幂等边界变得不可预测。
     */
    private ErpStockSyncRequest verify(ErpStockSyncRequest request, String headerKey) {
        if (request == null || request.getErpFlowNo() == null || request.getErpFlowNo().trim().isEmpty()) {
            throw new IllegalArgumentException("erpFlowNo 不能为空");
        }
        if (headerKey != null && !headerKey.trim().isEmpty()
                && !headerKey.trim().equals(request.getErpFlowNo().trim())) {
            throw new IllegalArgumentException(
                    "幂等键与请求体不一致：header=" + headerKey + " body=" + request.getErpFlowNo());
        }
        return request;
    }
}