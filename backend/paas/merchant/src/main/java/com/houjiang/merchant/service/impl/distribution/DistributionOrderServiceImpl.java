package com.houjiang.merchant.service.impl.distribution;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.distribution.DistributionOrder;
import com.houjiang.merchant.common.entity.mall.MallOrder;
import com.houjiang.merchant.common.entity.mall.MallOrderGoods;
import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.common.entity.user.User;
import com.houjiang.merchant.common.enums.DistributionEnum;
import com.houjiang.merchant.common.mapper.distribution.DistributionOrderMapper;
import com.houjiang.merchant.common.mapper.mall.MallOrderGoodsMapper;
import com.houjiang.merchant.common.mapper.mall.MallOrderMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopMapper;
import com.houjiang.merchant.common.mapper.user.UserMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import com.houjiang.merchant.service.distribution.IDistributionOrderService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.distribution.DistributionOrderSearchValidate;
import com.houjiang.merchant.vo.distribution.DistributionOrderListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * 分销订单服务实现类
 */
@Service
public class DistributionOrderServiceImpl implements IDistributionOrderService {
    @Resource
    DistributionOrderMapper distributionOrderMapper;
    @Resource
    MallShopMapper mallShopMapper;
    @Resource
    MallOrderMapper mallOrderMapper;
    @Resource
    MallOrderGoodsMapper mallOrderGoodsMapper;
    @Resource
    UserMapper userMapper;

    @Override
    public void distributionFailedByOrder(Integer orderId) {
        QueryWrapper queryWrapper = new QueryWrapper<DistributionOrder>();
        queryWrapper.eq("order_id", orderId);

        DistributionOrder order = new DistributionOrder();
        order.setStatus(DistributionEnum.ORDER_STATUS_FAILED.getCode());
        order.setUpdateTime(System.currentTimeMillis() / 1000);
        distributionOrderMapper.update(order, queryWrapper);
    }

    @Override
    public void distributionFailedByOrderGoods(Integer orderGoodsId) {
        QueryWrapper queryWrapper = new QueryWrapper<DistributionOrder>();
        queryWrapper.eq("order_goods_id", orderGoodsId);

        DistributionOrder order = new DistributionOrder();
        order.setStatus(DistributionEnum.ORDER_STATUS_FAILED.getCode());
        order.setUpdateTime(System.currentTimeMillis() / 1000);
        distributionOrderMapper.update(order, queryWrapper);
    }

