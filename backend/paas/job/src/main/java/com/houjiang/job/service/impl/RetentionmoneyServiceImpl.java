package com.houjiang.job.service.impl;

import com.houjiang.commons.dto.goods.PlatFormGoodsStateDTO;
import com.houjiang.commons.rocketmq.MqSendUtils;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.vo.base.BaseSysConfigsVO;
import com.houjiang.commons.vo.goods.PlatFormGoodsCatsVO;
import com.houjiang.commons.vo.order.OrderComplaintsVO;
import com.houjiang.commons.vo.payment.CardMemberservicesVO;
import com.houjiang.commons.vo.shop.RetentionShopInfoVO;
import com.houjiang.job.feign.ThirdPartyServiceClient;
import com.houjiang.job.mapper.RetentionmoneyMapper;
import com.houjiang.job.service.RetentionmoneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName RetentionmoneyServiceImpl
 * @Description
 * @Author Amos
 * @Date 2023/9/4 11:46
 * @Version 1.0
 * @author Amos
 */
@Service
public class RetentionmoneyServiceImpl implements RetentionmoneyService {

    @Autowired
    private RetentionmoneyMapper retentionmoneyMapper;

    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;


    /**
     * 处理商家投诉订单的提醒。
     * 该方法用于检查即将超过申诉期限的投诉订单，并向相关商家发送提醒。
     * 它首先计算出投诉订单的申诉截止时间，然后查询数据库中所有在这个时间点之前的待处理投诉订单。
     * 对于每个找到的投诉订单，它将发送一条站内信提醒商家，提醒他们投诉订单的申诉时间已不足一天。
     */
    @Override
    public void merchantprocessing() {
        // 获取当前日期并加一天，计算出申诉截止时间
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE, 1);
        //失效时间
        Date time = instance.getTime();

