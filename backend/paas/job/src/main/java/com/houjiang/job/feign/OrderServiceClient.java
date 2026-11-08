package com.houjiang.job.feign;


import com.houjiang.commons.dto.job.ExpireMarkIdUpdateDTO;
import com.houjiang.commons.dto.job.ShopSettleAccountInfoDTO;
import com.houjiang.commons.dto.order.SupplierShopSettleAccountInfoDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.job.AcquiescenceVO;
import com.houjiang.job.feign.support.InternalJobFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 *
 * <p>本客户端的定时任务端点（{@code /order/**&#47;job/**}）在 order 服务侧由
 * {@code OrderInternalJobTokenFilter} 校验 {@code X-Internal-Job-Token} 后放行，
 * 因此必须挂 {@link InternalJobFeignConfiguration} 随请求携带该内部令牌；
 * 否则 quartz 任务无用户 Token，会被 order 的 JWT 过滤器以「请先登录后再操作」拒绝。
 */
@FeignClient(name = "order", contextId = "orderServiceClient",
        configuration = InternalJobFeignConfiguration.class)
public interface OrderServiceClient {

    /**
     * 查订单数据
     *
     * @return
     */
    @PostMapping("order/base/job/queryNotEvaluatedOrder")
    ResponseResult queryNotEvaluatedOrder();

    /**
     * 根据订单id商品信息
     *
     * @param orderId
     * @return
     */
    @GetMapping("order/goods/job/queryGoodsInfoByjob")
    List<AcquiescenceVO> queryGoodsInfoByjob(@RequestParam("orderId") String orderId);

    /**
     * 确认收货超过14天默认默认好评
     *
     * @param acquiescenceVO
     * @return
     */
    @PostMapping("/order/goodsEvaluation/job/insertEvaluated")
    ResponseResult insertEvaluatedByJob(@RequestBody AcquiescenceVO acquiescenceVO);

    /**
     * 退货商家超时未处理
     *
     * @return
     */
    @RequestMapping("/order/return/job/updateTimeOutReturnGoods")
    @ResponseBody
    Boolean updateTimeOutReturnGoods();


    /**
     * 退款/仅退款 商家超时未处理
     *
     * @return
     */
    @PostMapping("/order/return/job/updateTimeOutReturn")
    Boolean updateTimeOutReturn();


    /**
     * 查看拼团状态
     *
     * @param para
     * @return
     */
    @PostMapping("/order/base/checkGroupBookingStatus")
    boolean checkGroupBookingStatus(@RequestBody ExpireMarkIdUpdateDTO para);


    /**
     * 商家拒绝退款/仅退款 买家超时未处理
     *
     * @return
     */
    @PostMapping("/order/return/job/updateTimeOutbuyReturn")
    Boolean updateTimeOutBuyReturn();

    /**
     * 商家拒绝退货 买家超时未处理
     *
     * @return
     */
    @PostMapping("/order/return/job/updatebuyTimeOutReturn")
    Boolean updatebuyTimeOutReturn();


    /**
     * 申请平台介入卖家超时未处理
     *
     * @return
     */
    @PostMapping("/order/return/platform/job/updateTimeOut")
    Boolean updateTimeOutReturnplatform();


    /**
     * 商家同意退货 买家未在有效期内填写物流信息
     *
     * @return
     */
    @PostMapping("/order/return/job/updatebuyTimeOutReturnGoods")
    Boolean updatebuyTimeOutReturnGoods();


    /**
     * 申请平台介入卖家超时未处理
     *
     * @return
     */
    @PostMapping("/order/return/platform/update")
    Boolean updateplatform();
    /**
     * 获取商家结算数据
     *
     * @param mapAccount
     * @return
     */
    @PostMapping("/order/settlement/feign/getShopSettleAccountData")
    ShopSettleAccountInfoDTO getShopSettleAccountData(@RequestBody Map<String, Object> mapAccount);

    /**
     * 获取供应商结算数据
     *
     * @param mapAccount
     * @return
     */
    @PostMapping("/order/settlement/feign/getSupplierSettleAccountData")
    SupplierShopSettleAccountInfoDTO getSupplierSettleAccountData(@RequestBody Map<String, Object> mapAccount);

    /**
     * job-定时任务处理 超过48小时商家未处理售后换货订单
     */
    @PostMapping("/orderBarter/feign/handleBarterOrderToJob")
    void handleBarterOrderToJob();

}
