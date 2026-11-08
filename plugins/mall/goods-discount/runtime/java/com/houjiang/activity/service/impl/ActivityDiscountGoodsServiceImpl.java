package com.houjiang.activity.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.activity.dto.DiscountBaseDetailQueryDTO;
import com.houjiang.activity.dto.DiscountGoodsAddParamDTO;
import com.houjiang.activity.dto.DiscountGoodsListDTO;
import com.houjiang.activity.entity.ActivityDiscountBaseEntity;
import com.houjiang.activity.entity.ActivityDiscountGoodsEntity;
import com.houjiang.activity.feign.GoodsServiceClient;
import com.houjiang.activity.feign.OrderServiceClient;
import com.houjiang.activity.mapper.ActivityDiscountBaseMapper;
import com.houjiang.activity.mapper.ActivityDiscountGoodsMapper;
import com.houjiang.activity.service.ActivityDiscountGoodsService;
import com.houjiang.activity.vo.*;
import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.dto.goods.DiscountGoodsListVO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.activity.AppDiscountGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.CheckDiscountLimitResultVO;
import com.houjiang.commons.vo.activity.ModularGoodsVO;
import com.houjiang.commons.vo.goods.DiscountGoodsInfoVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ActivityDiscountGoodsServiceImpl
 * @Description 限时折扣商品表
 * @Author Amos
 * @Date 2023-05-25 14:10:09
 * @Version 1.0
 * @author Amos
 */
