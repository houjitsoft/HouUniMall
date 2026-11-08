package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallOrderAfterSale;
import com.houjiang.merchant.common.entity.mall.MallRefund;
import com.houjiang.merchant.common.entity.mall.MallRefundLog;
import com.houjiang.merchant.common.entity.pt.PtOpen;
import com.houjiang.merchant.common.enums.OperatorEnum;
import com.houjiang.merchant.common.enums.OrderAfterSaleEnum;
import com.houjiang.merchant.common.enums.RefundEnum;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallRefundLogMapper;
import com.houjiang.merchant.common.mapper.mall.MallRefundMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallRefundLogService;
import com.houjiang.merchant.service.mall.IMallRefundService;
import com.houjiang.merchant.service.user.IUserService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallRefundReRefundValidate;
import com.houjiang.merchant.validate.mall.MallRefundSearchValidate;
import com.houjiang.merchant.vo.mall.MallRefundListedVo;
import com.houjiang.merchant.vo.mall.MallRefundSumVo;
import com.houjiang.merchant.vo.mallorder.OrderAfterDetailVo;
import com.houjiang.merchant.vo.mallorder.OrderDetailVo;
import com.houjiang.merchant.vo.pt.PtOpenListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家账号实现类
 */
@Service
public class MallRefundServiceImpl implements IMallRefundService {

    @Resource
    IUserService iUserService;
    @Resource
    MallRefundLogMapper mallRefundLogMapper;
    @Resource
    MallRefundMapper mallRefundMapper;
    @Resource
    IMallRefundLogService iMallRefundLogService;
    @Override
    public void createRefundByOrderAfterSale(OrderAfterDetailVo detail, Integer shopId, Integer refundOperatorType) {
        MallRefund mallRefund = new MallRefund();
        mallRefund.setShopId(detail.getShopId());
        mallRefund.setUserId(detail.getUser().getId());
        mallRefund.setSn(mallRefundMapper.randMakeOrderSn("sn"));
        mallRefund.setOrderId(detail.getOrderInfo().getId());
        mallRefund.setOrderAfterSaleId(detail.getId());
        mallRefund.setPayWay(detail.getOrderInfo().getPayWay());
        mallRefund.setFromSn(detail.getOrderInfo().getSn());
        mallRefund.setType(RefundEnum.TYPE_REFUND.getCode());
        mallRefund.setOrderMoney(new BigDecimal(detail.getOrderInfo().getPayMoney()));
        mallRefund.setMoney(detail.getRefundMoney());
        mallRefund.setTransactionSn(detail.getOrderInfo().getPayTransactionNo());
        mallRefund.setStatus(RefundEnum.STATUS_WAIT_RETURN.getCode());
        mallRefund.setCreateTime(System.currentTimeMillis() / 1000);
        mallRefund.setUpdateTime(System.currentTimeMillis() / 1000);
        mallRefundMapper.insert(mallRefund);

        mallRefundLogMapper.add(mallRefund.getId(), OperatorEnum.SHOP.getCode(), mallRefundLogMapper.randMakeOrderSn("sn"), MerchantThreadLocal.getShopId(), "", RefundEnum.STATUS_WAIT_RETURN.getCode(), mallRefund.getMoney());
    }

    @Override
    public void createRefundByOrderCancel(OrderDetailVo detail, Integer refundType) {
        MallRefund mallRefund = new MallRefund();
        mallRefund.setShopId(detail.getShopId());
        mallRefund.setUserId(detail.getUser().getId());
        mallRefund.setSn(mallRefundMapper.randMakeOrderSn("sn"));
        mallRefund.setOrderId(detail.getId());
        mallRefund.setOrderAfterSaleId(detail.getId());
        mallRefund.setPayWay(detail.getPayWay());
        mallRefund.setFromSn(detail.getSn());
        mallRefund.setType(refundType);
        mallRefund.setOrderMoney(new BigDecimal(detail.getPayMoney()));
        mallRefund.setMoney(new BigDecimal(detail.getPayMoney()));
        mallRefund.setTransactionSn(detail.getPayTransactionNo());
        mallRefund.setStatus(RefundEnum.STATUS_WAIT_RETURN.getCode());
        mallRefund.setCreateTime(System.currentTimeMillis() / 1000);
        mallRefund.setUpdateTime(System.currentTimeMillis() / 1000);
        mallRefundMapper.insert(mallRefund);

        mallRefundLogMapper.add(mallRefund.getId(), OperatorEnum.SHOP.getCode(), mallRefundLogMapper.randMakeOrderSn("sn"), MerchantThreadLocal.getShopId(), "", RefundEnum.STATUS_WAIT_RETURN.getCode(), mallRefund.getMoney());
    }

