package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.vop.JingDongReturnOrderDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.order.dto.ApplyReturnOrderDTO;
import com.houjiang.order.dto.ReturnOrderDTO;
import com.houjiang.order.entity.OrderBarterEntity;
import com.houjiang.order.vo.ReturnOrderVO;

import java.util.Map;

/**
 * 订单换货表(OrderBarter)表服务接口
 *
 * @author Amos
 * @since 2025-05-07 09:36:29
 */
public interface OrderBarterService extends IService<OrderBarterEntity> {

    /**
     * 申请换货
     */
    ResponseResult applyForBarter(ApplyReturnOrderDTO applyReturnOrders);

    /**
     * 查询换货订单
     */
    ResponseResult<PageUtils<ReturnOrderVO>> selectBarterOrder(ReturnOrderDTO applyReturnOrders);

    /**
     * 查询换货订单详情
     */
    ResponseResult queryBarterOrderDetails(ReturnOrderDTO applyReturnOrders);

    /**
     * 商家处理换货申请
     * 商家同意换货与否，type=1 同意，type=0 不同意
     */
    ResponseResult approveBarterAudit(ReturnOrderDTO dto);

    /**
     * 换货买家填写物流信息
     */
    ResponseResult barterShipping(ReturnOrderDTO dto);

    /**
     * 换货-买家未填写物流信息时商家确认收货
     */
    ResponseResult barterShopShipping(ReturnOrderDTO dto);

    /**
     * 换货-商家确认收货
     */
    ResponseResult shopConfirmReceipt(ReturnOrderDTO dto);

    /**
     * 换货-商家发起换货物流
     */
    ResponseResult shopInitiateLogistics(ReturnOrderDTO dto);

    /**
     * 换货-京东发起换货物流
     * @param map
     * @return
     */
    ResponseResult jingDongShopInitiateLogistics(Map<String,Object> map);

    /**
     * 换货-买家确认收货
     */
    ResponseResult buyerBarterConfirmReceipt(ReturnOrderDTO dto);

    /**
     * 换货-平台查询换货订单列表
     */
    ResponseResult selectBarterOrderListToPlatform(ReturnOrderDTO dto);

    /**
     * 换货-平台导出换货订单列表
     */
    ResponseResult exportBarterOrderList(ReturnOrderDTO dto);

    /**
     * 换货-查询换货订单详情
     */
    ResponseResult barterOrderDetail(ReturnOrderDTO dto);

    /**
     * 换货-平台介入同意退货与否，则 type=1 同意，则type=0不同意
     */
    ResponseResult platformApprove(ReturnOrderDTO dto);

    /**
     * 换货-查询换货代发明细列表
     */
    ResponseResult<PageUtils<ReturnOrderVO>> selectBarterDropShippingList(ReturnOrderDTO dto);

    /**
     * 换货-导出换货代发明细列表
     */
    ResponseResult exportBarterDropShippingList(ReturnOrderDTO dto);

    /**
     * 换货-寄回代发商品订单
     */
    ResponseResult sendBackDropShipping(ReturnOrderDTO dto);

    /**
     * job-定时任务处理 超过48小时商家未处理售后换货订单
     */
    void handleBarterOrderToJob();
}