    /**
     * 分销订单列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<DistributionOrderListedVo>
     */
    @Override
    public PageResult<DistributionOrderListedVo> list(PageValidate pageValidate, DistributionOrderSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        MPJQueryWrapper<DistributionOrder> queryWrapper = new MPJQueryWrapper<>();
        queryWrapper.setAlias("o");
        queryWrapper.orderByDesc("o.id");
        String orderSn = searchValidate.getOrder_sn();
        String userInfo = searchValidate.getUser();
        String userDistributionInfo  = searchValidate.getUser_distribution();
        Integer status = searchValidate.getStatus();
        String goodsName = searchValidate.getGoods_name();
        String startTime = searchValidate.getStart_time();
        String endTime = searchValidate.getEnd_time();

        if (StringUtils.isNotNull(searchValidate.getShopId())) {
            queryWrapper.eq("o.shop_id", searchValidate.getShopId());
        }

        if (StringUtils.isNotEmpty(goodsName)) {
            queryWrapper.like("og.goods_name", goodsName);
        }

        if (StringUtils.isNotEmpty(orderSn)) {
            queryWrapper.like("mo.sn",orderSn);
        }

        if (StringUtils.isNotEmpty(userInfo)) {
            queryWrapper.nested(q->q.like("u.nickname", userInfo).or().like("u.sn", userInfo));
        }

        if (StringUtils.isNotEmpty(userDistributionInfo)) {
            queryWrapper.nested(q->q.like("ud.nickname", userDistributionInfo).or().like("ud.sn", userDistributionInfo));
        }

        // 时间搜索
        if(StringUtils.isNotEmpty(startTime)){
            queryWrapper.ge("o.create_time", TimeUtils.dateToTimestamp(startTime));
        }

        if(StringUtils.isNotEmpty(endTime)){
            queryWrapper.le("o.create_time", TimeUtils.dateToTimestamp(endTime));
        }

        queryWrapper
                .leftJoin("?_mall_order_goods og ON o.order_goods_id=og.id".replace("?_", GlobalConfig.tablePrefix))
                .leftJoin("?_user u ON o.user_id=u.id".replace("?_", GlobalConfig.tablePrefix))
                .leftJoin("?_user ud ON o.user_distribution_id=ud.id".replace("?_", GlobalConfig.tablePrefix))
                .leftJoin("?_mall_shop s ON o.shop_id=s.id".replace("?_", GlobalConfig.tablePrefix))
                .leftJoin("?_mall_order mo ON o.order_id=mo.id".replace("?_", GlobalConfig.tablePrefix))
                .eq("o.delete_time", 0)
                .orderByDesc("o.id");

        if (StringUtils.isNotNull(status)) {
            queryWrapper.eq("o.status", status);
        }

        queryWrapper.selectAll(DistributionOrder.class);

        IPage<DistributionOrder> iPage = distributionOrderMapper.selectJoinPage(new Page<>(page, limit),DistributionOrder.class ,queryWrapper);

        List<DistributionOrderListedVo> list = new LinkedList<>();
        for(DistributionOrder item : iPage.getRecords()) {
            DistributionOrderListedVo vo = new DistributionOrderListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setRatio(item.getRatio() != null ? item.getRatio().toString() : "0");
            vo.setAloneMoney(item.getAloneMoney() != null ? item.getAloneMoney().toString() : "0");
            vo.setMoney(item.getMoney() != null ? item.getMoney().toString() : "0");
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));

            MallShop shop = mallShopMapper.selectOne(new QueryWrapper<MallShop>().eq("id", item.getShopId()).eq("delete_time", 0));
            if (shop != null) {
                shop.setLogo(UrlUtils.toAdminAbsoluteUrl(shop.getLogo()));
            }
            vo.setShop(shop);

            MallOrder mallOrder = mallOrderMapper.selectOne(new QueryWrapper<MallOrder>().eq("id", item.getOrderId()).eq("delete_time", 0));
            vo.setOrderInfo(mallOrder);

            MallOrderGoods mallOrderGoods = mallOrderGoodsMapper.selectOne(new QueryWrapper<MallOrderGoods>().eq("id", item.getOrderGoodsId()).eq("delete_time", 0));
            if (mallOrderGoods != null) {
                mallOrderGoods.setGoodsImage(UrlUtils.toShopAbsoluteUrl(mallOrderGoods.getGoodsImage()));
            }
            vo.setOrderGoods(mallOrderGoods);

            User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", item.getUserId()).isNull("delete_time"));
            if (user != null) {
                user.setAvatar(UrlUtils.toAdminAbsoluteUrl(user.getAvatar()));
            }
            vo.setUser(user);

            User userDistribution = userMapper.selectOne(new QueryWrapper<User>().eq("id", item.getUserDistributionId()).isNull("delete_time"));
            if (userDistribution != null) {
                userDistribution.setAvatar(UrlUtils.toShopAbsoluteUrl(userDistribution.getAvatar()));
            }

            vo.setUserDistribution(userDistribution);
            vo.setLevelText(DistributionEnum.getLevelByCode(item.getLevel()));
            vo.setStatusText(DistributionEnum.getMsgByCode(item.getStatus()));
            vo.setSettlementTime(item.getSettlementTime().equals(0L) ? "-" : TimeUtils.timestampToDate(item.getSettlementTime()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

}
