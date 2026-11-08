package com.houjiang.merchant.service.impl.pt;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallGoodsSku;
import com.houjiang.merchant.common.entity.pt.Pt;
import com.houjiang.merchant.common.entity.pt.PtGoods;
import com.houjiang.merchant.common.entity.pt.PtOpen;
import com.houjiang.merchant.common.enums.MsEnum;
import com.houjiang.merchant.common.enums.PtEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.pt.PtGoodsMapper;
import com.houjiang.merchant.common.mapper.pt.PtMapper;
import com.houjiang.merchant.common.mapper.pt.PtOpenMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallGoodsService;
import com.houjiang.merchant.service.mall.IMallGoodsSkuService;
import com.houjiang.merchant.service.pt.IPtGoodsService;
import com.houjiang.merchant.service.pt.IPtService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.pt.PtCreateValidate;
import com.houjiang.merchant.validate.pt.PtSearchValidate;
import com.houjiang.merchant.validate.pt.PtUpdateValidate;
import com.houjiang.merchant.vo.pt.*;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
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
 * 拼团活动实现类
 */
@Service
public class PtServiceImpl implements IPtService {

    @Resource
    PtMapper ptMapper;
    @Resource
    PtOpenMapper ptOpenMapper;
    @Resource
    PtGoodsMapper ptGoodsMapper;
    @Resource
            @Lazy
    IMallGoodsService iMallGoodsService;
    @Resource
    IMallGoodsSkuService iMallGoodsSkuService;
    @Resource
    IPtGoodsService iPtGoodsService;
    @Override
    public GoodsPtVo getGoodsPt(Integer goodsId) {
        QueryWrapper<Pt> queryWrapper = new QueryWrapper<Pt>();
        Pt model = ptMapper.selectOne(queryWrapper.eq("goods_id", goodsId).in("status", PtEnum.getStatusHasArr()).eq("delete_time", 0).last("limit 1"));
        if (StringUtils.isNull(model)) {
            return null;
        } else {
            GoodsPtVo ptVo = new GoodsPtVo();
            ptVo.setId(model.getId());
            ptVo.setGoodsId(model.getGoodsId());
            return ptVo;
        }
    }

