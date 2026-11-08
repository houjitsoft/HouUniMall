package com.houjiang.payment.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.kit.StrKit;
import com.jfinal.weixin.sdk.kit.PaymentKit;
import com.houjiang.commons.config.ALiPayConfig;
import com.houjiang.commons.config.WXConfigUtil;
import com.houjiang.commons.config.WXPayConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.order.ConsumeRecordByBusinessDTO;
import com.houjiang.commons.dto.order.ConsumeRecordParaDTO;
import com.houjiang.commons.dto.order.RefundParamDTO;
import com.houjiang.commons.dto.payment.AccountPeriodParamDTO;
import com.houjiang.commons.dto.payment.GroupBookingParamDTO;
import com.houjiang.commons.dto.payment.GroupBookingTradeDTO;
import com.houjiang.commons.dto.payment.RefundAccountParamDTO;
import com.houjiang.commons.dto.shop.ShopSalesStatisticsDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.base.BaseCurrencySymbolVO;
import com.houjiang.commons.vo.order.OrderBaseVO;
import com.houjiang.commons.vo.payment.AccountShopBillDetailsVO;
import com.houjiang.commons.vo.shop.AccountPeriodVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.dto.FinancialDetailsDTO;
import com.houjiang.payment.entity.PayConsumeRecordEntity;
import com.houjiang.payment.entity.PayConsumeTradeEntity;
import com.houjiang.payment.entity.PayConsumeWithdrawEntity;
import com.houjiang.payment.feign.BaseServiceClient;
import com.houjiang.payment.feign.MerchantServiceClient;
import com.houjiang.payment.feign.OrderServiceClient;
import com.houjiang.payment.mapper.*;
import com.houjiang.payment.service.PayConsumeRecordService;
import com.houjiang.payment.service.PaypalService;
import com.houjiang.payment.service.StripePaymentService;
import com.houjiang.payment.vo.ComsumeRecordParaVO;
import jakarta.annotation.Resource;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName PayConsumeRecordServiceImpl
 * @Description 交易明细表-账户收支明细-资金流水表-账户金额变化流水
 * @Author Amos
 * @Date 2023-05-31 15:36:55
 * @Version 1.0
 * @author Amos
 */
