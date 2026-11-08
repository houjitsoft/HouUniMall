package com.houjiang.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.activity.dto.PromotionIndexQueryParamDTO;
import com.houjiang.activity.dto.SeckillGoodsUpdateDTO;
import com.houjiang.activity.dto.SecondKillGoodsDTO;
import com.houjiang.activity.entity.ActivitySeckillBaseEntity;
import com.houjiang.activity.entity.ActivitySeckillGoodsEntity;
import com.houjiang.activity.feign.GoodsServiceClient;
import com.houjiang.activity.feign.OrderServiceClient;
import com.houjiang.activity.mapper.ActivitySeckillBaseMapper;
import com.houjiang.activity.mapper.ActivitySeckillGoodsMapper;
import com.houjiang.activity.service.ActivitySeckillGoodsService;
import com.houjiang.activity.vo.*;
import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.dto.goods.AppSeckillGoodsVO;
import com.houjiang.commons.dto.goods.SeckillGoodsDTO;
import com.houjiang.commons.dto.goods.SeckillGoodsListDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.activity.AppSeckillGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.ActivityCombinationGoodsVO;
import com.houjiang.commons.vo.activity.CheckSeckillLimitResultVO;
import com.houjiang.commons.vo.activity.ModularGoodsVO;
import com.houjiang.commons.vo.goods.SeckillGoodsInfoVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.houjiang.commons.dto.goods.DiscountGoodsListVO;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ActivitySeckillGoodsServiceImpl
 * @Description 限时秒杀商品表
 * @Author Amos
 * @Date 2023-05-25 13:58:28
 * @Version 1.0
 * @author Amos
 */
