package com.houjiang.merchant.common.logic;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.*;
import com.houjiang.merchant.common.enums.OrderEnum;
import com.houjiang.merchant.common.enums.SettlementEnum;
import com.houjiang.merchant.common.enums.ShopAccountEnum;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.mapper.mall.MallOrderMapper;
import com.houjiang.merchant.common.mapper.mall.MallSettlementMapper;
import com.houjiang.merchant.common.mapper.mall.MallSettlementRecordMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopMapper;
import com.houjiang.merchant.common.util.SpringUtils;
import com.houjiang.merchant.common.util.StringUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
public class CommonSettlementRecordLogic {

    /**
     * @notes 现在是否可结算
     * @return bool
     */
    public static Boolean nowCanSettle() {
//        JSONObject set = CommonConfigLogic.mallOrderSettlementGet();
//        Integer settleCycle = set.getInteger("settle_cycle");
//        LocalDateTime now = LocalDateTime.now();
//
//        // 判断是否为00:00时刻
//        String currentTime = now.format(DateTimeFormatter.ofPattern("HHmm"));
//        Boolean isMidnight = "0000".equals(currentTime);
//
//        switch (settleCycle) {
//            case 0: // OrderSettingEnum.SETTLEMENT_CYCLE_DAY
//                //return true;
//                return isMidnight;
//            case 1: // OrderSettingEnum.SETTLEMENT_CYCLE_WEEK
//                Integer settleWeekDay = set.getInteger("settle_cycle_week_day");
//                int currentDayOfWeek = now.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
//                return isMidnight && currentDayOfWeek == settleWeekDay;
//            case 2: // OrderSettingEnum.SETTLEMENT_CYCLE_MONTH
//                Integer settleMonthDay = set.getInteger("settle_cycle_month_day");
//                int currentDayOfMonth = now.getDayOfMonth();
//                return isMidnight && currentDayOfMonth == settleMonthDay;
//            default:
//                return false;
//        }

        return true;
    }

    /**
     * 结算数据整理
     */
    public static void runSettle() {

        // 获取未结算的订单涉及到的店铺id
        MallShopMapper mallShopMapper = SpringUtils.getBean(MallShopMapper.class);
        MallOrderMapper mallOrderMapper = SpringUtils.getBean(MallOrderMapper.class);
        QueryWrapper<MallOrder> queryMallOrderWrapper = new QueryWrapper();
        queryMallOrderWrapper.eq("pay_is", YesNoEnum.YES.getCode());
        queryMallOrderWrapper.eq("settlement_is", YesNoEnum.NO.getCode());
        queryMallOrderWrapper.eq("status", OrderEnum.STATUS_FINISH.getCode());
        queryMallOrderWrapper.lt("after_sale_deadline", System.currentTimeMillis()/ 1000);
        queryMallOrderWrapper.eq("delete_time", 0);
        queryMallOrderWrapper.orderByDesc("id");

        List<MallOrder> orderList = mallOrderMapper.selectList(queryMallOrderWrapper);
        List<Integer> shopIds = new ArrayList<>();
        Map<Integer, List<MallOrder>> shopOrderMap = new HashMap<>();
        //逐个逐个店铺结算
        if (orderList.size() > 0) {
            for (MallOrder mallOrder : orderList) {
                // 是否存在订单售后
                if(CommonOrderAfterSaleLogic.hasOrderAfterSaleIng(mallOrder.getId())) {
                    continue;
                }

                // 是否存在未结算的订单分销
                if (CommonOrderAfterSaleLogic.findWaitSettlement(mallOrder.getId())) {
                    continue;
                }

                if (shopIds.contains(mallOrder.getShopId()) == false) {
                    shopIds.add(mallOrder.getShopId());
                }

                if (shopOrderMap.containsKey(mallOrder.getShopId()) == false) {
                    shopOrderMap.put(mallOrder.getShopId(), new ArrayList<MallOrder>() {{
                        add(mallOrder);
                    }});
                } else {
                    List<MallOrder> mallOrders = shopOrderMap.get(mallOrder.getShopId());
                    mallOrders.add(mallOrder);
                    shopOrderMap.put(mallOrder.getShopId(), mallOrders);
                }
            }
        }

        if (shopIds.size() > 0) {
            for (Integer shopId : shopIds) {
                MallShop shop = mallShopMapper.selectById(shopId);
                //店铺被删了
                if (StringUtils.isNull(shop)) {
                    continue;
                }

                //以店铺为单位，结算所有订单
                // 有结算的订单，才执行
                if (shopOrderMap.get(shopId).size() > 0) {
                    executeSettle(shop, shopOrderMap.get(shopId));
                }
            }
        }
    }

