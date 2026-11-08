package com.houjiang.bffweb.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/** Exact consumer routes backed by the independent order service. */
@RestController
public class UcenterOrderRouteController {
    private final UcenterServiceForwarder forwarder;

    @Autowired
    public UcenterOrderRouteController(@Value("${feign.order.url:}") String configuredUrl,
                                       DiscoveryClient discovery) {
        this(configuredUrl, discovery, UcenterServiceForwarder.httpClient());
    }

    UcenterOrderRouteController(String configuredUrl, DiscoveryClient discovery, RestTemplate http) {
        this.forwarder = new UcenterServiceForwarder("order", "/web/order", configuredUrl, discovery, http);
    }

    @PostMapping({
            "/web/order/order/base/enquireSoldOrders",
            "/web/order/order/base/queryVirtualOrderByEvaluation",
            "/web/order/order/base/addComments",
            "/web/order/order/base/cancelTheOrder",
            "/web/order/order/base/deleteOrder",
            "/web/order/order/base/confirmTheGoods",
            "/web/order/order/base/delayToReceiveGoods",
            "/web/order/order/base/query/list",
            "/web/order/order/return/selectOrderReturn",
            "/web/order/order/return/onlyReturnMoney",
            "/web/order/order/return/applyRefund",
            "/web/order/order/return/applyReturn/more",
            "/web/order/order/return/calculate/price",
            "/web/order/order/return/revoke",
            "/web/order/order/return/platform/add/intervention",
            "/web/order/order/return/revoke/platform",
            "/web/order/order/return/return/shipping",
            "/web/order/order/return/listReturnOrderByRT",
            "/web/order/order/return/refundDetail",
            "/web/order/order/return/returnDetail",
            "/web/order/order/cancelReason/list",
            "/web/order/order/goodsEvaluation/queryMyEvaluation",
            "/web/order/order/complaints/add",
            "/web/order/order/complaints/complaintsDetail",
            "/web/order/order/complaints/revokeComplaints",
            "/web/order/order/complaints/queryComplaints",
            "/web/order/order/paymentVocher/query",
            "/web/order/order/paymentVocher/insert",
            "/web/order/orderBarter/applyForBarter",
            "/web/order/orderBarter/barter/shipping",
            "/web/order/orderBarter/buyerBarterConfirmReceipt",
            "/web/order/order/goods/queryOrderBarterGoods",
            "/web/order/order/goods/queryOrderBarterGoodsSpec",
            "/web/order/order/goods/queryLogistics",
            "/web/order/order/goods/queryLogisticsByCode",
            "/web/order/order/invoiceTemplateInfo/list",
            "/web/order/order/invoiceTemplateInfo/query",
            "/web/order/order/invoiceTemplateInfo/insert",
            "/web/order/order/invoiceTemplateInfo/update",
            "/web/order/order/invoiceTemplateInfo/delete",
            "/web/order/order/invoiceAddress/list",
            "/web/order/order/invoiceAddress/queryById",
            "/web/order/order/invoiceAddress/add",
            "/web/order/order/invoiceAddress/delete",
            "/web/order/orderInvoiceInfo/apply/invoice",
            "/web/order/orderInvoiceInfo/invoice/detail",
            "/web/order/orderBalanceRecharge/createBalanceRechargeOrder",
            "/web/order/activity/voucherBase/selectvcbase",
            "/web/order/activity/redpacketBase/select/myRedPacket",
            "/web/order/order/base/im/queryUserOrderInfo"
    })
    public ResponseEntity<byte[]> post(HttpServletRequest request) throws IOException {
        return UcenterServiceForwarder.sanitize(forwarder.forward(request, HttpMethod.POST));
    }

    @GetMapping("/web/order/order/returnReason/yiyatong/listreturnreason")
    public ResponseEntity<byte[]> get(HttpServletRequest request) throws IOException {
        return UcenterServiceForwarder.sanitize(forwarder.forward(request, HttpMethod.GET));
    }
}