@Service
public class ActivitySeckillGoodsServiceImpl extends ServiceImpl<ActivitySeckillGoodsMapper, ActivitySeckillGoodsEntity>
        implements ActivitySeckillGoodsService {

    private static final Logger log = LoggerFactory.getLogger(ActivitySeckillGoodsServiceImpl.class);

    @Autowired
    private ActivitySeckillBaseMapper activitySeckillBaseMapper;

    @Autowired
    private GoodsServiceClient goodsServiceClient;

    @Autowired
    private OrderServiceClient orderServiceClient;
    /**
     * 根据商品ID查询秒杀商品信息。
     *
     * @param map 查询参数的映射集合，包含了商品ID等关键查询条件。
     * @return 返回秒杀商品的查询结果，包含了商品的详细信息以及秒杀活动的相关数据。
     */
    /**
     * 登录态校验（替代 UserService.judgePermissions）。
     *
     * <p>老 {@code RedisUtil} 是独立 Jedis 连接、按老部署的本机配置文件取参，
     * 在容器化宿主里连不上 → judgePermissions 恒 false → 插件写接口一律 31005。
     * 这里统一改走宿主 RedisCache（与 {@link ParseTokenUtils} 同源），
     * 只保留「登录态有效」这一层语义；店铺维度过滤由各业务原有的 shopId 逻辑负责。
     */
    private static UserPermissionsResult loginPermissions(String token) {
        UserPermissionsResult result = new UserPermissionsResult();
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        if (userInfo != null) {
            result.setFlag(true);
            result.setUserId(userInfo.getUserId());
            result.setShopId(userInfo.getShopId());
            result.setShopType(userInfo.getShopType());
            result.setUserName(userInfo.getUserName());
            result.setUserMobile(userInfo.getUserMobile());
            result.setUserNickname(userInfo.getUserNickname());
        }
        return result;
    }

    @Override
    public AppSeckillGoodsQueryResultVO selectSeckillByGoodsId(Map<String, Object> map) {
        // 调用BaseMapper的selectSeckillByGoodsId方法，根据传入的映射集合查询秒杀商品信息
        return this.baseMapper.selectSeckillByGoodsId(map);
    }

    /**
     * 根据商品ID查询秒杀商品的信息。
     *
     * 本方法通过调用BaseMapper的selectSeckillByGoodsId2方法，实现了根据商品ID获取秒杀商品详情的功能。
     * 这是对BaseMapper中相应方法的重写，旨在提供一个更具体、更明确的使用场景接口。
     *
     * @param goodsId 商品的唯一标识ID。是查询秒杀商品信息的关键参数。
     * @return 返回AppSeckillGoodsQueryResultVO类型的对象，包含了查询到的秒杀商品的详细信息。
     */
    @Override
    public AppSeckillGoodsQueryResultVO selectSeckillByGoodsId2(Integer goodsId) {
        return this.baseMapper.selectSeckillByGoodsId2(goodsId);
    }

    /**
     * 根据商品ID查询秒杀商品的详细信息。
     *
     * @param goodsId 商品ID，作为查询条件。
     * @return 返回匹配的商品详细信息，包含在AppSeckillGoodsVO对象中。
     */
    @Override
    public AppSeckillGoodsVO selectByGoodsId(Integer goodsId) {
        // 调用BaseMapper的selectByGoodsId方法，根据goodsId查询秒杀商品信息
        return this.baseMapper.selectByGoodsId(goodsId);
    }

    /**
     * 检查秒杀活动的购买限制。
     * 通过传入的参数检查当前用户是否可以参与秒杀，以及是否超过了购买限制。
     *
     * @param param 包含秒杀商品ID、用户令牌等信息的参数映射。
     * @return 返回检查结果，包括是否可以参与秒杀、购买限制等信息。
     */
    @Override
    public ResponseResult seckillCheckLimit(Map<String, Object> param) {
        // 初始化返回结果对象
        ResponseResult rr = new ResponseResult();
        // 默认用户ID为空
        String userId = null;

        // 检查参数中是否包含token，如果包含，则尝试获取用户信息
        if(null != param.get("token")){
            String token = (String) param.get("token");
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(token);
            // 如果用户信息有效，则获取用户ID
            if (null != userInfo) {
                userId = userInfo.getUserId();
            }
        }

        // 从参数中获取商品ID和通用ID
        Integer goodsId = (Integer) param.get("goodsId");
        Integer commonId = (Integer) param.get("commonId");

        // 通过商品ID等信息查询秒杀活动的详细信息
        CheckSeckillLimitVO data = this.baseMapper.querySeckillInfo(param);

        // 默认购买数量为null
        Integer count = null;

        // 如果秒杀活动信息有效
        if(null != data){
            // 检查商品是否有限购，如果不限购，则直接返回限购信息
            Integer goodsUpperLimit = data.getGoodsUpperLimit();
            if( 0 == goodsUpperLimit){
                rr.setCode(1);
                rr.setData(null);
                rr.setMessage("该商品不限购");
                return rr;
            }

            // 更新查询参数中的商品开始时间、结束时间和用户ID等信息
            param.put("goodsStartTime",data.getGoodsStartTime());
            param.put("goodsEndTime",data.getGoodsEndTime());
            param.put("goodsId", goodsId);
            param.put("commonId", commonId);
            param.put("userId", userId);
            param.put("seckillId", data.getSeckillId());

            // 如果用户ID有效，则查询用户已购买的数量
            if(null != userId){
                count = this.baseMapper.queryOrderGoodsCount(param);
            }

            // 如果购买数量为null，则设置为0
            count = count==null?0:count;
            // 更新秒杀活动信息中的购买数量
            data.setBuyNum(count);

            // 根据购买数量和限购数量判断是否可以继续秒杀
            if(count >= goodsUpperLimit){
                rr.setCode(2);
                rr.setData(count);
                rr.setMessage("该商品已达购买上限,继续购买将以原价进行购买，是否继续");
            }else{
                rr.setCode(1);
                rr.setData(count);
                rr.setMessage("未达上限，可以购买");
            }
        }else{
            // 如果秒杀活动信息无效，则表示活动未开始或已结束，返回相应信息
            rr.setCode(2);
            rr.setMessage("活动未开始或活动已结束，需原价购买");
        }

        // 返回检查结果
        return rr;
    }

    /**
     * 根据商品ID查询秒杀活动信息。
     *
     * 本方法通过调用BaseMapper的querySeckillInfoByGoodsId方法，根据商品ID查询相应的秒杀活动信息。
     * 主要用于在秒杀活动中获取特定商品的秒杀详情，以便进行后续的秒杀逻辑处理。
     *
     * @param goodsId 商品ID，作为查询秒杀活动信息的依据。
     * @return CheckSeckillLimitResultVO 返回包含秒杀活动信息的结果对象。
     */
    @Override
    public CheckSeckillLimitResultVO querySeckillInfoByGoodsId(Integer goodsId) {
        return this.baseMapper.querySeckillInfoByGoodsId(goodsId);
    }

    /**
     * 查询秒杀活动的数量。
     *
     * 通过Map参数查询秒杀活动的数量。此方法用于统计当前系统中的秒杀活动总量，
     * 或根据特定条件（如活动状态、时间范围等）进行秒杀活动数量的查询。
     *
     * @param paramMap 一个Map对象，包含查询秒杀活动数量所需的条件。键值对的形式提供了灵活的查询方式。
     *                 可以通过此参数传递各种条件，以动态地调整查询结果。
     * @return 返回秒杀活动的数量。返回值为Integer类型，如果系统中没有秒杀活动，则返回0。
     */
    @Override
    public Integer querySeckillNum(Map<String, Object> paramMap) {
        return this.baseMapper.querySeckillNum(paramMap);
    }
    /**
     * 更新秒杀活动的销售信息。
     * <p>
     * 通过调用基础Mapper层的updateSaleForSeckill方法，使用传入的参数Map来更新秒杀活动的销售状况。
     * 此方法主要用于在秒杀活动进行中，对活动的销售数据进行实时更新，以保持数据的同步和准确性。
     * </p>
     * @param paramMap 一个包含更新秒杀活动销售信息所需参数的Map。具体的参数及其含义应在方法的实现中详细说明。
     * @return 返回一个表示操作成功的结果对象。使用ResponseResult.success()来构建表示成功的响应结果。
     */
    @Override
    public ResponseResult updateSaleForSeckill(Map<String, Object> paramMap) {
        this.baseMapper.updateSaleForSeckill(paramMap);
        return ResponseResult.success();
    }
    /**
     * 查询秒杀首页信息
     *
     * @param param 查询参数，包含商品开始时间等信息
     * @return 返回秒杀首页结果，包含商品列表、总数量等
     */
    @Override
    public ResponseResult querySeckillIndex(PromotionIndexQueryParamDTO param) {
        SeckillIndexResultVO result = new SeckillIndexResultVO();

        try {
            Date today = new Date();
            // 获取商品开始时间
            Date goodsStartTime = param.getGoodsStartTime();
            Date goodsStartTime2 = goodsStartTime;
            List<SeckillGoodsListQueryResultVO> promotionGoodsList = new ArrayList<>();
            Integer totalCount = 0;
            Long actOverplusTime;

            // 如果当前时间早于商品开始时间
            // 活动还未开始
            if(today.before(goodsStartTime)){
                // 处理未开始的活动逻辑
                // 将对象转换为map
                Map data = ParseMapUtils.beanToMap(param);
                // 分页查询数据
                IPage<SeckillGoodsListQueryResultVO> page = this.baseMapper.selectSeckillIndexGoods(new Query<SeckillGoodsListQueryResultVO>().getPage(data),param);
                promotionGoodsList = page.getRecords();
                totalCount = PageUtils.getPage(page).getTotalCount();
                actOverplusTime = DateUtils.getSecondByMinusDate(new Date(),goodsStartTime);
                result.setActState(0);
                // 活动已开始
            }else{
                // 处理已开始的活动逻辑
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                cal.setTime(today);
                // 调整时间为演示站数据的时间
                cal.add(Calendar.YEAR, -24);
                goodsStartTime = cal.getTime();
                param.setGoodsStartTime(goodsStartTime);
                // 将对象转换为map
                Map data = ParseMapUtils.beanToMap(param);
                // 分页查询数据
                IPage<SeckillGoodsListQueryResultVO> page = this.baseMapper.selectSeckillIndexGoods2(new Query<SeckillGoodsListQueryResultVO>().getPage(data),param);
                promotionGoodsList = page.getRecords();
                totalCount = PageUtils.getPage(page).getTotalCount();

                Date goodsEndTime = new Date();
                cal.setTime(goodsStartTime2);
                // 根据商品开始时间调整结束时间
                if(sdf.format(goodsStartTime2).contains("00:00:00")){
                    cal.add(Calendar.HOUR, 8);
                }else {
                    cal.add(Calendar.HOUR, 4);
                }
                goodsEndTime = cal.getTime();
                actOverplusTime = DateUtils.getSecondByMinusDate(new Date(),goodsEndTime );
                result.setActState(1);
            }

            // 给商品列表设置剩余时间等信息
            if (!CollectionUtils.isEmpty(promotionGoodsList)) {
                for (SeckillGoodsListQueryResultVO promotionGoods : promotionGoodsList) {
                    // 剩余时间
                    Long overplusTime = DateUtils.getSecondByMinusDate(new Date(), promotionGoods.getGoodsEndTime());
                    // 判断活动是否开始
                    // 判断是否开始
                    if (DateUtils.compareDate2(promotionGoods.getGoodsStartTime())) {
                        overplusTime = DateUtils.getSecondByMinusDate(new Date(), promotionGoods.getGoodsStartTime());
                        promotionGoods.setActivityStartStatus(0);
                    } else {
                        promotionGoods.setActivityStartStatus(1);
                    }
                    promotionGoods.setOverplusTime(overplusTime);

                    // 根据token获取用户信息，设置购买数量
                    String userId = null;
                    if(null != param.getToken()){
                        String token = param.getToken();
                        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(token);
                        if (null != userInfo) {
                            userId = userInfo.getUserId();
                        }
                    }
                    Map<String, Object> map = new HashMap<>();
                    Integer goodsId = promotionGoods.getGoodsId();
                    Integer commonId = promotionGoods.getCommonId();
                    map.put("goodsId", goodsId);
                    map.put("commonId", commonId);
                    map.put("userId", userId);
                    CheckSeckillLimitVO data = this.baseMapper.querySeckillInfo(map);
                    Integer count = 0;
                    if (null != data) {
                        map.put("goodsStartTime", data.getGoodsStartTime());
                        map.put("goodsEndTime", data.getGoodsEndTime());
                        if(null != userId){
                            count = this.baseMapper.queryOrderGoodsCount(map);
                        }
                    }
                    promotionGoods.setBuyNum(count);
                }
            } else {
                promotionGoodsList = Lists.newArrayList();
            }

            // 设置返回结果的总数量、商品列表、活动剩余时间
            result.setTotalCount(totalCount);
            result.setPromotionGoodsList(promotionGoodsList);
            result.setActOverplusTime(actOverplusTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 返回处理结果
        return ResponseResult.success(result);
    }

    /**
     * 检查秒杀活动的限制条件。
     * 通过传入的参数检查用户是否可以参与当前秒杀活动，包括检查商品库存和用户购买限制。
     *
     * @param param 包含秒杀商品ID、通用商品ID、用户令牌等信息的参数映射。
     * @return 返回检查结果，包括是否可以参与秒杀和当前秒杀活动的商品信息。
     */
    @Override
    public ResponseResult checkSeckillLimit(Map<String, Object> param) {
        // 初始化用户ID为空，用于后续判断用户是否登录
        String userId = null;
        // 通过token尝试获取用户信息
        if (null != param.get("token")) {
            String token = (String) param.get("token");
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(token);
            // 如果用户信息有效，提取用户ID
            if (null != userInfo) {
                userId = userInfo.getUserId();
            }
        }
        // 从参数中提取商品ID和通用商品ID
        Integer goodsId = (Integer) param.get("goodsId");
        Integer commonId = (Integer) param.get("commonId");
        // 查询秒杀活动的详细信息
        CheckSeckillLimitVO data = this.baseMapper.querySeckillInfo(param);
        // 初始化商品库存计数为null，用于后续判断商品是否有库存
        Integer count = null;
        // 如果秒杀活动信息有效，更新参数映射以包含秒杀活动的时间和ID，并根据用户ID查询已购买的商品数量
        if (null != data) {
            param.put("goodsStartTime", data.getGoodsStartTime());
            param.put("goodsEndTime", data.getGoodsEndTime());
            param.put("goodsId", goodsId);
            param.put("commonId", commonId);
            param.put("userId", userId);
            param.put("seckillId", data.getSeckillId());
            // 如果用户已登录，查询用户已购买的秒杀商品数量
            if (null != userId) {
                count = this.baseMapper.queryOrderGoodsCount(param);
            }
            // 如果商品库存计数为null，则默认为0，表示没有已购买的商品
            count = count == null ? 0 : count;
            // 设置秒杀活动的购买数量限制信息
            data.setBuyNum(count);
            // 返回成功响应，包括秒杀活动信息
            return ResponseResult.success(BizCodeEnume.ACTIVITY_NO_PURCHASE_LIMIT.getMsg(), data);
        } else {
            // 如果秒杀活动信息无效，返回失败响应，表示秒杀活动已结束
            return ResponseResult.fail(BizCodeEnume.ACTIVITY_ACTIVITY_ENDED.getCode(), BizCodeEnume.ACTIVITY_ACTIVITY_ENDED.getMsg());
        }
    }
    /**
     * 取消秒杀活动的商品状态和活动状态。
     *
     * @param paramMap 参数映射表，包含秒杀活动ID。
     * @return 返回操作结果，通常表示操作成功。
     *
     * 该方法首先根据秒杀活动ID查询关联的商品列表，收集这些商品的通用ID列表。
     * 如果通用ID列表不为空，则将这些商品的状态更新为下架。
     * 接着，更新秒杀活动的商品状态为已结束。
     * 最后，更新秒杀活动的状态为已结束。
     * 方法的目的是在后台管理系统中，通过一个操作批量结束秒杀活动和关联的商品。
     */
    @Override
    public ResponseResult inactiveShopSeckillBase(Map<String, Object> paramMap) {

        // 根据秒杀活动ID查询关联的商品ID列表
        List<Integer> commonIdList = this.baseMapper.selectList(new LambdaQueryWrapper<ActivitySeckillGoodsEntity>()
                                    .eq(ActivitySeckillGoodsEntity::getSeckillId,Integer.valueOf(paramMap.get("seckillId").toString())))
                                    .stream().map(ActivitySeckillGoodsEntity::getCommonId).collect(Collectors.toList());

        // 如果商品ID列表不为空，更新这些商品的状态为下架
        if (!CollectionUtils.isEmpty(commonIdList)) {
            goodsServiceClient.updateCommonIsXian2(commonIdList);
        }
        // 更新秒杀活动中的所有商品状态为已结束
        this.baseMapper.update(null,new LambdaUpdateWrapper<ActivitySeckillGoodsEntity>()
                       .set(ActivitySeckillGoodsEntity::getSeckillGoodsState,3)
                       .eq(ActivitySeckillGoodsEntity::getSeckillId,Integer.valueOf(paramMap.get("seckillId").toString())));

        // 更新秒杀活动状态为已结束
        activitySeckillBaseMapper.update(null,new LambdaUpdateWrapper<ActivitySeckillBaseEntity>()
                .set(ActivitySeckillBaseEntity::getSeckillState,3)
                .eq(ActivitySeckillBaseEntity::getSeckillId,Integer.valueOf(paramMap.get("seckillId").toString())));

        // 返回操作成功的结果
        return ResponseResult.success();
    }


    /**
     * 查询秒杀商品的价格
     *
     * @param goodsId 商品ID，用于指定查询哪个商品的价格
     * @return 返回包含秒杀商品价格的ResponseResult对象
     *
     * 本方法通过调用baseMapper的seckillGoodsPrice方法查询指定商品ID的秒杀商品价格，
     * 并将查询结果封装在ResponseResult对象中返回。
     * 如果查询成功，ResponseResult的状态码为200，数据部分为查询到的ModularGoodsVO对象。
     */
    @Override
    public ResponseResult seckillGoodsPrice(Integer goodsId) {
        // 通过商品ID查询秒杀商品的价格信息
        ModularGoodsVO modularGoodsVO = this.baseMapper.seckillGoodsPrice(goodsId);
        // 返回查询结果，如果查询成功，则状态码为200，数据为查询到的ModularGoodsVO对象
        return ResponseResult.success(modularGoodsVO);
    }

    /**
     * 查询秒杀商品信息
     *
     * @param param 秒杀商品查询参数DTO，包含秒杀ID等信息
     * @return 返回查询结果，包含分页信息和秒杀商品列表
     * @throws IllegalAccessException 如果访问参数字段失败，抛出此异常
     */
    @Override
    public ResponseResult querySecondKillGoods(SecondKillGoodsDTO param) throws IllegalAccessException {
        // 将查询参数DTO转换为Map对象，便于后续处理
        Map data = ParseMapUtils.beanToMap(param);

        // 根据秒杀ID查询秒杀商品，实现分页查询
        IPage<ActivitySeckillGoodsEntity> page = this.page(
                new Query<ActivitySeckillGoodsEntity>().getPage(data),
                new LambdaQueryWrapper<ActivitySeckillGoodsEntity>().eq(ActivitySeckillGoodsEntity::getSeckillId, param.getSeckillId())
        );

        // 返回查询结果，包含分页信息和秒杀商品列表
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 查询秒杀商品的差异信息。
     * 根据输入的查询参数，首先尝试查询符合参数条件的秒杀商品列表。如果列表为空或商品数量少于20个，
     * 则会查询热门秒杀商品以补充列表，确保最终返回的秒杀商品列表至少有20个商品。
     *
     * @param param 查询参数，包含秒杀商品的查询条件。
     * @return 返回包含秒杀商品信息的响应结果。
     */
    @Override
    public ResponseResult querySeckillDiffer(PromotionIndexQueryParamDTO param) {
        // 根据查询参数查询秒杀商品列表
        List<SeckillGoodsListQueryResultVO> seckillGoodsList = this.baseMapper.querySeckillDiffer(param);

        // 初始化日期格式和获取当前时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Date endTime = calendar.getTime();
        // 设置查询的开始时间为当前时间的一年前
        calendar.add(Calendar.YEAR, -1);
        Date startTime = calendar.getTime();

        // 格式化开始时间和结束时间
        String endDateTime = sdf.format(endTime);
        String startDateTime = sdf.format(startTime);

        // 创建包含开始时间和结束时间的查询参数Map
        Map map = new HashMap<>();
        map.put("endDateTime", endDateTime);
        map.put("startDateTime", startDateTime);

        // 如果秒杀商品列表为空，则查询热门秒杀商品并添加到列表中
        if (seckillGoodsList == null) {
            // 按照热度查询秒杀商品
            // 按商品销量查询20个
            List<Integer> integers = this.baseMapper.querySeckillGoodsHot(map);
            Map map1 = new HashMap<>();
            map1.put("goodsList", integers);
            // 如果有指定的店铺ID，则添加到查询参数中
            if (null != param.getShopId()) {
                map1.put("shopId", param.getShopId());
            }
            // 如果热门秒杀商品列表不为空，则查询对应的秒杀商品详情并添加到列表中
            if (integers != null && integers.size() > 0) {
                List<SeckillGoodsListQueryResultVO> queryResults = this.baseMapper.querySeckillDifferHot(map1);
                seckillGoodsList.addAll(queryResults);
            }
        // 如果秒杀商品列表不为空但商品数量少于20个，则补充不足20个的商品
        } else if (seckillGoodsList.size() < 20) {
            // 查询热门秒杀商品
            List<Integer> integers = this.baseMapper.querySeckillGoodsHot(map);
            Map map1 = new HashMap<>();
            map1.put("goodsList", integers);
            // 如果有指定的店铺ID，则添加到查询参数中
            if (null != param.getShopId()) {
                map1.put("shopId", param.getShopId());
            }
            // 如果热门秒杀商品列表不为空，则查询对应的秒杀商品详情，并添加到列表中
            if (integers != null && integers.size() > 0) {
                List<SeckillGoodsListQueryResultVO> queryResults = this.baseMapper.querySeckillDifferHot(map1);
                for (SeckillGoodsListQueryResultVO queryResult : queryResults) {
                    // 确保新增的秒杀商品不在当前列表中
                    boolean contains = seckillGoodsList.contains(queryResult);
                    if (!contains) {
                        seckillGoodsList.add(queryResult);
                    }
                }
            }
            // 如果最终列表商品数量超过20个，则只保留前20个
            if (seckillGoodsList.size() > 20) {
                seckillGoodsList.subList(0, 20);
            }
        }
        // 返回查询结果
        return ResponseResult.success(seckillGoodsList);
    }

    /**
     * 查询秒杀基础数据导出信息。
     * @param param 查询参数映射表。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult queryDiscountBaseDataExp(Map<String, Object> param) {
        // 定义表格标题
        String[] title = {"商品名称", "规格", "付款商品件数", "总成交额", "新客户下单数", "老客户下单数"};
        // 初始化数据列表
        List<Map<String, String>> list = Lists.newArrayList();

        // 初始化秒杀数据列表和各种统计量
        List<SeckillDataVO> dataList = new ArrayList<>();
        BigDecimal allDealAmount = BigDecimal.ZERO;

        // 查询秒杀商品信息
        IPage<AppSeckillGoodsQueryResultVO> page = this.baseMapper.querySeckillGoodsInfo(
                new Query<AppSeckillGoodsQueryResultVO>().getPage(param), param);

        // 获取当前日期作为查询的开始时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Date startTime = calendar.getTime();
        // 设置查询的前一天作为结束时间
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // 如果查询结果不为空
        if (null != page && page.getRecords().size() > 0) {
            for (AppSeckillGoodsQueryResultVO seckillGoodsListResult : page.getRecords()) {
                // 处理商品规格名称
                seckillGoodsListResult.setGoodsSpec(seckillGoodsListResult.getSpecValueName());
                // 初始化新老客户订单数量映射
                Map<Integer, Integer> newerNumMap = new HashMap<>();
                Map<Integer, Integer> olderNumMap = new HashMap<>();

                // 初始化秒杀商品数据对象
                Integer goodsId = seckillGoodsListResult.getGoodsId();
                SeckillDataVO data = new SeckillDataVO();
                data.setGoodsName(seckillGoodsListResult.getGoodsName());
                data.setSpecName(seckillGoodsListResult.getGoodsSpec());

                // 调整秒杀活动的时间范围
                if (calendar.getTime().before(seckillGoodsListResult.getStartDateTime())) {
                    seckillGoodsListResult.setEndDateTime(seckillGoodsListResult.getStartDateTime());
                } else {
                    seckillGoodsListResult.setEndDateTime(calendar.getTime());
                }
                seckillGoodsListResult.setStartDateTime(startTime);

                // 查询秒杀商品的详细信息
                SeckillGoodsInfoVO discountGoodsInfo = orderServiceClient.querySeckillGoodsInfo2(seckillGoodsListResult);
                if (null != discountGoodsInfo) {
                    data.setDealAmount(discountGoodsInfo.getDealAmount());
                    data.setGoodsNum(discountGoodsInfo.getGoodsNum());
                    allDealAmount = allDealAmount.add(discountGoodsInfo.getDealAmount());
                } else {
                    data.setDealAmount(BigDecimal.ZERO);
                    data.setGoodsNum(0);
                }
                DiscountGoodsListVO discountGoodsListResult = new DiscountGoodsListVO();
                discountGoodsListResult.setGoodsId(seckillGoodsListResult.getGoodsId());
                discountGoodsListResult.setGoodsPrice(seckillGoodsListResult.getSeckillPrice());
                discountGoodsListResult.setStartDateTime(seckillGoodsListResult.getStartDateTime());
                discountGoodsListResult.setEndDateTime(seckillGoodsListResult.getEndDateTime());
                // 查询购买过该秒杀商品的用户列表
                List<String> buyerUserIds = orderServiceClient.queryBuyerUsers(discountGoodsListResult);
                for (String userId : buyerUserIds) {
                    // 统计新老客户订单数量
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("userId", userId);
                    paramMap.put("startDateTime", seckillGoodsListResult.getStartDateTime());
                    paramMap.put("shopId", seckillGoodsListResult.getShopId());
                    paramMap.put("endDateTime", seckillGoodsListResult.getEndDateTime());
                    paramMap.put("goodsId", seckillGoodsListResult.getGoodsId());
                    paramMap.put("seckillPrice", seckillGoodsListResult.getSeckillPrice());

                    Integer olderCount = orderServiceClient.queryCountForOlder(paramMap);
                    paramMap.put("discountPrice",seckillGoodsListResult.getSeckillPrice());
                    Integer orderCount = orderServiceClient.queryUserOrderCount(paramMap);
                    if (null != olderCount) {
                        // 根据是否是新客户，更新订单数量统计
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



                // 更新秒杀商品数据对象的新老客户订单数量
                data.setNewerOrderNum(null != newerNumMap.get(goodsId) ? newerNumMap.get(goodsId) : 0);
                data.setOlderOrderNum(null != olderNumMap.get(goodsId) ? olderNumMap.get(goodsId) : 0);

                dataList.add(data);
            }

            // 将秒杀商品数据转换为表格数据格式
            for (SeckillDataVO data : dataList) {
                HashMap<String, String> map = com.google.common.collect.Maps.newHashMap();
                map.put("商品名称", data.getGoodsName());
                map.put("规格", data.getSpecName());
                map.put("付款商品件数", data.getGoodsNum() + "");
                map.put("总成交额", data.getDealAmount() + "");
                map.put("新客户下单数", data.getNewerOrderNum() + "");
                map.put("老客户下单数", data.getOlderOrderNum() + "");

                list.add(map);
            }

            // 构建包含表格数据的映射表
            Map<String, List<Map<String, String>>> map = Maps.newHashMap();
            map.put("合并数据", list);
            // 生成并返回Excel文件的URL地址
            String url = ExcelsUtil.createExcel(title, map, new int[]{0});
            return ResponseResult.success(url);
        } else {
            // 如果查询结果为空，返回失败响应
            return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED);
        }
    }
    /**
     * 查询秒杀基础数据
     *
     * @param param 查询参数，包含秒杀ID等信息
     * @return 返回秒杀基础数据的响应结果
     */
    @Override
    public ResponseResult querySeckillBaseData(Map<String, Object> param) {
        // 检查是否存在秒杀ID参数
        if (null != param.get("seckillId")) {
            // 初始化数据列表和统计变量
            List<SeckillDataVO> dataList = new ArrayList<>();
            Integer allGoodsCount = 0;
            BigDecimal allDealAmount = BigDecimal.ZERO;
            Integer allNewerOrderNum = 0;
            Integer allOlderOrderNum = 0;

            // 查询秒杀商品信息
            IPage<AppSeckillGoodsQueryResultVO> page = this.baseMapper.querySeckillGoodsInfo(
                    new Query<AppSeckillGoodsQueryResultVO>().getPage(param), param);

            // 获取当前日期时间作为默认的开始时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            Date startTime = calendar.getTime();

            // 设置昨天的开始时间为秒杀的起始时间
            calendar.add(Calendar.DATE, -1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // 如果查询结果存在，则处理每个秒杀商品
            if (null != page && page.getRecords().size() > 0) {
                for (AppSeckillGoodsQueryResultVO appSeckillGoodsResult : page.getRecords()) {
                    // 初始化新老用户订单数量映射
                    Map<Integer, Integer> newerNumMap = new HashMap<>();
                    Map<Integer, Integer> olderNumMap = new HashMap<>();

                    // 处理商品基本信息
                    Integer goodsId = appSeckillGoodsResult.getGoodsId();
                    SeckillDataVO data = new SeckillDataVO();
                    data.setGoodsName(appSeckillGoodsResult.getGoodsName());
                    data.setSpecName(appSeckillGoodsResult.getSpecValueName());

                    // 调整秒杀时间范围
                    if (calendar.getTime().before(appSeckillGoodsResult.getStartDateTime())) {
                        appSeckillGoodsResult.setEndDateTime(appSeckillGoodsResult.getStartDateTime());
                    } else {
                        appSeckillGoodsResult.setEndDateTime(calendar.getTime());
                    }
                    appSeckillGoodsResult.setStartDateTime(startTime);

                    // 查询秒杀商品详细信息
                    SeckillGoodsInfoVO seckillGoodsInfo = orderServiceClient.querySeckillGoodsInfo2(appSeckillGoodsResult);
                    if (null != seckillGoodsInfo) {
                        data.setDealAmount(seckillGoodsInfo.getDealAmount());
                        data.setGoodsNum(seckillGoodsInfo.getGoodsNum());
                        allGoodsCount += seckillGoodsInfo.getGoodsNum();
                        allDealAmount = allDealAmount.add(seckillGoodsInfo.getDealAmount());
                    } else {
                        data.setDealAmount(BigDecimal.ZERO);
                        data.setGoodsNum(0);
                    }
                    DiscountGoodsListVO discountGoodsListResult = new DiscountGoodsListVO();
                    discountGoodsListResult.setGoodsId(appSeckillGoodsResult.getGoodsId());
                    discountGoodsListResult.setGoodsPrice(appSeckillGoodsResult.getSeckillPrice());
                    discountGoodsListResult.setStartDateTime(appSeckillGoodsResult.getStartDateTime());
                    discountGoodsListResult.setEndDateTime(appSeckillGoodsResult.getEndDateTime());
                    // 查询购买过该秒杀商品的用户ID列表
                    List<String> buyerUserIds = orderServiceClient.queryBuyerUsers(discountGoodsListResult);

                    // 统计新老用户订单数量
                    for (String userId : buyerUserIds) {
                        Map<String, Object> paramMap = new HashMap<>();
                        paramMap.put("userId", userId);
                        paramMap.put("startDateTime", appSeckillGoodsResult.getStartDateTime());
                        paramMap.put("shopId", appSeckillGoodsResult.getShopId());
                        paramMap.put("endDateTime", appSeckillGoodsResult.getEndDateTime());
                        paramMap.put("goodsId", appSeckillGoodsResult.getGoodsId());
                        paramMap.put("seckillPrice", appSeckillGoodsResult.getSeckillPrice());

                        // 查询老用户订单数量
                        Integer olderCount = orderServiceClient.queryCountForOlder(paramMap);
                        // 查询总订单数量
                        paramMap.put("discountPrice",paramMap.get("seckillPrice"));
                        Integer orderCount = orderServiceClient.queryUserOrderCount(paramMap);

                        // 根据是否是新用户，更新订单数量统计
                        if (null != olderCount) {
                            if (0 == olderCount) {
                                newerNumMap.put(goodsId, (null == newerNumMap.get(goodsId)) ? orderCount : newerNumMap.get(goodsId) + orderCount);
                                allNewerOrderNum += orderCount;
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



                    // 设置新老用户订单数量到秒杀数据VO
                    data.setNewerOrderNum((null != newerNumMap.get(goodsId)) ? newerNumMap.get(goodsId) : 0);
                    data.setOlderOrderNum((null != olderNumMap.get(goodsId)) ? olderNumMap.get(goodsId) : 0);

                    dataList.add(data);
                }

                // 组装最终的返回结果
                Map<String, Object> result = new HashMap<>();
                result.put("dataEntityList", dataList);
                result.put("count", page.getTotal());
                result.put("allDealAmount", allDealAmount);
                result.put("allNewerOrderNum", allNewerOrderNum);
                result.put("allOlderOrderNum", allOlderOrderNum);
                result.put("allGoodsCount", allGoodsCount);

                return ResponseResult.success(result);
            }

            // 如果没有查询到数据，返回未获取到信息的错误码
            return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED);
        } else {
            // 如果缺少秒杀ID参数，返回缺少参数的错误码
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
        }
    }
    /**
     * 查询秒杀商品规格列表。
     *
     * @param param 查询参数的映射表，必须包含"commonId"和"seckillId"两个参数。
     * @return 返回秒杀商品规格的查询结果，如果查询失败，则返回错误信息。
     */
    @Override
    public ResponseResult querySeckillGoodsSpecList(Map<String, Object> param) {
        // 检查参数中是否包含必要的id信息
        // seckillId 必填；commonId 可选——不传表示查该场次下已配置的全部规格明细
        if (null == param.get("seckillId")) {
            // 如果必要参数缺失，则返回缺失参数的错误信息
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
        }
        // 根据参数查询秒杀商品的详细信息
        List<AppSeckillGoodsQueryResultVO> discountGoodsList = this.baseMapper.selectSeckillGoodsDetail2(param);

        // 如果查询结果为空，则初始化一个空列表，以避免返回null
        if (CollectionUtils.isEmpty(discountGoodsList)) {
            discountGoodsList = Lists.newArrayList();
        }
        // 返回查询成功的标志和商品列表
        return ResponseResult.success(discountGoodsList);
    }

    /**
     * 删除店铺秒杀商品
     *
     * @param map 包含秒杀商品信息和其他必要信息的映射表
     * @return 删除操作的结果，成功或失败及原因
     *
     * 本方法首先判断当前用户是否有权限执行删除操作，
     * 然后根据传入的映射表中的信息，更新商品表中的秒杀状态，
     * 如果商品未参与其他正在进行的秒杀活动，则将秒杀状态设为非秒杀，
     * 最后删除秒杀商品的相关信息。
     */
    @Override
    public ResponseResult deleteShopSeckillGoods(Map<String, Object> map) {
        // 判断用户权限
        // 权限判断
        UserPermissionsResult userPermissionsResult = loginPermissions(map.get("token").toString());

        // 权限检查
        if (userPermissionsResult.getFlag()) {
            // 初始化非秒杀状态
            // 更新商品表秒杀字段
            // 2为 秒杀
            Integer commonIsXian = 0;

            // 检查商品是否参与其他秒杀活动
            if (null != map.get("commonId")) {
                // 获取其他正在进行的活动
                List<Integer> activeCommonIdList = this.baseMapper.selectActiveSeckillGoods(map.get("seckillId").toString(),
                        map.get("commonId").toString());
                // 如果商品未参与其他秒杀活动，则更新商品秒杀状态
                if (CollectionUtils.isEmpty(activeCommonIdList)) {
                    Map<String,Object> param2 = new HashMap<>();
                    param2.put("commonIsXian",commonIsXian);
                    param2.put("commonId",map.get("commonId").toString());
                    goodsServiceClient.updateCommonIsXian(param2);
                }
            }
            // 删除秒杀商品相关信息
            this.baseMapper.deleteSeckillGoods2(map);
        } else {
            // 返回权限不足的错误信息
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        // 返回操作成功的信息
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }
    /**
     * 查询店铺秒杀商品信息。
     *
     * @param param 请求参数，包含秒杀商品ID和用户令牌等信息。
     * @return 返回秒杀商品信息的响应结果。
     *         如果用户权限足够，返回秒杀商品的成功响应；
     *         如果用户权限不足，返回权限不足的失败响应。
     */
    @Override
    public ResponseResult queryShopSeckillGoods(Map<String, Object> param) {
        // 判断用户权限是否足够
        // 权限判断
        UserPermissionsResult userPermissionsResult = loginPermissions(param.get("token").toString());

        // 如果用户权限足够
        if (userPermissionsResult.getFlag()) {
            // 根据秒杀商品ID查询秒杀商品信息
            ActivitySeckillGoodsEntity result = this.baseMapper.selectById(param.get("seckillGoodsId").toString());
            // 返回查询成功的响应结果
            return ResponseResult.success(result);
        } else {
            // 返回权限不足的失败响应
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
    }

    /**
     * 添加秒杀商品到店铺
     * @param param 包含秒杀活动ID、用户信息和秒杀商品详情的参数对象
     * @return 添加操作的结果，成功或失败及原因
     */
    @Override
    public ResponseResult addShopSeckillGoods(SeckillGoodsUpdateDTO param) {
        // 检查秒杀活动ID是否为空
        if (null == param.getSeckillId()) {
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
        }
        // 通过token获取用户信息
        // 权限判断
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());

        if (userInfo != null) {
            List<SeckillGoodsListDTO> seckillGoodsList = param.getSeckillGoodsList();
            // 平台超管不绑店铺，shop_id 置 0 表示全平台
            Integer shopId = null == userInfo.getShopId() || "".equals(userInfo.getShopId())
                    ? 0 : Integer.valueOf(userInfo.getShopId());

            Integer commonId = null;
            // 根据秒杀活动ID查询秒杀活动基础信息
            // 查询店铺活动详情
            ActivitySeckillBaseEntity seckillBase = activitySeckillBaseMapper.selectById(param.getSeckillId());
            for (SeckillGoodsListDTO seckillGoods : seckillGoodsList) {
                List<SeckillGoodsDTO> specLists = seckillGoods.getSpecLists();
                if(null != specLists && specLists.size() >0 ){
                    for (SeckillGoodsDTO specList : specLists) {
                        // 检查商品是否已存在于秒杀活动中
                        // 判断商品是否已存在 存在就不添加（跳过该规格，其余继续落库）
                        Integer count = this.baseMapper.goodsIsRepetition(param.getSeckillId(),specList.getGoodsId());
                        if(null != count && count >0 ){
                            continue;
                        }
                        // 秒杀价必填：空值会让 setSeckillPrice(...).doubleValue() 直接 NPE
                        if (null == specList.getSeckillPrice()) {
                            continue;
                        }
                        // 创建并设置秒杀商品信息
                        ActivitySeckillGoodsEntity addGoods = new ActivitySeckillGoodsEntity();
                        // 设置秒杀活动基础信息
                        // 设置店铺活动参数
                        addGoods.setSeckillId(seckillBase.getSeckillId());
                        addGoods.setSeckillName(seckillBase.getSeckillName());
                        addGoods.setGoodsStartTime(seckillBase.getSeckillStartTime());
                        addGoods.setGoodsEndTime(seckillBase.getSeckillEndTime());
                        addGoods.setSeckillGoodsState(Integer.valueOf(seckillBase.getSeckillState()));
                        addGoods.setGoodsUpperLimit(null == seckillGoods.getGoodsUpperLimit() ? 0 : seckillGoods.getGoodsUpperLimit());

                        commonId = seckillGoods.getCommonId();

                        // 商品名 / 原价 / 图片兜底：前端只传 goodsId + 秒杀价时从商品服务补，
                        // 否则 goods_name / goods_price / goods_image 三列 NOT NULL 直接报错。
                        ActivityCombinationGoodsVO goodsInfo = null;
                        if (null == specList.getGoodsPrice()
                                || !StringUtils.hasText(seckillGoods.getGoodsName())) {
                            try {
                                goodsInfo = goodsServiceClient.selectGoodsById(specList.getGoodsId());
                            } catch (Exception e) {
                                log.warn("秒杀商品回补商品信息失败: goodsId={}, {}", specList.getGoodsId(), e.getMessage());
                            }
                        }
                        String goodsName = StringUtils.hasText(seckillGoods.getGoodsName())
                                ? seckillGoods.getGoodsName()
                                : (null == goodsInfo ? "" : goodsInfo.getGoodsName());
                        if (null == goodsName) {
                            goodsName = "";
                        }
                        if (goodsName.length() > 100) {
                            goodsName = goodsName.substring(0, 100);
                        }
                        BigDecimal goodsPrice = specList.getGoodsPrice();
                        if (null == goodsPrice && null != goodsInfo) {
                            goodsPrice = goodsInfo.getGoodsPrice();
                        }
                        if (null == goodsPrice) {
                            goodsPrice = specList.getSeckillPrice();
                        }
                        addGoods.setGoodsId(specList.getGoodsId());
                        addGoods.setGoodsName(goodsName);
                        addGoods.setGoodsImage(null == seckillGoods.getGoodsImage() ? "" : seckillGoods.getGoodsImage());
                        addGoods.setGoodsPrice(goodsPrice.doubleValue());
                        addGoods.setSeckillNum(null == specList.getSeckillNum() ? 0 : specList.getSeckillNum());
                        addGoods.setCommonId(commonId);
                        addGoods.setShopId(shopId);
                        addGoods.setSeckillPrice(specList.getSeckillPrice().doubleValue());
                        // 根据秒杀商品ID判断是插入新商品还是更新已有商品
                        if (null == param.getSeckillGoodsId() || param.getSeckillGoodsId().equals("")) {
                            this.baseMapper.insert(addGoods);
                        } else {
                            addGoods.setSeckillGoodsId(param.getSeckillGoodsId());
                            this.baseMapper.updateById(addGoods);
                        }
                        // 更新商品的显示折扣状态
                        if (null != commonId) {
                            // 更新显示折扣字段
                            // 2为 秒杀
                            Integer commonIsXian = 2;
                            Map<String,Object> param2 = new HashMap<>();
                            param2.put("commonIsXian",commonIsXian);
                            param2.put("commonId",commonId.toString());
                            try {
                                goodsServiceClient.updateCommonIsXian(param2);
                            } catch (Exception e) {
                                // 商品活动标记失败不影响秒杀商品落库（列表以本表为准）
                                log.warn("更新商品秒杀标记失败: commonId={}, {}", commonId, e.getMessage());
                            }
                        }
                    }
                }
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 根据用户令牌查询秒杀商品列表。
     *
     * @param param 请求参数，包含分页信息和用户令牌等。
     * @return 返回秒杀商品列表的响应结果。
     */
    @Override
    public ResponseResult queryGoodsList(Map<String, Object> param) {
        // 尝试根据令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(String.valueOf(param.get("token")));

        if (userInfo != null) {
            // 如果用户信息存在，添加店铺ID到请求参数中，用于过滤商品
            // 分页
            // 平台超管不绑店铺：不加 shopId 条件即按全平台商品查询（空串会让 SQL 拼出 shop_id = '' 报错）
            if (StringUtils.hasText(userInfo.getShopId())) {
                param.put("shopId", Integer.valueOf(userInfo.getShopId()));
            } else {
                param.remove("shopId");
            }

            // 执行查询，获取秒杀商品的分页列表
            // 分页查询数据
            IPage<ShopSeckillGoodsListVO> page = this.baseMapper.selectShopSeckillGoodsList(new Query<ShopSeckillGoodsListVO>().getPage(param), param);

            // 返回查询结果
            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            // 如果用户信息不存在，返回权限不足的错误响应
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
    }

    /**
     * 查询某商品（commonId）下的全部规格，供后台按规格定价。
     */
    @Override
    public ResponseResult queryCommonGoodsSpecs(Map<String, Object> param) {
        if (null == param.get("commonId")) {
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
        }
        Integer commonId;
        try {
            commonId = Integer.valueOf(param.get("commonId").toString());
        } catch (NumberFormatException e) {
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
        }
        return ResponseResult.success(this.baseMapper.selectCommonGoodsSpecs(commonId));
    }
}