    @Override
    public Boolean goodsHas(Integer goodsId, Integer id) {
        QueryWrapper queryWrapper = new QueryWrapper<Pt>();
        queryWrapper.eq("delete_time", 0);
        queryWrapper.eq("goods_id", goodsId);
        if (StringUtils.isNotNull(id)) {
            queryWrapper.ne("id", id);
        }
        queryWrapper.select("id");
        queryWrapper.ne("status", 2);
        queryWrapper.ne("audit_status", 30);
        queryWrapper.last("limit 1");
        Pt model = ptMapper.selectOne(queryWrapper);
        if (StringUtils.isNull(model)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public PtOpenDetailVo getPtOpen(Integer orderId) {
        PtOpen ptOpen = ptOpenMapper.selectOne(new QueryWrapper<PtOpen>().eq("order_id", orderId).eq("delete_time", 0));
        if (StringUtils.isNull(ptOpen)) {
            return null;
        } else {
            PtOpenDetailVo vo = new PtOpenDetailVo();
            BeanUtils.copyProperties(ptOpen, vo);
            vo.setStatusText(PtEnum.getOpenStatusText(ptOpen.getStatus()));
            return vo;
        }
    }

    @Override
    public Boolean checkIsSuccess(Integer id) {
        PtOpen ptOpen = ptOpenMapper.selectOne(new QueryWrapper<PtOpen>().eq("id", id).eq("delete_time", 0).eq("status", PtEnum.OPEN_STATUS_SUCCESS.getCode()));
        return StringUtils.isNull(ptOpen) ? false : true;
    }

    @Override
    public PageResult<PtListedVo> list(PageValidate pageValidate, PtSearchValidate searchValidate) {
        Integer pageNo = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        MPJQueryWrapper<Pt> wrapper = new MPJQueryWrapper<>();
        wrapper.setAlias("pt");
        wrapper.eq("pt.shop_id", searchValidate.getShopId());
        wrapper.eq("pt.delete_time", 0);

        wrapper.leftJoin("?_mall_goods g ON g.id=pt.goods_id".replace("?_", GlobalConfig.tablePrefix));

        if (StringUtils.isNotEmpty(searchValidate.getGoods_name())) {
            wrapper.like("g.name", searchValidate.getGoods_name());
        }

        if (StringUtils.isNotNull(searchValidate.getStatus())) {
            wrapper.eq("pt.status", searchValidate.getStatus());
        }

        if (StringUtils.isNotNull(searchValidate.getStart_time())) {
            if (StringUtils.isNotEmpty(searchValidate.getStart_time())) {
                wrapper.ge("pt.end_time", TimeUtils.dateToTimestamp(searchValidate.getStart_time()));
            }
        }
        if (StringUtils.isNotNull(searchValidate.getEnd_time())) {
            if (StringUtils.isNotEmpty(searchValidate.getEnd_time())) {
                wrapper.le("pt.start_time", TimeUtils.dateToTimestamp(searchValidate.getEnd_time()));
            }
        }

        Map<String, Object> statistics = __statistics(wrapper.clone());
        if (StringUtils.isNotNull(searchValidate.getAudit_status())) {
            wrapper.eq("pt.audit_status", searchValidate.getAudit_status());
        }

        wrapper.select("pt.*").orderByDesc("pt.id");
        IPage<Pt> iPage = ptMapper.selectJoinPage(
                new Page<>(pageNo, pageSize),
                Pt.class,
                wrapper);

        List<PtListedVo> list = new ArrayList<>();
        for (Pt record : iPage.getRecords()) {
            PtListedVo vo = new PtListedVo();
            BeanUtils.copyProperties(record, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(record.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(record.getUpdateTime()));
            vo.setStartTime(record.getStartTime().equals(0L) ? "-" : TimeUtils.timestampToDate(record.getStartTime()));
            vo.setEndTime(record.getEndTime().equals(0L) ? "-" : TimeUtils.timestampToDate(record.getEndTime()));
            vo.setAuditStatusText(this.getAuditStatusTextAttr(record.getAuditStatus()));
            vo.setCanClose(this.getCanCloseAttr(record));
            vo.setCanEdit(this.getCanEditAttr(record));
            vo.setCanOpen(this.getCanOpenAttr(record));
            vo.setCanSubmit(this.getCanSubmitAttr(record));
            vo.setStatusText(this.getStatusTextAttr(record.getStatus()));
            PtGoods ptGoodsMin = iPtGoodsService.getMinPriceByPtId(record.getId());
            PtGoods ptGoodsMax = iPtGoodsService.getMaxPriceByPtId(record.getId());
            vo.setGoods(iMallGoodsService.getCouponGoods(record.getGoodsId()));
            vo.setMinPrice(ptGoodsMin.getPrice());
            vo.setMaxPrice(ptGoodsMax.getPrice());
            vo.setOpenCount(this.openCount(record));
            list.add(vo);
        }
        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list, statistics);
    }

    @Override
    public PtDetailVo detail(Integer id) {
        Pt model = this.getById(id);
        if (StringUtils.isNull(model)) {
            throw new OperateException("找不到该拼团信息");
        }
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能查看其他店铺拼团信息");
        }
        PtDetailVo vo = new PtDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setCreateTime(TimeUtils.timestampToDate(model.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(model.getUpdateTime()));
        vo.setStartTime(model.getStartTime().equals(0L) ? "-" : TimeUtils.timestampToDate(model.getStartTime()));
        vo.setEndTime(model.getEndTime().equals(0L) ? "-" : TimeUtils.timestampToDate(model.getEndTime()));
        vo.setAuditStatusText(this.getAuditStatusTextAttr(model.getAuditStatus()));
        vo.setCanClose(this.getCanCloseAttr(model));
        vo.setCanEdit(this.getCanEditAttr(model));
        vo.setCanOpen(this.getCanOpenAttr(model));
        vo.setCanSubmit(this.getCanSubmitAttr(model));
        vo.setStatusText(this.getStatusTextAttr(model.getStatus()));
        vo.setGoods(iMallGoodsService.getCouponGoods(model.getGoodsId()));
        vo.setPriceText(this.getPriceTextAttr(model));
        vo.setPtSkuList(iPtGoodsService.getMsSkuList(model.getId()));
        return vo;
    }

    @Override
    public Pt getById(Integer id) {
        return ptMapper.selectById(id);
    }

    private PtOpenCountVo openCount(Pt record) {
        PtOpenCountVo vo = new PtOpenCountVo();
        vo.setPtId(record.getId());
        QueryWrapper wrapper = new QueryWrapper<PtOpen>().eq("pt_id", record.getId()).eq("delete_time", "0");
        vo.setCount(ptOpenMapper.selectCount(wrapper));
        vo.setJoinCount((ptOpenMapper.sum("join_num", wrapper)).longValue());
        QueryWrapper wrapperSuccess = new QueryWrapper<PtOpen>().eq("pt_id", record.getId()).eq("delete_time", "0").eq("status", PtEnum.OPEN_STATUS_SUCCESS.getCode());
        vo.setSuccessCount(ptOpenMapper.selectCount(wrapperSuccess));
        return vo;
    }

    private String getPriceTextAttr(Pt record) {
        PtGoods ptGoodsMin = iPtGoodsService.getMinPriceByPtId(record.getId());
        PtGoods ptGoodsMax = iPtGoodsService.getMaxPriceByPtId(record.getId());
        if (ptGoodsMin.getPrice().compareTo(ptGoodsMax.getPrice()) == 0) {
            return String.valueOf(ptGoodsMin.getPrice());
        } else {
            return ptGoodsMin.getPrice() + " ~ " + ptGoodsMax.getPrice();
        }
    }

    /**
     * @notes status_text 状态
     * @param $filedValue
     * @param $data
     * @return string
     * @author Amos
     */
    private String getStatusTextAttr(Integer status) {
        return PtEnum.getStatusTextArr(status);
    }

    /**
     * @notes can_submit 可提交
     * @param $filedValue
     * @param $data
     * @return int
     * @author Amos
     */
    private Integer getCanSubmitAttr(Pt record) {
        return record.getAuditStatus().equals(PtEnum.AUDIT_STATUS_WAIT_SUBMIT.getCode()) ? 1 : 0;
    }

    /**
     * @notes can_edit 可编辑
     * @param $filedValue
     * @param $data
     * @return int
     * @author Amos
     */
    private Integer getCanEditAttr(Pt record) {
        return record.getStatus().equals(PtEnum.STATUS_WAIT.getCode()) && PtEnum.getAuditCanEditArr().contains(record.getAuditStatus()) ? 1 : 0;
    }

    /**
     * @notes can_open 可开启活动
     * @param $filedValue
     * @param $data
     * @return int
     * @author Amos
     */
    private Integer getCanOpenAttr(Pt record) {
        return record.getStatus().equals(PtEnum.STATUS_WAIT.getCode()) && record.getAuditStatus().equals(PtEnum.AUDIT_STATUS_SUCCESS.getCode()) ? 1 : 0;
    }

    /**
     * @notes can_close 可关闭活动
     * @return int
     * @author Amos
     */
    private Integer getCanCloseAttr(Pt record) {
        return record.getStatus().equals(PtEnum.STATUS_START.getCode()) ? 1 : 0;
    }

    /**
     * @notes audit_status_text 审核状态
     * @param $filedValue
     * @param $data
     * @return string
     * @author Amos
     */
    private String getAuditStatusTextAttr(Integer auditStatus) {
        return PtEnum.getAuditStatusTextArr(auditStatus);
    }

    private Map<String, Object> __statistics(MPJQueryWrapper<Pt> mpjQueryWrapper) {

        Map<String, Object> statistics = new LinkedHashMap<>();
        mpjQueryWrapper.select("*");
        // 全部
        MPJQueryWrapper<Pt> allQuery = mpjQueryWrapper.clone();
        statistics.put("all", ptMapper.selectCount(allQuery));

        // 待审核
        MPJQueryWrapper<Pt> waitQuery = mpjQueryWrapper.clone();
        statistics.put("wait", ptMapper.selectCount(waitQuery.eq("pt.audit_status", PtEnum.AUDIT_STATUS_WAIT.getCode())));

        // 待提交
        MPJQueryWrapper<Pt> waitSubmitQuery = mpjQueryWrapper.clone();
        statistics.put("wait_submit", ptMapper.selectCount(waitSubmitQuery.eq("pt.audit_status", PtEnum.AUDIT_STATUS_WAIT_SUBMIT.getCode())));

        // 审核成功
        MPJQueryWrapper<Pt> successQuery = mpjQueryWrapper.clone();
        statistics.put("success", ptMapper.selectCount(successQuery.eq("pt.audit_status", PtEnum.AUDIT_STATUS_SUCCESS.getCode())));

        // 审核失败
        MPJQueryWrapper<Pt> failedQuery = mpjQueryWrapper.clone();
        statistics.put("failed", ptMapper.selectCount(failedQuery.eq("pt.audit_status", PtEnum.AUDIT_STATUS_FAILED.getCode())));

        return statistics;
    }

    @Override
    @Transactional
    public void del(Integer id) {
        Pt model = this.getById(id);
        Assert.notNull(model, "找不到该拼团信息");

        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能删除其他单位的拼团信息");
        }

        model.setDeleteTime(System.currentTimeMillis() / 1000);
        ptMapper.updateById(model);

        iPtGoodsService.destroyItemByPtId(model.getId());
    }

    @Override
    @Transactional
    public void close(Integer id) {
        Pt model = this.getById(id);
        Assert.notNull(model, "找不到该拼团信息");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能关闭其他单位的拼团信息");
        }
        model.setStatus(PtEnum.STATUS_END.getCode());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        ptMapper.updateById(model);
    }

