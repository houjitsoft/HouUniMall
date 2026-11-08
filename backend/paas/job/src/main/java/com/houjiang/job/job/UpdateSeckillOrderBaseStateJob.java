package com.houjiang.job.job;


import cn.hutool.core.util.StrUtil;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.order.CancelParaEntityDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.job.feign.ActivityServiceClient;
import com.houjiang.job.feign.BaseServiceClient;
import com.houjiang.job.feign.GoodsServiceClient;
import com.houjiang.job.feign.PayMentServiceClient;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @Author Amos
 * @Description 此方法用于更新秒杀未支付订单状态并返还秒杀库存
 * @Date 09:12 2022/10/31
 * @Param
 * @return
 * @author Amos
 */
public class UpdateSeckillOrderBaseStateJob implements BaseJob {

    private static final Logger logger = LoggerFactory.getLogger(UpdateSeckillOrderBaseStateJob.class);

    @Autowired
    UpdateOrderStatusService UpdateOrderStatusService;

    @Autowired
    PayMentServiceClient payMentServiceClient;

    @Autowired
    GoodsServiceClient goodsServiceClient;

    @Autowired
    BaseServiceClient baseServiceClient;

    @Autowired
    ActivityServiceClient activityServiceClient;

    /**
     * 执行作业，取消未支付的订单。
     * 该方法从数据库中查询所有需要被取消的未支付订单，并对这些订单执行取消操作。
     * 取消订单包括更新订单状态、修改库存、退还红包等操作。
     *
     * @param context 作业执行上下文，提供作业执行所需的信息。
     * @throws JobExecutionException 如果作业执行过程中出现异常。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 查询需要被取消的未支付订单列表
        List<String> orderIdList = UpdateOrderStatusService.queryUnpaidOrdersToBeCancelled();
        // 定义订单取消身份标识
        Integer orderCancelIdentity = 3;

        // 遍历未支付订单列表，对每个订单执行取消操作
        for (String orderId : orderIdList) {
            // 订单取消状态
            Integer orderStatus = 7;
            // 订单取消原因
            String orderCancelReason = "未在规定时间内支付";
            // 订单取消时间
            String orderCancelDate = DateUtils.getNowDateToString();

            // 更新订单基础信息和订单商品状态
            boolean isUpdateSuccess = UpdateOrderStatusService.updateByOrderBase(orderId, orderStatus,
                    orderCancelIdentity, orderCancelReason, orderCancelDate);
            boolean isUpdateGoodsSuccess = UpdateOrderStatusService.updateByOrderGoods(orderId, orderStatus);

            // 取消佣金订单
            UpdateOrderStatusService.updateOrderStatus(CommonType.CommissionOrderStatus.CANCEL.getCode(), orderId);
            // 更新订单支付状态为已取消
            payMentServiceClient.updateCancelStatus(orderId);
            // 查询订单相关的支付单号、红包ID、优惠券ID等信息
            CancelParaEntityDTO cancelParaEntity = UpdateOrderStatusService.queryPaymentNumberAndRedpacketId(orderId);
            String paymentNumber = cancelParaEntity.getPaymentNumber();
            String redpacketId = cancelParaEntity.getRedpacketCode();
            String voucherCode = cancelParaEntity.getVoucherCode();
            String platformCouponCode = cancelParaEntity.getPlatformCouponCode();
            String markId = cancelParaEntity.getOrderBuyId();
            // 根据订单ID查询商品相关信息，更新商品库存
            //取消订单成功后 修改库存
            //获取goodsid commonid 购买数量
            List<WapGoodsSaleSumDTO> wapGood = UpdateOrderStatusService.queryGoodsXinXi(orderId);
            goodsServiceClient.updateGoodsStock(wapGood);

            // 如果存在红包ID，退还红包
            if (redpacketId != null) {
                // 查询订单状态
                List<Integer> orderStatusList = UpdateOrderStatusService.queryOrderStatus(paymentNumber);
                // 根据订单状态更新红包促销信息
                Map<String, Object> codeMap = new HashMap<String, Object>();
                codeMap.put("redpacketId", redpacketId);
                codeMap.put("platformVoucherCode", platformCouponCode);
                codeMap.put("type", "0");
                Set set = new HashSet(orderStatusList);
                if (set.size() == 1) {
                    activityServiceClient.updateSalesPrm(codeMap);
                    // 如果存在优惠券ID，更新优惠券促销信息
                    if (!StrUtil.isEmptyIfStr(voucherCode)) {
                        List<String> voucherCodeList = Arrays.asList(voucherCode.split(","));
                        Map<String, Object> voucherCodeMap = new HashMap<String, Object>();
                        voucherCodeMap.put("voucherId", voucherCodeList);
                        voucherCodeMap.put("type", "0");
                        activityServiceClient.updateSalesPrm(voucherCodeMap);
                    }
                }
            } else {
                // 如果不存在红包ID，但存在优惠券ID，更新优惠券促销信息
                if (!StrUtil.isEmptyIfStr(voucherCode)) {
                    List<String> voucherCodeList = Arrays.asList(voucherCode.split(","));
                    Map<String, Object> voucherCodeMap = new HashMap<String, Object>();
                    voucherCodeMap.put("voucherId", voucherCodeList);
                    voucherCodeMap.put("platformVoucherCode", platformCouponCode);
                    voucherCodeMap.put("type", "0");
                    activityServiceClient.updateSalesPrm(voucherCodeMap);
                }
            }
        }
    }
}
