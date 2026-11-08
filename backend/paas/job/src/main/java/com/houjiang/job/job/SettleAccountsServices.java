package com.houjiang.job.job;

import cn.hutool.core.util.RandomUtil;
import com.houjiang.commons.dto.job.ShopSettleAccountInfoDTO;
import com.houjiang.commons.dto.order.SupplierShopSettleAccountInfoDTO;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.util.TimeStampUtil;
import com.houjiang.job.dto.ForSettleShopInfoDTO;
import com.houjiang.job.feign.OrderServiceClient;
import com.houjiang.job.mapper.SettleAccountsMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName SettleAccountsServices
 * @Description 账号
 * @Author Amos
 * @Date 2023/5/17 16:14
 * @Version 1.0
 * @author Amos
 */
@Service
public class SettleAccountsServices {
    private static final Logger logger = LoggerFactory.getLogger(SettleAccountsServices.class);

    @Resource
    private SettleAccountsMapper settleAccountsMapper;
    @Resource
    private OrderServiceClient orderServiceClient;

    @Transactional(rollbackFor = Exception.class)
    public void settleAccounts(ForSettleShopInfoDTO forSettleShopInfo) {
        try {
            Map<String, String> paraMap = new HashMap<>();

            String shopId = forSettleShopInfo.getShopId();
            if (forSettleShopInfo.getShopSettlementLastTime() != null) {
                // 判断是否达到下次结算周期
                if (DateUtils.getDayByMinusDate(forSettleShopInfo.getShopSettlementLastTime(), new Date()) < forSettleShopInfo.getShopSettlementCycle()) {
                    return;
                }
                // 判断是否距离上次结算有一个月时间
                if (forSettleShopInfo.getShopSettlementCycle() == 30) {
                    if (!DateUtils.isMoreThanAMonth(forSettleShopInfo.getShopSettlementLastTime(), new Date())) {
                        return;
                    }
                }
            }
            logger.info("店铺" + shopId + "更新开始时间：" + TimeStampUtil.getNowDate());

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            String dateString = formatter.format(new Date());
            //当前时间
            Date dNow = new Date();
            //设置时间格式
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
            //格式化当前时间
            String end = sdf.format(dNow);
            //获取上次结算时间
            String dBefore = settleAccountsMapper.getBeformDate(shopId, 1);
            if (StringUtils.isEmpty(dBefore)) {
                dBefore = "2019-04-10";
            }
            //格式化上次结算时间
            String start = dBefore + " 00:00:00";
            //end = "2023-12-28 00:00:00";
            Map<String, Object> getMap = new HashMap<>();
            getMap.put("start", start);
            getMap.put("end", end);
            getMap.put("shopId", shopId);
            getMap.put("dropShipping", "2");
            ShopSettleAccountInfoDTO shopSettleAccountData = orderServiceClient.getShopSettleAccountData(getMap);

            //获取单个店铺所有未结算的订单。
            List<String> orderIds = shopSettleAccountData.getOrderIdList();
            //获取单个店铺所有未结算的退单
            List<String> returnOrderIds = shopSettleAccountData.getReturnOrderIdList();

            //有订单有退单
            String osId = dateString + "1" + shopId;
            paraMap.put("osId", osId);
            paraMap.put("osStartDate", start);
            paraMap.put("osEndDate", end);
            //商品金额 不含运费
            paraMap.put("orderPaymentAmount", String.valueOf(shopSettleAccountData.getOrderPaymentAmount().subtract(shopSettleAccountData.getOrderShippingFee())));
            //运费
            paraMap.put("osShippingAmount", String.valueOf(shopSettleAccountData.getOrderShippingFee()));
            //退单金额
            paraMap.put("osOrderReturnAmount", String.valueOf(shopSettleAccountData.getReturnCash()));
            //佣金
            paraMap.put("osCommisAmount", shopSettleAccountData.getOrderCommissionFee().toString());
            //退还佣金
            paraMap.put("osCommisReturnAmount", shopSettleAccountData.getOrderReturnCommissionFee().toString());

            //红包
            paraMap.put("osRedpacketAmount", shopSettleAccountData.getOrderRptPrice().toString());

            //退还红包
            paraMap.put("osRedpacketReturnAmount", shopSettleAccountData.getReturnRptCash().toString());
            //会员折扣
            paraMap.put("osBaseMemberDiscount", shopSettleAccountData.getBaseMemberDiscount().toString());
            //退还会员折扣
            paraMap.put("osReturnMemberDiscount", shopSettleAccountData.getReturnMemberDiscount().toString());
            //推广佣金
            paraMap.put("osTgCommission", shopSettleAccountData.getTgCommission().toString());
            //分享佣金
            paraMap.put("osFxCommission", shopSettleAccountData.getFxCommission().toString());
            //推广佣金
            paraMap.put("osReturnTgCommission", shopSettleAccountData.getReturnTgCommission().toString());
            //分享佣金
            paraMap.put("osReturnFxCommission", shopSettleAccountData.getReturnFxCommission().toString());
            //自销佣金
            paraMap.put("selfSellingCommission", shopSettleAccountData.getSelfSellingCommission().toString());
            //自销运费
            paraMap.put("selfSellingShippingFee", shopSettleAccountData.getSelfSellingShippingFee().toString());
            //自销退还金额
            paraMap.put("selfSellingReturnCash", (shopSettleAccountData.getSelfSellingReturnCash()).toString());
            //自销退还佣金
            paraMap.put("selfSellingReturnCommission", (shopSettleAccountData.getSelfSellingReturnCommission()).toString());
            //订单协议价
            paraMap.put("sellOnAmount", shopSettleAccountData.getSellOnAmount().toString());
            //经销佣金
            paraMap.put("sellOnCommission", shopSettleAccountData.getSellOnCommission().toString());
            //经销退还金额
            paraMap.put("sellOnReturnCash", shopSettleAccountData.getSellOnReturnCash().toString());
            //经销退还佣金
            paraMap.put("sellOnReturnCommission", shopSettleAccountData.getSellOnReturnCommission().toString());

            // 初始化订单结算金额为0.00
            BigDecimal osAmount = new BigDecimal(0.00);
            // 计算订单结算金额
            osAmount = osAmount
                    // 商品金额 不含运费
                    .add(shopSettleAccountData.getOrderPaymentAmount().subtract(shopSettleAccountData.getOrderShippingFee()))
                    // 红包金额
                    .add(shopSettleAccountData.getOrderRptPrice())
                    // 自销佣金金额
                    .subtract(shopSettleAccountData.getSelfSellingCommission())
                    // 自销退单金额
                    .subtract(shopSettleAccountData.getSelfSellingReturnCash())
                    // 自销退还佣金
                    .add(shopSettleAccountData.getSelfSellingReturnCommission())
                    // 自销商品运费
                    .add(shopSettleAccountData.getSelfSellingShippingFee())
                    // 订单协议价
                    .subtract(shopSettleAccountData.getSellOnAmount())
                    // 经销佣金金额
                    .subtract(shopSettleAccountData.getSellOnCommission())
                    // 经销退单金额
                    .subtract(shopSettleAccountData.getSellOnReturnCash())
                    // 经销退还佣金
                    .add(shopSettleAccountData.getSellOnReturnCommission())
                    // 退还订单协议价
                    .add(shopSettleAccountData.getReturnSellOnAmount())
                    // 会员折扣
                    .add(shopSettleAccountData.getBaseMemberDiscount())
                    // 退还红包金额
                    .subtract(shopSettleAccountData.getReturnRptCash())
                    // 退还推广佣金
                    .add(shopSettleAccountData.getReturnTgCommission())
                    // 退还分享佣金
                    .add(shopSettleAccountData.getReturnFxCommission())
                    // 退还会员折扣
                    .subtract(shopSettleAccountData.getReturnMemberDiscount())
                    // 推广佣金
                    .subtract(shopSettleAccountData.getTgCommission())
                    // 分享佣金
                    .subtract(shopSettleAccountData.getFxCommission());

            //四舍五入
            osAmount = osAmount.setScale(2, RoundingMode.HALF_UP);
            //应结算金额
            paraMap.put("osAmount", osAmount.toString());

            paraMap.put("osDatetime", TimeStampUtil.getNowDate());
            paraMap.put("osDate", dateString);
            //1默认(已出账)2店家已确认3平台已审核4结算完成 5已提现',
            paraMap.put("osState", "1");
            paraMap.put("shopId", shopId);
            paraMap.put("shopName", forSettleShopInfo.getShopName());
            //结算订单类型 1-虚拟订单 2-实物订单',
            paraMap.put("osOrderType", "1");
            //罚款金额
            paraMap.put("osFineAmount","0");
            //经销佣金
            paraMap.put("orderDealerCommission", "0");
            //供应商佣金
            paraMap.put("orderSupplyCommission", "0");
            //经销退还佣金
            paraMap.put("returnDealerCommission", "0");
            //供应商退还佣金
            paraMap.put("returnSupplyCommission", "0");

            // 通过插入结算信息来初始化结算单
            Integer one = settleAccountsMapper.insertSettlement(paraMap);
            // 准备用于更新结算状态的数据映射
            Map<String, String> updateMap = new HashMap<String, String>();
            // 设置结算单在操作系统中的唯一标识
            updateMap.put("settlementOsId", osId);
            // 设置店铺ID
            updateMap.put("shopId", shopId);
            // 设置结算时间范围的起始日期
            updateMap.put("start", start);
            // 设置结算时间范围的结束日期
            updateMap.put("end", end);
            // 设置配送方式为代发货
            updateMap.put("dropShipping", "2");

            Integer two = 0;
            // 如果存在需要更新的基础订单数据，则进行更新
            if (null != orderIds && !orderIds.isEmpty()) {
                two = settleAccountsMapper.updateOrderBaseSettlement(orderIds, osId);
            }
//             初始化用于记录退货订单更新结果的变量
            Integer three = 0;
            // 如果存在需要更新的退货订单数据，则进行更新
            if (null != returnOrderIds && !returnOrderIds.isEmpty()) {
                three = settleAccountsMapper.updateOrderReturnSettlement(returnOrderIds, osId);
            }
            // 更新店铺的结算状态
            settleAccountsMapper.updateShopSettlement(shopId);

            logger.info("更新店铺：" + shopId + "插入结算表结果：" + one + " 更新orderbase表待结算订单个数：" + two + " 更新退单表结算表个数" + three + "更新结束时间：" + TimeStampUtil.getNowDate());
        } catch (Exception e) {
            logger.info("异常============");
            e.printStackTrace();
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public void settleDealerAccounts(ForSettleShopInfoDTO forSettleShopInfo) {
        try {
            Map<String, String> paraMap = new HashMap<>(16);

            String shopId = forSettleShopInfo.getShopId();

            logger.info("店铺" + shopId + "更新开始时间：" + TimeStampUtil.getNowDate());

            //格式化当前时间
            String end = DateUtils.getNowDayZero();
            //获取上次结算时间
            String dBefore = settleAccountsMapper.getBeformDate(shopId, 2);
            if (org.springframework.util.StringUtils.isEmpty(dBefore)) {
                dBefore = "2021-05-01";
            }
            //格式化上次结算时间
            String start = dBefore + " 00:00:00";
            String dateString = TimeStampUtil.getYear() + TimeStampUtil.getMonth() + TimeStampUtil.getDay();

            Map<String, String> getMap = new HashMap<>(8);
            getMap.put("start", start);
            getMap.put("end", end);
            getMap.put("shopId", shopId);
            getMap.put("dropShipping", "1");
            //获取单个店铺所有未结算的订单。
            ShopSettleAccountInfoDTO orderSettleAccountOrder = settleAccountsMapper.getDealerSettleAccountOrder(getMap);
            List<String> orderIds = settleAccountsMapper.getSettleAccountOrderId(getMap);
            //获取单个店铺所有未结算的退单
            ShopSettleAccountInfoDTO returnSettleAccountOrder = settleAccountsMapper.getDealerSettleAccountReturn(getMap);
            List<String> returnOrderIds = settleAccountsMapper.getSettleAccountReturnId(getMap);
            if (null == orderSettleAccountOrder) {
                orderSettleAccountOrder = new ShopSettleAccountInfoDTO();
            }
            if (null == returnSettleAccountOrder) {
                returnSettleAccountOrder = new ShopSettleAccountInfoDTO();
            }

            //有订单有退单
            String osId = dateString + "2" + shopId;
            // 将操作系统ID存储在参数映射中
            paraMap.put("osId", osId);
            // 将操作系统的开始日期存储在参数映射中
            paraMap.put("osStartDate", start);
            // 将操作系统的结束日期存储在参数映射中
            paraMap.put("osEndDate", end);

            //订单金额，含运费
            paraMap.put("orderPaymentAmount", String.valueOf(orderSettleAccountOrder.getOrderPaymentAmount()));
            //运费
            paraMap.put("osShippingAmount", String.valueOf(orderSettleAccountOrder.getOrderShippingFee()));
            //退单金额
            paraMap.put("osOrderReturnAmount", String.valueOf(returnSettleAccountOrder.getReturnCash()));
            //佣金
            paraMap.put("osCommisAmount", orderSettleAccountOrder.getOrderCommissionFee().toString());
            //退还佣金
            paraMap.put("osCommisReturnAmount", returnSettleAccountOrder.getOrderReturnCommissionFee().toString());
            //红包
            paraMap.put("osRedpacketAmount", "0.00");
            //退还红包
            paraMap.put("osRedpacketReturnAmount", "0.00");


            //订单金额（不含运费）-供货价-抽佣+会员折扣+退还抽佣-退单金额（不含运费）+供货价-退还会员折扣
            BigDecimal osAmount = orderSettleAccountOrder.getOrderPaymentAmount()
                    .subtract(orderSettleAccountOrder.getSupperPriceSum())
                    .subtract(orderSettleAccountOrder.getOrderCommissionFee())
                    .add(orderSettleAccountOrder.getBaseMemberDiscount())
                    .add(returnSettleAccountOrder.getOrderReturnCommissionFee())
                    .subtract(returnSettleAccountOrder.getReturnCash())
                    .add(returnSettleAccountOrder.getSupperReturnPrice())
                    .subtract(returnSettleAccountOrder.getReturnMemberDiscount())
                    .subtract(orderSettleAccountOrder.getOrderShippingFee());
            //应结算金额
            paraMap.put("osAmount", osAmount.toString());
            // 将当前时间戳转换为指定格式的日期字符串，并存储为"osDatetime"键的值
            paraMap.put("osDatetime", TimeStampUtil.getNowDate());
            // 将传入的日期字符串存储为"osDate"键的值
            paraMap.put("osDate", dateString);

            //1默认(已出账)2店家已确认3平台已审核4结算完成 5已提现',
            paraMap.put("osState", "1");
            // 将店铺ID存储到参数映射中，用于后续的结算信息处理
            paraMap.put("shopId", shopId);
            // 获取待结算店铺的名称，并将其存储到参数映射中
            paraMap.put("shopName", forSettleShopInfo.getShopName());

            //结算订单类型 1-虚拟订单 2-实物订单',
            paraMap.put("osOrderType", "2");
            //罚款金额
            paraMap.put("osFineAmount", "0.00");
            //经销佣金
            paraMap.put("orderDealerCommission", orderSettleAccountOrder.getOrderDealerCommission().doubleValue() + "");
            //供应商佣金
            paraMap.put("orderSupplyCommission", "0");
            //经销退还佣金
            paraMap.put("returnDealerCommission", returnSettleAccountOrder.getReturnDealerCommission().doubleValue() + "");
            //供应商退还佣金
            paraMap.put("returnSupplyCommission", "0");
            //会员折扣
            paraMap.put("osBaseMemberDiscount", orderSettleAccountOrder.getBaseMemberDiscount().toString());
            //退还会员折扣
            paraMap.put("osReturnMemberDiscount", returnSettleAccountOrder.getReturnMemberDiscount().toString());
            //供货价
            paraMap.put("osSupplyPrice", orderSettleAccountOrder.getSupperPriceSum().toString());
            //退还供货价
            paraMap.put("osReturnSupplyPrice", returnSettleAccountOrder.getSupperReturnPrice().toString());


            /**
             * 插入结算信息。
             * 调用settleAccountsMapper的insertSettlement方法，传入结算参数paraMap，返回插入的结算ID。
             * @param paraMap 结算参数映射表，包含结算相关的所有信息。
             * @return 插入结算表后的自增长ID，用于标识该结算记录。
             */
            Integer one = settleAccountsMapper.insertSettlement(paraMap);

            Integer two = 0;
            // 如果存在需要更新的基础订单数据，则进行更新
            if (null != orderIds && !orderIds.isEmpty()) {
                two = settleAccountsMapper.updateOrderBaseSettlement(orderIds, osId);
            }

            Integer three = 0;
            if (null != returnOrderIds && !returnOrderIds.isEmpty()) {
                three = settleAccountsMapper.updateOrderReturnSettlement(returnOrderIds, osId);
            }

            logger.info("更新店铺：" + shopId + "插入结算表结果：" + one + " 更新orderbase表待结算订单个数：" + two + " 更新退单表结算表个数" + three + "更新结束时间：" + TimeStampUtil.getNowDate());
        } catch (Exception e) {
            logger.info("异常============");
            e.printStackTrace();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void settleSupplyAccounts(ForSettleShopInfoDTO forSettleShopInfo) {
        try {
            String shopId = forSettleShopInfo.getShopId();

            logger.info("店铺" + shopId + "更新开始时间：" + TimeStampUtil.getNowDate());

            //格式化当前时间
            String end = DateUtils.getNowDayZero();
            //获取上次结算时间
            String dBefore = settleAccountsMapper.getBeformDate(shopId, 3);
            if (org.springframework.util.StringUtils.isEmpty(dBefore)) {
                dBefore = "2021-05-01";
            }
            //格式化上次结算时间
            String start = dBefore + " 00:00:00";

            /**
             * 创建一个HashMap用于存储查询参数。
             * 初始化容量为8，以优化性能。
             */
            Map<String, String> getMap = new HashMap<>(8);

            /**
             * 添加查询的起始时间到Map中。
             * 这个参数用于限定查询结果的起始时间点。
             */
            getMap.put("start", start);

            /**
             * 添加查询的结束时间到Map中。
             * 这个参数用于限定查询结果的结束时间点。
             */
            getMap.put("end", end);

            /**
             * 添加店铺ID到Map中。
             * 这个参数用于指定查询的特定店铺。
             */
            getMap.put("shopId", shopId);

            /**
             * 添加滴灌运输标记到Map中。
             * 设置为"1"表示查询滴灌运输的相关数据。
             */
            getMap.put("dropShipping", "1");

            //获取单个店铺所有未结算的订单。
            ShopSettleAccountInfoDTO orderSettleAccountOrder = settleAccountsMapper.getSupplySettleAccountOrder(getMap);
            //获取单个店铺所有未结算的退单
            ShopSettleAccountInfoDTO returnSettleAccountOrder = settleAccountsMapper.getSupplySettleAccountReturn(getMap);
            // 需要结算的退款订单编码
            List<String> orderIdList = settleAccountsMapper.querySupplyOrderBaseInfo(getMap);

            // 检查订单结算信息是否为空，如果为空，则初始化一个新的订单结算信息对象
            if (null == orderSettleAccountOrder) {
                orderSettleAccountOrder = new ShopSettleAccountInfoDTO();
            }
            // 检查退款结算信息是否为空，如果为空，则初始化一个新的退款结算信息对象
            if (null == returnSettleAccountOrder) {
                returnSettleAccountOrder = new ShopSettleAccountInfoDTO();
            }


            //获取单个店铺普通订单分佣总价
            BigDecimal paymentAmounCats = orderSettleAccountOrder.getOrderCommissionFee();

            //获取单个店铺普通订单 退单 分佣总价
            BigDecimal refundPaymentAmounCats = returnSettleAccountOrder.getOrderReturnCommissionFee();

            //供应商经销等级 折扣
            BigDecimal supperDiscountFee = orderSettleAccountOrder.getSupperDiscountFee();
            //退还 供应商经销等级 折扣
            BigDecimal returnSupperDiscountFee = returnSettleAccountOrder.getReturnSupperDiscountFee();
            /**
             * 构建日期字符串
             * 通过拼接年、月、日来创建一个表示日期的字符串。这种方式用于生成与时间戳相关的唯一标识。
             * 使用TimeStampUtil类的静态方法获取年、月、日，确保了日期字符串的格式一致性。
             */
            String dateString = TimeStampUtil.getYear() + TimeStampUtil.getMonth() + TimeStampUtil.getDay();

            /**
             * 初始化参数映射表
             * 使用HashMap来存储参数和其对应的值。这里预设了16个元素的容量，以优化存储效率。
             * 在后续的代码中，这个映射表将被用于存储和传递参数，方便进行各种操作和处理。
             */
            Map<String, String> paraMap = new HashMap<>(16);

            if (null == orderSettleAccountOrder && null == returnSettleAccountOrder) {
                //直接返回
                return;
            }
            //没有订单没有退单
            if (null != orderSettleAccountOrder && null == returnSettleAccountOrder) {
                /* 根据日期字符串、特定字符串"3"和商店ID拼接生成osId */
                String osId = dateString + "3" + shopId;
                /* 将开始时间和结束时间分别作为osStartDate和osEndDate的值存入参数映射表 */
                paraMap.put("osStartDate", start);
                paraMap.put("osEndDate", end);
                /* 将生成的osId作为参数存入映射表 */
                paraMap.put("osId", osId);

                //订单金额，含运费
                paraMap.put("orderPaymentAmount", "0.00");
                //运费
                paraMap.put("osShippingAmount", "0.00");
                //退单金额
                paraMap.put("osOrderReturnAmount", "0.00");

                Double commission = 0.00;
                //佣金
                paraMap.put("osCommisAmount", commission.toString());
                Double returnCommission = 0.00;
                //退还佣金
                paraMap.put("osCommisReturnAmount", returnCommission.toString());
                //退还佣金
                paraMap.put("osCommisReturnAmount", "0.00");
                //红包
                paraMap.put("osRedpacketAmount", "0.00");
                //供应商经销等级 折扣
                paraMap.put("osSupperDiscountFee", "0.00");
                //供应商退还经销等级折扣
                paraMap.put("osReturnSupperDiscountFee", "0.00");
                //退还红包
                paraMap.put("osRedpacketReturnAmount", "0.00");
                //应结算金额
                paraMap.put("osAmount", "0.00");

                paraMap.put("osDatetime", TimeStampUtil.getNowDate());
                paraMap.put("osDate", dateString);
                //1默认(已出账)2店家已确认3平台已审核4结算完成 5已提现',
                paraMap.put("osState", "1");
                paraMap.put("shopId", shopId);
                paraMap.put("shopName", forSettleShopInfo.getShopName());
                //结算订单类型 1-虚拟订单 2-实物订单',
                paraMap.put("osOrderType", "3");
                //罚款金额
                paraMap.put("osFineAmount", "0.00");
                //经销佣金
                paraMap.put("orderDealerCommission", "0");
                //供应商佣金
                paraMap.put("orderSupplyCommission", "0");
                //经销退还佣金
                paraMap.put("returnDealerCommission", "0");
                //供应商退还佣金
                paraMap.put("returnSupplyCommission", "0");

                Integer one = settleAccountsMapper.insertSettlement(paraMap);
                logger.info(orderSettleAccountOrder.toString() + "更新店铺： " + shopId + "  插入结算表结果：" + one);
                return;
            }
            //有订单没有退单的情况
            if (null != orderSettleAccountOrder && null == returnSettleAccountOrder) {
                /* 根据日期字符串、特定字符串"3"和商店ID拼接生成osId */
                String osId = dateString + "3" + shopId;
                /* 将开始时间和结束时间分别作为osStartDate和osEndDate的值存入参数映射表 */
                paraMap.put("osStartDate", start);
                paraMap.put("osEndDate", end);
                /* 将生成的osId作为参数存入映射表 */
                paraMap.put("osId", osId);

                //订单金额，不含运费
                paraMap.put("orderPaymentAmount", String.valueOf(orderSettleAccountOrder.getOrderPaymentAmount()));
                //运费
                paraMap.put("osShippingAmount", orderSettleAccountOrder.getOrderShippingFee().toString());
                //退单金额
                paraMap.put("osOrderReturnAmount", "0.00");
                //佣金
                paraMap.put("osCommisAmount", paymentAmounCats.toString());
                Double returnCommission = 0.00;
                //退还佣金
                paraMap.put("osCommisReturnAmount", returnCommission.toString());
                //供应商经销等级 折扣
                paraMap.put("osSupperDiscountFee", supperDiscountFee.toString());
                //供应商退还经销等级折扣
                paraMap.put("osReturnSupperDiscountFee", "0.00");
                //红包
                paraMap.put("osRedpacketAmount", "0.00");
                //退还红包
                paraMap.put("osRedpacketReturnAmount", "0.00");

                //供货价+运费-抽佣-退单（供货价）+退还抽佣-经销商等级折扣+退还经销商等级折扣
                BigDecimal osAmount = orderSettleAccountOrder.getOrderPaymentAmount()
                        .subtract(supperDiscountFee)
                        .subtract(paymentAmounCats)
                        .add(orderSettleAccountOrder.getOrderShippingFee());
                //应结算金额
                paraMap.put("osAmount", osAmount.toString());

                paraMap.put("osDatetime", TimeStampUtil.getNowDate());
                paraMap.put("osDate", dateString);
                //1默认(已出账)2店家已确认3平台已审核4结算完成 5已提现',
                paraMap.put("osState", "1");
                paraMap.put("shopId", shopId);
                paraMap.put("shopName", forSettleShopInfo.getShopName());
                //结算订单类型 1-虚拟订单 2-实物订单',
                paraMap.put("osOrderType", "3");
                //罚款金额
                paraMap.put("osFineAmount", "0.00");
                //经销佣金
                paraMap.put("orderDealerCommission", "0");
                //供应商佣金
                paraMap.put("orderSupplyCommission", orderSettleAccountOrder.getOrderSupplyCommission().doubleValue() + "");
                //经销退还佣金
                paraMap.put("returnDealerCommission", "0");
                //供应商退还佣金
                paraMap.put("returnSupplyCommission", "0");

                /**
                 * 插入结算信息，并返回新插入的结算记录ID。
                 * @param paraMap 包含结算相关参数的映射。
                 * @return 新插入的结算记录的ID。
                 */
                Integer one = settleAccountsMapper.insertSettlement(paraMap);

                /**
                 * 准备更新供应订单基础结算信息的数据映射。
                 * 此映射包含用于更新结算状态的相关字段。
                 */
                Map<String, Object> updateMap = new HashMap<>(8);

                /**
                 * 将结算系统ID、店铺ID、开始时间和服务结束时间添加到更新映射中。
                 * 这些字段用于标识需要更新的具体结算记录。
                 */
                updateMap.put("settlementOsId", osId);
                updateMap.put("shopId", shopId);
                updateMap.put("start", start);
                updateMap.put("end", end);

                /**
                 * 更新供应订单的基础结算信息。
                 * @param updateMap 包含需要更新的字段及其新值的映射。
                 * @return 更新操作影响的记录数。
                 */
                Integer two = settleAccountsMapper.updateSupplyOrderBaseSettlement(updateMap);

                logger.info(orderSettleAccountOrder.toString() + "更新店铺： " + shopId + "  插入结算表结果：" + one + " 更新orderbase表待结算订单个数：" + two);
                return;
            }
            //没有订单 有退单
            if (null == orderSettleAccountOrder && null != returnSettleAccountOrder) {
                /* 根据日期字符串、特定字符串"3"和商店ID拼接生成osId */
                String osId = dateString + "3" + shopId;
                /* 将开始时间和结束时间分别作为osStartDate和osEndDate的值存入参数映射表 */
                paraMap.put("osStartDate", start);
                paraMap.put("osEndDate", end);
                /* 将生成的osId作为参数存入映射表 */
                paraMap.put("osId", osId);

                //订单金额，含运费
                paraMap.put("orderPaymentAmount", "0.00");
                //运费
                paraMap.put("osShippingAmount", "0.00");
                //退单金额
                paraMap.put("osOrderReturnAmount", String.valueOf(returnSettleAccountOrder.getReturnCash()));

                Double commission = 0.00;
                //佣金
                paraMap.put("osCommisAmount", commission.toString());
                //退还佣金
                paraMap.put("osCommisReturnAmount", refundPaymentAmounCats.toString());
                //供应商经销等级 折扣
                paraMap.put("osSupperDiscountFee", "0.00");
                //供应商退还经销等级折扣
                paraMap.put("osReturnSupperDiscountFee", returnSupperDiscountFee.toString());

                Double osRedpacketAmount = 0.00;
                //红包
                paraMap.put("osRedpacketAmount", osRedpacketAmount.toString());

                Double osRedpacketReturnAmount = 0.00;
                //退还红包
                paraMap.put("osRedpacketReturnAmount", osRedpacketReturnAmount.toString());

                //供货价+运费-抽佣-退单（供货价）+退还抽佣-经销商等级折扣+退还经销商等级折扣
                BigDecimal osAmount = orderSettleAccountOrder.getOrderPaymentAmount()
                        .subtract(returnSettleAccountOrder.getReturnCash())
                        .add(refundPaymentAmounCats)
                        .add(returnSupperDiscountFee);
                //应结算金额
                paraMap.put("osAmount", osAmount.toString());

                // 将当前时间戳转换为指定格式的日期字符串，并存储为"osDatetime"键对应的值
                paraMap.put("osDatetime", TimeStampUtil.getNowDate());
                // 将传入的日期字符串存储为"osDate"键对应的值
                paraMap.put("osDate", dateString);

                //1默认(已出账)2店家已确认3平台已审核4结算完成 5已提现',
                paraMap.put("osState", "1");
                paraMap.put("shopId", shopId);
                paraMap.put("shopName", forSettleShopInfo.getShopName());
                //结算订单类型 1-虚拟订单 2-实物订单',
                paraMap.put("osOrderType", "3");
                //罚款金额
                paraMap.put("osFineAmount", "0.00");
                //经销佣金
                paraMap.put("orderDealerCommission", "0");
                //供应商佣金
                paraMap.put("orderSupplyCommission", "0");
                //经销退还佣金
                paraMap.put("returnDealerCommission", "0");
                //供应商退还佣金
                paraMap.put("returnSupplyCommission", returnSettleAccountOrder.getReturnSupplyCommission().doubleValue() + "");

                /**
                 * 插入结算信息，并返回插入的结算ID。
                 * @param paraMap 包含结算所需参数的映射。
                 * @return 插入的结算记录的ID。
                 */
                Integer one = settleAccountsMapper.insertSettlement(paraMap);

                /* 初始化一个映射，用于存储更新结算信息所需的参数 */
                Map<String, Object> updateMap = new HashMap<>(8);

                /* 将结算平台订单ID、店铺ID、结算起始时间、结束时间以及订单列表添加到updateMap中 */
                updateMap.put("settlementOsId", osId);
                updateMap.put("shopId", shopId);
                updateMap.put("start", start);
                updateMap.put("end", end);
                updateMap.put("orderIdList", orderIdList);

                /* 初始化一个变量，用于记录更新供应订单结算状态的结果 */
                Integer three = 0;

                /* 当订单列表不为空时，尝试更新供应订单的结算状态 */
                if (!orderIdList.isEmpty()) {
                    three = settleAccountsMapper.updateSupplyOrderReturnSettlement(updateMap);
                }

                logger.info(returnSettleAccountOrder.toString() + "更新店铺：" + shopId + "插入结算表结果：" + one + " 更新退单表结算表个数" + three + "更新结束时间：" + TimeStampUtil.getNowDate());

                return;
            }
            //有订单有退单
            String osId = dateString + "3" + shopId;
            paraMap.put("osId", osId);
            paraMap.put("osStartDate", start);
            paraMap.put("osEndDate", end);
            //订单金额，含运费
            paraMap.put("orderPaymentAmount", String.valueOf(orderSettleAccountOrder.getOrderPaymentAmount()));
            //运费
            paraMap.put("osShippingAmount", orderSettleAccountOrder.getOrderShippingFee().toString());
            //退单金额
            paraMap.put("osOrderReturnAmount", String.valueOf(returnSettleAccountOrder.getReturnCash()));
            //佣金
            paraMap.put("osCommisAmount", paymentAmounCats.toString());
            //退还佣金
            paraMap.put("osCommisReturnAmount", refundPaymentAmounCats.toString());
            //供应商经销等级 折扣
            paraMap.put("osSupperDiscountFee", supperDiscountFee.toString());
            //供应商退还经销等级折扣
            paraMap.put("osReturnSupperDiscountFee", returnSupperDiscountFee.toString());
            //红包
            paraMap.put("osRedpacketAmount", orderSettleAccountOrder.getOrderRptPrice() + "");
            //退还红包
            paraMap.put("osRedpacketReturnAmount", returnSettleAccountOrder.getReturnRptCash() + "");

            ///供货价+运费-抽佣-退单（供货价）+退还抽佣-经销商等级折扣+退还经销商等级折扣
            BigDecimal osAmount = orderSettleAccountOrder.getOrderPaymentAmount()
                    .subtract(paymentAmounCats)
                    .subtract(orderSettleAccountOrder.getOrderVoucherPrice())
                    .add(orderSettleAccountOrder.getOrderRptPrice())
                    .add(orderSettleAccountOrder.getOrderShippingFee())
                    .subtract(returnSettleAccountOrder.getReturnCash())
                    .add(returnSettleAccountOrder.getReturnVoucherPrice())
                    .subtract(returnSettleAccountOrder.getReturnRptCash())
                    .add(refundPaymentAmounCats);

            //应结算金额
            paraMap.put("osAmount", osAmount.toString());

            paraMap.put("osDatetime", TimeStampUtil.getNowDate());
            paraMap.put("osDate", dateString);
            //1默认(已出账)2店家已确认3平台已审核4结算完成 5已提现',
            paraMap.put("osState", "1");
            // 将店铺ID存储到参数映射中，用于后续的结算信息处理
            paraMap.put("shopId", shopId);
            // 获取待结算店铺的名称，并将其存储到参数映射中
            paraMap.put("shopName", forSettleShopInfo.getShopName());

            //结算订单类型 1-虚拟订单 2-实物订单',
            paraMap.put("osOrderType", "3");
            //罚款金额
            paraMap.put("osFineAmount", "0.00");
            //经销佣金
            paraMap.put("orderDealerCommission", orderSettleAccountOrder.getOrderCommissionFee() + "");
            //供应商佣金
            paraMap.put("orderSupplyCommission", orderSettleAccountOrder.getOrderSupplyCommission() + "");
            //经销退还佣金
            paraMap.put("returnDealerCommission", returnSettleAccountOrder.getOrderReturnCommissionFee() + "");
            //供应商退还佣金
            paraMap.put("returnSupplyCommission", returnSettleAccountOrder.getReturnSupplyCommission() + "");
            /**
             * 插入结算信息，并返回新插入的结算ID。
             * @param paraMap 包含结算相关参数的映射。
             * @return 新插入的结算ID。
             */
            Integer one = settleAccountsMapper.insertSettlement(paraMap);

            /* 初始化用于更新供应订单结算信息的映射 */
            Map<String, Object> updateMap = new HashMap<>(8);

            /* 将结算平台ID、店铺ID、结算起始时间、结束时间、订单ID列表放入更新映射中 */
            updateMap.put("settlementOsId", osId);
            updateMap.put("shopId", shopId);
            updateMap.put("start", start);
            updateMap.put("end", end);
            updateMap.put("orderIdList", orderIdList);

            /* 更新供应订单的基础结算信息 */
            Integer two = settleAccountsMapper.updateSupplyOrderBaseSettlement(updateMap);

            /* 初始化用于更新供应订单退货结算信息的变量 */
            Integer three = 0;

            /* 如果订单ID列表不为空，则更新供应订单的退货结算信息 */
            if (!orderIdList.isEmpty()) {
                three = settleAccountsMapper.updateSupplyOrderReturnSettlement(updateMap);
            }

            logger.info("更新店铺：" + shopId + "插入结算表结果：" + one + " 更新orderbase表待结算订单个数：" + two + " 更新退单表结算表个数" + three + "更新结束时间：" + TimeStampUtil.getNowDate());
        } catch (Exception e) {
            logger.info("异常============");
            e.printStackTrace();
        }
    }

    public void settleWebPosOrder(ForSettleShopInfoDTO forSettleShopInfo) {
        try {
            Map<String, String> paraMap = new HashMap<>();

            String shopId = forSettleShopInfo.getShopId();
            logger.info("店铺" + shopId + "更新开始时间：" + TimeStampUtil.getNowDate());

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            String dateString = formatter.format(new Date());
            //当前时间
            Date dNow = new Date();
            //设置时间格式
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
            //格式化当前时间
            String end = sdf.format(dNow);
            //获取上次结算时间
            String dBefore = settleAccountsMapper.getBeformDate(shopId, 5);
            if (StringUtils.isEmpty(dBefore)) {
                dBefore = "2019-04-10";
            }
            //格式化上次结算时间
            String start = dBefore + " 00:00:00";
            //end = "2023-12-28 00:00:00";
            Map<String, String> getMap = new HashMap<>();
            getMap.put("start", start);
            getMap.put("end", end);
            getMap.put("shopId", shopId);
            //获取单个店铺所有未结算的订单。
            ShopSettleAccountInfoDTO orderSettleAccountOrder = settleAccountsMapper.getSettleWebPosOrder(getMap);
            List<String> orderIds = settleAccountsMapper.getSettleWebPosOrderId(getMap);
            //获取单个店铺所有未结算的退单
            ShopSettleAccountInfoDTO returnSettleAccountOrder = settleAccountsMapper.getSettleWebPosReturn(getMap);
            List<String> returnOrderIds = settleAccountsMapper.getSettleWebPosReturnId(getMap);
            if (null == orderSettleAccountOrder) {
                orderSettleAccountOrder = new ShopSettleAccountInfoDTO();
            }
            if (null == returnSettleAccountOrder) {
                returnSettleAccountOrder = new ShopSettleAccountInfoDTO();
            }
            //获取单个店铺普通订单分佣总价
            BigDecimal paymentAmounCats = orderSettleAccountOrder.getOrderCommissionFee();

            //获取单个店铺普通订单 退单 分佣总价
            BigDecimal refundPaymentAmounCats = returnSettleAccountOrder.getOrderReturnCommissionFee();

            //有订单有退单
            String osId = dateString + "5" + shopId;
            paraMap.put("osId", osId);
            paraMap.put("osStartDate", start);
            paraMap.put("osEndDate", end);
            //订单金额，含运费
            paraMap.put("orderPaymentAmount", String.valueOf(orderSettleAccountOrder.getOrderPaymentAmount()));
            //运费
            paraMap.put("osShippingAmount", String.valueOf(orderSettleAccountOrder.getOrderShippingFee()));
            //退单金额
            paraMap.put("osOrderReturnAmount", String.valueOf(returnSettleAccountOrder.getReturnCash()));
            //佣金
            paraMap.put("osCommisAmount", paymentAmounCats.toString());
            //退还佣金
            paraMap.put("osCommisReturnAmount", refundPaymentAmounCats.toString());

            BigDecimal osRedpacketAmount = orderSettleAccountOrder.getOrderRptPrice();
            //红包
            paraMap.put("osRedpacketAmount", osRedpacketAmount.toString());

            BigDecimal osRedpacketReturnAmount = returnSettleAccountOrder.getReturnRptCash();

            //退还红包
            paraMap.put("osRedpacketReturnAmount", osRedpacketReturnAmount.toString());
            //会员折扣
            paraMap.put("osBaseMemberDiscount", orderSettleAccountOrder.getBaseMemberDiscount().toString());
            //退还会员折扣
            paraMap.put("osReturnMemberDiscount", returnSettleAccountOrder.getReturnMemberDiscount().toString());
            //推广佣金
            paraMap.put("osTgCommission", orderSettleAccountOrder.getTgCommission().toString());
            //分享佣金
            paraMap.put("osFxCommission", orderSettleAccountOrder.getFxCommission().toString());
            //订单金额(不含运费) - 佣金金额 - 退单金额 + 退还佣金 + （运费—退还运费）
            // 初始化订单结算金额为0.00
            BigDecimal osAmount =new BigDecimal(0.00);
            // 计算订单结算金额
            // 通过累加订单支付金额、减去订单佣金费用、加上基础会员折扣、减去退款现金和会员折扣、加上订单退款佣金费用，来得到最终的订单结算金额
            osAmount = osAmount
                    .add(orderSettleAccountOrder.getOrderPaymentAmount())
                    .subtract(orderSettleAccountOrder.getOrderCommissionFee())
                    .add(orderSettleAccountOrder.getBaseMemberDiscount())
                    .subtract(returnSettleAccountOrder.getReturnCash())
                    .subtract(returnSettleAccountOrder.getReturnMemberDiscount())
                    .add(returnSettleAccountOrder.getOrderReturnCommissionFee());

            //四舍五入
            osAmount = osAmount.setScale(2, RoundingMode.HALF_UP);
            //应结算金额
            paraMap.put("osAmount", osAmount.toString());

            paraMap.put("osDatetime", TimeStampUtil.getNowDate());
            paraMap.put("osDate", dateString);
            //1默认(已出账)2店家已确认3平台已审核4结算完成 5已提现',
            paraMap.put("osState", "1");
            paraMap.put("shopId", shopId);
            paraMap.put("shopName", forSettleShopInfo.getShopName());
            //结算订单类型 1-虚拟订单 2-实物订单',
            paraMap.put("osOrderType", "5");
            //罚款金额
            paraMap.put("osFineAmount", "0");
            //经销佣金
            paraMap.put("orderDealerCommission", "0");
            //供应商佣金
            paraMap.put("orderSupplyCommission", "0");
            //经销退还佣金
            paraMap.put("returnDealerCommission", "0");
            //供应商退还佣金
            paraMap.put("returnSupplyCommission", "0");
            /**
             * 结算账户操作。
             * 本段代码负责处理结算账户的插入和更新操作，针对不同的订单类型进行不同的操作。
             * 包括插入新的结算记录以及更新已有的结算记录，操作基于传入的订单ID集合。
             */

            // 插入结算信息，返回插入的记录数
            Integer one = settleAccountsMapper.insertSettlement(paraMap);
            // 初始化更新操作的记录数
            Integer two = 0;
            // 当存在需要更新的订单ID集合时，执行更新操作，并记录更新的记录数
            if (!orderIds.isEmpty()) {
                two = settleAccountsMapper.updateWebPosSettlement(orderIds, osId);
            }
            // 初始化退单更新操作的记录数
            Integer three = 0;
            // 当存在需要更新的退单ID集合时，执行退单更新操作，并记录更新的记录数
            if (!returnOrderIds.isEmpty()) {
                three = settleAccountsMapper.updateWebPosSettlement(returnOrderIds, osId);
            }
            logger.info("更新店铺：" + shopId + "插入结算表结果：" + one + " 更新orderbase表待结算订单个数：" + two + " 更新退单表结算表个数" + three + "更新结束时间：" + TimeStampUtil.getNowDate());
        } catch (Exception e) {
            logger.info("异常============");
            e.printStackTrace();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void settleSupplierAccounts(ForSettleShopInfoDTO forSettleShopInfo) {
        try {
            Map<String, String> paraMap = new HashMap<>();

            String shopId = forSettleShopInfo.getShopId();
            if (forSettleShopInfo.getShopSettlementLastTime() != null) {
                // 判断是否达到下次结算周期
                if (DateUtils.getDayByMinusDate(forSettleShopInfo.getShopSettlementLastTime(), new Date()) < forSettleShopInfo.getShopSettlementCycle()) {
                    return;
                }
                // 判断是否距离上次结算有一个月时间
                if (forSettleShopInfo.getShopSettlementCycle() == 30) {
                    if (!DateUtils.isMoreThanAMonth(forSettleShopInfo.getShopSettlementLastTime(), new Date())) {
                        return;
                    }
                }
            }
            logger.info("店铺" + shopId + "更新开始时间：" + TimeStampUtil.getNowDate());

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            String dateString = formatter.format(new Date());
            //当前时间
            Date dNow = new Date();
            //设置时间格式
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
            //格式化当前时间
            String end = sdf.format(dNow);
            //获取上次结算时间
            String dBefore = "";
            //String dBefore = settleAccountsMapper.getBeformDate(shopId, 1);
            if (StringUtils.isEmpty(dBefore)) {
                dBefore = "2019-04-10";
            }
            //格式化上次结算时间
            String start = dBefore + " 00:00:00";

//            end = "2023-06-07 00:00:00";
            Map<String, Object> getMap = new HashMap<>();
            getMap.put("start", start);
            getMap.put("end", end);
            getMap.put("shopId", shopId);
            getMap.put("dropShipping", "2");
            SupplierShopSettleAccountInfoDTO supplierSettleAccountData = orderServiceClient.getSupplierSettleAccountData(getMap);

            List<String> orderIds = supplierSettleAccountData.getOrderIdList();
            List<String> returnOrderIds = supplierSettleAccountData.getReturnOrderIdList();


            //有订单有退单
            String osId = dateString + "1" + shopId+ RandomUtil.randomString(3);
            paraMap.put("osId", osId);
            paraMap.put("osStartDate", start);
            paraMap.put("osEndDate", end);
            //订单金额，不含运费
            paraMap.put("orderPaymentAmount", String.valueOf(supplierSettleAccountData.getOrderPaymentAmount() - supplierSettleAccountData.getOrderShippingFee()));
            //运费
            paraMap.put("osShippingAmount", String.valueOf(supplierSettleAccountData.getOrderShippingFee() - supplierSettleAccountData.getReturnShippingFee()));
            //退单金额
            paraMap.put("osOrderReturnAmount", String.valueOf(supplierSettleAccountData.getReturnCash()));
            //佣金
            paraMap.put("osCommisAmount",supplierSettleAccountData.getOrderCommissionFee().toString());
            //退还佣金
            paraMap.put("osCommisReturnAmount", supplierSettleAccountData.getOrderReturnCommissionFee().toString());
            //红包
            paraMap.put("osRedpacketAmount", "0");

            //退还红包
            paraMap.put("osRedpacketReturnAmount", "0");
            //会员折扣
            paraMap.put("osBaseMemberDiscount", "0");
            //退还会员折扣
            paraMap.put("osReturnMemberDiscount", "0");
            //推广佣金
            paraMap.put("osTgCommission", "0");
            //分享佣金
            paraMap.put("osFxCommission", "0");
            //订单金额(不含运费) - 佣金金额 - 退单金额 + 退还佣金 + （运费—退还运费）
            Double osAmount = supplierSettleAccountData.getOrderPaymentAmount() - supplierSettleAccountData.getOrderReturnCommissionFee() - supplierSettleAccountData.getReturnCash()
                    + supplierSettleAccountData.getOrderReturnCommissionFee()
                    + (supplierSettleAccountData.getOrderShippingFee() - supplierSettleAccountData.getReturnShippingFee());
            //应结算金额
            paraMap.put("osAmount", osAmount.toString());

            paraMap.put("osDatetime", TimeStampUtil.getNowDate());
            paraMap.put("osDate", dateString);
            //1默认(已出账)2店家已确认3平台已审核4结算完成 5已提现',
            paraMap.put("osState", "1");
            paraMap.put("shopId", shopId);
            paraMap.put("shopName", forSettleShopInfo.getShopName());
            //结算订单类型 1-虚拟订单 2-实物订单',
            paraMap.put("osOrderType", "1");
            //罚款金额
            paraMap.put("osFineAmount", "0");
            //经销佣金
            paraMap.put("orderDealerCommission", "0");
            //供应商佣金
            paraMap.put("orderSupplyCommission", "0");
            //经销退还佣金
            paraMap.put("returnDealerCommission", "0");
            //供应商退还佣金
            paraMap.put("returnSupplyCommission", "0");

            Integer one = settleAccountsMapper.insertSupplierSettlement(paraMap);
            Map<String, String> updateMap = new HashMap<>();
            updateMap.put("settlementOsId", osId);
            updateMap.put("shopId", shopId);
            updateMap.put("start", start);
            updateMap.put("end", end);
            updateMap.put("dropShipping", "2");
            Integer two = 0;
            if (null != orderIds && !orderIds.isEmpty()) {
                two = settleAccountsMapper.updateOrderBaseSettlement2(orderIds, osId);
            }
            Integer three = 0;
            if (null != returnOrderIds && !returnOrderIds.isEmpty()) {
                three = settleAccountsMapper.updateOrderReturnSettlement2(returnOrderIds, osId);
            }
            settleAccountsMapper.updateSupplierShopSettlement(shopId);
            logger.info("更新店铺：" + shopId + "插入供应商结算表结果：" + one + " 更新orderbase表待结算订单个数：" + two + " 更新退单表结算表个数" + three + "更新结束时间：" + TimeStampUtil.getNowDate());
        } catch (Exception e) {
            logger.info("异常============");
            e.printStackTrace();
        }
    }
}
