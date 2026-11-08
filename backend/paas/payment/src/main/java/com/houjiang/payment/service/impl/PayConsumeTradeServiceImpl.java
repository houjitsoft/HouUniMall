package com.houjiang.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.dto.order.ConsumeTradeParaDTO;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.PayToolUtil;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.dto.GoodsDTO;
import com.houjiang.payment.entity.PayConsumeTradeEntity;
import com.houjiang.payment.feign.OrderServiceClient;
import com.houjiang.payment.mapper.PayConsumeRecordMapper;
import com.houjiang.payment.mapper.PayConsumeTradeBackMapper;
import com.houjiang.payment.mapper.PayConsumeTradeMapper;
import com.houjiang.payment.service.PayConsumeTradeService;
import com.houjiang.payment.vo.CallBackVO;
import com.houjiang.payment.vo.RecordVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName PayConsumeTradeServiceImpl
 * @Description 交易订单表-强调唯一订单-充值则先创建充值订单
 * @Author Amos
 * @Date 2023-05-31 16:13:57
 * @Version 1.0
 * @author Amos
 */
@Service
public class PayConsumeTradeServiceImpl extends ServiceImpl<PayConsumeTradeMapper, PayConsumeTradeEntity>
        implements PayConsumeTradeService {
    @Resource
    private PayConsumeTradeBackMapper payConsumeTradeBackMapper;

    @Resource
    private PayConsumeRecordMapper payConsumeRecordMapper;

    @Resource
    private OrderServiceClient orderServiceClient;

    final Logger log = LoggerFactory.getLogger(PayConsumeTradeServiceImpl.class);

    @Override
    @Transactional
    public boolean insert(ConsumeTradeParaDTO ctp) {
        String orderId = ctp.getOrderId();
        String paymentNumber = ctp.getPaymentNumber();
        // 支付单号
        String consumeTradeId = paymentNumber;
        String userId = ctp.getUserId();
        String sellerId = ctp.getSellerId();
        String payUserId = userId;
        // 订单状态
        Integer orderStateId = 1;
        // 交易类型
        Integer tradeTypeId = 1;
        // 支付渠道
        Integer paymentChannelId = 1;
        // 实付金额，在线支付金额
        BigDecimal tradePaymentAmount = ctp.getOrderCashPayment();
        // 嘉币

        // 劲豆

        // 初始化订单支付金额，使用贸易支付金额作为初始值
        BigDecimal orderPaymentAmount = tradePaymentAmount;
        // 获取交易折扣
        BigDecimal tradeDiscount = ctp.getTradeDiscount();
        // 组装交易日期字符串
        String tradeDate = DateUtils.getYear() + "-" + DateUtils.getMonth() + "-" + DateUtils.getDay();
        // 获取交易年份
        String tradeYear = DateUtils.getYear();
        // 获取交易月份
        String tradeMonth = DateUtils.getMonth();
        // 获取交易日
        String tradeDay = DateUtils.getDay();
        // 设置交易标题为"购物"
        String tradeTitle = "购物";
        // 获取商品名称列表
        List<String> goodsName = ctp.getGoodsName();
        // 初始化总积分，用于后续计算或记录
        // 兑换积分商品所需的积分
        Integer totalIntegral = null;
        // 处理积分商品数量，如果未设置则默认为1
        Integer integralGoods = ctp.getIntegralGoods() == null ? 1 : ctp.getIntegralGoods();
        // 如果存在总积分，则赋值
        if (null != ctp.getTotalIntegral()) {
            totalIntegral = ctp.getTotalIntegral();
        }
        // 设置交易描述为商品名称列表的第一个元素
        String tradeDesc = goodsName.get(0);
        // 初始化交易备注为空字符串
        String tradeRemark = "";
        // 获取当前时间并转换为字符串，作为交易创建时间
        String tradeCreateTime = DateUtils.getNowDateToString();
        // 尝试插入消费交易记录到数据库，并返回操作结果
        boolean isInsertSuccess = this.baseMapper.insertConsumeTrade(consumeTradeId, orderId, userId, sellerId, payUserId, orderStateId, tradeTypeId, paymentChannelId, orderPaymentAmount, tradePaymentAmount, ctp.getCustomerType(), tradeDiscount, tradeDate, tradeYear, tradeMonth, tradeDay, tradeTitle, tradeDesc, tradeRemark, tradeCreateTime, totalIntegral, integralGoods);
        // 返回插入操作是否成功的标志
        return isInsertSuccess;

    }

    @Override
    public List<CallBackVO> treasureOrder(String token, String orderId) {
        List<CallBackVO> callBackParaList = null;
        try {
            // 通过token获取用户信息
            // 获取user信息
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
            String userId = ui.getUserId();
            String userName = ui.getUserName();

            callBackParaList = new ArrayList<CallBackVO>();
            CallBackVO callBackPara = new CallBackVO();
            // 设置用户ID和用户名
            callBackPara.setUserId(userId.toString());
            callBackPara.setUserName(userName);

            String treasureShopId = null;

            // 判断订单号是否为整数，用于区分不同的查询逻辑
            if (PayToolUtil.isInteger(orderId)) {
                // 查询商品价格和名称
                GoodsDTO queryGoodsPara = payConsumeTradeBackMapper.queryGoodsPriceAndName(orderId, userId);
                BigDecimal paymentAmount = queryGoodsPara.getTradePaymentAmount();
                // 根据整数订单号查询宝藏商店ID
                treasureShopId = orderServiceClient.queryTreasureShopIdByInt(orderId);
                // 设置订单全额和商店ID
                callBackPara.setOrderFullAmount(paymentAmount.toString());
                callBackPara.setShopId(treasureShopId);
                callBackParaList.add(callBackPara);
            } else {
                // 查询消费记录列表
                List<RecordVO> rp = payConsumeRecordMapper.queryRecordParaList(orderId);
                for (int i = 0; i < rp.size(); i++) {
                    callBackPara = new CallBackVO();
                    // 设置用户名和用户ID
                    callBackPara.setUserName(userName);
                    callBackPara.setUserId(userId.toString());
                    // 根据消费记录中的订单号列表，查询每个订单对应的宝藏商店ID
                    String str[] = rp.get(i).getRecordPayorder().split(",");
                    List<String> shopIdList = Arrays.asList(str);
                    for (int j = 0; j < shopIdList.size(); j++) {
                        treasureShopId = orderServiceClient.queryTreasureShopIdByInt(shopIdList.get(j));
                        callBackPara.setShopId(treasureShopId);
                    }
                    // 设置订单全额
                    callBackPara.setOrderFullAmount(rp.get(i).getRecordMoney().toString());
                    callBackParaList.add(callBackPara);
                }
            }
        } catch (Exception e) {
            // 记录异常信息
            log.info("-----treasureOrder-----" + e.getMessage());
        }
        return callBackParaList;
    }


    @Override
    public String queryOrderId(String orderId) {
        return orderServiceClient.queryOrderId(orderId);
    }
}