    @Override
    @Transactional
    public void submit(Integer id) {
        Pt model = this.getById(id);
        Assert.notNull(model, "找不到该拼团信息");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能提交其他单位的拼团信息");
        }
        model.setAuditStatus(PtEnum.AUDIT_STATUS_WAIT.getCode());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        ptMapper.updateById(model);
    }

    @Override
    @Transactional
    public void open(Integer id) {
        Pt model = this.getById(id);
        Assert.notNull(model, "找不到该拼团信息");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能开启其他单位的拼团信息");
        }
        model.setStatus(PtEnum.STATUS_START.getCode());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        ptMapper.updateById(model);
    }

    @Override
    @Transactional
    public void add(PtCreateValidate createValidate) {
        Pt pt = new Pt();
        pt.setShopId(createValidate.getShopId());
        pt.setGoodsId(createValidate.getGoodsId());
        pt.setStartTime(TimeUtils.dateToTimestamp(createValidate.getStartTime()));
        pt.setEndTime(TimeUtils.dateToTimestamp(createValidate.getEndTime()));
        pt.setCreateTime(System.currentTimeMillis() / 1000);
        pt.setUpdateTime(System.currentTimeMillis() / 1000);
        pt.setTimeLimit(createValidate.getTimeLimit());
        pt.setUserNum(createValidate.getUserNum());
        pt.setStatus(MsEnum.STATUS_WAIT.getCode());
        pt.setAuditStatus(MsEnum.AUDIT_STATUS_WAIT_SUBMIT.getCode());
        pt.setMinPrice(0);
        pt.setMaxPrice(0);
        pt.setSaleNum(0);
        ptMapper.insert(pt);

        BigDecimal minPrice = new BigDecimal(0);
        BigDecimal maxPrice = new BigDecimal(0);
        if (createValidate.getPtSkuList().size() > 0) {
            for (PtCUSkuListedVo vo : createValidate.getPtSkuList()) {
                //判断sku是否有效
                this.checkSkuList(vo);
                //为0 的时候，就赋值
                if (minPrice.compareTo(BigDecimal.ZERO) == 0) {
                    minPrice = vo.getPrice();
                }
                if(minPrice.compareTo(vo.getPrice()) >= 0) {
                    minPrice = vo.getPrice();
                }
                if(maxPrice.compareTo(vo.getPrice()) <= 0) {
                    maxPrice = vo.getPrice();
                }

                PtGoods ptGoods = new PtGoods();
                ptGoods.setPrice(vo.getPrice());
                ptGoods.setSaleNum(0);
                ptGoods.setCreateTime(System.currentTimeMillis() / 1000);
                ptGoods.setUpdateTime(System.currentTimeMillis() / 1000);
                ptGoods.setPtId(pt.getId());
                ptGoods.setShopId(pt.getShopId());
                ptGoods.setGoodsId(pt.getGoodsId());
                ptGoods.setGoodsSkuId(vo.getGoodsSkuId());
                ptGoods.setPriceInt((vo.getPrice().multiply(new BigDecimal(100))).intValue());
                ptGoodsMapper.insert(ptGoods);
            }

            pt.setMinPrice((minPrice.multiply(new BigDecimal(100))).intValue());
            pt.setMaxPrice((maxPrice.multiply(new BigDecimal(100))).intValue());
            ptMapper.updateById(pt);
        } else {
            throw new OperateException("请提交sku");
        }
    }

    @Override
    @Transactional
    public void edit(PtUpdateValidate updateValidate) {
        Pt model = this.getById(updateValidate.getId());
        Assert.notNull(model, "找不到该拼团信息");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能开启其他单位的拼团信息");
        }
        BigDecimal minPrice = new BigDecimal(0);
        BigDecimal maxPrice = new BigDecimal(0);
        if (updateValidate.getPtSkuList().size() > 0) {
            iPtGoodsService.destroyItemByPtId(updateValidate.getId());
            for (PtCUSkuListedVo vo : updateValidate.getPtSkuList()) {
                //判断sku是否有效，是否已有秒杀记录
                this.checkSkuList(vo);
                //为0 的时候，就赋值
                if (minPrice.compareTo(BigDecimal.ZERO) == 0) {
                    minPrice = vo.getPrice();
                }
                if(minPrice.compareTo(vo.getPrice()) >= 0) {
                    minPrice = vo.getPrice();
                }
                if(maxPrice.compareTo(vo.getPrice()) <= 0) {
                    maxPrice = vo.getPrice();
                }
                PtGoods ptGoods = new PtGoods();
                ptGoods.setPrice(vo.getPrice());
                ptGoods.setSaleNum(0);
                ptGoods.setCreateTime(System.currentTimeMillis() / 1000);
                ptGoods.setUpdateTime(System.currentTimeMillis() / 1000);
                ptGoods.setPtId(model.getId());
                ptGoods.setShopId(model.getShopId());
                ptGoods.setGoodsId(model.getGoodsId());
                ptGoods.setGoodsSkuId(vo.getGoodsSkuId());
                ptGoods.setPriceInt((vo.getPrice().multiply(new BigDecimal(100))).intValue());
                ptGoodsMapper.insert(ptGoods);
            }

            model.setGoodsId(updateValidate.getGoodsId());
            model.setStartTime(TimeUtils.dateToTimestamp(updateValidate.getStartTime()));
            model.setEndTime(TimeUtils.dateToTimestamp(updateValidate.getEndTime()));
            model.setUpdateTime(System.currentTimeMillis() / 1000);
            model.setTimeLimit(updateValidate.getTimeLimit());
            model.setUserNum(updateValidate.getUserNum());
            model.setStatus(MsEnum.STATUS_WAIT.getCode());
            model.setAuditStatus(MsEnum.AUDIT_STATUS_WAIT_SUBMIT.getCode());
            model.setSaleNum(0);
            model.setMinPrice((minPrice.multiply(new BigDecimal(100))).intValue());
            model.setMaxPrice((maxPrice.multiply(new BigDecimal(100))).intValue());
            ptMapper.updateById(model);
        } else {
            throw new OperateException("请提交sku");
        }
    }

    private void checkSkuList(PtCUSkuListedVo vo) {
        MallGoodsSku goodsSku = iMallGoodsSkuService.getById(vo.getGoodsSkuId());
        if (StringUtils.isNull(goodsSku)) {
            throw new OperateException("商品规格信息错误");
        }
        if (goodsSku.getPrice().compareTo(vo.getPrice()) < 0) {
            throw new OperateException("规格团购价格必须低于销售价格");
        }
    }
}
