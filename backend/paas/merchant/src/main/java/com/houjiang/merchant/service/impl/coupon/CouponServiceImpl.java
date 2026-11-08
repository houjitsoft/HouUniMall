package com.houjiang.merchant.service.impl.coupon;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.merchant.common.validation.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.coupon.Coupon;
import com.houjiang.merchant.common.entity.coupon.CouponGet;
import com.houjiang.merchant.common.entity.coupon.CouponGoods;
import com.houjiang.merchant.common.entity.user.User;
import com.houjiang.merchant.common.enums.CouponEnum;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.coupon.CouponGetMapper;
import com.houjiang.merchant.common.mapper.coupon.CouponGoodsMapper;
import com.houjiang.merchant.common.mapper.coupon.CouponMapper;
import com.houjiang.merchant.common.util.*;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.user.IUserService;
import com.houjiang.merchant.service.coupon.ICouponGetService;
import com.houjiang.merchant.service.coupon.ICouponGoodsService;
import com.houjiang.merchant.service.coupon.ICouponService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.coupon.CouponCreateValidate;
import com.houjiang.merchant.validate.coupon.CouponRenameValidate;
import com.houjiang.merchant.validate.coupon.CouponSearchValidate;
import com.houjiang.merchant.validate.coupon.CouponSysSendValidate;
import com.houjiang.merchant.validate.coupon.CouponUpdateValidate;
import com.houjiang.merchant.vo.coupon.CouponDetailVo;
import com.houjiang.merchant.vo.coupon.CouponListedVo;
import com.houjiang.merchant.vo.coupon.CouponShortDetailVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * 营销优惠券服务实现类
 */
@Service
public class CouponServiceImpl implements ICouponService {

    @Resource
    ICouponGoodsService iCouponGoodsService;
    @Resource
    IUserService iUserService;
    @Resource
    ICouponGetService iCouponGetService;
    @Resource
    CouponMapper couponMapper;
    @Resource
    CouponGoodsMapper couponGoodsMapper;
    @Resource
    CouponGetMapper couponGetMapper;

