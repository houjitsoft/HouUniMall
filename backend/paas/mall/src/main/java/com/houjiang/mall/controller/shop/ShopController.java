package com.houjiang.mall.controller.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.shop.*;
import com.houjiang.mall.service.shop.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 店铺管理Controller
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final ShopLevelService shopLevelService;
    private final MerchantEntryService merchantEntryService;
    private final InquiryService inquiryService;
    private final CashOnDeliveryService cashOnDeliveryService;
    private final InvoiceService invoiceService;
    private final QualityDepositService qualityDepositService;
    private final ShopCategoryService shopCategoryService;
    private final ShopMenuService shopMenuService;

    /** ============== 店铺管理 ============== */
    
    @PostMapping("/list")
    public ResponseResult<IPage<Shop>> shopList(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(shopService.pageList(params));
    }

    @GetMapping("/detail/{id}")
    public ResponseResult<Shop> shopDetail(@PathVariable Long id) {
        return ResponseResult.success(shopService.getById(id));
    }

    @PostMapping("/create")
    public ResponseResult<Boolean> createShop(@RequestBody Shop shop) {
        return ResponseResult.success(shopService.save(shop));
    }

    @PostMapping("/update")
    public ResponseResult<Boolean> updateShop(@RequestBody Shop shop) {
        return ResponseResult.success(shopService.updateById(shop));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseResult<Boolean> deleteShop(@PathVariable Long id) {
        return ResponseResult.success(shopService.removeById(id));
    }

    /**
     * 更新店铺状态（启用/禁用/冻结等）
     *
     * @param params 包含id(Long)和status(Integer)字段
     * @return ResponseResult<Boolean>
     */
    @PostMapping("/update-status")
    public ResponseResult<Boolean> updateShopStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        return ResponseResult.success(shopService.updateStatus(id, status));
    }

    /**
     * 获取店铺统计概览
     *
     * @return ResponseResult 含total/normal/frozen/closed/pending字段
     */
    @PostMapping("/overview")
    public ResponseResult<Map<String, Object>> shopOverview() {
        return ResponseResult.success(shopService.overview());
    }

    /** ============== 店铺等级 ============== */
    
    @PostMapping("/level/list")
    public ResponseResult<List<ShopLevel>> levelList() {
        return ResponseResult.success(shopLevelService.list());
    }

    @PostMapping("/level/create")
    public ResponseResult<Boolean> createLevel(@RequestBody ShopLevel level) {
        return ResponseResult.success(shopLevelService.save(level));
    }

    @PostMapping("/level/update")
    public ResponseResult<Boolean> updateLevel(@RequestBody ShopLevel level) {
        return ResponseResult.success(shopLevelService.updateById(level));
    }

    /** ============== 商家入驻 ============== */
    
    @PostMapping("/entry/list")
    public ResponseResult<IPage<MerchantEntry>> entryList(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(merchantEntryService.pageList(params));
    }

    @PostMapping("/entry/audit")
    public ResponseResult<Boolean> auditEntry(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        String remark = (String) params.get("remark");
        return ResponseResult.success(merchantEntryService.audit(id, status, remark));
    }

    /** ============== 询价功能 ============== */
    
    @PostMapping("/inquiry/list")
    public ResponseResult<IPage<Inquiry>> inquiryList(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(inquiryService.pageList(params));
    }

    @PostMapping("/inquiry/quote")
    public ResponseResult<Boolean> quoteInquiry(@RequestBody Inquiry inquiry) {
        return ResponseResult.success(inquiryService.quote(inquiry));
    }

    /** ============== 货到付款 ============== */
    
    @PostMapping("/cod/list")
    public ResponseResult<IPage<CashOnDelivery>> codList(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(cashOnDeliveryService.pageList(params));
    }

    /** ============== 发票管理 ============== */
    
    @PostMapping("/invoice/list")
    public ResponseResult<IPage<Invoice>> invoiceList(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(invoiceService.pageList(params));
    }

    @PostMapping("/invoice/issue")
    public ResponseResult<Boolean> issueInvoice(@RequestBody Invoice invoice) {
        return ResponseResult.success(invoiceService.issue(invoice));
    }

    /** ============== 质保金 ============== */
    
    @PostMapping("/deposit/list")
    public ResponseResult<IPage<QualityDeposit>> depositList(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(qualityDepositService.pageList(params));
    }

    @PostMapping("/deposit/refund")
    public ResponseResult<Boolean> refundDeposit(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return ResponseResult.success(qualityDepositService.refund(id));
    }

    // ============== 经营类目 ==============

    /**
     * 分页查询经营类目列表
     *
     * @param params 查询参数：page, size, name, status
     * @return 分页结果
     */
    @PostMapping("/category/list")
    public ResponseResult<IPage<ShopCategory>> categoryList(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(shopCategoryService.pageList(params));
    }

    /**
     * 获取经营类目详情
     *
     * @param id 类目ID
     * @return 类目详情
     */
    @GetMapping("/category/detail/{id}")
    public ResponseResult<ShopCategory> categoryDetail(@PathVariable Long id) {
        return ResponseResult.success(shopCategoryService.getById(id));
    }

    /**
     * 新增经营类目
     *
     * @param category 类目数据
     * @return 是否成功
     */
    @PostMapping("/category/create")
    public ResponseResult<Boolean> createCategory(@RequestBody ShopCategory category) {
        return ResponseResult.success(shopCategoryService.save(category));
    }

    /**
     * 编辑经营类目
     *
     * @param category 类目数据（必须包含id）
     * @return 是否成功
     */
    @PostMapping("/category/update")
    public ResponseResult<Boolean> updateCategory(@RequestBody ShopCategory category) {
        return ResponseResult.success(shopCategoryService.updateById(category));
    }

    /**
     * 删除经营类目（逻辑删除）
     *
     * @param params 包含id字段
     * @return 是否成功
     */
    @PostMapping("/category/delete")
    public ResponseResult<Boolean> deleteCategory(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return ResponseResult.success(shopCategoryService.removeById(id));
    }

    /**
     * 更改经营类目状态
     *
     * @param params 包含id和status字段
     * @return 是否成功
     */
    @PostMapping("/category/status")
    public ResponseResult<Boolean> updateCategoryStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        return ResponseResult.success(shopCategoryService.updateStatus(id, status));
    }

    /**
     * 获取启用的经营类目列表（下拉选择用）
     *
     * @return 启用类目列表
     */
    @PostMapping("/category/enabled")
    public ResponseResult<List<ShopCategory>> enabledCategoryList() {
        return ResponseResult.success(shopCategoryService.listEnabled());
    }

    // ============== 商家菜单权限 ==============

    /**
     * 获取商家菜单树形列表（全量）
     *
     * @return 树形菜单列表
     */
    @PostMapping("/menu/list")
    public ResponseResult<List<ShopMenu>> menuList() {
        return ResponseResult.success(shopMenuService.treeList());
    }

    /**
     * 获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单详情
     */
    @GetMapping("/menu/detail/{id}")
    public ResponseResult<ShopMenu> menuDetail(@PathVariable Long id) {
        return ResponseResult.success(shopMenuService.getById(id));
    }

    /**
     * 新增商家菜单
     *
     * @param menu 菜单数据
     * @return 是否成功
     */
    @PostMapping("/menu/create")
    public ResponseResult<Boolean> createMenu(@RequestBody ShopMenu menu) {
        return ResponseResult.success(shopMenuService.save(menu));
    }

    /**
     * 编辑商家菜单
     *
     * @param menu 菜单数据（必须包含id）
     * @return 是否成功
     */
    @PostMapping("/menu/update")
    public ResponseResult<Boolean> updateMenu(@RequestBody ShopMenu menu) {
        return ResponseResult.success(shopMenuService.updateById(menu));
    }

    /**
     * 删除商家菜单（逻辑删除）
     *
     * @param params 包含id字段
     * @return 是否成功
     */
    @PostMapping("/menu/delete")
    public ResponseResult<Boolean> deleteMenu(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return ResponseResult.success(shopMenuService.removeById(id));
    }

    /**
     * 更改商家菜单状态
     *
     * @param params 包含id和status字段
     * @return 是否成功
     */
    @PostMapping("/menu/status")
    public ResponseResult<Boolean> updateMenuStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        return ResponseResult.success(shopMenuService.updateStatus(id, status));
    }

    /**
     * 获取启用的菜单列表（用于父级选择下拉）
     *
     * @return 启用菜单列表
     */
    @PostMapping("/menu/enabled")
    public ResponseResult<List<ShopMenu>> enabledMenuList() {
        return ResponseResult.success(shopMenuService.treeListEnabled());
    }
}
