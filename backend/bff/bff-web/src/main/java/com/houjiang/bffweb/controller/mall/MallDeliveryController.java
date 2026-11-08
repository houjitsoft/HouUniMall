package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.BaseServiceFeign;
import com.houjiang.bffweb.feign.MerchantServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 商城配送管理Controller（BFF层）
/**
 * B1-12 delivery 子模块：配送方式 + 运费模板 + 快递公司
 * 
 * 路由策略：
 * - 配送方式 4 接口 → merchant 服务（mall_delivery_type 配置）
 * - 运费模板 5 接口 → merchant 服务（mall.expressTpl）
 * - 快递公司 5 接口 → base 服务（express CRUD）
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/mall/delivery")
public class MallDeliveryController {

    @Autowired
    private MerchantServiceFeign merchantServiceFeign;

    @Autowired
    private BaseServiceFeign baseServiceFeign;

    // ================= 配送方式设置（merchant 通用配置） =================
    // 注：merchant 服务返回 AjaxResult（code=200），前端 request 工具 default case 直接解构 data
    //     不能用 ResponseResult.success() 包装，否则产生双重包装

    /**
     * 获取配送方式配置
/**
     * 对应前端 getDelivery() → 原 GET /mall.DeliveryType/get_config
     */
    @GetMapping("/type/config")
    public Object getDeliveryConfig(@RequestParam(required = false) Map<String, Object> params) {
        try {
            return merchantServiceFeign.forwardGet("mall.DeliveryType", "get_config", params);
        } catch (Exception e) {
            log.error("获取配送方式配置失败", e);
            return ResponseResult.fail("获取配送方式配置失败: " + e.getMessage());
        }
    }

    /**
     * 设置配送方式配置
/**
     * 对应前端 apiDelivery(params) → 原 POST /mall.DeliveryType/set_config
     */
    @PostMapping("/type/config")
    public Object setDeliveryConfig(@RequestBody Map<String, Object> params) {
        try {
            return merchantServiceFeign.forwardPost("mall.DeliveryType", "set_config", params);
        } catch (Exception e) {
            log.error("设置配送方式配置失败", e);
            return ResponseResult.fail("设置配送方式配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取物流追踪配置
/**
     * 对应前端 getLogistics() → 原 GET /mall.DeliveryType/get_logistics_config
     */
    @GetMapping("/logistics/config")
    public Object getLogisticsConfig(@RequestParam(required = false) Map<String, Object> params) {
        try {
            return merchantServiceFeign.forwardGet("mall.DeliveryType", "get_logistics_config", params);
        } catch (Exception e) {
            log.error("获取物流追踪配置失败", e);
            return ResponseResult.fail("获取物流追踪配置失败: " + e.getMessage());
        }
    }

    /**
     * 设置物流追踪配置
/**
     * 对应前端 apiLogistics(params) → 原 POST /mall.DeliveryType/set_logistics_config
     */
    @PostMapping("/logistics/config")
    public Object setLogisticsConfig(@RequestBody Map<String, Object> params) {
        try {
            return merchantServiceFeign.forwardPost("mall.DeliveryType", "set_logistics_config", params);
        } catch (Exception e) {
            log.error("设置物流追踪配置失败", e);
            return ResponseResult.fail("设置物流追踪配置失败: " + e.getMessage());
        }
    }

    // ================= 运费模板（merchant mall.expressTpl） =================

    /**
     * 运费模板列表
/**
     * 对应前端 getFreightLists() → 原 GET /mall.ExpressTpl/lists
     */
    @GetMapping("/freight/lists")
    public Object getFreightLists(@RequestParam(required = false) Map<String, Object> params) {
        try {
            return merchantServiceFeign.forwardGet("mall.expressTpl", "lists", params);
        } catch (Exception e) {
            log.error("获取运费模板列表失败", e);
            return ResponseResult.fail("获取运费模板列表失败: " + e.getMessage());
        }
    }

    /**
     * 运费模板详情
/**
     * 对应前端 getFreightDetail({id}) → 原 GET /mall.ExpressTpl/detail
     */
    @GetMapping("/freight/detail")
    public Object getFreightDetail(@RequestParam Map<String, Object> params) {
        try {
            return merchantServiceFeign.forwardGet("mall.expressTpl", "detail", params);
        } catch (Exception e) {
            log.error("获取运费模板详情失败", e);
            return ResponseResult.fail("获取运费模板详情失败: " + e.getMessage());
        }
    }

    /**
     * 运费模板新增
/**
     * 对应前端 apiFreightAdd(params) → 原 POST /mall.ExpressTpl/add
     */
    @PostMapping("/freight/add")
    public Object addFreight(@RequestBody Map<String, Object> params) {
        try {
            return merchantServiceFeign.forwardPost("mall.expressTpl", "add", params);
        } catch (Exception e) {
            log.error("新增运费模板失败", e);
            return ResponseResult.fail("新增运费模板失败: " + e.getMessage());
        }
    }

    /**
     * 运费模板编辑
/**
     * 对应前端 apiFreightEdit(params) → 原 POST /mall.ExpressTpl/edit
     */
    @PostMapping("/freight/edit")
    public Object editFreight(@RequestBody Map<String, Object> params) {
        try {
            return merchantServiceFeign.forwardPost("mall.expressTpl", "edit", params);
        } catch (Exception e) {
            log.error("编辑运费模板失败", e);
            return ResponseResult.fail("编辑运费模板失败: " + e.getMessage());
        }
    }

    /**
     * 运费模板删除
/**
     * 对应前端 apiFreightDel({id}) → 原 POST /mall.ExpressTpl/delete
     */
    @PostMapping("/freight/delete")
    public Object deleteFreight(@RequestBody Map<String, Object> params) {
        try {
            return merchantServiceFeign.forwardPost("mall.expressTpl", "delete", params);
        } catch (Exception e) {
            log.error("删除运费模板失败", e);
            return ResponseResult.fail("删除运费模板失败: " + e.getMessage());
        }
    }

    // ================= 快递公司（base 服务 CRUD） =================

    /**
     * 快递公司列表
/**
     * 对应前端 getExpressLists({name}) → 原 GET /mall.ExpressCompany/lists
     */
    @GetMapping("/express/lists")
    public ResponseResult getExpressLists(@RequestParam(required = false) Map<String, Object> params) {
        try {
            Map<String, Object> query = params != null ? new HashMap<>(params) : new HashMap<>();
            return baseServiceFeign.queryExpressCompanyList(query);
        } catch (Exception e) {
            log.error("获取快递公司列表失败", e);
            return ResponseResult.fail("获取快递公司列表失败: " + e.getMessage());
        }
    }

    /**
     * 快递公司详情
/**
     * 对应前端 getExpressDetail({id}) → 原 GET /mall.ExpressCompany/detail
     */
    @GetMapping("/express/detail")
    public ResponseResult getExpressDetail(@RequestParam("id") Integer id) {
        try {
            return baseServiceFeign.getExpressInfoById(id);
        } catch (Exception e) {
            log.error("获取快递公司详情失败", e);
            return ResponseResult.fail("获取快递公司详情失败: " + e.getMessage());
        }
    }

    /**
     * 快递公司新增
/**
     * 对应前端 apiExpressAdd(params) → 原 POST /mall.ExpressCompany/add
     */
    @PostMapping("/express/add")
    public ResponseResult addExpress(@RequestBody Map<String, Object> params) {
        try {
            return baseServiceFeign.addExpressCompany(params);
        } catch (Exception e) {
            log.error("新增快递公司失败", e);
            return ResponseResult.fail("新增快递公司失败: " + e.getMessage());
        }
    }

    /**
     * 快递公司编辑
/**
     * 对应前端 apiExpressEdit(params) → 原 POST /mall.ExpressCompany/edit
     */
    @PostMapping("/express/edit")
    public ResponseResult editExpress(@RequestBody Map<String, Object> params) {
        try {
            return baseServiceFeign.updateExpressCompany(params);
        } catch (Exception e) {
            log.error("编辑快递公司失败", e);
            return ResponseResult.fail("编辑快递公司失败: " + e.getMessage());
        }
    }

    /**
     * 快递公司删除
/**
     * 对应前端 apiExpressDel({id}) → 原 POST /mall.ExpressCompany/delete
     */
    @PostMapping("/express/delete")
    public ResponseResult deleteExpress(@RequestBody Map<String, Object> params) {
        try {
            return baseServiceFeign.deleteExpressCompany(params);
        } catch (Exception e) {
            log.error("删除快递公司失败", e);
            return ResponseResult.fail("删除快递公司失败: " + e.getMessage());
        }
    }
}
