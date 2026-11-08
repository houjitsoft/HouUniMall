package com.houjiang.merchant.service.impl.mall;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.merchant.common.validation.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallExpressCompany;
import com.houjiang.merchant.common.entity.mall.MallOrder;
import com.houjiang.merchant.common.entity.mall.MallOrderAfterSale;
import com.houjiang.merchant.common.enums.*;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallExpressCompanyMapper;
import com.houjiang.merchant.common.mapper.mall.MallOrderAfterSaleMapper;
import com.houjiang.merchant.common.mapper.mall.MallOrderLogMapper;
import com.houjiang.merchant.common.mapper.mall.MallOrderMapper;
import com.houjiang.merchant.common.modelAttr.MallOrderAttr;
import com.houjiang.merchant.common.plugin.delivery.DeliveryDriver;
import com.houjiang.merchant.common.plugin.delivery.vo.KdQueryTrackParam;
import com.houjiang.merchant.common.plugin.delivery.vo.KdTrackResultVo;
import com.houjiang.merchant.common.plugin.notice.NoticeDriver;
import com.houjiang.merchant.common.plugin.notice.vo.NoticeSmsVo;
import com.houjiang.merchant.common.util.ConfigUtils;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.printer.IPrinterService;
import com.houjiang.merchant.service.user.IUserService;
import com.houjiang.merchant.service.distribution.IDistributionOrderService;
import com.houjiang.merchant.service.mall.*;
import com.houjiang.merchant.service.pt.IPtOpenService;
import com.houjiang.merchant.service.pt.IPtService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallOrderGoodsSearchValidate;
import com.houjiang.merchant.validate.mallorder.MallOrderSendDeliveryValidate;
import com.houjiang.merchant.validate.mallorder.OrderSearchValidate;
import com.houjiang.merchant.vo.mall.HasAfterSaleWaitDealVo;
import com.houjiang.merchant.vo.mall.MallOrderGoodsListedVo;
import com.houjiang.merchant.vo.mallorder.*;
import com.houjiang.merchant.vo.user.UserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * 商家账号实现类
 */
@Service
public class MallOrderServiceImpl implements IMallOrderService {

    @Resource
    MallOrderMapper mallOrderMapper;
    @Resource
    MallExpressCompanyMapper mallExpressCompanyMapper;
    @Resource
    MallOrderLogMapper mallOrderLogMapper;
    @Resource
    MallOrderAfterSaleMapper mallOrderAfterSaleMapper;
    @Resource
    IMallShopService iMallShopService;
    @Resource
    IMallOrderGoodsService iMallOrderGoodsService;
    @Resource
    IPtService iPtService;
    @Resource
    IUserService iUserService;
    @Resource
    IMallExpressCompanyService iMallExpressCompanyService;
    @Resource
    IMallOrderLogService iMallOrderLogService;
    @Resource
    IMallOrderAfterSaleService iMallOrderAfterSaleService;
    @Resource
    IMallRefundService iMallRefundService;
    @Resource
    IPtOpenService iPtOpenService;
    @Resource
    IDistributionOrderService iDistributionOrderService;
    @Resource
    IPrinterService iPrinterService;
    @Override
    public JSONObject saleDay(Integer day, Integer shopId) {
        JSONObject ret = new JSONObject();
        List<String> date = new ArrayList<String>();
        List<BigDecimal> todayAmountList = new ArrayList<BigDecimal>();
        List<Long> daysAgoTime = TimeUtils.daysAgoTime(day);
        Collections.reverse(daysAgoTime);
        for (Long item : daysAgoTime) {
            date.add(TimeUtils.timestampToDate(item, "yyyy/MM/dd"));
            todayAmountList.add(mallOrderMapper.sum("money", new QueryWrapper<MallOrder>().eq("pay_is", YesNoEnum.YES.getCode()).eq("shop_id", MerchantThreadLocal.getShopId()).eq("delete_time", 0).between("create_time", item, TimeUtils.endOfDayTimestamp(item))));
        }
        ret.put("date", date);
        ret.put("list", new JSONArray() {{
            add(new JSONObject() {{
                put("data", todayAmountList);
                put("name", "营业额");
            }});
        }});
        return ret;
    }

