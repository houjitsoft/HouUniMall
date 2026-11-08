package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.feign.OrderServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商城管理端订单Controller
/**
 * 列表走 mall 代理（MALL_ADMIN_TOKEN）；详情等仍直连 order（逐步迁移）。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/mall/order/manage")
@RequiredArgsConstructor
public class MallOrderManageController {

    private final MallServiceFeign mallServiceFeign;
    private final OrderServiceFeign orderServiceFeign;

    // ========== 订单列表/详情/统计 ==========

    /**
     * 管理端查询全部订单列表
/**
     * 支持按类型、状态等筛选
     *
     * @param param 查询参数：page, size, status, orderType, keyword等
     * @return ResponseResult
     */
    @PostMapping("/list")
    public ResponseResult orderList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.mallAdminOrderManageList(param);
    }

    /**
     * 管理端获取订单详情
/**
     *
     * @param param 包含orderId字段
     * @return ResponseResult
     */
    @PostMapping("/detail")
    public ResponseResult orderDetail(@RequestBody Map<String, Object> param) {
        String orderId = param.get("orderId") != null ? param.get("orderId").toString() : null;
        if (orderId == null) {
            return ResponseResult.error("订单ID不能为空");
        }
        // 与 /list 一致走 mall 管理端代理（mall 侧做 order 详情契约适配）。
        // 旧写法转发 orderServiceFeign.getOrderDetail —— order 没有 /order/detail 端点，
        // 该路径也不在 JWT 白名单，恒返 30803（2026-10-31 后台订单详情打通时修正）。
        return mallServiceFeign.mallAdminOrderManageDetail(param);
    }

    /**
     * 管理端获取订单统计数据
/**
     *
     * @param param 查询参数
     * @return ResponseResult
     */
    @PostMapping("/statistics")
    public ResponseResult orderStatistics(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.getOrderStatistics(param);
    }

    /**
     * 管理端更新订单状态
/**
     *
     * @param param 包含orderId和status字段
     * @return ResponseResult
     */
    @PostMapping("/update-status")
    public ResponseResult updateOrderStatus(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.updateOrderStatus(param);
    }

    /**
     * 管理端删除订单
/**
     *
     * @param param 包含orderId字段
     * @return ResponseResult
     */
    @PostMapping("/delete")
    public ResponseResult deleteOrder(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.deleteOrder(param);
    }

    // ========== 投诉管理 ==========

    /**
     * 查询投诉列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult
     */
    @PostMapping("/complaint/list")
    public ResponseResult complaintList(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.queryComplaintsList(param);
    }

    /**
     * 查询投诉详情
/**
     *
     * @param param 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/complaint/detail")
    public ResponseResult complaintDetail(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.queryComplaintsDetail(param);
    }

    /**
     * 处理投诉
/**
     *
     * @param param 包含id和处理结果字段
     * @return ResponseResult
     */
    @PostMapping("/complaint/handle")
    public ResponseResult complaintHandle(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.handleComplaints(param);
    }

    // ========== 退款管理 ==========

    /**
     * 查询退款列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult
     */
    @PostMapping("/refund/list")
    public ResponseResult refundList(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.queryRefundList(param);
    }

    /**
     * 处理退款
/**
     *
     * @param param 包含退款处理参数
     * @return ResponseResult
     */
    @PostMapping("/refund/process")
    public ResponseResult refundProcess(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.processRefund(param);
    }

    // ========== 退货管理 ==========

    /**
     * 查询平台退货列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult
     */
    @PostMapping("/return/list")
    public ResponseResult returnList(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.queryPlatformReturnList(param);
    }

    /**
     * 查询平台退货详情
/**
     *
     * @param param 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/return/detail")
    public ResponseResult returnDetail(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.queryPlatformReturnDetail(param);
    }

    /**
     * 平台处理退货
/**
     *
     * @param param 包含处理参数
     * @return ResponseResult
     */
    @PostMapping("/return/process")
    public ResponseResult returnProcess(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.processPlatformReturn(param);
    }

    // ========== 换货管理 ==========

    /**
     * 查询换货列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult
     */
    @PostMapping("/exchange/list")
    public ResponseResult exchangeList(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.queryExchangeList(param);
    }

    /**
     * 处理换货
/**
     *
     * @param param 包含处理参数
     * @return ResponseResult
     */
    @PostMapping("/exchange/process")
    public ResponseResult exchangeProcess(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.processExchange(param);
    }

    // ========== 结算管理 ==========

    /**
     * 查询结算列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult
     */
    @PostMapping("/settlement/list")
    public ResponseResult settlementList(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.querySettlementList(param);
    }

    /**
     * 查询结算详情
/**
     *
     * @param param 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/settlement/detail")
    public ResponseResult settlementDetail(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.querySettlementDetail(param);
    }

    /**
     * 执行结算
/**
     *
     * @param param 包含结算参数
     * @return ResponseResult
     */
    @PostMapping("/settlement/process")
    public ResponseResult settlementProcess(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.processSettlement(param);
    }

    // ========== 平台运营补齐：发货/物流/备注/售后/统计 ==========

    /**
     * 平台发货（代理发货场景）
/**
     *
     * @param param orderId / expressNo / expressCompany 等
     * @return ResponseResult
     */
    @PostMapping("/ship")
    public ResponseResult ship(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.shipOrder(param);
    }

    /**
     * 修改快递单号（重新发货）
/**
     *
     * @param param orderId / 新 expressNo 等
     * @return ResponseResult
     */
    @PostMapping("/ship/change")
    public ResponseResult changeShipments(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.changeShipments(param);
    }

    /**
     * 查询订单物流信息
/**
     *
     * @param param 包含 orderId
     * @return ResponseResult
     */
    @PostMapping("/logistics")
    public ResponseResult logistics(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.queryLogistics(param);
    }

    /**
     * 运营备注（通用字段更新）
/**
     *
     * @param param 包含 orderId / remark 等任意字段
     * @return ResponseResult
     */
    @PostMapping("/remark")
    public ResponseResult remark(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.updateOrderByMap(param);
    }

    /**
     * 售后服务列表（退货/退款）
/**
     *
     * @param param 查询参数
     * @return ResponseResult
     */
    @PostMapping("/aftersale/list")
    public ResponseResult afterSaleList(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.queryAfterSaleList(param);
    }

    /**
     * 售后服务详情
/**
     *
     * @param param 包含 id
     * @return ResponseResult
     */
    @PostMapping("/aftersale/detail")
    public ResponseResult afterSaleDetail(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.queryAfterSaleDetail(param);
    }

    /**
     * 退款详情
/**
     *
     * @param param 包含 id
     * @return ResponseResult
     */
    @PostMapping("/aftersale/refundDetail")
    public ResponseResult afterSaleRefundDetail(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.queryRefundDetail(param);
    }

    /**
     * 售后审批（同意/拒绝）统一入口
/**
     * 默认退货退款使用 forSellerApprove；可通过入参 mode=return 调用专门退货审批。
     *
     * @param param id / type(1同意/0拒绝) / mode(可选)
     * @return ResponseResult
     */
    @PostMapping("/aftersale/approve")
    public ResponseResult afterSaleApprove(@RequestBody Map<String, Object> param) {
        Object mode = param == null ? null : param.get("mode");
        if (mode != null && "return".equalsIgnoreCase(mode.toString())) {
            return orderServiceFeign.sellerApproveReturn(param);
        }
        return orderServiceFeign.sellerApproveAfterSale(param);
    }

    /**
     * 订单各状态汇总数量统计（顶部计数）
/**
     *
     * @param param 查询参数
     * @return ResponseResult
     */
    @PostMapping("/total")
    public ResponseResult ordersTotal(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.getOrdersTotal(param);
    }

    /**
     * 平台运营全量订单列表（soldOrders）
/**
     *
     * @param param 查询参数
     * @return ResponseResult
     */
    @PostMapping("/soldOrders")
    public ResponseResult soldOrders(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.querySoldOrders(param);
    }

    /**
     * 订单导出（Excel）
/**
     *
     * @param param 导出查询参数
     * @return ResponseResult
     */
    @PostMapping("/export")
    public ResponseResult exportOrders(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.exportOrders(param);
    }
}
