package com.houjiang.merchant.service.impl.pt;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallOrder;
import com.houjiang.merchant.common.entity.ms.Ms;
import com.houjiang.merchant.common.entity.pt.Pt;
import com.houjiang.merchant.common.entity.pt.PtOpen;
import com.houjiang.merchant.common.entity.pt.PtOpenJoin;
import com.houjiang.merchant.common.enums.MsEnum;
import com.houjiang.merchant.common.enums.PtEnum;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallOrderMapper;
import com.houjiang.merchant.common.mapper.pt.PtMapper;
import com.houjiang.merchant.common.mapper.pt.PtOpenJoinMapper;
import com.houjiang.merchant.common.mapper.pt.PtOpenMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallOrderGoodsService;
import com.houjiang.merchant.service.pt.IPtOpenJoinService;
import com.houjiang.merchant.service.pt.IPtOpenService;
import com.houjiang.merchant.service.pt.IPtService;
import com.houjiang.merchant.service.user.IUserService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.pt.PtOpenSearchValidate;
import com.houjiang.merchant.vo.ms.MsListedVo;
import com.houjiang.merchant.vo.pt.GoodsPtVo;
import com.houjiang.merchant.vo.pt.PtOpenDetailVo;
import com.houjiang.merchant.vo.pt.PtOpenListedVo;
import com.houjiang.merchant.vo.pt.PtOpenVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * 拼团活动实现类
 */
@Service
public class PtOpenServiceImpl implements IPtOpenService {

    @Resource
    PtMapper ptMapper;
    @Resource
    PtOpenMapper ptOpenMapper;
    @Resource
    PtOpenJoinMapper ptOpenJoinMapper;
    @Resource
    MallOrderMapper mallOrderMapper;
    @Resource
    IUserService iUserService;
    @Resource
    IMallOrderGoodsService iMallOrderGoodsService;
    @Resource
    IPtOpenJoinService iPtOpenJoinService;
    @Override
    public Boolean orderPayCheckStatus(Integer id) {
        PtOpen ptOpen = this.getById(id);
        if (StringUtils.isNull(ptOpen)) {
            return true;
        }

        if (ptOpen.getUserNum() > ptOpen.getJoinNum()) {
            return true;
        }

        Long orderPayCount = mallOrderMapper.selectCount(new QueryWrapper<MallOrder>().eq("pt_open_id", id).eq("pay_is", YesNoEnum.YES.getCode()).eq("delete_time", 0));

        // 已全部支付 更改拼团成功
        if (orderPayCount.equals(Long.valueOf(ptOpen.getUserNum()))) {
            ptOpen.setStatus(PtEnum.OPEN_STATUS_SUCCESS.getCode());
            ptOpen.setUpdateTime(System.currentTimeMillis() / 1000);
            ptOpenMapper.updateById(ptOpen);
        }
        return true;
    }

    @Override
    public PtOpen getById(Integer id) {
        return ptOpenMapper.selectOne(new QueryWrapper<PtOpen>().eq("id", id).eq("delete_time", 0));
    }

    @Override
    public PageResult<PtOpenListedVo> list(PageValidate pageValidate, PtOpenSearchValidate searchValidate) {
        Integer pageNo = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        MPJQueryWrapper<PtOpen> wrapper = new MPJQueryWrapper<>();
        wrapper.setAlias("po");
        wrapper.eq("po.shop_id", searchValidate.getShopId());
        wrapper.eq("po.delete_time", 0);

        wrapper.leftJoin("?_mall_order_goods og ON og.id=po.order_goods_id".replace("?_", GlobalConfig.tablePrefix))
                .orderByDesc("po.id");

        if (StringUtils.isNotEmpty(searchValidate.getGoods_name())) {
            wrapper.like("og.goods_name", searchValidate.getGoods_name());
        }

        if (StringUtils.isNotNull(searchValidate.getStart_time())) {
            if (StringUtils.isNotEmpty(searchValidate.getStart_time())) {
                wrapper.ge("po.create_time", TimeUtils.dateToTimestamp(searchValidate.getStart_time()));
            }
        }
        if (StringUtils.isNotNull(searchValidate.getEnd_time())) {
            if (StringUtils.isNotEmpty(searchValidate.getEnd_time())) {
                wrapper.le("po.create_time", TimeUtils.dateToTimestamp(searchValidate.getEnd_time()));
            }
        }

        Map<String, Object> statistics = __statistics(wrapper.clone());

        if (StringUtils.isNotNull(searchValidate.getStatus())) {
            wrapper.eq("po.status", searchValidate.getStatus());
        }

        wrapper.select("po.*").orderByDesc("id");
        IPage<PtOpen> iPage = ptOpenMapper.selectJoinPage(
                new Page<>(pageNo, pageSize),
                PtOpen.class,
                wrapper);

        List<PtOpenListedVo> list = new ArrayList<>();
        for (PtOpen record : iPage.getRecords()) {
            PtOpenListedVo vo = new PtOpenListedVo();
            BeanUtils.copyProperties(record, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(record.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(record.getUpdateTime()));
            vo.setCanClose(this.getCanCloseAttr(record));
            vo.setStatusText(this.getStatusTextAttr(record.getStatus()));
            vo.setOrderGoods(iMallOrderGoodsService.getSimpleById(record.getOrderGoodsId()));
            vo.setUser(iUserService.detail(record.getUserId()));
            list.add(vo);
        }
        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list, statistics);
    }