    @Override
    public PageResult<OrderListedVo> list(PageValidate pageValidate, OrderSearchValidate orderSearchValidate) {
        Integer pageNo = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        MPJQueryWrapper<MallOrder> orderMPJQueryWrapper = new MPJQueryWrapper<>();
        orderMPJQueryWrapper.setAlias("o");
        orderMPJQueryWrapper.eq("o.shop_id", MerchantThreadLocal.getShopId());
        Integer searchType = orderSearchValidate.getKey_word_type();
        String searchKeyword = orderSearchValidate.getKey_word();
        Integer userSearchType = orderSearchValidate.getBuyer_key_word_type();
        String searchUserKeyword = orderSearchValidate.getBuyer_key_word();
        String searchTimeType = orderSearchValidate.getTime_type();
        String startTime = orderSearchValidate.getStart_time();
        String endTime = orderSearchValidate.getEnd_time();

        // 订单搜索
        if(null != searchType && StringUtils.isNotEmpty(searchKeyword)){
            switch (searchType){
                case 1:
                    orderMPJQueryWrapper.like("og.goods_name","%"+searchKeyword+"%");
                    break;
                case 2:
                    orderMPJQueryWrapper.like("og.goods_code","%"+searchKeyword+"%");
                    break;
                case 3:
                    orderMPJQueryWrapper.like("o.sn","%"+searchKeyword+"%");
                    break;
            }
        }

        // 买家信息
        if(null != userSearchType && StringUtils.isNotEmpty(searchUserKeyword)){
            switch (userSearchType){
                case 1:
                    orderMPJQueryWrapper.like("u.nickname","%"+searchUserKeyword+"%");
                    break;
                case 2:
                    orderMPJQueryWrapper.like("u.sn","%"+searchUserKeyword+"%");
                    break;
                case 3:
                    orderMPJQueryWrapper.like("o.address_contact","%"+searchUserKeyword+"%");
                    break;
                case 4:
                    orderMPJQueryWrapper.like("o.address_mobile","%"+searchUserKeyword+"%");
                    break;
            }
        }

        // 时间搜索
        if(null != searchTimeType){
            String column = "o.create_time";
            if(searchTimeType.equals("pay")){
                column = "o.pay_time";
            }

            if(StringUtils.isNotEmpty(startTime)){
                orderMPJQueryWrapper.ge(column, TimeUtils.dateToTimestamp(startTime));
            }

            if(StringUtils.isNotEmpty(endTime)){
                orderMPJQueryWrapper.le(column, TimeUtils.dateToTimestamp(endTime));
            }
        }

        // 其它搜索
        mallOrderMapper.setSearch(orderMPJQueryWrapper, orderSearchValidate, new String[]{
                "=:from@o.from:int",
                "=:pay_status@o.pay_is:int",
                "=:pay_way@o.pay_way:int",
        });
        orderMPJQueryWrapper
                .leftJoin("?_mall_order_goods og ON o.id=og.order_id".replace("?_", GlobalConfig.tablePrefix))
                .leftJoin("?_user u ON o.user_id=u.id".replace("?_", GlobalConfig.tablePrefix))
                .leftJoin("?_mall_shop s ON o.shop_id=s.id".replace("?_", GlobalConfig.tablePrefix))
                .eq("o.delete_time", 0)
                .orderByDesc("o.id");

        Map<String, Object> statistics = __statistics(orderMPJQueryWrapper.clone());


        if (StringUtils.isNotNull(orderSearchValidate.getStatus())) {
            orderMPJQueryWrapper.eq("o.status", orderSearchValidate.getStatus());
        }

        orderMPJQueryWrapper
                .select("o.id, o.create_time, o.sn, o.user_id, o.shop_id, o.ms_id, o.pt_id, o.pt_open_id, o.pt_open_join_id,o.type,o.status,o.pay_is, o.pay_time,o.address_contact,o.delivery_type,o.need_pay_money,o.express_is, o.express_confirm_time, o.user_remark, o.shop_remark,o.address_contact, o.address_mobile, o.address_content,o.coupon_money, o.discount_money, o.express_money,u.nickname, u.sn as user_sn, u.avatar, o.shop_info");
        IPage<OrderJoinListVo> iPage = mallOrderMapper.selectJoinPage(
                new Page<>(pageNo, pageSize),
                OrderJoinListVo.class,
                orderMPJQueryWrapper);

        List<OrderListedVo> orderList = new ArrayList<>();
        for (OrderJoinListVo record : iPage.getRecords()) {
            OrderListedVo orderManageListedVo = new OrderListedVo();
            BeanUtils.copyProperties(record, orderManageListedVo);
            orderManageListedVo.setPayIsMsg(PaymentEnum.getPayStatusMsg(record.getPayIs()));
            orderManageListedVo.setStatusText(OrderEnum.getStatusDesc(record.getStatus()));
            orderManageListedVo.setPayMoney(record.getPayIs().equals(1) ? record.getNeedPayMoney().toString() : "-");
            orderManageListedVo.setCanCancel(this.getCanCancelAttr(record.getStatus()));
            orderManageListedVo.setCanConfirm(this.getCanConfirmAttr(record.getStatus()));
            orderManageListedVo.setDecMoney(this.getDecMoneyAttr(record.getDiscountMoney(), record.getCouponMoney()));
            orderManageListedVo.setCanSeeLogistics(this.getCanSeeLogisticsAttr(record.getExpressIs()));
            orderManageListedVo.setCanApplyRefund(MallOrderAttr.getCanApplyRefundAttr(record.getStatus(), record.getExpressConfirmTime()));
            orderManageListedVo.setAvatar(UrlUtils.toShopAbsoluteUrl(record.getAvatar()));
            orderManageListedVo.setCreateTime(TimeUtils.timestampToDate(record.getCreateTime()));
            orderManageListedVo.setExpressConfirmTime(TimeUtils.timestampToDate(record.getExpressConfirmTime()));
            orderManageListedVo.setExpressIs(record.getExpressIs());
            orderManageListedVo.setIsAutoCancel(this.getIsAutoCancelAttr(record.getStatus()));
            orderManageListedVo.setPayTime(TimeUtils.timestampToDate(record.getPayTime()));
            orderManageListedVo.setShopInfo(iMallShopService.simplifyDetail(record.getShopId()));
            orderManageListedVo.setTypeText(OrderEnum.getTypeTextArr(record.getType()));

            MallOrderGoodsSearchValidate validate = new MallOrderGoodsSearchValidate();
            validate.setOrderId(record.getId());
            validate.setShopId(record.getShopId());
            orderManageListedVo.setGoods(iMallOrderGoodsService.getListByOrderId(validate));
            orderList.add(orderManageListedVo);

            orderManageListedVo.setPtOpen(iPtService.getPtOpen(record.getId()));
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), orderList, statistics);
    }

    @Override
    public OrderDetailVo detail(Integer id) {
        MallOrder order = mallOrderMapper.selectOne(
                new QueryWrapper<MallOrder>()
                        .eq("id", id)
                        .eq("delete_time", 0)
                        .last("limit 1"));

        Assert.notNull(order, "订单不存在!");
        if (order.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能查看其他店铺的订单信息");
        }
        UserVo userVo = iUserService.detail(order.getUserId());
        OrderDetailVo vo = new OrderDetailVo();
        BeanUtils.copyProperties(order, vo);
        vo.setPayWay(order.getPayWay());
        vo.setPayIsMsg(PaymentEnum.getPayStatusMsg(order.getPayIs()));
        vo.setStatusText(OrderEnum.getStatusDesc(order.getStatus()));
        vo.setPayMoney(order.getPayIs().equals(1) ? order.getNeedPayMoney().toString() : "-");
        vo.setCanCancel(this.getCanCancelAttr(order.getStatus()));
        vo.setCanConfirm(this.getCanConfirmAttr(order.getStatus()));
        vo.setDecMoney(this.getDecMoneyAttr(order.getDiscountMoney(), order.getCouponMoney()));
        vo.setCanSeeLogistics(this.getCanSeeLogisticsAttr(order.getExpressIs()));
        vo.setCanApplyRefund(MallOrderAttr.getCanApplyRefundAttr(order.getStatus(), order.getExpressConfirmTime()));
        vo.setPayWayText(PaymentEnum.getPayWayMsg(order.getPayWay()));
        vo.setDeliveryTypeText(ConfigUtils.get("mall_delivery_type", "express_name", "快递发货"));
        //vo.setAvatar(UrlUtils.toShopAbsoluteUrl(order.getAvatar()));
        vo.setFromText(ClientEnum.getMsgByCode(order.getFromFeild()));
        vo.setCreateTime(TimeUtils.timestampToDate(order.getCreateTime()));
        vo.setExpressConfirmTime(order.getExpressConfirmTime().equals(0L) ? "-" : TimeUtils.timestampToDate(order.getExpressConfirmTime()));
        vo.setExpressIs(order.getExpressIs());
        vo.setExpressIsText(this.getExpressIsTextAttr(order.getExpressIs()));
        vo.setExpressTime(order.getExpressTime().equals(0L) ? "-" : TimeUtils.timestampToDate(order.getExpressTime()));
        vo.setIsAutoCancel(this.getIsAutoCancelAttr(order.getStatus()));
        vo.setPayTime(order.getPayTime().equals(0L) ? "-" : TimeUtils.timestampToDate(order.getPayTime()));
        vo.setShopInfo(iMallShopService.simplifyDetail(order.getShopId()));
        vo.setTypeText(OrderEnum.getTypeTextArr(order.getType()));
        vo.setFrom(order.getFromFeild());
        vo.setUser(userVo);
        vo.setCancelTime(order.getCancelTime().equals(0L) ? "-" : TimeUtils.timestampToDate(order.getCancelTime()));
        vo.setExpressCompany(iMallExpressCompanyService.getSingleDetail(order.getExpressCompanyId()));
        MallOrderGoodsSearchValidate validate = new MallOrderGoodsSearchValidate();
        validate.setOrderId(order.getId());
        validate.setShopId(order.getShopId());
        vo.setGoods(iMallOrderGoodsService.getListByOrderId(validate));
        vo.setPtOpen(iPtService.getPtOpen(order.getId()));
        vo.setLog(iMallOrderLogService.getListByOId(order.getId()));
        vo.setCanSend(this.getCanSendAttr(order.getStatus()));
        JSONObject address = JSONObject.parseObject(order.getAddress());
        vo.setAddressContent(order.getAddressContent() + " " + address.getString("info"));
        return vo;
    }

    /**
     * @notes can_send 可发货
     */
    private Integer getCanSendAttr(Integer status) {
        return status.equals(OrderEnum.STATUS_WAIT_SEND.getCode()) ? 1 : 0;
    }

    @Override
    public void setRemarks(Integer orderId, String remarks) {
        MallOrder order = this.getById(orderId);
        Assert.notNull(order, "订单不存在!");

        order.setShopRemark(remarks);
        order.setUpdateTime(System.currentTimeMillis() / 1000);
        mallOrderMapper.updateById(order);
    }

    @Override
    public MallOrder getById(Integer id) {
        return mallOrderMapper.selectOne(new QueryWrapper<MallOrder>().eq("id", id).eq("delete_time", 0));
    }

    @Override
    @Transactional
    public void sendDelivery(MallOrderSendDeliveryValidate sendDeliveryValidate) {
        OrderDetailVo order = this.detail(sendDeliveryValidate.getId());
        if (order.getCanSend().equals(YesNoEnum.NO.getCode())) {
            throw new OperateException("当前订单不能发货");
        }
        Integer expressId = 0;
        String expressName = "";
        String invoiceNo = "";
        if (sendDeliveryValidate.getHasExpress().equals(YesNoEnum.YES.getCode())) {
            MallExpressCompany expressCompany = mallExpressCompanyMapper.selectOne(
                    new QueryWrapper<MallExpressCompany>()
                            .eq("id", sendDeliveryValidate.getExpressCompanyId())
                            .eq("delete_time", 0)
                            .last("limit 1"));

            Assert.notNull(expressCompany, "快递公司不存在!");
            expressId = expressCompany.getId();
            expressName = expressCompany.getName();
            invoiceNo = sendDeliveryValidate.getExpressNo();
        }
        // 是否拼团成功
        if (order.getPtId() > 0) {
            if (iPtService.checkIsSuccess(order.getPtOpenId()).equals(false)) {
                throw new OperateException("请等待拼团成功再发货");
            }
        }

        //是否有售后
        HasAfterSaleWaitDealVo afterSaleWaitDealVo = iMallOrderAfterSaleService.orderSendHasWait(order.getId());

        if (StringUtils.isNotNull(afterSaleWaitDealVo) && StringUtils.isNotNull(afterSaleWaitDealVo.getId())) {
            throw new OperateException("订单商品：" + afterSaleWaitDealVo.getOrderGoods().get(0).getGoodsName() + "正在售后中，不能发货");
        }
        MallOrder mallOrder = new MallOrder();
        //BeanUtils.copyProperties(order, mallOrder);
        mallOrder.setId(order.getId());
        mallOrder.setStatus(OrderEnum.STATUS_WAIT_SEND.getCode());
        mallOrder.setStatus(OrderEnum.STATUS_WAIT_RECEIVE.getCode());
        mallOrder.setExpressIs(YesNoEnum.YES.getCode());
        mallOrder.setExpressNo(sendDeliveryValidate.getExpressNo());
        mallOrder.setExpressCompanyId(sendDeliveryValidate.getExpressCompanyId());
        mallOrder.setExpressTime(System.currentTimeMillis() / 1000);
        mallOrder.setUpdateTime(System.currentTimeMillis() / 1000);
        mallOrderMapper.updateById(mallOrder);

        // 订单日志
        Integer channel = OrderLogEnum.SHOP_DELIVERY_ORDER.getCode();
        mallOrderLogMapper.add(
                order.getId(),
                OperatorEnum.SHOP.getCode(),
                channel,
                MerchantThreadLocal.getShopId(),
                OrderLogEnum.getRecordDesc(channel)
        );
        // 发货通知
        try {
            NoticeSmsVo UserParams = new NoticeSmsVo()
                    .setScene(NoticeEnum.SCENE_ORDER_SEND_TO_USER.getCode())
                    .setMobile(order.getAddressMobile())
                    .setExpire(900)
                    .setParams(new String[]{
                            "nickname:" + order.getAddressContact(),
                            "order_sn:" + order.getSn(),
                            "express_name:" + expressName,
                            "express_no:" + invoiceNo,
                    });
            NoticeDriver.handle(UserParams);
        } catch (Exception ignored) {}
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void takeDelivery(Integer id) {
        OrderDetailVo order = this.detail(id);
        if (order.getCanConfirm().equals(YesNoEnum.NO.getCode())) {
            throw new OperateException("当前订单不能收货");
        }
        // 订单更新
        MallOrder mallOrder = new MallOrder();
        mallOrder.setId(order.getId());
        mallOrder.setStatus(OrderEnum.STATUS_FINISH.getCode());
        mallOrder.setAfterSaleDeadline(getAfterSaleDeadline());
        mallOrder.setExpressTime(System.currentTimeMillis() / 1000);
        mallOrder.setUpdateTime(System.currentTimeMillis() / 1000);
        mallOrderMapper.updateById(mallOrder);

        // 订单日志
        Integer channel = OrderLogEnum.SHOP_CONFIRM_ORDER.getCode();
        mallOrderLogMapper.add(
                order.getId(),
                OperatorEnum.SHOP.getCode(),
                channel,
                MerchantThreadLocal.getShopId(),
                OrderLogEnum.getRecordDesc(channel));
    }

    /**
     * is_auto_cancel 系统是否自动取消
     * @notes
     * @author Amos
     */
    private Object getIsAutoCancelAttr(Integer status) {
        if (status.equals(OrderEnum.STATUS_WAIT_PAY.getCode())) {
            return 0;
        } else {
            String config = ConfigUtils.get("mall-order-transaction", "auto_cancel", "1");
            return StringUtils.equals(config, "1") ? 1 : 0;
        }
    }

    /**
     * @notes 获取总优惠金额
     * @return string
     * @author Amos
     * @datetime 2024-01-05 15:17:28
     */
    private BigDecimal getDecMoneyAttr(BigDecimal discountMoney, BigDecimal couponMoney) {
        return discountMoney.add(couponMoney);
    }

    /**
     * @notes can_see_logistics 可查看物流
     * @author Amos
     */
    private Integer getCanSeeLogisticsAttr(Integer expressIs) {
        return expressIs;
    }

    /**
     * @notes can_confirm 可确认收货
     * @author Amos
     */
    private Integer getCanConfirmAttr(Integer status) {
        if (status.equals(OrderEnum.STATUS_WAIT_RECEIVE.getCode())) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * @notes can_cancel 是否可取消
     * @author Amos
     * @DateTime 2024-10-27T09:28:19+0800
     */
    private Integer getCanCancelAttr(Integer status) {
        List<Integer> statusArr = OrderEnum.getStatusCanCancelArr();
        if (statusArr.contains(status)) {
            return YesNoEnum.YES.getCode();
        } else {
            return YesNoEnum.NO.getCode();
        }
    }

    /**
     * 订单统计
     *
     * @author Amos
     * @param mpjQueryWrapper 搜索器
     * @return Map<Object>
     */
    private Map<String, Object> __statistics(MPJQueryWrapper<MallOrder> mpjQueryWrapper) {
        Integer waitPay     = OrderEnum.STATUS_WAIT_PAY.getCode();
        Integer waitDeliver = OrderEnum.STATUS_WAIT_SEND.getCode();
        Integer takeDeliver = OrderEnum.STATUS_WAIT_RECEIVE.getCode();
        Integer completed   = OrderEnum.STATUS_FINISH.getCode();
        Integer cancel      = OrderEnum.STATUS_CANCEL.getCode();
//        Integer pickUpTrue      = OrderEnum.VERIFYSTATUS_TRUE.getCode();
//        Integer pickUpFalse = OrderEnum.VERIFYSTATUS_FALSE.getCode();

        Map<String, Object> statistics = new LinkedHashMap<>();
        mpjQueryWrapper.select("DISTINCT o.id").eq("o.delete_time", 0);

        // 全部
        MPJQueryWrapper<MallOrder> allNumQuery = mpjQueryWrapper.clone();
        statistics.put("all_count", mallOrderMapper.selectCount(allNumQuery));
        // 待付款
        MPJQueryWrapper<MallOrder> waitPayNumQuery = mpjQueryWrapper.clone();
        statistics.put("pay_count", mallOrderMapper.selectCount(waitPayNumQuery.eq("o.status", waitPay)));
        // 待发货
        MPJQueryWrapper<MallOrder> waitDeliverNumQuery = mpjQueryWrapper.clone();
        statistics.put("delivery_count", mallOrderMapper.selectCount(waitDeliverNumQuery.eq("o.status", waitDeliver)));
        // status
        MPJQueryWrapper<MallOrder> takeDeliverNumQuery = mpjQueryWrapper.clone();
        statistics.put("receive_count", mallOrderMapper.selectCount(takeDeliverNumQuery.eq("o.status", takeDeliver)));
        // 已完成
        MPJQueryWrapper<MallOrder> completedNumQuery = mpjQueryWrapper.clone();
        statistics.put("finish_count", mallOrderMapper.selectCount(completedNumQuery.eq("o.status", completed)));
        // 已取消
        MPJQueryWrapper<MallOrder> cancelNumQuery = mpjQueryWrapper.clone();
        statistics.put("close_count", mallOrderMapper.selectCount(cancelNumQuery.eq("o.status", cancel)));

//        //已核销
//        MPJQueryWrapper<Order> pickupNumQuery = mpjQueryWrapper.clone();
//        statistics.put("pickupNum", orderMapper.selectCount(pickupNumQuery.eq("t.verification_status", pickUpTrue)));
//
//        //未核销
//        MPJQueryWrapper<Order> unPickupNumQuery = mpjQueryWrapper.clone();
//        statistics.put("unPickupNum", orderMapper.selectCount(unPickupNumQuery.eq("t.verification_status", pickUpFalse)));

        return statistics;
    }

    /**
     * @notes can_apply_refund 可申请售后退款
     * @author Amos
     * @DateTime 2024-12-27T11:25:19+0800
     */
    private String getExpressIsTextAttr(Integer expressIs) {
        if(expressIs.equals(YesNoEnum.YES.getCode())) {
            return "已发货";
        }
        if(expressIs.equals(YesNoEnum.NO.getCode())) {
            return "未发货";
        }
        return "";
    }

    /**
     * 当前售后时间
     *
     * @author Amos
     * @return Long
     */
    @Override
    public Long getAfterSaleDeadline() {
        float afterSalesDay = Float.parseFloat(ConfigUtils.get("mall-order-transaction", "after_sale_day", "-1"));
        long afterSaleDeadline = TimeUtils.timestamp();
        if (afterSalesDay != -1) {
            afterSaleDeadline = (long)(afterSalesDay *24 * 60 * 60) + TimeUtils.timestamp();
        }
        return afterSaleDeadline;
    }

    @Override
    public LogisticsDetailVo logistics(Integer id) {
        OrderDetailVo order = this.detail(id);
        LogisticsDetailVo ret = new LogisticsDetailVo();
        BeanUtils.copyProperties(order, ret);

        MallExpressCompany expressCompany = mallExpressCompanyMapper.selectById(order.getExpressCompanyId());
        // 查询物流
        List<KdTrackResultVo> track = new LinkedList<>();
        String trackError = null;
        JSONArray trackJSONArray = new JSONArray();

        if (StringUtils.isNotNull(expressCompany)) {
            try {
                String expressEngine = ConfigUtils.get("mall_delivery_type", "express_config", "");
                if (StringUtils.isEmpty(expressEngine)) {
                    throw new OperateException("请联系管理员配置物流设置");
                }
                JSONObject engineJ = JSONObject.parseObject(expressEngine);
                String expressType = engineJ.getString("express_type");
                // 快递查询参数
                KdQueryTrackParam kdQueryTrackParam = new KdQueryTrackParam();
                // 物流公司编码
                String expressCode = expressCompany.getCodeKdniao();
                if (expressType.equals("express_kd100")) {
                    expressCode = expressCompany.getCodeKd100();
                    if (expressCode.equals("SF") || expressCode.equals("sf") || expressCode.equals("shunfeng")) {
                        kdQueryTrackParam.setPhone(order.getAddressMobile());
                    }
                } else {
                    if (expressCode.equals("SF")) {
                        kdQueryTrackParam.setPhone(order.getAddressMobile().substring(order.getAddressMobile().length() - 4));
                    }
                }
                kdQueryTrackParam.setCom(expressCode);
                kdQueryTrackParam.setNum(order.getExpressNo());
                track = (new DeliveryDriver()).queryTrack(kdQueryTrackParam);
                if (track.size() > 0) {
                    for (KdTrackResultVo vo : track) {
                        trackJSONArray.add(new JSONObject() {{
                            put("0", vo.getTime());
                            put("1", vo.getContent());
                        }});
                    }
                }
            } catch (OperateException e) {
                trackError = e.getMsg();
            }
        }

        ret.setLogisticsError(trackError);
        ret.setLogistics(trackJSONArray);

        return ret;
    }

    @Override
    @Transactional
    public void cancel(Integer id) {
        Integer refundType = RefundEnum.TYPE_ORDER_SHOP_CANCEL.getCode();
        Integer logChannel = OrderLogEnum.SHOP_CANCEL_ORDER.getCode();
        OrderDetailVo detail = this.detail(id);
        if (detail.getCanCancel().equals(YesNoEnum.NO.getCode())) {
            throw new OperateException("当前订单已不支持取消");
        }

        MallOrder order = new MallOrder();
        BeanUtils.copyProperties(detail, order);
        order.setStatus(OrderEnum.STATUS_CANCEL.getCode());
        order.setUpdateTime(System.currentTimeMillis() / 1000);
        mallOrderMapper.updateById(order);

        // 已支付 生成退款
        if (detail.getPayIs().equals(YesNoEnum.YES.getCode())) {
            iMallRefundService.createRefundByOrderCancel(detail, refundType);
        }

        // 取消订单库存退回
        String returnSkuNum = ConfigUtils.get("mall-order-transaction", "order_cancel_return_sku_num", "0");
        if (StringUtils.equals(returnSkuNum, "1")) {
            iMallOrderGoodsService.orderCancelIncStock(detail.getId());
        }

        // 扣减商户余额
        if (detail.getPayIs().equals(YesNoEnum.YES.getCode())) {
            Boolean decMoneyStatus = iMallShopService.decMoney(detail.getShopId(), new BigDecimal(detail.getPayMoney()), true);
            if (decMoneyStatus.equals(false)) {
                throw new OperateException("商家扣款失败，请稍后再试");
            }
        }

        // 拼团 取消直接失败
        if (detail.getPtId() > 0) {
            iPtOpenService.orderPayCheckStatus(detail.getPtOpenId());
        }

        // 分销订单失效
        iDistributionOrderService.distributionFailedByOrder(detail.getId());

        // 订单日志
        mallOrderLogMapper.add(
                order.getId(),
                OperatorEnum.SHOP.getCode(),
                logChannel,
                MerchantThreadLocal.getShopId(),
                OrderLogEnum.getRecordDesc(logChannel)
        );
    }

    @Override
    public OrderAfterOrderDetailVo getAfterOrderDetailById(Integer id) {
        MallOrder order = this.getById(id);
        if (StringUtils.isNull(order)) {
            return null;
        } else {
            OrderAfterOrderDetailVo vo = new OrderAfterOrderDetailVo();
            BeanUtils.copyProperties(order, vo);
            return vo;
        }
    }

    @Override
    public void chechAfterSaleSave(Integer orderId) {
        MallOrderGoodsSearchValidate validate = new MallOrderGoodsSearchValidate();
        validate.setOrderId(orderId);
        validate.setShopId(MerchantThreadLocal.getShopId());
        List<MallOrderGoodsListedVo> goodsList = iMallOrderGoodsService.getListByOrderId(validate);
        if (goodsList.size() > 0) {
            for (MallOrderGoodsListedVo vo : goodsList) {
                MallOrderAfterSale afterSale = mallOrderAfterSaleMapper.selectById(vo.getAfterSaleId());
                if (StringUtils.isNull(afterSale) || afterSale.getStatus().equals(OrderAfterSaleEnum.STATUS_SUCCESS.getCode()) == false) {
                    return;
                }
            }
        }
        MallOrder mallOrder = new MallOrder();
        mallOrder.setId(orderId);
        mallOrder.setStatus(OrderEnum.STATUS_CANCEL.getCode());
        mallOrderMapper.updateById(mallOrder);

        // 如果有拼团，且是待发货状态就要把拼团订单取消
        MallOrder mallOrder1 = mallOrderMapper.selectOne(new QueryWrapper<MallOrder>().eq("id", orderId));
        if (mallOrder1.getStatus().equals(OrderEnum.STATUS_WAIT_SEND.getCode()) && mallOrder1.getPtOpenId() > 0) {
            iPtOpenService.orderCancelDeal(mallOrder1.getPtOpenId(), mallOrder1.getPtOpenJoinId());
        }
    }

    @Override
    public Integer getPayIsCount(Integer shopId) {
        Long count = mallOrderMapper.selectCount(new QueryWrapper<MallOrder>().eq("shop_id", shopId).eq("pay_is", YesNoEnum.YES.getCode()).eq("delete_time", 0));
        return count.intValue();
    }

    @Override
    public Integer getWaitSettlementAllOrderNum(Integer shopId) {
        Long count = mallOrderMapper.selectCount(new QueryWrapper<MallOrder>().eq("pay_is", YesNoEnum.YES.getCode()).eq("settlement_is", YesNoEnum.NO.getCode()).eq("shop_id", shopId).in("status", OrderEnum.STATUS_WAIT_SEND.getCode(), OrderEnum.STATUS_WAIT_RECEIVE.getCode(), OrderEnum.STATUS_FINISH.getCode()).eq("delete_time", 0));
        return count.intValue();
    }

    @Override
    public BigDecimal getWaitSettlementAllOrderMoeny(Integer shopId) {
        return mallOrderMapper.sum("pay_money", new QueryWrapper<MallOrder>().eq("pay_is", YesNoEnum.YES.getCode()).eq("settlement_is", YesNoEnum.NO.getCode()).eq("shop_id", shopId).in("status", OrderEnum.STATUS_WAIT_SEND.getCode(), OrderEnum.STATUS_WAIT_RECEIVE.getCode(), OrderEnum.STATUS_FINISH.getCode()).eq("delete_time", 0));
    }

    @Override
    public void print(Integer id) {
        OrderDetailVo detail = this.detail(id);
        iPrinterService.printOrder(detail, "self_print");
    }
}
