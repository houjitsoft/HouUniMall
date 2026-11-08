package com.houjiang.job.job;

import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.feign.PayMentServiceClient;
import com.houjiang.job.feign.UserServiceClient;
import com.houjiang.job.mapper.OrderbaseMapper;
import com.houjiang.job.vo.UpdateALiWxOrderVO;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName UpdateALiWxOrderStart
 * @Description 处理支付宝 微信 支付成功后 回调接口异常的单
 * @Author Amos
 * @Date 2024/4/16 11:46
 * @Version 1.0
 * @author Amos
 */
@Controller
public class UpdateALiWxOrderStart implements BaseJob {

    private static final Logger logger = LoggerFactory.getLogger(UpdateALiWxOrderStart.class);

    @Autowired
    private PayMentServiceClient payMentServiceClient;
    @Autowired
    private OrderbaseMapper orderbaseMapper;
    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    @RequestMapping("UpdateALiWxOrderStart")
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //查询待支付的订单
        List<UpdateALiWxOrderVO> orderVOList = orderbaseMapper.queryOrderNoPay();
        //遍历订单
        for (UpdateALiWxOrderVO updateALiWxOrderVO : orderVOList) {
            // 先根据支付单号 查询 支付宝 微信 是否已经支付 如果已经支付就调用 支付补偿接口修改订单状态等信息
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", updateALiWxOrderVO.getPaymentNumber());
            // 根据支付单号查询支付宝 微信 是否已经支付
            Boolean aBoolean = checkOrderStartIsPay(map);
            if (aBoolean) {
                continue;
            }
            // 如果根据支付单号未查询 再根据订单号 查询 支付宝 微信 是否已经支付 如果已经支付就调用 支付补偿接口修改订单状态等信息
            map.put("orderId", updateALiWxOrderVO.getOrderId());
            // 根据订单号查询支付宝 微信 是否已经支付
            checkOrderStartIsPay(map);
        }
        // 查询所有plus 预支付单号
        List<UpdateALiWxOrderVO> plusOrderVOList = orderbaseMapper.queryPlusOrderNoPay();
        for (UpdateALiWxOrderVO updateALiWxOrderVO : plusOrderVOList) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", updateALiWxOrderVO.getPlusOrderid());
            map.put("plusOrder", updateALiWxOrderVO.getPlusOrderid());
            checkPlusOrderStartIsPay(map);
        }

    }

    /**
     * 根据传入的订单号 或者 支付单号 查询支付 或者微信是否已经支付 如果已经支付就 调用对应的补偿接口 修改订单状态
     */
    public Boolean checkOrderStartIsPay(Map<String, Object> map) {
        // 根据单号查询支付宝是否已经支付
        ResponseResult aLiResponseResult = payMentServiceClient.alipayQuery(map);
        if (aLiResponseResult.getCode() == CommonStatusEnum.SUCCESS.getCode()) {
            // 调用支付宝支付补偿接口
            payMentServiceClient.alipayCompensate((Map<String, Object>) aLiResponseResult.getData());
            return true;
        }
        // 根据单号查询微信是否已经支付
        ResponseResult wxResponseResult = payMentServiceClient.queryWxPay(map);
        if (wxResponseResult.getCode() == CommonStatusEnum.SUCCESS.getCode()) {
            // 调用微信支付补偿接口
            payMentServiceClient.wxPayCompensate((Map<String, Object>) wxResponseResult.getData());
            return true;
        }
        return false;
    }

    /**
     * 根据传入的订单号 或者 支付单号 查询支付 或者微信是否已经支付 如果已经支付就 调用对应的补偿接口 修改订单状态
     */
    public Boolean checkPlusOrderStartIsPay(Map<String, Object> map) {
        // 根据单号查询支付宝是否已经支付
        ResponseResult aLiResponseResult = payMentServiceClient.alipayQuery(map);
        if (aLiResponseResult.getCode() == CommonStatusEnum.SUCCESS.getCode()) {
            // 调用会员权益补偿接口
            userServiceClient.plusOrderCompensate(map);
            return true;
        }
        // 根据单号查询微信是否已经支付
        ResponseResult wxResponseResult = payMentServiceClient.queryWxPay(map);
        if (wxResponseResult.getCode() == CommonStatusEnum.SUCCESS.getCode()) {
            // 调用会员权益补偿接口
            userServiceClient.plusOrderCompensate(map);
            return true;
        }
        return false;
    }
}