        // 查询申诉截止时间之前的待处理投诉订单
        List<OrderComplaintsVO> entities = retentionmoneyMapper.querycomplaintsstatus(time);
        // 如果查询结果不为空
        if (!CollectionUtils.isEmpty(entities)) {
            // 遍历每个投诉订单
            for (OrderComplaintsVO entity : entities) {
                // 提取商家ID、投诉单ID和卖家ID
                Integer shopId = entity.getShopId();
                String complaintsId = entity.getComplaintsId();
                String sellUserId = entity.getSellUserId();
                // 组装提醒内容
                String content = "您的投诉单" + complaintsId + "申诉时间已不足1天，过期将判定为投诉成立，请及时处理";

                // 发送站内信提醒商家
                // 发送站内信通知
                retentionmoneyMapper.insertSysMessage(complaintsId, sellUserId, "", 2,
                        "投诉单处理时效", content, String.valueOf(shopId));
            }
        }
    }


    /**
     * 处理商家超时未处理的投诉订单，对涉及的商家进行质保金扣除或赔付。
     * 超时未处理的投诉订单将被标记为处理中，系统会自动根据商家的质保金余额进行相应操作：
     * 如果质保金余额足够支付赔偿金额，则扣除质保金并标记订单为已赔付；
     * 如果质保金余额不足，则不进行赔付，并标记订单为未赔付。
     * 此操作旨在确保商家及时处理消费者投诉，并保障消费者的权益。
     */
    @Override
    public void merchantprocessingouttime() {
        //超时未处理投诉单
        HashMap<Object, Object> map = new HashMap<>();
        // 查询超时未处理的投诉订单
        List<OrderComplaintsVO> entities = retentionmoneyMapper.merchantprocessingouttime();
        // 遍历查询结果，对每个投诉订单进行处理
        if (!CollectionUtils.isEmpty(entities)){
            for (OrderComplaintsVO entity : entities) {
                // 获取当前投诉订单的赔偿金额
                //退费金额
                BigDecimal amount = entity.getCompensationAmount();
                // 获取当前投诉订单关联的商家ID和用户ID
                Integer shopId = entity.getShopId();
                String userId = entity.getUserId();
                // 查询商家的质保金余额
                RetentionShopInfoVO selectretention = retentionmoneyMapper.selectretention(shopId);
                //质保金余额
                BigDecimal balance = selectretention.getRetentionMoneyBalance();
                // 判断质保金余额是否足够支付赔偿金额
                if (amount.compareTo(balance) > 0) {
                    // 质保金余额不足，不进行赔付，更新订单状态为未赔付
                    retentionmoneyMapper.updateispay1(0,entity.getId());
                } else {
                    // 质保金余额足够，进行赔付操作，并更新订单状态为已赔付
                    determinedeliverytime1(userId,amount,entity.getOrderId());
                    retentionmoneyMapper.updateispay1(1,entity.getId());
                }
                // 扣除质保金，无论是否进行赔付操作都会执行此步骤
                updateshopmoney(shopId,amount);
            }
        }
    }


    /**
     * 更新商家的质保金金额。
     * 本方法每天零点执行，用于计算并更新每个商家的质保金金额。
     * 它首先获取当天零点之前和之后的时间点，然后查询这个时间范围内的所有商家订单。
     * 对于每个有订单的商家，它计算订单时间范围内的基础额度，并更新商家的质保金金额。
     * 同时，它也会更新商家的首次质保金状态。
     */
    @Override
    @Transactional
    public void updateshopmoney() {
        // 初始化日期格式和获取当前时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar instance = Calendar.getInstance();

        // 设置时间为当天零点
        instance.set(Calendar.SECOND,0);
        instance.set(Calendar.MINUTE,0);
        instance.set(Calendar.HOUR_OF_DAY,0);
        Date timePrevious = instance.getTime();

        // 设置时间为当天23点59分59秒
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND,59);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.HOUR_OF_DAY,23);
        Date time = calendar.getTime();

        // 查询当天零点前后的订单
        List<Map<String, Object>> selectorders = retentionmoneyMapper.selectorder(timePrevious,time);
        if (selectorders != null && !selectorders.isEmpty()){
            for (Map<String, Object> selectorder : selectorders) {
                Integer shopId = (Integer) selectorder.get("shop_id");

                // 解析订单创建时间
                Date orderCreateTime = null;
                try {
                    String orderTime=selectorder.get("order_create_time").toString().replace("T"," ");
                    orderCreateTime = format.parse(orderTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // 判断订单时间是否在指定范围内
                boolean b = DateUtils.belongCalendar(orderCreateTime, timePrevious, time);
                if (b) {
                    calendar.setTime(orderCreateTime);
                    // 设置时间为当天零点
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    Date time1 = calendar.getTime();
                    // 设置时间为当天23点59分59秒
                    calendar.set(Calendar.SECOND, 59);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    Date time2 = calendar.getTime();

                    // 计算商家的基础额度
                    Integer integer = countbasicquota(shopId, time1, time2);
                    // 更新商家的质保金金额
                    updateshopmoney1(shopId, new BigDecimal(integer));
                    // 更新商家的首次质保金状态
                    retentionmoneyMapper.updateFirstRetention(shopId);
                }

            }
        }

    }


    /**
     * 对于应补缴质保金而未补缴的店铺，进行限权处理。
     * 具体操作包括：检查店铺是否需要补缴质保金，若需补缴则从用户账户扣款并更新相关记录，
     * 若账户余额不足，则对店铺进行限权操作，下架店铺所有商品。
     */
    @Override
    @Transactional
    public void restrictedstore() {
        // 初始化用于存储资产详情的映射
        Map<String,Object> assetDetailMap = new HashMap<>();
        // 初始化用于存储充值记录的映射
        Map<Object, Object> record = new HashMap<>();
        // 初始化用于更新店铺基础信息的映射
        HashMap<Object, Object> map1 = new HashMap<>();

        // 查询需要补缴质保金的店铺信息
        List<RetentionShopInfoVO> selectshopbase = retentionmoneyMapper.selectshopbase();
        // 获取当前时间
        Calendar instance = Calendar.getInstance();
        Date date = new Date();

        // 遍历需要补缴质保金的店铺
        if (!CollectionUtils.isEmpty(selectshopbase)){
            for (RetentionShopInfoVO map : selectshopbase) {
                // 获取店铺ID和用户ID
                Integer shopId = map.getShopId();
                String userId = map.getUserId();
                // 获取待补缴质保金金额
                BigDecimal retentionMoneyPayment = map.getRetentionMoneyPayment();
                // 获取支付时间，并计算出应缴日期
                Date paymentTime = map.getPaymentTime();
                instance.setTime(paymentTime);
                instance.add(Calendar.DATE,3);
                Date time = instance.getTime();

                // 检查当前时间是否晚于应缴日期
                if (date.compareTo(time) >= 0){
                    // 查询用户账户余额
                    CardMemberservicesVO port = retentionmoneyMapper.queryCardMemberservicesPort(userId);
                    BigDecimal userMoney = BigDecimal.valueOf(port.getUserMoney());

                    // 检查用户账户余额是否足够支付质保金
                    if (userMoney.compareTo(retentionMoneyPayment) >= 0){
                        // 查询商家是否有未赔付订单
                        unpaidorders(retentionMoneyPayment,shopId);
                        // 充值质保金
                        addshopmoney(shopId,retentionMoneyPayment);
                        // 生成充值单号
                        StringBuffer id = new StringBuffer("C");
                        DateFormat dFormat3 = new SimpleDateFormat("yyyyMMdd");
                        String format = dFormat3.format(new Date());
                        Random random = new Random();
                        String verificationCode = String.valueOf(random.nextInt(9) + 1);
                        for (int i = 0; i < 5; i++) {
                            verificationCode += random.nextInt(10);
                        }
                        id.append(format).append(verificationCode);

                        // 插入充值记录
                        record.put("shopId", shopId);
                        record.put("payPrice", retentionMoneyPayment);
                        record.put("rechargeType", 1);
                        record.put("rechargeStatus", 1);
                        record.put("rType", 3);
                        record.put("payExplanation", "过期未补缴，平台扣款补缴");
                        record.put("rechargeNumber", id.toString());
                        retentionmoneyMapper.insertretentionmoney(record);

                        // 更新用户资产详情
                        assetDetailMap.put("assetDirection", "质保金余额");
                        assetDetailMap.put("payType",1);
                        assetDetailMap.put("userId",userId);
                        assetDetailMap.put("detailType",10);
                        assetDetailMap.put("assetType",2);
                        assetDetailMap.put("assetAmount",retentionMoneyPayment);
                        assetDetailMap.put("orderId",id.toString());
                        retentionmoneyMapper.insertDetail(assetDetailMap);

                    }else {

                        // 更新店铺基础信息，设置质保金状态为限权
                        map1.put("shopId", shopId);
                        map1.put("retentionStatus", 4);
                        retentionmoneyMapper.updateshopbase(map1);

                        // 查询并下架店铺所有商品
                        List<Integer> list = retentionmoneyMapper.selectshopgoods(shopId);
                        if (!CollectionUtils.isEmpty(list)) {
                            retentionmoneyMapper.updateshopgoods(shopId);
                            PlatFormGoodsStateDTO param = new PlatFormGoodsStateDTO();

                            HashMap<String, Object> map2 = new HashMap<>();
                            map2.put("commonIdS", list);
                            // 1 是添加 2是删除
                            map2.put("type", 2);
                            param.setCommonState(2);
                            param.setCommonIdList(list);

                            retentionmoneyMapper.updateGoodsBaseState(param);

                            // 发送MQ消息，通知下架商品
                            MqSendUtils.mqSendGoods(map2);
                        }
                    }
                }
            }
        }
    }


    /**
     * 更新商家的基础配额信息。
     * 该方法通过计算上个月的订单情况来更新每个商家的基础配额，并相应地调整商家的质保金状态。
     * 如果质保金余额不足以覆盖基础配额，将标记需要补缴的金额。
     * 同时，向商家发送站内信通知更新后的质保金信息，并更新商家基础信息表中的相关数据。
     *
     * @throws ParseException 如果日期格式化失败。
     */
    @Override
    @Transactional
    public void updatebasicquota() throws ParseException {
        // 初始化一个映射，用于存储月份时间信息。
        Map monthTime = new HashMap();
        try {
            // 获取上个月的开始和结束时间。
            monthTime = DateUtils.getLastMonthTime();
        } catch (Exception e) {
            // 异常处理，忽略错误。
        }
        // 定义日期格式化对象。
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 提取上个月的开始和结束日期字符串。
        //上月开始时间
        String startDate = String.valueOf(monthTime.get("startDate"));
        //上月结束时间
        String endDate = String.valueOf(monthTime.get("endDate"));

        // 初始化一个映射，用于存储商家配额和质保金相关信息。
        HashMap<Object, Object> paymap = new HashMap<>();
        // 查询上个月有订单的商家ID。
        List<Integer> selectshopid = retentionmoneyMapper.selectshopid(format.parse(startDate), format.parse(endDate));
        if (!CollectionUtils.isEmpty(selectshopid)){
            for (Integer shopId : selectshopid) {
                // 计算商家的基础配额。
                Integer amount = countbasicquota(shopId, format.parse(startDate), format.parse(endDate));
                // 将商家ID和基础配额信息存入映射。
                paymap.put("shopId", shopId);
                paymap.put("retentionMoneyLimit", amount);

                // 初始化BigDecimal对象，用于数学运算。
                BigDecimal bigDecimal = new BigDecimal(amount);
                BigDecimal zero = new BigDecimal(0);
                // 查询商家的质保金信息。
                RetentionShopInfoVO selectretention = retentionmoneyMapper.selectretention(shopId);
                // 获取质保金余额。
                //质保金余额
                BigDecimal balance = selectretention.getRetentionMoneyBalance();

                // 计算质保金余额减去基础配额的差额。
                //质保金余额 - 基础额度
                BigDecimal subtract = balance.subtract(bigDecimal);
                if(subtract.compareTo(zero) > 0){
                    // 如果差额大于0，表示质保金余额足够，无需补缴。
                    BigDecimal add = selectretention.getRetentionMoneyWithdrawal().add(subtract);
                    //可提现
                    paymap.put("payPrice1", add);
                    //待补缴
                    paymap.put("payPrice2", 0);
                    //质保金状态
                    paymap.put("retentionStatus", 1);
                }else if (subtract.compareTo(zero) == 0){
                    // 如果差额等于0，表示质保金余额刚好等于基础配额。
                    //待补缴
                    paymap.put("payPrice2", 0);
                    //质保金状态
                    paymap.put("retentionStatus", 1);
                }else if (subtract.compareTo(zero) < 0){
                    // 如果差额小于0，表示质保金余额不足以覆盖基础配额，需要补缴。
                    BigDecimal add = selectretention.getRetentionMoneyWithdrawal().subtract(subtract);
                    //可提现
                    paymap.put("payPrice1", 0);
                    //待补缴
                    paymap.put("payPrice2", add);
                    //质保金状态
                    paymap.put("retentionStatus", 2);
                }
                // 如果质保金余额小于0，设置质保金状态为空。
                if (balance.compareTo(zero) < 0){
                    //质保金状态
                    paymap.put("retentionStatus", null);
                }

                // 发送站内信通知商家质保金信息的更新。
                String content = "亲爱的卖家您好，根据质保金基础额度已更新为" + amount + " 元质保金，请登录商家后台查询。";
                retentionmoneyMapper.insertSysMessage("",selectretention.getUserId(),"",5,
                        "质保金额度更新",content, String.valueOf(selectretention.getShopId()));

                // 更新商家基础信息表中的相关数据。
                retentionmoneyMapper.updateshopbase(paymap);
            }
        }
    }


    /**
     * 充值商家质保金
     * 充值质保金
     */
    @Transactional
    public Integer addshopmoney(Integer shopId, BigDecimal amount) {
        // 查询当前商家的质保金信息
        RetentionShopInfoVO selectretention = retentionmoneyMapper.selectretention(shopId);
        // 计算充值后的质保金总额
        //质保金余额 + 充值金额
        BigDecimal payPrice = selectretention.getRetentionMoneyBalance().add(amount);

        // 计算充值后可提现的质保金金额
        BigDecimal payPrice1 = payPrice.subtract(selectretention.getRetentionMoneyLimit());
        if (payPrice1.compareTo(new BigDecimal(0)) == -1) {
            payPrice1 = new BigDecimal(0);
        }

        // 计算还需补缴的质保金金额
        //待补缴金额
        BigDecimal payPrice2 = selectretention.getRetentionMoneyPayment().subtract(amount);
        if (payPrice2.compareTo(new BigDecimal(0)) == -1) {
            payPrice2 = new BigDecimal(0);
        }

        // 创建一个Map用于存储更新后的质保金信息
        HashMap<Object, Object> map = new HashMap<>();
        map.put("shopId", shopId);
        map.put("payPrice", payPrice);
        map.put("payPrice1", payPrice1);
        map.put("payPrice2", payPrice2);

        // 根据质保金余额和限制金额判断商家质保金的状态
        if (payPrice.compareTo(selectretention.getRetentionMoneyLimit()) >= 0) {
            map.put("retentionStatus", 1);
        } else if (payPrice.compareTo(new BigDecimal(0)) >= 0 && payPrice2.compareTo(new BigDecimal(0)) > 0) {
            // 如果需要补缴质保金，则发送站内信通知商家
            // 发送站内信
            String content = "亲爱的卖家您好，根据平台质保金规则您需补缴" + payPrice2 + " 元质保金，请登录商家后台查询。";
            retentionmoneyMapper.insertSysMessage("", selectretention.getUserId(), "", 5,
                    "补缴质保金", content, String.valueOf(selectretention.getShopId()));

            map.put("retentionStatus", 2);
        } else if (payPrice.compareTo(new BigDecimal(0)) == -1) {
            map.put("retentionStatus", 3);
        }

        // 如果质保金状态为冻结（3），则清除支付时间

        if (selectretention.getRetentionStatus() == 3) {
            map.put("paymentTime", null);
        }

        // 更新商家质保金信息
        return retentionmoneyMapper.updateshopbase(map);
    }




    /** 查询是否有未赔付投诉单 */
    public void unpaidorders(BigDecimal amount,Integer shopId){
        //查询未赔付投诉单
        List<OrderComplaintsVO> unpaidcomplaints = retentionmoneyMapper.unpaidcomplaints(shopId);
        if (unpaidcomplaints != null && unpaidcomplaints.size() > 0){
            for (OrderComplaintsVO unpaidcomplaint : unpaidcomplaints) {
                //赔付金额
                BigDecimal decimal = unpaidcomplaint.getCompensationAmount();

                if (amount.compareTo(decimal) < 0){
                    continue;
                }

                if (amount.compareTo(decimal) >= 0){

                    if (unpaidcomplaint.getFundDestination() == 1){
                        //将赔付金额发放进用户余额
                        determinedeliverytime1(unpaidcomplaint.getUserId(),unpaidcomplaint.getCompensationAmount(),
                                unpaidcomplaint.getOrderId());
                    }

                    //修改投诉单赔付状态
                    retentionmoneyMapper.updateispay(unpaidcomplaint.getId());

                    //充值的金额足够赔付
                    amount = amount.subtract(decimal);
                }

                if (amount .compareTo(new BigDecimal(0)) <= 0){
                    return;
                }
            }
        }
    }

    /** 计算质保金基础额度 */
    public Integer countbasicquota(Integer shopId,Date startDate,Date endDate) {
        //查询质保金默认额度
        List<BaseSysConfigsVO> pos =
                retentionmoneyMapper.selectByConfigType("retention_money_default_limit");
        String amount = null;
        if (pos != null && !pos.isEmpty()){
            amount = pos.get(0).getConfigValue();
        }
        //需缴纳质保金金额
        Integer money = 0;
        //获取该时间段内 店铺的订单商品分类信息
        List<PlatFormGoodsCatsVO> selectordergoods = retentionmoneyMapper.selectordergoods(shopId, startDate, endDate);
        if (!CollectionUtils.isEmpty(selectordergoods)){
            for (PlatFormGoodsCatsVO selectordergood : selectordergoods) {
                //质保金额度
                BigDecimal refundRatio = selectordergood.getRefundRatio();
                //分类id
                Integer catId = selectordergood.getCatId();


                if (refundRatio == null){
                    //查看父类是否为null  3级继承2级  2级继承1级
                    List<Integer> selectcat = retentionmoneyMapper.selectcat(catId);

                    if (selectcat != null && !selectcat.isEmpty()){
                        if (selectcat.get(1) != null){
                            //继承2级
                            if (money < selectcat.get(1)){
                                money = selectcat.get(1);
                            }
                        }else if (selectcat.get(0) != null){
                            //继承1级
                            if (money < selectcat.get(0)) {
                                money = selectcat.get(0);
                            }
                        }else{
                            //选取通用额度
                            if (amount != null && money < Integer.parseInt(amount)) {
                                money = Integer.valueOf(amount);
                            }
                        }
                    }
                }else {
                    if (money < refundRatio.intValue()) {
                        money = refundRatio.intValue();
                    }
                }
            }
        }
        return money;
    }



    /**
     * 处理投诉退款，更新用户余额和记录消费明细。
     * @param userId 用户ID。
     * @param money 退款金额。
     * @param orderId 订单ID。
     * @return 返回处理结果，1表示处理成功，2表示处理失败。
     */
    @Transactional
    public Integer determinedeliverytime1(String userId,BigDecimal money,String orderId){
        // 初始化资产详情和管理金额的映射表
        Map<String,Object> assetDetailMap = new HashMap<>();
        Map<String,Object> ManageMoney = new HashMap<>();

        // 退回用户余额
        assetDetailMap.put("detailType", 12);
        assetDetailMap.put("assetType", 1);
        assetDetailMap.put("assetAmount", money);
        assetDetailMap.put("assetDirection", "投诉退款");
        assetDetailMap.put("payType", 1);
        assetDetailMap.put("userId", userId);
        assetDetailMap.put("orderId", orderId);

        // 组装管理金额信息，用于更新用户余额
        ManageMoney.put("money", money);
        ManageMoney.put("userId", userId);
        // 更新用户余额
        Integer integer = retentionmoneyMapper.updateCardMemberDetailPort(ManageMoney);

        // 插入资产详情记录
        Integer integer1 = retentionmoneyMapper.insertDetail(assetDetailMap);

        // 初始化消费记录映射表
        Map<String, Object> comRecord = new HashMap<String, Object>();
        // 格式化当前日期
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
        String recordDate = dateformat.format(new Date());
        // 提取日期的年、月、日
        String[] reDate = recordDate.split("-");
        String year = reDate[0];
        String month = reDate[1];
        String day = reDate[2];
        // 组装消费记录信息
        String recordTitle = "投诉退款";
        String record_desc = "投诉退款";
        int tradeTypeId = 3;
        int userType = 1;
        int recordWithdraw = 0;
        double osCommisAmount = 0;
        double osCommisReturnAmount = 0;
        comRecord.put("recordDate", recordDate);
        comRecord.put("recordMoney", money);
        comRecord.put("orderId", money);
        comRecord.put("year", year);
        comRecord.put("month", month);
        comRecord.put("day", day);
        comRecord.put("plusOrMinus", 1);
        comRecord.put("recordTitle", recordTitle);
        comRecord.put("record_desc", record_desc);
        comRecord.put("tradeTypeId", tradeTypeId);
        comRecord.put("userType", userType);
        comRecord.put("recordWithdraw", recordWithdraw);
        comRecord.put("osCommisAmount", osCommisAmount);
        comRecord.put("osCommisReturnAmount", osCommisReturnAmount);
        comRecord.put("money", money);
        comRecord.put("userId", userId);
        comRecord.put("isCurrentUser", 1);
        // 插入消费记录
        Integer integer2 = retentionmoneyMapper.insertConsumeRecordport(comRecord);

        // 检查所有操作是否成功，如果都成功则返回1，否则返回2
        if (integer > 0 && integer1 > 0 && integer2 > 0){
            return 1;
        }
        return 2;
    }



    //罚扣质保金
    /**
     * 更新商家质保金信息。
     * 通过质保金ID和扣款金额，计算更新后的质保金余额、可提现金额、待补缴金额和累计赔付金额。
     * 根据不同的条件，设置质保金状态并发送相应的站内信提醒。
     * 最后，根据计算结果更新商家质保金信息。
     *
     * @param shopId 商家ID。
     * @param amount 扣款金额。
     * @return 更新操作的影响行数。
     */
    @Transactional
    public Integer updateshopmoney(Integer shopId,BigDecimal amount){
        // 查询商家当前的质保金信息
        RetentionShopInfoVO selectretention = retentionmoneyMapper.selectretention(shopId);
        // 计算扣款后的质保金余额
        //质保金余额-
        BigDecimal payPrice = selectretention.getRetentionMoneyBalance().subtract(amount);

        // 计算质保金可提现金额
        BigDecimal payPrice1 = payPrice.subtract(selectretention.getRetentionMoneyLimit());
        // 如果可提现金额小于0，则设置为0
        if (payPrice1.compareTo(new BigDecimal(0)) == -1) {
            payPrice1 = new BigDecimal(0);
        }

        // 计算待补缴金额
        BigDecimal payPrice2 = new BigDecimal(0);
        if (payPrice.compareTo(selectretention.getRetentionMoneyLimit()) < 0) {
            //待补缴金额  基础额度 - 余额
            payPrice2 = selectretention.getRetentionMoneyLimit().subtract(payPrice);
            // 如果待补缴金额小于0，则设置为0
            if (payPrice2.compareTo(new BigDecimal(0)) == -1) {
                payPrice2 = new BigDecimal(0);
            }
        }

        // 计算累计赔付金额
        BigDecimal payPrice3 = selectretention.getAccumulatedCompensation().add(amount);
        // 创建一个Map用于存储计算结果和更新信息
        HashMap<Object, Object> paymap = new HashMap<>();
        paymap.put("payPrice", payPrice);
        paymap.put("payPrice1", payPrice1);
        paymap.put("payPrice2", payPrice2);
        paymap.put("payPrice3", payPrice3);
        paymap.put("shopId", shopId);

        // 根据质保金余额判断质保金状态，并更新Map中的相应字段
        if (payPrice.compareTo(selectretention.getRetentionMoneyLimit()) >= 0) {
            paymap.put("retentionStatus", 1);
        } else if (payPrice.compareTo(new BigDecimal(0)) >= 0 && payPrice2.compareTo(new BigDecimal(0)) > 0) {
            // 如果需要补缴质保金，则发送站内信提醒
            //发送站内信
            String content = "亲爱的卖家您好，根据平台质保金规则您需补缴" + payPrice2 + " 元质保金，请登录商家后台查询。";
            retentionmoneyMapper.insertSysMessage("",selectretention.getUserId(),"",5,
                    "补缴质保金",content, String.valueOf(selectretention.getShopId()));
            paymap.put("retentionStatus", 2);
        } else if (payPrice.compareTo(new BigDecimal(0)) == -1) {
            // 如果质保金余额小于0，则发送站内信提醒，并记录补缴时间
            String content = "亲爱的卖家您好，根据平台质保金规则您需补缴" + payPrice2 + " 元质保金，请登录商家后台查询。";
            retentionmoneyMapper.insertSysMessage("",selectretention.getUserId(),"",5,
                    "补缴质保金",content, String.valueOf(selectretention.getShopId()));
            paymap.put("retentionStatus", 3);
            paymap.put("paymentTime", new Date());
        }
        // 如果之前的状态是待补缴（3），则清除补缴时间
        if(selectretention.getRetentionStatus() == 3){
            paymap.put("paymentTime", null);
        }
        // 更新商家质保金信息
        return retentionmoneyMapper.updateshopbase(paymap);
    }



    //罚扣质保金
    /**
     * 更新商家质保金信息，包括可提现金额和待补缴金额。
     * @param shopId 商家ID。
     * @param amount 需要更新的质保金金额。
     * @return 更新操作的结果，即影响的行数。
     */
    @Transactional
    public Integer updateshopmoney1(Integer shopId,BigDecimal amount){
        // 根据商家ID查询当前商家的质保金信息
        RetentionShopInfoVO selectretention = retentionmoneyMapper.selectretention(shopId);
        // 获取商家当前的质保金余额
        //质保金余额
        BigDecimal balance = selectretention.getRetentionMoneyBalance();

        // 创建一个Map用于存储更新后的商家质保金信息
        HashMap<Object, Object> paymap = new HashMap<>();

        // 计算更新后的可提现金额
        if (balance.subtract(amount).compareTo(BigDecimal.valueOf(0)) > 0){
            paymap.put("payPrice1", balance.subtract(amount));
        }else{
            paymap.put("payPrice1", 0);
        }

        // 计算更新后的待补缴金额
        if (amount.subtract(balance).compareTo(BigDecimal.valueOf(0)) > 0){
            paymap.put("payPrice2", amount.subtract(balance));
        }else{
            paymap.put("payPrice2", 0);
        }

        // 设置商家ID和更新后的质保金限额
        paymap.put("shopId", shopId);
        paymap.put("retentionMoneyLimit", amount);

        // 发送站内信通知商家需要补缴质保金
        String content = "亲爱的卖家您好，根据平台质保金规则您需补缴" + amount + " 元质保金，请登录商家后台查询。";
        retentionmoneyMapper.insertSysMessage("",selectretention.getUserId(),"",5,
                "补缴质保金",content, String.valueOf(selectretention.getShopId()));

        // 发送手机短信通知商家需要补缴质保金
        thirdPartyServiceClient.sendRetentionMoneyMessage(selectretention.getUserMobile(),amount.toString());

        // 设置更新时间 and 质保金状态（假设2为已补缴）
        paymap.put("paymentTime", new Date());
        paymap.put("retentionStatus", 2);

        // 更新商家质保金信息
        return retentionmoneyMapper.updateshopbase(paymap);
    }

}
