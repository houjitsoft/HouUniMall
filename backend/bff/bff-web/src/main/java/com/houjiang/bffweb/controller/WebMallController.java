package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GoodsSellableStockFeign;
import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.feign.OssServiceFeign;
import com.houjiang.bffweb.service.LocalFileUploadService;
import com.houjiang.bffweb.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Mall商城控制器（BFF层）
/**
 * 提供分销、返利、仓储、风控模块接口
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/mall")
@Tag(name = "Mall商城管理", description = "分销、返利、仓储、风控模块接口")
public class WebMallController {

    @Autowired
    private MallServiceFeign mallServiceFeign;

    @Autowired
    private GoodsSellableStockFeign goodsSellableStockFeign;

    @Autowired
    private OssServiceFeign ossServiceFeign;

    @Autowired
    private LocalFileUploadService localFileUploadService;

    /** local=本机 public/uploads/mall；oss=走 thirdparty（易超时） */
    @Value("${mall.upload.mode:local}")
    private String mallUploadMode;

    /** ================= 店铺管理 ================= */

    @PostMapping("/shop/list")
    @Operation(summary = "分页查询店铺列表")
    public ResponseResult getShopList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getShopList(param);
    }

    @GetMapping("/shop/detail/{id}")
    @Operation(summary = "获取店铺详情")
    public ResponseResult getShopDetail(@PathVariable Long id) {
        return mallServiceFeign.getShopDetail(id);
    }

    @PostMapping("/shop/create")
    @Operation(summary = "创建店铺")
    public ResponseResult createShop(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.createShop(param);
    }

    @PostMapping("/shop/update")
    @Operation(summary = "更新店铺信息")
    public ResponseResult updateShop(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.updateShop(param);
    }

    @PostMapping("/shop/update-status")
    @Operation(summary = "更新店铺状态(启用/禁用/冻结)")
    public ResponseResult updateShopStatus(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.updateShopStatus(param);
    }

    @PostMapping("/shop/overview")
    @Operation(summary = "获取店铺概览统计")
    public ResponseResult getShopOverview() {
        return mallServiceFeign.getShopOverview();
    }

    @PostMapping("/shop/level/list")
    @Operation(summary = "店铺等级列表")
    public ResponseResult getShopLevelList() {
        return mallServiceFeign.getShopLevelList();
    }

    @PostMapping("/shop/level/create")
    @Operation(summary = "创建店铺等级")
    public ResponseResult createShopLevel(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.createShopLevel(param);
    }

    @PostMapping("/shop/level/update")
    @Operation(summary = "更新店铺等级")
    public ResponseResult updateShopLevel(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.updateShopLevel(param);
    }

    @PostMapping("/shop/entry/list")
    @Operation(summary = "商家入驻申请列表")
    public ResponseResult getMerchantEntryList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getMerchantEntryList(param);
    }

    @PostMapping("/shop/entry/audit")
    @Operation(summary = "商家入驻审核")
    public ResponseResult auditMerchantEntry(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.auditMerchantEntry(param);
    }

    @PostMapping("/shop/shipping/address/queryDeliveryTime")
    @Operation(summary = "查询配送时效")
    public ResponseResult queryDeliveryTime(@RequestBody(required = false) Map<String, Object> param) {
        return ResponseResult.success(null);
    }

    @PostMapping("/shop/cashOnDelivery/queryIsDelivery")
    @Operation(summary = "查询货到付款开关")
    public ResponseResult queryIsDelivery(@RequestBody(required = false) Map<String, Object> param) {
        Map<String, Object> result = new HashMap<>();
        result.put("shopId", null);
        result.put("isDelivery", 0);
        return ResponseResult.success(result);
    }

    /** ================= 财务管理 - 概览 ================= */

    // 平台财务页面统一从 /web/mall/finance 转发，避免前端直连 merchant 内部接口。
    @PostMapping("/finance/overview")
    @Operation(summary = "获取财务概览统计")
    public ResponseResult getFinanceOverview() {
        return mallServiceFeign.getFinanceOverview();
    }

    @PostMapping("/finance/center")
    @Operation(summary = "获取商城平台财务中心补充指标")
    public ResponseResult getMerchantFinanceCenter() {
        return mallServiceFeign.getMerchantFinanceCenter();
    }

    @PostMapping("/finance/refund/stats")
    public ResponseResult getRefundStats(@RequestBody(required = false) Map<String, Object> param) {
        return mallServiceFeign.getRefundStats(param == null ? Map.of() : param);
    }

    @PostMapping("/finance/refund/list")
    public ResponseResult getRefundList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getRefundList(param);
    }

    @PostMapping("/finance/refund/retry")
    public ResponseResult retryRefund(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.retryRefund(param);
    }

    @PostMapping("/finance/shop/balance/list")
    public ResponseResult getShopBalanceList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getShopBalanceList(param);
    }

    @PostMapping("/finance/user/wallet/list")
    public ResponseResult getUserWalletList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getUserWalletList(param);
    }

    @PostMapping("/finance/user/recharge/list")
    public ResponseResult getUserRechargeList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getUserRechargeList(param);
    }

    /** ================= 财务管理 - 提现 ================= */

    @PostMapping("/finance/withdraw/list")
    @Operation(summary = "分页查询提现列表")
    public ResponseResult getWithdrawList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getWithdrawList(param);
    }

    @PostMapping("/finance/withdraw/detail")
    @Operation(summary = "获取提现详情")
    public ResponseResult getWithdrawDetail(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getWithdrawDetail(param);
    }

    @PostMapping("/finance/withdraw/audit")
    @Operation(summary = "提现审核(通过/拒绝)")
    public ResponseResult auditWithdraw(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.auditWithdraw(param);
    }

    @PostMapping("/finance/withdraw/stats")
    @Operation(summary = "获取提现统计")
    public ResponseResult getWithdrawStats(@RequestBody(required = false) Map<String, Object> param) {
        return mallServiceFeign.getWithdrawStats(param == null ? Map.of() : param);
    }

    /** ================= 财务管理 - 收入 ================= */

    @PostMapping("/finance/income/list")
    @Operation(summary = "分页查询收入列表")
    public ResponseResult getFinanceIncomeList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getFinanceIncomeList(param);
    }

    @PostMapping("/finance/income/detail")
    @Operation(summary = "获取收入详情")
    public ResponseResult getFinanceIncomeDetail(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getFinanceIncomeDetail(param);
    }

    @PostMapping("/finance/income/create")
    @Operation(summary = "创建收入记录")
    public ResponseResult createFinanceIncome(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.createFinanceIncome(param);
    }

    @PostMapping("/finance/income/confirm")
    @Operation(summary = "确认收入记录")
    public ResponseResult confirmFinanceIncome(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.confirmFinanceIncome(param);
    }

    /** ================= 财务管理 - 支出 ================= */

    @PostMapping("/finance/expense/list")
    @Operation(summary = "分页查询支出列表")
    public ResponseResult getFinanceExpenseList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getFinanceExpenseList(param);
    }

    @PostMapping("/finance/expense/detail")
    @Operation(summary = "获取支出详情")
    public ResponseResult getFinanceExpenseDetail(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getFinanceExpenseDetail(param);
    }

    @PostMapping("/finance/expense/create")
    @Operation(summary = "创建支出记录")
    public ResponseResult createFinanceExpense(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.createFinanceExpense(param);
    }

    @PostMapping("/finance/expense/confirm")
    @Operation(summary = "确认支出记录")
    public ResponseResult confirmFinanceExpense(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.confirmFinanceExpense(param);
    }

    /** ================= 财务管理 - 结算 ================= */

    @PostMapping("/finance/settlement/list")
    @Operation(summary = "分页查询结算列表")
    public ResponseResult getFinanceSettlementList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getFinanceSettlementList(param);
    }

    @PostMapping("/finance/settlement/detail")
    @Operation(summary = "获取结算详情")
    public ResponseResult getFinanceSettlementDetail(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getFinanceSettlementDetail(param);
    }

    @PostMapping("/finance/settlement/create")
    @Operation(summary = "创建结算单")
    public ResponseResult createFinanceSettlement(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.createFinanceSettlement(param);
    }

    @PostMapping("/finance/settlement/settle")
    @Operation(summary = "执行结算")
    public ResponseResult settleFinanceSettlement(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.settleFinanceSettlement(param);
    }

    /** ================= 分销模块 - 分销员 ================= */

    @PostMapping("/distribution/distributor/list")
    @Operation(summary = "分页查询分销员")
    public ResponseResult getDistributorList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getDistributorList(param);
    }

    @PostMapping("/distribution/distributor/detail")
    @Operation(summary = "获取分销员详情")
    public ResponseResult getDistributorDetail(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getDistributorDetail(param);
    }

    @PostMapping("/distribution/distributor/apply")
    @Operation(summary = "申请成为分销员")
    public ResponseResult applyDistributor(@RequestBody Map<String, Object> distributor) {
        return mallServiceFeign.applyDistributor(distributor);
    }

    @PostMapping("/distribution/distributor/audit")
    @Operation(summary = "审核分销员申请")
    public ResponseResult auditDistributor(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.auditDistributor(param);
    }

    @PostMapping("/distribution/distributor/status")
    @Operation(summary = "更新分销员状态")
    public ResponseResult updateDistributorStatus(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.updateDistributorStatus(param);
    }

    @PostMapping("/distribution/distributor/update")
    @Operation(summary = "更新分销员信息")
    public ResponseResult updateDistributor(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.updateDistributor(param);
    }

    @PostMapping("/distribution/distributor/delete")
    @Operation(summary = "删除分销员")
    public ResponseResult deleteDistributor(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.deleteDistributor(param);
    }

    /** ================= 分销模块 - 分销等级 ================= */

    @PostMapping("/distribution/level/list")
    @Operation(summary = "分页查询分销等级")
    public ResponseResult getDistributorLevelList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getDistributorLevelList(param);
    }

    @PostMapping("/distribution/level/all")
    @Operation(summary = "获取所有分销等级")
    public ResponseResult getAllDistributorLevels(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getAllDistributorLevels(param);
    }

    @PostMapping("/distribution/level/create")
    @Operation(summary = "创建分销等级")
    public ResponseResult createDistributorLevel(@RequestBody Map<String, Object> level) {
        return mallServiceFeign.createDistributorLevel(level);
    }

    @PostMapping("/distribution/level/update")
    @Operation(summary = "更新分销等级")
    public ResponseResult updateDistributorLevel(@RequestBody Map<String, Object> level) {
        return mallServiceFeign.updateDistributorLevel(level);
    }

    @PostMapping("/distribution/level/delete")
    @Operation(summary = "删除分销等级")
    public ResponseResult deleteDistributorLevel(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.deleteDistributorLevel(param);
    }

    @PostMapping("/distribution/level/detail")
    @Operation(summary = "获取分销等级详情")
    public ResponseResult getDistributorLevelDetail(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getDistributorLevelDetail(param);
    }

    /** ================= 分销模块 - 佣金记录 ================= */

    @PostMapping("/distribution/commission/list")
    @Operation(summary = "分页查询佣金记录")
    public ResponseResult getCommissionRecordList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getCommissionRecordList(param);
    }

    @PostMapping("/distribution/commission/detail")
    @Operation(summary = "获取佣金记录详情")
    public ResponseResult getCommissionRecordDetail(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getCommissionRecordDetail(param);
    }

    @PostMapping("/distribution/commission/settle")
    @Operation(summary = "结算佣金")
    public ResponseResult settleCommission(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.settleCommission(param);
    }

    @PostMapping("/distribution/commission/batchSettle")
    @Operation(summary = "批量结算佣金")
    public ResponseResult batchSettleCommission(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.batchSettleCommission(param);
    }

    @PostMapping("/distribution/commission/stats")
    @Operation(summary = "佣金统计")
    public ResponseResult getCommissionStats(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getCommissionStats(param);
    }

    /** ================= 分销模块 - 佣金规则 ================= */

    @PostMapping("/distribution/rule/list")
    @Operation(summary = "分页查询佣金规则")
    public ResponseResult getCommissionRuleList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getCommissionRuleList(param);
    }

    @PostMapping("/distribution/rule/create")
    @Operation(summary = "创建佣金规则")
    public ResponseResult createCommissionRule(@RequestBody Map<String, Object> rule) {
        return mallServiceFeign.createCommissionRule(rule);
    }

    @PostMapping("/distribution/rule/update")
    @Operation(summary = "更新佣金规则")
    public ResponseResult updateCommissionRule(@RequestBody Map<String, Object> rule) {
        return mallServiceFeign.updateCommissionRule(rule);
    }

    @PostMapping("/distribution/rule/delete")
    @Operation(summary = "删除佣金规则")
    public ResponseResult deleteCommissionRule(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.deleteCommissionRule(param);
    }

    @PostMapping("/distribution/rule/detail")
    @Operation(summary = "获取佣金规则详情")
    public ResponseResult getCommissionRuleDetail(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getCommissionRuleDetail(param);
    }

    /** ================= 分销模块 - 分销订单 ================= */

    @PostMapping("/distribution/order/list")
    @Operation(summary = "分页查询分销订单")
    public ResponseResult getDistributionOrderList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getDistributionOrderList(param);
    }

    @PostMapping("/distribution/order/detail")
    @Operation(summary = "获取分销订单详情")
    public ResponseResult getDistributionOrderDetail(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getDistributionOrderDetail(param);
    }

    @PostMapping("/distribution/order/status")
    @Operation(summary = "更新分销订单状态")
    public ResponseResult updateDistributionOrderStatus(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.updateDistributionOrderStatus(param);
    }

    /** ================= 分销模块 - 分销商品 ================= */

    @PostMapping("/distribution/goods/list")
    @Operation(summary = "分页查询分销商品")
    public ResponseResult getDistributionGoodsList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getDistributionGoodsList(param);
    }

    @PostMapping("/distribution/goods/detail")
    @Operation(summary = "获取分销商品详情")
    public ResponseResult getDistributionGoodsDetail(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getDistributionGoodsDetail(param);
    }

    @PostMapping("/distribution/goods/create")
    @Operation(summary = "创建分销商品")
    public ResponseResult createDistributionGoods(@RequestBody Map<String, Object> goods) {
        return mallServiceFeign.createDistributionGoods(goods);
    }

    @PostMapping("/distribution/goods/update")
    @Operation(summary = "更新分销商品")
    public ResponseResult updateDistributionGoods(@RequestBody Map<String, Object> goods) {
        return mallServiceFeign.updateDistributionGoods(goods);
    }

    @PostMapping("/distribution/goods/status")
    @Operation(summary = "更新分销商品状态")
    public ResponseResult updateDistributionGoodsStatus(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.updateDistributionGoodsStatus(param);
    }

    @PostMapping("/distribution/goods/delete")
    @Operation(summary = "删除分销商品")
    public ResponseResult deleteDistributionGoods(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.deleteDistributionGoods(param);
    }

    /** ================= 返利模块 - 返利规则 ================= */

    @PostMapping("/rebate/rule/list")
    @Operation(summary = "分页查询返利规则")
    public ResponseResult getRebateRuleList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getRebateRuleList(param);
    }

    @GetMapping("/rebate/rule/detail/{id}")
    @Operation(summary = "获取返利规则详情")
    public ResponseResult getRebateRuleDetail(@PathVariable Long id) {
        return mallServiceFeign.getRebateRuleDetail(id);
    }

    @PostMapping("/rebate/rule/create")
    @Operation(summary = "创建返利规则")
    public ResponseResult createRebateRule(@RequestBody Map<String, Object> rule) {
        return mallServiceFeign.createRebateRule(rule);
    }

    @PostMapping("/rebate/rule/update")
    @Operation(summary = "更新返利规则")
    public ResponseResult updateRebateRule(@RequestBody Map<String, Object> rule) {
        return mallServiceFeign.updateRebateRule(rule);
    }

    @PostMapping("/rebate/rule/status")
    @Operation(summary = "更新返利规则状态")
    public ResponseResult updateRebateRuleStatus(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.updateRebateRuleStatus(param);
    }

    @DeleteMapping("/rebate/rule/delete/{id}")
    @Operation(summary = "删除返利规则")
    public ResponseResult deleteRebateRule(@PathVariable Long id) {
        return mallServiceFeign.deleteRebateRule(id);
    }

    /** ================= 返利模块 - 返利记录 ================= */

    @PostMapping("/rebate/record/list")
    @Operation(summary = "分页查询返利记录")
    public ResponseResult getRebateRecordList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getRebateRecordList(param);
    }

    @GetMapping("/rebate/record/detail/{id}")
    @Operation(summary = "获取返利记录详情")
    public ResponseResult getRebateRecordDetail(@PathVariable Long id) {
        return mallServiceFeign.getRebateRecordDetail(id);
    }

    @PostMapping("/rebate/record/settle/{id}")
    @Operation(summary = "结算返利记录")
    public ResponseResult settleRebateRecord(@PathVariable Long id) {
        return mallServiceFeign.settleRebateRecord(id);
    }

    @PostMapping("/rebate/record/batchSettle")
    @Operation(summary = "批量结算返利记录")
    public ResponseResult batchSettleRebateRecord(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.batchSettleRebateRecord(param);
    }

    @PostMapping("/rebate/record/cancel")
    @Operation(summary = "取消返利记录")
    public ResponseResult cancelRebateRecord(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.cancelRebateRecord(param);
    }

    /** ================= 返利模块 - 返利调整 ================= */

    @PostMapping("/rebate/adjustment/list")
    @Operation(summary = "分页查询返利调整")
    public ResponseResult getRebateAdjustmentList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getRebateAdjustmentList(param);
    }

    @PostMapping("/rebate/adjustment/create")
    @Operation(summary = "创建返利调整")
    public ResponseResult createRebateAdjustment(@RequestBody Map<String, Object> adjustment) {
        return mallServiceFeign.createRebateAdjustment(adjustment);
    }

    @PostMapping("/rebate/adjustment/audit")
    @Operation(summary = "审核返利调整")
    public ResponseResult auditRebateAdjustment(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.auditRebateAdjustment(param);
    }

    /** ================= 返利模块 - 返利发放 ================= */

    @PostMapping("/rebate/payment/list")
    @Operation(summary = "分页查询返利发放")
    public ResponseResult getRebatePaymentList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getRebatePaymentList(param);
    }

    @PostMapping("/rebate/payment/create")
    @Operation(summary = "创建返利发放")
    public ResponseResult createRebatePayment(@RequestBody Map<String, Object> payment) {
        return mallServiceFeign.createRebatePayment(payment);
    }

    @PostMapping("/rebate/payment/execute/{id}")
    @Operation(summary = "执行返利发放")
    public ResponseResult executeRebatePayment(@PathVariable Long id) {
        return mallServiceFeign.executeRebatePayment(id);
    }

    /** ================= 仓储模块 - 库存管理 ================= */

    @PostMapping("/storage/stock/list")
    @Operation(summary = "分页查询库存")
    public ResponseResult getStockList(@RequestBody Map<String, Object> param) {
        return goodsSellableStockFeign.getStockList(param);
    }

    @GetMapping("/storage/stock/detail/{id}")
    @Operation(summary = "获取库存详情")
    public ResponseResult getStockDetail(@PathVariable Long id) {
        return goodsSellableStockFeign.getStockDetail(id);
    }

    @GetMapping("/storage/stock/warehouse/{warehouseId}")
    @Operation(summary = "获取仓库库存列表")
    public ResponseResult getStocksByWarehouse(@PathVariable Long warehouseId) {
        return goodsSellableStockFeign.getStocksByWarehouse(warehouseId);
    }

    @GetMapping("/storage/stock/warning")
    @Operation(summary = "获取库存预警列表")
    public ResponseResult getWarningStocks(@RequestParam(required = false) Long warehouseId) {
        return goodsSellableStockFeign.getWarningStocks(warehouseId);
    }

    @PostMapping("/storage/stock/create")
    @Operation(summary = "创建库存记录")
    public ResponseResult createStock(@RequestBody Map<String, Object> stock) {
        return goodsSellableStockFeign.createStock(stock);
    }

    @PostMapping("/storage/stock/update")
    @Operation(summary = "更新库存")
    public ResponseResult updateStock(@RequestBody Map<String, Object> stock) {
        return goodsSellableStockFeign.updateStock(stock);
    }

    @PostMapping("/storage/stock/increase")
    @Operation(summary = "增加库存")
    public ResponseResult increaseStock(@RequestBody Map<String, Object> param) {
        return goodsSellableStockFeign.increaseStock(param);
    }

    @PostMapping("/storage/stock/decrease")
    @Operation(summary = "减少库存")
    public ResponseResult decreaseStock(@RequestBody Map<String, Object> param) {
        return goodsSellableStockFeign.decreaseStock(param);
    }

    @PostMapping("/storage/stock/lock")
    @Operation(summary = "锁定库存")
    public ResponseResult lockStock(@RequestBody Map<String, Object> param) {
        return goodsSellableStockFeign.lockStock(param);
    }

    @PostMapping("/storage/stock/unlock")
    @Operation(summary = "释放锁定库存")
    public ResponseResult unlockStock(@RequestBody Map<String, Object> param) {
        return goodsSellableStockFeign.unlockStock(param);
    }

    @DeleteMapping("/storage/stock/delete/{id}")
    @Operation(summary = "删除库存")
    public ResponseResult deleteStock(@PathVariable Long id) {
        return goodsSellableStockFeign.deleteStock(id);
    }

    /** ================= 仓储模块 - 仓库管理 ================= */

    @PostMapping("/storage/warehouse/list")
    @Operation(summary = "分页查询仓库")
    public ResponseResult getWarehouseList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getWarehouseList(param);
    }

    @GetMapping("/storage/warehouse/enabled")
    @Operation(summary = "获取启用的仓库列表")
    public ResponseResult getEnabledWarehouses(@RequestParam(required = false) Long shopId) {
        return mallServiceFeign.getEnabledWarehouses(shopId);
    }

    @GetMapping("/storage/warehouse/detail/{id}")
    @Operation(summary = "获取仓库详情")
    public ResponseResult getWarehouseDetail(@PathVariable Long id) {
        return mallServiceFeign.getWarehouseDetail(id);
    }

    @PostMapping("/storage/warehouse/create")
    @Operation(summary = "创建仓库")
    public ResponseResult createWarehouse(@RequestBody Map<String, Object> warehouse) {
        return mallServiceFeign.createWarehouse(warehouse);
    }

    @PostMapping("/storage/warehouse/update")
    @Operation(summary = "更新仓库")
    public ResponseResult updateWarehouse(@RequestBody Map<String, Object> warehouse) {
        return mallServiceFeign.updateWarehouse(warehouse);
    }

    @PostMapping("/storage/warehouse/setDefault")
    @Operation(summary = "设置默认仓库")
    public ResponseResult setDefaultWarehouse(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.setDefaultWarehouse(param);
    }

    @DeleteMapping("/storage/warehouse/delete/{id}")
    @Operation(summary = "删除仓库")
    public ResponseResult deleteWarehouse(@PathVariable Long id) {
        return mallServiceFeign.deleteWarehouse(id);
    }

    /** ================= 仓储模块 - 入库管理 ================= */

    @PostMapping("/storage/inbound/list")
    @Operation(summary = "分页查询入库单")
    public ResponseResult getStockInboundList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getStockInboundList(param);
    }

    @GetMapping("/storage/inbound/detail/{id}")
    @Operation(summary = "获取入库单详情")
    public ResponseResult getStockInboundDetail(@PathVariable Long id) {
        return mallServiceFeign.getStockInboundDetail(id);
    }

    @PostMapping("/storage/inbound/create")
    @Operation(summary = "创建入库单")
    public ResponseResult createStockInbound(@RequestBody Map<String, Object> inbound) {
        return mallServiceFeign.createStockInbound(inbound);
    }

    @PostMapping("/storage/inbound/confirm/{id}")
    @Operation(summary = "确认入库")
    public ResponseResult confirmStockInbound(@PathVariable Long id) {
        return mallServiceFeign.confirmStockInbound(id);
    }

    @PostMapping("/storage/inbound/cancel/{id}")
    @Operation(summary = "取消入库")
    public ResponseResult cancelStockInbound(@PathVariable Long id) {
        return mallServiceFeign.cancelStockInbound(id);
    }

    /** ================= 仓储模块 - 出库管理 ================= */

    @PostMapping("/storage/outbound/list")
    @Operation(summary = "分页查询出库单")
    public ResponseResult getStockOutboundList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getStockOutboundList(param);
    }

    @GetMapping("/storage/outbound/detail/{id}")
    @Operation(summary = "获取出库单详情")
    public ResponseResult getStockOutboundDetail(@PathVariable Long id) {
        return mallServiceFeign.getStockOutboundDetail(id);
    }

    @PostMapping("/storage/outbound/create")
    @Operation(summary = "创建出库单")
    public ResponseResult createStockOutbound(@RequestBody Map<String, Object> outbound) {
        return mallServiceFeign.createStockOutbound(outbound);
    }

    @PostMapping("/storage/outbound/confirm/{id}")
    @Operation(summary = "确认出库")
    public ResponseResult confirmStockOutbound(@PathVariable Long id) {
        return mallServiceFeign.confirmStockOutbound(id);
    }

    @PostMapping("/storage/outbound/cancel/{id}")
    @Operation(summary = "取消出库")
    public ResponseResult cancelStockOutbound(@PathVariable Long id) {
        return mallServiceFeign.cancelStockOutbound(id);
    }

    /** ================= 仓储模块 - 库存调拨 ================= */

    @PostMapping("/storage/transfer/list")
    @Operation(summary = "分页查询调拨单")
    public ResponseResult getStockTransferList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getStockTransferList(param);
    }

    @GetMapping("/storage/transfer/detail/{id}")
    @Operation(summary = "获取调拨单详情")
    public ResponseResult getStockTransferDetail(@PathVariable Long id) {
        return mallServiceFeign.getStockTransferDetail(id);
    }

    @PostMapping("/storage/transfer/create")
    @Operation(summary = "创建调拨单")
    public ResponseResult createStockTransfer(@RequestBody Map<String, Object> transfer) {
        return mallServiceFeign.createStockTransfer(transfer);
    }

    @PostMapping("/storage/transfer/audit")
    @Operation(summary = "审核调拨单")
    public ResponseResult auditStockTransfer(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.auditStockTransfer(param);
    }

    @PostMapping("/storage/transfer/complete/{id}")
    @Operation(summary = "完成调拨")
    public ResponseResult completeStockTransfer(@PathVariable Long id) {
        return mallServiceFeign.completeStockTransfer(id);
    }

    /** ================= 仓储模块 - 库存盘点 ================= */

    @PostMapping("/storage/inventory/list")
    @Operation(summary = "分页查询盘点单")
    public ResponseResult getStockInventoryList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getStockInventoryList(param);
    }

    @GetMapping("/storage/inventory/detail/{id}")
    @Operation(summary = "获取盘点单详情")
    public ResponseResult getStockInventoryDetail(@PathVariable Long id) {
        return mallServiceFeign.getStockInventoryDetail(id);
    }

    @PostMapping("/storage/inventory/create")
    @Operation(summary = "创建盘点单")
    public ResponseResult createStockInventory(@RequestBody Map<String, Object> inventory) {
        return mallServiceFeign.createStockInventory(inventory);
    }

    @PostMapping("/storage/inventory/start/{id}")
    @Operation(summary = "开始盘点")
    public ResponseResult startStockInventory(@PathVariable Long id) {
        return mallServiceFeign.startStockInventory(id);
    }

    @PostMapping("/storage/inventory/submit")
    @Operation(summary = "提交盘点结果")
    public ResponseResult submitStockInventory(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.submitStockInventory(param);
    }

    @PostMapping("/storage/inventory/audit")
    @Operation(summary = "审核盘点")
    public ResponseResult auditStockInventory(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.auditStockInventory(param);
    }

    /** ================= 风控模块 - 预警规则 ================= */

    @PostMapping("/risk/rule/list")
    @Operation(summary = "分页查询风控规则")
    public ResponseResult getRiskRuleList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getRiskRuleList(param);
    }

    @GetMapping("/risk/rule/enabled")
    @Operation(summary = "获取启用的风控规则")
    public ResponseResult getEnabledRiskRules(@RequestParam(required = false) Long shopId) {
        return mallServiceFeign.getEnabledRiskRules(shopId);
    }

    @GetMapping("/risk/rule/detail/{id}")
    @Operation(summary = "获取风控规则详情")
    public ResponseResult getRiskRuleDetail(@PathVariable Long id) {
        return mallServiceFeign.getRiskRuleDetail(id);
    }

    @PostMapping("/risk/rule/create")
    @Operation(summary = "创建风控规则")
    public ResponseResult createRiskRule(@RequestBody Map<String, Object> rule) {
        return mallServiceFeign.createRiskRule(rule);
    }

    @PostMapping("/risk/rule/update")
    @Operation(summary = "更新风控规则")
    public ResponseResult updateRiskRule(@RequestBody Map<String, Object> rule) {
        return mallServiceFeign.updateRiskRule(rule);
    }

    @PostMapping("/risk/rule/status")
    @Operation(summary = "更新风控规则状态")
    public ResponseResult updateRiskRuleStatus(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.updateRiskRuleStatus(param);
    }

    @DeleteMapping("/risk/rule/delete/{id}")
    @Operation(summary = "删除风控规则")
    public ResponseResult deleteRiskRule(@PathVariable Long id) {
        return mallServiceFeign.deleteRiskRule(id);
    }

    /** ================= 风控模块 - 实时监控 ================= */

    @PostMapping("/risk/monitor/list")
    @Operation(summary = "分页查询监控记录")
    public ResponseResult getRiskMonitorList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getRiskMonitorList(param);
    }

    @GetMapping("/risk/monitor/detail/{id}")
    @Operation(summary = "获取监控记录详情")
    public ResponseResult getRiskMonitorDetail(@PathVariable Long id) {
        return mallServiceFeign.getRiskMonitorDetail(id);
    }

    @GetMapping("/risk/monitor/statistics")
    @Operation(summary = "获取监控统计数据")
    public ResponseResult getRiskMonitorStatistics(@RequestParam(required = false) Long shopId) {
        return mallServiceFeign.getRiskMonitorStatistics(shopId);
    }

    @PostMapping("/risk/monitor/save")
    @Operation(summary = "保存监控记录")
    public ResponseResult saveRiskMonitor(@RequestBody Map<String, Object> monitor) {
        return mallServiceFeign.saveRiskMonitor(monitor);
    }

    /** ================= 风控模块 - 预警处理 ================= */

    @PostMapping("/risk/alert/list")
    @Operation(summary = "分页查询预警记录")
    public ResponseResult getRiskAlertList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getRiskAlertList(param);
    }

    @GetMapping("/risk/alert/detail/{id}")
    @Operation(summary = "获取预警详情")
    public ResponseResult getRiskAlertDetail(@PathVariable Long id) {
        return mallServiceFeign.getRiskAlertDetail(id);
    }

    @GetMapping("/risk/alert/pendingCount")
    @Operation(summary = "获取待处理预警数量")
    public ResponseResult getRiskAlertPendingCount(@RequestParam(required = false) Long shopId) {
        return mallServiceFeign.getRiskAlertPendingCount(shopId);
    }

    @PostMapping("/risk/alert/create")
    @Operation(summary = "创建预警记录")
    public ResponseResult createRiskAlert(@RequestBody Map<String, Object> alert) {
        return mallServiceFeign.createRiskAlert(alert);
    }

    @PostMapping("/risk/alert/handle")
    @Operation(summary = "处理预警")
    public ResponseResult handleRiskAlert(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.handleRiskAlert(param);
    }

    @PostMapping("/risk/alert/ignore")
    @Operation(summary = "忽略预警")
    public ResponseResult ignoreRiskAlert(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.ignoreRiskAlert(param);
    }

    // ================= dcenter 商家管理后台 - 商品管理 =================
    // ⚠️ 已移除：商品列表、详情、保存、删除等方法已迁移到 MallGoodsController
    // 避免 Ambiguous mapping 冲突
    // 相关接口请使用：/web/mall/goods/* 路径

    /** ================= dcenter 商家管理后台 - 商品评价 ================= */

    @PostMapping("/comment/list")
    @Operation(summary = "评价列表")
    public ResponseResult getCommentList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getCommentList(params);
    }

    @PostMapping("/comment/audit")
    @Operation(summary = "审核评价")
    public ResponseResult auditComment(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.auditComment(params);
    }

    @PostMapping("/comment/reply")
    @Operation(summary = "回复评价")
    public ResponseResult replyComment(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.replyComment(params);
    }

    /** ================= dcenter 商家管理后台 - 会员管理 ================= */

    @PostMapping("/member/list")
    @Operation(summary = "会员列表")
    public ResponseResult getMemberList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getMemberList(params);
    }

    @PostMapping("/member/detail")
    @Operation(summary = "会员详情")
    public ResponseResult getMemberDetail(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getMemberDetail(params);
    }

    @PostMapping("/member/updateLevel")
    @Operation(summary = "更新会员等级")
    public ResponseResult updateMemberLevel(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.updateMemberLevel(params);
    }

    /** ================= dcenter 商家管理后台 - 店铺装修 ================= */

    @PostMapping("/decoration/page/list")
    @Operation(summary = "装修页面列表")
    public ResponseResult getDecoratePageList(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return mallServiceFeign.getDecoratePageList(params, authorization);
    }

    @PostMapping("/decoration/page/detail")
    @Operation(summary = "装修页面详情")
    public ResponseResult getDecoratePageDetail(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return mallServiceFeign.getDecoratePageDetail(params, authorization);
    }

    @PostMapping("/decoration/page/save")
    @Operation(summary = "保存装修页面")
    public ResponseResult saveDecoratePage(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return mallServiceFeign.saveDecoratePage(params, authorization);
    }

    @PostMapping("/decoration/page/delete")
    @Operation(summary = "删除装修页面")
    public ResponseResult deleteDecoratePage(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return mallServiceFeign.deleteDecoratePage(params, authorization);
    }

    @PostMapping("/decoration/home/list")
    @Operation(summary = "首页列表")
    public ResponseResult getHomePageList(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return mallServiceFeign.getHomePageList(params, authorization);
    }

    @PostMapping("/decoration/home/set")
    @Operation(summary = "设为首页")
    public ResponseResult setHomePage(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return mallServiceFeign.setHomePage(params, authorization);
    }

    /** ================= 平台管理后台素材库（兼容 like-admin /file/*） ================= */

    @GetMapping("/file/listCate")
    @Operation(summary = "素材分类列表")
    public ResponseResult adminAlbumListCate(
            @RequestParam Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return mallServiceFeign.adminAlbumListCate(params, authorization);
    }

    @GetMapping("/file/lists")
    @Operation(summary = "素材文件列表")
    public ResponseResult adminAlbumLists(
            @RequestParam Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return mallServiceFeign.adminAlbumLists(params, authorization);
    }

    @PostMapping("/file/addCate")
    public ResponseResult adminAlbumAddCate(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return mallServiceFeign.adminAlbumAddCate(params, authorization);
    }

    @PostMapping("/file/editCate")
    public ResponseResult adminAlbumEditCate(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return mallServiceFeign.adminAlbumEditCate(params, authorization);
    }

    @PostMapping("/file/delCate")
    public ResponseResult adminAlbumDelCate(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return mallServiceFeign.adminAlbumDelCate(params, authorization);
    }

    @PostMapping("/file/delete")
    public ResponseResult adminAlbumDelete(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return mallServiceFeign.adminAlbumDelete(params, authorization);
    }

    @PostMapping("/file/move")
    public ResponseResult adminAlbumMove(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return mallServiceFeign.adminAlbumMove(params, authorization);
    }

    @PostMapping("/file/rename")
    public ResponseResult adminAlbumRename(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return mallServiceFeign.adminAlbumRename(params, authorization);
    }

    @PostMapping(value = "/upload/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "素材上传（默认本机磁盘 + 入库）")
    public ResponseResult<?> adminAlbumUpload(
            @PathVariable("type") String type,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "cid", defaultValue = "0") String cid,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (file == null || file.isEmpty()) {
            return ResponseResult.fail(400, "请选择文件");
        }
        if (authorization == null || authorization.isBlank()) {
            return ResponseResult.fail(11006, "请先登录商城管理后台");
        }
        String url;
        try {
            url = resolveUploadUrl(type, file);
        } catch (Exception e) {
            log.error("素材上传失败 type={} file={}", type, file.getOriginalFilename(), e);
            return ResponseResult.fail(500, "上传失败: " + e.getMessage());
        }
        if (url == null || url.isEmpty()) {
            return ResponseResult.fail(500, "未生成文件地址");
        }
        Map<String, Object> record = new HashMap<>();
        record.put("cid", cid);
        record.put("type", "video".equalsIgnoreCase(type) ? 20 : 10);
        record.put("name", file.getOriginalFilename());
        record.put("uri", url);
        ResponseResult mallResult = mallServiceFeign.adminAlbumRecord(record, authorization);
        if (mallResult != null && mallResult.getCode() != 1 && mallResult.getCode() != 200) {
            int code = mallResult.getCode() > 0 ? mallResult.getCode() : 500;
            String msg = mallResult.getMessage() != null ? mallResult.getMessage() : "素材入库失败";
            return ResponseResult.fail(code, msg);
        }
        return mallResult;
    }

    private String resolveUploadUrl(String type, MultipartFile file) throws Exception {
        if ("oss".equalsIgnoreCase(mallUploadMode)) {
            ResponseResult ossResult = ossServiceFeign.uploadFile(file);
            if (ossResult == null || (ossResult.getCode() != 1 && ossResult.getCode() != 200)) {
                String msg = ossResult != null && ossResult.getMessage() != null
                        ? ossResult.getMessage() : "OSS 上传失败";
                throw new IllegalStateException(msg);
            }
            String url = resolveOssUrl(ossResult.getData());
            if (url == null || url.isEmpty()) {
                throw new IllegalStateException("OSS 未返回文件地址");
            }
            return url;
        }
        return localFileUploadService.upload(file);
    }

    private static String resolveOssUrl(Object data) {
        if (data == null) {
            return null;
        }
        if (data instanceof String s) {
            return s;
        }
        if (data instanceof Map<?, ?> map) {
            Object url = map.get("url");
            if (url == null) {
                url = map.get("uri");
            }
            return url != null ? url.toString() : null;
        }
        return data.toString();
    }

    /** ================= dcenter 商家管理后台 - 文件管理 ================= */

    @PostMapping("/file/cate/list")
    @Operation(summary = "文件分类列表")
    public ResponseResult getFileCategoryList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getFileCategoryList(params);
    }

    @PostMapping("/file/cate/save")
    @Operation(summary = "保存文件分类")
    public ResponseResult saveFileCategory(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.saveFileCategory(params);
    }

    @PostMapping("/file/cate/delete")
    @Operation(summary = "删除文件分类")
    public ResponseResult deleteFileCategory(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.deleteFileCategory(params);
    }

    @PostMapping("/file/list")
    @Operation(summary = "文件列表")
    public ResponseResult getFileList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getFileList(params);
    }

    @PostMapping("/file/upload")
    @Operation(summary = "上传文件")
    public ResponseResult uploadFile(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.uploadFile(params);
    }

    // /file/delete、/file/move、/file/rename 已由上方 adminAlbum* 映射（bcenter 素材库），避免与 dcenter 重复注册

    /** ================= dcenter 商家管理后台 - 系统公告 ================= */

    @PostMapping("/notice/announcement/list")
    @Operation(summary = "公告列表")
    public ResponseResult getAnnouncementList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getAnnouncementList(params);
    }

    @PostMapping("/notice/announcement/latest")
    @Operation(summary = "最新公告")
    public ResponseResult getLatestAnnouncement(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getLatestAnnouncement(params);
    }

    @PostMapping("/notice/announcement/detail")
    @Operation(summary = "公告详情")
    public ResponseResult getAnnouncementDetail(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getAnnouncementDetail(params);
    }

    @PostMapping("/notice/announcement/save")
    @Operation(summary = "保存公告（新增或编辑）")
    public ResponseResult saveAnnouncement(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.saveAnnouncement(params);
    }

    @PostMapping("/notice/announcement/delete")
    @Operation(summary = "删除公告")
    public ResponseResult deleteAnnouncement(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.deleteAnnouncement(params);
    }

    /** ================= dcenter 商家管理后台 - 客服配置 ================= */

    @PostMapping("/service/config/get")
    @Operation(summary = "获取客服配置")
    public ResponseResult getServiceConfig(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getServiceConfig(params);
    }

    @PostMapping("/service/config/save")
    @Operation(summary = "保存客服配置")
    public ResponseResult saveServiceConfig(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.saveServiceConfig(params);
    }

    /** ================= dcenter 商家管理后台 - 消息通知设置 ================= */

    @PostMapping("/message/list")
    @Operation(summary = "消息通知设置列表")
    public ResponseResult getMessageSettingList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getMessageSettingList(params);
    }

    @PostMapping("/message/detail")
    @Operation(summary = "消息通知设置详情")
    public ResponseResult getMessageSettingDetail(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getMessageSettingDetail(params);
    }

    @PostMapping("/message/save")
    @Operation(summary = "保存消息通知设置")
    public ResponseResult saveMessageSetting(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.saveMessageSetting(params);
    }

    @PostMapping("/message/delete")
    @Operation(summary = "删除消息通知设置")
    public ResponseResult deleteMessageSetting(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.deleteMessageSetting(params);
    }

    /** ================= dcenter 商家管理后台 - 集采业务 ================= */

    @PostMapping("/gather/list")
    @Operation(summary = "集采列表")
    public ResponseResult getGatherList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getGatherList(params);
    }

    @PostMapping("/gather/detail")
    @Operation(summary = "集采详情")
    public ResponseResult getGatherDetail(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getGatherDetail(params);
    }

    @PostMapping("/gather/save")
    @Operation(summary = "保存集采配置")
    public ResponseResult saveGather(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.saveGather(params);
    }

    @PostMapping("/gather/delete")
    @Operation(summary = "删除集采配置")
    public ResponseResult deleteGather(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.deleteGather(params);
    }

    /** ================= setting/dict_type 字典类型 ================= */

    @GetMapping("/setting/dict_type/all")
    @Operation(summary = "字典类型全部")
    public ResponseResult allDictType() {
        return mallServiceFeign.allDictType();
    }

    @GetMapping("/setting/dict_type/lists")
    @Operation(summary = "字典类型列表（分页）")
    public ResponseResult listsDictType(@RequestParam Map<String, String> params) {
        return mallServiceFeign.listsDictType(params);
    }

    @GetMapping("/setting/dict_type/detail")
    @Operation(summary = "字典类型详情")
    public ResponseResult detailDictType(@RequestParam("id") Integer id) {
        return mallServiceFeign.detailDictType(id);
    }

    @PostMapping("/setting/dict_type/add")
    @Operation(summary = "字典类型新增")
    public ResponseResult addDictType(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.addDictType(body);
    }

    @PostMapping("/setting/dict_type/edit")
    @Operation(summary = "字典类型编辑")
    public ResponseResult editDictType(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.editDictType(body);
    }

    @PostMapping("/setting/dict_type/delete")
    @Operation(summary = "字典类型删除")
    public ResponseResult delDictType(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.delDictType(body);
    }

    /** ================= setting/dict_data 字典数据 ================= */

    @GetMapping("/setting/dict_data/all")
    @Operation(summary = "字典数据全部（按 typeId 过滤）")
    public ResponseResult allDictData(@RequestParam Map<String, String> params) {
        return mallServiceFeign.allDictData(params);
    }

    @GetMapping("/setting/dict_data/lists")
    @Operation(summary = "字典数据列表（分页）")
    public ResponseResult listsDictData(@RequestParam Map<String, String> params) {
        return mallServiceFeign.listsDictData(params);
    }

    @GetMapping("/setting/dict_data/detail")
    @Operation(summary = "字典数据详情")
    public ResponseResult detailDictData(@RequestParam("id") Integer id) {
        return mallServiceFeign.detailDictData(id);
    }

    @PostMapping("/setting/dict_data/add")
    @Operation(summary = "字典数据新增")
    public ResponseResult addDictData(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.addDictData(body);
    }

    @PostMapping("/setting/dict_data/edit")
    @Operation(summary = "字典数据编辑")
    public ResponseResult editDictData(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.editDictData(body);
    }

    @PostMapping("/setting/dict_data/delete")
    @Operation(summary = "字典数据删除")
    public ResponseResult delDictData(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.delDictData(body);
    }

    /** ================= setting/pay_config 支付配置 ================= */

    @GetMapping("/setting/pay_config/lists")
    @Operation(summary = "支付配置列表")
    public ResponseResult listsPayConfig() {
        return mallServiceFeign.listsPayConfig();
    }

    @GetMapping("/setting/pay_config/detail")
    @Operation(summary = "支付配置详情")
    public ResponseResult detailPayConfig(@RequestParam("id") Integer id) {
        return mallServiceFeign.detailPayConfig(id);
    }

    @PostMapping("/setting/pay_config/edit")
    @Operation(summary = "支付配置编辑")
    public ResponseResult editPayConfig(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.editPayConfig(body);
    }

    /** ================= setting/pay_way 支付方式（B1-4）================= */

    @GetMapping("/setting/pay_way/lists")
    @Operation(summary = "支付方式列表（按场景分组）")
    public ResponseResult listsPayWay() {
        return mallServiceFeign.listsPayWay();
    }

    @PostMapping("/setting/pay_way/edit")
    @Operation(summary = "支付方式批量编辑")
    public ResponseResult editPayWay(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.editPayWay(body);
    }

    /** ================= setting/storage 存储配置（B1-5）================= */

    @GetMapping("/setting/storage/lists")
    @Operation(summary = "存储引擎列表")
    public ResponseResult listsStorage() {
        return mallServiceFeign.listsStorage();
    }

    @GetMapping("/setting/storage/detail")
    @Operation(summary = "存储引擎详情")
    public ResponseResult detailStorage(@RequestParam("engine") String engine) {
        return mallServiceFeign.detailStorage(engine);
    }

    @PostMapping("/setting/storage/setup")
    @Operation(summary = "存储引擎编辑")
    public ResponseResult setupStorage(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.setupStorage(body);
    }

    @PostMapping("/setting/storage/change")
    @Operation(summary = "存储引擎切换")
    public ResponseResult changeStorage(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.changeStorage(body);
    }

    /** ================= setting/sms 短信配置（B1-6）================= */

    @GetMapping("/setting/sms/lists")
    @Operation(summary = "短信引擎列表")
    public ResponseResult listsSms() {
        return mallServiceFeign.listsSms();
    }

    @GetMapping("/setting/sms/detail")
    @Operation(summary = "短信引擎详情")
    public ResponseResult detailSms(@RequestParam("type") String type) {
        return mallServiceFeign.detailSms(type);
    }

    @PostMapping("/setting/sms/edit")
    @Operation(summary = "短信引擎编辑")
    public ResponseResult editSms(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.editSms(body);
    }

    /** ================= setting/notice 通知设置（B1-7）================= */

    @GetMapping("/setting/notice/lists")
    @Operation(summary = "通知设置列表")
    public ResponseResult listsNotice(@RequestParam("recipient") Integer recipient) {
        return mallServiceFeign.listsNotice(recipient);
    }

    @GetMapping("/setting/notice/detail")
    @Operation(summary = "通知设置详情")
    public ResponseResult detailNotice(@RequestParam("id") Integer id) {
        return mallServiceFeign.detailNotice(id);
    }

    @PostMapping("/setting/notice/edit")
    @Operation(summary = "通知设置保存")
    public ResponseResult editNotice(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.editNotice(body);
    }

    /** ================= setting/order 订单设置（B1-8）================= */

    @GetMapping("/setting/order/transaction")
    @Operation(summary = "交易设置详情")
    public ResponseResult getOrderTransaction() {
        return mallServiceFeign.getOrderTransaction();
    }

    @PostMapping("/setting/order/transaction/save")
    @Operation(summary = "交易设置保存")
    public ResponseResult saveOrderTransaction(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.saveOrderTransaction(body);
    }

    @GetMapping("/setting/order/settlement")
    @Operation(summary = "结算设置详情")
    public ResponseResult getOrderSettlement() {
        return mallServiceFeign.getOrderSettlement();
    }

    @PostMapping("/setting/order/settlement/save")
    @Operation(summary = "结算设置保存")
    public ResponseResult saveOrderSettlement(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.saveOrderSettlement(body);
    }

    /** ================= setting/search 热门搜索（B1-9）================= */

    @GetMapping("/setting/search/lists")
    @Operation(summary = "热搜详情")
    public ResponseResult listsSearch() {
        return mallServiceFeign.listsSearch();
    }

    @PostMapping("/setting/search/edit")
    @Operation(summary = "热搜编辑")
    public ResponseResult editSearch(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.editSearch(body);
    }

    /** ================= setting/user 用户设置（B1-10）================= */

    @GetMapping("/setting/user/lists")
    @Operation(summary = "用户基础设置详情")
    public ResponseResult listsUser() {
        return mallServiceFeign.listsUser();
    }

    @PostMapping("/setting/user/edit")
    @Operation(summary = "用户基础设置保存")
    public ResponseResult editUser(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.editUser(body);
    }

    @GetMapping("/setting/user/login/lists")
    @Operation(summary = "登录注册设置详情")
    public ResponseResult listsUserLogin() {
        return mallServiceFeign.listsUserLogin();
    }

    @PostMapping("/setting/user/login/edit")
    @Operation(summary = "登录注册设置保存")
    public ResponseResult editUserLogin(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.editUserLogin(body);
    }

    /** ================= setting/website 网站设置（B1-11）================= */

    @GetMapping("/setting/website/lists")
    @Operation(summary = "网站基础详情")
    public ResponseResult listsWebsite() {
        return mallServiceFeign.listsWebsite();
    }

    @PostMapping("/setting/website/edit")
    @Operation(summary = "网站基础保存")
    public ResponseResult editWebsite(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.editWebsite(body);
    }

    @GetMapping("/setting/website/copyright/lists")
    @Operation(summary = "备案详情")
    public ResponseResult listsWebsiteCopyright() {
        return mallServiceFeign.listsWebsiteCopyright();
    }

    @PostMapping("/setting/website/copyright/edit")
    @Operation(summary = "备案保存")
    public ResponseResult editWebsiteCopyright(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.editWebsiteCopyright(body);
    }

    @GetMapping("/setting/website/agreement/lists")
    @Operation(summary = "政策详情")
    public ResponseResult listsWebsiteAgreement() {
        return mallServiceFeign.listsWebsiteAgreement();
    }

    @PostMapping("/setting/website/agreement/edit")
    @Operation(summary = "政策保存")
    public ResponseResult editWebsiteAgreement(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.editWebsiteAgreement(body);
    }

    @GetMapping("/setting/website/statistics/lists")
    @Operation(summary = "站点统计详情")
    public ResponseResult listsWebsiteStatistics() {
        return mallServiceFeign.listsWebsiteStatistics();
    }

    @PostMapping("/setting/website/statistics/edit")
    @Operation(summary = "站点统计保存")
    public ResponseResult editWebsiteStatistics(@RequestBody Map<String, Object> body) {
        return mallServiceFeign.editWebsiteStatistics(body);
    }

    /** ================= H5 首页装修 ================= */

    /**
     * H5 首页装修数据。新版后台装修数据源，转成 H5 认的老结构后返回。
     * 公开接口：H5 未登录也要能拿到首页装修。
     */
    @GetMapping("/front/index/legacyHome")
    @Operation(summary = "H5 首页装修数据")
    public ResponseResult frontLegacyHomeGet() {
        return mallServiceFeign.frontLegacyHomeGet();
    }

    @PostMapping("/front/index/legacyHome")
    @Operation(summary = "H5 首页装修数据")
    public ResponseResult frontLegacyHomePost() {
        return mallServiceFeign.frontLegacyHomePost();
    }
}
