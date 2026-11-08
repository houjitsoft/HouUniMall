package com.houjiang.activity.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.houjiang.activity.feign.OrderServiceClient;
import com.houjiang.activity.mapper.ActivityDiscountGoodsMapper;
import com.houjiang.plugin.api.extension.activity.ActivityGoodsLimitExtension;
import com.houjiang.activity.service.ActivityGoodsService;
import com.houjiang.activity.vo.CheckDiscountLimitVO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * @ClassName ActivityGoodsServiceImpl
 * @Description
 * @Author Amos
 * @Date 2023-12-06 14:13:09
 * @Version 1.0
 * @author Amos
 */
@Service
public class ActivityGoodsServiceImpl implements ActivityGoodsService {
    private static final Logger log = LoggerFactory.getLogger(ActivityGoodsServiceImpl.class);

    /**
     * 活动商品限购扩展点：限时秒杀、拼团等由各自插件提供判定，不向 goods 侧泄漏数据层。
     *
     * <p>原实现直接注入 ActivitySeckillGoodsMapper 读另一宿主（order）插件的数据表，
     * 插件化后插件间既不能互相 import，也不应共享数据层，故改为扩展点：
     * 秒杀插件实现 ActivityGoodsLimitExtension 自行查询，goods 侧只消费判定结果。
     */
    private final java.util.Map<String, ActivityGoodsLimitExtension> limitExtensions =
            new java.util.concurrent.ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void registerLimitExtensions(java.util.List<ActivityGoodsLimitExtension> extensions) {
        if (extensions == null) {
            return;
        }
        for (ActivityGoodsLimitExtension e : extensions) {
            String kind = e.kind();
            if (kind != null && !kind.isBlank()) {
                limitExtensions.putIfAbsent(kind, e);
            }
        }
    }

    /**
     * 按活动种类取限购判定；无实现返回 {@code null}（该活动不参与限购，不阻断下单）。
     *
     * <p>插件异常在此兜底：限购校验属辅助能力，绝不能因插件故障阻断下单主链路。
     */
    private ActivityGoodsLimitExtension.Context resolveLimit(String kind, Integer commonId,
                                                               Integer requestCount) {
        ActivityGoodsLimitExtension ext = limitExtensions.get(kind);
        if (ext == null) {
            return null;
        }
        ActivityGoodsLimitExtension.Context hit = new ActivityGoodsLimitExtension.Context();
        hit.commonId = commonId;
        hit.requestCount = requestCount;
        try {
            ext.execute(hit);
        } catch (RuntimeException ex) {
            log.warn("活动限购扩展执行失败，已跳过该活动类型: kind={}", kind, ex);
            return null;
        }
        return hit;
    }

    @Autowired
    private ActivityDiscountGoodsMapper activityDiscountGoodsMapper;

    @Autowired
    private OrderServiceClient orderServiceClient;