    @Override
    public PtOpenVo detail(Integer id) {
        PtOpen model = this.getById(id);
        Assert.notNull(model, "找不到该拼团信息");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能查看其他商户的拼团信息");
        }
        PtOpenVo vo = new PtOpenVo();
        BeanUtils.copyProperties(model, vo);
        vo.setCreateTime(TimeUtils.timestampToDate(model.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(model.getUpdateTime()));
        vo.setJoinList(iPtOpenJoinService.getJoinList(vo.getId()));
        return vo;
    }

    @Override
    @Transactional
    public void close(Integer id) {
        PtOpen model = this.getById(id);
        Assert.notNull(model, "找不到该拼团信息");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能关闭其他商户的拼团信息");
        }
        if (this.getCanCloseAttr(model).equals(YesNoEnum.NO.getCode())) {
            throw new OperateException("当前状态不能结束");
        }
        model.setStatus(PtEnum.OPEN_STATUS_FAILED.getCode());
        ptOpenMapper.updateById(model);
    }

    @Override
    public void orderCancelDeal(Integer ptOpenId, Integer ptOpenJoinId) {
        QueryWrapper queryWrapper = new QueryWrapper<PtOpen>();
        queryWrapper.eq("id", ptOpenId);
        queryWrapper.eq("delete_time", 0);
        queryWrapper.eq("status", PtEnum.OPEN_STATUS_WAIT.getCode());
        PtOpen ptOpenModel = new PtOpen();
        ptOpenModel.setStatus(PtEnum.OPEN_STATUS_FAILED.getCode());
        ptOpenMapper.update(ptOpenModel, queryWrapper);

        QueryWrapper queryWrapper2 = new QueryWrapper<PtOpenJoin>();
        queryWrapper2.eq("id", ptOpenJoinId);
        PtOpenJoin ptOpenJoinModel = new PtOpenJoin();
        ptOpenJoinModel.setIsRefund(YesNoEnum.YES.getCode());
        ptOpenJoinMapper.update(ptOpenJoinModel, queryWrapper2);
    }

    private Map<String, Object> __statistics(MPJQueryWrapper<PtOpen> mpjQueryWrapper) {
        Map<String, Object> statistics = new LinkedHashMap<>();
        mpjQueryWrapper.select("*");
        // 全部
        MPJQueryWrapper<PtOpen> allQuery = mpjQueryWrapper.clone();
        statistics.put("all", ptOpenMapper.selectCount(allQuery));

        // 待审核
        MPJQueryWrapper<PtOpen> waitQuery = mpjQueryWrapper.clone();
        statistics.put("wait", ptOpenMapper.selectCount(waitQuery.eq("po.status", PtEnum.OPEN_STATUS_WAIT.getCode())));

        // 审核成功
        MPJQueryWrapper<PtOpen> successQuery = mpjQueryWrapper.clone();
        statistics.put("success", ptOpenMapper.selectCount(successQuery.eq("po.status", PtEnum.OPEN_STATUS_SUCCESS.getCode())));

        // 审核失败
        MPJQueryWrapper<PtOpen> failedQuery = mpjQueryWrapper.clone();
        statistics.put("failed", ptOpenMapper.selectCount(failedQuery.eq("po.status", PtEnum.OPEN_STATUS_FAILED.getCode())));

        return statistics;
    }

    /**
     * @notes status_text 状态
     * @param $filedValue
     * @param $data
     * @return string
     * @author Amos
     */
    private String getStatusTextAttr(Integer status) {

        return PtEnum.getOpenStatusText(status);
    }

    /**
     * @notes can_close 可关闭活动
     * @return int
     * @author Amos
     */
    private Integer getCanCloseAttr(PtOpen record) {
        return record.getStatus().equals(PtEnum.OPEN_STATUS_WAIT.getCode()) && record.getTimeout() <= TimeUtils.timestamp() ? 1 : 0;
    }
}
