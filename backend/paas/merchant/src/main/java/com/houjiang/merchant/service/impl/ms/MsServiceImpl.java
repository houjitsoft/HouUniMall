package com.houjiang.merchant.service.impl.ms;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.coupon.Coupon;
import com.houjiang.merchant.common.entity.mall.MallGoodsSku;
import com.houjiang.merchant.common.entity.ms.Ms;
import com.houjiang.merchant.common.entity.ms.MsGoods;
import com.houjiang.merchant.common.enums.MsEnum;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.ms.MsGoodsMapper;
import com.houjiang.merchant.common.mapper.ms.MsMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallGoodsService;
import com.houjiang.merchant.service.mall.IMallGoodsSkuService;
import com.houjiang.merchant.service.ms.IMsGoodsService;
import com.houjiang.merchant.service.ms.IMsOrderService;
import com.houjiang.merchant.service.ms.IMsService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.ms.MsCreateValidate;
import com.houjiang.merchant.validate.ms.MsOpenValidate;
import com.houjiang.merchant.validate.ms.MsSearchValidate;
import com.houjiang.merchant.validate.ms.MsUpdateValidate;
import com.houjiang.merchant.vo.ms.GoodsMsVo;
import com.houjiang.merchant.vo.ms.MsCUSkuListedVo;
import com.houjiang.merchant.vo.ms.MsDetailVo;
import com.houjiang.merchant.vo.ms.MsListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * 拼团活动实现类
 */
@Service
public class MsServiceImpl implements IMsService {

    @Resource
    IMallGoodsSkuService iMallGoodsSkuService;
    @Resource
    IMallGoodsService iMallGoodsService;
    @Resource
    MsMapper msMapper;
    @Resource
    MsGoodsMapper msGoodsMapper;
    @Resource
    IMsGoodsService iMsGoodsService;
    @Resource
    IMsOrderService iMsOrderService;
    @Override
    public GoodsMsVo getGoodsMs(Integer goodsId) {
        QueryWrapper<Ms> queryWrapper = new QueryWrapper<Ms>();
        Ms model = msMapper.selectOne(queryWrapper.eq("goods_id", goodsId).in("status", MsEnum.getStatusHasArr()).eq("delete_time", 0).last("limit 1"));
        if (StringUtils.isNull(model)) {
            return null;
        } else {
            GoodsMsVo msVo = new GoodsMsVo();
            msVo.setId(model.getId());
            msVo.setGoodsId(model.getGoodsId());
            return msVo;
        }
    }

    @Override
    public Boolean goodsHas(Integer goodsId, Integer id) {
        QueryWrapper queryWrapper = new QueryWrapper<Ms>();
        queryWrapper.eq("delete_time", 0);
        queryWrapper.eq("goods_id", goodsId);
        if (StringUtils.isNotNull(id)) {
            queryWrapper.ne("id", id);
        }
        queryWrapper.ne("status", 2);
        queryWrapper.select("id");
        queryWrapper.last("limit 1");
        Ms model = msMapper.selectOne(queryWrapper);
        if (StringUtils.isNull(model)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public PageResult<MsListedVo> list(PageValidate pageValidate, MsSearchValidate searchValidate) {
        Integer pageNo = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        MPJQueryWrapper<Ms> wrapper = new MPJQueryWrapper<>();
        wrapper.setAlias("ms");
        wrapper.eq("ms.shop_id", searchValidate.getShopId());
        wrapper.eq("ms.delete_time", 0);

        wrapper.leftJoin("?_mall_goods g ON g.id=ms.goods_id".replace("?_", GlobalConfig.tablePrefix));

        if (StringUtils.isNotEmpty(searchValidate.getGoods_name())) {
            wrapper.like("g.name", searchValidate.getGoods_name());
        }

        if (StringUtils.isNotNull(searchValidate.getStatus())) {
            wrapper.eq("ms.status", searchValidate.getStatus());
        }

        if (StringUtils.isNotNull(searchValidate.getStart_time())) {
            if (StringUtils.isNotEmpty(searchValidate.getStart_time())) {
                wrapper.ge("ms.end_time", TimeUtils.dateToTimestamp(searchValidate.getStart_time()));
            }
        }
        if (StringUtils.isNotNull(searchValidate.getEnd_time())) {
            if (StringUtils.isNotEmpty(searchValidate.getEnd_time())) {
                wrapper.le("ms.start_time", TimeUtils.dateToTimestamp(searchValidate.getEnd_time()));
            }
        }

        Map<String, Object> statistics = __statistics(wrapper.clone());
        if (StringUtils.isNotNull(searchValidate.getAudit_status())) {
            wrapper.eq("ms.audit_status", searchValidate.getAudit_status());
        }

        wrapper.select("ms.*").orderByDesc("id");
        IPage<Ms> iPage = msMapper.selectJoinPage(
                new Page<>(pageNo, pageSize),
                Ms.class,
                wrapper);

        List<MsListedVo> list = new ArrayList<>();
        for (Ms record : iPage.getRecords()) {
            MsListedVo vo = new MsListedVo();
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
            vo.setPriceText(this.getPriceTextAttr(record));
            vo.setGoods(iMallGoodsService.detail(record.getGoodsId()));
            vo.setOrderCount(iMsOrderService.getOrderCount(record));
            list.add(vo);
        }
        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list, statistics);
    }

    @Override
    public MsDetailVo detail(Integer id) {
        Ms model = this.getById(id);
        if (StringUtils.isNull(model)) {
            throw new OperateException("找不到该秒杀信息");
        }
        MsDetailVo vo = new MsDetailVo();
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
        vo.setPriceText(this.getPriceTextAttr(model));
        vo.setGoods(iMallGoodsService.detail(model.getGoodsId()));
        //vo.setOrderCount(iMsOrderService.getOrderCount(model));
        vo.setMsSkuList(iMsGoodsService.getMsSkuList(model.getId()));
        return vo;
    }

    @Override
    public Ms getById(Integer id) {
        return msMapper.selectById(id);
    }

    @Override
    @Transactional
    public void del(Integer id) {
        Ms model = this.getById(id);
        Assert.notNull(model, "找不到该秒杀信息");

        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能删除其他单位的秒杀信息");
        }

        model.setDeleteTime(System.currentTimeMillis() / 1000);
        msMapper.updateById(model);

        iMsGoodsService.destroyItemByMsId(model.getId());
    }

