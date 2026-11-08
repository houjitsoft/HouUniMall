package com.houjiang.merchant.service.impl.mall;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShopAccountLog;
import com.houjiang.merchant.common.entity.mall.MallShopBank;
import com.houjiang.merchant.common.entity.mall.MallShopWithdraw;
import com.houjiang.merchant.common.enums.NoticeEnum;
import com.houjiang.merchant.common.enums.ShopAccountEnum;
import com.houjiang.merchant.common.enums.ShopWithdrawEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallShopWithdrawMapper;
import com.houjiang.merchant.common.plugin.notice.NoticeDriver;
import com.houjiang.merchant.common.plugin.notice.vo.NoticeSmsVo;
import com.houjiang.merchant.common.util.ConfigUtils;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallShopAccountLogService;
import com.houjiang.merchant.service.mall.IMallShopBankService;
import com.houjiang.merchant.service.mall.IMallShopService;
import com.houjiang.merchant.service.mall.IMallShopWithdrawService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopWithdrawAddValidate;
import com.houjiang.merchant.validate.mall.MallShopWithdrawSearchValidate;
import com.houjiang.merchant.vo.mall.MallShopWithdrawDetailVo;
import com.houjiang.merchant.vo.mall.MallShopWithdrawListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 商家提现实现类
 * @author Amos
 */
@Service
public class MallShopWithdrawServiceImpl implements IMallShopWithdrawService {

    @Resource
    IMallShopService iMallShopService;
    @Resource
    IMallShopBankService iMallShopBankService;
    @Resource
    IMallShopAccountLogService iMallShopAccountLogService;
    @Resource
    MallShopWithdrawMapper mallShopWithdrawMapper;
    @Override
    public BigDecimal getWithdrawMoney(Integer shopId) {
        return mallShopWithdrawMapper.sum("money", new QueryWrapper<MallShopWithdraw>().eq("shop_id", shopId).eq("delete_time", 0).in("status", ShopWithdrawEnum.getInWithdraw()));
    }

    @Override
    public BigDecimal getWithdrawServiceMoney(Integer shopId) {
        return mallShopWithdrawMapper.sum("service_money", new QueryWrapper<MallShopWithdraw>().eq("shop_id", shopId).eq("delete_time", 0).eq("status", ShopWithdrawEnum.STATUS_SUCCESS.getCode()));
    }

    @Override
    public BigDecimal getWithdrawSuccessMoney(Integer shopId) {
        return mallShopWithdrawMapper.sum("real_money", new QueryWrapper<MallShopWithdraw>().eq("shop_id", shopId).eq("delete_time", 0).eq("status", ShopWithdrawEnum.STATUS_SUCCESS.getCode()));
    }