    @Override
    public BigDecimal getAllRefunMoneyByShopId(Integer shopId) {
        return mallRefundMapper.sum("money", applyShopScope(new QueryWrapper<MallRefund>(), shopId).eq("delete_time", 0));
    }

    @Override
    public BigDecimal getRefunMoneyByShopId(Integer shopId) {
        return mallRefundMapper.sum("money", applyShopScope(new QueryWrapper<MallRefund>(), shopId).eq("status", RefundEnum.STATUS_SUCCESS.getCode()).eq("delete_time", 0));
    }

    @Override
    public BigDecimal getRefundWaitMoneyByShopId(Integer shopId) {
        return mallRefundMapper.sum("money", applyShopScope(new QueryWrapper<MallRefund>(), shopId)
                .eq("status", RefundEnum.STATUS_WAIT_RETURN.getCode()).eq("delete_time", 0));
    }

    @Override
    public BigDecimal getRefunFailedMoneyByShopId(Integer shopId) {
        return mallRefundMapper.sum("money", applyShopScope(new QueryWrapper<MallRefund>(), shopId).eq("status", RefundEnum.STATUS_FAILED.getCode()).eq("delete_time", 0));
    }

    static QueryWrapper<MallRefund> applyShopScope(QueryWrapper<MallRefund> wrapper, Integer shopId) {
        if (StringUtils.isNotNull(shopId)) {
            wrapper.eq("shop_id", shopId);
        }
        return wrapper;
    }

    @Override
    public MallRefundSumVo sums(Integer shopId) {
        MallRefundSumVo vo = new MallRefundSumVo();
        vo.setAll(this.getAllRefunMoneyByShopId(shopId));
        vo.setWait(this.getRefundWaitMoneyByShopId(shopId));
        vo.setSuccess(this.getRefunMoneyByShopId(shopId));
        vo.setFailed(this.getRefunFailedMoneyByShopId(shopId));
        return vo;
    }

    @Override
    public PageResult<MallRefundListedVo> list(PageValidate pageValidate, MallRefundSearchValidate searchValidate) {
        Integer pageNo = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        MPJQueryWrapper<MallRefund> wrapper = new MPJQueryWrapper<>();
        wrapper.setAlias("r");
        if (StringUtils.isNotNull(searchValidate.getShopId())) {
            wrapper.eq("r.shop_id", searchValidate.getShopId());
        }
        wrapper.eq("r.delete_time", 0);

        wrapper.leftJoin("?_user u ON r.user_id = u.id".replace("?_", GlobalConfig.tablePrefix));

        if (StringUtils.isNotEmpty(searchValidate.getNickname())) {
            wrapper.like("u.nickname", searchValidate.getNickname());
        }
        if (StringUtils.isNotEmpty(searchValidate.getSn())) {
            wrapper.like("r.sn", searchValidate.getSn());
        }
        if (StringUtils.isNotEmpty(searchValidate.getFrom_sn())) {
            wrapper.like("r.from_sn", searchValidate.getFrom_sn());
        }

        if (StringUtils.isNotNull(searchValidate.getType())) {
            wrapper.like("r.type", searchValidate.getType());
        }

        if (StringUtils.isNotNull(searchValidate.getStart_time())) {
            if (StringUtils.isNotEmpty(searchValidate.getStart_time())) {
                wrapper.ge("r.create_time", TimeUtils.dateToTimestamp(searchValidate.getStart_time()));
            }
        }
        if (StringUtils.isNotNull(searchValidate.getEnd_time())) {
            if (StringUtils.isNotEmpty(searchValidate.getEnd_time())) {
                wrapper.le("r.create_time", TimeUtils.dateToTimestamp(searchValidate.getEnd_time()));
            }
        }

        Map<String, Object> statistics = __statistics(wrapper.clone());

        if (StringUtils.isNotNull(searchValidate.getStatus())) {
            wrapper.eq("r.status", searchValidate.getStatus());
        }

        wrapper.select("r.*").orderByDesc("id");
        IPage<MallRefund> iPage = mallRefundMapper.selectJoinPage(
                new Page<>(pageNo, pageSize),
                MallRefund.class,
                wrapper);

        List<MallRefundListedVo> list = new ArrayList<>();
        for (MallRefund record : iPage.getRecords()) {
            MallRefundListedVo vo = new MallRefundListedVo();
            BeanUtils.copyProperties(record, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(record.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(record.getUpdateTime()));
            vo.setUser(iUserService.detail(record.getUserId()));
            vo.setTypeText(RefundEnum.getTypeTextArr(record.getType()));
            vo.setStatusText(RefundEnum.getRefundStatusMsg(record.getStatus()));
            vo.setLog(iMallRefundLogService.getListByRefundId(record.getId()));
            vo.setCanReRefund(this.getCanReRefundAttr(record.getStatus()));
            list.add(vo);
        }
        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list, statistics);
    }

