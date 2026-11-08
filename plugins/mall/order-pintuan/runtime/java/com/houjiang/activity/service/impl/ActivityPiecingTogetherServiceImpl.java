package com.houjiang.activity.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.houjiang.activity.dto.*;
import com.houjiang.activity.entity.*;
import com.houjiang.activity.feign.BaseServiceClient;
import com.houjiang.activity.feign.GoodsServiceClient;
import com.houjiang.activity.feign.OrderServiceClient;
import com.houjiang.activity.mapper.*;
import com.houjiang.activity.service.ActivityPiecingTogetherService;
import com.houjiang.plugin.api.extension.activity.ActivityDetailExtension;
import com.houjiang.activity.utils.AccessTokenUtil;
import com.houjiang.activity.vo.*;
import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.goods.CommonIsXianUpdateDTO;
import com.houjiang.commons.dto.goods.ListPageQueryParamDTO;
import com.houjiang.commons.dto.goods.NavSortLogoDTO;
import com.houjiang.commons.dto.job.ExpireMarkIdUpdateDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.util.secverifyapiutil.Base64Utils;
import com.houjiang.commons.vo.activity.AppPintuanGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.ExpireMarkIdUpdateParamVO;
import com.houjiang.commons.vo.activity.JoinPintuanParamVO;
import com.houjiang.commons.vo.activity.ModularGoodsVO;
import com.houjiang.commons.vo.base.BaseSysConfigsVO;
import com.houjiang.commons.vo.goods.GdsDetailsCommonQueryResultVO;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ActivityPiecingTogetherServiceImpl
 * @Description 拼团主表
 * @Author Amos
 * @Date 2023-05-25 14:27:05
 * @Version 1.0
 * @author Amos
 */