    @Override
    public PageResult<MallShopWithdrawListedVo> list(PageValidate pageValidate, MallShopWithdrawSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<MallShopWithdraw> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");

        if (StringUtils.isNotNull(searchValidate.getStart_time())) {
            if (StringUtils.isNotEmpty(searchValidate.getStart_time())) {
                queryWrapper.ge("create_time", TimeUtils.dateToTimestamp(searchValidate.getStart_time()));
            }
        }
        if (StringUtils.isNotNull(searchValidate.getEnd_time())) {
            if (StringUtils.isNotEmpty(searchValidate.getEnd_time())) {
                queryWrapper.le("create_time", TimeUtils.dateToTimestamp(searchValidate.getEnd_time()));
            }
        }


        if (StringUtils.isNotNull(searchValidate.getShopId())) {
            queryWrapper.eq("shop_id", searchValidate.getShopId());
        }
        Map<String, Object> statistics = __statistics(queryWrapper.clone());

        if (StringUtils.isNotNull(searchValidate.getStatus())) {
            queryWrapper.eq("status", searchValidate.getStatus());
        }

        IPage<MallShopWithdraw> iPage = mallShopWithdrawMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<MallShopWithdrawListedVo> list = new LinkedList<>();
        for(MallShopWithdraw item : iPage.getRecords()) {
            MallShopWithdrawListedVo vo = new MallShopWithdrawListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            vo.setShop(iMallShopService.simplifyDetail(item.getShopId()));
            vo.setStatusText(ShopWithdrawEnum.getMsgByCode(item.getStatus()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list, statistics);
    }

    @Override
    public MallShopWithdrawDetailVo detail(Integer id) {
        MallShopWithdraw model = this.getById(id);
        if (StringUtils.isNull(model)) {
            throw new OperateException("找不到商家提现详情");
        }
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("无法查看别的商家提现详情");
        }
        MallShopWithdrawDetailVo vo = new MallShopWithdrawDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setCreateTime(TimeUtils.timestampToDate(model.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(model.getUpdateTime()));
        vo.setShop(iMallShopService.simplifyDetail(model.getShopId()));
        vo.setStatusText(ShopWithdrawEnum.getMsgByCode(model.getStatus()));
        return vo;
    }

    @Override
    public MallShopWithdraw getById(Integer id) {
        return mallShopWithdrawMapper.selectById(id);
    }

    @Override
    @Transactional
    public void add(MallShopWithdrawAddValidate validate) {
        MallShopBank shopBank = iMallShopBankService.getById(validate.getBankId());
        Assert.notNull(shopBank, "银行卡不存在");
        if (shopBank.getShopId().equals(validate.getShopId()) == false) {
            throw new OperateException("该银行卡信息不是本店铺");
        }
        JSONObject config = ConfigUtils.getShopWithdrawConfig();
        if (validate.getMoney().compareTo(config.getBigDecimal("sw_min_money")) < 0) {
            throw new OperateException("提现最低金额：" + config.getBigDecimal("sw_min_money"));
        }
        if (validate.getMoney().compareTo(config.getBigDecimal("sw_max_money")) > 0) {
            throw new OperateException("提现最高金额：" + config.getBigDecimal("sw_max_money"));
        }
        MallShopWithdraw shopWithdraw = new MallShopWithdraw();
        shopWithdraw.setShopId(validate.getShopId());
        shopWithdraw.setSn(mallShopWithdrawMapper.randMakeOrderSn("sn"));
        shopWithdraw.setMoney(validate.getMoney());
        shopWithdraw.setServiceRatio(config.getBigDecimal("sw_service_ratio"));
        BigDecimal realRatio = config.getBigDecimal("sw_service_ratio").compareTo(new BigDecimal(100)) == 0 ? new BigDecimal(0) : new BigDecimal(100).subtract(config.getBigDecimal("sw_service_ratio")).divide(new BigDecimal(100));
        //shopWithdraw.setRealMoney(validate.getMoney().multiply(realRatio));
        shopWithdraw.setServiceMoney(validate.getMoney().multiply(config.getBigDecimal("sw_service_ratio").divide(new BigDecimal(100))));
        shopWithdraw.setRealMoney(validate.getMoney().subtract(shopWithdraw.getServiceMoney()));
        shopWithdraw.setStatus(ShopWithdrawEnum.STATUS_APPLY.getCode());
        shopWithdraw.setBankId(shopBank.getId());
        shopWithdraw.setBankName(shopBank.getName());
        shopWithdraw.setBankBranch(shopBank.getBranch());
        shopWithdraw.setBankUsername(shopBank.getUsername());
        shopWithdraw.setBankCardNumber(shopBank.getCardNumber());
        shopWithdraw.setCreateTime(System.currentTimeMillis() / 1000);
        shopWithdraw.setUpdateTime(System.currentTimeMillis() / 1000);
        mallShopWithdrawMapper.insert(shopWithdraw);

        // 扣减账户余额
        Boolean decBoolean = iMallShopService.decMoney(shopWithdraw.getShopId(), shopWithdraw.getMoney(), true);
        if (decBoolean.equals(false)) {
            throw new OperateException("店铺余额不足");
        }

        // 账户明细记录
        MallShopAccountLog accountLog = new MallShopAccountLog();
        accountLog.setShopId(shopWithdraw.getShopId());
        accountLog.setChangeObject(ShopAccountEnum.CHANGE_OBJECT_SHOP_MONEY.getCode());
        accountLog.setChangeType(ShopAccountEnum.SHOP_MONEY_WITHDRAW.getCode());
        accountLog.setFromId(shopWithdraw.getId());
        accountLog.setFromSn(shopWithdraw.getSn());
        accountLog.setChangeAmount(shopWithdraw.getMoney());
        accountLog.setParams("[]");
        accountLog.setAccountStatus(2);
        iMallShopAccountLogService.createAccountLog(accountLog);

        //发送通知
        try {
            String mobile = ConfigUtils.get("website", "mobile", "");
            if (StringUtils.isNotEmpty(mobile)) {
                NoticeSmsVo UserParams = new NoticeSmsVo()
                        .setScene(NoticeEnum.SCENE_SHOP_WITHDRAW_TO_PLATFORM.getCode())
                        .setMobile(mobile)
                        .setExpire(900)
                        .setParams(new String[]{
                                "shop_name:" + MerchantThreadLocal.getShopName()
                        });
                NoticeDriver.handle(UserParams);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> __statistics(QueryWrapper<MallShopWithdraw> wrapper) {
        Map<String, Object> statistics = new LinkedHashMap<>();
        // 全部
        QueryWrapper<MallShopWithdraw> allNumQuery = wrapper.clone();
        statistics.put("all", mallShopWithdrawMapper.selectCount(allNumQuery));

        // 待提现
        QueryWrapper<MallShopWithdraw> applyNumQuery = wrapper.clone();
        statistics.put("apply", mallShopWithdrawMapper.selectCount(applyNumQuery.eq("status", ShopWithdrawEnum.STATUS_APPLY.getCode())));

        // 提现中
        QueryWrapper<MallShopWithdraw> dealNumQuery = wrapper.clone();
        statistics.put("deal", mallShopWithdrawMapper.selectCount(dealNumQuery.eq("status", ShopWithdrawEnum.STATUS_DEAL.getCode())));

        // 提现成功
        QueryWrapper<MallShopWithdraw> successNumQuery = wrapper.clone();
        statistics.put("success", mallShopWithdrawMapper.selectCount(successNumQuery.eq("status", ShopWithdrawEnum.STATUS_SUCCESS.getCode())));

        // 提现失败
        QueryWrapper<MallShopWithdraw> failNumQuery = wrapper.clone();
        statistics.put("fail", mallShopWithdrawMapper.selectCount(failNumQuery.eq("status", ShopWithdrawEnum.STATUS_FAILED.getCode())));

        return statistics;
    }
}
