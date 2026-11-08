package com.houjiang.goods.feign;

import com.houjiang.commons.dto.goods.StorageInventoryFlowGoodsDTO;
import com.houjiang.commons.dto.vop.JingDongReturnOrderDTO;
import com.houjiang.commons.dto.vop.OrderGoodsJdVopDTO;
import com.houjiang.commons.dto.yiyatong.ReturnListDTO;
import com.houjiang.commons.dto.yiyatong.ShipmentsListDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.goods.PurchaseOrderVO;
import com.houjiang.commons.vo.order.OrderBaseVO;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@FeignClient("order")
public interface OrderServiceClient {

    /**
     * 查询商品在购物车的数量
     */
    @PostMapping("order/userCart/selectCount")
    public Integer selectCurrentCartQuantity(@RequestParam("userId")String userId,@RequestParam("goodsId")Integer goodsId);

    /**
     * 查询采购单商品信息
     * @param param 查询参数
     * @return 进货单商品信息
     */
    @PostMapping("/order/base/queryPurchaseOrderGoods")
    PurchaseOrderVO queryPurchaseOrderGoods(@RequestBody StorageInventoryFlowGoodsDTO param);

    /**
     * 怡亚通发货
     *
     * @param shipmentsListDTO
     * @return
     */
    @PostMapping("/order/base/yiyatong/shipments")
    ResponseResult yiyatongShipments(@RequestBody ShipmentsListDTO shipmentsListDTO);


    /**
     * 怡亚通售后
     *
     * @param returnListDTO
     * @return
     */

    /**
     * @param returnListDTO return列表传输对象
     * @return 处理结果
     */
    @PostMapping("/order/base/yiyatong/return")
    ResponseResult yiyatongReturn(@RequestBody ReturnListDTO returnListDTO);

    /**
     * 查询订单信息
     * @param orderId
     * @return
     */
    @GetMapping("/order/base/feign/queryOrderInfoFegin")
    OrderBaseVO queryOrderInfoFegin(@RequestParam("orderId") String orderId);

    /**
     * 将京东订单号存入库中
     * @param orderId
     * @param jingDongOrderId
     */
    @PostMapping("/order/base/updateOrderJingDongOrderId")
    ResponseResult updateOrderJingDongOrderId(@RequestParam("orderId") String orderId, @RequestParam("jingDongOrderId") String jingDongOrderId);

    /**
     * 根据京东订单号 修改我方订单状态为 已发货
     * @param jdOrderIds
     * @return
     */
    @PostMapping("/order/base/updateOrderStatusByJDOrderId")
    ResponseResult updateOrderStatusByJDOrderId(@RequestParam("jdOrderIds") List<String> jdOrderIds);

    /**
     * 京东取消订单 商城端退款
     *
     * @param jdOrderIds
     * @return
     */
    @PostMapping("/order/return/jingdong/forSellerApprove")
    ResponseResult jingdongForSellerApprove(@RequestParam("jdOrderIds") List<String> jdOrderIds) throws TransactionException;
    /**
     * 将京东子订单号保存入库中
     * @param orderGoodsJdVopDTOS
     * @return
     */
    @PostMapping("/order/base/updateJongDongSonOrder")
    ResponseResult updateJongDongSonOrder(@RequestBody List<OrderGoodsJdVopDTO> orderGoodsJdVopDTOS);

    /**
     * 将京东售后申请号存入库
     * @param jdOrderId
     * @param jdReturenOrderId
     */

    /**
     * @return 处理结果
     */
    @PostMapping("/order/goods/updateJDReturnId")
    ResponseResult updateJDReturnId(@RequestParam("jdOrderId") String jdOrderId, @RequestParam("jdReturenOrderId")String jdReturenOrderId);

    /**
     * 京东售后单 退换货 处理
     *
     * @param map
     * @return
     */
    @PostMapping("/order/return/jingDong/return/audit")
    ResponseResult jingDongReturnAudit(@RequestBody Map<String, Object> map);

    /**
     * 京东 退货 换货 确认收货后 商品质检审核 通过 不通过
     *
     * @param map
     * @return
     */
    @PostMapping("/order/return/jingDong/qualityInspection/receive")
    @GlobalTransactional
    ResponseResult jingDongQualityInspectionReceive(@RequestBody Map<String,Object> map) throws TransactionException;

    /**
     * 换货-京东发起换货物流
     */
    @Transactional
    @PostMapping("/orderBarter/jingDongShopInitiateLogistics")
    ResponseResult jingDongShopInitiateLogistics(@RequestBody Map<String,Object> map);
    }
