package com.houjiang.order.service;

import com.houjiang.commons.dto.mall.MallStockReservationRequest;
import com.houjiang.order.feign.GoodsStockLifecycleClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单侧库存生命周期编排；实际预占/实扣/释放/回补只调用 goods。
 */
@RequiredArgsConstructor
@Service
public class OrderInventoryLifecycleService {

    private final GoodsStockLifecycleClient stockClient;

    public void reserve(String orderNo, List<MallStockReservationRequest> lines) {
        if (orderNo == null || orderNo.isBlank() || lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("库存预占参数不完整");
        }
        lines.forEach(line -> {
            if (line == null || !orderNo.equals(line.getOrderNo())) {
                throw new IllegalArgumentException("库存预占订单号不一致");
            }
        });
        stockClient.reserve(lines);
    }

    public void markPaid(String orderNo) {
        stockClient.commit(requireOrderNo(orderNo));
    }

    public void closeUnpaid(String orderNo) {
        stockClient.release(requireOrderNo(orderNo));
    }

    public void returnToStock(String orderNo, boolean warehouseReceived) {
        if (!warehouseReceived) {
            throw new IllegalStateException("退货尚未验收入库，不能回补可售库存");
        }
        stockClient.returnToStock(requireOrderNo(orderNo));
    }

    private String requireOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        return orderNo;
    }
}