@Service
public class ActivityPiecingTogetherServiceImpl extends ServiceImpl<ActivityPiecingTogetherMapper, ActivityPiecingTogetherEntity>
        implements ActivityPiecingTogetherService {

    /**活动详情扩展点：限时折扣/限时秒杀/满减分属不同插件，数据层互不可见，由各插件自行提供详情查询。 */
    private final java.util.Map<String, ActivityDetailExtension> detailExtensions =
            new java.util.concurrent.ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void registerDetailExtensions(java.util.List<ActivityDetailExtension> extensions) {
        if (extensions == null) {
            return;
        }
        for (ActivityDetailExtension impl : extensions) {
            String t = impl.activityType();
            if (t != null && !t.isBlank()) {
                detailExtensions.putIfAbsent(t, impl);
            }
        }
    }
    @Autowired
    private ActivityPiecingTogetherMarkMapper activityPiecingTogetherMarkMapper;
    @Autowired
    private ActivityPiecingTogetherBuyerMapper activityPiecingTogetherBuyerMapper;
    @Autowired
    private ActivityPiecingTogetherTempMapper activityPiecingTogetherTempMapper;
    @Value("${domainName}")
    private String domainName;
    @Autowired
    private BaseServiceClient baseServiceClient;
    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private GoodsServiceClient goodsServiceClient;

    @Autowired
    private ActivityPiecingTogetherDetailMapper activityPiecingTogetherDetailMapper;

    @Autowired
    private RedisCache redisCache;

    private static final Logger LOG = LoggerFactory.getLogger(ActivityPiecingTogetherServiceImpl.class);
    /**
     * 根据商品ID查询拼团商品信息。
     *
     * @param commonId 商品的通用ID，用于标识商品的唯一性。
     * @param goodsId 拼团商品的ID，用于特定于拼团商品的标识。
     * @return 返回查询到的拼团商品信息，包含拼团相关的详细数据。
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
    public AppPintuanGoodsQueryResultVO selectByGoodsId(Integer commonId, Integer goodsId) {
        return this.baseMapper.selectByGoodsId(commonId,goodsId);
    }

    /**
     * 查询指定标记ID对应的剩余数量。
     *
     * @param markId 标记ID，用于查询特定标记下的剩余数量。
     * @return 返回查询结果，如果标记ID对应的剩余数量不存在，则返回失败结果。
     */
    @Override
    public ResponseResult querySurplusNum(String markId) {
        Integer surplusNum = this.baseMapper.selectSurplusNum(markId);
        // 如果查询到的剩余数量为null，表示该标记ID不存在或未配置剩余数量，返回失败结果。
        if(surplusNum == null){
            return ResponseResult.fail(BizCodeEnume.MARKID_IS_NULL);
        }
        // 返回查询到的剩余数量，表示查询成功。
        return ResponseResult.success(surplusNum);
    }

    /**
     * 参加拼团的接口
     *
     * @param param 包含拼团相关参数的VO对象，如用户信息、商品ID、拼团ID等
     * @return 返回操作结果，成功或失败以及相应的信息
     */
    @Override
    public ResponseResult addJoinPintuan(JoinPintuanParamVO param) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 根据商品ID和拼团通用ID查询拼团基础信息
        // 查询该商品的拼团信息
        PintuanBaseMessageVO pintuanBaseMessage = this.baseMapper.selectPintuanId(param.getCommonId(),
                param.getGoodsId());
        // 如果查询不到拼团信息，则返回拼团失败的响应
        if (null == pintuanBaseMessage) {
            return ResponseResult.fail(0, "发起拼团失败：无拼团信息");
        }
        Integer personNum = pintuanBaseMessage.getPersonNum();

        // 初始化标记消息对象，用于记录拼团相关的用户信息和状态
        PintuanMarkMessageVO markMessage = new PintuanMarkMessageVO();
        // 设置创建时间、用户ID、头像、用户名等信息
        markMessage.setCreatedTime(new Date());
        markMessage.setUserId(param.getUserId());
        markMessage.setUserAvatar(userInfo.getUserAvatar());
        markMessage.setUserName(userInfo.getUserName());
        // 设置拼团详情ID、类型、商品ID等信息
        markMessage.setDetailId(pintuanBaseMessage.getDetailId());
        markMessage.setType(param.getType());
        markMessage.setGoodsId(pintuanBaseMessage.getGoodsId());
        // 设置标记ID、支付状态等
        // 支付状态 0 未付款 1 已付款 2 以退款
        markMessage.setMarkId(param.getMarkId());
        markMessage.setPayTtart(0);

        // 初始化参与拼团的人数、余位数、状态等
        Integer num = 1;
        Integer surplusNum = 1;
        // 0,等待成团。1为已成团，2为成团失败，3为已完成退款
        Integer status = 0;

        // 判断是否为团长发起的拼团
        if (param.getIsMark().equals(1)) {
            // 迁 Mall 收银台：下单时仅占位，成团由支付成功回调按已付款人数判定，故此处恒为等待成团
            surplusNum = personNum - num;
            status = 0;
            // 设置团长相关信息，并插入数据库
            // 发起新的拼团
            markMessage.setNum(num);
            markMessage.setSurplusNum(surplusNum);
            markMessage.setStatus(status);
            // 拼团，团长标识-我的拼团
            activityPiecingTogetherMarkMapper.insertPintuanMark(markMessage);

            // 插入购买人信息
            markMessage.setRanking(1);
            activityPiecingTogetherBuyerMapper.insertPintuanBuyer(markMessage);

            // 生成订单号并记录
            markMessage.setOrderId(param.getOrderId());
            activityPiecingTogetherTempMapper.insertPintuanTemp(markMessage);
        } else {
            /*-------------------------------拼团增加参与者-------------------------------------*/
            // 查询当前拼团的团长信息
            PintuanMarkMessageVO pintuanMarkMessage = this.baseMapper.selectMarkMessage(param.getMarkId());
            // 如果团长信息存在，则更新参与人数、余位数等信息
            if (null != pintuanMarkMessage) {
                num = pintuanMarkMessage.getNum() + 1;
                surplusNum = pintuanMarkMessage.getSurplusNum() - 1;
                personNum = pintuanMarkMessage.getPersonNum();
                // 迁 Mall 收银台：下单时仅占位，成团由支付成功回调按已付款人数判定，故此处恒为等待成团
                surplusNum = personNum - num;
                status = 0;
                // 更新团长信息，并插入新的购买人信息
                markMessage.setNum(num);
                markMessage.setSurplusNum(surplusNum);
                markMessage.setStatus(status);
                markMessage.setMarkId(param.getMarkId());
                markMessage.setRanking(num);
                activityPiecingTogetherBuyerMapper.insertPintuanBuyer(markMessage);
                activityPiecingTogetherMarkMapper.updateMarkMessage(markMessage);

                // 生成订单号并记录
                markMessage.setOrderId(param.getOrderId());
                activityPiecingTogetherTempMapper.insertPintuanTemp(markMessage);
            } else {
                // 如果查询不到团长信息，则返回拼团失败的响应
                return ResponseResult.fail(0, "发起拼团失败：无拼团信息");
            }
        }

        // 迁 Mall 收银台：下单不再判定成团、不再写 Redis 交接键；成团在支付成功回调 markPaidAndEvaluate 中按已付款人数判定
        // 返回操作成功的响应
        return ResponseResult.success();
    }

    /**
     * 支付成功后回写已付款，并按已付款人数判定成团。
     *
     * <p>拼团迁至 Mall 收银台后，成团不再在下单时按参团人数判定，而是在每笔订单
     * 支付成功回调时按 temp.pay_start=1 的已付款人数判定；满员则置团为已成团并
     * 返回已付款订单号列表，由 order 侧推进发货。</p>
     */
    @Override
    public ResponseResult markPaidAndEvaluate(Map<String, Object> param) {
        String markId = param.get("markId") == null ? null : String.valueOf(param.get("markId"));
        String orderId = param.get("orderId") == null ? null : String.valueOf(param.get("orderId"));
        if (markId == null || markId.isBlank() || orderId == null || orderId.isBlank()) {
            return ResponseResult.fail(0, "参数缺失：markId/orderId");
        }
        // 幂等标记该订单已付款（temp.pay_start 0->1）
        activityPiecingTogetherTempMapper.updatePintByOrder(orderId);
        // 按已付款人数判定成团
        Integer paid = activityPiecingTogetherTempMapper.countPaidTempByMarkId(markId);
        Integer personNum = activityPiecingTogetherMarkMapper.selectPersonNumByMarkId(markId);
        Map<String, Object> data = new HashMap<>();
        data.put("formed", 0);
        if (personNum != null && personNum > 0 && paid != null && paid >= personNum) {
            // 幂等置团为已成团（仅当当前 status=0）
            activityPiecingTogetherMarkMapper.updateMarkFormedByMarkId(markId);
            data.put("formed", 1);
            data.put("orderIdList", activityPiecingTogetherTempMapper.selectPaidOrderIdsByMarkId(markId));
        }
        return ResponseResult.success(data);
    }

    /**
     * 查询所有数据，根据分页参数进行分页查询。
     *
     * @param param 分页查询参数，包含页码、每页数量等信息。
     * @return 返回查询结果，如果查询成功，则包含分页信息和数据列表；如果查询失败，则返回空。
     */
    @Override
    public ResponseResult queryAll(ListPageQueryParamDTO param) {
        try {
            // 将查询参数转换为Map对象，以便后续处理
            Map data = ParseMapUtils.beanToMap(param);

            // 执行分页查询，获取品牌列表
            // 分页查询数据
            IPage<Map<String, Object>> page = this.baseMapper.queryBrandList(new Query<Map<String, Object>>().getPage(data),param);

            // 构建成功响应结果，包含分页信息和查询数据
            return ResponseResult.success(PageUtils.getPage(page));
        } catch (IllegalAccessException e) {
            // 处理异常，打印堆栈跟踪
            e.printStackTrace();
        }
        // 如果查询过程中发生异常，则返回失败响应结果
        return ResponseResult.fail();
    }

    /**
     * 取消拼团订单并删除相关数据。
     *
     * @param param 包含订单ID的参数映射。
     * @return 如果操作成功，返回成功的响应结果；如果订单信息不完整或不存在，返回失败的响应结果。
     */
    @Override
    public ResponseResult delete(Map<String, Object> param) {
        // 获取订单ID
        String orderId = (String) param.get("orderId");
        // 如果订单ID不为空
        if (null != orderId) {
            // 根据订单ID查询拼团临时表中的订单信息
            ///查询该订单的markId userid
            ActivityPiecingTogetherTempEntity orderPintuanTempEntity = activityPiecingTogetherTempMapper.selectOne(new LambdaQueryWrapper<ActivityPiecingTogetherTempEntity>()
                    .eq(ActivityPiecingTogetherTempEntity::getOrderId, orderId));

            // 如果查询到的订单信息完整
            if (null != orderPintuanTempEntity && null != orderPintuanTempEntity.getMarkId() && null != orderPintuanTempEntity.getUserId()) {
                // 删除拼团临时表中该用户的订单信息
                //拼团订单取消 删除当前用户在该拼团的信息 释放拼团名额
                activityPiecingTogetherTempMapper.delete(new LambdaQueryWrapper<ActivityPiecingTogetherTempEntity>()
                        .eq(ActivityPiecingTogetherTempEntity::getUserId, orderPintuanTempEntity.getUserId())
                        .eq(ActivityPiecingTogetherTempEntity::getMarkId, orderPintuanTempEntity.getMarkId()));

                // 删除拼团购买者表中该用户的订单信息
                activityPiecingTogetherBuyerMapper.delete(new LambdaQueryWrapper<ActivityPiecingTogetherBuyerEntity>()
                        .eq(ActivityPiecingTogetherBuyerEntity::getUserId, orderPintuanTempEntity.getUserId())
                        .eq(ActivityPiecingTogetherBuyerEntity::getMarkId, orderPintuanTempEntity.getMarkId()));

                // 判断该用户是否是团长
                // 判断当前用户是否是团长 是 删除团 不是 人数减一
                Integer num = Math.toIntExact(activityPiecingTogetherMarkMapper.selectCount(new LambdaQueryWrapper<ActivityPiecingTogetherMarkEntity>()
                        .eq(ActivityPiecingTogetherMarkEntity::getUserId, orderPintuanTempEntity.getUserId())
                        .eq(ActivityPiecingTogetherMarkEntity::getMarkId, orderPintuanTempEntity.getMarkId())));
                if (null != num && num > 0) {
                    // 如果是团长，删除团长信息
                    activityPiecingTogetherMarkMapper.delete(new LambdaQueryWrapper<ActivityPiecingTogetherMarkEntity>()
                            .eq(ActivityPiecingTogetherMarkEntity::getUserId, orderPintuanTempEntity.getUserId())
                            .eq(ActivityPiecingTogetherMarkEntity::getMarkId, orderPintuanTempEntity.getMarkId()));
                } else {
                    // 如果不是团长，减少拼团剩余人数
                    ActivityPiecingTogetherMarkEntity markEntity = activityPiecingTogetherMarkMapper.selectOne(new LambdaQueryWrapper<ActivityPiecingTogetherMarkEntity>()
                            .eq(ActivityPiecingTogetherMarkEntity::getMarkId, orderPintuanTempEntity.getMarkId()));
                    // 当前参团人数 -1  还差几人成团 +1 标记当前团可使用
                    activityPiecingTogetherMarkMapper.update(markEntity, new LambdaUpdateWrapper<ActivityPiecingTogetherMarkEntity>()
                            .eq(ActivityPiecingTogetherMarkEntity::getMarkId, orderPintuanTempEntity.getMarkId())
                            .set(ActivityPiecingTogetherMarkEntity::getNum, markEntity.getNum() - 1)
                            .set(ActivityPiecingTogetherMarkEntity::getStatus,0)
                            .set(ActivityPiecingTogetherMarkEntity::getSurplusNum, markEntity.getSurplusNum() + 1));
                }
                // 操作成功，返回成功响应
                return ResponseResult.success();
            }
        }
        // 订单信息不完整或不存在，操作失败，返回失败响应
        return ResponseResult.fail();
    }


    /**
     * 根据参数查询拼团商品信息。
     *
     * @param param 查询参数，包含拼团商品的过滤条件和分页信息。
     * @return 返回查询结果，包含拼团商品列表和轮播图信息。
     */
    @Override
    public ResponseResult queryGoods(WebPinTuanParamDTO param) {
        try {
            // 初始化结果映射表
            Map<String, Object> map = new HashMap<>();
            // 将查询参数转换为Map格式，以便后续处理
            //查询平台商品
            Map data = ParseMapUtils.beanToMap(param);
            // 执行拼团商品查询，获取分页结果
            //分页查询数据
            IPage<GoodsPinTuanResultVO> page = this.baseMapper.queryPinTuanGoods(new Query<GoodsPinTuanResultVO>().getPage(data),param);

            // 获取查询结果列表
            List<GoodsPinTuanResultVO> results =  page.getRecords();
            // 如果查询结果不为空
            if(null != results && 0 < results.size()){
                // 遍历结果列表，为每个商品生成二维码
                for (GoodsPinTuanResultVO result : results) {
                    // 生成H5二维码，并转换为Base64编码
                    //生成h5 二维码
                    byte[] b = QrCodeUtil.createQRCode(150, 150, domainName + "/h5/#/pages/description/descrption?goodsId=" + result.getGoodsId() + "&commonId=" + result.getCommonId());
                    // 转base 64
                    String orCode = QrCodeUtil.BASE_PREFIX + Base64Utils.encode(b);
                    result.setOrCode(orCode);
                    // 生成APP二维码，并转换为Base64编码
                    //生成app 二维码
                    System.out.println("/pages/description/descrption?goodsId=" + result.getGoodsId() + "&commonId=" + result.getCommonId()+"&type=\"pingTuan\"");
                    byte[] app = QrCodeUtil.createQRCode(150, 150, "/pages/description/descrption?goodsId=" + result.getGoodsId() + "&commonId=" + result.getCommonId()+"&type=\"pingTuan\"");
                    // 转base 64
                    String appOrCode = QrCodeUtil.BASE_PREFIX + Base64Utils.encode(app);
                    result.setAppOrCode(appOrCode);
                }
            }
            // 将查询结果和总数量添加到映射表中
            map.put("results",results);
            map.put("count",PageUtils.getPage(page).getTotalCount());
            // 查询轮播图信息
            //查询轮播图
            NavSortLogoDTO gdbNavSortLogoDTO = new NavSortLogoDTO();
            gdbNavSortLogoDTO.setConfigType("group_slide_picture_pc");
            // 调用服务获取轮播图数据
            ResponseResult resp1 = baseServiceClient.quertNavLogo(gdbNavSortLogoDTO);
            // 将轮播图数据添加到映射表中
            List<BaseSysConfigsVO> list = (List<BaseSysConfigsVO>) resp1.getData();
            map.put("images",list);
            // 返回查询结果
            return ResponseResult.success(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 如果发生异常，返回系统错误信息
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(),BizCodeEnume.SYSTEM_ERROR.getMsg());
    }

    /**
     * 根据传入的参数Map查询拼团信息。
     * 本方法主要用于从数据库中根据拼团ID查询相应的拼团信息。如果参数Map中包含"id"键，
     * 则会尝试将该id转换为字符串，然后通过查询操作获取对应的拼团信息实体。
     *
     * @param param 一个包含查询参数的Map，其中可能包含用于查询拼团信息的"id"参数。
     * @return 返回一个ResponseResult对象，其中包含查询操作的成功与否信息以及查询到的拼团信息实体。
     */
    @Override
    public ResponseResult selectMarkInfo(Map<String, Object> param) {
        // 初始化拼团信息实体对象，用于存放查询结果
        ActivityPiecingTogetherMarkEntity entity = null;

        // 检查参数Map中是否包含"id"键，如果存在，则尝试将其转换为字符串并查询对应的拼团信息
        if (null != param.get("id")) {
            entity = activityPiecingTogetherMarkMapper.selectById(param.get("id").toString());
        }

        // 构建并返回一个表示查询操作成功的ResponseResult对象，其中包含查询到的拼团信息实体
        return ResponseResult.success(entity);
    }


    /**
     * 更新支付开始状态
     * @param param 包含订单ID等参数的映射
     * @return 更新操作的结果
     */
    @Override
    public ResponseResult updatePayStart(Map<String, Object> param) {
        // 从参数中提取订单ID
        String orderId = (String) param.get("orderId");
        // 检查订单ID是否存在
        if(null != orderId ){
            // 查询该订单是否为拼团订单
            //查询该订单是否是拼团订单
            String markId = activityPiecingTogetherTempMapper.queryPintByOrder(orderId);
            // 检查是否为拼团订单
            if(null != markId){
                // 查询拼团剩余人数
                //判断拼团 人员是否已拼满
                Integer surplusNum = activityPiecingTogetherMarkMapper.querySurplusNum(markId);
                // 检查拼团是否已满员
                if(0 == surplusNum){
                    // 查询除当前订单外，其他同团订单的支付状态
                    // 判断除了当前单外其他单是否都已支付
                    Integer orderNum = activityPiecingTogetherMarkMapper.otherOrderIsPay(markId,orderId);
                    // 检查其他订单是否已全部支付
                    if (0 == orderNum) {
                        // 查询同团所有订单ID
                        List<String> orderIdlist = activityPiecingTogetherMarkMapper.selectByMarkId(markId);
                        // 更新同团所有订单的拼团状态
                        //修改拼团订单支付状态 order_base  groupbuy_status 1-拼团中 2-已成功 3未成功 0非拼团
                        orderServiceClient.updateOrderGroupbuyStatus(orderIdlist);
                    }
                }
                // 更新拼团订单的支付状态
                //修改拼团的支付状态
                Integer num = activityPiecingTogetherTempMapper.updatePintByOrder(orderId);
                // 检查更新是否成功
                if(num > 0){
                    // 更新成功，返回成功结果
                    return ResponseResult.success();
                }
            }
        }
        // 更新失败或参数缺失，返回失败结果
        return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
    }
    /**
     * 组件化查询拼团活动列表
     * 该方法通过接收一个查询参数对象，转换为Map格式后，查询拼团活动的基础信息，并返回分页结果。
     *
     * @param param 查询参数对象，包含拼团活动的查询条件
     * @return 返回查询结果的分页信息，包含拼团活动的基础信息
     */
    @Override
    public ResponseResult queryList(PintuanBaseListQueryDTO param) {
        // 初始化一个Map对象，用于存储查询参数
        Map map = null;
        try {
            // 将查询参数对象转换为Map格式，便于后续的查询操作
            map = ParseMapUtils.beanToMap(param);
        } catch (IllegalAccessException e) {
            // 打印异常信息，处理访问权限异常
            e.printStackTrace();
        }
        // 根据查询参数Map和分页信息，查询拼团活动的基础信息
        IPage<Map<String, Object>> page = activityPiecingTogetherTempMapper.selectModularizationPintuanBaseList(new Query<Map<String, Object>>().getPage(map),param);
        // 将查询结果封装为分页响应对象，返回给调用方
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 查询指定商品在活动中的价格信息。
     *
     * 本方法通过商品ID查询活动拼装临时表中的商品价格信息。适用于活动期间，需要获取商品的活动价格以供展示或计算。
     * 使用活动拼装临时表是因为活动期间的商品价格可能发生变化，通过临时表可以更灵活地管理活动期间的商品价格。
     *
     * @param goodsId 商品的唯一标识ID。用于指定查询的商品。
     * @return 返回查询结果，包含商品的活动价格信息。如果查询成功，结果中会包含商品的详细价格信息；如果查询失败，则会根据失败原因返回相应的错误信息。
     */
    @Override
    public ResponseResult queryGoodsPrice(Integer goodsId) {
        // 通过商品ID查询活动拼装临时表中的商品价格信息
        ModularGoodsVO modularGoodsVO = activityPiecingTogetherTempMapper.queryGoodsPrice(goodsId);
        // 返回查询结果，如果查询成功，则将查询到的商品价格信息封装在ResponseResult中返回
        return ResponseResult.success(modularGoodsVO);
    }

    /**
     * 查询所有拼团商品的详细信息。
     *
     * 本方法通过调用baseMapper的queryGoodsAll方法，查询所有拼团商品的信息，并返回相应的分页结果。
     * 使用Map<String, Object>作为参数，可以灵活地传递各种查询条件，以满足不同的查询需求。
     * 返回的ResponseResult中包含了分页信息和查询结果，方便前端进行展示和处理。
     *
     * @param param 查询参数，以Map形式接收，可根据需求传递不同的查询条件。
     * @return 包含分页信息和查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult queryGoodsAll(Map<String, Object> param) {

        // 调用baseMapper的queryGoodsAll方法查询所有拼团商品信息，并返回分页结果
        IPage<GoodsPinTuanResultVO> page=this.baseMapper.queryGoodsAll(new Query<GoodsPinTuanResultVO>().getPage(param),
                param);

        // 构建成功响应结果，并返回分页处理后的数据
        return ResponseResult.success(PageUtils.getPage(page));
    }


    /**
     * 更新拼团信息，主要是对拼团活动的排序进行更新。
     * @param param 包含拼团活动ID等信息的参数映射。
     * @return 返回操作结果，成功则返回一个空的成功结果。
     */
    @Override
    public ResponseResult updateDisorder(Map<String, Object> param) {
        // 将Map参数转换为ActivityPiecingTogetherEntity实体，便于后续更新操作。
        ActivityPiecingTogetherEntity entity = JSONObject.parseObject(JSON.toJSONString(param), ActivityPiecingTogetherEntity.class);
        // 使用Lambada表达式更新指定拼团ID的拼团活动信息。
        this.baseMapper.update(entity,new LambdaQueryWrapper<ActivityPiecingTogetherEntity>()
                               .eq(ActivityPiecingTogetherEntity::getPintuanId,param.get("pintuanId").toString()));
        return ResponseResult.success();
    }

    /**
     * 查询拼团基础信息列表。
     * @param param 包含分页信息和查询条件的参数映射。
     * @return 返回查询结果，包含分页信息和拼团基础信息列表。
     */
    @Override
    public ResponseResult queryPintuanBaseList(Map<String, Object> param) {
        // 执行查询操作，获取分页结果。
        IPage<ReductionPolicyBaseListVO> page =this.baseMapper.queryPintuanBaseList(new Query<ReductionPolicyBaseListVO>().getPage(param),
                param);
        List<ReductionPolicyBaseListVO> records = page.getRecords();
        // 遍历查询结果，设置商品详情URL。
        for (ReductionPolicyBaseListVO record : records) {
            record.setGoodsDetailUrl(domainName+ "/pc/pages/description.html?goodsId=" + record.getGoodsId());
        }
        // 返回处理后的查询结果。
        return ResponseResult.success(PageUtils.getPage(page));
    }
    /**
     * 根据类型查询模块化拼团、折扣、秒杀基础信息。
     * @param param 包含查询类型(type)和ID列表(id)的参数映射。
     * @return 返回查询结果的ResponseResult对象。
     * @throws Exception 如果查询过程中发生异常。
     */
    @Override
    public ResponseResult modularizationQueryPintuanBasebyId(Map<String, Object> param) {
        // 初始化用于存储查询结果的映射和列表
        Map<String,Object> map = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        List<Integer> ids = new ArrayList<>();
        List resultList = new ArrayList();
        String type = null;
        try {
            // 从参数中提取查询类型和ID列表
            type = (String) param.get("type");
            ids =(List<Integer>) param.get("id");
            // 校验查询类型是否为空
            if(null == type){
                return ResponseResult.fail(0, "type不能为null");
            }
            // 校验ID列表是否为空
            if(0 == ids.size()){
                return ResponseResult.fail(0, "拼团Id不能为null");
            }
            // 根据查询类型分情况查询并组装结果
            // 查询拼团的商品详情
            if("pintuan".equals(type)){
                for (Integer id : ids) {
                    map2 = this.baseMapper.selectModularization(id);
                    map = this.baseMapper.selectModularizationPintuanBaseById(id);
                    map2.put("items",map);
                    resultList.add(map2);
                }
                return ResponseResult.success(resultList);
            }else {
                // 折扣 / 秒杀 / 满减 / 拼团 的详情查询统一走 ActivityDetailExtension：
                // 这些活动分属不同插件，数据层互不可见，由各活动插件自行提供查询实现，
                // 本类只按 type 路由并消费结果，不再直接注入其他活动插件的 Mapper。
                String extType = type;
                if ("zhekou".equals(type)) {
                    extType = "discount";
                } else if ("miaosha".equals(type)) {
                    extType = "seckill";
                } else if ("manjian".equals(type)) {
                    extType = "reduction";
                }
                ActivityDetailExtension ext = detailExtensions.get(extType);
                if (ext == null) {
                    return ResponseResult.fail("不支持的活动类型：" + type);
                }
                ActivityDetailExtension.Context detailCtx = new ActivityDetailExtension.Context();
                detailCtx.activityType = extType;
                detailCtx.activityIds = ids;
                try {
                    ext.execute(detailCtx);
                } catch (RuntimeException ex) {
                    log.error("活动详情扩展执行失败: type=" + type, ex);
                    return ResponseResult.fail(ex.getMessage());
                }
                if (detailCtx.isAborted()) {
                    return ResponseResult.fail(detailCtx.getAbortReason());
                }
                return ResponseResult.success(detailCtx.result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // 对于查询过程中的异常，返回错误信息
            return ResponseResult.fail(e.getMessage());
        }

    }

/**
 * 根据传入的参数获取拼团海报信息。
 * @param param 包含token、markId、type等参数的映射。
 * @return 返回包含拼团信息和二维码信息的响应结果。
 */
@Override
public ResponseResult getPinTuanPoster(Map<String, Object> param) {
    // 初始化响应映射
    Map<String,Object> respMap = new HashMap<>();
    // 初始化二维码字符串
    // 二维码的base64位
    String orCode = null;
    try {
        // 根据token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        // 获取markId和type参数
        String markId = (String) param.get("markId");
        String type = (String) param.get("type");
        // 检查用户信息是否为空
        if(null == userInfo){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        // 检查markId和type是否为空
        if(null == markId || null == type){
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(),BizCodeEnume.VALID_EXCEPTION.getMsg());
        }
        // 根据markId查询拼团信息
        //查询当前拼团
        GoodsPinTuanResultVO goodsPinTuanResult = activityPiecingTogetherMarkMapper.queryPintByMarkId(markId);
        // 将用户姓名和头像添加到响应映射中
        respMap.put("userName",userInfo.getUserName());
        respMap.put("userAvatar",userInfo.getUserAvatar());
        // 根据type生成相应的二维码
        //生成二维码1 是 h5  2 是小程序
        if(null != type && "1".equals(type)){
            // 生成H5二维码的URL并转换为base64字符串
            byte[] b = QrCodeUtil.createQRCode(150, 150, domainName + "/h5/#/pages/description/descrption?goodsId="+ goodsPinTuanResult.getGoodsId() +"&commonId=" + goodsPinTuanResult.getCommonId() + "&markId=" + markId);
            // 转base 64
            orCode = QrCodeUtil.BASE_PREFIX + Base64Utils.encode(b);
        }else{
            // 生成小程序二维码，由于参数长度限制，部分参数存入Redis
            //定义变长字符串
            StringBuilder str=new StringBuilder();
            Random random=new Random();
            // 随机生成数字字符串用于小程序二维码的场景标识
            //随机生成数字，并添加到字符串  小程序二维码不能超过 32 位
            for(int i=0;i<8;i++){
                str.append(random.nextInt(6));
            }
            int num=Integer.parseInt(str.toString());
            // 生成二维码场景字符串和文件名
            String sceneStr = "codeId=" + num+"&codeType=PINTUAN";
            String fileName = AccessTokenUtil.getminiqrQr(sceneStr, "pages/description/descrption");
            // 组合二维码的base64字符串
            orCode = QrCodeUtil.BASE_PREFIX + fileName;
            // 将拼团信息存入Redis
            Map<String,Object> map = new HashMap<>();
            map.put("goodsId",goodsPinTuanResult.getGoodsId());
            map.put("commonId",goodsPinTuanResult.getCommonId());
            map.put("markId",markId);
            //将拼团团id存入redis从另外接口查询
            redisCache.setCacheObject("PINTUAN_" + num, JsonUtils.toString(map));
            redisCache.expire("PINTUAN_" + num, 60*60*24*7, TimeUnit.SECONDS);
        }
        // 将二维码信息添加到拼团信息中
        goodsPinTuanResult.setOrCode(orCode);
        // 将拼团信息添加到响应映射中
        respMap.put("goodsPinTuanResult",goodsPinTuanResult);
        // 返回成功的响应结果
        return ResponseResult.success(respMap);
    } catch (Exception e) {
        e.printStackTrace();
    }
    // 如果发生异常，返回失败的响应结果
    return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(),BizCodeEnume.SYSTEM_ERROR.getMsg());
}


    /**
     * 查询拼团基础详情
     *
     * @param param 请求参数，包含拼团ID和其他可能的请求信息
     * @return ResponseResult 返回查询结果，如果权限不足，则返回失败结果
     */
    @Override
    public ResponseResult queryPinTuanBaseDetail(Map<String, Object> param) {
        // 判断用户权限
        // 权限判断
        UserPermissionsResult userPermissionsResult = loginPermissions(param.get("token").toString());

        // 如果用户具有权限
        if (userPermissionsResult.getFlag()) {
            // 根据拼团ID查询拼团信息
            ActivityPiecingTogetherEntity entity = this.baseMapper.selectById(param.get("pintuanId").toString());
            // 如果拼团信息存在
            if (null != entity) {
                // 根据拼团商品的通用ID查询拼团商品的详细信息
                GdsDetailsCommonQueryResultVO commonBase = goodsServiceClient.selectByCommonIdFeign(entity.getCommonId());
                // 根据拼团ID和拼团商品的通用ID查询拼团商品的规格列表
                List<PintuanGoodsSpecListVO> goodsSpecList = this.baseMapper.selectByCommonId5(entity.getCommonId(), param.get("pintuanId").toString());

                // 将查询到的拼团商品详细信息和规格列表设置到拼团信息对象中
                entity.setCommonBase(commonBase);
                entity.setGoodsSpecList(goodsSpecList);
            }
            // 返回查询成功的响应结果，包含拼团信息
            return ResponseResult.success(entity);
        } else {
            // 返回权限不足的失败响应结果
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
    }

    /**
     * 添加拼团基础信息
     * @param param 拼团基础信息添加参数
     * @return 添加结果
     */
    @Override
    public ResponseResult addPinTuanBase(PintuanBaseAddDTO param) {
        // 通过token获取用户信息
        // 权限判断
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());

        if (userInfo != null) {
            // 设置店铺ID和用户ID；平台超管无店铺时落 0，避免 shop_id 非空约束失败
            String shopId = userInfo.getShopId();
            if (shopId == null || shopId.isBlank()) {
                shopId = "0";
            }
            param.setShopId(shopId);
            param.setUserId(userInfo.getUserId());
            param.setIsRecommend(0);
            // 设置拼团开始小时
            // 拼团持续时间
            param.setStartHour(DateUtils.getDateHost(param.getStartTime(),param.getEndTime()));
            // 设置创建时间和更新时间
            param.setCreatedTime(new Date());
            param.setUpdateTime(new Date());
            // 默认设置为不可用和待开启状态
            Integer commonIsXian = 0;
            // 是否可用，1为可用,0不可用 2 待开启
            Integer status = 0;
            // 根据当前时间与拼团时间范围判断，设置状态和商品是否可用
            if (DateUtils.belongCalendar(new Date(), param.getStartTime(), param.getEndTime())) {
                status = 1;
                commonIsXian = 4;
            } else {
                status = 2;
                commonIsXian = 4;
            }
            // 兼容 mall 新商品表：旧 Feign 查 hou_goods_common，空库/仅 mall 商品时允许降级继续建活动
            GdsDetailsCommonQueryResultVO goodsMessage = null;
            try {
                goodsMessage = goodsServiceClient.selectByCommonIdFeign(param.getCommonId());
            } catch (Exception e) {
                // 忽略，下方用默认分类
            }
            Integer catId = (goodsMessage != null && goodsMessage.getCatId() != null) ? goodsMessage.getCatId() : 0;

            // 设置商品状态
            param.setStatus(status);
            List<PintuanDetailSpecAddDTO> pintuanDetail = param.getGoodsSpecList();
            if (pintuanDetail == null || pintuanDetail.isEmpty()) {
                return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "请至少配置一个规格拼团价");
            }
            // 如果拼团ID为空，表示新增拼团信息
            // 活动存在就修改
            if (null == param.getPintuanId()) {
                // 插入拼团基础信息
                //将拼团信息 存入活动基础表
                this.baseMapper.insertPintuanBase(param);

                // 遍历拼团详情规格信息，设置分类ID和拼团ID，并插入拼团详情表
                for (PintuanDetailSpecAddDTO pintuanDetailSpecAddParam : pintuanDetail) {
                    pintuanDetailSpecAddParam.setCid(catId);
                    pintuanDetailSpecAddParam.setPintuanId(param.getPintuanId());
                    //存入拼团活动描述表
                    this.baseMapper.insertPintuanDetail(pintuanDetailSpecAddParam);
                }

            } else {
                // 更新拼团基础信息
                //拼团信息活动基础表更新
                this.baseMapper.updatePintuanBase(param);
                // 遍历拼团详情规格信息，设置分类ID和拼团ID，并更新拼团详情表
                for (PintuanDetailSpecAddDTO pintuanDetailSpecAddParam : pintuanDetail) {
                    pintuanDetailSpecAddParam.setCid(catId);
                    pintuanDetailSpecAddParam.setPintuanId(param.getPintuanId());
                    //拼团活动描述表更新
                    this.baseMapper.updatePintuanDetail(pintuanDetailSpecAddParam);
                }

            }

            // 更新商品促销状态（旧表可能不存在，失败不阻断建活动）
            List<Integer> commonList = Lists.newArrayList();
            commonList.add(param.getCommonId());
            if (!CollectionUtils.isEmpty(commonList)) {
                CommonIsXianUpdateDTO commonIsXianUpdateParam = new CommonIsXianUpdateDTO();
                // 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
                commonIsXianUpdateParam.setCommonIdList(commonList);
                commonIsXianUpdateParam.setCommonIsXian(commonIsXian);
                try {
                    goodsServiceClient.updateCommonIsXian3(commonIsXianUpdateParam);
                } catch (Exception e) {
                    // mall 商品无旧促销位字段时忽略
                }
            }

        } else {
            // 如果用户信息为空，表示权限不足，返回失败结果
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        // 返回成功结果，携带拼团ID
        return ResponseResult.success(param.getPintuanId());
    }

    /**
     * 删除拼团基础信息。
     *
     * @param param 包含拼团ID的Map对象。
     * @return 返回操作结果的ResponseResult对象。
     */
    @Override
    public ResponseResult deletePinTuanBase(Map<String, Object> param) {
        // 根据拼团ID查询拼团信息
        ActivityPiecingTogetherEntity result = this.baseMapper.selectById(param.get("pintuanId").toString());

        // 查询未成团用户列表，为他们标记成团失败并退款
        //查询未成团用户 退款
        List<String> expireMarkIdList = activityPiecingTogetherMarkMapper.selectUnformedMarkId(Integer.valueOf(param.get("pintuanId").toString()));
        refundUnformedMarks(expireMarkIdList);

        // 删除拼团基础信息
        this.baseMapper.deleteById(param.get("pintuanId").toString());
        // 删除拼团详情信息
        activityPiecingTogetherDetailMapper.delete(new LambdaQueryWrapper<ActivityPiecingTogetherDetailEntity>()
                                                .eq(ActivityPiecingTogetherDetailEntity::getPintuanId,param.get("pintuanId").toString()));

        // 若拼团信息存在，则更新相关商品的促销状态
        // 查询参与此限时折扣的商品
        if (null != result) {
            List<Integer> commonList = Lists.newArrayList();
            commonList.add(result.getCommonId());
            CommonIsXianUpdateDTO commonIsXianUpdateParam = new CommonIsXianUpdateDTO();
            // 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
            commonIsXianUpdateParam.setCommonIdList(commonList);
            commonIsXianUpdateParam.setCommonIsXian(0);
            if (!CollectionUtils.isEmpty(commonList) && commonList.size() > 0) {
                goodsServiceClient.updateCommonIsXian3(commonIsXianUpdateParam);
            }
        }

        // 返回删除成功的响应结果
        return ResponseResult.success(BizCodeEnume.DELETE_SUCCESS);
    }

    /**
     * 拼团失败 / 到期未成团的统一退款处置（迁新 Mall 收银台）。
     * <p>流程：团态置成团失败(2) → 触发 order 侧逐笔 Mall 退款 → 成功后
     * 将已付款名额 temp.pay_start 置已退款(2)、团态置已完成退款(3)。
     * <p>幂等：团级 Redis 锁防并发/重复触发；order 侧再按订单状态二次幂等，未付款/已退款自动跳过。
     *
     * @param expireMarkIdList 未成团（等待成团态）的 mark_id 列表
     */
    private void refundUnformedMarks(List<String> expireMarkIdList) {
        if (CollectionUtils.isEmpty(expireMarkIdList)) {
            return;
        }
        // 团态置成团失败：0等待成团,1已成团,2成团失败,3已完成退款
        ExpireMarkIdUpdateDTO failParam = new ExpireMarkIdUpdateDTO();
        failParam.setExpireMarkIdList(expireMarkIdList);
        failParam.setStatus(2);
        activityPiecingTogetherMarkMapper.updateMarkStatus(failParam);

        // 根据标记ID查询相关订单ID，交给 order 侧逐笔退款
        List<String> orderIdList = activityPiecingTogetherTempMapper.selectByMarkId2(expireMarkIdList);
        if (CollectionUtils.isEmpty(orderIdList)) {
            return;
        }
        failParam.setOrderIdList(orderIdList);

        // 幂等：整团退款锁，避免并发或重复触发退款
        String lockKey = "pintuan:refund:mark:" + String.join(",", expireMarkIdList);
        if (redisCache.hasKey(lockKey)) {
            return;
        }
        redisCache.setCacheObject(lockKey, 1, 60, TimeUnit.MINUTES);
        try {
            orderServiceClient.checkGroupBookingStatus(failParam);
            // 退款发起成功后：temp 已付款名额置已退款(2)，团态置已完成退款(3)
            for (String orderId : orderIdList) {
                activityPiecingTogetherTempMapper.markTempRefundedByOrder(orderId);
            }
            ExpireMarkIdUpdateDTO doneParam = new ExpireMarkIdUpdateDTO();
            doneParam.setExpireMarkIdList(expireMarkIdList);
            doneParam.setStatus(3);
            activityPiecingTogetherMarkMapper.updateMarkStatus(doneParam);
        } catch (Exception e) {
            redisCache.deleteObject(lockKey);
            LOG.error("拼团失败退款处置异常 expireMarkIdList={}", expireMarkIdList, e);
        }
    }

    /**
     * 启停拼团活动。status=1 启用；status=0 停用。
     * 启用时若当前在活动期内则标为可用并挂促销位；停用时清促销位。
     */
    @Override
    public ResponseResult updatePinTuanBaseStatus(Map<String, Object> param) {
        if (param == null || param.get("pintuanId") == null || param.get("status") == null) {
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
        }
        Integer pintuanId;
        Integer status;
        try {
            pintuanId = Integer.valueOf(String.valueOf(param.get("pintuanId")));
            status = Integer.valueOf(String.valueOf(param.get("status")));
        } catch (NumberFormatException e) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "pintuanId 或 status 非法");
        }
        if (status != 0 && status != 1) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "status 仅支持 0/1");
        }

        ActivityPiecingTogetherEntity entity = this.baseMapper.selectById(pintuanId);
        if (entity == null) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "拼团活动不存在");
        }

        Integer nextStatus = status;
        Integer commonIsXian = 0;
        if (status == 1) {
            Date now = new Date();
            if (entity.getStartTime() != null && entity.getEndTime() != null
                    && DateUtils.belongCalendar(now, entity.getStartTime(), entity.getEndTime())) {
                nextStatus = 1;
            } else if (entity.getStartTime() != null && now.before(entity.getStartTime())) {
                nextStatus = 2;
            } else {
                nextStatus = 1;
            }
            commonIsXian = 4;
        }

        this.update(new LambdaUpdateWrapper<ActivityPiecingTogetherEntity>()
                .eq(ActivityPiecingTogetherEntity::getPintuanId, pintuanId)
                .set(ActivityPiecingTogetherEntity::getStatus, nextStatus)
                .set(ActivityPiecingTogetherEntity::getUpdateTime, new Date()));

        if (entity.getCommonId() != null) {
            CommonIsXianUpdateDTO commonIsXianUpdateParam = new CommonIsXianUpdateDTO();
            commonIsXianUpdateParam.setCommonIdList(Lists.newArrayList(entity.getCommonId()));
            commonIsXianUpdateParam.setCommonIsXian(commonIsXian);
            try {
                goodsServiceClient.updateCommonIsXian3(commonIsXianUpdateParam);
            } catch (Exception e) {
                // 促销位同步失败不阻断启停
            }
        }
        return ResponseResult.success(nextStatus);
    }

    /**
     * 根据传入的参数查询拼团基础信息列表。
     *
     * @param param 一个Map对象，包含查询所需的参数，其中必须包含token信息。
     * @return 返回查询结果的ResponseResult对象。如果token无效，返回失败结果。
     */
    @Override
    public ResponseResult queryPinTuanBaseList(Map<String, Object> param) {
        // 通过token获取用户信息
        UserInfoVO userInfoVO = ParseTokenUtils.getUserInfoByToken(String.valueOf(param.get("token")));

        // 检查用户信息是否有效
        if (userInfoVO != null) {
            // 将用户所属店铺ID添加到查询参数中；平台超管不绑店铺 → 不加条件，看全部店铺拼团
            if (null != userInfoVO.getShopId() && !"".equals(userInfoVO.getShopId())) {
                param.put("shopId", userInfoVO.getShopId());
            }

            // 执行查询，获取拼团基础信息的分页结果
            IPage<ActivityPiecingTogetherEntity> page = this.baseMapper.selectPinTuanBaseList(
                new Query<ActivityPiecingTogetherEntity>().getPage(param), param);

            // 构建并返回查询成功的响应结果
            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            // 构建并返回token无效的响应结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    /**
     * 查询拼团详情
     * @param param 查询参数，包含拼团标记ID和用户令牌
     * @return 返回拼团详情的响应结果
     */
    @Override
    public ResponseResult queryDetail(JoinPintuanParamVO param) {
        JoinPintuanDetailDTO joinPintuanDetail = null;
        // 查询参与拼团的买家信息
        // 查询购买者信息
        List<PintuanBuyerMassageDTO> buyerMessage = this.baseMapper.selectBuyerMassage(param.getMarkId());
        // 查询当前用户是否加入了拼团
        // 查询拼团详情
        joinPintuanDetail = this.baseMapper.selectUserIsJoin(param);

        if (joinPintuanDetail != null) {
            // 校验拼团是否超时
            // 跳转拼团详情页
            Long overplusTime = DateUtils.getPinTuanOverplusTime(joinPintuanDetail.getCreatedTime(),
                    joinPintuanDetail.getStartHour(), joinPintuanDetail.getEndTime());
            if (overplusTime < 0) {
                return ResponseResult.fail(0, "没有正在拼团的商品，请返回重新拼团");
            }

            // 检查用户是否已领取优惠券
            String redisKey = HouConfig.HOU_USER_TOKEN_USER + param.getToken();
            if (redisCache.hasKey(redisKey)) {
                UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
                String userId = userInfo.getUserId();
                // 查询用户是否已领取过该拼团的优惠券
                Integer count = Math.toIntExact(activityPiecingTogetherTempMapper.selectCount(new LambdaQueryWrapper<ActivityPiecingTogetherTempEntity>()
                        .eq(ActivityPiecingTogetherTempEntity::getUserId, userId)
                        .eq(ActivityPiecingTogetherTempEntity::getMarkId, param.getMarkId())));
                if (count == null || count == 0) {
                    joinPintuanDetail.setCheckIsSelfMark(0);
                } else {
                    joinPintuanDetail.setCheckIsSelfMark(1);
                }
            }
            joinPintuanDetail.setOverplusTime(overplusTime);

            // 隐藏拼团买家的姓名信息，保护隐私
            for (PintuanBuyerMassageDTO pintuanBuyerMassage : buyerMessage) {
                String userName = pintuanBuyerMassage.getUserName();
                userName = userName.substring(0, 1) + "****" + userName.substring(userName.length() - 1, userName.length());
                pintuanBuyerMassage.setUserName(userName);
            }
            joinPintuanDetail.setBuyerMessage(buyerMessage);
        } else {
            return ResponseResult.fail(0, "没有正在拼团的商品，请返回重新拼团");
        }

        return ResponseResult.success(joinPintuanDetail);
    }
    /**
     * 根据参数查询拼团活动列表。
     *
     * @param param 查询参数，包含拼团通用ID等信息。
     * @return 返回拼团活动列表的响应结果。
     */
    @Override
    public ResponseResult queryList(Map<String, Object> param) {
        // 从参数中提取拼团通用ID
        Integer commonId = Integer.valueOf(param.get("commonId").toString());
        // 根据拼团通用ID查询对应的拼团ID
        Integer pintuanId = this.baseMapper.selectPintuanByCommonId(commonId);
        // 将拼团ID添加到查询参数中
        param.put("pintuanId", pintuanId);

        // 根据参数查询拼团活动列表，并分页
        IPage<PintuanListResultVO> page = this.baseMapper.selectPintuanList(new Query<PintuanListResultVO>().getPage(param), param);

        // 如果查询结果为空，返回空页面信息
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            // 遍历查询结果，处理每个拼团活动的信息
            for (PintuanListResultVO pintuanListResult : page.getRecords()) {
                // 计算拼团结束剩余时间
                long pinTuanOverplusTime = DateUtils.getPinTuanOverplusTime(pintuanListResult.getCreatedTime(),
                        pintuanListResult.getStartHour(), pintuanListResult.getEndTime());
                pintuanListResult.setOverplusTime(pinTuanOverplusTime);
                // 对用户名进行脱敏处理
                // 处理用户名
                String userName = pintuanListResult.getUserName();
                userName = userName.substring(0, 1) + "****"
                        + userName.substring(userName.length() - 1, userName.length());
                pintuanListResult.setUserName(userName);

                // 判断当前用户是否参团
                // 判断是否自己的团
                String redisKey = HouConfig.HOU_USER_TOKEN_USER + param.get("token").toString();
                if (redisCache.hasKey(redisKey)) {
                    UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
                    String userId = userInfo.getUserId();
                    // 查询当前用户是否参团
                    Integer count = Math.toIntExact(activityPiecingTogetherTempMapper.selectCount(new LambdaQueryWrapper<ActivityPiecingTogetherTempEntity>()
                            .eq(ActivityPiecingTogetherTempEntity::getUserId, userId)
                            .eq(ActivityPiecingTogetherTempEntity::getMarkId, pintuanListResult.getMarkId())));
                    if (!count.equals(0)) {
                        pintuanListResult.setCheckIsSelfMark(1);
                    } else {
                        pintuanListResult.setCheckIsSelfMark(0);
                    }
                } else {
                    pintuanListResult.setCheckIsSelfMark(0);
                }
            }
        }
        // 返回处理后的拼团活动列表页面信息
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 通过扫小程序二维码 获取类型参数 根据参数从redis中获取具体参数
     */
    @Override
    public ResponseResult getParameterByToken(Map<String, Object> param) {
        try {
            // 从参数映射中提取二维码类型和类型ID
            String orCodeType = (String) param.get("orCodeType");
            String orCodeTypeId = (String) param.get("orCodeTypeId");

            // 根据二维码类型和类型ID拼接键名，并从Redis中获取对应的数据
            String jsonstr = (String) redisCache.getCacheObject(orCodeType + "_" + orCodeTypeId);

            // 如果数据存在，则将其解析为Map并返回成功结果，包含解析出的数据
            if(null != jsonstr){
                Map maps = (Map) JSON.parse(jsonstr);
                return ResponseResult.success(maps);
            }
            // 如果数据不存在，返回空的成功结果
            return ResponseResult.success();
        } catch (Exception e) {
            // 捕获任何异常，打印堆栈跟踪，并返回失败结果，包含异常消息
            e.printStackTrace();
            return ResponseResult.fail(e.getMessage());
        }

    }

}