    @Override
    @Transactional
    public void close(Integer id) {
        Ms model = this.getById(id);
        Assert.notNull(model, "找不到该秒杀信息");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能关闭其他单位的秒杀信息");
        }
        model.setStatus(MsEnum.STATUS_END.getCode());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        msMapper.updateById(model);
    }

    @Override
    @Transactional
    public void submit(Integer id) {
        Ms model = this.getById(id);
        Assert.notNull(model, "找不到该秒杀信息");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能关闭其他单位的秒杀信息");
        }
        model.setAuditStatus(MsEnum.AUDIT_STATUS_WAIT.getCode());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        msMapper.updateById(model);
    }

    @Override
    @Transactional
    public void add(MsCreateValidate msCreateValidate) {
        Ms ms = new Ms();
        ms.setShopId(msCreateValidate.getShopId());
        ms.setGoodsId(msCreateValidate.getGoodsId());
        ms.setStartTime(TimeUtils.dateToTimestamp(msCreateValidate.getStartTime()));
        ms.setEndTime(TimeUtils.dateToTimestamp(msCreateValidate.getEndTime()));
        ms.setCreateTime(System.currentTimeMillis() / 1000);
        ms.setUpdateTime(System.currentTimeMillis() / 1000);
        ms.setOrderLimit(msCreateValidate.getOrderLimit());
        ms.setStatus(MsEnum.STATUS_WAIT.getCode());
        ms.setAuditStatus(MsEnum.AUDIT_STATUS_WAIT_SUBMIT.getCode());
        ms.setMinPrice(0);
        ms.setMaxPrice(0);
        ms.setTotalStock(0);
        ms.setSaleNum(0);
        msMapper.insert(ms);

        BigDecimal minPrice = new BigDecimal(0);
        BigDecimal maxPrice = new BigDecimal(0);
        Integer totalStock = 0;
        if (msCreateValidate.getMsSkuList().size() > 0) {
            for (MsCUSkuListedVo vo : msCreateValidate.getMsSkuList()) {
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
                totalStock = totalStock + vo.getTotalStock();
                MsGoods msGoods = new MsGoods();
                msGoods.setPrice(vo.getPrice());
                msGoods.setTotalStock(vo.getTotalStock());
                msGoods.setStock(vo.getTotalStock());
                msGoods.setSaleNum(0);
                msGoods.setCreateTime(System.currentTimeMillis() / 1000);
                msGoods.setUpdateTime(System.currentTimeMillis() / 1000);
                msGoods.setMsId(ms.getId());
                msGoods.setShopId(ms.getShopId());
                msGoods.setGoodsId(ms.getGoodsId());
                msGoods.setGoodsSkuId(vo.getGoodsSkuId());
                msGoods.setPriceInt((vo.getPrice().multiply(new BigDecimal(100))).intValue());
                msGoodsMapper.insert(msGoods);
            }

            ms.setTotalStock(totalStock);
            ms.setStock(totalStock);
            ms.setMinPrice((minPrice.multiply(new BigDecimal(100))).intValue());
            ms.setMaxPrice((maxPrice.multiply(new BigDecimal(100))).intValue());
            msMapper.updateById(ms);
        } else {
            throw new OperateException("请提交sku");
        }
    }

    @Override
    @Transactional
    public void edit(MsUpdateValidate msUpdateValidate) {
        Ms ms = this.getById(msUpdateValidate.getId());
        Assert.notNull(ms, "找不到该秒杀信息");
        if (ms.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能编辑其他店铺秒杀信息");
        }
        BigDecimal minPrice = new BigDecimal(0);
        BigDecimal maxPrice = new BigDecimal(0);
        Integer totalStock = 0;
        if (msUpdateValidate.getMsSkuList().size() > 0) {
            iMsGoodsService.destroyItemByMsId(ms.getId());
            for (MsCUSkuListedVo vo : msUpdateValidate.getMsSkuList()) {
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
                totalStock = totalStock + vo.getTotalStock();
                MsGoods msGoods = new MsGoods();
                msGoods.setPrice(vo.getPrice());
                msGoods.setTotalStock(vo.getTotalStock());
                msGoods.setStock(vo.getTotalStock());
                msGoods.setSaleNum(0);
                msGoods.setCreateTime(System.currentTimeMillis() / 1000);
                msGoods.setUpdateTime(System.currentTimeMillis() / 1000);
                msGoods.setMsId(ms.getId());
                msGoods.setShopId(ms.getShopId());
                msGoods.setGoodsId(ms.getGoodsId());
                msGoods.setGoodsSkuId(vo.getGoodsSkuId());
                msGoods.setPriceInt((vo.getPrice().multiply(new BigDecimal(100))).intValue());
                msGoodsMapper.insert(msGoods);
            }

            ms.setGoodsId(msUpdateValidate.getGoodsId());
            ms.setStartTime(TimeUtils.dateToTimestamp(msUpdateValidate.getStartTime()));
            ms.setEndTime(TimeUtils.dateToTimestamp(msUpdateValidate.getEndTime()));
            ms.setUpdateTime(System.currentTimeMillis() / 1000);
            ms.setOrderLimit(msUpdateValidate.getOrderLimit());
            ms.setStatus(MsEnum.STATUS_WAIT.getCode());
            ms.setAuditStatus(MsEnum.AUDIT_STATUS_WAIT_SUBMIT.getCode());
            ms.setSaleNum(0);
            ms.setTotalStock(totalStock);
            ms.setStock(totalStock);
            ms.setMinPrice((minPrice.multiply(new BigDecimal(100))).intValue());
            ms.setMaxPrice((maxPrice.multiply(new BigDecimal(100))).intValue());
            msMapper.updateById(ms);
        } else {
            throw new OperateException("请提交sku");
        }
    }

    @Override
    public void open(MsOpenValidate validate) {
        Ms ms = this.getById(validate.getId());
        if (this.getCanOpenAttr(ms).equals(YesNoEnum.NO.getCode())) {
            throw new OperateException("开启活动失败");
        }
        if (ms.getShopId().equals(validate.getShopId()) == false) {
            throw new OperateException("不能编辑其他人的秒杀活动");
        }
        ms.setStatus(MsEnum.STATUS_START.getCode());
        ms.setUpdateTime(System.currentTimeMillis() / 1000);
        msMapper.updateById(ms);
    }

    /**
     * 判断sku是否有效
     * @param vo
     */
    private void checkSkuList(MsCUSkuListedVo vo) {
        MallGoodsSku goodsSku = iMallGoodsSkuService.getById(vo.getGoodsSkuId());
        if (StringUtils.isNull(goodsSku)) {
            throw new OperateException("商品规格信息错误");
        }
        if (goodsSku.getPrice().compareTo(vo.getPrice()) < 0) {
            throw new OperateException("规格秒杀价格必须低于销售价格");
        }
        if (goodsSku.getStock() < vo.getTotalStock()) {
            throw new OperateException("规格秒杀库存必须低于原库存");
        }

        MPJQueryWrapper queryWrapper = new MPJQueryWrapper<MsGoods>()
                .selectAll(MsGoods.class)
                .leftJoin("?_ms ms ON ms.id=t.ms_id".replace("?_", GlobalConfig.tablePrefix))
                .in("ms.status", Arrays.asList(MsEnum.STATUS_WAIT.getCode(), MsEnum.STATUS_START.getCode()))
                .gt("end_time", System.currentTimeMillis() / 1000)
                .orderByDesc("ms.id")
                .eq("t.goods_sku_id", vo.getGoodsSkuId())
                .eq("ms.delete_time", 0)
                .eq("t.delete_time", 0)
                .last("limit 1");
        MsGoods msGoods = msGoodsMapper.selectOne(queryWrapper);
        if (StringUtils.isNotNull(msGoods)) {
            throw new OperateException("商品已申请过秒杀");
        }
    }

    private Map<String, Object> __statistics(MPJQueryWrapper<Ms> mpjQueryWrapper) {

        Map<String, Object> statistics = new LinkedHashMap<>();
        mpjQueryWrapper.select("*");
        // 全部
        MPJQueryWrapper<Ms> allQuery = mpjQueryWrapper.clone();
        statistics.put("all", msMapper.selectCount(allQuery));

        // 待审核
        MPJQueryWrapper<Ms> waitQuery = mpjQueryWrapper.clone();
        statistics.put("wait", msMapper.selectCount(waitQuery.eq("ms.audit_status", MsEnum.AUDIT_STATUS_WAIT.getCode())));

        // 待提交
        MPJQueryWrapper<Ms> waitSubmitQuery = mpjQueryWrapper.clone();
        statistics.put("wait_submit", msMapper.selectCount(waitSubmitQuery.eq("ms.audit_status", MsEnum.AUDIT_STATUS_WAIT_SUBMIT.getCode())));

        // 审核成功
        MPJQueryWrapper<Ms> successQuery = mpjQueryWrapper.clone();
        statistics.put("success", msMapper.selectCount(successQuery.eq("ms.audit_status", MsEnum.AUDIT_STATUS_SUCCESS.getCode())));

        // 审核失败
        MPJQueryWrapper<Ms> failedQuery = mpjQueryWrapper.clone();
        statistics.put("failed", msMapper.selectCount(failedQuery.eq("ms.audit_status", MsEnum.AUDIT_STATUS_FAILED.getCode())));

        return statistics;
    }

    /**
     * @notes price_text 价格文本
     * @return string
     * @author Amos
     */
    private String getPriceTextAttr(Ms record) {
        MsGoods msGoodsMin = iMsGoodsService.getMinPriceByMsId(record.getId());
        MsGoods msGoodsMax = iMsGoodsService.getMaxPriceByMsId(record.getId());
        if (msGoodsMin.getPrice().compareTo(msGoodsMax.getPrice()) == 0) {
            return String.valueOf(msGoodsMin.getPrice());
        } else {
            return msGoodsMin.getPrice() + " ~ " + msGoodsMax.getPrice();
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
        return MsEnum.getStatusTextArr(status);
    }

    /**
     * @notes can_submit 可提交
     * @param $filedValue
     * @param $data
     * @return int
     * @author Amos
     */
    private Integer getCanSubmitAttr(Ms record) {
        return record.getAuditStatus().equals(MsEnum.AUDIT_STATUS_WAIT_SUBMIT.getCode()) ? 1 : 0;
    }

    /**
     * @notes can_open 可开启活动
     * @param $filedValue
     * @param $data
     * @return int
     * @author Amos
     */
    private Integer getCanOpenAttr(Ms record) {
        return record.getStatus().equals(MsEnum.STATUS_WAIT.getCode()) && record.getAuditStatus().equals(MsEnum.AUDIT_STATUS_SUCCESS.getCode()) ? 1 : 0;
    }

    /**
     * @notes can_edit 可编辑
     * @param $filedValue
     * @param $data
     * @return int
     * @author Amos
     */
    private Integer getCanEditAttr(Ms record) {
        return record.getStatus().equals(MsEnum.STATUS_WAIT.getCode()) && MsEnum.getAuditCanEditArr().contains(record.getAuditStatus()) ? 1 : 0;
    }

    /**
     * @notes can_close 可关闭活动
     * @return int
     * @author Amos
     * @datetime 2023-04-03 17:01:44
     */
    private Integer getCanCloseAttr(Ms record) {
        //商家 进行中的才可以
        return record.getStatus().equals(MsEnum.STATUS_START.getCode()) ? 1 : 0;
    }

    /**
     * @notes audit_status_text 审核状态
     * @param $filedValue
     * @param $data
     * @return string
     * @author Amos
     */
    private String getAuditStatusTextAttr(Integer auditStatus) {
        return MsEnum.getAuditStatusTextArr(auditStatus);
    }
}
