package com.houjiang.merchant.service.impl.mall;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.merchant.common.validation.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallOrderAfterSale;
import com.houjiang.merchant.common.entity.mall.MallOrderGoods;
import com.houjiang.merchant.common.enums.*;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallOrderAfterSaleMapper;
import com.houjiang.merchant.common.mapper.mall.MallOrderGoodsMapper;
import com.houjiang.merchant.common.plugin.notice.NoticeDriver;
import com.houjiang.merchant.common.plugin.notice.vo.NoticeSmsVo;
import com.houjiang.merchant.common.util.*;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.user.IUserService;
import com.houjiang.merchant.service.distribution.IDistributionOrderService;
import com.houjiang.merchant.service.mall.*;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallOrderGoodsSearchValidate;
import com.houjiang.merchant.validate.mallorder.*;
import com.houjiang.merchant.vo.mall.HasAfterSaleWaitDealVo;
import com.houjiang.merchant.vo.mall.MallOrderAfterSaleGoodsListedVo;
import com.houjiang.merchant.vo.mallorder.*;
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
public class MallOrderAfterSaleServiceImpl implements IMallOrderAfterSaleService {
    @Resource
    MallOrderAfterSaleMapper mallOrderAfterSaleMapper;
    @Resource
    MallOrderGoodsMapper mallOrderGoodsMapper;
    @Resource
    IUserService iUserServicel;
    @Resource
    IMallOrderService iMallOrderService;
    @Resource
    IMallOrderGoodsService iMallOrderGoodsService;
    @Resource
    IMallOrderAfteSaleLogService iMallOrderAfteSaleLogService;
    @Resource
    IDistributionOrderService iDistributionOrderService;
    @Resource
    IMallRefundService iMallRefundService;
    @Resource
    IMallShopService iMallShopService;

    @Override
    public HasAfterSaleWaitDealVo orderSendHasWait(Integer orderId) {
        QueryWrapper<MallOrderAfterSale> saleQueryWrapper = new QueryWrapper<MallOrderAfterSale>();
        saleQueryWrapper.eq("order_id", orderId);
        saleQueryWrapper.eq("delete_time", 0);
        saleQueryWrapper.orderByDesc("id");
        saleQueryWrapper.last("limit 1");
        saleQueryWrapper.in("status", OrderAfterSaleEnum.getStatusApplyArr() );
        MallOrderAfterSale sale = mallOrderAfterSaleMapper.selectOne(saleQueryWrapper);
        if (StringUtils.isNull(sale)) {
            return null;
        }
        HasAfterSaleWaitDealVo vo = new HasAfterSaleWaitDealVo();
        vo.setId(sale.getId());
        vo.setStatus(sale.getStatus());
        List<MallOrderGoods> goodsList = mallOrderGoodsMapper.selectList(new QueryWrapper<MallOrderGoods>().eq("after_sale_id", sale.getId()).eq("delete_time", 0));
        List<MallOrderAfterSaleGoodsListedVo> list = new ArrayList<>();
        if (goodsList.size() > 0) {
            for (MallOrderGoods item : goodsList) {
                MallOrderAfterSaleGoodsListedVo listedVo = new MallOrderAfterSaleGoodsListedVo();
                listedVo.setAfterSale(item.getAfterSale());
                listedVo.setAfterSaleId(item.getAfterSaleId());
                listedVo.setGoodsName(item.getGoodsName());
                list.add(listedVo);
            }
        }
        vo.setOrderGoods(list);
        return vo;
    }