    /**
     * 优惠券列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MarketingCouponListedVo>
     */
    @Override
    public PageResult<CouponListedVo> list(PageValidate pageValidate, CouponSearchValidate searchValidate) {
        Integer pageNo   = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        // 条件构造
        QueryWrapper<Coupon> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("delete_time", 0).eq("shop_id", MerchantThreadLocal.getShopId())
                .orderByDesc("id");

        // 关键词搜
        if (StringUtils.isNotEmpty(searchValidate.getName())) {
            queryWrapper.nested(wq->wq
                    .like("name", searchValidate.getName())
            );
        }

        if (StringUtils.isNotNull(searchValidate.getGet_type())) {
            queryWrapper.eq("get_type", searchValidate.getGet_type());
        }

        if (StringUtils.isNotNull(searchValidate.getStart_time())) {
            if (StringUtils.isNotEmpty(searchValidate.getStart_time())) {
                queryWrapper.ge("use_start_time", TimeUtils.dateToTimestamp(searchValidate.getStart_time()));
            }
        }
        if (StringUtils.isNotNull(searchValidate.getEnd_time())) {
            if (StringUtils.isNotEmpty(searchValidate.getEnd_time())) {
                queryWrapper.le("use_end_time", TimeUtils.dateToTimestamp(searchValidate.getEnd_time()));
            }
        }

        // 数据统计
        QueryWrapper<Coupon> allQuery = queryWrapper.clone();
        QueryWrapper<Coupon> notQuery = queryWrapper.clone();
        QueryWrapper<Coupon> endQuery = queryWrapper.clone();
        QueryWrapper<Coupon> conductQuery = queryWrapper.clone();
        Map<String, Object> extend = new LinkedHashMap<>();
        extend.put("all", couponMapper.selectCount(allQuery));
        extend.put("start", couponMapper.selectCount(notQuery.eq("status", CouponEnum.STATUS_START.getCode())));
        extend.put("end", couponMapper.selectCount(endQuery.eq("status", CouponEnum.STATUS_END.getCode())));
        extend.put("wait", couponMapper.selectCount(conductQuery.eq("status", CouponEnum.STATUS_WAIT.getCode())));

        if (StringUtils.isNotNull(searchValidate.getStatus())) {
            queryWrapper.eq("status", searchValidate.getStatus() - 1);
        }

        // 发起查询
        IPage<Coupon> iPage = couponMapper.selectPage(new Page<>(pageNo, pageSize), queryWrapper);

        // 处理数据
        List<CouponListedVo> list = new LinkedList<>();
        for (Coupon coupon: iPage.getRecords()) {
            CouponListedVo vo = new CouponListedVo();
            BeanUtils.copyProperties(coupon, vo);
            vo.setCanDelete(this.getCanDeleteAttr(coupon.getStatus()));
            vo.setCanEdit(this.getCanEditAttr(coupon.getStatus()));
            vo.setCanEnd(this.getCanEndAttr(coupon.getStatus()));
            vo.setCanStart(this.getCanStartAttr(coupon.getStatus()));
            vo.setCanSysSend(this.getCanSysSendAttr(coupon));
            vo.setGetLimitText(this.getGetLimitTextAttr(coupon.getGetLimit()));
            vo.setGetTypeText(this.getGetTypeTextAttr(coupon.getGetType()));
            vo.setStatusText(this.getStatusTextAttr(coupon.getStatus()));
            vo.setStockTypeText(this.getStockTypeTextAttr(coupon));
            vo.setUseEndTime(TimeUtils.timestampToDate(coupon.getUseEndTime()));
            vo.setUseGoodsTypeText(this.getUseGoodsTypeTextAttr(coupon.getUseGoodsType()));
            vo.setUseStartTime(TimeUtils.timestampToDate(coupon.getUseStartTime()));
            vo.setCreateTime(TimeUtils.timestampToDate(coupon.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(coupon.getUpdateTime()));
            vo.setUseTimeTypeText(this.getUseTimeTypeTextAttr(coupon.getUseTimeType()));
            vo.setUseTimeText(this.getUseTimeTextAttr(coupon));
            vo.setCouponContent(this.getCouponContentAttr(coupon));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list, extend);
    }

    @Override
    public CouponDetailVo detail(Integer id) {
        Coupon coupon = this.getById(id);
        if (StringUtils.isNull(coupon)) {
            return null;
        }
        CouponDetailVo vo = new CouponDetailVo();
        BeanUtils.copyProperties(coupon, vo);
        vo.setCanDelete(this.getCanDeleteAttr(coupon.getStatus()));
        vo.setCanEdit(this.getCanEditAttr(coupon.getStatus()));
        vo.setCanEnd(this.getCanEndAttr(coupon.getStatus()));
        vo.setCanStart(this.getCanStartAttr(coupon.getStatus()));
        vo.setCanSysSend(this.getCanSysSendAttr(coupon));
        vo.setGetLimitText(this.getGetLimitTextAttr(coupon.getGetLimit()));
        vo.setGetTypeText(this.getGetTypeTextAttr(coupon.getGetType()));
        vo.setStatusText(this.getStatusTextAttr(coupon.getStatus()));
        vo.setStockTypeText(this.getStockTypeTextAttr(coupon));
        vo.setUseEndTime(TimeUtils.timestampToDate(coupon.getUseEndTime()));
        vo.setUseGoodsTypeText(this.getUseGoodsTypeTextAttr(coupon.getUseGoodsType()));
        vo.setUseStartTime(TimeUtils.timestampToDate(coupon.getUseStartTime()));
        vo.setCreateTime(TimeUtils.timestampToDate(coupon.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(coupon.getUpdateTime()));
        vo.setUseTimeTypeText(this.getUseTimeTypeTextAttr(coupon.getUseTimeType()));
        vo.setUseTimeText(this.getUseTimeTextAttr(coupon));
        vo.setCouponContent(this.getCouponContentAttr(coupon));
        vo.setConditionTypeText(this.getConditionTypeTextAttr(coupon.getConditionType()));
        vo.setCanUserGet(this.getCanUserGetAttr(coupon));
        vo.setGoods(iCouponGoodsService.getListByCid(coupon.getId()));
        return vo;
    }

    /**
     * @notes can_user_get 可领取
     * @return int
     * @author Amos
     */
    private Integer getCanUserGetAttr(Coupon coupon) {
        return coupon.getStatus().equals(CouponEnum.STATUS_START.getCode()) || coupon.getGetType().equals(CouponEnum.GET_TYPE_USER.getCode()) ? 1 : 0;
    }

    /**
     * @notes condition_type_text 使用方式
     * @param $fieldValue
     * @param $data
     * @return string
     * @author Amos
     */
    private String getConditionTypeTextAttr(Integer conditionType) {
        return CouponEnum.getConditionTypeTextArr(conditionType);
    }

    @Override
    public Coupon getById(Integer id) {
        return couponMapper.selectOne(new QueryWrapper<Coupon>().eq("id", id).eq("delete_time", 0).eq("shop_id", MerchantThreadLocal.getShopId()));
    }

    @Override
    @Transactional
    public void edit(CouponUpdateValidate updateValidate) {
        Coupon coupon = this.getById(updateValidate.getId());
        Assert.notNull(coupon, "找不到该优惠券的信息");
        if (coupon.getStatus().equals(CouponEnum.STATUS_WAIT.getCode())) {
            coupon.setName(updateValidate.getName());
            coupon.setStockType(updateValidate.getStock_type());
            coupon.setStock(updateValidate.getStock());
            coupon.setUseTimeType(updateValidate.getUse_time_type());
            coupon.setUseStartTime(TimeUtils.dateToTimestamp(updateValidate.getUse_start_time()));
            coupon.setUseEndTime(TimeUtils.dateToTimestamp(updateValidate.getUse_end_time()));
            coupon.setUseCurrentDay(updateValidate.getUse_current_day());
            coupon.setUseNextDay(updateValidate.getUse_next_day());
            coupon.setMoney(updateValidate.getMoney());
            coupon.setConditionType(updateValidate.getCondition_type());
            coupon.setConditionOrderMoney(updateValidate.getCondition_order_money());
            coupon.setGetType(updateValidate.getGet_type());
            coupon.setGetLimit(updateValidate.getGet_limit());
            coupon.setGetLimitNum(updateValidate.getGet_limit_num());
            coupon.setGetLimitDayNum(updateValidate.getGet_limit_day_num());
            coupon.setUseGoodsType(updateValidate.getUse_goods_type());
            couponMapper.updateById(coupon);

            // 商品记录
            if (!updateValidate.getUse_goods_type().equals(CouponEnum.USE_GOODS_TYPE_ALL.getCode())) {
                couponGoodsMapper.delete(new QueryWrapper<CouponGoods>().eq("coupon_id", updateValidate.getId()));

                if (updateValidate.getGoods().size() > 0) {
                    for (Object item : updateValidate.getGoods()) {
                        JSONObject goods = JSONObject.parseObject(JSONObject.toJSONString(item));
                        CouponGoods couponGoods = new CouponGoods();
                        couponGoods.setCouponId(updateValidate.getId());
                        couponGoods.setGoodsId(goods.getInteger("goods_id"));
                        couponGoods.setCreateTime(System.currentTimeMillis() / 1000);
                        couponGoods.setUpdateTime(System.currentTimeMillis() / 1000);
                        couponGoodsMapper.insert(couponGoods);
                    }
                }
            }

        } else if (coupon.getStatus().equals(CouponEnum.STATUS_START.getCode())) {
            coupon.setName(updateValidate.getName());
            if (coupon.getStockType().equals(CouponEnum.STOCK_TYPE_LIMIT.getCode())) {
                coupon.setStock(updateValidate.getStock());
            }
            couponMapper.updateById(coupon);
        } else {
            throw new OperateException("优惠券不能编辑");
        }
    }

    @Override
    @Transactional
    public void rename(CouponRenameValidate renameValidate) {
        Coupon coupon = this.getById(renameValidate.getId());
        Assert.notNull(coupon, "找不到该优惠券的信息");
        if (!coupon.getShopId().equals(MerchantThreadLocal.getShopId())) {
            throw new OperateException("不能修改其他店铺的优惠券");
        }
        if (coupon.getStatus().equals(CouponEnum.STATUS_END.getCode())) {
            throw new OperateException("已结束的优惠券不能重命名");
        }
        coupon.setName(renameValidate.getName());
        if (renameValidate.getSendTotal() != null
                && coupon.getStockType().equals(CouponEnum.STOCK_TYPE_LIMIT.getCode())) {
            Integer sendTotal = renameValidate.getSendTotal();
            if (sendTotal < coupon.getHasGetNum()) {
                throw new OperateException("发放数量不能小于已领取数量");
            }
            coupon.setStock(sendTotal);
        }
        coupon.setUpdateTime(System.currentTimeMillis() / 1000);
        couponMapper.updateById(coupon);
    }

    @Override
    @Transactional
    public void end(Integer id) {
        Coupon coupon = this.getById(id);
        Assert.notNull(coupon, "找不到该优惠券信息");

        Coupon couponUp = new Coupon();

        couponUp.setStatus(CouponEnum.STATUS_END.getCode());
        Integer ret = couponMapper.update(couponUp, new QueryWrapper<Coupon>().eq("id", id).eq("shop_id", MerchantThreadLocal.getShopId()).eq("status", CouponEnum.STATUS_START.getCode()).eq("delete_time", 0));
        if (ret.equals(0)) {
            throw new OperateException("结束失败");
        }
    }

    @Override
    @Transactional
    public void start(Integer id) {
        Coupon coupon = this.getById(id);
        Assert.notNull(coupon, "找不到该优惠券信息");

        Coupon couponUp = new Coupon();

        couponUp.setStatus(CouponEnum.STATUS_START.getCode());
        Integer ret = couponMapper.update(couponUp, new QueryWrapper<Coupon>().eq("id", id).eq("shop_id", MerchantThreadLocal.getShopId()).eq("status", CouponEnum.STATUS_WAIT.getCode()).eq("delete_time", 0));
        if (ret.equals(0)) {
            throw new OperateException("结束失败");
        }
    }

    @Override
    @Transactional
    public void del(Integer id) {
        couponMapper.delete(new QueryWrapper<Coupon>().eq("id", id).eq("shop_id", MerchantThreadLocal.getShopId()));

        couponGetMapper.delete(new QueryWrapper<CouponGet>().eq("coupon_id", id).eq("shop_id", MerchantThreadLocal.getShopId()));
    }

    @Override
    @Transactional
    public void add(CouponCreateValidate createValidate) {
        Coupon coupon = new Coupon();
        coupon.setName(createValidate.getName());
        coupon.setStockType(createValidate.getStock_type());
        coupon.setStock(createValidate.getStock());
        coupon.setUseTimeType(createValidate.getUse_time_type());
        coupon.setUseStartTime(StringUtils.isNotEmpty(createValidate.getUse_start_time()) ? TimeUtils.dateToTimestamp(createValidate.getUse_start_time()) : null);
        coupon.setUseEndTime(StringUtils.isNotEmpty(createValidate.getUse_end_time()) ?  TimeUtils.dateToTimestamp(createValidate.getUse_end_time()) : null);
        coupon.setUseCurrentDay(createValidate.getUse_current_day());
        coupon.setUseNextDay(createValidate.getUse_next_day());
        coupon.setMoney(Integer.parseInt(createValidate.getMoney()));
        coupon.setConditionType(createValidate.getCondition_type());
        coupon.setConditionOrderMoney(createValidate.getCondition_order_money());
        coupon.setGetType(createValidate.getGet_type());
        coupon.setGetLimit(createValidate.getGet_limit());
        coupon.setGetLimitNum(createValidate.getGet_limit_num());
        coupon.setGetLimitDayNum(createValidate.getGet_limit_day_num());
        coupon.setUseGoodsType(createValidate.getUse_goods_type());
        coupon.setShopId(MerchantThreadLocal.getShopId());
        coupon.setCreateTime(System.currentTimeMillis() / 1000);
        coupon.setUpdateTime(System.currentTimeMillis() / 1000);
        couponMapper.insert(coupon);

        // 商品记录
        if (!createValidate.getUse_goods_type().equals(CouponEnum.USE_GOODS_TYPE_ALL.getCode())) {

            if (createValidate.getGoods().size() > 0) {
                for (Object item : createValidate.getGoods()) {
                    JSONObject goods = JSONObject.parseObject(JSONObject.toJSONString(item));
                    CouponGoods couponGoods = new CouponGoods();
                    couponGoods.setCouponId(coupon.getId());
                    couponGoods.setGoodsId(goods.getInteger("goods_id"));
                    couponGoods.setCreateTime(System.currentTimeMillis() / 1000);
                    couponGoods.setUpdateTime(System.currentTimeMillis() / 1000);
                    couponGoodsMapper.insert(couponGoods);
                }
            }
        }
    }

    @Override
    @Transactional
    public void userGetCoupon(CouponSysSendValidate sysSendValidate) {
        CouponDetailVo coupon = this.detail(sysSendValidate.getId());
        Assert.notNull(coupon, "优惠券不存在");
        if (sysSendValidate.getUser_ids().size() > 0) {
            for (Integer userId : sysSendValidate.getUser_ids()) {
                User user = iUserService.getById(userId);
                if (StringUtils.isNull(user)) {
                    throw new OperateException("用户不存在");
                }

                if (sysSendValidate.getGetType().equals(CouponEnum.GET_TYPE_USER.getCode())) {
                    if (coupon.getCanUserGet().equals(YesNoEnum.NO.getCode())) {
                        throw new OperateException("不支持领取优惠券");
                    }
                }
                if (sysSendValidate.getGetType().equals(CouponEnum.GET_TYPE_SYS.getCode())) {
                    if (coupon.getCanSysSend().equals(YesNoEnum.NO.getCode())) {
                        throw new OperateException("不支持发放优惠券");
                    }
                }

                // 检测用户领取数量
                if (coupon.getGetLimit().equals(CouponEnum.GET_LIMIT_NUM.getCode())) {
                    Integer userHasGetNum = iCouponGetService.getCountByCouponId(coupon.getId());
                    if (userHasGetNum + sysSendValidate.getNums() > coupon.getGetLimitNum()) {
                        throw new OperateException("领取数量已到上限");
                    }
                }

                if (coupon.getGetLimit().equals(CouponEnum.GET_LIMIT_DAY_NUM.getCode())) {
                    Integer userHasGetNum = iCouponGetService.getTodayCountByUid(userId);
                    if (userHasGetNum + sysSendValidate.getNums() > coupon.getGetLimitDayNum()) {
                        throw new OperateException("今日领取数量已到上限");
                    }
                }

                // 检测库存
                if (coupon.getStockType().equals(CouponEnum.STOCK_TYPE_LIMIT.getCode()) && coupon.getStock() <= coupon.getHasGetNum()) {
                    throw new OperateException("优惠券已被领取完");
                }

                // 优惠券获取数量增加
                Coupon couponUpdate = this.getById(coupon.getId());
                QueryWrapper couponUpdateQueryWrapper = new QueryWrapper<Coupon>();
                couponUpdateQueryWrapper.eq("status", coupon.getStatus());
                couponUpdateQueryWrapper.eq("id", coupon.getId());
                if (coupon.getStockType().equals(CouponEnum.STOCK_TYPE_LIMIT.getCode())) {
                    couponUpdateQueryWrapper.lt("stock", couponUpdate.getHasGetNum() + sysSendValidate.getNums());
                }
                couponUpdate.setHasGetNum(couponUpdate.getHasGetNum() + sysSendValidate.getNums());
                couponMapper.update(couponUpdate, couponUpdateQueryWrapper);

                // 保存领取记录
                for (Integer i = 0; i < sysSendValidate.getNums(); i++) {
                    CouponGet couponGet = new CouponGet();
                    couponGet.setCreateTime(System.currentTimeMillis() / 1000);
                    couponGet.setUpdateTime(System.currentTimeMillis() / 1000);
                    couponGet.setUserId(userId);
                    couponGet.setShopId(coupon.getShopId());
                    couponGet.setCouponId(coupon.getId());
                    List<Long> startEnd = this.getStartEnd(coupon);
                    couponGet.setStartTime(startEnd.get(0));
                    couponGet.setEndTime(startEnd.get(1));
                    couponGetMapper.insert(couponGet);
                }
            }
        } else {
            throw new OperateException("请提交发放用户ID");
        }
    }

    @Override
    public CouponShortDetailVo getShortById(Integer couponId) {
        Coupon coupon = this.getById(couponId);
        if (StringUtils.isNull(coupon)) {
            return null;
        }
        CouponShortDetailVo vo = new CouponShortDetailVo();
        BeanUtils.copyProperties(coupon, vo);
        return vo;
    }

    /**
     * @notes 根据领取时间 获取使用时间
     * @param $time
     * @param object $coupon
     * @return array
     * @author Amos
     */
    private List<Long> getStartEnd(CouponDetailVo coupon) {
        Long time = System.currentTimeMillis() / 1000;
        List<Long> ret = new ArrayList<>();

        // 默认固定日期
        if (coupon.getUseTimeType().equals(CouponEnum.USE_TIME_TYPE_DEFAULT.getCode())) {
            ret.add(TimeUtils.dateToTimestamp(coupon.getUseStartTime()));
            ret.add(TimeUtils.dateToTimestamp(coupon.getUseEndTime()));
            return ret;
        }

        // 领取当日起多少天
        if (coupon.getUseTimeType().equals(CouponEnum.USE_TIME_TYPE_CURRENT.getCode())) {
            ret.add(time);
            ret.add(time + 3600 * 24 * coupon.getUseCurrentDay());
            return ret;
        }
        // 领取次日起多少天
        if (coupon.getUseTimeType().equals(CouponEnum.USE_TIME_TYPE_NEXT.getCode())) {
            List<Long> today = TimeUtils.today();
            ret.add(today.get(0) + 3600 * 24);
            ret.add(TimeUtils.endOfDayTimestamp(time + 3600 * 24 * (coupon.getUseNextDay() + 1)));
            return ret;
        }
        return ret;
    }

    /**
     * @notes coupon_content 优惠内容
     * @param $fieldValue
     * @param $data
     * @return string
     * @author Amos
     */
    private String getCouponContentAttr(Coupon coupon) {
        if (coupon.getConditionType().equals(CouponEnum.CONDITION_TYPE_NONE.getCode())) {
            return "无门槛，减" + coupon.getMoney() + "元";
        }
        if (coupon.getConditionType().equals(CouponEnum.CONDITION_TYPE_ORDER_MONEY
                .getCode())) {
            return "订单满" + coupon.getConditionOrderMoney() + "元，减" + coupon.getMoney() + "元";
        }
        return "";
    }

    /**
     * @notes use_time_text 使用时间
     * @param $fieldValue
     * @param $data
     * @return string
     * @author Amos
     */
    private String getUseTimeTextAttr(Coupon coupon) {
        if (coupon.getUseTimeType().equals(CouponEnum.USE_TIME_TYPE_DEFAULT.getCode())) {
            return "有效期至" + TimeUtils.timestampToDate(coupon.getUseEndTime(), "yyyy-MM-dd");
        }
        if (coupon.getUseTimeType().equals(CouponEnum.USE_TIME_TYPE_CURRENT.getCode())) {
            return "领取当日起" + coupon.getUseCurrentDay() + "日内可用";
        }
        if (coupon.getUseTimeType().equals(CouponEnum.USE_TIME_TYPE_NEXT.getCode())) {
            return "领取次日起" + coupon.getUseNextDay() + "日内可用";
        }
        return "";
    }

    /**
     * @notes use_time_type_text 使用时间
     * @param $fieldValue
     * @param $data
     * @return string
     * @author Amos
     */
    private String getUseTimeTypeTextAttr(Integer useTimeType) {
        return CouponEnum.getUseTimeTypeText(useTimeType);
    }

    /**
     * @notes use_goods_type_text 适用商品
     * @param $fieldValue
     * @param $data
     * @return string
     * @author Amos
     */
    private String getUseGoodsTypeTextAttr(Integer useGoodsType) {
        return CouponEnum.getUseGoodsTypeMsg(useGoodsType);
    }

    /**
     * @notes stock_type_text 发放限制
     * @param $fieldValue
     * @param $data
     * @return string
     * @author Amos
     */
    private Object getStockTypeTextAttr(Coupon coupon) {
        if (coupon.getStockType().equals(CouponEnum.STOCK_TYPE_LIMIT.getCode())) {
            return coupon.getStock();
        } else {
            return "无限制";
        }
    }

    /**
     * @notes status_text 状态
     * @param $fieldValue
     * @param $data
     * @return string
     * @author Amos
     */
    private String getStatusTextAttr(Integer status) {
        return CouponEnum.getCouponStatusMsg(status);
    }

    /**
     * @notes get_type_text 发放方式
     * @param $fieldValue
     * @param $data
     * @return string
     * @author Amos
     */
    private String getGetTypeTextAttr(Integer getType) {
        return CouponEnum.getGetTypeMsg(getType);
    }

    /**
     * @notes get_limit_text 领取限制
     * @param $fieldValue
     * @param $data
     * @return string
     * @author Amos
     */
    private String getGetLimitTextAttr(Integer getLimit) {
        return CouponEnum.getLimitTextArr(getLimit);
    }

    /**
     * @notes can_sys_send 可系统发放
     * @param $fieldValue
     * @param $data
     * @return int
     * @author Amos
     */
    private Integer getCanSysSendAttr(Coupon coupon) {
        return coupon.getStatus().equals(CouponEnum.STATUS_START.getCode()) || coupon.getGetType().equals(CouponEnum.GET_TYPE_SYS.getCode()) ? 1 : 0;
    }

    /**
     * @notes can_start 可开始活动
     * @return int
     * @author Amos
     */
    private Integer getCanStartAttr(Integer status) {
        return status.equals(CouponEnum.STATUS_WAIT.getCode()) ? 1 : 0;
    }

    /**
     * @notes can_end 可结束
     * @param $fieldValue
     * @param $data
     * @return int
     * @author Amos
     */
    private Integer getCanEndAttr(Integer status) {
        return status.equals(CouponEnum.STATUS_START.getCode()) ? 1 : 0;
    }

    /**
     * @notes can_edit 可编辑
     * @param $fieldValue
     * @param $data
     * @return int
     * @author Amos
     */
    private Integer getCanEditAttr(Integer status) {
        return status.equals(CouponEnum.STATUS_WAIT.getCode()) || status.equals(CouponEnum.STATUS_START.getCode()) ? 1 : 0;
    }

    /**
     * @notes can_delete 可删除
     * @param $fieldValue
     * @param $data
     * @return int
     * @author Amos
     */
    private Integer getCanDeleteAttr(Integer status) {
        return status.equals(CouponEnum.STATUS_WAIT.getCode()) || status.equals(CouponEnum.STATUS_END.getCode()) ? 1 : 0;
    }
}