    /**
     * 结算执行
     * @param shop
     */
    public static void executeSettle(MallShop shop, List<MallOrder> orderList) {
        // 获取事务管理器
        DataSourceTransactionManager transactionManager = SpringUtils.getBean(DataSourceTransactionManager.class);
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);

        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            // 获取Mapper实例
            MallSettlementMapper mallSettlementMapper = SpringUtils.getBean(MallSettlementMapper.class);
            MallSettlementRecordMapper mallSettlementRecordMapper = SpringUtils.getBean(MallSettlementRecordMapper.class);
            MallOrderMapper mallOrderMapper = SpringUtils.getBean(MallOrderMapper.class);

            // 1. 生成结算流水号
            String settlementSn = mallSettlementMapper.randMakeOrderSn("sn");

            // 2. 在MallSettlement表中新增一条记录
            MallSettlement settlement = new MallSettlement();
            settlement.setShopId(shop.getId());
            settlement.setSn(settlementSn);
            settlement.setOrderMoney(BigDecimal.ZERO);
            settlement.setOrderNum(0);
            settlement.setOrderRefundNum(0);
            settlement.setOrderRefundMoney(BigDecimal.ZERO);
            settlement.setServiceRatio(BigDecimal.ZERO);
            settlement.setServiceMoney(BigDecimal.ZERO);
            settlement.setMoney(BigDecimal.ZERO);
            settlement.setCreateTime(System.currentTimeMillis() / 1000);
            settlement.setUpdateTime(System.currentTimeMillis() / 1000);
            settlement.setDeleteTime(0L);

            mallSettlementMapper.insert(settlement);