    /**
     * 根据传入的参数检查商品的购买限制。
     * 主要包括对限时折扣和秒杀活动商品的购买数量限制的检查。
     *
     * @param param 包含用户信息、商品信息等的参数映射。
     * @return 返回检查结果，如果购买限制检查失败，则返回具体的错误信息；如果检查成功，则返回成功信息。
     */
    @Override
    public ResponseResult checkGoodsLimit(Map<String, Object> param) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        // 如果用户信息存在
        if (null != userInfo) {
            // 获取用户ID
            String userId = userInfo.getUserId();
            // 从参数中获取商品列表
            List<Map<String,Object>> list = (List) param.get("list");
            // 初始化存储唯一商品ID的列表和对应商品信息的列表
            List<Integer> commonIds = new ArrayList<>();
            List<Map<String,Object>> commonMapList = new ArrayList<>();
            // 遍历商品列表，去重并累加相同商品的购买数量
            for (Map<String, Object> map : list) {
                Integer commonId = (Integer) map.get("commonId");
                // 如果当前商品ID未出现过，则添加到列表中
                if (!commonIds.contains(commonId)){
                    commonIds.add(commonId);
                    commonMapList.add(map);
                }else{
                    // 如果当前商品ID已出现过，则累加购买数量
                    for (Map<String, Object> stringObjectMap : commonMapList) {
                        if(stringObjectMap.get("commonId").equals(commonId)){
                            Integer goodsNum = (Integer) stringObjectMap.get("goodsNum") + (Integer) map.get("goodsNum");
                            stringObjectMap.put("goodsNum",goodsNum);
                        }
                    }
                }
            }
            // 初始化标志位，用于标记是否存在不受购买限制的商品
            Integer flag = 0;
            // 遍历去重后的商品列表，进行购买限制检查
            for (Map<String, Object> stringObjectMap : commonMapList) {
                Integer commonId = (Integer) stringObjectMap.get("commonId");
                Integer goodsNum = (Integer) stringObjectMap.get("goodsNum");
                // 检查限时折扣商品的购买限制
                // 限时折扣
                CheckDiscountLimitVO data = activityDiscountGoodsMapper.queryDiscountInfo(commonId);
                Integer count = null;
                if (null != data) {
                    // 检查商品的购买上限和活动时间
                    Integer goodsUpperLimit = data.getGoodsUpperLimit();
                    Integer discountUpperTime = data.getDiscountUpperTime();
                    // 如果商品购买上限和活动时间都为0，则跳过该商品的限制检查
                    if (0 == goodsUpperLimit && 0 == discountUpperTime) {
                        flag = 1;
                        continue;
                    }
                    // 根据活动时间计算出折扣活动的起始日期
                    Calendar cal = Calendar.getInstance();
                    Date fdate = new Date();
                    cal.setTime(fdate);
                    cal.add(Calendar.DATE, -discountUpperTime);
                    fdate = cal.getTime();
                    // 更新参数映射中的日期和商品ID，用于查询订单数量
                    LocalDate today = LocalDate.now();
                    LocalDateTime startOfDay = today.atStartOfDay();
                    LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);
                    param.put("fDate", fdate);
                    param.put("commonId", commonId);
                    param.put("userId", userId);
                    param.put("goodsPrice", data.getDiscountPrice());
                    param.put("goodsStartTime", startOfDay);
                    param.put("goodsEndTime", endOfDay);
                    // 查询用户在当前折扣活动中的订单数量
                    count = orderServiceClient.queryOrderGoodsCountFeign(param);
                    count = count == null ? 0 : count;
                    // 如果订单数量加上当前请求购买的数量超过商品购买上限，则返回购买限制错误信息
                    if ((count + goodsNum) > goodsUpperLimit) {
                        return ResponseResult.fail(2,"商品【"+data.getGoodsName()+"】已达购买上限");
                    }
                }

                // 秒杀限购：数据归 order-seckill 宿主，跨宿主不得直接注入其 Mapper，
                // 改由该插件通过 ActivityGoodsLimitExtension 提供判定结果。
                ActivityGoodsLimitExtension.Context hit = resolveLimit("seckill", commonId, goodsNum);
                if (hit != null && hit.involved) {
                    // 商品购买上限为 0 表示不限购，跳过该商品的限购检查
                    if (hit.upperLimit == null || hit.upperLimit == 0) {
                        flag = 1;
                        continue;
                    }
                    // 查询用户在当前秒杀活动中的订单件数
                    if (null != userId) {
                        param.put("commonId", commonId);
                        param.put("userId", userId);
                        Integer count2 = orderServiceClient.queryOrderGoodsCountFeign(param);
                        hit.boughtCount = count2 == null ? 0 : count2;
                    }
                    // 已购件数 + 本次请求件数超过购买上限
                    if ((hit.boughtCount + goodsNum) >= hit.upperLimit) {
                        return ResponseResult.fail(2, "商品【" + hit.goodsName + "】已达购买上限");
                    }
                }
            }
            // 如果所有商品都通过了购买限制检查，则返回成功信息
            if(flag == 1){
                return ResponseResult.success();
            }
            return ResponseResult.success();
        }else{
            // 如果用户信息不存在，则返回令牌无效的错误信息
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);

        }
    }

}

