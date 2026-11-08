package com.houjiang.order.feign;

import com.houjiang.commons.dto.order.ConsumeRecordByBusinessDTO;
import com.houjiang.commons.dto.order.ConsumeRecordParaDTO;
import com.houjiang.commons.dto.order.ConsumeTradeParaDTO;
import com.houjiang.commons.dto.order.RefundParamDTO;
import com.houjiang.commons.dto.payment.MallCashierCreateRequest;
import com.houjiang.commons.dto.payment.MallCashierCreateResponse;
import com.houjiang.commons.dto.payment.MallRefundRequest;
import com.houjiang.commons.dto.payment.MallCashierStatusResponse;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Amos
 * @date 2023/3/27 10:06
 */
@FeignClient("payment")
public interface PayMentServiceClient {

    /**
     * TODO 补充方法说明
     * @param request TODO
     * @return TODO
     */
    @PostMapping("/api/payment/cashier/mall/create")
    ResponseResult<MallCashierCreateResponse> createCashier(@RequestBody MallCashierCreateRequest request);

    /**
     * TODO 补充方法说明
     * @param request TODO
     * @return TODO
     */
    @PostMapping("/api/payment/cashier/mall/refund")
    ResponseResult<?> refund(@RequestBody MallRefundRequest request);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @PostMapping("/api/payment/cashier/mall/status")
    ResponseResult<MallCashierStatusResponse> cashierStatus(@RequestParam("cashierNo") String cashierNo);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @PostMapping("/payment/userResource/feign/queryAccountBalance")
    String queryAccountBalance(@RequestParam("userId") String userId);

    /**
     * 插入购物记录 商家
     * @param crb
     */
    @PostMapping("pay/consumeRecord/insertByBusiness")
    boolean insertConsumeRecordByBusiness(@RequestBody ConsumeRecordByBusinessDTO crb);
    /**
     * 插入购物记录 买家
     * @param crp
     */
    @PostMapping("pay/consumeRecord/insertByBuyer")
    boolean insertConsumeRecord(@RequestBody ConsumeRecordParaDTO crp);

    /**
     * 插入交易记录
     * @param ctp
     */
    @PostMapping("pay/consumeTrade/insert")
    boolean insertConsumeTrade(@RequestBody ConsumeTradeParaDTO ctp);

    /**
     * 取消付款状态
     *
     * @param orderId
     * @return
     */
    @PostMapping("/pay/consumeRecord/updateCancelStatus")
    ResponseResult updateCancelStatus(@RequestBody String orderId);

    /**
     * 发货成功，修改状态
     *
     * @param orderId
     * @return
     */
    @PostMapping("/pay/consumeRecord/change")
    ResponseResult changeConsumeRecord(@RequestBody String orderId);

    /**
     * 增加卖家金额
     *
     * @param map
     * @return
     */
    @PostMapping("/payment/userResource/addMoney")
    ResponseResult addMoney(@RequestBody Map<String, Object> map);


    /**
     * 新增交易明细
     *
     * @param returnMap
     * @return
     */
    @PostMapping("/pay/consumeRecord/insertRecord")
    ResponseResult insertRecord(@RequestBody Map<String, Map<String, String>> returnMap);

    /**
     *线下支付上传凭证，审核通过
/**
     *
     * @param map
     * @return
     */
    @Transactional
    @RequestMapping("payment/checkPaymentVocher")
    ResponseResult checkPaymentVocher(@RequestBody Map<Object, Object> map);


    /**
     * 更新退款金额
     *
     * @param feignMap
     */
    @PostMapping("/pay/consumeRecord/updateRefuse")
    void updateRefuse(@RequestBody Map<String, String> feignMap);


    /**
     * 生成结算单
     *
     * @param settlementMap
     * @return
     */
    @PostMapping("/pay/consumeRecord/insertSettlementRecord")
    boolean insertSettlementRecord(@RequestBody Map<String, Object> settlementMap);

    /**
     * 纯积分支付
     *
     * @param
     * @return
     */
    @RequestMapping("pay/entrance/userPay")
    ResponseResult payMentUnifyInlet(@RequestBody Map<Object, Object> payInfo);

    /**
     * 修改个人余额
     * @param userId
     * @param rechargeAmount
     * @return
     */
    @PostMapping("/payment/userResource/updateUserBalance")
    ResponseResult updateUserBalance(@RequestParam("userId") String userId, @RequestParam("rechargeAmount") BigDecimal rechargeAmount);

    /**
     * 获取支付配置
     *
     * @param param
     * @return
     */
    @PostMapping("/pay/payConfig/queryFeign")
    ResponseResult selectPayConfig(@RequestBody Map<String, Object> param);
    /**
     * 待发货退款
     *
     * @param refundPara
     * @return
     */
    @PostMapping("/pay/consumeRecord/waitShipmentsRefund")
    boolean waitShipmentsRefund(@RequestBody RefundParamDTO refundPara);
}