    @Override
    @Transactional
    public void reRefund(MallRefundReRefundValidate validate) {
        MallRefund detail = this.getById(validate.getId());
        Assert.notNull(detail, "退款不存在");
        if (detail.getStatus().equals(RefundEnum.STATUS_FAILED.getCode()) == false) {
            throw new OperateException("非失败状态不能重新退款");
        }
        if (detail.getShopId().equals(validate.getShopId()) == false) {
            throw new OperateException("不能修改其他店铺的退款状态");
        }

        long updateTime = System.currentTimeMillis() / 1000;
        int changed = mallRefundMapper.update(null, new UpdateWrapper<MallRefund>()
                .set("status", RefundEnum.STATUS_WAIT_RETURN.getCode())
                .set("update_time", updateTime)
                .eq("id", detail.getId())
                .eq("shop_id", validate.getShopId())
                .eq("status", RefundEnum.STATUS_FAILED.getCode())
                .eq("delete_time", 0));
        if (changed != 1) {
            throw new OperateException("退款状态已变化，请刷新后重试");
        }

        //保存日志
        MallRefundLog log = new MallRefundLog();
        log.setRefundId(detail.getId());
        log.setMoney(detail.getMoney());
        log.setRefundStatus(RefundEnum.STATUS_WAIT_RETURN.getCode());
        log.setOperatorId(MerchantThreadLocal.getAdminId());
        log.setOperatorType(OperatorEnum.ADMIN.getCode());
        iMallRefundLogService.createLog(log);
    }

    @Override
    @Transactional
    public void deal(MallRefundReRefundValidate validate) {
        MallRefund detail = this.getById(validate.getId());
        Assert.notNull(detail, "退款不存在");
        if (!detail.getShopId().equals(validate.getShopId())) {
            throw new OperateException("不能修改其他店铺的退款状态");
        }
        if (!detail.getStatus().equals(RefundEnum.STATUS_WAIT_RETURN.getCode())) {
            throw new OperateException("仅退款中状态可处理");
        }

        // 无外部支付通道时做本地闭环：退款中 → 退款成功
        detail.setStatus(RefundEnum.STATUS_SUCCESS.getCode());
        detail.setUpdateTime(System.currentTimeMillis() / 1000);
        mallRefundMapper.updateById(detail);

        MallRefundLog log = new MallRefundLog();
        log.setRefundId(detail.getId());
        log.setMoney(detail.getMoney());
        log.setRefundStatus(RefundEnum.STATUS_SUCCESS.getCode());
        log.setOperatorId(MerchantThreadLocal.getAdminId());
        log.setOperatorType(OperatorEnum.ADMIN.getCode());
        iMallRefundLogService.createLog(log);
    }

    @Override
    public MallRefund getById(Integer id) {
        return mallRefundMapper.selectOne(new QueryWrapper<MallRefund>().eq("id", id).eq("delete_time", 0));
    }

    /**
     * @notes can_re_refund 可重新退款
     * @return int
     * @author Amos
     */
    private Integer getCanReRefundAttr(Integer status) {
        return status.equals(RefundEnum.STATUS_FAILED.getCode()) == true ? 1 : 0;
    }


    private Map<String, Object> __statistics(MPJQueryWrapper<MallRefund> mpjQueryWrapper) {

        Map<String, Object> statistics = new LinkedHashMap<>();
        // 全部
        MPJQueryWrapper<MallRefund> allNumQuery = mpjQueryWrapper.clone();
        statistics.put("all", mallRefundMapper.selectCount(allNumQuery.select("DISTINCT r.id")));

        // 申请中
        MPJQueryWrapper<MallRefund> applyNumQuery = mpjQueryWrapper.clone();
        statistics.put("wait", mallRefundMapper.selectCount(applyNumQuery.select("DISTINCT r.id").eq("r.status", RefundEnum.STATUS_WAIT_RETURN.getCode())));

        // 失败
        MPJQueryWrapper<MallRefund> failNumQuery = mpjQueryWrapper.clone();
        statistics.put("failed", mallRefundMapper.selectCount(failNumQuery.select("DISTINCT r.id").eq("r.status", RefundEnum.STATUS_FAILED.getCode())));

        // 成功
        MPJQueryWrapper<MallRefund> successNumQuery = mpjQueryWrapper.clone();
        statistics.put("success", mallRefundMapper.selectCount(successNumQuery.select("DISTINCT r.id").eq("r.status", RefundEnum.STATUS_SUCCESS.getCode())));

        return statistics;
    }
}
