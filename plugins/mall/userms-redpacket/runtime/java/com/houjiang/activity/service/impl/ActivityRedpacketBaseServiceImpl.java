package com.houjiang.activity.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.activity.dto.*;
import com.houjiang.activity.entity.ActivityRedpacketBaseEntity;
import com.houjiang.activity.feign.UserServiceClient;
import com.houjiang.activity.mapper.ActivityRedpacketBaseMapper;
import com.houjiang.activity.mapper.ActivityRedpacketTemplateMapper;
import com.houjiang.activity.service.ActivityRedpacketBaseService;
import com.houjiang.activity.vo.AvaliableRpBaseResultVO;
import com.houjiang.activity.vo.CheckReceivedRpOrNotResultVO;
import com.houjiang.activity.vo.RedpacketDetailVO;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.util.*;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.activity.RedTemplateEntityVO;
import com.houjiang.commons.vo.activity.HouRedpacketBaseVO;
import com.houjiang.commons.vo.activity.HouRedpacketTemplateVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName ActivityRedpacketBaseServiceImpl
 * @Description 红包表
 * @Author Amos
 * @Date 2023-05-23 16:42:52
 * @Version 1.0
 * @author Amos
 */
@Service
public class ActivityRedpacketBaseServiceImpl extends ServiceImpl<ActivityRedpacketBaseMapper, ActivityRedpacketBaseEntity>
        implements ActivityRedpacketBaseService {

    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private ActivityRedpacketTemplateMapper activityRedpacketTemplateMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 根据用户Token查询用户是否收到红包。
     * 该方法主要用于验证用户是否有未领取的红包，如果存在未领取的红包，则返回成功结果，否则返回失败结果。
     *
     * @param param 用户查询红包信息的参数，包含用户Token等信息。
     * @return 如果用户有未领取的红包，则返回成功结果，包含提示信息；如果用户没有未领取的红包，则返回失败结果，包含错误码和错误信息。
     */
    @Override
    public ResponseResult checkReceivedRpOrNot(UserAvailableRedpacketQueryDTO param) {
        // 通过Token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        Boolean type = false;
        // 查询平台 有效红包 排除 plus 和 新人红包
        List<Integer> redpacketTIdList = activityRedpacketTemplateMapper.selectredtemplateId();
        for (Integer redpacketId : redpacketTIdList) {
            // 查询当前用户是否已经领取过该红包
            Integer cout = this.baseMapper.checkReceivedRpOrNot(redpacketId,userInfo.getUserId());
            if(cout == 0){
                type = true;
                break;
            }
        }
        // 没有未领取的红包，返回失败结果
        if (!type) {
            return ResponseResult.fail(BizCodeEnume.NO_RED_ENVELOPES.getCode(), BizCodeEnume.NO_RED_ENVELOPES.getMsg());
        }

        // 如果未领取红包的数量不为0，表示有未领取的红包，返回成功结果
        return ResponseResult.success("有可领取红包");
    }

    /**
     * 插入所有可用红包
     *
     * @param paraMap 参数映射，包含必要的红包信息和用户信息
     * @return 返回插入红包的数量
     */
    @Override
    public ResponseResult insertAllAvaliable(Map<String, String> paraMap) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));
        // 查询所有可领取的红包模板
        List<HouRedpacketTemplateVO> houRedpacketTemplates = this.baseMapper.selectRedpacketTem(paraMap);
        // 初始化领取红包的数量
        Integer amount = 0;

        // 遍历所有红包模板，尝试领取非新人红包
        // 领取所有非新人红包
        for (HouRedpacketTemplateVO houRedpacketTemplate : houRedpacketTemplates) {
            // 检查该红包是否已被领取
            // 查询红包是否领取过
            Map<String, String> checkMap = new HashMap<>();
            checkMap.put("redpacketOwnerId", userInfo.getUserId());
            checkMap.put("redpacketTId", houRedpacketTemplate.getRedpacketTId().toString());
            List<String> redpacketIdList = this.baseMapper.checkReceivedRedpacketOrNot(checkMap);
            // 如果未领取，则插入领取记录，并更新红包领取状态
            // 如果未领取则可以领取
            if(CollectionUtils.isEmpty(redpacketIdList)){
                // 准备插入红包领取信息的数据
                Map<String, Object> insertMap = new HashMap<>();
                insertMap.put("redpacketOwnerId", userInfo.getUserId());
                insertMap.put("redpacketOwnerName", userInfo.getUserName());
                insertMap.put("redpacketTId", houRedpacketTemplate.getRedpacketTId());
                insertMap.put("redpacketTitle", houRedpacketTemplate.getRedpacketTTitle());
                insertMap.put("redpacketDesc", houRedpacketTemplate.getRedpacketTDesc());
                insertMap.put("redpacketStartDate", houRedpacketTemplate.getRedpacketTStartDate());
                insertMap.put("redpacketEndDate", houRedpacketTemplate.getRedpacketTEndDate());
                insertMap.put("redpacketPrice", houRedpacketTemplate.getRedpacketTPrice());
                insertMap.put("shopLimit", houRedpacketTemplate.getShopLimit());
                insertMap.put("goodsCatLimit", houRedpacketTemplate.getGoodsCatLimit());
                insertMap.put("redpacketTOrderlimit", houRedpacketTemplate.getRedpacketTOrderlimit());
                insertMap.put("redpacketFrom", 1);
                // 插入红包领取信息
                Integer torf = this.baseMapper.insertyfRedpacketBase(insertMap);
                // 累加领取数量
                amount = amount + torf;
                // 更新红包已领取数量
                // 增加红包领取数量
                Map<String, String> updateMap = new HashMap<String, String>();
                updateMap.put("redpacketTId", houRedpacketTemplate.getRedpacketTId().toString());
                this.baseMapper.updateRedpGiveout(updateMap);
            }
        }

        // 如果用户是新人，尝试领取新人红包
        // 新人则继续领取
        if ("1".equals(userInfo.getUserIsNew())) {
            // 查询所有可领取的新人红包模板
            List<HouRedpacketTemplateVO> newRedpacketTems = this.baseMapper.selectNewRedpacketTem(paraMap);
            // 遍历新人红包模板，领取红包
            for (HouRedpacketTemplateVO houRedpacketTemplate : newRedpacketTems) {
                // 准备插入红包领取信息的数据
                Map<String, Object> insertMap = new HashMap<String, Object>();
                insertMap.put("redpacketOwnerId", userInfo.getUserId());
                insertMap.put("redpacketOwnerName", userInfo.getUserName());
                insertMap.put("redpacketTId", houRedpacketTemplate.getRedpacketTId());
                insertMap.put("redpacketTitle", houRedpacketTemplate.getRedpacketTTitle());
                insertMap.put("redpacketDesc", houRedpacketTemplate.getRedpacketTDesc());
                insertMap.put("redpacketStartDate", houRedpacketTemplate.getRedpacketTStartDate());
                insertMap.put("redpacketEndDate", houRedpacketTemplate.getRedpacketTEndDate());
                insertMap.put("redpacketPrice", houRedpacketTemplate.getRedpacketTPrice());
                insertMap.put("shopLimit", houRedpacketTemplate.getShopLimit());
                insertMap.put("goodsCatLimit", houRedpacketTemplate.getGoodsCatLimit());
                insertMap.put("redpacketTOrderlimit", houRedpacketTemplate.getRedpacketTOrderlimit());
                insertMap.put("redpacketFrom", 1);
                // 插入红包领取信息
                Integer torf = this.baseMapper.insertyfRedpacketBase(insertMap);
                // 累加领取数量
                amount = amount + torf;
                // 更新红包已领取数量
                // 增加红包领取数量
                Map<String, String> updateMap = new HashMap<String, String>();
                updateMap.put("redpacketTId", houRedpacketTemplate.getRedpacketTId().toString());
                this.baseMapper.updateRedpGiveout(updateMap);
            }
            // 如果有领取新人红包，则更新用户不再为新人
            if (!CollectionUtils.isEmpty(newRedpacketTems)) {
                // 更新是否是新人
                userServiceClient.updateIsNew(paraMap);
            }
        }

        // 返回领取红包的总数量
        return ResponseResult.success(amount);
    }


    /**
     * 根据传入的参数获取红包详情信息。
     *
     * @param paraMap 包含查询红包详情所需的参数的映射。
     *                 参数的具体含义和使用依赖于底层Mapper的实现。
     * @return HouRedpacketBaseVO 类型的对象，包含了查询到的红包的详细信息。
     *         如果查询不到相关数据，返回值可能为null。
     */
    @Override
    public HouRedpacketBaseVO getRedDetail(Map<String, String> paraMap) {
        // 通过baseMapper查询红包详情信息
        HouRedpacketBaseVO redpDetail = this.baseMapper.getRedDetail(paraMap);
        // 返回查询到的红包详情信息
        return redpDetail;
    }

    /**
     * 根据用户Token查询我的红包信息。
     *
     * @param param 包含用户Token和红包状态等信息的请求参数对象。
     * @return 返回查询到的红包信息的结果对象。
     * @throws DefinedException 如果用户Token无效，则抛出定义好的异常。
     */
    @Override
    public ResponseResult selectMyRedPacket(UserAvailableRedpacketDTO param) {
        // 从Redis中根据用户Token获取用户信息字符串
        String userString = (String) redisCache.getCacheObject(HouConfig.HOU_USER_TOKEN_USER + param.getToken());
        // 校验用户Token是否有效，如果无效则抛出异常
        if (userString == null || "".equals(userString)) {
            throw new DefinedException(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }

        // 将用户信息字符串反序列化为UserInfoDTO对象
        // 获取用户信息
        UserInfoDTO dto = JSON.parseObject(userString, UserInfoDTO.class);
        // 设置红包的拥有者ID
        param.setRedpacketOwnerId(dto.getUser().getUserId());
        // 获取并设置红包的状态
        // (1-未用,2-已用,3-过期))
        Integer redpacketTState = param.getRedpacketState();
        param.setRedpacketState(redpacketTState);

        // 将请求参数对象转换为Map，用于后续的分页查询
        // 分页
        // 将对象转换为map
        Map data = null;
        try {
            data = ParseMapUtils.beanToMap(param);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // 执行分页查询，获取红包列表
        // 分页查询数据
        IPage<ActivityRedpacketBaseEntity> page = this.baseMapper.queryBrandList(new Query<ActivityRedpacketBaseEntity>().getPage(data), param);
        // 构建并返回查询结果的成功响应
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 根据ID列表查询红包模板信息。
     * 通过调用基本Mapper接口，查询指定ID列表对应的红包模板详情。
     * 此方法旨在提供对红包模板数据库查询的封装，以便上层业务逻辑可以方便地获取红包模板信息。
     *
     * @param id 红包模板ID的列表，用于指定查询的模板范围。
     * @return 返回查询到的红包模板详情列表。
     */
    @Override
    public List<HouRedpacketTemplateVO> selectRedPacket(List<Integer> id) {
        List<HouRedpacketTemplateVO> redpDetail = this.baseMapper.selectRedPacket(id);
        return redpDetail;
    }

    /**
     * 插入红包信息到数据库。
     * 通过调用基本Mapper接口的插入方法，将包含红包信息的Map对象插入到数据库中。
     * 此方法用于处理新红包的添加操作，确保红包信息正确存储以便后续查询和使用。
     *
     * @param insertMap 包含红包信息的Map对象，其中键值对代表了红包的各个属性及其对应的值。
     * @return 返回操作结果，成功则返回带有成功标识的ResponseResult对象。
     */
    @Override
    public ResponseResult insertRedPacket(Map<String, Object> insertMap) {
        this.baseMapper.insertRedPacket(insertMap);
        return ResponseResult.success();
    }

    /**
     * 更新红包发放信息。
     * 通过传递一个包含更新信息的Map对象，来更新红包发放记录。
     *
     * @param updateMap 包含更新信息的键值对，键是字段名，值是新的字段值。
     * @return 返回一个表示操作成功的ResponseResult对象。
     */
    @Override
    public ResponseResult updateRedpGiveout(Map<String, String> updateMap) {
        this.baseMapper.updateRedpGiveout(updateMap);
        return ResponseResult.success();
    }

    /**
     * 根据红包ID列表查询红包模板信息。
     * 通过红包ID列表来查询相应的红包模板信息，返回一个包含这些信息的实体列表。
     *
     * @param redIdList 红包ID列表。
     * @return 返回一个包含查询到的红包模板信息的实体VO列表。
     */
    @Override
    public List<RedTemplateEntityVO> selectredtemplateId(List<Integer> redIdList) {
        return this.baseMapper.selectredtemplateId(redIdList);
    }

    /**
     * 根据红包ID列表和用户ID查询用户红包数量。
     * 通过红包ID列表和用户ID来查询特定用户拥有的红包数量。
     *
     * @param redIdList 红包ID列表。
     * @param userId 用户ID。
     * @return 返回该用户拥有的指定红包ID列表中的红包数量。
     */
    @Override
    public Integer selectuserred(List<Integer> redIdList, String userId) {
        return this.baseMapper.selectuserred(redIdList,userId);
    }

    /**
     * 插入红包基础信息。
     * 通过调用BaseMapper的insertRedpacketBase方法，将传入的红包信息参数插入到数据库中。
     * @param paraMap 包含红包基础信息的参数映射表。
     * @return 返回操作结果的ResponseResult对象，操作成功时，result字段为非零值。
     */
    @Override
    public ResponseResult insertRedpacketBase(Map<String, Object> paraMap) {
        Integer result=this.baseMapper.insertRedpacketBase(paraMap);
        return ResponseResult.success();
    }

    /**
     * 获取红包用户列表。
     * 根据传入的参数查询符合条件的红包用户列表，并分页返回查询结果。
     * @param paraMap 包含查询条件和分页信息的参数映射表，其中redpacketTId为红包主题ID。
     * @return 返回包含查询结果的ResponseResult对象，结果通过PageUtils进行包装。
     */
    @Override
    public ResponseResult userList(Map<String, Object> paraMap) {
        IPage<ActivityRedpacketBaseEntity> page=this.page(new Query<ActivityRedpacketBaseEntity>().getPage(paraMap),
                new LambdaQueryWrapper<ActivityRedpacketBaseEntity>()
                                        .eq(ActivityRedpacketBaseEntity::getRedpacketTId, Integer.valueOf(paraMap.get("redpacketTId").toString()))
                                        .orderByDesc(ActivityRedpacketBaseEntity::getRedpacketActiveDate));

        return ResponseResult.success(PageUtils.getPage(page));
    }

/**
 * 查询红包详情列表，并导出为Excel文件。
 *
 * @param param 查询参数，包含红包的各种条件过滤信息。
 * @param response HTTP响应，用于导出Excel时设置响应头信息。
 * @return 返回查询结果的URL，用于下载导出的Excel文件。
 * @throws IOException 如果导出文件时发生IO错误。
 */
@Override
public ResponseResult DetaillistExp(Map<String, Object> param, HttpServletResponse response) throws IOException {
    // 格式化当前日期时间，用于生成红包导出文件名。
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMddHHmmss");
    String finishTime = dateformat.format(new Date());
    // 初始化数据集，用于存储查询结果的映射表，便于导出Excel。
    List<Map<String, String>> dataset = Lists.newArrayList();
    // 判断是否指定了红包所有者名称。
    boolean flag=param.containsKey("redpacketOwnerName");
    if(flag){
        flag=StringUtils.isNotEmpty(param.get("redpacketOwnerName").toString());
    }
    // 根据查询参数，查询红包详情列表。
    List<ActivityRedpacketBaseEntity> detail =this.baseMapper.selectList(new LambdaQueryWrapper<ActivityRedpacketBaseEntity>()
            .eq(param.containsKey("redpacketTId")&&param.get("redpacketTId")!="",ActivityRedpacketBaseEntity::getRedpacketTId,param.get("redpacketTId").toString())
            .le(param.containsKey("finishTime"),ActivityRedpacketBaseEntity::getRedpacketEndDate,param.containsKey("finishTime")?param.get("finishTime").toString():null)
            .ge(param.containsKey("startTime"),ActivityRedpacketBaseEntity::getRedpacketEndDate,param.containsKey("startTime")?param.get("startTime").toString():null)
            .like(flag,ActivityRedpacketBaseEntity::getRedpacketOwnerName,flag?param.get("redpacketOwnerName").toString():null)
            .eq(param.containsKey("redpacketState"),ActivityRedpacketBaseEntity::getRedpacketState,param.containsKey("redpacketState")?param.get("redpacketState").toString():null));
    // 遍历查询结果，格式化每个红包的详细信息，并添加到数据集中。
    for (ActivityRedpacketBaseEntity res : detail) {
        HashMap<String, String> map = Maps.newHashMap();
        // 根据红包状态，设置相应的描述。
        if (res.getRedpacketState() != null) {
            switch (res.getRedpacketState()) {
                case 1:
                    map.put("红包状态","未使用");
                    break;
                case 2:
                    map.put("红包状态","已使用");
                    break;
                case 3:
                    map.put("红包状态","已过期");
                    break;
            }
        }
        // 设置红包的其他详细信息。
        map.put("红包标题",res.getRedpacketTitle());
        map.put("红包有效期开始时间",DateUtils.format(res.getRedpacketStartDate(),"yyyy-MM-dd HH:mm:ss"));
        map.put("红包有效期结束时间",DateUtils.format(res.getRedpacketEndDate(),"yyyy-MM-dd HH:mm:ss"));
        map.put("红包面额",res.getRedpacketPrice().toString());
        map.put("订单编号",res.getRedpacketOrderId());
        map.put("所有者名称",res.getRedpacketOwnerName());
        map.put("红包领取时间",DateUtils.format(res.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
        dataset.add(map);
    }
    // 定义导出Excel的列头。
    String[] headers = {"红包标题","红包有效期开始时间","红包有效期结束时间","红包面额","红包状态","订单编号","所有者名称","红包领取时间"};
    // 初始化导出的数据映射，将数据集放入其中。
    // excel文件名
    Map<String, List<Map<String, String>>> map = Maps.newHashMap();
    map.put("红包使用详情", dataset);
    // 调用工具类生成Excel文件，并返回下载链接。
    String url = ExcelsUtil.createExcel(headers, map, new int[]{0});
    return ResponseResult.success(url);
}

    /**
     * 查询红包详情模板列表
     *
     * @param param 查询参数，包含页码、令牌等信息
     * @return 返回查询结果，如果令牌无效，则返回失败响应
     */
    @Override
    public ResponseResult listRedpacketDetailTemp(Map<String, Object> param) {
        // 检查令牌是否存在于Redis中，以验证令牌的有效性
        if (redisCache.hasKey(HouConfig.HOU_PLATFORM_TOKEN_USER + String.valueOf(param.get("token")))) {
            // 根据查询参数和分页信息，查询红包详情模板列表
            IPage<RedpacketDetailVO> page = this.baseMapper.querylistRedpacketDetailTemp(
                    new Query<RedpacketDetailVO>().getPage(param), param);
            // 返回查询结果的成功响应
            return ResponseResult.success(PageUtils.getPage(page));
        } else {
            // 令牌无效，返回失败响应
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    /**
     * 根据用户条件查询可领取的红包列表
     * @param param 查询条件，包括商品ID、用户令牌等
     * @return 返回查询结果，包含用户可领取的红包信息
     */
    @Override
    public ResponseResult queryList(UserAvailableRedpacketQueryDTO param) {
        // 获取商品ID
        Integer goodsId = param.getGoodsId();

        // 初始化符合条件的红包列表
        List<HouRedpacketTemplateVO> newRpList = Lists.newArrayList();
        // 初始化是否满足领取条件的标志
        Integer receiveOrNot = 0;
        boolean flag = false;

        // 获取用户令牌
        String token = param.getToken();
        // 根据令牌获取用户信息
        // 获取用户信息
        UserInfoVO userInfo = null;

        if (StringUtils.isNotBlank(token)) {
            userInfo = ParseTokenUtils.getUserInfoByToken(token);
            // 设置红包所有人ID
            param.setRedpacketOwnerId(userInfo.getUserId());
        }

        // 查询用户已领取的红包状态
        List<CheckReceivedRpOrNotResultVO> receivedRpList = this.baseMapper.checkReceivedRpOrNot2(param);
        // 查询所有红包模板信息
        List<HouRedpacketTemplateVO> houRedpacketTemplates = this.baseMapper.selectRedpacketTemList(param);

        // 遍历红包模板信息，筛选出用户可领取的红包
        for (HouRedpacketTemplateVO houRedpacketTemplate : houRedpacketTemplates) {
            // 获取红包类型
            Integer redpacketTType = houRedpacketTemplate.getRedpacketTType();

            // 判断是否需要根据用户领取状态进行判断
            if (StringUtils.isNotBlank(token)) {
                // 遍历已领取的红包状态，判断当前红包是否可领取
                // 判断是否
                for (CheckReceivedRpOrNotResultVO receivedRp : receivedRpList) {
                    Integer redpacketState = receivedRp.getRedpacketState();
                    // 根据红包ID匹配
                    if (houRedpacketTemplate.getRedpacketTId().intValue() == receivedRp.getRedpacketTId().intValue()) {
                        // 根据红包类型和用户状态确定是否可领取
                        // 判断是否是新人红包，如果是则根据userIsNew判断是否是新人
                        if (houRedpacketTemplate.getRedpacketTType().equals(CommonType.redpacketTType.NEW_PERSON_REGISTRATION
                                .getCode())
                                || houRedpacketTemplate.getRedpacketTType().equals(CommonType.redpacketTType.MULTI_PRODUCT_NEWCOMERS
                                .getCode())) {
                            // 根据用户是否是新人确定领取状态
                            // 0未领取1已领取2新人专享3已用4过期
                            if ("0".equals(userInfo.getUserIsNew())) {
                                // 根据红包状态确定领取状态
                                if (null == redpacketState || redpacketState.intValue() == 1) {
                                    receiveOrNot = CommonType.PromotionReceiveOrNot.IMMEDIATELY.getCode();
                                } else if (redpacketState.intValue() == 2) {
                                    receiveOrNot = CommonType.PromotionReceiveOrNot.ALREADY_USE.getCode();
                                } else if (redpacketState.intValue() == 3) {
                                    receiveOrNot = CommonType.PromotionReceiveOrNot.EXPIRE.getCode();
                                } else {
                                    receiveOrNot = CommonType.PromotionReceiveOrNot.NEW_VIP.getCode();
                                }
                            } else {
                                receiveOrNot = CommonType.PromotionReceiveOrNot.NO_RECEIVE.getCode();
                            }

                        } else {
                            // 根据红包状态确定是否可领取
                            // 如果未领取则可以领取 红包状态(1-未用,2-已用,3-过期
                            // ReceiveOrNot:0未领取1立即使用2不可领取3已用4过期
                            if (null == redpacketState) {
                                receiveOrNot = CommonType.PromotionReceiveOrNot.NO_RECEIVE.getCode();
                            } else if (redpacketState.intValue() == 2) {
                                receiveOrNot = CommonType.PromotionReceiveOrNot.ALREADY_USE.getCode();
                            } else if (redpacketState.intValue() == 3) {
                                receiveOrNot = CommonType.PromotionReceiveOrNot.EXPIRE.getCode();
                            } else {
                                receiveOrNot = CommonType.PromotionReceiveOrNot.IMMEDIATELY.getCode();
                            }
                        }
                        // 设置红包领取状态
                        houRedpacketTemplate.setReceiveOrNot(receiveOrNot);
                        flag = true;
                        break;
                    }
                }
            } else {
                flag = true;
            }

            // 根据红包类型和商品ID确定是否满足领取条件
            if (CommonType.redpacketTType.NEW_PERSON_REGISTRATION.getCode().equals(redpacketTType)
                    || CommonType.redpacketTType.ORDINARY.getCode().equals(redpacketTType)
                    || CommonType.redpacketTType.EXCLUSIVE.getCode().equals(redpacketTType)) {
                flag = true;
            } else {
                // 多品红包类型判断
                // 多品红包类型：1多品配置2专区
                Integer manyRedpacketType = houRedpacketTemplate.getManyRedpacketType();
                if (manyRedpacketType.equals(1)) {
                    // 根据商品ID判断是否满足领取条件
                    String goodsCatLimit = houRedpacketTemplate.getGoodsCatLimit();
                    if (StringUtils.isNotEmpty(goodsCatLimit)) {
                        List<Integer> goodsCatLimitList = Arrays
                                .asList((Integer[]) ConvertUtils.convert(goodsCatLimit.split(","), Integer.class));
                        if (goodsCatLimitList.contains(goodsId)) {
                            flag = true;
                        } else {
                            flag = false;
                        }
                    }
                } else {
                    flag = false;
                }
            }

            // 符合条件的红包添加到结果列表
            if (flag) {
                newRpList.add(houRedpacketTemplate);
            }
        }

        // 返回查询结果
        return ResponseResult.success(newRpList);

    }

    /**
     * 接收红包相关信息并处理领取逻辑。
     *
     * @param paraMap 包含红包领取所需参数的映射，如token、redpacketTId、macAddress等。
     * @return 返回处理结果的ResponseResult对象。
     */
    @Override
    public ResponseResult receive(Map<String, String> paraMap) {
        ResponseResult result = new ResponseResult();
        // 获取Jedis实例，用于Redis操作
        // 获取jedis实例
        Jedis jedis = RedisUtil.getJedis();
        RedisLock redisLock = null;
        try {
            // 通过token解析获取用户信息
            UserInfoVO userInfo =  ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));
            // 用户ID转换为字符串
            String redpacketOwnerId = userInfo.getUserId().toString();
            // 获取红包交易ID
            String redpacketTId = paraMap.get("redpacketTId");

            // 创建Redis锁，用于保证并发下的数据一致性
            redisLock = new RedisLock(jedis, "user-rp-" + redpacketOwnerId + redpacketTId, 10000);
            // 尝试获取锁，如果成功，则处理红包插入逻辑
            // 用户锁
            if (redisLock.lock()) {
                result = this.insertRpBase(paraMap, userInfo);
            } else {
                // 返回红包领取失败信息
                return ResponseResult.fail(0, "红包领取失败");
            }
            // 返回处理结果
            return result;
        } catch (InterruptedException e) {
            // 如果发生中断异常，返回红包领取失败信息
            return ResponseResult.fail(0, "红包领取失败");
        } finally {
            // 释放Redis锁
            redisLock.unLock();
            // 关闭Jedis资源
            jedis.close();
        }
    }
    /**
     * 发放红包的基础操作方法
     *
     * @param paraMap 包含红包相关参数的映射
     * @param userInfo 用户信息对象
     * @return 发放结果的响应对象
     */
    @Transactional
    public ResponseResult insertRpBase(Map<String, String> paraMap, UserInfoVO userInfo) {
        // 初始化用于校验的参数映射
        // 判断红包是否领取过
        Map<String, String> checkMap = new HashMap<String, String>();
        // 用户ID转换为字符串
        String redpacketOwnerId = userInfo.getUserId().toString();
        // 获取红包交易ID
        String redpacketTId = paraMap.get("redpacketTId");

        // 构建校验参数
        checkMap.put("redpacketOwnerId", redpacketOwnerId);
        checkMap.put("redpacketTId", redpacketTId);

        // 查询用户领取红包的限制次数
        // 查询该红包领取限制
        Integer redlimit = activityRedpacketTemplateMapper.checkReceivedRp(checkMap);

        // 检查是否达到领取限制
        // 判断红包是否到达领取限制
        Integer voucherId = this.baseMapper.checkReceivedRpOrNot1(checkMap);
        if (voucherId >= redlimit && redlimit != 0) {
            return ResponseResult.fail(0, "红包已到达领取上限");
        }

        // 根据交易ID查询红包模板信息
        HouRedpacketTemplateVO houRedpacketTemplate = activityRedpacketTemplateMapper.selectRpTemById(paraMap);

        // 检查红包是否处于可领取状态
        if (houRedpacketTemplate.getRedpacketIsBooking().intValue() == 2
                && !DateUtils.compareDate(houRedpacketTemplate.getRobRedpacketStartDate(),
                houRedpacketTemplate.getRobRedpacketEndDate())) {
            return ResponseResult.fail(0, "活动未开始，敬请期待！");
        }

        // 检查红包是否已被领完
        if (houRedpacketTemplate.getRedpacketTGiveout() >= houRedpacketTemplate.getRedpacketTTotal()) {
            return ResponseResult.fail(0, "红包已领完");
        }

        // 如果是新人且红包类型为注册红包或新人专享红包
        if (userInfo.getUserIsNew().equals("1") && (houRedpacketTemplate.getRedpacketTType().equals(1)
                || houRedpacketTemplate.getRedpacketTType().equals(5))) {
            // 查询符合条件的新人红包模板
            List<HouRedpacketTemplateVO> newRedpacketTems = this.baseMapper.selectNewRedpacketTem(paraMap);

            // 如果没有新人红包，则返回相应信息
            if (CollectionUtils.isEmpty(newRedpacketTems)) {
                return ResponseResult.fail(0, "没有新人红包");
            }

            // 遍历新人红包模板，发放红包
            for (HouRedpacketTemplateVO newRedpacketTem : newRedpacketTems) {
                // 检查红包是否还有剩余
                if (houRedpacketTemplate.getRedpacketTGiveout() < houRedpacketTemplate.getRedpacketTTotal()) {
                    // 构建插入红包基础信息的参数
                    Map<String, Object> insertMap = new HashMap<String, Object>();
                    // 设置红包相关参数
                    insertMap.put("redpacketOwnerId", userInfo.getUserId());
                    insertMap.put("redpacketOwnerName", userInfo.getUserName());
                    insertMap.put("redpacketTId", newRedpacketTem.getRedpacketTId());
                    insertMap.put("redpacketTitle", newRedpacketTem.getRedpacketTTitle());
                    insertMap.put("redpacketDesc", newRedpacketTem.getRedpacketTDesc());
                    insertMap.put("redpacketStartDate", newRedpacketTem.getRedpacketTStartDate());
                    insertMap.put("redpacketEndDate", newRedpacketTem.getRedpacketTEndDate());
                    insertMap.put("redpacketPrice", newRedpacketTem.getRedpacketTPrice());
                    insertMap.put("shopLimit", newRedpacketTem.getShopLimit());
                    insertMap.put("goodsCatLimit", newRedpacketTem.getGoodsCatLimit());
                    insertMap.put("redpacketTOrderlimit", newRedpacketTem.getRedpacketTOrderlimit());
                    // 插入红包基础信息
                    this.baseMapper.insertRedpacketBase(insertMap);

                    // 更新红包已发放数量
                    // 增加红包已领取数量
                    Map<String, String> updateMap = new HashMap<String, String>();
                    updateMap.put("redpacketTId", newRedpacketTem.getRedpacketTId().toString());
                    this.baseMapper.updateRedpGiveout(updateMap);
                }
            }

            // 尝试更新用户新人状态
            try {
                if (!CollectionUtils.isEmpty(newRedpacketTems)) {
                    // 更新是否是新人
                    userServiceClient.updateIsNewFeign(paraMap);
                }
            } catch (Exception e) {
                // 异常处理
            }
            // 返回领取成功的响应
            return ResponseResult.success("红包领取成功");
        }

        // 如果红包类型为注册红包或新人专享红包，返回领取失败信息
        // 红包类型，1-新人注册红包，2-普通红包，3-单品红包 4-店铺红包
        // ,5-单品新人红包 默认2
        if (houRedpacketTemplate.getRedpacketTType().intValue() == 1
                || houRedpacketTemplate.getRedpacketTType().intValue() == 5) {
            return ResponseResult.fail(0, "领取失败，新人专享");
        }

        // 构建插入红包基础信息的参数
        Map<String, Object> insertMap = new HashMap<String, Object>();
        // 设置红包相关参数
        insertMap.put("redpacketOwnerId", userInfo.getUserId());
        insertMap.put("redpacketOwnerName", userInfo.getUserName());
        insertMap.put("redpacketTId", houRedpacketTemplate.getRedpacketTId());
        insertMap.put("redpacketTitle", houRedpacketTemplate.getRedpacketTTitle());
        insertMap.put("redpacketDesc", houRedpacketTemplate.getRedpacketTDesc());
        insertMap.put("redpacketStartDate", houRedpacketTemplate.getRedpacketTStartDate());
        insertMap.put("redpacketEndDate", houRedpacketTemplate.getRedpacketTEndDate());
        insertMap.put("redpacketPrice", houRedpacketTemplate.getRedpacketTPrice());
        insertMap.put("shopLimit", houRedpacketTemplate.getShopLimit());
        insertMap.put("goodsCatLimit", houRedpacketTemplate.getGoodsCatLimit());
        insertMap.put("redpacketTOrderlimit", houRedpacketTemplate.getRedpacketTOrderlimit());

        // 插入红包基础信息
        Integer torf =  this.baseMapper.insertyfRedpacketBase(insertMap);
        // 更新红包已发放数量
        // 增加红包领取数量
        Map<String, String> updateMap = new HashMap<String, String>();
        updateMap.put("redpacketTId", houRedpacketTemplate.getRedpacketTId().toString());
        this.baseMapper.updateRedpGiveout(updateMap);

        // 如果插入失败，返回相应信息
        if (0 == torf) {
            return ResponseResult.fail(0, "插入红包失败");
        }
        // 返回领取成功的响应
        return ResponseResult.success("红包领取成功");
    }
    /**
     * 插入抽奖红包基础信息方法
     *
     * @param paraMap 包含红包相关参数的映射
     * @param userInfo 用户信息对象
     * @return 插入后的红包ID
     *
     * 该方法用于处理抽奖红包的插入操作。它首先检查红包是否已领取过，然后根据传入的参数和用户信息，
     * 构建插入红包的基础信息映射，并调用mapper进行插入操作。最后，更新红包的领取数量，并返回插入的红包ID。
     */
    public Integer insertSpinwinRpBase(Map<String, String> paraMap, UserInfoVO userInfo) {
        // 初始化用于检查的映射
        // 判断红包是否领取过
        Map<String, String> checkMap = new HashMap<String, String>();
        // 使用用户ID作为红包所有者ID
        String redpacketOwnerId = userInfo.getUserId().toString();
        // 从参数映射中获取红包交易ID
        String redpacketTId = paraMap.get("redpacketTId");

        // 构建检查映射
        checkMap.put("redpacketOwnerId", redpacketOwnerId);
        checkMap.put("redpacketTId", redpacketTId);

        // 根据参数映射查询红包模板信息
        HouRedpacketTemplateVO houRedpacketTemplate = activityRedpacketTemplateMapper.selectRpTemById(paraMap);

        // 初始化插入映射
        Map<String, Object> insertMap = new HashMap<String, Object>();
        // 初始化红包ID为空字符串
        insertMap.put("redpacketId", "");
        // 构建插入映射，包含红包的所有者信息和模板信息
        insertMap.put("redpacketOwnerId", userInfo.getUserId());
        insertMap.put("redpacketOwnerName", userInfo.getUserName());
        insertMap.put("redpacketTId", houRedpacketTemplate.getRedpacketTId());
        insertMap.put("redpacketTitle", houRedpacketTemplate.getRedpacketTTitle());
        insertMap.put("redpacketDesc", houRedpacketTemplate.getRedpacketTDesc());
        insertMap.put("redpacketStartDate", houRedpacketTemplate.getRedpacketTStartDate());
        insertMap.put("redpacketEndDate", houRedpacketTemplate.getRedpacketTEndDate());
        insertMap.put("redpacketPrice", houRedpacketTemplate.getRedpacketTPrice());
        insertMap.put("shopLimit", houRedpacketTemplate.getShopLimit());
        insertMap.put("goodsCatLimit", houRedpacketTemplate.getGoodsCatLimit());
        insertMap.put("redpacketTOrderlimit", houRedpacketTemplate.getRedpacketTOrderlimit());
        insertMap.put("macAddress", "");

        // 调用mapper插入红包基础信息
        this.baseMapper.insertyfRedpacketBase(insertMap);
        // 解析插入后的红包ID
        Integer redpacketId = Integer.parseInt(insertMap.get("redpacketId").toString());

        // 初始化更新映射
        // 增加红包领取数量
        Map<String, String> updateMap = new HashMap<String, String>();
        // 设置更新映射中的红包交易ID
        updateMap.put("redpacketTId", houRedpacketTemplate.getRedpacketTId().toString());
        // 调用mapper更新红包领取数量
        this.baseMapper.updateRedpGiveout(updateMap);

        // 返回插入的红包ID
        return redpacketId;
    }

    /**
     * 根据存款信息查询可用红包
     *
     * @param param 包含用户token、商店列表和总价的信息
     * @return 可用红包的列表
     */
    @Override
    public ResponseResult selectByDeposit(GetAvaliableRedpDTO param) {

        // 通过token获取用户信息
        // 获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        String userId = userInfo.getUserId();

        // 获取用户购物的总价
        Double totalPrice = param.getTotalPrice();

        // 初始化可用红包列表
        // redpacketTType; 红包类型，1-新人注册红包，2-普通红包，3-单品红包 4-店铺红包
        // 5-单品新人红包 6-赠送红包 默认2
        List<AvaliableRpBaseResultVO> avaliableRpList = Lists.newArrayList();

        // 分别查询不同类型的可用红包，并合并到一个列表中
        // 查询普通红包
        List<AvaliableRpBaseResultVO> generalRbList = this.baseMapper.selectUserAvaliableRpList(userId, 2,
                param.getPlatForm());
        avaliableRpList = getAvaliableRp(totalPrice, avaliableRpList, generalRbList);

        // 查询赠送红包
        // 查询赠送红包
        List<AvaliableRpBaseResultVO> sendUserRbList = this.baseMapper.selectUserAvaliableRpList(userId, 6,
                param.getPlatForm());
        avaliableRpList = getAvaliableRp(totalPrice, avaliableRpList, sendUserRbList);

        // 查询新人红包
        // 查询新人红包
        List<AvaliableRpBaseResultVO> newPersonRbList = this.baseMapper.selectUserAvaliableRpList(userId, 1,
                param.getPlatForm());

        avaliableRpList = getAvaliableRp(totalPrice, avaliableRpList, newPersonRbList);

        // 查询PLUS红包
        //查询plus红包
        List<AvaliableRpBaseResultVO> plusRbList = this.baseMapper.selectUserAvaliableRpList(userId, 7,
                param.getPlatForm());
        avaliableRpList = getAvaliableRp(totalPrice, avaliableRpList, plusRbList);

        // 返回可用红包列表
        return ResponseResult.success(avaliableRpList);
    }

    /**
     * 根据总价和红包规则，筛选可用的红包
     *
     * @param totalPrice 用户购物的总价
     * @param avaliableRpList 当前可用的红包列表
     * @param newPersonRbList 新人红包列表
     * @return 过滤后的可用红包列表
     */
    private List<AvaliableRpBaseResultVO> getAvaliableRp(Double totalPrice, List<AvaliableRpBaseResultVO> avaliableRpList,
                                                       List<AvaliableRpBaseResultVO> newPersonRbList) {
        // 如果新人红包列表不为空，遍历检查每个新人红包的使用条件
        if (!CollectionUtils.isEmpty(newPersonRbList)) {
            for (AvaliableRpBaseResultVO newPersonRb : newPersonRbList) {
                // 如果总价为空，则默认为0
                if (totalPrice == null) {
                    totalPrice = 0.0;
                }
                // 如果总价满足红包的最低消费条件，将红包添加到可用列表中
                if (totalPrice >= newPersonRb.getRedpacketTOrderlimit()) {
                    avaliableRpList.add(newPersonRb);
                }
            }
        }
        return avaliableRpList;
    }
}
