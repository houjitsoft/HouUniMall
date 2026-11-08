package com.houjiang.activity.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.activity.dto.*;
import com.houjiang.activity.entity.ActivityDiscountBaseEntity;
import com.houjiang.activity.entity.ActivityDiscountGoodsEntity;
import com.houjiang.activity.feign.MerchantServiceClient;
import com.houjiang.activity.feign.GoodsServiceClient;
import com.houjiang.activity.feign.OrderServiceClient;
import com.houjiang.activity.mapper.ActivityDiscountBaseMapper;
import com.houjiang.activity.mapper.ActivityDiscountGoodsMapper;
import com.houjiang.activity.service.ActivityDiscountBaseService;
import com.houjiang.activity.vo.*;
import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.dto.goods.CommonIsXianUpdateDTO;
import com.houjiang.commons.dto.goods.DiscountGoodsListVO;
import com.houjiang.commons.dto.goods.ListPageQueryParamDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName ActivityDiscountBaseServiceImpl
 * @Description 限时折扣活动表
 * @Author Amos
 * @Date 2023-05-30 18:05:20
 * @Version 1.0
 * @author Amos
 */
@Service
public class ActivityDiscountBaseServiceImpl extends ServiceImpl<ActivityDiscountBaseMapper, ActivityDiscountBaseEntity>
        implements ActivityDiscountBaseService {

    @Resource
    private ActivityDiscountBaseMapper activityDiscountBaseMapper;

    @Autowired
    private MerchantServiceClient merchantServiceClient;

    @Autowired
    private GoodsServiceClient goodsServiceClient;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private RedisCache redisCache;

    @Resource
    private ActivityDiscountGoodsMapper activityDiscountGoodsMapper;


    /**
     * 根据传入的参数检查商品的折扣限制。
     * 通过解析token获取用户信息，查询商品的折扣信息和用户的购买记录，以判断当前用户是否还能享受折扣购买。
     *
     * @param param 包含token、goodsId、commonId等参数的映射。
     * @return 返回检查结果，包括是否能享受折扣、已购买数量等信息。
     */
    @Override
    public ResponseResult discountCheckLimit(Map<String, Object> param) {
        // 初始化返回结果对象
        ResponseResult rr = new ResponseResult();
        // 根据token构造Redis中的键名
        String redisKey = HouConfig.HOU_USER_TOKEN_USER + param.get("token");
        // 检查token是否存在于Redis中
        if (redisCache.hasKey(redisKey)) {
            // 通过token获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(String.valueOf(param.get("token")));
            // 获取用户ID
            String userId = userInfo.getUserId();
            // 从参数中获取商品ID和通用ID
            Integer goodsId = (Integer) param.get("goodsId");
            Integer commonId = (Integer) param.get("commonId");
            Integer goodsNum = param.get("goodsNum") == null ? 1 : (Integer) param.get("goodsNum");
            // 查询商品的折扣限制信息
            CheckDiscountLimitVO data = this.baseMapper.queryDiscountInfo(goodsId);
            // 初始化商品购买数量
            Integer count = null;
            // 如果折扣信息存在
            if (null != data) {
                // 获取商品的限购数量和折扣次数限制
                Integer goodsUpperLimit = data.getGoodsUpperLimit();
                Integer discountUpperTime = data.getDiscountUpperTime();
                // 如果商品不限购且不限制折扣次数，直接返回可以购买
                if (0 == goodsUpperLimit && 0 == discountUpperTime) {
                    rr.setCode(1);
                    rr.setData(null);
                    rr.setMessage("该商品不限购");
                    return rr;
                }
                // 计算折扣期限内的日期
                Calendar cal = Calendar.getInstance();
                Date fdate = new Date();
                cal.setTime(fdate);
                cal.add(Calendar.DATE, -discountUpperTime);
                fdate = cal.getTime();
                // 更新参数映射，添加日期、商品ID、通用ID、用户ID和折扣ID
                param.put("fDate", fdate);
                param.put("goodsId", goodsId);
                param.put("commonId", commonId);
                param.put("userId", userId);
                param.put("discountId", data.getDiscountId());
                param.put("goodsStartTime", DateUtils.getStartTime());
                param.put("goodsEndTime", DateUtils.getEndTime());
                // 查询用户在折扣期限内的购买数量
                count = orderServiceClient.queryOrderGoodsCount(param);
                // 如果购买数量为空，则设为0
                count = count == null ? 0 : count;
                // 如果购买数量达到限购数量，则不能享受折扣
                if (count + goodsNum > goodsUpperLimit) {
                    rr.setCode(2);
                    rr.setData(count);
                    rr.setMessage("该商品已达购买上限,需原价购买");
                } else {
                    // 可以享受折扣购买
                    rr.setCode(1);
                    rr.setData(count);
                    rr.setMessage("未达上限，可以购买");
                }
            } else {
                // 如果折扣信息不存在，表示活动未开始或已结束
                rr.setCode(2);
                rr.setMessage("活动未开始或活动已结束，需原价购买");
            }
        } else {
            // 如果token失效
            rr.setCode(3);
            rr.setMessage("token失效，请重新登录");
        }
        // 返回检查结果
        return rr;
    }

    /**
     * 查询所有折扣信息。
     *
     * @param param 分页查询参数，包含页码和每页数量等信息。
     * @return 返回查询结果，包含分页信息和数据列表。
     */
    @Override
    public ResponseResult queryAllZheKou(ListPageQueryParamDTO param) {
        IPage<Map<String,Object>> page = null;
        try {
            // 将查询参数对象转换为Map，以便于后续的查询操作。
            // 分页
            // 将对象转换为map
            Map data = ParseMapUtils.beanToMap(param);

            // 执行查询操作，获取所有折扣信息的分页结果。
            // 分页查询数据
            page = this.baseMapper.queryAllZheKou(new Query<Map<String,Object>>().getPage(data),param);
        } catch (IllegalAccessException e) {
            // 打印异常信息，但不中断程序执行。
            e.printStackTrace();
        }

        // 构建并返回查询结果，包含分页信息和数据列表。
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 根据权限和条件查询优惠基础信息列表。
     *
     * @param param 查询条件，包含分页信息和权限token等。
     * @return 返回查询结果，包含优惠基础信息列表和总条数。
     */
    @Override
    public ResponseResult queryDiscountBaseList(DiscountBaseListQueryDTO param) {
        // 初始化查询结果VO
        DiscountBaseListQueryVO result = new DiscountBaseListQueryVO();

        // 判断用户权限
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());

        // 如果用户具有查询权限
        if (userPermissionsResult.getFlag()) {
            // 设置查询条件中的店铺ID
            param.setShopId(userPermissionsResult.getShopId());
            // 计算分页查询的起始位置
            param.setPage((param.getPage() - 1) * param.getLimit());
            // 根据条件查询优惠基础信息列表
            List<DiscountBaseListVO> discountBaseList = activityDiscountBaseMapper.selectDiscountBaseList(param);
            // 查询符合条件的总条数
            Integer totalCount = activityDiscountBaseMapper.selectDiscountBaseListCount(param);
            // 设置查询结果的列表和总条数
            result.setDiscountBaseList(discountBaseList);
            result.setTotalCount(totalCount);
        } else {
            // 如果用户权限不足，返回失败结果
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }

        // 返回查询成功的结果
        return ResponseResult.success(result);
    }


    /**
     * 添加折扣基础信息，并关联折扣商品。
     * 使用全局事务确保操作的原子性。
     *
     * @param param 折扣添加参数，包含折扣基础信息和关联的商品信息。
     * @return 如果操作成功，返回新增的折扣ID；如果权限不足，返回错误信息。
     */
    @Override
    public ResponseResult addDiscountBase(DiscountBaseAddParamDTO param) {
        // 根据token获取用户信息，用于权限校验和设置折扣相关用户信息。
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(param.getToken());
        if (userInfoVO == null) {
            // 平台后台 token 走 getPlatformUser
            userInfoVO = ParseTokenUtils.getPlatformUser(param.getToken());
        }

        if (userInfoVO != null) {
            // 设置折扣所属店铺ID和名称（平台账号 shopId 为空时记 0=全平台）。
            String shopId = userInfoVO.getShopId();
            if (shopId == null || shopId.isBlank()) {
                shopId = "0";
            }
            param.setShopId(shopId);
            String shopName = "平台";
            try {
                if (!"0".equals(shopId)) {
                    String remote = merchantServiceClient.selectShopNameByShopId(shopId);
                    if (remote != null && !remote.isBlank()) {
                        shopName = remote;
                    }
                }
            } catch (Exception ignored) {
                // 店铺名仅展示用，查不到不阻断创建
            }
            param.setShopName(shopName);
            // 设置操作用户ID和昵称。
            param.setUserId(userInfoVO.getUserId());
            param.setUserNickName(userInfoVO.getUserNickname() == null ? "平台" : userInfoVO.getUserNickname());
            // 默认设置折扣小时和创建时间。
            param.setDiscountHour(1);
            param.setDiscountCreateTime(new Date());

            // 处理预热时间逻辑。
            // 预热时间
            if(null == param.getPreStartHour()){
                param.setPreStartHour(0);
            }
            Calendar cal = Calendar.getInstance();
            Date fdate = param.getDiscountStartTime();
            cal.setTime(fdate);
            cal.add(Calendar.HOUR,-param.getPreStartHour());
            fdate = cal.getTime();
            Date currentDate = new Date();
            // 根据当前时间和预热设置，确定折扣状态。
            // 有预热
            if(0 != param.getPreStartHour()) {
                param.setPreStartTime(fdate);
                if (currentDate.before(fdate)) {
                    param.setDiscountState(0);
                } else if (DateUtils.belongCalendar(currentDate, fdate, param.getDiscountStartTime()) && 0 != param.getPreStartHour()) {
                    param.setDiscountState(4);
                } else if (DateUtils.belongCalendar(currentDate, param.getDiscountStartTime(), param.getDiscountEndTime())) {
                    param.setDiscountState(1);
                } else {
                    param.setDiscountState(2);
                }
            //没有预热
            }else {
                if (currentDate.before(param.getDiscountStartTime())) {
                    param.setDiscountState(0);
                } else if (DateUtils.belongCalendar(currentDate, param.getDiscountStartTime(), param.getDiscountEndTime())) {
                    param.setDiscountState(1);
                } else {
                    param.setDiscountState(2);
                }
            }

            // 处理新增和更新折扣商品的逻辑。
            DiscountGoodsAddParamDTO goodsAddParam = new DiscountGoodsAddParamDTO();
            if (null == param.getDiscountId()) {
                // 新增折扣基础信息。
                activityDiscountBaseMapper.insertDiscountBase(param);
                // 新增折扣商品信息。
                goodsAddParam.setDiscountId(param.getDiscountId());
                goodsAddParam.setDiscountGoodsList(param.getDiscountGoodsList());
                goodsAddParam.setToken(param.getToken());
                addDiscountGoods(goodsAddParam);
                // 返回新增的折扣ID。
                return ResponseResult.success(param.getDiscountId());
            } else {
                // 更新折扣基础信息。
                CommonIsXianUpdateDTO commonIsXianUpdateParam = new CommonIsXianUpdateDTO();
                activityDiscountBaseMapper.updateDiscountBase(param);
                // 获取并更新原有折扣商品的状态。
                // 查询参与此限时折扣的商品
                List<Integer> commonList = activityDiscountGoodsMapper.selectDiscountGoods(param.getDiscountId());
                // 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
                commonIsXianUpdateParam.setCommonIdList(commonList);
                commonIsXianUpdateParam.setCommonIsXian(0);

                if (!CollectionUtils.isEmpty(commonList) && commonList.size() > 0) {
                    try {
                        goodsServiceClient.updateCommonIsXian3(commonIsXianUpdateParam);
                    } catch (Exception ignored) {
                        // mall 路径可能无旧 common_is_xian 字段，不阻断活动保存
                    }
                }
                // 删除原有折扣商品，新增更新后的折扣商品。
                activityDiscountGoodsMapper.deleteDiscountGoods(param.getDiscountId());
                // 插入限时折扣商品表
                goodsAddParam.setDiscountId(param.getDiscountId());
                goodsAddParam.setDiscountGoodsList(param.getDiscountGoodsList());
                goodsAddParam.setToken(param.getToken());
                addDiscountGoods(goodsAddParam);
            }
        } else {
            // 权限不足，返回错误信息。
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        // 操作成功，返回空结果。
        return ResponseResult.success();
    }

    /**
     * 装修 查看限时 活动列表
     *
     * @param param 查询条件的DTO对象，包含分页信息和查询条件
     * @return 返回查询结果的ResponseResult对象，其中包含分页处理后的数据
     *
     */
    @Override
    public ResponseResult queryFinishBaseList(FinishBaseListQueryDTO param) {
        // 初始化一个Map对象，用于存储查询条件
        Map map = null;
        try {
            // 将查询条件DTO转换为Map对象，便于后续的数据库查询
            map = ParseMapUtils.beanToMap(param);
        } catch (IllegalAccessException e) {
            // 打印异常堆栈信息，用于问题排查
            e.printStackTrace();
        }
        // 调用Mapper接口查询已完成的基础列表信息，同时传入分页信息
        IPage<Map<String,Object>> baseList = activityDiscountBaseMapper.selectFinishBaseList(new Query<>().getPage(map),param);
        // 将查询结果进行分页处理，并将处理后的分页数据封装到ResponseResult对象中返回
        return ResponseResult.success(PageUtils.getPage(baseList));
    }

    /**
     * 删除平台优惠基础信息。
     *
     * @param param 优惠基础列表查询DTO，包含优惠ID和用户令牌信息。
     * @return 如果删除成功，返回删除成功的响应结果；如果删除失败，例如令牌非法，返回删除失败的响应结果。
     * @Override
     */
    @Override
    public ResponseResult deletePlatforDiscountBase(DiscountBaseListQueryDTO param) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        // 如果用户信息存在
        if (null != userInfo) {
            // 删除优惠基础信息
            this.baseMapper.deleteById(param.getDiscountId());
            // 删除与该优惠相关的商品优惠信息
            activityDiscountGoodsMapper.delete(new LambdaQueryWrapper<ActivityDiscountGoodsEntity>()
                                       .eq(ActivityDiscountGoodsEntity::getDiscountId,param.getDiscountId()));
            // 返回删除成功的响应结果
            return ResponseResult.success(BizCodeEnume.DELETE_SUCCESS);
        }
        // 如果用户信息不存在，返回令牌非法的响应结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 根据优惠基础信息查询平台优惠详情。
     *
     * @param param 优惠基础列表查询DTO，包含查询条件和分页信息。
     * @return 返回查询结果，如果token非法，则返回失败响应。
     * @throws IllegalAccessException 如果bean转map失败抛出此异常。
     */
    @Override
    public ResponseResult queryPlatforDiscountBaseDetail(DiscountBaseListQueryDTO param) {
        // 通过token获取平台用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        // 检查用户信息是否有效
        if (null != userInfo) {
            try {
                // 将查询条件DTO转换为Map，用于后续查询
                //将对象转换为map
                Map data = ParseMapUtils.beanToMap(param);
                // 执行分页查询，根据优惠ID过滤结果
                //分页查询数据
                IPage<ActivityDiscountGoodsEntity> page = activityDiscountGoodsMapper.selectPage(new Query<ActivityDiscountGoodsEntity>().getPage(data), new LambdaQueryWrapper<ActivityDiscountGoodsEntity>().eq(null!=param.getDiscountId(),ActivityDiscountGoodsEntity::getDiscountId,param.getDiscountId()));

                // 返回查询结果
                return ResponseResult.success(PageUtils.getPage(page));
            } catch (IllegalAccessException e) {
                // 打印异常堆栈信息
                e.printStackTrace();
            }
        }
        // 如果token非法或查询过程中发生异常，返回失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 根据传入的查询条件，查询平台优惠基础信息列表。
     *
     * @param param 优惠基础信息查询条件DTO，包含分页信息和查询条件。
     * @return 查询结果的响应对象，包含分页信息和查询到的优惠基础信息列表。
     */
    @Override
    public ResponseResult queryPlatformDiscountBaseList(DiscountBaseListQueryDTO param) {
        // 通过token获取平台用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        // 如果用户信息不为空
        if (null != userInfo) {
            try {
                // 将查询条件DTO转换为Map，以便进行数据库查询
                //将对象转换为map
                Map data = ParseMapUtils.beanToMap(param);
                // 执行查询，返回分页后的优惠基础信息列表
                //分页查询数据
                IPage<DiscountBaseListVO> page = this.baseMapper.queryPlatformDiscountBaseList(new Query<DiscountBaseListVO>().getPage(data), param);

                // 构建成功响应结果，包含分页信息和查询到的列表数据
                return ResponseResult.success(PageUtils.getPage(page));
            } catch (IllegalAccessException e) {
                // 处理转换过程中的异常，打印异常堆栈信息
                e.printStackTrace();
            }
        }
        // 如果用户信息为空或查询过程中发生异常，返回失败响应，错误码为TOKEN_IS_ILLEGAL
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 根据折扣ID查询折扣基础信息的副本。
     *
     * 该方法主要用于查询一个折扣活动的基础信息及其关联的商品列表。通过传入的参数Map，提取折扣ID，
     * 并根据此ID查询折扣的详细信息和关联的商品列表。查询到的结果经过处理后，返回一个包含折扣信息和商品列表的ResponseResult对象。
     *
     * @param param 一个包含查询参数的Map对象，其中至少应包含折扣ID（discountId）。
     * @return 返回一个ResponseResult对象，其中包含查询到的折扣基础信息和商品列表。
     */
    @Override
    public ResponseResult queryDiscountBaseCopy(Map<String, Object> param) {
        // 根据传入的折扣ID查询折扣基础详情
        DiscountBaseDetailVO discountBase = this.baseMapper.selectDiscountBaseDetail(Integer.valueOf(param.get("discountId").toString()));

        // 查询折扣关联的商品详情，同时对分页信息进行处理
        IPage<DiscountGoodsListVO> page = this.baseMapper.selectDiscountGoodsDetail(new Query<DiscountGoodsListVO>().getPage(param), param);

        // 如果查询结果不为空，则移除商品列表中不必要的关联信息
        if (page != null && page.getRecords().size() > 0) {
            for (DiscountGoodsListVO goodsListResult : page.getRecords()) {
                goodsListResult.setDiscountGoodsId(null);
                goodsListResult.setDiscountId(null);
            }
        }

        // 设置折扣的商品列表和总数，并移除折扣ID的关联信息，以防止不必要的数据泄露或误用
        discountBase.setDiscountGoodsList(page.getRecords());
        discountBase.setDiscountGoodsListCount((int) page.getTotal());
        discountBase.setDiscountId(null);

        // 返回处理后的折扣基础信息，标志查询操作成功
        return ResponseResult.success(discountBase);
    }

    /**
     * 添加折扣商品信息
     *
     * @param param 包含折扣活动ID和待添加的折扣商品列表的参数对象
     * @return 添加结果的响应对象
     */
    public ResponseResult addDiscountGoods(DiscountGoodsAddParamDTO param) {
        // 判断用户权限（商家 token）；失败时再试平台后台 token
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(param.getToken());
        if (!userPermissionsResult.getFlag()) {
            UserInfoVO platformUser = ParseTokenUtils.getPlatformUser(param.getToken());
            if (platformUser != null) {
                userPermissionsResult.setFlag(true);
                userPermissionsResult.setShopId(platformUser.getShopId() == null || platformUser.getShopId().isBlank()
                        ? "0" : platformUser.getShopId());
                userPermissionsResult.setUserId(platformUser.getUserId());
            }
        }

        if (userPermissionsResult.getFlag()) {
            List<DiscountGoodsListDTO> discountGoodsList = param.getDiscountGoodsList();
            String shopId = userPermissionsResult.getShopId();
            if (shopId == null || shopId.isBlank()) {
                shopId = "0";
            }

            // 查询折扣活动基础详情
            DiscountBaseDetailResultVO discountBase = activityDiscountBaseMapper
                    .selectDiscountBaseDetail2(param.getDiscountId());

            Integer commonId = null;

            // 校验折扣商品列表是否为空
            if (CollectionUtils.isEmpty(discountGoodsList)) {
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "无效必传参数：" + JSON.toJSONString(param));
            }

            // 校验商品规格是否已存在
            Integer commonId2 = discountGoodsList.get(0).getCommonId();
            Integer flag = activityDiscountGoodsMapper.checkSpecNum(commonId2);
            if (flag.equals(1)) {
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(),BizCodeEnume.SPEC_REPEAT.getMsg()+JSON.toJSONString(param));
            }

            // 遍历折扣商品列表，处理每个商品
            for (DiscountGoodsListDTO discountGoods : discountGoodsList) {
                commonId = discountGoods.getCommonId();

                // 构造添加或更新折扣商品的信息
                DiscountGoodsAddParamDTO addGoods = new DiscountGoodsAddParamDTO();
                // 设置折扣活动相关的基本信息
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

                // 设置商品的详细信息
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
                // 更新商品显示折扣字段
                // 更新显示折扣字段
                // 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣',
                if (null != commonId) {
                    try {
                        Integer commonIsXian = 3;
                        Map<String,Object> param2 = new HashMap<>();
                        param2.put("commonIsXian",commonIsXian);
                        param2.put("commonId",commonId);
                        goodsServiceClient.updateCommonIsXian(param2);
                    } catch (Exception ignored) {
                        // mall 商品不写旧 common_is_xian，详情侧改由宿主探测
                    }
                }
            }
        } else {
            // 权限不足，返回失败结果
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        // 操作成功，返回成功结果
        return ResponseResult.success();
    }

    /**
     * 根据优惠基础信息查询详情。
     *
     * @param param 优惠基础详情查询参数DTO。
     * @return 查询结果的响应对象。
     * @throws IllegalAccessException 如果访问字段失败。
     */
    @Override
    public ResponseResult queryDiscountBaseDetail(DiscountBaseDetailQueryDTO param) throws IllegalAccessException {
        // 根据优惠ID查询优惠基础详情
        DiscountBaseDetailVO discountBase = activityDiscountBaseMapper.selectDiscountBaseDetail(param.getDiscountId());

        // 将查询参数转换为Map，用于后续分页查询
        Map data = ParseMapUtils.beanToMap(param);

        // 根据优惠ID和分页信息查询优惠商品详情
        IPage<DiscountGoodsListVO> page = activityDiscountGoodsMapper.selectDiscountGoodsDetail(
                new Query<DiscountGoodsListVO>().getPage(data), param);

        // 将查询到的优惠商品列表设置到优惠基础详情中
        discountBase.setDiscountGoodsList(page.getRecords());
        // 设置优惠商品总数
        discountBase.setDiscountGoodsListCount((int) page.getTotal());

        // 返回查询成功的响应结果，包含优惠基础详情和优惠商品信息
        return ResponseResult.success(discountBase);
    }

}