    @Override
    public PageResult<OrderAfterListVo> list(PageValidate pageValidate, OrderAfterSearchValidate searchValidate) {
        Integer pageNo = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();
        Integer keyWordType = searchValidate.getKey_word_type();
        String keyWord = searchValidate.getKey_word();

        MPJQueryWrapper<MallOrderAfterSale> mpjQueryWrapper = new MPJQueryWrapper<>();
        mpjQueryWrapper.setAlias("oas");
        mpjQueryWrapper.eq("oas.shop_id", searchValidate.getShopId());
        mpjQueryWrapper
                .leftJoin("?_mall_order o ON o.id=oas.order_id".replace("?_", GlobalConfig.tablePrefix))
                .leftJoin("?_user u ON oas.user_id=u.id".replace("?_", GlobalConfig.tablePrefix))
                .eq("oas.delete_time", 0)
                .orderByDesc("oas.id");

        if(StringUtils.isNotEmpty(searchValidate.getStart_time())){
            mpjQueryWrapper.ge("oas.create_time", TimeUtils.dateToTimestamp(searchValidate.getStart_time()));
        }

        if(StringUtils.isNotEmpty(searchValidate.getEnd_time())){
            mpjQueryWrapper.le("oas.create_time", TimeUtils.dateToTimestamp(searchValidate.getEnd_time()));
        }

        if (StringUtils.isNotNull(searchValidate.getType())) {
            mpjQueryWrapper.eq("oas.type", searchValidate.getType());
        }


        if (StringUtils.isNotNull(keyWordType) && StringUtils.isNotEmpty(keyWord)) {
            //订单编号
            if (keyWordType.equals(1)) {
                mpjQueryWrapper.like("o.sn", keyWord);
            }
            //用户编号
            if (keyWordType.equals(2)) {
                mpjQueryWrapper.like("u.sn", keyWord);
            }
            //用户昵称
            if (keyWordType.equals(3)) {
                mpjQueryWrapper.like("u.nickname", keyWord);
            }
            //收货人名称
            if (keyWordType.equals(4)) {
                mpjQueryWrapper.like("o.address_contact", keyWord);
            }
            //收货人手机
            if (keyWordType.equals(5)) {
                mpjQueryWrapper.like("o.address_mobile", keyWord);
            }
            //售后订单
            if (keyWordType.equals(6)) {
                mpjQueryWrapper.like("oas.sn", keyWord);
            }
        }

        if (StringUtils.isNotEmpty(searchValidate.getGoods_name())) {
            mpjQueryWrapper.leftJoin("?_mall_order_goods og ON oas.id=og.after_sale_id".replace("?_", GlobalConfig.tablePrefix));
            mpjQueryWrapper.like("og.goods_name", searchValidate.getGoods_name());
        }

        Map<String, Object> statistics = __statistics(mpjQueryWrapper.clone());

        if (StringUtils.isNotNull(searchValidate.getStatus())) {
            mpjQueryWrapper.eq("oas.status", searchValidate.getStatus());
        }

        mpjQueryWrapper.select("oas.id, oas.create_time, oas.sn, oas.user_id, oas.order_id, oas.order_goods_id, oas.type, oas.status, oas.sub_status, oas.refund_reason, oas.refund_content, oas.refund_image, oas.refund_money, oas.refuse_content, oas.shop_id");
        IPage<OrderAfterJoinListVo> iPage = mallOrderAfterSaleMapper.selectJoinPage(
                new Page<>(pageNo, pageSize),
                OrderAfterJoinListVo.class,
                mpjQueryWrapper);

        List<OrderAfterListVo> orderList = new ArrayList<>();
        for (OrderAfterJoinListVo record : iPage.getRecords()) {
            OrderAfterListVo vo = new OrderAfterListVo();
            BeanUtils.copyProperties(record, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(record.getCreateTime()));
            vo.setStatusText(OrderAfterSaleEnum.getMsgByCode(record.getStatus()));
            vo.setSubStatusText(this.getSubStatusTextAttr(record.getSubStatus(), record.getRefundMoney()));
            vo.setTypeText(OrderAfterSaleEnum.getMsgByCode(record.getType()));
            vo.setUser(iUserServicel.detail(record.getUserId()));
            vo.setOrder(iMallOrderService.getAfterOrderDetailById(record.getOrderId()));
            MallOrderGoodsSearchValidate validate = new MallOrderGoodsSearchValidate();
            validate.setOrderId(record.getOrderId());
            validate.setShopId(record.getShopId());
            validate.setAfterSaleId(record.getId());
            vo.setOrderGoods(iMallOrderGoodsService.getListByOrderId(validate));
            orderList.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), orderList, statistics);
    }

    @Override
    public OrderAfterDetailVo detail(Integer id) {
        MallOrderAfterSale model = mallOrderAfterSaleMapper.selectById(id);
        Assert.notNull(model, "订单不存在!");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("找不到该单位的订单");
        }
        OrderAfterDetailVo vo = new OrderAfterDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setCreateTime(TimeUtils.timestampToDate(model.getCreateTime()));
        vo.setStatusText(OrderAfterSaleEnum.getStatusTextArr(model.getStatus()));
        vo.setSubStatusText(this.getSubStatusTextAttr(model.getSubStatus(), model.getRefundMoney()));
        vo.setTypeText(OrderAfterSaleEnum.getMsgByCode(model.getType()));
        vo.setUser(iUserServicel.detail(model.getUserId()));
        vo.setOrderInfo(iMallOrderService.detail(model.getOrderId()));
        vo.setRefundReasonText(OrderAfterSaleEnum.getReasonTextArr(model.getRefundReason()));
        vo.setLog(iMallOrderAfteSaleLogService.getListByOId(model.getId()));
        MallOrderGoodsSearchValidate validate = new MallOrderGoodsSearchValidate();
        validate.setOrderId(model.getOrderId());
        validate.setShopId(model.getShopId());
        validate.setAfterSaleId(model.getId());
        vo.setOrderGoods(iMallOrderGoodsService.getListByOrderId(validate));
        JSONArray refundImageArr = JSONArray.parseArray(model.getRefundImage());
        List<String> refundImage = new ArrayList<String>();
        if (refundImageArr.size() > 0) {
            for (Object item :refundImageArr) {
                String img = UrlUtils.toShopAbsoluteUrl(item.toString());
                refundImage.add(img);
            }
        }
        vo.setRefundImage(refundImage);
        vo.setCanCancel(this.getCanCancelAttr(model.getSubStatus()));
        vo.setCanAudit(this.getcanAuditAttr(model.getStatus()));
        vo.setCanRefund(this.getcanRefundAttr(model.getSubStatus()));
        vo.setUpdateTime(TimeUtils.timestampToDate(model.getUpdateTime()));
        vo.setCanReApply(this.getCanReApplyAttr(model.getSubStatus()));
        vo.setShopCanReceive(this.getShopCanReceiveAttr(model.getSubStatus()));
        vo.setShopCanDeal(this.getShopCanDealAttr(model.getSubStatus()));
        return vo;
    }

    /**
     * @notes shop_can_deal 商家可处理
     * @return int
     * @author Amos
     */
    private Integer getShopCanDealAttr(Integer subStatus) {
        return subStatus.equals(OrderAfterSaleEnum.SUB_STATUS_WAIT_SELLER_DEAL.getCode()) ? 1 : 0;
    }

    @Override
    @Transactional
    public void audit(OrderAfterAuditValidate validate) {
        if (validate.getAgree().equals(YesNoEnum.NO.getCode()) && StringUtils.isEmpty(validate.getRefuseContent())) {
            throw new OperateException("请填写拒绝内容");
        }
        OrderAfterDetailVo detail = this.detail(validate.getId());
        MallOrderAfterSale mallOrderAfterSale = new MallOrderAfterSale();
        if (detail.getCanAudit().equals(YesNoEnum.NO.getCode())) {
            throw new OperateException("当前售后状态不能审核");
        }
        String logContent = "";
        if (validate.getAgree().equals(YesNoEnum.NO.getCode())) {
            System.out.println("bbbb");
            mallOrderAfterSale.setStatus(OrderAfterSaleEnum.STATUS_FAILED.getCode());
            mallOrderAfterSale.setSubStatus(OrderAfterSaleEnum.SUB_STATUS_SELLER_REFUSE.getCode());

            logContent = "商家拒绝：" + validate.getRefuseContent();

            // 订单商品 更改状态
            List<Integer> ids = new ArrayList<>();
            JSONArray goodIdsArray = JSONArray.parseArray(detail.getOrderGoodsIds());
            if (goodIdsArray.size() == 0) {
                ids.add(detail.getOrderGoodsId());
            } else {
                for (Object item : goodIdsArray) {
                    ids.add(Integer.valueOf(item.toString()));
                }
            }
            iMallOrderGoodsService.updateAfterSaleBase(ids, OrderGoodsEnum.AFTER_SALE_NONE.getCode());
        } else {
            mallOrderAfterSale.setStatus(OrderAfterSaleEnum.STATUS_AGREE.getCode());

            logContent = "卖家同意售后，等待卖家退款";
            // 退款 等待退款
            if (detail.getType().equals(OrderAfterSaleEnum.TYPE_ONLY_MONEY.getCode())) {
                mallOrderAfterSale.setSubStatus(OrderAfterSaleEnum.SUB_STATUS_WAIT_SELLER_REFUND.getCode());
            }
            if (detail.getType().equals(OrderAfterSaleEnum.TYPE_GOODS_AND_MONEY.getCode())) {
                mallOrderAfterSale.setSubStatus(OrderAfterSaleEnum.SUB_STATUS_WAIT_BUYER_RETURN.getCode());
            }

            // 分销订单失效
            if (detail.getIsOrderAll().equals(YesNoEnum.YES.getCode())) {
                iDistributionOrderService.distributionFailedByOrder(detail.getOrderId());
            } else {
                iDistributionOrderService.distributionFailedByOrderGoods(detail.getOrderGoodsId());
            }
        }
        System.out.println(mallOrderAfterSale);
        QueryWrapper queryWrapper = new QueryWrapper<MallOrderAfterSale>();
        queryWrapper.eq("id" , detail.getId());
        queryWrapper.eq("status", detail.getStatus());
        queryWrapper.eq("sub_status", detail.getSubStatus());
        mallOrderAfterSaleMapper.update(mallOrderAfterSale, queryWrapper);

        //添加日志
        OrderAfterSaleLogCreateValidate log = new OrderAfterSaleLogCreateValidate();
        log.setAfterSaleId(detail.getId());
        log.setOperatorType(OperatorEnum.SHOP.getCode());
        log.setOperatorId(MerchantThreadLocal.getShopId());
        log.setContent(logContent);
        iMallOrderAfteSaleLogService.add(log);

        if (validate.getAgree().intValue() == 0) {
            // 退款审核拒绝通知
            try {
                NoticeSmsVo UserParams = new NoticeSmsVo()
                        .setScene(NoticeEnum.SCENE_AFTER_REFUSE_TO_USER.getCode())
                        .setMobile(detail.getUser().getMobile())
                        .setExpire(900)
                        .setParams(new String[]{
                                "nickname:" + detail.getUser().getNickname(),
                                "after_sale_sn:" + detail.getSn()
                        });
                NoticeDriver.handle(UserParams);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }

    }

    @Override
    @Transactional
    public void goodsReceive(OrderAfterGoodsReceiveValidate validate) {
        if (validate.getAgree().equals(YesNoEnum.NO.getCode()) && StringUtils.isEmpty(validate.getRefuseContent())) {
            throw new OperateException("请填写拒绝内容");
        }
        OrderAfterDetailVo detail = this.detail(validate.getId());
        MallOrderAfterSale mallOrderAfterSale = new MallOrderAfterSale();
        String logContent = "";
        if (detail.getShopCanReceive().equals(YesNoEnum.NO.getCode())) {
            throw new OperateException("当前售后状态不能收货");
        }
        if (validate.getAgree().equals(YesNoEnum.YES.getCode())) {
            mallOrderAfterSale.setSubStatus(OrderAfterSaleEnum.SUB_STATUS_WAIT_SELLER_DEAL.getCode());
            logContent = "商家已收货，等待商家处理";
        } else {
            mallOrderAfterSale.setStatus(OrderAfterSaleEnum.STATUS_FAILED.getCode());
            mallOrderAfterSale.setSubStatus(OrderAfterSaleEnum.SUB_STATUS_REFUSE_RECEIVE.getCode());
            logContent = "商家拒绝收货：" + validate.getRefuseContent();

            // 订单商品 更改状态
            List<Integer> ids = new ArrayList<>();
            JSONArray goodIdsArray = JSONArray.parseArray(detail.getOrderGoodsIds());
            if (goodIdsArray.size() == 0) {
                ids.add(detail.getOrderGoodsId());
            } else {
                for (Object item : goodIdsArray) {
                    ids.add(Integer.valueOf(item.toString()));
                }
            }
            iMallOrderGoodsService.updateAfterSaleBase(ids, OrderGoodsEnum.AFTER_SALE_NONE.getCode());
        }

        QueryWrapper queryWrapper = new QueryWrapper<MallOrderAfterSale>();
        queryWrapper.eq("id" , detail.getId());
        queryWrapper.eq("status", detail.getStatus());
        queryWrapper.eq("sub_status", detail.getSubStatus());
        mallOrderAfterSaleMapper.update(mallOrderAfterSale, queryWrapper);

        //添加日志
        OrderAfterSaleLogCreateValidate log = new OrderAfterSaleLogCreateValidate();
        log.setAfterSaleId(detail.getId());
        log.setOperatorType(OperatorEnum.SHOP.getCode());
        log.setOperatorId(MerchantThreadLocal.getShopId());
        log.setContent(logContent);
        iMallOrderAfteSaleLogService.add(log);

        if (validate.getAgree().intValue() == 0) {
            // 退款审核拒绝通知
            try {
                NoticeSmsVo UserParams = new NoticeSmsVo()
                        .setScene(NoticeEnum.SCENE_AFTER_REFUSE_TO_USER.getCode())
                        .setMobile(detail.getUser().getMobile())
                        .setExpire(900)
                        .setParams(new String[]{
                                "nickname:" + detail.getUser().getNickname(),
                                "after_sale_sn:" + detail.getSn()
                        });
                NoticeDriver.handle(UserParams);
            } catch (Exception ignored) {}
        }

    }

    @Override
    @Transactional
    public void refund(Integer id) {
        OrderAfterDetailVo detail = this.detail(id);
        MallOrderAfterSale mallOrderAfterSale = new MallOrderAfterSale();
        if (detail.getCanRefund().equals(YesNoEnum.NO.getCode())) {
            throw new OperateException("当前售后状态不能退款");
        }

        mallOrderAfterSale.setStatus(OrderAfterSaleEnum.STATUS_SUCCESS.getCode());
        mallOrderAfterSale.setSubStatus(OrderAfterSaleEnum.SUB_STATUS_SUCCESS.getCode());
        QueryWrapper queryWrapper = new QueryWrapper<MallOrderAfterSale>();
        queryWrapper.eq("id" , detail.getId());
        queryWrapper.eq("status", detail.getStatus());
        queryWrapper.eq("sub_status", detail.getSubStatus());
        mallOrderAfterSaleMapper.update(mallOrderAfterSale, queryWrapper);

        // 创建售后退款
        iMallRefundService.createRefundByOrderAfterSale(detail, MerchantThreadLocal.getShopId(), OperatorEnum.SHOP.getCode());
        //添加日志
        OrderAfterSaleLogCreateValidate log = new OrderAfterSaleLogCreateValidate();
        log.setAfterSaleId(detail.getId());
        log.setOperatorType(OperatorEnum.SHOP.getCode());
        log.setOperatorId(MerchantThreadLocal.getShopId());
        log.setContent("卖家开始退款");
        iMallOrderAfteSaleLogService.add(log);

        // 订单商品 更改状态
        List<Integer> ids = new ArrayList<>();
        JSONArray goodIdsArray = JSONArray.parseArray(detail.getOrderGoodsIds());
        if (goodIdsArray.size() == 0) {
            ids.add(detail.getOrderGoodsId());
        } else {
            for (Object item : goodIdsArray) {
                ids.add(Integer.valueOf(item.toString()));
            }
        }
        iMallOrderGoodsService.updateAfterSaleBase(ids, OrderGoodsEnum.AFTER_SALE_NONE.getCode());

        // 订单检测改为已关闭
        iMallOrderService.chechAfterSaleSave(detail.getOrderId());

        // 商家扣款
//        Boolean decMoney = iMallShopService.decMoney(detail.getShopId(), detail.getRefundMoney(), true);
//        if (decMoney.equals(false)) {
//            throw new OperateException("商家扣款失败，请稍后再试");
//        }
    }

    @Override
    @Transactional
    public void deal(Integer id) {
        OrderAfterDetailVo detail = this.detail(id);
        MallOrderAfterSale mallOrderAfterSale = new MallOrderAfterSale();
        if (detail.getShopCanDeal().equals(YesNoEnum.NO.getCode())) {
            throw new OperateException("当前售后状态不能处理");
        }

        mallOrderAfterSale.setSubStatus(OrderAfterSaleEnum.SUB_STATUS_DEAL_WAIT_SELLER_REFUND.getCode());
        QueryWrapper queryWrapper = new QueryWrapper<MallOrderAfterSale>();
        queryWrapper.eq("id" , detail.getId());
        queryWrapper.eq("status", detail.getStatus());
        queryWrapper.eq("sub_status", detail.getSubStatus());
        mallOrderAfterSaleMapper.update(mallOrderAfterSale, queryWrapper);

        //添加日志
        OrderAfterSaleLogCreateValidate log = new OrderAfterSaleLogCreateValidate();
        log.setAfterSaleId(detail.getId());
        log.setOperatorType(OperatorEnum.SHOP.getCode());
        log.setOperatorId(MerchantThreadLocal.getShopId());
        log.setContent("商家已处理，等待商家退款");
        iMallOrderAfteSaleLogService.add(log);
    }

    /**
     * @notes can_re_apply 可重新申请
     * @author Amos
     */
    private Integer getCanReApplyAttr(Integer subStatus) {
        return OrderAfterSaleEnum.getSubStatusReApplyArr().contains(subStatus) ? 1 : 0;
    }

    /**
     * @notes can_refund 可退款
     * @author Amos
     */
    private Integer getcanRefundAttr(Integer subStatus) {
        return OrderAfterSaleEnum.getSubStatusCanRefundArr().contains(subStatus) ? 1 : 0;
    }

    /**
     * @notes can_audit 可审核
     * @author Amos
     */
    private Integer getcanAuditAttr(Integer status) {
        return status.equals(OrderAfterSaleEnum.STATUS_APPLY.getCode()) ? 1 : 0;
    }

    /**
     * @notes shop_can_receive 商家可收货
     * @return int
     * @author Amos
     */
    private Integer getShopCanReceiveAttr(Integer subStatus) {
        return subStatus.equals(OrderAfterSaleEnum.SUB_STATUS_WAIT_SELLER_RECEIVE.getCode()) ? 1 : 0;
    }

    private Map<String, Object> __statistics(MPJQueryWrapper<MallOrderAfterSale> mpjQueryWrapper) {
        Integer apply     = OrderAfterSaleEnum.STATUS_APPLY.getCode();
        Integer waitRefund = OrderAfterSaleEnum.STATUS_AGREE.getCode();
        Integer success = OrderAfterSaleEnum.STATUS_SUCCESS.getCode();
        Integer fail   = OrderAfterSaleEnum.STATUS_FAILED.getCode();

        Map<String, Object> statistics = new LinkedHashMap<>();
        mpjQueryWrapper.select("DISTINCT oas.id").eq("oas.delete_time", 0);
        // 全部
        MPJQueryWrapper<MallOrderAfterSale> allNumQuery = mpjQueryWrapper.clone();
        statistics.put("all", mallOrderAfterSaleMapper.selectCount(allNumQuery));

        // 申请中
        MPJQueryWrapper<MallOrderAfterSale> applyNumQuery = mpjQueryWrapper.clone();
        statistics.put("apply", mallOrderAfterSaleMapper.selectCount(applyNumQuery.eq("oas.status", apply)));

        // 失败
        MPJQueryWrapper<MallOrderAfterSale> failNumQuery = mpjQueryWrapper.clone();
        statistics.put("fail", mallOrderAfterSaleMapper.selectCount(failNumQuery.eq("oas.status", fail)));

        // 成功
        MPJQueryWrapper<MallOrderAfterSale> successNumQuery = mpjQueryWrapper.clone();
        statistics.put("success", mallOrderAfterSaleMapper.selectCount(successNumQuery.eq("oas.status", success)));
        // 申请中
        MPJQueryWrapper<MallOrderAfterSale> waitRefundNumQuery = mpjQueryWrapper.clone();
        statistics.put("wait_refund", mallOrderAfterSaleMapper.selectCount(waitRefundNumQuery.eq("oas.status", waitRefund)));

        return statistics;
    }

    /**
     * @notes sub_status_text
     * @author Amos
     */
    private String getSubStatusTextAttr(Integer subStatus, BigDecimal refundMoney) {
        if (subStatus.equals(OrderAfterSaleEnum.SUB_STATUS_SUCCESS.getCode())) {
            return "已退款 ¥" + refundMoney;
        }
        return OrderAfterSaleEnum.getMsgByCode(subStatus);
    }

    /**
     * @notes can_cancel 可取消
     * @author Amos
     */
    private Integer getCanCancelAttr(Integer subStatus) {
        List<Integer> ret = OrderAfterSaleEnum.getSubStatusCancelArr();
        if (ret.contains(subStatus)) {
            return 1;
        } else {
            return 0;
        }
    }

}
