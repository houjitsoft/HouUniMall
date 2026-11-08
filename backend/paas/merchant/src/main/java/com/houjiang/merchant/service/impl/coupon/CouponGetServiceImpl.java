package com.houjiang.merchant.service.impl.coupon;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.coupon.CouponGet;
import com.houjiang.merchant.common.enums.CouponGetEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.coupon.CouponGetMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.user.IUserService;
import com.houjiang.merchant.service.coupon.ICouponGetService;
import com.houjiang.merchant.service.coupon.ICouponService;
import com.houjiang.merchant.validate.commons.IdsValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.coupon.CouponGetSearchValidate;
import com.houjiang.merchant.vo.coupon.CouponGetListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 优惠券领取实现类
 */
@Service
public class CouponGetServiceImpl implements ICouponGetService {

    @Resource
    IUserService iUserService;
    @Resource
    ICouponService iCouponService;
    @Resource
    CouponGetMapper couponGetMapper;

    @Override
    public Integer getCountByCouponId(Integer couponId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("delete_time", 0);
        queryWrapper.eq("coupon_id", couponId);
        Long count = couponGetMapper.selectCount(queryWrapper);
        return count.intValue();
    }

    @Override
    public Integer getTodayCountByUid(Integer userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("delete_time", 0);
        queryWrapper.eq("user_id", userId);
        List<Long> timeRange = TimeUtils.today();
        queryWrapper.between("create_time", timeRange.get(0), timeRange.get(1));
        Long count = couponGetMapper.selectCount(queryWrapper);
        return count.intValue();
    }

    @Override
    public PageResult<CouponGetListedVo> list(PageValidate pageValidate, CouponGetSearchValidate searchValidate) {
        Integer pageNo = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        MPJQueryWrapper<CouponGet> wrapper = new MPJQueryWrapper<>();
        wrapper.setAlias("cg");
        wrapper.eq("cg.shop_id", searchValidate.getShopId());
        wrapper.eq("cg.delete_time", 0);

        wrapper.leftJoin("?_coupon c ON c.id=cg.coupon_id".replace("?_", GlobalConfig.tablePrefix))
                .leftJoin("?_user u ON cg.user_id=u.id".replace("?_", GlobalConfig.tablePrefix))
                .eq("cg.delete_time", 0)
                .orderByDesc("cg.id");

        // 买家信息
        if(StringUtils.isNotEmpty(searchValidate.getUser())){
            String keyword = searchValidate.getUser();
            wrapper.nested(wq->wq
                    .like("u.nickname", keyword).or()
                    .like("u.account", keyword).or()
                    .like("u.sn", keyword).or()
                    .like("u.mobile", keyword));
        }
        if (StringUtils.isNotEmpty(searchValidate.getCoupon_mame())) {
            wrapper.like("c.name", searchValidate.getCoupon_mame());
        }

        if (StringUtils.isNotNull(searchValidate.getStatus())) {
            wrapper.eq("cg.status", searchValidate.getStatus());
        }

        if (StringUtils.isNotNull(searchValidate.getGet_time_start())) {
            if (StringUtils.isNotEmpty(searchValidate.getGet_time_start())) {
                wrapper.ge("cg.create_time", TimeUtils.dateToTimestamp(searchValidate.getGet_time_start()));
            }
        }
        if (StringUtils.isNotNull(searchValidate.getGet_time_end())) {
            if (StringUtils.isNotEmpty(searchValidate.getGet_time_end())) {
                wrapper.le("cg.create_time", TimeUtils.dateToTimestamp(searchValidate.getGet_time_end()));
            }
        }



        wrapper.select("cg.*");
        IPage<CouponGet> iPage = couponGetMapper.selectJoinPage(
                new Page<>(pageNo, pageSize),
                CouponGet.class,
                wrapper);

        List<CouponGetListedVo> list = new ArrayList<>();
        for (CouponGet record : iPage.getRecords()) {
            CouponGetListedVo vo = new CouponGetListedVo();
            BeanUtils.copyProperties(record, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(record.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(record.getUpdateTime()));
            vo.setStartTime(record.getStartTime().equals(0L) ? "-" : TimeUtils.timestampToDate(record.getStartTime()));
            vo.setEndTime(record.getEndTime().equals(0L) ? "-" : TimeUtils.timestampToDate(record.getEndTime()));
            vo.setUseTime(record.getUseTime().equals(0L) ? "-" : TimeUtils.timestampToDate(record.getUseTime()));
            vo.setCoupon(iCouponService.getShortById(record.getCouponId()));
            vo.setCouponName(vo.getCoupon().getName());
            vo.setUser(iUserService.detail(record.getUserId()));
            vo.setCanInvalid(this.getCanInvalidAttr(record.getStatus()));
            vo.setStatusText(CouponGetEnum.getRecordDesc(record.getStatus()));
            list.add(vo);
        }
        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list, new HashMap<>());
    }

    @Override
    @Transactional
    public void invalid(IdsValidate idsValidate) {
        if (idsValidate.getIds().size() <= 0) {
            throw new OperateException("请选择至少一个");
        }
        QueryWrapper queryWrapper = new QueryWrapper<CouponGet>();
        queryWrapper.in("id", idsValidate.getIds());
        queryWrapper.eq("shop_id", MerchantThreadLocal.getShopId());
        queryWrapper.eq("status", CouponGetEnum.STATUS_WAIT.getCode());

        CouponGet couponGet = new CouponGet();
        couponGet.setStatus(CouponGetEnum.STATUS_INVALID.getCode());
        couponGet.setUpdateTime(System.currentTimeMillis() / 1000);
        couponGetMapper.update(couponGet, queryWrapper);
    }

    /**
     * @notes can_invalid 可作废
     * @return int
     * @author Amos
     */
    private Integer getCanInvalidAttr(Integer status) {
        return status.equals(CouponGetEnum.STATUS_WAIT.getCode()) ? 1 : 0;
    }
}
