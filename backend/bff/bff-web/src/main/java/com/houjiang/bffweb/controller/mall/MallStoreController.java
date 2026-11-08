package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商城店铺管理Controller
/**
 * 通过MallServiceFeign调用mall服务店铺相关接口
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/mall/store")
@RequiredArgsConstructor
public class MallStoreController {

    private final MallServiceFeign mallServiceFeign;

    /**
     * 获取店铺列表
/**
     *
     * @param params 查询参数：page, size, 筛选条件
     * @return ResponseResult 分页数据
     */
    @PostMapping("/list")
    public ResponseResult getStoreList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getPhysicalStoreList(params);
    }

    /** 在指定商家下新增门店。 */
    @PostMapping("/create")
    public ResponseResult createStore(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.createPhysicalStore(params);
    }

    /** 新增门店表单所需的商家选项。 */
    @PostMapping("/merchant-options")
    public ResponseResult merchantOptions() {
        return mallServiceFeign.getPhysicalStoreMerchantOptions();
    }

    /** 新增门店时搜索已有统一账号。 */
    @PostMapping("/account-options")
    public ResponseResult accountOptions(@RequestBody(required = false) Map<String, Object> params) {
        return mallServiceFeign.getPhysicalStoreAccountOptions(params == null ? Map.of() : params);
    }

    /** 更新门店营业、骑手配送或 POS 开关。 */
    @PostMapping("/toggle")
    public ResponseResult toggleStore(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.togglePhysicalStore(params);
    }

    /**
     * 获取店铺详情
/**
     *
     * @param params 包含id字段
     * @return ResponseResult 店铺信息
     */
    @PostMapping("/detail")
    public ResponseResult getStoreDetail(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return mallServiceFeign.getShopDetail(id);
    }

    /**
     * 获取店铺设置（复用店铺详情）
/**
     *
     * @param params 包含id字段
     * @return ResponseResult 店铺配置信息
     */
    @PostMapping("/setting")
    public ResponseResult getStoreSetting(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return mallServiceFeign.getShopDetail(id);
    }

    /**
     * 保存店铺设置（复用店铺更新）
/**
     *
     * @param params 店铺配置数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/setting/save")
    public ResponseResult saveSetting(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.updateShop(params);
    }

    /**
     * 获取店铺装修配置（复用店铺详情）
/**
     *
     * @param params 包含id字段
     * @return ResponseResult 装修配置
     */
    @PostMapping("/decoration")
    public ResponseResult getDecoration(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return mallServiceFeign.getShopDetail(id);
    }

    /**
     * 保存店铺装修配置（复用店铺更新）
/**
     *
     * @param params 装修配置数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/decoration/save")
    public ResponseResult saveDecoration(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.updateShop(params);
    }

    /**
     * 获取待审核店铺列表（商家入驻列表）
/**
     *
     * @param params 查询参数
     * @return ResponseResult 分页数据
     */
    @PostMapping("/audit/list")
    public ResponseResult getAuditList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getMerchantEntryList(params);
    }

    /**
     * 审核店铺（商家入驻审核）
/**
     *
     * @param params 审核参数：id, status, remark
     * @return ResponseResult 操作结果
     */
    @PostMapping("/audit")
    public ResponseResult auditStore(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.auditMerchantEntry(params);
    }

    /**
     * 店铺统计（通过Dashboard概览获取）
/**
     *
     * @param params 查询参数
     * @return ResponseResult 统计数据
     */
    @PostMapping("/statistics")
    public ResponseResult getStatistics(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getDashboardOverview();
    }

    // ================= 店铺等级 =================

    /**
     * 获取店铺等级列表
/**
     *
     * @return ResponseResult 等级列表
     */
    @PostMapping("/level/list")
    public ResponseResult getLevelList() {
        return mallServiceFeign.getShopLevelList();
    }

    /**
     * 创建店铺等级
/**
     *
     * @param params 等级数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/level/create")
    public ResponseResult createLevel(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.createShopLevel(params);
    }

    /**
     * 更新店铺等级
/**
     *
     * @param params 等级数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/level/update")
    public ResponseResult updateLevel(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.updateShopLevel(params);
    }

    // ================= 商家入驻 =================

    /**
     * 获取商家入驻申请列表
/**
     *
     * @param params 查询参数
     * @return ResponseResult 分页数据
     */
    @PostMapping("/merchant-entry/list")
    public ResponseResult getMerchantEntryList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getMerchantEntryList(params);
    }

    // ================= 询价功能 =================

    /**
     * 获取询价列表
/**
     *
     * @param params 查询参数
     * @return ResponseResult 分页数据
     */
    @PostMapping("/inquiry/list")
    public ResponseResult getInquiryList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getInquiryList(params);
    }

    /**
     * 询价报价
/**
     *
     * @param params 报价数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/inquiry/quote")
    public ResponseResult quoteInquiry(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.quoteInquiry(params);
    }

    // ================= 货到付款 =================

    /**
     * 获取货到付款列表
/**
     *
     * @param params 查询参数
     * @return ResponseResult 分页数据
     */
    @PostMapping("/cash-on-delivery/list")
    public ResponseResult getCashOnDeliveryList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getCashOnDeliveryList(params);
    }

    // ================= 发票管理 =================

    /**
     * 获取发票列表
/**
     *
     * @param params 查询参数
     * @return ResponseResult 分页数据
     */
    @PostMapping("/invoice/list")
    public ResponseResult getInvoiceList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getInvoiceList(params);
    }

    /**
     * 开具发票
/**
     *
     * @param params 发票数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/invoice/issue")
    public ResponseResult issueInvoice(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.issueInvoice(params);
    }

    // ================= 质保金 =================

    /**
     * 获取质保金列表
/**
     *
     * @param params 查询参数
     * @return ResponseResult 分页数据
     */
    @PostMapping("/deposit/list")
    public ResponseResult getDepositList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getQualityDepositList(params);
    }

    /**
     * 质保金退款
/**
     *
     * @param params 退款参数
     * @return ResponseResult 操作结果
     */
    @PostMapping("/deposit/refund")
    public ResponseResult refundDeposit(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.refundQualityDeposit(params);
    }

    // ================= 经营类目 =================

    /**
     * 分页查询经营类目列表
/**
     *
     * @param params 查询参数：page, size, name, status
     * @return ResponseResult 分页数据
     */
    @PostMapping("/shop-category/list")
    public ResponseResult getShopCategoryList(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getShopCategoryList(params);
    }

    /**
     * 获取经营类目详情
/**
     *
     * @param params 包含id字段
     * @return ResponseResult 类目详情
     */
    @PostMapping("/shop-category/detail")
    public ResponseResult getShopCategoryDetail(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return mallServiceFeign.getShopCategoryDetail(id);
    }

    /**
     * 新增经营类目
/**
     *
     * @param params 类目数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/shop-category/create")
    public ResponseResult createShopCategory(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.createShopCategory(params);
    }

    /**
     * 编辑经营类目
/**
     *
     * @param params 类目数据（必须包含id）
     * @return ResponseResult 操作结果
     */
    @PostMapping("/shop-category/update")
    public ResponseResult updateShopCategory(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.updateShopCategory(params);
    }

    /**
     * 删除经营类目
/**
     *
     * @param params 包含id字段
     * @return ResponseResult 操作结果
     */
    @PostMapping("/shop-category/delete")
    public ResponseResult deleteShopCategory(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.deleteShopCategory(params);
    }

    /**
     * 更改经营类目状态
/**
     *
     * @param params 包含id和status字段
     * @return ResponseResult 操作结果
     */
    @PostMapping("/shop-category/status")
    public ResponseResult updateShopCategoryStatus(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.updateShopCategoryStatus(params);
    }

    /**
     * 获取启用的经营类目列表（下拉选择用）
/**
     *
     * @return ResponseResult 启用类目列表
     */
    @PostMapping("/shop-category/enabled")
    public ResponseResult getEnabledShopCategoryList() {
        return mallServiceFeign.getEnabledShopCategoryList();
    }

    // ================= 商家菜单权限 =================

    /**
     * 获取商家菜单树形列表
/**
     *
     * @return ResponseResult 树形菜单列表
     */
    @PostMapping("/shop-menu/list")
    public ResponseResult getShopMenuList() {
        return mallServiceFeign.getShopMenuList();
    }

    /**
     * 获取菜单详情
/**
     *
     * @param params 包含id字段
     * @return ResponseResult 菜单详情
     */
    @PostMapping("/shop-menu/detail")
    public ResponseResult getShopMenuDetail(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return mallServiceFeign.getShopMenuDetail(id);
    }

    /**
     * 新增商家菜单
/**
     *
     * @param params 菜单数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/shop-menu/create")
    public ResponseResult createShopMenu(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.createShopMenu(params);
    }

    /**
     * 编辑商家菜单
/**
     *
     * @param params 菜单数据（必须包含id）
     * @return ResponseResult 操作结果
     */
    @PostMapping("/shop-menu/update")
    public ResponseResult updateShopMenu(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.updateShopMenu(params);
    }

    /**
     * 删除商家菜单
/**
     *
     * @param params 包含id字段
     * @return ResponseResult 操作结果
     */
    @PostMapping("/shop-menu/delete")
    public ResponseResult deleteShopMenu(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.deleteShopMenu(params);
    }

    /**
     * 更改商家菜单状态
/**
     *
     * @param params 包含id和status字段
     * @return ResponseResult 操作结果
     */
    @PostMapping("/shop-menu/status")
    public ResponseResult updateShopMenuStatus(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.updateShopMenuStatus(params);
    }

    /**
     * 获取启用菜单列表（父级选择用）
/**
     *
     * @return ResponseResult 启用菜单列表
     */
    @PostMapping("/shop-menu/enabled")
    public ResponseResult getEnabledShopMenuList() {
        return mallServiceFeign.getEnabledShopMenuList();
    }
}