@Service
public class ActivityDiscountGoodsServiceImpl extends ServiceImpl<ActivityDiscountGoodsMapper, ActivityDiscountGoodsEntity>
        implements ActivityDiscountGoodsService {

    @Autowired
    private ActivityDiscountGoodsMapper activityDiscountGoodsMapper;

    @Autowired
    private ActivityDiscountBaseMapper activityDiscountBaseMapper;

    @Autowired
    private GoodsServiceClient goodsServiceClient;

    @Autowired
    private OrderServiceClient orderServiceClient;
    
    @Autowired
    private RedisCache redisCache;

    /**
     * 根据商品ID查询对应的促销信息。
     *
     * @param goodsId 商品的唯一标识ID。
     * @return 返回查询到的促销信息，包含在AppDiscountGoodsQueryResultVO对象中。
     */
    @Override
    public AppDiscountGoodsQueryResultVO selectDiscountByGoodsId(Integer goodsId) {
        // 调用BaseMapper的selectDiscountByGoodsId方法，根据goodsId查询促销信息
        return this.baseMapper.selectDiscountByGoodsId(goodsId);
    }

    /**
     * 根据商品ID查询优惠信息。
     *
     * 本方法通过调用基本Mapper接口，查询指定商品ID对应的优惠信息。
     * 主要用于在前端展示或后台管理中，根据商品ID快速获取该商品的优惠情况。
     *
     * @param goodsId 商品ID，作为查询条件。
     * @return CheckDiscountLimitResultVO 返回查询到的优惠信息结果集。
     */
    @Override
    public CheckDiscountLimitResultVO queryDiscountInfoByGoodsId(Integer goodsId) {
        return this.baseMapper.queryDiscountInfoByGoodsId(goodsId);
    }

    /**
     * 根据商品ID和时间查询优惠信息。
     *
     * 本方法旨在通过商品ID和特定时间查询相应的优惠信息。首先尝试根据商品ID和时间查询优惠，如果不存在，则退而求其次
     * 只根据商品ID查询。这种设计考虑了不同场景下的查询需求，首先尝试获取更精确的优惠信息，如果不可行，则返回一般的优惠信息。
     *
     * @param goodsId 商品的唯一标识ID。是查询优惠信息的关键参数。
     * @return 返回查询到的优惠信息。如果根据商品ID和时间未找到优惠信息，则返回根据商品ID查询到的优惠信息；如果都未找到，则返回null。
     */
    @Override
    public AppDiscountGoodsQueryResultVO selectDiscountByGoodsIdAndTime(Integer goodsId) {
        // 尝试根据商品ID和时间查询优惠信息
        AppDiscountGoodsQueryResultVO vo = this.baseMapper.selectDiscountByGoodsIdAndTime(goodsId);
        // 如果未查询到优惠信息，则尝试只根据商品ID查询
        if(vo == null){
            vo = this.baseMapper.selectDiscountByGoodsId(goodsId);
        }
        // 返回查询结果
        return vo;
    }

    /**
     * 查询优惠限制指数信息。
     * 该方法根据传入的参数查询特定用户的优惠限制指数，以及对应的商品列表。
     * 如果用户有优惠折扣，会为查询到的商品列表设置该折扣。
     * 最后，根据参数指定的数量，随机选择商品返回。
     *
     * @param param 查询参数，包括用户令牌和限制返回的商品数量。
     * @return 返回查询结果，包含设置好折扣的商品列表。
     */
    @Override
    public ResponseResult queryDiscountLimitIndex(Map<String, Object> param) {
        // 首页允许匿名访问：token/limitSize 缺省时也必须返回可用的空/有限列表，不能因拆箱空值抛 11009。
        Map<String, Object> query = param == null ? new HashMap<>() : param;
        Object token = query.get("token");
        String userInfoStr = token == null ? null
                : (String) redisCache.getCacheObject(HouConfig.HOU_USER_TOKEN_USER + token);
        BigDecimal discount = null;
        // 如果用户信息存在，则解析用户信息并获取优惠折扣
        if (null != userInfoStr) {
            UserInfoVO dto = JSON.parseObject(userInfoStr, UserInfoVO.class);
            discount = dto.getPlusDiscount();
        }
        List<DiscountGoodsListQueryResultVO> returnList = new ArrayList<>();
        // 根据参数查询商品列表
        List<DiscountGoodsListQueryResultVO> promotionGoodsList = this.baseMapper.queryDiscountLimitIndexGoods(query);
        // 如果查询结果为空，则直接返回空列表
        if (CollectionUtils.isEmpty(promotionGoodsList)) {
            returnList = Lists.newArrayList();
        } else {
            // 为查询到的商品列表设置用户优惠折扣
            for (DiscountGoodsListQueryResultVO discountGoodsListQueryResult : promotionGoodsList) {
                discountGoodsListQueryResult.setDiscountMember(discount);
            }
            // 获取参数中指定的返回商品数量
            Object limitValue = query.get("limitSize");
            int limitSize = limitValue instanceof Number ? ((Number) limitValue).intValue() : promotionGoodsList.size();
            limitSize = Math.max(0, limitSize);
            int sumSize = promotionGoodsList.size();
            // 如果指定数量大于查询到的商品数量，则实际返回数量为商品数量
            if (sumSize < limitSize) {
                limitSize = sumSize;
            }
            // 使用随机数选择商品，直到选满指定数量
            Random rand = new Random();
            for (int i = 0; i < limitSize; i++) {
                sumSize = promotionGoodsList.size();
                int randNum = rand.nextInt(sumSize);
                returnList.add(promotionGoodsList.get(randNum));
                promotionGoodsList.remove(promotionGoodsList.get(randNum));
            }
        }
        // 返回查询结果
        return ResponseResult.success(returnList);
    }

    /**
     * 根据传入的参数检查折扣限制。
     * 该方法主要用于验证用户购买商品的数量是否符合折扣活动的限制条件。
     * @param param 包含用户令牌和商品信息的参数映射。
     * @return 返回检查结果，如果检查通过，则返回成功消息；否则，返回失败消息和错误代码。
     */
    @Override
    public ResponseResult checkDiscountLimit(Map<String, Object> param) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        // 如果用户信息存在
        if (null != userInfo) {
            // 获取用户ID
            String userId = userInfo.getUserId();
            // 从参数中获取商品列表
            List<Map<String,Object>> list = (List) param.get("list");
            // 初始化包含所有不同商品ID的列表
            List<Integer> commonIds = new ArrayList<>();
            // 初始化用于存储每个商品详细信息的列表
            List<Map<String,Object>> commonMapList = new ArrayList<>();
            // 遍历商品列表，去重并累加相同商品的数量
            for (Map<String, Object> map : list) {
                Integer commonId = (Integer) map.get("commonId");
                // 如果当前商品ID未出现过，则添加到列表中
                if (!commonIds.contains(commonId)){
                    commonIds.add(commonId);
                    commonMapList.add(map);
                }else{
                    // 如果当前商品ID已存在，则累加该商品的数量
                    for (Map<String, Object> stringObjectMap : commonMapList) {
                        if(stringObjectMap.get("commonId") == commonId){
                            Integer goodsNum = (Integer) stringObjectMap.get("goodsNum") + (Integer) map.get("goodsNum");
                            stringObjectMap.put("goodsNum",goodsNum);
                        }
                    }
                }
            }
            // 初始化标志变量，用于标记是否存在不限购的商品
            Integer flag = 0;
            // 遍历每个商品，检查购买数量是否符合折扣限制
            for (Map<String, Object> stringObjectMap : commonMapList) {
                Integer commonId = (Integer) stringObjectMap.get("commonId");
                Integer goodsNum = (Integer) stringObjectMap.get("goodsNum");
                // 根据商品ID查询折扣信息
                CheckDiscountLimitVO data = this.baseMapper.queryDiscountInfo(commonId);
                // 初始化计数器，用于统计当前用户针对当前商品的已购买数量
                Integer count = null;
                // 如果折扣信息存在
                if (null != data) {
                    // 获取商品限购数量和折扣期限
                    Integer goodsUpperLimit = data.getGoodsUpperLimit();
                    Integer discountUpperTime = data.getDiscountUpperTime();
                    // 如果商品限购数量和折扣期限均为0，表示该商品不限购
                    if (0 == goodsUpperLimit && 0 == discountUpperTime) {
                        flag = 1;
                        continue;
                    }
                    // 计算折扣期限内的起始日期
                    Calendar cal = Calendar.getInstance();
                    Date fdate = new Date();
                    cal.setTime(fdate);
                    cal.add(Calendar.DATE, -discountUpperTime);
                    fdate = cal.getTime();
                    // 根据当前日期计算折扣活动的起始和结束时间
                    LocalDate today = LocalDate.now();
                    LocalDateTime startOfDay = today.atStartOfDay();
                    LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);
                    // 更新参数映射，用于查询订单数量
                    param.put("fDate", fdate);
                    param.put("commonId", commonId);
                    param.put("seckillId", data.getDiscountId());
                    param.put("userId", userId);
                    param.put("goodsPrice", data.getDiscountPrice());
                    param.put("goodsStartTime", startOfDay);
                    param.put("goodsEndTime", endOfDay);
                    // 查询用户在折扣期限内针对当前商品的购买数量
                    count = orderServiceClient.queryOrderGoodsCount(param);
                    count = count == null ? 0 : count;
                    // 如果当前用户的购买数量加上待购买数量超过限购数量，则返回限购错误信息
                    if ((count + goodsNum) > goodsUpperLimit) {
                        return ResponseResult.fail(BizCodeEnume.ACTIVITY_LIMIT_PURCHASE_LIMIT);
                    }
                } else {
                    // 如果折扣信息不存在，则表示活动已结束
                    return ResponseResult.fail(BizCodeEnume.ACTIVITY_ACTIVITY_ENDED);
                }
            }
            // 如果存在不限购的商品，则返回成功消息
            if(flag == 1){
                return ResponseResult.success("该商品不限购");
            }
            // 如果所有商品都未超过限购限制，则返回成功消息
            return ResponseResult.success(BizCodeEnume.ACTIVITY_NO_PURCHASE_LIMIT.getMsg());
        }else{
            // 如果用户信息不存在，则返回令牌无效的错误信息
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);

        }
    }


    /**
     * 根据传入的参数检查折扣限制。
     * 该方法主要用于查询某个商品的折扣信息，并根据用户的购买情况和折扣有效期来判断用户当前是否能够享受折扣。
     * @param param 一个Map对象，包含commonId（商品通用ID）和其他可能的参数。
     * @return 返回一个ResponseResult对象，其中包含查询到的折扣信息和是否能够享受折扣的结果。
     */
    @Override
    public ResponseResult checkDiscountLimit2(Map<String, Object> param) {
        // 从参数中获取商品通用ID
        Integer commonId = (Integer) param.get("commonId");
        // 根据商品通用ID查询折扣信息
        CheckDiscountLimitVO data = this.baseMapper.queryDiscountInfo(commonId);
        // 初始化购买数量
        Integer count = 0;
        // 初始化用户ID
        String userId = null;

        // 检查参数中是否包含token，并尝试从token中获取用户ID
        if (null != param.get("token") && StringUtils.isNotBlank(param.get("token").toString())) {
            boolean isUserExist = redisCache.hasKey(HouConfig.HOU_USER_TOKEN_USER + param.get("token").toString());
            if(isUserExist){
                UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
                userId = userInfo.getUserId();
            }else{
                // 如果没有提供token或token无效，则不考虑用户购买数量，直接返回折扣信息
                data.setBuyNum(count);
                return ResponseResult.success(data);
            }

        } else {
            // 如果没有提供token或token无效，则不考虑用户购买数量，直接返回折扣信息
            data.setBuyNum(count);
            return ResponseResult.success(data);
        }

        // 如果查询到折扣信息
        if (null != data) {
            // 获取折扣有效时间，并计算出折扣有效期截止日期
            Integer discountUpperTime = data.getDiscountUpperTime();
            Calendar cal = Calendar.getInstance();
            Date fdate = new Date();
            cal.setTime(fdate);
            cal.add(Calendar.DATE, -discountUpperTime);
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);

            // 将商品ID、用户ID和折扣有效期添加到参数中，用于后续查询用户购买数量
            param.put("commonId", commonId);
            param.put("userId", userId);
            param.put("goodsStartTime", startOfDay);
            param.put("goodsEndTime", endOfDay);
            param.put("seckillId", param.get("discountId"));

            // 如果用户ID不为空，则查询用户购买数量
            if(userId != null) {
                count = orderServiceClient.queryOrderGoodsCount(param);
            }
            // 如果查询结果为null，则默认购买数量为0
            count = count == null ? 0 : count;

            // 更新折扣信息中的购买数量，并返回成功结果
            data.setBuyNum(count);
            return ResponseResult.success(data);
        } else {
            // 如果没有查询到折扣信息，则返回失败结果，提示token无效
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    /**
     * 删除优惠基础信息。
     *
     * @param param 优惠详情查询DTO，包含优惠ID和用户令牌信息。
     * @return 如果操作成功，返回成功的响应结果；如果用户权限不足，返回失败的响应结果。
     *
     * 此方法首先判断用户是否有权限执行删除操作。如果有权限，则查询指定优惠ID相关的商品ID列表。
     * 如果该列表不为空，进一步查询这些商品中是否有正在其他活动中使用的商品。如果没有，则将这些商品的
     * 是否显示状态更新。最后，删除优惠商品关联信息和优惠基础信息。
     */
    @Override
    @Transactional
    public ResponseResult deleteDiscountBase(DiscountBaseDetailQueryDTO param) {
        // 判断用户权限
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            // 查询指定优惠ID相关的商品ID列表
            List<Integer> commonIdList = activityDiscountGoodsMapper.selectDiscountGoods(param.getDiscountId());

            // 检查是否有正在其他活动中使用的商品
            if (!org.springframework.util.CollectionUtils.isEmpty(commonIdList)) {
                // 获取其他正在进行的活动
                List<Integer> activeCommonIdList = activityDiscountGoodsMapper.selectActiveDiscountGoods(param.getDiscountId(),
                        commonIdList.get(0));
                if (org.springframework.util.CollectionUtils.isEmpty(activeCommonIdList)) {
                    // 如果没有，则更新商品的是否显示状态
                    goodsServiceClient.updateCommonIsXian2(commonIdList);
                }
            }
            // 删除优惠商品关联信息和优惠基础信息
            activityDiscountGoodsMapper.deleteDiscountGoods(param.getDiscountId());
            activityDiscountBaseMapper.deleteByPrimaryKey(param.getDiscountId());
        } else {
            // 返回权限不足的错误信息
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        // 返回操作成功的响应结果
        return ResponseResult.success();
    }


    /**
     * 删除促销商品。
     *
     * @param param 优惠详情查询DTO，包含需要删除的促销商品的信息。
     * @return 如果删除成功，返回成功的响应结果；如果用户权限不足，返回失败的响应结果。
     *
     * 此方法首先判断用户是否有权限执行此操作，然后根据传入的参数判断是否需要更新商品的促销状态。
     * 如果商品当前没有其他正在进行的促销活动，则将商品的促销状态设为非促销，并删除该促销商品。
     * 最后，如果用户有权限且操作成功，返回成功的响应结果。
     */
    @Override
    @Transactional
    public ResponseResult deleteDiscountGoods(DiscountBaseDetailQueryDTO param) {
        // 判断用户权限
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            Integer commonIsXian = 0;

            // 判断传入的商品是否有正在进行的其他促销活动
            if (null != param.getCommonId()) {
                // 获取其他正在进行的活动
                List<Integer> activeCommonIdList = activityDiscountGoodsMapper.selectActiveDiscountGoods(param.getDiscountId(),
                        param.getCommonId());
                // 如果没有其他活动，则更新商品促销状态为非促销
                if (org.springframework.util.CollectionUtils.isEmpty(activeCommonIdList)) {
                    Map<String,Object> map = new HashMap<>();
                    map.put("commonIsXian",commonIsXian);
                    map.put("commonId",param.getCommonId());
                    goodsServiceClient.updateCommonIsXian(map);
                }

            }
            // 删除促销商品
            activityDiscountGoodsMapper.deleteDiscountGoods2(param);
        } else {
            // 用户权限不足，返回失败结果
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }

        // 操作成功，返回成功结果
        return ResponseResult.success();
    }




    /**
     * 添加折扣商品信息
     *
     * @param param 添加折扣商品的参数DTO
     * @return 返回操作结果，成功或失败
     */
    @Override
    @Transactional
    public ResponseResult addDiscountGoods(DiscountGoodsAddParamDTO param) {
        // 判断用户权限
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        // 检查用户是否有权限
        if (userPermissionsResult.getFlag()) {
            List<DiscountGoodsListDTO> discountGoodsList = param.getDiscountGoodsList();
            String shopId = userPermissionsResult.getShopId();

            // 根据折扣ID查询折扣活动基础详情
            DiscountBaseDetailResultVO discountBase = activityDiscountBaseMapper
                    .selectDiscountBaseDetail2(param.getDiscountId());

            Integer commonId = null;

            // 检查传入的商品列表是否为空
            if (org.springframework.util.CollectionUtils.isEmpty(discountGoodsList)) {
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "无效必传参数：" + JSON.toJSONString(param));
            }

            // 校验商品规格是否重复
            Integer commonId2 = discountGoodsList.get(0).getCommonId();
            Integer flag = activityDiscountGoodsMapper.checkSpecNum(commonId2);
            if (flag.equals(1)) {
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(),BizCodeEnume.SPEC_REPEAT.getMsg()+JSON.toJSONString(param));
            }

            // 遍历商品列表，为每个商品添加折扣信息
            for (DiscountGoodsListDTO discountGoods : discountGoodsList) {
                commonId = discountGoods.getCommonId();

                // 设置新增折扣商品的信息
                DiscountGoodsAddParamDTO addGoods = new DiscountGoodsAddParamDTO();
                // 从折扣活动基础详情中获取相关属性填充到新增商品中
                addGoods.setDiscountId(param.getDiscountId());
                addGoods.setDiscountName(discountBase.getDiscountName());
                addGoods.setDiscountTitle(discountBase.getDiscountTitle());
                addGoods.setDiscountExplain(discountBase.getDiscountExplain());
                addGoods.setGoodsStartTime(discountBase.getDiscountStartTime());
                addGoods.setGoodsEndTime(discountBase.getDiscountEndTime());
                addGoods.setGoodsUpperLimit(discountBase.getDiscountUpperLimit());
                addGoods.setGoodsLowerLimit(discountBase.getDiscountLowerLimit());
                addGoods.setDiscountGoodsState(discountBase.getDiscountState());
                addGoods.setDiscountUpperTime(discountBase.getDiscountUpperTime());
                addGoods.setPreStartHour(discountBase.getPreStartHour());
                if(null != discountBase.getPreStartHour() && 0 != discountBase.getPreStartHour()){
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Calendar cal = Calendar.getInstance();
                    Date fdate = discountBase.getDiscountStartTime();
                    cal.setTime(fdate);
                    cal.add(Calendar.HOUR,-discountBase.getPreStartHour());
                    fdate = cal.getTime();
                    addGoods.setPreStartTime(fdate);
                }
                addGoods.setDiscountGoodsRecommend(0);
                addGoods.setGoodsBuyCount(0);

                // 设置商品的具体信息
                addGoods.setGoodsId(discountGoods.getGoodsId());
                addGoods.setGoodsName(discountGoods.getGoodsName());
                addGoods.setGoodsImage(discountGoods.getGoodsImage());
                addGoods.setGoodsPrice(discountGoods.getGoodsPrice());
                addGoods.setDiscountPrice(discountGoods.getDiscountPrice());
                addGoods.setCommonId(commonId);
                addGoods.setShopId(shopId);
                addGoods.setDiscountAmount(discountGoods.getDiscountAmount());

                // 根据折扣商品ID判断是插入新商品还是更新已有商品
                if (null == param.getDiscountGoodsId()) {
                    activityDiscountGoodsMapper.insertDiscountGoods(addGoods);
                } else {
                    activityDiscountGoodsMapper.updateDiscountGoods(addGoods);
                }

                // 更新商品的显示折扣字段
                if (null != commonId) {
                    Integer commonIsXian = 3;
                    Map<String,Object> param2 = new HashMap<>();
                    param2.put("commonIsXian",commonIsXian);
                    param2.put("commonId",commonId);
                    goodsServiceClient.updateCommonIsXian(param2);
                }
            }
        } else {
            // 用户权限不足，返回失败结果
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        // 操作成功，返回成功结果
        return ResponseResult.success();
    }


    /**
     * 更新促销商品价格。
     *
     * 本方法用于根据传入的参数更新促销商品的价格。首先，它会通过用户权限服务验证调用者的权限。
     * 如果调用者具有足够的权限，那么它将调用数据库操作类来更新促销商品的价格信息。
     * 如果调用者权限不足，方法将返回一个表示失败的响应结果，其中包含权限不足的错误信息。
     *
     * @param param 包含促销商品价格更新信息的参数对象。
     * @return 如果更新成功，返回一个表示成功的响应结果；如果权限不足，返回一个表示失败的响应结果。
     */
    @Override
    @Transactional
    public ResponseResult updateDiscountGoodsPrice(DiscountGoodsAddParamDTO param) {
        // 验证调用者的权限
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        if (userPermissionsResult.getFlag()) {
            // 如果权限验证通过，更新促销商品价格
            activityDiscountGoodsMapper.updateDiscountGoodsPrice(param);

        } else {
            // 如果权限验证失败，返回权限不足的错误信息
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        // 如果权限验证通过，返回成功信息
        return ResponseResult.success();
    }

    /**
     * 根据折扣详情查询DTO查询折扣商品规格列表。
     *
     * @param param 折扣基础详情查询DTO，包含查询所需参数。
     * @return 返回查询结果，如果查询失败，返回错误信息。
     */
    @Override
    public ResponseResult queryDiscountGoodsSpecList(DiscountBaseDetailQueryDTO param) {
        // 检查必要参数是否为空
        if (null == param.getCommonId() || null == param.getDiscountId()) {
            // 如果必要参数为空，则返回失败响应，指示缺少参数
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.MISSING_PARAMETER.getMsg());
        }
        // 根据参数查询折扣商品详情
        List<DiscountGoodsListVO> discountGoodsList = activityDiscountGoodsMapper.selectDiscountGoodsDetail2(param);
        // 如果查询结果为空，则初始化一个空列表
        if (org.springframework.util.CollectionUtils.isEmpty(discountGoodsList)) {
            discountGoodsList = Lists.newArrayList();
        }
        // 返回查询成功的响应，包含折扣商品详情列表
        return ResponseResult.success(discountGoodsList);
    }

    /**
     * 根据商品ID查询商品的模块化价格信息。
     *
     * @param goodsId 商品的唯一标识ID。
     * @return 返回查询结果，包含商品的模块化价格信息。
     */
    @Override
    public ResponseResult selectPriceByGoodsId(Integer goodsId) {
        // 通过商品ID查询商品的模块化价格信息
        ModularGoodsVO modularGoodsVO = activityDiscountGoodsMapper.selectPriceByGoodsId(goodsId);
        // 返回查询结果，如果查询成功，则将查询结果封装在ResponseResult对象中返回
        return ResponseResult.success(modularGoodsVO);
    }

    /**
     * 查询折扣基础数据
     * 根据传入的参数查询与折扣相关的商品列表、销售数量、成交金额等数据，并统计新老顾客的订单数量。
     *
     * @param param 查询参数，包括折扣ID等信息
     * @return 返回包含折扣数据列表、总数量、总成交金额、新老顾客订单数量等信息的响应结果
     */
    @Override
    public ResponseResult queryDiscountBaseData(Map<String, Object> param) {
        // 检查是否传入了折扣ID
        if(null != param.get("discountId")){
            // 初始化返回结果对象
            DiscountDataListVO result = new DiscountDataListVO();
            List<DiscountDataVO> dataList = new ArrayList<>();
            // 记录所有商品的总数量
            Integer allGoodsCount = 0;
            // 记录所有商品的总成交金额
            BigDecimal allDealAmount = BigDecimal.ZERO;
            // 记录所有新顾客的订单数量
            Integer allNewerOrderNum = 0;
            // 记录所有老顾客的订单数量
            Integer allOlderOrderNum = 0;

            // 查询折扣商品信息，分页处理
            IPage<DiscountGoodsListVO> page=this.baseMapper.queryDiscountGoodsInfo(new Query<DiscountGoodsListVO>().getPage(param),
                    param);

            // 获取当前日期，用于计算折扣活动的开始时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            Date startTime = calendar.getTime();
            // 设置折扣活动的结束时间为前一天的零点
            calendar.add(Calendar.DATE,-1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // 如果查询结果存在，则遍历处理每个商品的详细数据
            if(null != page && page.getRecords().size()>0){
                for(DiscountGoodsListVO discountGoodsListResult:page.getRecords()){
                    // 统计新老顾客订单数量的映射表
                    Map<Integer,Integer> newerNumMap = new HashMap<>();
                    Map<Integer,Integer> olderNumMap = new HashMap<>();
                    // 商品基本信息
                    Integer goodsId = discountGoodsListResult.getGoodsId();
                    DiscountDataVO data = new DiscountDataVO();
                    data.setGoodsName(discountGoodsListResult.getGoodsName());
                    data.setSpecName(discountGoodsListResult.getSpecValueName());
                    // 根据当前时间调整折扣活动的开始和结束时间
                    if (calendar.getTime().before(discountGoodsListResult.getStartDateTime())){
                        discountGoodsListResult.setEndDateTime(discountGoodsListResult.getStartDateTime());
                    }else{
                        discountGoodsListResult.setEndDateTime(calendar.getTime());
                    }
                    discountGoodsListResult.setStartDateTime(startTime);
                    // 查询商品的销售数据
                    DiscountGoodsInfoVO discountGoodsInfo= orderServiceClient.queryDiscountGoodsInfo2(discountGoodsListResult);
                    if(null != discountGoodsInfo){
                        data.setDealAmount(discountGoodsInfo.getDealAmount());
                        data.setGoodsNum(discountGoodsInfo.getGoodsNum());
                        allGoodsCount = allGoodsCount + discountGoodsInfo.getGoodsNum();
                        allDealAmount = allDealAmount.add(discountGoodsInfo.getDealAmount());
                    }else{
                        data.setDealAmount(BigDecimal.ZERO);
                        data.setGoodsNum(0);
                    }
                    // 查询购买该商品的顾客ID列表
                    List<String> buyerUserIds =  orderServiceClient.queryBuyerUsers(discountGoodsListResult);
                    // 遍历顾客ID列表，统计新老顾客的订单数量
                    for(String userId:buyerUserIds) {
                        // 统计参数
                        Map<String, Object> paramMap = new HashMap<>();
                        paramMap.put("userId", userId);
                        paramMap.put("startDateTime", discountGoodsListResult.getStartDateTime());
                        paramMap.put("shopId",discountGoodsListResult.getShopId());
                        paramMap.put("endDateTime",discountGoodsListResult.getEndDateTime());
                        paramMap.put("goodsId",discountGoodsListResult.getGoodsId());
                        paramMap.put("discountPrice",discountGoodsListResult.getDiscountPrice());

                        // 查询该顾客是否为老顾客的订单数量
                        Integer olderCount = orderServiceClient.queryCountForOlder(paramMap);
                        // 查询该顾客的订单总数
                        Integer orderCount = orderServiceClient.queryUserOrderCount(paramMap);
                        // 根据是否为老顾客，分别统计新老顾客的订单数量
                        if (null != olderCount) {
                            // 新客
                            if (0 == olderCount) {
                                if(null == newerNumMap.get(goodsId)){
                                    newerNumMap.put(goodsId,orderCount);
                                }else{
                                    newerNumMap.put(goodsId,newerNumMap.get(goodsId)+orderCount);
                                }
                                allNewerOrderNum = allNewerOrderNum + orderCount;
                            } else {
                                if(null == olderNumMap.get(goodsId)){
                                    olderNumMap.put(goodsId,orderCount);
                                }else{
                                    olderNumMap.put(goodsId,olderNumMap.get(goodsId)+orderCount);
                                }
                                allOlderOrderNum = allOlderOrderNum + orderCount;
                            }
                        }
                    }

                    // 设置商品的新老顾客订单数量
                    if(null != newerNumMap.get(goodsId)){
                        data.setNewerOrderNum(newerNumMap.get(goodsId));
                    }else{
                        data.setNewerOrderNum(0);
                    }

                    if(null != olderNumMap.get(goodsId)){
                        data.setOlderOrderNum(olderNumMap.get(goodsId));
                    }else{
                        data.setOlderOrderNum(0);
                    }

                    dataList.add(data);
                }
                // 设置返回结果的详细数据和统计信息
                result.setDataEntityList(dataList);
                result.setCount((int) page.getTotal());
                result.setAllDealAmount(allDealAmount);
                result.setAllNewerOrderNum(allNewerOrderNum);
                result.setAllOlderOrderNum(allOlderOrderNum);
                result.setAllGoodsCount(allGoodsCount);

            }
            // 返回查询结果
            return ResponseResult.success(result);
        }else{
            // 如果缺少参数，则返回失败结果
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
        }
    }

    /**
     * 查询折扣基础数据导出信息。
     * @param param 查询参数映射表。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult queryDiscountBaseDataExp(Map<String, Object> param) {
        // 定义导出Excel的标题
        String[] title = {"商品名称", "规格", "付款商品件数", "总成交额", "新客户下单数", "老客户下单数"};
        // 初始化数据列表
        List<Map<String, String>> list = Lists.newArrayList();

        // 初始化返回结果对象
        List<DiscountDataVO> dataList = new ArrayList<>();
        // 初始化统计变量
        BigDecimal allDealAmount = BigDecimal.ZERO;

        // 查询折扣商品信息
        IPage<DiscountGoodsListVO> page = this.baseMapper.queryDiscountGoodsInfo(
                new Query<DiscountGoodsListVO>().getPage(param), param);

        // 获取当前日期作为查询的结束时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Date startTime = calendar.getTime();
        // 设置查询的开始时间为前一天的0点
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // 如果查询结果不为空
        if (null != page && page.getRecords().size() > 0) {
            // 遍历查询结果
            for (DiscountGoodsListVO discountGoodsListResult : page.getRecords()) {
                // 初始化新老客户订单数量映射
                Map<Integer, Integer> newerNumMap = new HashMap<>();
                Map<Integer, Integer> olderNumMap = new HashMap<>();
                // 处理商品信息
                Integer goodsId = discountGoodsListResult.getGoodsId();
                DiscountDataVO data = new DiscountDataVO();
                data.setGoodsName(discountGoodsListResult.getGoodsName());
                data.setSpecName(discountGoodsListResult.getSpecValueName());
                // 调整时间范围
                if (calendar.getTime().before(discountGoodsListResult.getStartDateTime())) {
                    discountGoodsListResult.setEndDateTime(discountGoodsListResult.getStartDateTime());
                } else {
                    discountGoodsListResult.setEndDateTime(calendar.getTime());
                }
                discountGoodsListResult.setStartDateTime(startTime);
                // 查询商品的详细折扣信息
                DiscountGoodsInfoVO discountGoodsInfo = orderServiceClient.queryDiscountGoodsInfo2(discountGoodsListResult);
                // 处理商品的成交金额和销售数量
                if (null != discountGoodsInfo) {
                    data.setDealAmount(discountGoodsInfo.getDealAmount());
                    data.setGoodsNum(discountGoodsInfo.getGoodsNum());
                    allDealAmount = allDealAmount.add(discountGoodsInfo.getDealAmount());
                } else {
                    data.setDealAmount(BigDecimal.ZERO);
                    data.setGoodsNum(0);
                }
                // 查询购买该商品的用户ID列表
                List<String> buyerUserIds = orderServiceClient.queryBuyerUsers(discountGoodsListResult);
                // 统计新老客户订单数量
                for (String userId : buyerUserIds) {
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("userId", userId);
                    paramMap.put("startDateTime", discountGoodsListResult.getStartDateTime());
                    paramMap.put("shopId", discountGoodsListResult.getShopId());
                    paramMap.put("endDateTime", discountGoodsListResult.getEndDateTime());
                    paramMap.put("goodsId", discountGoodsListResult.getGoodsId());
                    paramMap.put("discountPrice", discountGoodsListResult.getDiscountPrice());
                    // 统计订单数量
                    Integer olderCount = orderServiceClient.queryCountForOlder(paramMap);
                    Integer orderCount = orderServiceClient.queryUserOrderCount(paramMap);
                    // 根据是否是老客户，更新订单数量统计
                    if (null != olderCount) {
                        if (0 == olderCount) {
                            if (null == newerNumMap.get(goodsId)) {
                                newerNumMap.put(goodsId, orderCount);
                            } else {
                                newerNumMap.put(goodsId, newerNumMap.get(goodsId) + orderCount);
                            }
                        } else {
                            if (null == olderNumMap.get(goodsId)) {
                                olderNumMap.put(goodsId, orderCount);
                            } else {
                                olderNumMap.put(goodsId, olderNumMap.get(goodsId) + orderCount);
                            }
                        }
                    }
                }
                // 更新数据对象的新老客户订单数量
                data.setNewerOrderNum(null != newerNumMap.get(goodsId) ? newerNumMap.get(goodsId) : 0);
                data.setOlderOrderNum(null != olderNumMap.get(goodsId) ? olderNumMap.get(goodsId) : 0);
                dataList.add(data);
            }
            // 处理完所有商品后，将数据对象转换为Excel表格格式
            for (DiscountDataVO data : dataList) {
                HashMap<String, String> map = com.google.common.collect.Maps.newHashMap();
                map.put("商品名称", data.getGoodsName());
                map.put("规格", data.getSpecName());
                map.put("付款商品件数", data.getGoodsNum() + "");
                map.put("总成交额", data.getDealAmount() + "");
                map.put("新客户下单数", data.getNewerOrderNum() + "");
                map.put("老客户下单数", data.getOlderOrderNum() + "");
                list.add(map);
            }
            // 生成Excel文件并返回下载链接
            Map<String, List<Map<String, String>>> map = Maps.newHashMap();
            map.put("合并数据", list);
            String url = ExcelsUtil.createExcel(title, map, new int[]{0});
            return ResponseResult.success(url);
        } else {
            return null;
        }
    }

    /**
     * 取消折扣活动的函数。
     * 该函数主要用于将指定折扣活动及其关联的商品状态更新为失效状态。
     * 它首先根据折扣ID查询所有关联的商品的通用ID列表，然后检查是否有其他活动正在使用这些通用ID中的任何一个。
     * 如果没有其他活动在使用这些通用ID，那么将这些通用ID对应的商品状态更新为失效。
     * 最后，将折扣活动本身的状态也更新为失效。
     *
     * @param param 一个包含折扣ID的Map参数。
     * @return 返回一个表示操作成功的ResponseResult对象。
     */
    @Override
    public ResponseResult inactiveDiscountBase(Map<String, Object> param) {
        // 根据折扣ID查询所有关联的商品的通用ID列表
        List<Integer> commonIdList = this.baseMapper.selectList(new LambdaQueryWrapper<ActivityDiscountGoodsEntity>()
                .eq(ActivityDiscountGoodsEntity::getDiscountId, param.get("discountId").toString()))
                .stream().map(ActivityDiscountGoodsEntity::getCommonId).collect(Collectors.toList());

        // 检查是否有其他活动正在使用这些通用ID
        if (!CollectionUtils.isEmpty(commonIdList)) {
            // 获取其他正在进行的活动使用的通用ID列表
            List<Integer> activeCommonIdList = this.baseMapper.selectActiveDiscountGoods(Integer.valueOf(param.get("discountId").toString()),
                    commonIdList.get(0));

            // 如果没有其他活动在使用这些通用ID，将这些商品状态更新为失效
            if (CollectionUtils.isEmpty(activeCommonIdList)) {
                try {
                    goodsServiceClient.updateCommonIsXian2(commonIdList);
                } catch (Exception ignored) {
                    // mall 路径可能无旧 common_is_xian 字段，不阻断活动失效
                }
            }
        }

        // 将所有关联的商品状态更新为失效
        this.baseMapper.update(null, new LambdaUpdateWrapper<ActivityDiscountGoodsEntity>()
                .set(ActivityDiscountGoodsEntity::getDiscountGoodsState, 3)
                .eq(ActivityDiscountGoodsEntity::getDiscountId, Integer.valueOf(param.get("discountId").toString())));

        // 将折扣活动状态更新为失效
        activityDiscountBaseMapper.update(null, new LambdaUpdateWrapper<ActivityDiscountBaseEntity>()
                .set(ActivityDiscountBaseEntity::getDiscountState, 3)
                .eq(ActivityDiscountBaseEntity::getDiscountId, Integer.valueOf(param.get("discountId").toString())));

        // 返回表示操作成功的ResponseResult对象
        return ResponseResult.success();
    }


}