            // 3. 循环处理orderList
            for (MallOrder order : orderList) {
                BigDecimal payMoney = order.getPayMoney();
                BigDecimal serviceRatio = order.getServiceRatio();
                BigDecimal distributionMoney = CommonDistributionOrderLogic.orderDistributionMoney(order.getId());
                List<MallOrderAfterSale> afterSaleLists = CommonOrderAfterSaleLogic.orderWaitSettlementList(order.getId());
                BigDecimal afterSaleMoney = BigDecimal.ZERO;
                if (afterSaleLists.size() > 0) {
                    for (MallOrderAfterSale afterSale : afterSaleLists) {
                        afterSaleMoney = afterSaleMoney.add(afterSale.getRefundMoney());
                    }
                }

                // 结算金额 = 订单实付 - 订单售后金额
                // 服务金额 = 结算金额 * 服务比例
                // 商家实际金额 = 结算金额 - 分销金额 -服务金额
                BigDecimal settlementMoney = payMoney.subtract(afterSaleMoney).setScale(2, RoundingMode.HALF_UP);
                BigDecimal serviceMoney = settlementMoney.multiply(serviceRatio.divide(new BigDecimal(100))).setScale(2, RoundingMode.HALF_UP);
                BigDecimal money = settlementMoney.subtract(distributionMoney).subtract(serviceMoney).setScale(2, RoundingMode.HALF_UP);

                // MallSettlementRecord表中为每条订单新增一条记录
                MallSettlementRecord record = new MallSettlementRecord();
                String recordSn = mallSettlementRecordMapper.randMakeOrderSn("sn");
                record.setShopId(shop.getId());
                record.setSettlementId(settlement.getId());
                record.setSn(recordSn);
                record.setFromSn(order.getSn());
                record.setType(SettlementEnum.TYPE_ORDER.getCode());
                record.setOrderMoney(payMoney);
                record.setOrderRefundMoney(afterSaleMoney);
                record.setDistributionMoney(distributionMoney);
                record.setServiceMoney(serviceMoney.multiply(new BigDecimal(-1)));
                record.setServiceRatio(order.getServiceRatio());
                record.setMoney(money);
                record.setCreateTime(System.currentTimeMillis() / 1000);
                record.setUpdateTime(System.currentTimeMillis() / 1000);
                record.setDeleteTime(0L);

                mallSettlementRecordMapper.insert(record);


                MallSettlement tempSettlement = mallSettlementMapper.selectById(settlement.getId());

                if (tempSettlement != null) {
                    tempSettlement.setOrderMoney(tempSettlement.getOrderMoney() != null ? tempSettlement.getOrderMoney().add(record.getOrderMoney()) : new BigDecimal("0").add(record.getOrderMoney()));
                    tempSettlement.setDistributionMoney(tempSettlement.getDistributionMoney() != null ? tempSettlement.getDistributionMoney().add(distributionMoney) : new BigDecimal("0").add(distributionMoney));
                    tempSettlement.setOrderNum(tempSettlement.getOrderNum() != null ? tempSettlement.getOrderNum() + 1 : 1);
                    tempSettlement.setOrderRefundMoney(tempSettlement.getOrderRefundMoney() != null ? tempSettlement.getOrderRefundMoney().add(afterSaleMoney) : new BigDecimal("0").add(afterSaleMoney));
                    tempSettlement.setOrderRefundNum(tempSettlement.getOrderRefundNum() != null ? tempSettlement.getOrderRefundNum() + afterSaleLists.size() : 0 + afterSaleLists.size());
                    tempSettlement.setServiceMoney(tempSettlement.getServiceMoney() != null ? tempSettlement.getServiceMoney().add(serviceMoney) : new BigDecimal("0").add(serviceMoney));
                    tempSettlement.setMoney(tempSettlement.getMoney() != null ? tempSettlement.getMoney().add(money) : new BigDecimal("0").add(money));
                    tempSettlement.setUpdateTime(System.currentTimeMillis() / 1000);
                    mallSettlementMapper.updateById(tempSettlement);

                }

                // 增加店铺余额
                CommonShopLogic.incMoney(shop.getId(), money);

                // 店铺账户流水记录
                MallShopAccountLog log = new MallShopAccountLog();
                log.setShopId(shop.getId());
                log.setChangeObject(ShopAccountEnum.CHANGE_OBJECT_SHOP_MONEY.getCode());
                log.setChangeType(ShopAccountEnum.SHOP_MONEY_REBATE.getCode());
                log.setChangeAmount(record.getMoney().setScale(2, RoundingMode.HALF_UP));
                log.setFromId(record.getId());
                log.setFromSn(record.getSn());
                log.setParams("[]");
                CommonMallShopAccountLogLogic.createAccountLog(log);



                MallOrder tempOrder = mallOrderMapper.selectById(order.getId());
                if (tempOrder != null) {
                    tempOrder.setSettlementIs(YesNoEnum.YES.getCode());
                    tempOrder.setUpdateTime(System.currentTimeMillis() / 1000);
                    mallOrderMapper.updateById(tempOrder);
                }


            }

            // 提交事务
            transactionManager.commit(status);

        } catch (Exception e) {
            e.getMessage();
            // 回滚事务
            transactionManager.rollback(status);
            throw new RuntimeException("结算执行失败: " + e.getMessage(), e);
        }
    }

}