@Service
public class PayConsumeRecordServiceImpl extends ServiceImpl<PayConsumeRecordMapper, PayConsumeRecordEntity>
        implements PayConsumeRecordService {

    @Resource
    private PayConsumeTradeMapper payConsumeTradeMapper;

    @Resource
    private PayConsumeWithdrawMapper payConsumeWithdrawMapper;

    @Resource
    private PayUserResourceMapper payUserResourceMapper;

    @Autowired
    private BaseServiceClient baseServiceClient;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private MerchantServiceClient merchantServiceClient;

    @Autowired
    private PaypalService paypalService;
    @Autowired
    private StripePaymentService stripePaymentService;
    @Resource
    private PayConfigMapper payConfigMapper;

    @Override
    public boolean insertByBusiness(ConsumeRecordByBusinessDTO crb) {
        // 从CRB对象中获取卖家ID
        String sellerId = crb.getSellId();
        // 从CRB对象中获取用户名
        String userName = crb.getUserName();
        // 从CRB对象中获取总支付金额
        BigDecimal paymentAmout = crb.getOrderCashPayment();

        // 嘉币

        // 劲豆
        // 获取当前日期字符串
        String tradeDate = DateUtils.getNowDateToString();
        // 获取当前年份字符串
        String tradeYear = DateUtils.getYear();
        // 获取当前月份字符串
        String tradeMonth = DateUtils.getMonth();
        // 获取当前日字符串
        String tradeDay = DateUtils.getDay();
        // 设置交易标题为"购物"
        String tradeTitle = "购物";
        // 从CRB对象中获取商品名称列表
        List<String> goodsName = crb.getGoodsName();
        // 选取商品名称列表中的第一个作为交易描述
        String tradeDesc = goodsName.get(0);
        // 获取当前日期字符串作为记录时间
        String recordTime = DateUtils.getNowDateToString();
        // 设置交易类型ID为1，代表某种特定类型的交易
        Integer tradeTypeId = 1;
        // 设置用户类型为1，代表收款方
        //1-收款方 2-付款方
        Integer userType = 1;
        // 设置记录状态为1，代表某种特定的状态
        Integer recordStatus = 1;
        if (CommonType.payType.PAY_TYPE_PRODUCT_VOUCHER.getCode().equals(crb.getPayCode())) {
            // 待发货
            recordStatus = 3;
        }
        // 设置当前订单是否属于当前用户，2代表不属于
        //判断当前订单是否属于当前用户 1 是 2 否
        Integer isCurrentUser = 2;
        // 从CRB对象中获取订单号
        // 实际单号
        String recordPayorderId = crb.getOrderId();
        // 从CRB对象中获取支付号
        // 支付号
        String paymentNumber = crb.getPaymentNumber();
        // 调用底层Mapper，尝试向卖家插入消费记录，返回操作是否成功的标志
        boolean isInsertSellerSuccess = this.baseMapper.insertConsumeRecordToSeller(paymentNumber, sellerId, userName, paymentAmout, crb.getCustomerType(), tradeDate, tradeYear, tradeMonth, tradeDay, tradeTitle, tradeDesc, recordTime, tradeTypeId, userType, recordStatus, recordPayorderId, isCurrentUser);
        // 根据插入操作的成功与否，返回对应的布尔值
        if (isInsertSellerSuccess) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean insertByBuyer(ConsumeRecordParaDTO crp) {
        // 从CRP对象中获取用户ID，用于后续记录消费信息
        String userId = crp.getUserId();
        // 从CRP对象中获取用户姓名，用于后续记录消费信息
        String userName = crp.getUserName();
        // 从CRP对象中获取总支付金额，用于后续记录消费信息
        BigDecimal paymentAmout = crp.getOrderCashPayment();

        // 嘉币
        // 劲豆

        // 获取当前日期，用于记录交易日期
        String tradeDate = DateUtils.getNowDateToString();
        // 获取当前年份，用于细分交易时间
        String tradeYear = DateUtils.getYear();
        // 获取当前月份，用于细分交易时间
        String tradeMonth = DateUtils.getMonth();
        // 获取当前日，用于细分交易时间
        String tradeDay = DateUtils.getDay();
        // 设置交易标题为"购物"
        String tradeTitle = "购物";
        // 从CRP对象中获取商品名称列表，用于后续记录消费详情
        List<String> goodsName = crp.getGoodsName();
        // 选取第一个商品名称作为交易描述
        String tradeDesc = goodsName.get(0);
        // 获取当前日期，用于记录消费记录的时间
        String recordTime = DateUtils.getNowDateToString();
        // 设置交易类型ID为1，代表购物消费
        Integer tradeTypeId = 1;
        // 设置用户类型为2，代表付款方
        //1-收款方 2-付款方
        Integer userType = 2;
        // 设置消费记录状态为1，代表正常
        Integer recordStatus = 1;
        if (CommonType.payType.PAY_TYPE_PRODUCT_VOUCHER.getCode().equals(crp.getPayCode())) {
            // 待发货
            recordStatus = 3;
        }
        // 设置当前订单是否属于当前用户，这里假设均为1，即属于
        //判断当前订单是否属于当前用户 1 是 2 否
        Integer isCurrentUser = 1;
        // 从CRP对象中获取订单号，用于后续关联订单和消费记录
        // 实际单号
        String recordPayorderId = crp.getOrderId();
        // 从CRP对象中获取支付号，用于后续关联支付和消费记录
        // 支付号
        String paymentNumber = crp.getPaymentNumber();
        // 尝试插入消费记录到数据库，返回是否插入成功
        boolean isInsertBuyerSuccess = this.baseMapper.insertBuyUserConsumeRecord(paymentNumber, userId, userName, paymentAmout, crp.getCustomerType(), tradeDate, tradeYear, tradeMonth, tradeDay, tradeTitle, tradeDesc, recordTime, tradeTypeId, userType, recordStatus, recordPayorderId, isCurrentUser);
        // 根据插入结果返回true或false
        if (isInsertBuyerSuccess) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public ResponseResult queryDealRecord(Map<Object, Object> map) {
        // 从请求参数中获取用户令牌
        String token = (String) map.get("token");
        // 使用令牌解析用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        // 获取用户ID
        String userId = ui.getUserId();
        try {
            // 从请求参数中获取交易类型ID
            Integer tradeTypeId = (Integer) map.get("tradeTypeId");
            // 根据用户ID和交易类型ID查询最近的交易记录
            List<ComsumeRecordParaVO> comsumeRecordParaList = this.baseMapper.queryRecentDealRecord(userId, tradeTypeId);
            // 遍历交易记录列表
            for (ComsumeRecordParaVO cp : comsumeRecordParaList) {
                // 获取订单号
                String orderId = cp.getRecordPayorder();
                // 如果交易类型不是5，则设置订单退款和退货状态为0
                if (5 != cp.getTradeTypeId()) {
                    cp.setOrderRefundStatus(0);
                    cp.setOrderReturnStatus(0);
                } else {
                    // 如果交易类型是5，查询对应的退款订单号
                    //查询退单号
                    String refundOrderNum = orderServiceClient.queryRefundOrderNum(orderId);
                    // 设置订单号为退款订单号
                    cp.setOrderId(refundOrderNum);

                }
            }
            // 返回查询结果成功的响应
            return ResponseResult.success(comsumeRecordParaList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 返回系统错误的响应
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());

    }

    /**
     * 删除交易记录
     *
     * @param map 包含令牌和记录ID的映射对象
     * @return 删除操作的结果
     * <p>
     * 本方法通过解析令牌获取用户信息，并验证令牌的有效性。
     * 如果令牌无效，返回相应的错误结果；如果令牌有效，
     * 则根据用户ID和记录ID更新交易记录的删除状态。
     */
    @Override
    public ResponseResult deleteDealRecord(Map<Object, Object> map) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 验证令牌是否有效
        if (null == userInfo) {
            // 令牌无效时，返回错误结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        // 提取记录ID
        Integer recordId = (Integer) map.get("recordId");
        // 更新交易记录的删除状态
        this.baseMapper.updateRecordDelete(userInfo.getUserId(), recordId);
        // 返回成功结果
        return ResponseResult.success();
    }

    /**
     * 根据传入的参数映射查询交易记录详情。
     *
     * @param map 一个包含查询参数的对象映射。每个参数的具体含义取决于实现。
     * @return ResponseResult 对象，包含查询结果。在当前实现中，由于方法未完成，返回null。
     */
    @Override
    public ResponseResult queryDealRecordDetails(Map<Object, Object> map) {
        return null;
    }

    @Override
    public ResponseResult updateCancelStatus(String orderId) {
        // 尝试更新支付消费记录的状态为取消
        boolean isUpdateRecord = this.baseMapper.update(null, new LambdaUpdateWrapper<PayConsumeRecordEntity>()
                .eq(PayConsumeRecordEntity::getOrderId, orderId)
                .set(PayConsumeRecordEntity::getRecordStatus, CommonType.recordStatus.CANCEL.getCode())) == CommonStatusEnum.SUCCESS.getCode();

        // 尝试更新支付交易记录的状态为取消
        boolean isUpdateTrade = payConsumeTradeMapper.update(null, new LambdaUpdateWrapper<PayConsumeTradeEntity>()
                .eq(PayConsumeTradeEntity::getOrderId, orderId)
                .set(PayConsumeTradeEntity::getOrderStateId, CommonType.recordStatus.CANCEL.getCode())) == CommonStatusEnum.SUCCESS.getCode();

        // 检查支付消费记录和支付交易记录是否都成功更新为取消状态
        if (isUpdateRecord && isUpdateTrade) {
            // 如果都成功更新，返回成功的响应结果
            return ResponseResult.success();
        } else {
            // 如果有任何一个更新失败，返回失败的响应结果
            return ResponseResult.fail();
        }

    }

    @Override
    public ResponseResult change(String orderId) {
        // 根据订单号更新支付消费记录的状态为确认收货
        boolean isUpdateRecord = this.baseMapper.update(null, new LambdaUpdateWrapper<PayConsumeRecordEntity>()
                .eq(PayConsumeRecordEntity::getRecordPayorder, orderId)
                .set(PayConsumeRecordEntity::getRecordStatus, CommonType.recordStatus.CONFIRM_RECEIPT.getCode())) == CommonStatusEnum.SUCCESS.getCode();

        // 根据订单号更新支付交易记录的状态为确认收货
        boolean isUpdateTrade = payConsumeTradeMapper.update(null, new LambdaUpdateWrapper<PayConsumeTradeEntity>()
                .eq(PayConsumeTradeEntity::getOrderId, orderId)
                .set(PayConsumeTradeEntity::getOrderStateId, CommonType.recordStatus.CONFIRM_RECEIPT.getCode())) == CommonStatusEnum.SUCCESS.getCode();

        // 如果支付消费记录和支付交易记录都更新成功，则返回成功响应，否则返回失败响应
        if (isUpdateRecord && isUpdateTrade) {
            return ResponseResult.success();
        } else {
            return ResponseResult.fail();
        }

    }

    @Override
    public ResponseResult insertRecord(Map<String, Map<String, String>> returnMap) {
        // 根据用户类型从返回的映射中获取用户和卖家的信息
        Map<String, String> userMap = returnMap.get("user");
        Map<String, String> sellerMap = returnMap.get("seller");

        // 计算卖家的记录金额的绝对值，确保金额为正数
        Double money = Math.abs(Double.parseDouble(sellerMap.get("recordMoney")));

        // 为买家记录设置交易类型为收入
        //买家
        userMap.put("plusOrMinus", "1");
        // 为卖家记录设置交易类型为支出
        //卖家
        sellerMap.put("plusOrMinus", "0");
        // 更新卖家的记录金额
        sellerMap.put("recordMoney", String.valueOf(money));

        // 分别插入用户和卖家的交易记录到数据库
        Integer one = this.baseMapper.insertRecord(userMap);
        Integer two = this.baseMapper.insertRecord(sellerMap);

        // 如果任一插入操作失败，则回滚事务并返回失败响应
        if (!CommonType.commonStatus.YES.getCode().equals(one) || !CommonType.commonStatus.YES.getCode().equals(two)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseResult.fail();
        }

        // 所有操作成功，返回成功响应
        return ResponseResult.success();

    }

    @Override
    public void updateRefuse(Map<String, String> feignMap) {
        this.baseMapper.updateRecordRefuse(feignMap);
    }

    @Override
    public ResponseResult querySalesStatistics(ShopSalesStatisticsDTO param) {
        // 检查查询参数中是否未设置开始时间和结束时间
        if (param.getStartTime() == "" && param.getEndTime() == "") {
            // 使用SimpleDateFormat格式化日期时间
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 获取当前日期时间作为查询的结束时间
            String finishTime = dateformat.format(new Date());
            // 获取一个Calendar实例用于日期时间操作
            Calendar c = Calendar.getInstance();
            // 设置Calendar时间为当前时间的前一个月
            // 过去1个月
            c.setTime(new Date());
            c.add(Calendar.MONTH, -1);
            // 获取前一个月的最后时间，并格式化为字符串
            Date m3 = c.getTime();
            String startTime = dateformat.format(m3);
            // 设置查询参数的开始时间和结束时间
            param.setEndTime(finishTime);
            param.setStartTime(startTime);
        }

        // 使用token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        // 如果用户信息存在
        if (null != userInfo) {
            // 调用方法获取统计数据，并返回分页结果
            IPage<ShopSalesStatisticsDTO> page = getStatistics(param);
            return ResponseResult.success(PageUtils.getPage(page));
        }
        // 如果用户信息不存在，返回认证失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    @Override
    public ResponseResult querySalesStatisticsExcel(ShopSalesStatisticsDTO param) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            String[] title = {"店铺名称", "卡支付金额", "余额支付金额", "微信", "支付宝", "优惠券", "红包", "运费",
                    "退款", "合计"};
            if (param.getStartTime() == "" && param.getEndTime() == "") {
                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String finishTime = dateformat.format(new Date());
                Calendar c = Calendar.getInstance();
                // 过去1个月
                c.setTime(new Date());
                c.add(Calendar.MONTH, -1);
                Date m3 = c.getTime();
                String startTime = dateformat.format(m3);
                param.setEndTime(finishTime);
                param.setStartTime(startTime);
            }
            List<Map<String, String>> list = Lists.newArrayList();
            param.setLimit(6000);
            IPage<ShopSalesStatisticsDTO> page = getStatistics(param);
            List<ShopSalesStatisticsDTO> records = page.getRecords();
            for (ShopSalesStatisticsDTO res : records) {
                HashMap<String, String> map = com.google.common.collect.Maps.newHashMap();
                map.put("店铺名称", res.getShopName());
                map.put("卡支付金额", BigDecimal.valueOf(res.getCardPayment()).setScale(2, RoundingMode.HALF_UP) + "");
                map.put("余额支付金额", BigDecimal.valueOf(res.getBalancePayment()).setScale(2, RoundingMode.HALF_UP) + "");
                map.put("微信", BigDecimal.valueOf(res.getWeChatTwoPayment()).setScale(2, RoundingMode.HALF_UP) + "");
                map.put("支付宝", BigDecimal.valueOf(res.getAlipayPayment()).setScale(2, RoundingMode.HALF_UP) + "");
                map.put("优惠券", BigDecimal.valueOf(res.getVoucherPrice()).setScale(2, RoundingMode.HALF_UP) + "");
                map.put("红包", BigDecimal.valueOf(res.getOrderRptPrice()).setScale(2, RoundingMode.HALF_UP) + "");
                map.put("运费", BigDecimal.valueOf(res.getShippingFee()).setScale(2, RoundingMode.HALF_UP) + "");
                map.put("退款", BigDecimal.valueOf(res.getRefundMoney()).setScale(2, RoundingMode.HALF_UP) + "");
                map.put("合计", res.getTotalPayment() + "");
                list.add(map);
            }
            // excel文件名
            //此处的key为每个sheet的名称，一个excel中可能有多个sheet页 此处key对应每一列的标题 该list为每个sheet页的数据
            Map<String, List<Map<String, String>>> map = Maps.newHashMap();
            map.put("店铺销售统计", list);
            //此处数组为需要合并的列，可能有的需求是只需要某些列里面相同内容合并
            String url = ExcelsUtil.createExcel(title, map, new int[]{0});

            return ResponseResult.success(url);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    @Override
    public ResponseResult platformQueryDetails(FinancialDetailsDTO param) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            try {
                // 初始化返回标志
                Integer returnFlag = 0;
                // 将请求参数转换为Map
                //将对象转换为map
                Map data = ParseMapUtils.beanToMap(param);
                // 分页查询消费记录
                //分页查询数据
                IPage<PayConsumeRecordEntity> page = this.baseMapper.platformQueryDetails(new Query<PayConsumeRecordEntity>().getPage(data), param);
                // 判断查询结果是否存在
                if (null != page.getRecords() && page.getRecords().size() > 0) {
                    // 获取脱敏开关配置值
                    String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
                    // 遍历查询结果进行脱敏处理或其它业务逻辑
                    for (PayConsumeRecordEntity entity : page.getRecords()) {
                        // 根据配置决定是否进行脱敏处理
                        if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                            BeanUtils.copyProperties(DesensitizationUtil.Desensitization(entity, PayConsumeRecordEntity.class), entity);
                        }
                        // 根据订单号前缀判断订单类型并设置详细类型
                        String orderId = entity.getRecordPayorder();
                        //通过是否R开头以区分充值单号和订单号
                        if (null != orderId && !orderId.startsWith("R") && !orderId.startsWith("W") && !orderId.startsWith("T")) {
                            // 设置为购物消费类型
                            //1-充值;2-提现;3-购物消费;4-转账;
                            entity.setDetailType(3);
                            // 查询关联的订单信息
                            OrderBaseVO orderInfoEntity = orderServiceClient.queryOrderInfoFegin(orderId);
                            // 如果订单信息不存在，则创建一个空的VO对象
                            if (null == orderInfoEntity) {
                                orderInfoEntity = new OrderBaseVO();
                            }
                            // 设置消费记录的相关信息基于订单信息
                            entity.setRecordPaytime(orderInfoEntity.getPaymentTime());
                            entity.setOrderStatus(orderInfoEntity.getOrderStatus());
                            Integer virtualGoods = orderInfoEntity.getVirtualGoods();
                            Integer orderIsShopTake = orderInfoEntity.getOrderIsShopTake();
                            // 根据订单属性设置消费记录的订单类型
                            if (null != orderIsShopTake && 1 == orderIsShopTake) {
                                entity.setOrderType(3);
                            } else {
                                if (null != virtualGoods && 0 == virtualGoods) {
                                    entity.setOrderType(2);
                                    entity.setPastDue(orderInfoEntity.getPastDue());
                                    entity.setIsCancel(orderInfoEntity.getIsCancel());
                                } else {
                                    entity.setOrderType(1);
                                }
                            }
                            // 设置退款退货状态
                            Integer returnStatus = orderInfoEntity.getOrderReturnStatus() == null ? 0 : Integer.valueOf(orderInfoEntity.getOrderReturnStatus());
                            Integer refundStatus = orderInfoEntity.getOrderRefundStatus() == null ? 0 : Integer.valueOf(orderInfoEntity.getOrderRefundStatus());

                            entity.setReturnStatus(returnStatus);
                            entity.setRefundStatus(refundStatus);
                            // 根据退款退货状态计算返回标志
                            if (2 == returnStatus || 2 == refundStatus) {
                                BigDecimal returnAmount = new BigDecimal(orderInfoEntity.getOrderRefundAmount());
                                BigDecimal orderAmount = orderInfoEntity.getOrderPaymentAmount();
                                if (orderAmount.compareTo(returnAmount) > 0) {
                                    returnFlag = 2;
                                } else if ((orderAmount.compareTo(returnAmount) == 0)) {
                                    returnFlag = 1;
                                }
                            }
                            entity.setReturnFlag(returnFlag);
                        } else if (null != orderId && orderId.startsWith("R")) {
                            // 设置为充值类型
                            //1-充值;2-提现;3-购物消费;4-转账;
                            entity.setDetailType(1);
                            // 设置充值状态为成功
                            //充值成功
                            entity.setRecordStatus(1);
                        } else if (null != orderId && orderId.startsWith("W")) {
                            // 设置为提现类型
                            entity.setDetailType(2);
                            // 根据提现订单状态设置消费记录状态
                            Integer start = payConsumeWithdrawMapper.selectOne(new LambdaQueryWrapper<PayConsumeWithdrawEntity>()
                                    .eq(PayConsumeWithdrawEntity::getOrderid, orderId)).getIsSucceed();
                            //0 待审核  2 打款完成 3 通过 4不通过
                            entity.setRecordStatus(start);
                        } else if (null != orderId && orderId.startsWith("T")) {
                            // 设置为转账类型
                            //1-充值;2-提现;3-购物消费;4-转账;
                            entity.setDetailType(4);
                        }
                    }
                }
                // 返回处理后的分页数据
                return ResponseResult.success(PageUtils.getPage(page));
            } catch (IllegalAccessException e) {
                // 打印异常信息
                e.printStackTrace();
            }
        }
        // 如果用户信息获取失败，返回非法token的错误信息
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }

    /**
     * 根据传入的结算信息，插入结算记录。
     *
     * @param map 包含结算相关信息的Map对象，如结算ID、结算金额等。
     * @return 插入操作是否成功的标志。
     */
    @Override
    public boolean insertSettlementRecord(Map<String, Object> map) {
        // 从map中提取token
        String token = (String) map.get("token");
        boolean flag = false;
        try {
            // 根据token解析用户信息
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
            // 如果用户信息为空，则直接返回false
            if (ui == null) {
                return false;
            }
            // 提取用户ID和用户名
            String userId = ui.getUserId();
            String userName = ui.getUserName();
            // 从map中提取结算ID和结算金额
            // 结算编号
            String settlementId = (String) map.get("settlementId");
            // 结算金额
            String settlementMoney = (String) map.get("settlementMoney");
            // 处理结算类型，如果未提供，则默认为1
            Integer settlementType = null == map.get("settlementType") ? 1 : Integer.parseInt(map.get("settlementType") + "");
            // 创建BigDecimal对象处理结算金额，确保精确计算
            BigDecimal money = new BigDecimal(settlementMoney);
            // 获取当前日期信息，用于记录结算记录的时间
            String recordDate = DateUtils.getYear() + "-" + DateUtils.getMonth() + "-" + DateUtils.getDay();
            String recordYear = DateUtils.getYear();
            String recordMonth = DateUtils.getMonth();
            String recordDay = DateUtils.getDay();
            // 设置记录标题和描述
            String recordTitle = "商家结算";
            String recordDesc = "结算";
            // 获取当前时间
            String recordTime = DateUtils.getTime();
            // 设置交易类型、用户类型、记录状态等固定值
            Integer tradeTypeId = 11;
            Integer userType = 1;
            Integer recordStatus = 6;
            // 记录订单号为结算ID
            String recordPayorder = settlementId;
            // 初始化加减标志
            Integer plusOrMinus = null;
            // 判断结算金额是否为负，确定加减标志
            BigDecimal judge = new BigDecimal(0);
            if (money.compareTo(judge) < 0) {
                plusOrMinus = 0;
            } else {
                plusOrMinus = 1;
            }
            // 准备插入数据库的结算记录信息
            Map<String, Object> mapRecord = new HashMap<>();
            mapRecord.put("settlementId", settlementId);
            mapRecord.put("settlementType", settlementType);
            mapRecord.put("userId", userId);
            mapRecord.put("userName", userName);
            mapRecord.put("settlementMoney", settlementMoney);
            mapRecord.put("recordDate", recordDate);
            mapRecord.put("recordYear", recordYear);
            mapRecord.put("recordMonth", recordMonth);
            mapRecord.put("recordDay", recordDay);
            mapRecord.put("recordTitle", recordTitle);
            mapRecord.put("recordDesc", recordDesc);
            mapRecord.put("recordTime", recordTime);
            mapRecord.put("tradeTypeId", tradeTypeId);
            mapRecord.put("userType", userType);
            mapRecord.put("recordStatus", recordStatus);
            mapRecord.put("recordPayorder", recordPayorder);
            mapRecord.put("plusOrMinus", plusOrMinus);
            // 尝试插入结算记录到数据库
            boolean isInsertSuccess = this.baseMapper.insertSettlementRecord(mapRecord);
            // 如果插入成功，设置标志为true
            if (isInsertSuccess) {
                flag = true;
            }
        } catch (Exception e) {
            // 捕获异常，打印堆栈跟踪，并设置当前事务回滚
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        // 返回插入操作是否成功的标志
        return flag;
    }

    @Override
    public ResponseResult recentTransactionOrder(Map<String, Object> map) {
        // 从map中获取token
        String token = (String) map.get("token");
        // 初始化返回标志为0
        Integer returnFlag = 0;
        // 检查token在Redis中是否存在
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            // 从Redis中获取userId
            String userId = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USERID + token);
            try {
                // 根据userId查询消费记录
                IPage<PayConsumeRecordEntity> spct = this.page(new Query<PayConsumeRecordEntity>().getPage(map),
                        new LambdaQueryWrapper<PayConsumeRecordEntity>().eq(PayConsumeRecordEntity::getUserId, userId)
                                .between(PayConsumeRecordEntity::getUserType, 1, 2)
                                .orderByDesc(PayConsumeRecordEntity::getRecordTime));
                // 如果查询结果为空，返回订单为空的响应
                if (spct.getRecords() == null) {
                    return ResponseResult.success(BizCodeEnume.ORDER_IS_NULL);
                }

                // 遍历消费记录，进行细节处理
                for (PayConsumeRecordEntity financialDetail : spct.getRecords()) {
                    String recordPayorder = financialDetail.getOrderId();
                    // 根据订单号前缀判断订单类型，并设置详情类型
                    //通过是否R开头以区分充值单号和订单号
                    if (null != recordPayorder && !recordPayorder.startsWith("R") && !recordPayorder.startsWith("W") && !recordPayorder.startsWith("T")) {
                        financialDetail.setDetailType(3);
                        // 查询订单基本信息，如果不存在则创建新实例
                        OrderBaseVO orderInfoEntity = orderServiceClient.queryOrderInfoFegin(financialDetail.getRecordPayorder());
                        if (null == orderInfoEntity) {
                            orderInfoEntity = new OrderBaseVO();
                        }
                        // 根据订单基本信息，设置消费记录的状态和类型
                        financialDetail.setOrderStatus(orderInfoEntity.getOrderStatus());
                        Integer virtualGoods = orderInfoEntity.getVirtualGoods();
                        Integer orderIsShopTake = orderInfoEntity.getOrderIsShopTake();
                        if (null != orderIsShopTake && 1 == orderIsShopTake) {
                            financialDetail.setOrderType(3);
                        }
                        if (null != virtualGoods && 0 == virtualGoods) {
                            financialDetail.setOrderType(2);
                            financialDetail.setPastDue(orderInfoEntity.getPastDue());
                            financialDetail.setIsCancel(orderInfoEntity.getIsCancel());
                        } else {
                            financialDetail.setOrderType(1);
                        }
                        // 设置退款和退货状态，如果不存在则默认为0
                        Integer returnStatus = orderInfoEntity.getOrderReturnStatus() != null ? Integer.valueOf(orderInfoEntity.getOrderReturnStatus()) : null;
                        Integer refundStatus = orderInfoEntity.getOrderRefundStatus() != null ? Integer.valueOf(orderInfoEntity.getOrderRefundStatus()) : null;
                        if (null == returnStatus) {
                            returnStatus = 0;
                        }
                        if (null == refundStatus) {
                            refundStatus = 0;
                        }

                        financialDetail.setReturnStatus(returnStatus);
                        financialDetail.setRefundStatus(refundStatus);
                        // 根据退款和退货状态，更新返回标志
                        if (2 == returnStatus || 2 == refundStatus) {
                            BigDecimal returnAmount = BigDecimal.valueOf(orderInfoEntity.getOrderRefundAmount());
                            BigDecimal orderAmount = orderInfoEntity.getOrderPaymentAmount();
                            if (orderAmount.compareTo(returnAmount) > 0) {
                                returnFlag = 2;
                            } else if ((orderAmount.compareTo(returnAmount) == 0)) {
                                returnFlag = 1;
                            }
                        }
                        financialDetail.setReturnFlag(returnFlag);
                    } else if (null != recordPayorder && recordPayorder.startsWith("R")) {
                        financialDetail.setDetailType(1);
                        // 充值订单，设置为成功状态
                        financialDetail.setRecordStatus(1);
                    } else if (null != recordPayorder && recordPayorder.startsWith("W")) {
                        financialDetail.setDetailType(2);
                        // 提现订单，根据审核状态设置订单状态
                        Integer start = payConsumeWithdrawMapper.selectOne(new LambdaQueryWrapper<PayConsumeWithdrawEntity>()
                                .eq(PayConsumeWithdrawEntity::getOrderid, recordPayorder)).getIsSucceed();
                        financialDetail.setRecordStatus(start);
                    } else if (null != recordPayorder && recordPayorder.startsWith("T")) {
                        financialDetail.setDetailType(4);
                    }
                    // 格式化订单金额
                    BigDecimal recordMoney = new BigDecimal(financialDetail.getRecordMoney()).setScale(2, RoundingMode.HALF_UP);
                    String money = recordMoney.toString();
                    financialDetail.setMoney(money);
                }
                // 返回处理后的消费记录页面
                return ResponseResult.success(PageUtils.getPage(spct));
            } catch (Exception e) {
                // 异常情况下，返回失败响应并打印异常信息
                e.printStackTrace();
                return ResponseResult.fail(2, e.getMessage());
            }
        }
        // 如果token无效，返回失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);

    }

    @Override
    public boolean waitShipmentsRefund(RefundParamDTO refundPara) {
        //查询支付配置信息
        // 根据支付单号 查询当前订单 店铺是否是品牌店铺 如果是用品牌店铺 微信值参数验签
        // 如果是是品牌店铺的 一个支付单号 只会对应一个店铺id
        // 查询当前店铺是否是品牌 店铺  如果是返回shopId
        Integer shopId = payUserResourceMapper.queryshopIdByOutTradeNo(refundPara.getOrderId());
        if (shopId != null) {
            //初始化品牌店铺支付参数
            initWXShopPayConfig(shopId);
        } else {
            //查询支付配置信息
            selectYFPayConfig();
        }
        System.out.println("======进入待发货退款=====");
        boolean flag = false;
        String orderId = refundPara.getOrderId();
        String refundOrderId = refundPara.getReturnCode();
        Integer payCode = refundPara.getPayCode();
        Integer orderFrom = refundPara.getOrderFrom();
        String paymentOtherNumber = refundPara.getPaymentOtherNumber();
        // 退款共用
        List<GroupBookingParamDTO> gbpList = this.baseMapper.queryRefundConsumeRecordMoney(orderId);

        String userId = null;
        String uOrderId = null;
        BigDecimal money = refundPara.getReturnCash();
        // 退款金额
        // 总金额
        BigDecimal allMoney = BigDecimal.ZERO;

        for (int i = 0; i < gbpList.size(); i++) {
            Integer type = 1;
            Integer userType = 3;
            if (userType.intValue() == gbpList.get(i).getUserType()) {
                // 当userType为 3 时，此订单为合并支付
                userId = gbpList.get(i).getUserId();
                // 合并支付总金额
                allMoney = gbpList.get(i).getRecordMoney();
            } else if (type.intValue() == gbpList.get(i).getUserType()) {
                // 其他情况，正常取值
                userId = gbpList.get(i).getUserId();
                uOrderId = gbpList.get(i).getOrderId();
                // 计算订单总金额
                allMoney = this.baseMapper.calculateAllOrderMoney(uOrderId);
            }
        }
        if (null == allMoney) {
            allMoney = BigDecimal.ZERO;
        }

        if (payCode == 1) {
            // 账户余额
            flag = this.baseMapper.updateUserMoneyResource(userId, money);
        } else if (payCode == 9) {
            // 蜂卡
        } else if (payCode == 10) {
            // 蜂卡+支付宝

        } else if (payCode == 11) {

            // 组合(充值+微信)
        } else if (payCode == 2) {
            // 支付宝
            flag = aliPayRefund(paymentOtherNumber, money);
        } else if (payCode == 3) {
            // 微信
            flag = weChatRefund(paymentOtherNumber, refundOrderId, allMoney, money, orderFrom);
        } else if (payCode == 6) {
            // 充值卡
            flag = this.baseMapper.updateUserRechargeCardMoneyResource(userId, money);
        } else if (payCode == 7) {
            // 组合(充值+支付宝)

        } else if (payCode == 8) {
            // 组合(充值+微信)

        } else if (payCode == 12) {
            //线下付款
            flag = true;

        } else if (payCode == 13) {
            //账期付款
            String accountingPeriodId = orderServiceClient.queryAccountingPeriodId(orderId);
            RefundAccountParamDTO refundAccountParam = new RefundAccountParamDTO();
            refundAccountParam.setAccountingPeriodId(accountingPeriodId);
            refundAccountParam.setRefundPrice(money);
            flag = updateRefundAccountPeriod(refundAccountParam);
        } else if (payCode == 14) {
            // 账户余额
            flag = this.baseMapper.updateUserMoneyResource(userId, money);
        } else if (payCode == 15) {
            // 购物卡
            flag = this.baseMapper.updateUserRechargeCardMoneyResource(userId, money);
        } else if (payCode == 16) {
            BaseCurrencySymbolVO baseCurrencySymbolVO = orderServiceClient.queryOrderCurrencyInfo(orderId);
            BigDecimal exchangeRate = baseCurrencySymbolVO.getExchangeRate();
            money = money.multiply(exchangeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            // paypal
            flag = paypalService.refundSale(paymentOtherNumber, money, baseCurrencySymbolVO.getCurrencyCode());
        } else if (payCode == 18) {
            // stripe
            BaseCurrencySymbolVO baseCurrencySymbolVO = orderServiceClient.queryOrderCurrencyInfo(orderId);
            BigDecimal exchangeRate = baseCurrencySymbolVO.getExchangeRate();
            money = money.multiply(exchangeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            Map<Object, Object> para = new HashMap<>();
            para.put("returnAmount", money);
            para.put("tradeNo", paymentOtherNumber);
            para.put("currencyCode", baseCurrencySymbolVO.getCurrencyCode());

            ResponseResult responseResult = stripePaymentService.stripeReturnAmount(para);
            if (responseResult.getCode() == CommonStatusEnum.SUCCESS.getCode()) {
                flag = true;
            }
        }

        if (flag) {
            // 更新record退款状态
            this.baseMapper.updateOrderRefund(orderId);
            // 更新trade退款状态
            this.baseMapper.updateOrderTrade(orderId);
        }
        return flag;
    }

    @Override
    public void insertConsumeRecordport(Map<String, Object> comRecord) {
        this.baseMapper.insertConsumeRecordportFeign(comRecord);
    }

    public static String postSSL(String url, String data, String certPath, String certPass) {
        String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36";
        HttpsURLConnection conn = null;
        OutputStream out = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            // 加载客户端证书并初始化KeyManagerFactory
            KeyStore clientStore = KeyStore.getInstance("PKCS12");
            clientStore.load(new FileInputStream(certPath), certPass.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(clientStore, certPass.toCharArray());

            // 初始化SSLContext以配置SSL连接
            KeyManager[] kms = kmf.getKeyManagers();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kms, null, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            // 创建HTTPS连接
            URL _url = new URL(url);
            conn = (HttpsURLConnection) _url.openConnection();

            // 配置连接参数
            conn.setConnectTimeout(25000);
            conn.setReadTimeout(25000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // 设置请求头信息
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);

            // 连接服务器
            conn.connect();
            out = conn.getOutputStream();
            out.write(data.getBytes(Charset.forName("UTF-8")));
            out.flush();

            // 读取服务器响应
            inputStream = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 关闭资源
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(inputStream);
            if (conn != null) {
                conn.disconnect();
            }
        }

    }

    public static boolean weChatRefund(String paymentOtherNumber, String refundOrderId, BigDecimal allMoney,
                                       BigDecimal money, Integer orderFrom) {
        boolean flag = false;
        System.out.println("-----uOrderId-----" + paymentOtherNumber);
        if (orderFrom == 2) {
            // 订单来自wap端
            Map<String, String> params = new HashMap<String, String>();
            params.put("appid", WXPayConfig.APP_ID);
            params.put("mch_id", WXPayConfig.MCH_ID);
            params.put("nonce_str", PayToolUtil.create_nonce_str());
            params.put("transaction_id", paymentOtherNumber);
            params.put("out_refund_no", refundOrderId);
            params.put("total_fee", PayToolUtil.getMoney(allMoney.toString()));
            params.put("refund_fee", PayToolUtil.getMoney(money.toString()));
            String sign = PaymentKit.createSign(params, WXPayConfig.API_KEY);
            params.put("sign", sign);

            System.out.println("abcd = weixin chat wap path = " + WXPayConfig.weChatWapPath);
            String xmlResult = postSSL(WXPayConfig.REFUND_URL, PaymentKit.toXml(params), WXPayConfig.weChatWapPath, WXPayConfig.MCH_ID);
            System.out.println(xmlResult);
            Map<String, String> result = PaymentKit.xmlToMap(xmlResult);

            String return_code = result.get("return_code");
            if (StrKit.isBlank(return_code) || !"SUCCESS".equals(return_code)) {
            }
            String result_code = result.get("result_code");
            if (StrKit.isBlank(result_code) || !"SUCCESS".equals(result_code)) {
            } else {
                flag = true;
            }
        } else if (orderFrom == 6) {
            System.out.println("-----进入小程序判断-----");
            Map<String, String> params = new HashMap<String, String>();
            params.put("appid", WXPayConfig.Applet_APP_ID);
            params.put("mch_id", WXPayConfig.MCH_ID);
            params.put("nonce_str", PayToolUtil.create_nonce_str());
            params.put("transaction_id", paymentOtherNumber);
            params.put("out_refund_no", refundOrderId);
            params.put("total_fee", PayToolUtil.getMoney(allMoney.toString()));
            params.put("refund_fee", PayToolUtil.getMoney(money.toString()));
            String sign = PaymentKit.createSign(params, WXPayConfig.API_KEY);
            params.put("sign", sign);


            String xmlResult = postSSL(WXPayConfig.REFUND_URL, PaymentKit.toXml(params), WXPayConfig.weChatWapPath, WXPayConfig.MCH_ID);
            System.out.println(xmlResult);

            Map<String, String> result = PaymentKit.xmlToMap(xmlResult);
            String return_code = result.get("return_code");
            if (StrKit.isBlank(return_code) || !"SUCCESS".equals(return_code)) {
            }
            String result_code = result.get("result_code");
            if (StrKit.isBlank(result_code) || !"SUCCESS".equals(result_code)) {
            } else {
                flag = true;
            }
        } else {

            // 其他端
            Map<String, String> params = new HashMap<String, String>();
            params.put("appid", WXPayConfig.APP_ID);
            params.put("mch_id", WXPayConfig.MCH_ID);
            params.put("nonce_str", PayToolUtil.create_nonce_str());
            params.put("transaction_id", paymentOtherNumber);
            params.put("out_refund_no", refundOrderId);
            params.put("total_fee", PayToolUtil.getMoney(allMoney.toString()));
            params.put("refund_fee", PayToolUtil.getMoney(money.toString()));
            String sign = PaymentKit.createSign(params, WXPayConfig.API_KEY);
            params.put("sign", sign);

            File file = new File(WXPayConfig.weChatWapPath);
            if (file.exists()) {
            }
            String xmlResult = "";
            try {
                System.out.println(WXPayConfig.REFUND_URL);
                System.out.println(PaymentKit.toXml(params));
                System.out.println(WXPayConfig.weChatWapPath);
                System.out.println(WXPayConfig.MCH_ID);
                xmlResult = postSSL(WXPayConfig.REFUND_URL, PaymentKit.toXml(params), WXPayConfig.weChatWapPath, WXPayConfig.MCH_ID);
                System.out.println(xmlResult);
            } catch (Exception e) {
            }

            Map<String, String> result = PaymentKit.xmlToMap(xmlResult);
            String return_code = result.get("return_code");
            if (StrKit.isBlank(return_code) || !"SUCCESS".equals(return_code)) {
            }
            String result_code = result.get("result_code");
            if (StrKit.isBlank(result_code) || !"SUCCESS".equals(result_code)) {
            } else {
                flag = true;
            }
        }
        return flag;
    }

    public static boolean aliPayRefund(String paymentOtherNumber, BigDecimal money) {
        boolean flag = false;
        try {
            // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
            CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
            // 支付宝网关
            certAlipayRequest.setServerUrl(ALiPayConfig.URL);
            // 应用id
            certAlipayRequest.setAppId(ALiPayConfig.APPID);
            // 应用私钥路径
            certAlipayRequest.setPrivateKey(ALiPayConfig.RSA_PRIVATE_KEY);
            // 应用公钥路径
            certAlipayRequest.setCertPath(ALiPayConfig.ALIPAY_PUBLIC_CERT_PATH);
            // 支付宝应用公钥证书路径
            certAlipayRequest.setAlipayPublicCertPath(ALiPayConfig.CERT_PATH);
            // 支付宝证书，根证书路径
            certAlipayRequest.setRootCertPath(ALiPayConfig.ROOT_CERT_PATH);
            //设置签名
            certAlipayRequest.setSignType(ALiPayConfig.SIGNTYPE);
            // 设置签名类型
            certAlipayRequest.setFormat(ALiPayConfig.FORMAT);
            // 设置编码
            certAlipayRequest.setCharset(ALiPayConfig.CHARSET);
            // 调用RSA签名方式
            AlipayClient client = new DefaultAlipayClient(certAlipayRequest);
            // 设置请求参数
            AlipayTradeRefundModel refundModel = new AlipayTradeRefundModel();
            refundModel.setTradeNo(paymentOtherNumber);
            refundModel.setRefundAmount(money.toString());
            refundModel.setRefundReason("超时退款");
            refundModel.setOutRequestNo("FR" + TimeStampUtil.nowtime() + TimeStampUtil.randomLengthByTwo() + "001");
            // 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            request.setBizModel(refundModel);

            AlipayTradeRefundResponse response = client.certificateExecute(request);
            if (response.isSuccess()) {
                flag = true;
            } else {
                flag = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 更新退款账期信息。
     *
     * @param param 退款账期参数DTO，包含需要更新的账期信息。
     * @return 更新成功返回true，更新失败返回false。
     */
    public Boolean updateRefundAccountPeriod(RefundAccountParamDTO param) {
        // 更新账期退款信息
        int row = orderServiceClient.updateRefundAccountPeriod(param);
        // 如果更新行数小于等于0，表示更新失败，返回false
        if (row <= 0) {
            return false;
        }
        // 根据账期ID查询账单详情
        AccountShopBillDetailsVO detail = orderServiceClient.selectDetailsByPeriodId(param.getAccountingPeriodId());
        // 根据剩余金额调整账单状态
        if (detail.getRemainingAccounts().compareTo(BigDecimal.ZERO) == 0) {
            // 如果剩余金额为0，表示全部还款，修改账单状态为2
            orderServiceClient.editAccountStatus(param.getAccountingPeriodId(), 2);
        } else if (detail.getRemainingAccounts().compareTo(BigDecimal.ZERO) == 1 && detail.getAccountStatus() != 3) {
            // 如果剩余金额大于0且账单状态不为逾期，表示部分还款，修改账单状态为1
            //部分还款且不逾期
            orderServiceClient.editAccountStatus(param.getAccountingPeriodId(), 1);
        }
        // 根据账单详情更新账期会员的使用额度和可用额度
        //修改账期会员的使用额度跟可用额度
        //查找账期会员的账期信息
        AccountPeriodVO account = merchantServiceClient.getAccountPeriodByIds(detail.getShopId(), detail.getUserId());
        // 计算更新后的使用额度
        BigDecimal amountUsed = account != null ? account.getAmountUsed().subtract(param.getRefundPrice()) : BigDecimal.ZERO;
        // 初始化可用额度
        BigDecimal availableAmount = BigDecimal.ZERO;
        // 如果账期有限额，更新可用额度
        if (account != null && account.getAccountIsLimit() != 0) {
            availableAmount = account.getAvailableAmount().add(param.getRefundPrice());
        }
        // 计算更新后的月度使用额度
        BigDecimal monthAmountUsed = account != null ? account.getMonthAmountUsed() : BigDecimal.ZERO;
        // 判断是否为当前月份，以决定是否更新月度使用额度
        Boolean isMonth = DateUtils.isMonth(new Date(), detail.getStartTime());
        if (isMonth && account != null) {
            monthAmountUsed = account.getMonthAmountUsed().subtract(param.getRefundPrice());
        }
        // 构造更新账期的参数
        AccountPeriodParamDTO periodParam = new AccountPeriodParamDTO(detail.getShopId(), detail.getUserId(), amountUsed, availableAmount, monthAmountUsed);
        // 更新账期信息
        merchantServiceClient.updateAccountPeriod(periodParam);
        // 更新成功，返回true
        return true;
    }

    /**
     * 初始化 品牌店铺支付信息
     */
    public void initWXShopPayConfig(Integer shopId) {
        // 根据店铺ID查询支付配置信息
        Map<String, String> stringStringMap = payConfigMapper.selectYfPayConfigShop(shopId);
        // 设置微信小程序的APP ID
        WXPayConfig.Applet_APP_ID = stringStringMap.get("Applet_APP_ID");
        // 设置微信小程序的APP SECRET
        WXPayConfig.Applet_APP_SECRET = stringStringMap.get("Applet_APP_SECRET");
        // 设置API密钥
        WXPayConfig.API_KEY = stringStringMap.get("API_KEY");
        // 设置商户号
        WXPayConfig.MCH_ID = stringStringMap.get("MCH_ID");
        // 设置微信H5支付的跳转路径
        WXPayConfig.weChatWapPath = stringStringMap.get("weChatWapPath");
        // 设置支付通知URL
        WXPayConfig.NOTIFY_URL = stringStringMap.get("NOTIFY_URL");
    }

    public void selectYFPayConfig() {

        //微信 pc  h5
        String paymentMode = "00";
        Map<String, String> outMap = this.baseMapper.selectYfPayConfig(paymentMode);
        WXPayConfig.API_KEY = outMap.get("appAPIKey").toString();
        WXPayConfig.APP_SECRET = outMap.get("appSecret").toString();
        WXPayConfig.MCH_ID = outMap.get("appMCHId").toString();
        WXPayConfig.APP_ID = outMap.get("appID").toString();
        WXPayConfig.NOTIFY_URL = outMap.get("notifyUrl").toString();
        WXPayConfig.REDIRECT_URL = outMap.get("redirectUrl").toString();
        WXPayConfig.weChatWapPath = outMap.get("weChatPcPath").toString();

        //微信app
        WXConfigUtil.KEY = outMap.get("appAPIKey").toString();
        WXConfigUtil.APP_SECRET = outMap.get("appSecret").toString();
        WXConfigUtil.MCH_ID = outMap.get("appMCHId").toString();
        WXConfigUtil.APP_ID = outMap.get("appID").toString();
        WXConfigUtil.NOTIFY_URL_APP = outMap.get("notifyUrl").toString();

        //微信小程序
        String paymentMode2 = "02";
        Map<String, String> outMap2 = this.baseMapper.selectYfPayConfig(paymentMode2);
        WXPayConfig.Applet_APP_ID = outMap2.get("appID").toString();
        WXPayConfig.Applet_APP_SECRET = outMap2.get("appSecret").toString();


        //支付宝
        String paymentMode3 = "03";
        outMap = this.baseMapper.selectYfPayConfig(paymentMode3);
        if (null != outMap && !outMap.isEmpty()) {
            // 设置支付宝网关URL，用于发起支付请求
            ALiPayConfig.URL = outMap.get("gatewayURL").toString();
            // 设置应用的ID，用于标识调用支付宝的应用
            ALiPayConfig.APPID = outMap.get("appID").toString();
            // 设置卖家ID，即商户号，用于标识收款的商家
            ALiPayConfig.sellerId = outMap.get("appMCHId").toString();
            // 设置应用的私钥，用于签名和验证签名
            ALiPayConfig.RSA_PRIVATE_KEY = outMap.get("appAPIKey").toString();
            // 设置应用公钥证书的路径，用于支付宝验证商家的公钥
            ALiPayConfig.CERT_PATH = outMap.get("cert_public_key_res2").toString();
            // 设置根证书的路径，用于验证支付宝的公钥
            ALiPayConfig.ROOT_CERT_PATH = outMap.get("cert_root_key").toString();
            // 设置支付宝公钥证书的路径，用于商家验证支付宝的公钥
            ALiPayConfig.ALIPAY_PUBLIC_CERT_PATH = outMap.get("cert_app_public_key").toString();
            // 设置交易成功的通知URL，支付宝会向这个URL发送通知
            ALiPayConfig.shopping_notify_url = outMap.get("shoppingNotifyUrl").toString();
            // 设置H5支付成功的通知URL，支付宝会向这个URL发送通知
            ALiPayConfig.shopping_notify_url_h5 = outMap.get("rechargeNotifyUrl2").toString();
            // 设置PC端支付返回的URL，用户支付完成后会跳转到这个URL
            ALiPayConfig.shopping_return_url_pc = outMap.get("shoppingReturnUrlPc").toString();

        }
    }

    private IPage<ShopSalesStatisticsDTO> getStatistics(ShopSalesStatisticsDTO param) {
        try {
            //将对象转换为map
            Map data = ParseMapUtils.beanToMap(param);
            //分页查询数据
            IPage<ShopSalesStatisticsDTO> page = this.baseMapper.querySalesStatistics(new Query<ShopSalesStatisticsDTO>().getPage(data), param);
            // 获取分页结果中的记录列表
            List<ShopSalesStatisticsDTO> records = page.getRecords();
            // 遍历每条记录，进行销售统计信息的计算和设置
            for (ShopSalesStatisticsDTO pojo : records) {
                // 初始化总支付金额为0
                BigDecimal totalPayment = BigDecimal.ZERO;
                // 分别将卡支付、余额支付、支付宝支付、微信支付和订单报告价格转换为BigDecimal类型，并累加到总支付金额
                BigDecimal cardPayment = BigDecimal.valueOf(pojo.getCardPayment());
                BigDecimal balancePayment = BigDecimal.valueOf(pojo.getBalancePayment());
                BigDecimal alipayPayment = BigDecimal.valueOf(pojo.getAlipayPayment());
                BigDecimal weChatPayment = BigDecimal.valueOf(pojo.getWeChatTwoPayment());
                BigDecimal orderRptPrice = BigDecimal.valueOf(pojo.getOrderRptPrice());
                totalPayment = totalPayment.add(cardPayment).add(balancePayment).add(alipayPayment).add(weChatPayment).add(orderRptPrice);
                // 对总支付金额进行四舍五入，保留两位小数
                totalPayment = totalPayment.setScale(2, RoundingMode.HALF_UP);
                // 查询该店铺的退款金额
                // 查询店铺退款信息
                param.setShopId(pojo.getShopId());
                BigDecimal returnCash = orderServiceClient.querShopReturnCash(param);
                // 如果查询结果为空，则将退款金额设置为0
                if (null == returnCash) {
                    returnCash = new BigDecimal(0);
                }
                // 设置每条记录的退款金额和总支付金额，保留两位小数
                pojo.setRefundMoney(returnCash.doubleValue());
                pojo.setTotalPayment(totalPayment.doubleValue());
                pojo.setWeChatTwoPayment(pojo.getWeChatOnePayment() + pojo.getWeChatTwoPayment());
            }
            // 更新分页结果的记录列表，并返回处理后的分页结果
            page.setRecords(records);
            return page;
        } catch (IllegalAccessException e) {
            // 捕获并打印IllegalAccessException异常
            e.printStackTrace();
        }
        // 如果发生异常，返回null
        return null;
    }

}
