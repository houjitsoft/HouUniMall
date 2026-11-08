package com.houjiang.order.controller;


import com.houjiang.commons.dto.vop.JingDongReturnOrderDTO;
import com.houjiang.commons.group.Update;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.order.dto.ApplyReturnOrderDTO;
import com.houjiang.order.dto.ReturnOrderDTO;
import com.houjiang.order.service.OrderBarterService;
import com.houjiang.order.vo.ReturnOrderVO;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * 订单换货表(OrderBarter)表控制层
 *
 * @author Amos
 * @since 2025-05-07 09:36:29
 */
@RestController
@RequestMapping("orderBarter")
public class OrderBarterController {

    /**
     * 服务对象
     */
    @Resource
    private OrderBarterService orderBarterService;

    /**
     * 申请换货
     */
    @GlobalTransactional
    @PostMapping("/applyForBarter")
    public ResponseResult applyForBarter(@RequestBody @Validated({Update.applyForBarter.class}) ApplyReturnOrderDTO applyReturnOrders) {
        return orderBarterService.applyForBarter(applyReturnOrders);
    }

    /**
     * 查询换货订单
     */
    @PostMapping("/selectBarterOrder")
    public ResponseResult<PageUtils<ReturnOrderVO>> selectBarterOrder(@RequestBody ReturnOrderDTO applyReturnOrders) {
        return orderBarterService.selectBarterOrder(applyReturnOrders);
    }

    /**
     * 查询换货订单详情
     */
    @PostMapping("/queryBarterOrderDetails")
    public ResponseResult queryBarterOrderDetails(@RequestBody ReturnOrderDTO applyReturnOrders) {
        return orderBarterService.queryBarterOrderDetails(applyReturnOrders);
    }

    /**
     * 商家处理换货申请
     * 商家同意换货与否，type=1 同意，type=0 不同意
     */
    @Transactional
    @PostMapping("/approveBarter")
    public ResponseResult approveBarter(@RequestBody @Validated({Update.approveBarter.class}) ReturnOrderDTO dto) {
        return orderBarterService.approveBarterAudit(dto);
    }

    /**
     * 换货-买家填写物流信息
     */
    @Transactional
    @PostMapping("/barter/shipping")
    public ResponseResult barterShipping(@RequestBody ReturnOrderDTO dto) {
        return orderBarterService.barterShipping(dto);
    }

    /**
     * 换货-买家未填写物流信息时商家确认收货
     */
    @Transactional
    @PostMapping("/barter/shop/shipping")
    public ResponseResult barterShopShipping(@RequestBody ReturnOrderDTO dto) {
        return orderBarterService.barterShopShipping(dto);
    }

    /**
     * 换货-商家确认收货
     */
    @Transactional
    @PostMapping("/shopConfirmReceipt")
    public ResponseResult shopConfirmReceipt(@RequestBody ReturnOrderDTO dto) {
        return orderBarterService.shopConfirmReceipt(dto);
    }

    /**
     * 换货-商家发起换货物流
     */
    @Transactional
    @PostMapping("/shopInitiateBarterLogistics")
    public ResponseResult shopInitiateLogistics(@RequestBody ReturnOrderDTO dto) {
        return orderBarterService.shopInitiateLogistics(dto);
    }
    /**
     * 换货-京东发起换货物流
     */
    @Transactional
    @PostMapping("/jingDongShopInitiateLogistics")
    public ResponseResult jingDongShopInitiateLogistics(@RequestBody Map<String,Object> map) {
        return orderBarterService.jingDongShopInitiateLogistics(map);
    }
    /**
     * 换货-买家确认收货
     */
    @Transactional
    @PostMapping("/buyerBarterConfirmReceipt")
    public ResponseResult buyerBarterConfirmReceipt(@RequestBody ReturnOrderDTO dto) {
        return orderBarterService.buyerBarterConfirmReceipt(dto);
    }

    /**
     * 换货-平台查询换货订单列表
     */
    @PostMapping("/platform/selectBarterOrderList")
    public ResponseResult selectBarterOrderListToPlatform(@RequestBody ReturnOrderDTO dto) {
        return orderBarterService.selectBarterOrderListToPlatform(dto);
    }

    /**
     * 换货-平台导出换货订单列表
     */
    @PostMapping("/platform/exportBarterOrderList")
    public ResponseResult exportBarterOrderList(@RequestBody ReturnOrderDTO dto) {
        return orderBarterService.exportBarterOrderList(dto);
    }

    /**
     * 换货-查询换货订单详情
     */
    @PostMapping("/barterOrderDetail")
    public ResponseResult barterOrderDetail(@RequestBody ReturnOrderDTO dto) {
        return orderBarterService.barterOrderDetail(dto);
    }

    /**
     * 换货-平台介入同意退货与否，则 type=1 同意，则type=0不同意
     */
    @Transactional
    @PostMapping("/platform/approve")
    public ResponseResult platformApprove(@RequestBody ReturnOrderDTO dto) {
        return orderBarterService.platformApprove(dto);
    }

    /**
     * 换货-查询换货代发明细列表
     */
    @PostMapping("/selectBarterDropShippingList")
    public ResponseResult<PageUtils<ReturnOrderVO>> selectBarterDropShippingList(@RequestBody ReturnOrderDTO dto) {
        return orderBarterService.selectBarterDropShippingList(dto);
    }

    /**
     * 换货-导出换货代发明细列表
     */
    @PostMapping("/exportBarterDropShippingList")
    public ResponseResult exportBarterDropShippingList(@RequestBody ReturnOrderDTO dto) {
        return orderBarterService.exportBarterDropShippingList(dto);
    }

    /**
     * 换货-寄回代发商品订单
     */
    @Transactional
    @PostMapping("/sendBackDropShipping")
    public ResponseResult sendBackDropShipping(@RequestBody ReturnOrderDTO dto) {
        return orderBarterService.sendBackDropShipping(dto);
    }

    /**
     * job-定时任务处理 超过48小时商家未处理售后换货订单
     */
    @PostMapping("/feign/handleBarterOrderToJob")
    public void handleBarterOrderToJob(){
        orderBarterService.handleBarterOrderToJob();
    }

}

