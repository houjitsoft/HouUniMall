package com.houjiang.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.activity.entity.ActivitySeckillBaseEntity;
import com.houjiang.activity.entity.ActivitySeckillGoodsEntity;
import com.houjiang.activity.entity.ActivitySeckillSubscribeEntity;
import com.houjiang.activity.feign.UserServiceClient;
import com.houjiang.activity.mapper.ActivitySeckillBaseMapper;
import com.houjiang.activity.mapper.ActivitySeckillGoodsMapper;
import com.houjiang.activity.mapper.ActivitySeckillSubscribeMapper;
import com.houjiang.activity.service.ActivitySeckillSubscribeService;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 限时秒杀开抢预约 Service 实现
 *
 * @author Amos
 */
@Service
public class ActivitySeckillSubscribeServiceImpl
        extends ServiceImpl<ActivitySeckillSubscribeMapper, ActivitySeckillSubscribeEntity>
        implements ActivitySeckillSubscribeService {

    private static final Logger log = LoggerFactory.getLogger(ActivitySeckillSubscribeServiceImpl.class);

    @Autowired
    private ActivitySeckillBaseMapper activitySeckillBaseMapper;

    @Autowired
    private ActivitySeckillGoodsMapper activitySeckillGoodsMapper;

    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    public ResponseResult subscribe(Map<String, Object> param) {
        String userId = resolveUserId(param);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.error("请先登录后再预约");
        }
        Integer seckillId = toInt(param.get("seckillId"));
        Integer goodsId = toInt(param.get("goodsId"));
        if (goodsId == null) {
            return ResponseResult.error("缺少商品参数");
        }

        Date now = new Date();
        ActivitySeckillGoodsEntity goods;
        if (seckillId != null) {
            LambdaQueryWrapper<ActivitySeckillGoodsEntity> goodsWrapper = new LambdaQueryWrapper<>();
            goodsWrapper.eq(ActivitySeckillGoodsEntity::getSeckillId, seckillId)
                    .eq(ActivitySeckillGoodsEntity::getGoodsId, goodsId)
                    .last("LIMIT 1");
            goods = activitySeckillGoodsMapper.selectOne(goodsWrapper);
        } else {
            // C 端只有 goodsId：取该商品最近一场尚未开抢的秒杀（goods_start_time >= now，最早的一条）
            LambdaQueryWrapper<ActivitySeckillGoodsEntity> goodsWrapper = new LambdaQueryWrapper<>();
            goodsWrapper.eq(ActivitySeckillGoodsEntity::getGoodsId, goodsId)
                    .ge(ActivitySeckillGoodsEntity::getGoodsStartTime, now)
                    .orderByAsc(ActivitySeckillGoodsEntity::getGoodsStartTime)
                    .last("LIMIT 1");
            goods = activitySeckillGoodsMapper.selectOne(goodsWrapper);
            if (goods != null) {
                seckillId = goods.getSeckillId();
            }
        }
        if (goods == null) {
            return ResponseResult.error("该商品没有可预约的秒杀场次");
        }

        ActivitySeckillBaseEntity base = activitySeckillBaseMapper.selectById(seckillId);
        if (base == null) {
            return ResponseResult.error("秒杀场次不存在");
        }
        if (base.getSeckillStartTime() != null && !now.before(base.getSeckillStartTime())) {
            return ResponseResult.error("该场次已开抢，无需预约");
        }

        // 幂等：已预约直接返回成功
        LambdaQueryWrapper<ActivitySeckillSubscribeEntity> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(ActivitySeckillSubscribeEntity::getSeckillId, seckillId)
                .eq(ActivitySeckillSubscribeEntity::getGoodsId, goodsId)
                .eq(ActivitySeckillSubscribeEntity::getUserId, userId)
                .last("LIMIT 1");
        if (this.count(existWrapper) > 0) {
            return ResponseResult.success("已预约");
        }

        ActivitySeckillSubscribeEntity entity = new ActivitySeckillSubscribeEntity();
        entity.setSeckillId(seckillId);
        entity.setGoodsId(goodsId);
        entity.setCommonId(goods.getCommonId());
        entity.setShopId(goods.getShopId());
        entity.setUserId(userId);
        entity.setGoodsName(goods.getGoodsName());
        entity.setSeckillStartTime(base.getSeckillStartTime());
        entity.setSubscribeTime(now);
        entity.setRemindStatus(0);
        entity.setUserMobile(fetchMobile(userId));
        this.save(entity);
        return ResponseResult.success("预约成功，开抢前将提醒你");
    }

    @Override
    public ResponseResult cancel(Map<String, Object> param) {
        String userId = resolveUserId(param);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.error("请先登录后再操作");
        }
        Integer seckillId = toInt(param.get("seckillId"));
        Integer goodsId = toInt(param.get("goodsId"));
        if (goodsId == null) {
            return ResponseResult.error("缺少商品参数");
        }
        LambdaQueryWrapper<ActivitySeckillSubscribeEntity> wrapper = new LambdaQueryWrapper<>();
        // C 端只有 goodsId 时按 goodsId 取消（同一商品通常只有一个待开抢场次）
        if (seckillId != null) {
            wrapper.eq(ActivitySeckillSubscribeEntity::getSeckillId, seckillId);
        }
        wrapper.eq(ActivitySeckillSubscribeEntity::getGoodsId, goodsId)
                .eq(ActivitySeckillSubscribeEntity::getUserId, userId);
        this.remove(wrapper);
        return ResponseResult.success("已取消预约");
    }

    @Override
    public ResponseResult mySubscribes(Map<String, Object> param) {
        String userId = resolveUserId(param);
        Integer seckillId = toInt(param.get("seckillId"));
        List<Integer> goodsIds = new ArrayList<>();
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.success(goodsIds);
        }
        // seckillId 缺省时返回该用户全部已预约的 goodsId（C 端秒杀页只有 goodsId，不带场次）
        LambdaQueryWrapper<ActivitySeckillSubscribeEntity> wrapper = new LambdaQueryWrapper<>();
        if (seckillId != null) {
            wrapper.eq(ActivitySeckillSubscribeEntity::getSeckillId, seckillId);
        }
        wrapper.eq(ActivitySeckillSubscribeEntity::getUserId, userId)
                .select(ActivitySeckillSubscribeEntity::getGoodsId);
        for (ActivitySeckillSubscribeEntity row : this.list(wrapper)) {
            goodsIds.add(row.getGoodsId());
        }
        return ResponseResult.success(goodsIds);
    }

    @Override
    public ResponseResult countBySeckill(Map<String, Object> param) {
        Integer seckillId = toInt(param.get("seckillId"));
        Map<String, Object> result = new HashMap<>();
        if (seckillId == null) {
            return ResponseResult.success(result);
        }
        List<Map<String, Object>> rows = this.baseMapper.countBySeckillGroupGoods(seckillId);
        for (Map<String, Object> row : rows) {
            Object gid = row.get("goods_id") != null ? row.get("goods_id") : row.get("goodsId");
            Object cnt = row.get("subscribe_count") != null ? row.get("subscribe_count") : row.get("subscribeCount");
            if (gid != null) {
                result.put(String.valueOf(gid), cnt);
            }
        }
        return ResponseResult.success(result);
    }

    @Override
    public ResponseResult queryPage(Map<String, Object> param) {
        Integer seckillId = toInt(param.get("seckillId"));
        int page = toInt(param.get("page")) == null ? 1 : toInt(param.get("page"));
        int limit = toInt(param.get("limit")) == null ? 20 : toInt(param.get("limit"));
        LambdaQueryWrapper<ActivitySeckillSubscribeEntity> wrapper = new LambdaQueryWrapper<>();
        if (seckillId != null) {
            wrapper.eq(ActivitySeckillSubscribeEntity::getSeckillId, seckillId);
        }
        wrapper.orderByDesc(ActivitySeckillSubscribeEntity::getSubscribeTime);
        Page<ActivitySeckillSubscribeEntity> pageParam = new Page<>(page, limit);
        Page<ActivitySeckillSubscribeEntity> pageResult = this.page(pageParam, wrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("totalCount", pageResult.getTotal());
        return ResponseResult.success(result);
    }

    /* ---------------- 内部工具 ---------------- */

    private String resolveUserId(Map<String, Object> param) {
        Object token = param.get("token");
        if (token == null) {
            return null;
        }
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(String.valueOf(token));
        return userInfo == null ? null : userInfo.getUserId();
    }

    private String fetchMobile(String userId) {
        try {
            return userServiceClient.queryUserMobileByuserId(userId);
        } catch (Exception e) {
            log.warn("秒杀预约拉取用户手机号失败 userId={}: {}", userId, e.getMessage());
            return null;
        }
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
