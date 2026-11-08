package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.vo.activity.RedTemplateEntityVO;
import com.houjiang.commons.vo.activity.HouRedpacketTemplateVO;
import com.houjiang.commons.vo.base.PlusEquityEntityVO;
import com.houjiang.commons.vo.base.PlusThaliEntityVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.entity.UserPlusInfoEntity;
import com.houjiang.userms.entity.UserPlusOrderEntity;
import com.houjiang.userms.feign.ActivityServiceClient;
import com.houjiang.userms.feign.BaseServiceClient;
import com.houjiang.userms.feign.ThirdPartyServiceClient;
import com.houjiang.userms.mapper.*;
import com.houjiang.userms.plugin.RedpacketQueryGateway;
import com.houjiang.userms.service.UserPlusInfoService;
import com.houjiang.userms.vo.PlusMemberInfoVO;
import com.houjiang.userms.vo.PlusRenewEntityVO;
import com.houjiang.userms.vo.PlusRuleEntityVO;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Amos
 */
@Service
public class UserPlusInfoServiceImpl extends ServiceImpl<UserPlusInfoMapper, UserPlusInfoEntity>
    implements UserPlusInfoService {

    @Value("${system.pcIndexConfig}")
    private String pcIndexConfig;

    private static String pcIndexConfig2;


    @PostConstruct
    public String getPcIndexConfig2() {
        return pcIndexConfig2 = this.pcIndexConfig;
    }

    @Autowired
    private UserPlusEquityMapper userPlusEquityMapper;
    @Autowired
    private UserPlusEquityTempMapper userPlusEquityTempMapper;
    @Autowired
    private UserPlusRenewMapper userPlusRenewMapper;
    @Autowired
    private UserPlusOrderMapper userPlusOrderMapper;
    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;
    @Autowired
    private BaseServiceClient baseServiceClientl;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private ActivityServiceClient activityServiceClient;
    @Autowired
    private RedpacketQueryGateway redpacketQueryGateway;
    @Autowired
    private UserPlatformMessageMapper userPlatformMessageMapper;
    @Autowired
    private UserInfoServiceImpl userInfoServiceImpl;
    @Autowired
    private UserPointsLogServiceImpl userPointsLogServiceImpl;
    @Autowired
    private BaseServiceClient baseServiceClient;

    /**
     * 用户 ID 文本转 Long，供只读红包查询扩展点使用；非数值返回 {@code null}。
     */
    private static Long parseUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(userId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 根据用户ID查询用户附加信息。
     *
     * @param map 一个包含查询条件的键值对映射表，其中可能包含用户ID。
     * @return 返回查询结果的ResponseResult对象，如果查询成功，结果包含UserPlusInfoEntity实体；如果查询失败或无结果，则实体为null。
     */
    @Override
    public ResponseResult selectByUserId(Map<String, String> map) {
        // 使用LambdaUpdateWrapper构造查询条件，条件为用户ID等于map中可能存在的用户ID值。
        UserPlusInfoEntity entity = this.baseMapper.selectOne(new LambdaUpdateWrapper<UserPlusInfoEntity>()
                .eq(map.containsKey("userId"), UserPlusInfoEntity::getUserId, map.containsKey("userId") ? map.get("userId").toString() : null));
        // 将查询结果封装进ResponseResult对象并返回，如果查询结果为空，则entity为null。
        return ResponseResult.success(entity);
    }
    /**
     * 根据用户ID查询用户的附加信息及优惠额度。
     * @param userId 用户ID。
     * @return 返回查询结果，包含用户的优惠额度和剩余限制。
     */
    @Override
    public ResponseResult queryUserPlusInfo(String userId) {
        // 初始化优惠额度为0
        BigDecimal bigDecimal1 = new BigDecimal(0);
        // 通过用户ID查询用户的附加信息
        UserPlusInfoEntity map1 = this.baseMapper.selectplusmember(userId);
        if (map1 != null) {
            // 获取用户的权益类型
            Integer integer = map1.getThaliType();
            // 将已用金额转换为BigDecimal类型
            String used_amount = String.valueOf(map1.getUsedAmount());
            BigDecimal bigDecimal = new BigDecimal(used_amount);
            BigDecimal subtract = new BigDecimal(0);

            // 获取用户的权益资产
            String selectplusthali = map1.getPlusEquity();
            // 如果权益资产为空或为空字符串，返回失败结果
            if (selectplusthali == null || "".equals(selectplusthali)) {
                return ResponseResult.fail(BizCodeEnume.UNKNOW_EXCEPTION.getCode(), BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
            }
            // 将权益资产字符串按逗号分隔，转换为整数列表
            String[] split = selectplusthali.split(",");
            List<Integer> appIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());

            // 分别从两个数据源查询权益信息
            List<PlusEquityEntityVO> entities = userPlusEquityMapper.selectplusequitys(appIdList);
            List<PlusEquityEntityVO> entities1 = userPlusEquityTempMapper.selectplusequitys(appIdList);
            // 合并两个列表，并去除重复项
            entities.addAll(entities1);
            entities = entities.stream().distinct().collect(Collectors.toList());

            // 如果权益信息列表为空，返回失败结果
            if (entities.isEmpty()) {
                return ResponseResult.fail(BizCodeEnume.UNKNOW_EXCEPTION.getCode(), BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
            }
            // 遍历权益信息列表，计算优惠额度和剩余限制
            for (PlusEquityEntityVO selectplusequity : entities) {
                if (selectplusequity.getEquityType() == 1) {
                    String equityValue = selectplusequity.getEquityValue();
                    bigDecimal1 = new BigDecimal(equityValue);
                    // 如果优惠限制为0，则设置优惠额度为负数，表示无限制
                    if (selectplusequity.getDiscountLimit().compareTo(new BigDecimal(0)) == 0) {
                        subtract = new BigDecimal(-1);
                    } else {
                        // 否则，计算剩余限制
                        subtract = selectplusequity.getDiscountLimit().subtract(bigDecimal);
                    }
                }
            }
            // 构建结果映射，包含优惠额度和剩余限制
            Map<String, BigDecimal> result = new HashMap<>(2);
            result.put("discount", bigDecimal1);
            result.put("residualLimit", subtract.setScale(2, RoundingMode.DOWN));
            // 返回成功结果，包含计算得到的优惠额度和剩余限制
            return ResponseResult.success(result);
        }
        // 如果用户附加信息为空，返回失败结果
        return ResponseResult.fail();
    }

    /**
     * 更新用户权限限制信息。
     * 通过传递一个包含更新信息的HashMap来更新用户的相关限制参数。此方法的目的是为了增加或修改用户的某些限制条件，
     * 例如：更新用户的空间使用限制、操作频率限制等。
     *
     * @param map 包含更新信息的HashMap，键值对代表了需要更新的字段及其新的值。
     * @return 返回一个表示操作成功的结果对象。使用ResponseResult.success()来构建表示成功的响应结果。
     */
    @Override
    public ResponseResult updateUserPluslimit(HashMap<Object, Object> map) {
        // 调用基本Mapper接口的方法，执行更新操作。
        this.baseMapper.updateUserPluslimit(map);
        // 返回表示操作成功的响应结果。
        return ResponseResult.success();
    }

    @Override
    public ResponseResult plusMemberOpen(Map<String, Object> map) {
        UserInfoVO userInfo = null;
        if(null == map.get("token")){
            //微信plue会员支付 接口存入 token
            String token = RedisUtil.get(HouConfig.HOU_PLUS_ORDER + map.get("plusOrder"));
            if(null == token){
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
            }
            userInfo = ParseTokenUtils.getUserInfoByToken(token);
            map.put("token",token);
        }else{
            userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        }
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        String userId = userInfo.getUserId();
        //判断用户是续费还是开通
        Date startTime = null;
        PlusMemberInfoVO plusMemberInfo = this.baseMapper.selectByUserId(userId);
        if (plusMemberInfo == null){
            //用户第一次开通
            startTime = new Date();
        }else if (plusMemberInfo.getThaliStatus() == 0){
            //会员过期
            startTime = new Date();
        }else if (plusMemberInfo.getThaliStatus() == 1){
            //会员未过期续费
            //查询最新续费记录
            PlusRenewEntityVO plusRenewEntity = userPlusRenewMapper.selectRenewNew(userId);
            if (plusRenewEntity == null){
                startTime = plusMemberInfo.getThaliEndtime();
            }else{
                startTime = plusRenewEntity.getRenewEndtime();
            }

        }
        //获取时间
        Calendar instance = Calendar.getInstance();
        //当前月份
        int month = instance.get(Calendar.MONTH) + 1;

        Date endTime = null;
        String result1 = null;
        instance.setTime(startTime);
        Integer redGrantnum = 0;
        if (map.get("thaliType").equals(CommonType.plusThaliType.PLUS_THALI_MOON.getCode())){
            //月卡
            instance.add(Calendar.MONTH,1);
            endTime = instance.getTime();
            result1 =CommonType.plusThaliType.PLUS_THALI_MOON.getDesc();
            redGrantnum = 1;
        }else if(map.get("thaliType").equals(CommonType.plusThaliType.PLUS_THALI_SEASON.getCode())){
            //季卡
            instance.add(Calendar.MONTH,3);
            endTime = instance.getTime();
            result1 =CommonType.plusThaliType.PLUS_THALI_SEASON.getDesc();
            redGrantnum = 3;
        }else if (map.get("thaliType").equals(CommonType.plusThaliType.PLUS_THALI_YEAR.getCode())){
            //年卡
            instance.add(Calendar.YEAR,1);
            endTime = instance.getTime();
            result1 =CommonType.plusThaliType.PLUS_THALI_YEAR.getDesc();
            redGrantnum = 12;
        }

        try {

            //向订单表加入数据
            Integer integer1 = 0;
            HashMap<String, Object> payMap = new HashMap<>();
            payMap.put("plusOrderid", map.get("plusOrder"));
            payMap.put("userId", userId);
            payMap.put("payPrice", map.get("payPrice"));
            payMap.put("payType", map.get("type"));
            payMap.put("thaliType", map.get("thaliType"));
            payMap.put("startTime", startTime);
            payMap.put("endTime", endTime);
            payMap.put("plusEquity", map.get("thaliEquity"));
            //是否已支付 0 否  1 是
            payMap.put("isPay", CommonType.commonStatus.YES.getCode());
            integer1 = userPlusOrderMapper.insertplusorder(payMap);

            Integer integer2 = 0;
            Integer integer3 = 0;

            HashMap<String, Object> renew = new HashMap<>();
            renew.put("userId", userId);
            renew.put("thaliType", map.get("thaliType"));
            renew.put("renewStarttime", startTime);
            renew.put("renewEndtime", endTime);
            renew.put("plusEquity", map.get("thaliEquity"));
            renew.put("plusOrderid", map.get("plusOrder"));

            //向plusinfo插入信息
            if (plusMemberInfo == null){
                //用户第一次开通
                HashMap<String, Object> info = new HashMap<>();
                info.put("userId", userId);
                info.put("thaliType", map.get("thaliType"));
                info.put("thaliStarttime", startTime);
                info.put("thaliEndtime", endTime);
                info.put("redGrantnum", redGrantnum);
                info.put("redGrantmonth", Integer.valueOf(String.valueOf(map.get("thaliType"))) - 1);
                info.put("plusEquity", map.get("thaliEquity"));
                integer2 = this.baseMapper.insertplusinfo(info);

                renew.put("renewStatus", 1);
                integer3 = userPlusRenewMapper.insertplusrenew(renew);

                //刷新token
                userInfo.setThaliType((Integer) map.get("thaliType"));
                userInfo.setPlusMember(1);
                updatetoken(userInfo, String.valueOf(map.get("thaliEquity")),map.get("token").toString());
            }else if (plusMemberInfo.getThaliStatus() == 0){
                //会员过期
                HashMap<String, Object> info = new HashMap<>();
                info.put("userId", userId);
                info.put("thaliType", map.get("thaliType"));
                info.put("thaliStarttime", startTime);
                info.put("thaliEndtime", endTime);
                info.put("thaliStatus", 1);
                info.put("redGrantnum", redGrantnum);
                info.put("redGrantmonth", Integer.valueOf(String.valueOf(map.get("thaliType"))) - 1);
                info.put("plusEquity", map.get("thaliEquity"));
                info.put("usedAmount", 0);
                integer2 = this.baseMapper.updateplusinfo(info);

                renew.put("renewStatus", 1);
                integer3 = userPlusRenewMapper.insertplusrenew(renew);
                //刷新token
                userInfo.setThaliType((Integer) map.get("thaliType"));
                userInfo.setPlusMember(1);
                updatetoken(userInfo, String.valueOf(map.get("thaliEquity")),map.get("token").toString());
            }else if (plusMemberInfo.getThaliStatus() == 1){
                //会员未过期续费 向续费表插入数据
                renew.put("renewStatus", 0);
                integer3 = userPlusRenewMapper.insertplusrenew(renew);
            }

            //更新短信通知状态
            this.baseMapper.updateplusinfo1(userId);


            //短信通知
            String userMobile = userInfo.getUserMobile();
            Map<String,String> sendMap = new HashMap<>();
            sendMap.put("userMobile",userMobile);
            sendMap.put("result1",result1);
            thirdPartyServiceClient.plusmemberopenSend(sendMap);

            //删除首页缓存
            RedisUtil.remove(RedisPrefixUtils.pcIndexConfig + "_" + userId);

            //开通成功发放红包和积分
            if (integer1 > 0 && integer2 > 0){
                //查询套餐
                if (map.get("thaliEquity") != null && map.get("thaliEquity") != ""){
                    String thaliEquity = String.valueOf(map.get("thaliEquity"));
                    String[] split = thaliEquity.split(",");
                    List<Integer> appIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());

                    //查询套餐权益
                    List<PlusEquityEntityVO> entities = userPlusEquityMapper.selectplusequity(appIdList);
                    if (entities != null && entities.size() > 0){
                        for (PlusEquityEntityVO entity : entities) {
                            //发放红包
                            if (entity.getEquityType().equals(CommonType.plusEquityType.PLUS_NO_THRESHOLD_REDPAPER.getCode())
                                    || entity.getEquityType().equals(CommonType.plusEquityType.PLUS_RED_PAPER.getCode())){
                                String equityValue = entity.getEquityValue();
                                String[] red = equityValue.split(",");
                                //红包模板id
                                List<Integer> redList = Stream.of(red).map(Integer::parseInt).collect(Collectors.toList());
                                Integer integer = redGrant(userInfo, redList);

                                HashMap<String, Object> map1 = new HashMap<>();
                                map1.put("userId", userId);
                                map1.put("redGrantmonth", month);
                                this.baseMapper.updateplusinfo(map1);

                                //发送站内信通知
                                StringBuffer Content = new StringBuffer(CommonType.plusThaliType.PLUS_CONTENT1.getDesc());
                                StringBuffer Content1 = new StringBuffer(CommonType.plusThaliType.PLUS_CONTENT2.getDesc());

                                Map send = new HashMap<>();
                                if (map.get("thaliType").equals(CommonType.plusThaliType.PLUS_THALI_MOON.getCode())){
                                    Content.append(CommonType.plusThaliType.PLUS_THALI_MOON.getDesc());
                                }else if (map.get("thaliType").equals(CommonType.plusThaliType.PLUS_THALI_SEASON.getCode())){
                                    Content.append(CommonType.plusThaliType.PLUS_THALI_SEASON.getDesc());
                                }else if (map.get("thaliType").equals(CommonType.plusThaliType.PLUS_THALI_YEAR.getCode())){
                                    Content.append(CommonType.plusThaliType.PLUS_THALI_YEAR.getDesc());
                                }
                                Content.append(Content1);
                                send.put("userId",userId);
                                send.put("messageTitle",CommonType.plusThaliType.PLUS_CONTENT3.getDesc());
                                send.put("messageContent",Content.toString());
                                sendmessage(send);

                                if (integer == 2){
                                    return ResponseResult.fail(0, "红包发放失败");
                                }
                            }else if (entity.getEquityType().equals(CommonType.plusEquityType.PLUS_INTEGRAL.getCode())){
                                //发放积分
                                String equityValue = entity.getEquityValue();
                                Integer integer = updateIntegral(userInfo, Integer.parseInt(equityValue), String.valueOf(map.get("plusOrder")));
                                if (integer == 2){
                                    return ResponseResult.fail(0, "积分发放失败");
                                }

                                //修改获取积分
                                Map<String,Object> map1 = new HashMap<>();
                                map1.put("userId",userId);
                                map1.put("earnPoints",equityValue);
                                Integer integer4 = this.baseMapper.updateplusinfo(map1);
                                if (!(integer4 > 0)){
                                    return ResponseResult.fail(0, "积分发放失败");
                                }
                            }
                        }
                    }
                }
            }
            if (integer3 > 0){
                return ResponseResult.success("续费成功");
            }
            if (integer1 > 0 && integer2 > 0){
                return ResponseResult.success("开通成功");
            }
        }catch (Exception e) {
            throw e;
        }
        return ResponseResult.fail(0, "开通失败");
    }

    /** 开通会员成功更新token信息 */
    public void updatetoken(UserInfoVO userInfo,String plusEquity,String token){
        Map<String,Object> pmap= (Map<String, Object>) baseServiceClientl.getMemberClassIdByUserId(userInfo.getUserId()).getData();
        int i =Integer.valueOf(pmap.get("memberClassId").toString());
        Map<String, Object> memberClassIdByUserId = (Map<String, Object>) baseServiceClientl.getMemberNameByClassId(String.valueOf(i)).getData();

        //会员折扣
        BigDecimal discount = new BigDecimal(String.valueOf(memberClassIdByUserId.get("gradeDiscount")));
        String[] split = plusEquity.split(",");
        List<Integer> appIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());

        //查询权益详情
        List<PlusEquityEntityVO> selectplusequitys = userPlusEquityMapper.selectplusequitys(appIdList);
        if (selectplusequitys != null && selectplusequitys.size() > 0) {
            for (PlusEquityEntityVO plusequity : selectplusequitys) {
                if (plusequity.getEquityType() == 1) {
                    String equityValue = plusequity.getEquityValue();
                    BigDecimal bigDecimal1 = new BigDecimal(equityValue);
                    //折扣
                    userInfo.setPlusDiscount(bigDecimal1);
                }
            }
        }
        // 把用户信息根据根据token存在redis中
        // 用户信息和卖家信息都保存在userinfo中
        try {
            UserInfoDTO userInfoDTO = new UserInfoDTO();
            userInfoDTO.setUser(userInfo);
            RedisUtil.set(HouConfig.HOU_USER_TOKEN_USER + token, mapper.writeValueAsString(userInfoDTO),
                    HouConfig.TOKEN_OUTTIME);
            RedisUtil.set(HouConfig.HOU_USER_TOKEN_USERID + token, userInfo.getUserId().toString(),
                    HouConfig.TOKEN_OUTTIME);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
    /** 发放红包 */
    @Transactional
    public Integer redGrant(UserInfoVO userInfo,List<Integer> id){
        List<HouRedpacketTemplateVO> newRedpacketTems = activityServiceClient.selectNewRedpacketTem(id);
        if (newRedpacketTems != null && !newRedpacketTems.isEmpty()) {
            try {
                for (HouRedpacketTemplateVO newRedpacketTem : newRedpacketTems) {
                    if (newRedpacketTem.getRedTermofvalidity() != null){
                        Calendar instance = Calendar.getInstance();
                        //开始时间
                        Date redpacketStartDate = new Date();
                        instance.setTime(redpacketStartDate);
                        instance.add(Calendar.DATE,newRedpacketTem.getRedTermofvalidity());
                        //结束时间
                        Date redpacketEndDate = instance.getTime();

                        Map<String, Object> insertMap = new HashMap<String, Object>();
                        insertMap.put("redpacketOwnerId", userInfo.getUserId());
                        insertMap.put("redpacketOwnerName", userInfo.getUserName());
                        insertMap.put("redpacketTId", newRedpacketTem.getRedpacketTId());
                        insertMap.put("redpacketTitle", newRedpacketTem.getRedpacketTTitle());
                        insertMap.put("redpacketDesc", newRedpacketTem.getRedpacketTDesc());
                        insertMap.put("redpacketStartDate", redpacketStartDate);
                        insertMap.put("redpacketEndDate", redpacketEndDate);
                        insertMap.put("redpacketPrice", newRedpacketTem.getRedpacketTPrice());
                        insertMap.put("shopLimit", newRedpacketTem.getShopLimit());
                        insertMap.put("goodsCatLimit", newRedpacketTem.getGoodsCatLimit());
                        insertMap.put("redpacketTOrderlimit", newRedpacketTem.getRedpacketTOrderlimit());
                        insertMap.put("macAddress", "");
                        //plus红包
                        insertMap.put("redpacketType", 7);
                        activityServiceClient.insertXinRenYfRedpacketBase(insertMap);

                        // 增加红包已领取数量
                        Map<String, String> updateMap = new HashMap<String, String>();
                        updateMap.put("redpacketTId", newRedpacketTem.getRedpacketTId().toString());

                        activityServiceClient.updateRedpGiveout(updateMap);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
                throw e;
            }
        }
        return 1;
    }
    /** 站内信通知 */
    public void sendmessage(Map map){
        userPlatformMessageMapper.insertmessage(map);
    }
    public Integer updateIntegral(UserInfoVO userInfo,Integer integral,String orderId){
        //修改会员积分和会员等级
        try {
            MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
            ArrayList<String> list = new ArrayList<>();
            String userId = userInfo.getUserId();
            list.add(userId);
            updateMemberMessageListIntegral.setUserIdList(list);
            updateMemberMessageListIntegral.setIntegral(integral);
            //1是增加积分 0 是扣除积分
            updateMemberMessageListIntegral.setType(CommonType.commonStatus.YES.getCode());
            userInfoServiceImpl.updateMemberIntegral(updateMemberMessageListIntegral);

            //添加积分修改记录
            Map<String, Object> pointParam = new HashMap<>();
            pointParam.put("PointsLogType", CommonType.pointsLogType.GAIN.getCode().toString());
            pointParam.put("ClassId", CommonType.pointType.PlUS_REMOVE_OBSTACLES.getCode().toString());
            pointParam.put("PointsLogTime", DateUtils.getTime());
            pointParam.put("PointsLogDesc", CommonType.pointType.PlUS_REMOVE_OBSTACLES.getDesc());
            pointParam.put("PointsLogFlag", "reg");
            pointParam.put("UserId", userId);
            pointParam.put("orderId", orderId);
            pointParam.put("PointsLogPoints", integral.toString());
            pointParam.put("PointsLogOperate", CommonType.pointType.PlUS_REMOVE_OBSTACLES.getDesc());
            userPointsLogServiceImpl.insertByMap(pointParam);
        }catch(Exception e){
            // 回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 2;
        }
        return 1;
    }

    @Override
    public ResponseResult selectPlusMember(Map<String, Object> map) {
        List<PlusThaliEntityVO> plusThaliEntities = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //查询规则
        PlusRuleEntityVO plusRuleEntity = this.baseMapper.selectplusrule();
        if (map.get("token") == null || map.get("token") == ""){
            //用户未登录
            //查询套餐表
            plusThaliEntities = this.baseMapper.selectthaliList();
            if (plusThaliEntities != null && plusThaliEntities.size() > 0){
                for (PlusThaliEntityVO plusThaliEntity : plusThaliEntities) {
                    if (plusRuleEntity != null) {
                        if (plusThaliEntity.getThaliType() == 1) {
                            plusThaliEntity.setLogoImg(plusRuleEntity.getPlusMonthcardlogo());
                        } else if (plusThaliEntity.getThaliType() == 2) {
                            plusThaliEntity.setLogoImg(plusRuleEntity.getPlusSeasoncardlogo());
                        }
                        if (plusThaliEntity.getThaliType() == 3) {
                            plusThaliEntity.setLogoImg(plusRuleEntity.getPlusYearcardlogo());
                        }

                        plusThaliEntity.setRecommend(plusRuleEntity.getPlusSigns());
                        plusThaliEntity.setPropaganda(plusRuleEntity.getPlusPropaganda());
                    }

                    String thaliEquity = plusThaliEntity.getThaliEquity();
                    String[] split = thaliEquity.split(",");
                    List<Integer> appIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());
                    plusThaliEntity.setThaliEquityList(appIdList);
                    //查询权益
                    List<PlusEquityEntityVO> selectplusequitys = userPlusEquityMapper.selectplusequitys(appIdList);
                    plusThaliEntity.setEquityTotal(selectplusequitys.size());
                    plusThaliEntity.setPlusEquitys(selectplusequitys);
                    if (selectplusequitys.size() > 0 && selectplusequitys != null){
                        for (PlusEquityEntityVO plusEquityEntity : selectplusequitys) {
                            //获取红包模板
                            if (plusEquityEntity.getEquityType() == 2 || plusEquityEntity.getEquityType() == 3){
                                String equityValue = plusEquityEntity.getEquityValue();
                                String[] split1 = equityValue.split(",");
                                List<Integer> redIdList = Stream.of(split1).map(Integer::parseInt).collect(Collectors.toList());
                                //查询红包模板
                                List<RedTemplateEntityVO> redTemplateEntities = activityServiceClient.selectredtemplateId(redIdList);
                                // 只读扩展点：二开插件优先，无插件回落官方 Feign 结果（未登录分支无 userId）。
                                redTemplateEntities = redpacketQueryGateway.redTemplatesByIds(null, redIdList, redTemplateEntities);
                                plusEquityEntity.setRedList(redTemplateEntities);
                            }
                            if (plusEquityEntity.getEquityType() == 1){

                                BigDecimal multiply = new BigDecimal(plusEquityEntity.getEquityValue()).multiply(new BigDecimal(10));
                                plusEquityEntity.setEquityValue(multiply.toString());
                            }
                        }
                    }
                }
            }
        }else{
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
            if (null == userInfo) {
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
            }
            String userId = userInfo.getUserId();

            PlusThaliEntityVO plusThaliEntity = this.baseMapper.selectthaliId(userId);
            if (plusThaliEntity == null){
                return ResponseResult.fail(BizCodeEnume.PLUS_MEMBER_NO_EXIST.getCode(),BizCodeEnume.PLUS_MEMBER_NO_EXIST.getMsg());
            }
            plusThaliEntity.setRedAvailable(0);
            //查询是否有续费权益
            Integer integer = userPlusRenewMapper.selectthaliIdnext(userId);
            plusThaliEntity.setNextThaliType(integer);
            //查询权益最后到期时间
            Map map1 = userPlusRenewMapper.selectthaliIdnexttime(userId);
            if (integer == null){
                //判断是否快到期
                if (plusThaliEntity.getThaliType() == 1){
                    //套餐结束时间
                    Date thaliEndtime = plusThaliEntity.getThaliEndtime();
                    Calendar instance = Calendar.getInstance();
                    instance.setTime(thaliEndtime);
                    instance.add(Calendar.DATE,-5);
                    Date time = instance.getTime();
                    if (new Date().compareTo(time) >= 0){
                        plusThaliEntity.setRenewalDisplay(1);
                    }
                }else if (plusThaliEntity.getThaliType() == 2){
                    //套餐结束时间
                    Date thaliEndtime = plusThaliEntity.getThaliEndtime();
                    Calendar instance = Calendar.getInstance();
                    instance.setTime(thaliEndtime);
                    instance.add(Calendar.MONTH,-1);
                    Date time = instance.getTime();
                    if (new Date().compareTo(time) >= 0){
                        plusThaliEntity.setRenewalDisplay(1);
                    }
                }else if (plusThaliEntity.getThaliType() == 3){
                    //套餐结束时间
                    Date thaliEndtime = plusThaliEntity.getThaliEndtime();
                    Calendar instance = Calendar.getInstance();
                    instance.setTime(thaliEndtime);
                    instance.add(Calendar.MONTH,-3);
                    Date time = instance.getTime();
                    if (new Date().compareTo(time) >= 0){
                        plusThaliEntity.setRenewalDisplay(1);
                    }
                }
            }
            if (plusThaliEntity != null){
                String thaliEquity = plusThaliEntity.getThaliEquity();
                String[] split = thaliEquity.split(",");
                List<Integer> appIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());
                plusThaliEntity.setThaliEquityList(appIdList);

                List<PlusEquityEntityVO> selectplusequitys = userPlusEquityMapper.selectplusequitys(appIdList);
                List<PlusEquityEntityVO> selectplusequitys1 = userPlusEquityMapper.selectplusequitysTmp(appIdList);
                selectplusequitys.addAll(selectplusequitys1);
                selectplusequitys =  selectplusequitys.stream().distinct().collect(Collectors.toList());

                plusThaliEntity.setEquityTotal(selectplusequitys.size());
                plusThaliEntity.setPlusEquitys(selectplusequitys);
                if (selectplusequitys.size() > 0 && selectplusequitys != null){
                    for (PlusEquityEntityVO plusEquityEntity : selectplusequitys) {
                        //获取红包模板
                        if (plusEquityEntity.getEquityType() == 2 || plusEquityEntity.getEquityType() == 3){
                            String equityValue = plusEquityEntity.getEquityValue();
                            String[] split1 = equityValue.split(",");
                            List<Integer> redIdList = Stream.of(split1).map(Integer::parseInt).collect(Collectors.toList());
                            //查询红包模板
                            List<RedTemplateEntityVO> redTemplateEntities = activityServiceClient.selectredtemplateId(redIdList);
                            // 只读扩展点：二开插件优先，无插件回落官方 Feign 结果。
                            redTemplateEntities = redpacketQueryGateway.redTemplatesByIds(
                                    parseUserId(userId), redIdList, redTemplateEntities);
                            plusEquityEntity.setRedList(redTemplateEntities);

                            if (plusEquityEntity.getEquityType() == 3){
                                plusThaliEntity.setRedTotal(redIdList.size());
                                //判断红包可用张数
                                Integer selectuserred = activityServiceClient.selectuserred(redIdList, userId);
                                // 只读扩展点：插件按领取记录条数返回，无插件回落官方计数。
                                selectuserred = redpacketQueryGateway.countUserReceived(
                                        parseUserId(userId), redIdList, selectuserred);
                                plusThaliEntity.setRedAvailable(selectuserred);
                            }
                        }
                        if (plusEquityEntity.getEquityType() == 1){
                            BigDecimal multiply = new BigDecimal(plusEquityEntity.getEquityValue()).multiply(new BigDecimal(10));
                            plusEquityEntity.setEquityValue(multiply.toString());
                        }
                    }
                }
            }
            //有续费权益将到期时间替换
            if (map1 != null){
                try {
                    plusThaliEntity.setThaliEndtime(format.parse(String.valueOf(map1.get("renew_endtime"))));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            plusThaliEntities.add(plusThaliEntity);
        }
        return ResponseResult.success(plusThaliEntities);
    }
    @GlobalTransactional
    @Override
    public ResponseResult openMember(Map<String, Object> map) throws IllegalAccessException {
        String userInfoStr = RedisUtil.get(HouConfig.HOU_PLATFORM_TOKEN_USER + map.get("token"));
        if (null == userInfoStr || userInfoStr.isEmpty()) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }

        //查询套餐权益
        ResponseResult selectthalitype = baseServiceClient.queryMerchantPlusThali(map);
        Map<String, Object> thaliMap = (Map) selectthalitype.getData();

        HashMap<String, Object> returnmap = new HashMap<>();
        List userId = (List) map.get("userId");
        //失败用户id
        ArrayList<Object> list = new ArrayList<>();
        for (Object o : userId) {
            //创建支付单号
            int i = PayToolUtil.buildRandom(6);
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String plusorder = "PLUS" + format.format(new Date()) + i;
            //获取用户手机号
            String selectphone = this.baseMapper.selectPhone((String) o);
            returnmap.put("userId", o);
            returnmap.put("type", CommonType.plusPayType.PLUS_PLATFORM_PAY.getCode());
            returnmap.put("payPrice", 0);
            returnmap.put("thaliType", map.get("thaliType"));
            returnmap.put("plusOrder", plusorder);
            returnmap.put("thaliEquity", thaliMap.get("thaliEquity"));
            returnmap.put("userMobile", selectphone);
            ResponseResult plusmemberopen = openPlusMember(returnmap);
            if (plusmemberopen.getCode() != 1) {
                list.add(o);
            }
        }
        return ResponseResult.success(list);
    }
    /** plus会员开通 */
    public ResponseResult openPlusMember(Map<String, Object> map) {

        String userId = String.valueOf(map.get("userId")) ;
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setUserId(userId);
        userInfo.setUserName(this.baseMapper.queryUserName(userId));
        //判断用户是续费还是开通
        Date startTime = null;
        PlusMemberInfoVO plusMemberInfo = this.baseMapper.selectByUserId(userId);
        if (plusMemberInfo == null){
            //用户第一次开通
            startTime = new Date();
        }else if (plusMemberInfo.getThaliStatus() == 0){
            //会员过期
            startTime = new Date();
        }else if (plusMemberInfo.getThaliStatus() == 1){
            //会员未过期续费
            //查询最新续费记录
            PlusRenewEntityVO plusRenewEntity = userPlusRenewMapper.selectRenewNew(userId);
            if (plusRenewEntity == null){
                startTime = plusMemberInfo.getThaliEndtime();
            }else{
                startTime = plusRenewEntity.getRenewEndtime();
            }

        }
        //获取时间
        Calendar instance = Calendar.getInstance();
        //当前月份
        int month = instance.get(Calendar.MONTH) + 1;

        Date endTime = null;
        instance.setTime(startTime);
        String result1 = null;
        if (map.get("thaliType").equals(1)){
            //月卡
            instance.add(Calendar.DAY_OF_MONTH,30);
            endTime = instance.getTime();
            result1 = "月卡";
        }else if(map.get("thaliType").equals(2)){
            //季卡
            instance.add(Calendar.DAY_OF_MONTH,30 * 3);
            endTime = instance.getTime();
            result1 = "季卡";
        }else if (map.get("thaliType").equals(3)){
            //年卡
            instance.add(Calendar.DAY_OF_MONTH,30 * 12);
            endTime = instance.getTime();
            result1 = "年卡";
        }

        try {

            //向订单表加入数据
            Integer integer1 = 0;
            HashMap<String, Object> payMap = new HashMap<>();
            payMap.put("plusOrderid", map.get("plusOrder"));
            payMap.put("userId", userId);
            payMap.put("payPrice", map.get("payPrice"));
            payMap.put("payType", map.get("type"));
            payMap.put("thaliType", map.get("thaliType"));
            payMap.put("startTime", startTime);
            payMap.put("endTime", endTime);
            payMap.put("plusEquity", map.get("thaliEquity"));
            //是否已支付 0 否  1 是
            payMap.put("isPay", CommonType.commonStatus.YES.getCode());
            integer1 = userPlusOrderMapper.insertplusorder(payMap);

            Integer integer2 = 0;
            Integer integer3 = 0;
            //向plusinfo插入信息
            if (plusMemberInfo == null){
                //用户第一次开通
                HashMap<String, Object> info = new HashMap<>();
                info.put("userId", userId);
                info.put("thaliType", map.get("thaliType"));
                info.put("thaliStarttime", startTime);
                info.put("thaliEndtime", endTime);
                info.put("redGrantnum", Integer.parseInt(String.valueOf(map.get("thaliType"))) - 1);
                info.put("plusEquity", map.get("thaliEquity"));
                integer2 = this.baseMapper.insertplusinfo(info);
            }else if (plusMemberInfo.getThaliStatus() == 0){
                //会员过期
                HashMap<String, Object> info = new HashMap<>();
                info.put("userId", userId);
                info.put("thaliType", map.get("thaliType"));
                info.put("thaliStarttime", startTime);
                info.put("thaliEndtime", endTime);
                info.put("thaliStatus", 1);
                info.put("redGrantnum", Integer.parseInt(String.valueOf(map.get("thaliType"))) - 1);
                info.put("plusEquity", map.get("thaliEquity"));
                info.put("usedAmount", 0);
                integer2 = this.baseMapper.updateplusinfo(info);
            }else if (plusMemberInfo.getThaliStatus() == 1){
                //会员未过期续费 向续费表插入数据
                HashMap<String, Object> renew = new HashMap<>();
                renew.put("userId", userId);
                renew.put("thaliType", map.get("thaliType"));
                renew.put("renewStarttime", startTime);
                renew.put("renewEndtime", endTime);
                renew.put("plusEquity", map.get("thaliEquity"));
                renew.put("plusOrderid", map.get("plusOrder"));
                integer3 = userPlusRenewMapper.insertplusrenew(renew);
            }

            if (plusMemberInfo != null) {
                //更新短信通知状态
                this.baseMapper.updateplusinfo1(userId);
            }

            //发送短信通知
            String userMobile = String.valueOf(map.get("userMobile"));
            Map<String,String> sendMap = new HashMap<>();
            sendMap.put("userMobile",userMobile);
            sendMap.put("result1",result1);
            thirdPartyServiceClient.plusmemberopenSend(sendMap);

            //删除首页缓存
            RedisUtil.remove(pcIndexConfig2 + "_" + userId);

            //开通成功发放红包和积分
            if (integer1 > 0 && integer2 > 0){
                //查询套餐
                //PlusThaliEntity thali = plusMemberMapper.selectthalitype((Integer) map.get("thaliType"));
                if (map.get("thaliEquity") != null && map.get("thaliEquity") != ""){
                    String thaliEquity = String.valueOf(map.get("thaliEquity"));
                    String[] split = thaliEquity.split(",");
                    List<Integer> appIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());
                    //thali.setThaliEquityList(appIdList);

                    //查询套餐权益
                    List<PlusEquityEntityVO> entities = userPlusEquityMapper.selectplusequity(appIdList);
                    if (entities != null && !entities.isEmpty()){
                        for (PlusEquityEntityVO entity : entities) {
                            //发放红包
                            if (entity.getEquityType() == 2 || entity.getEquityType() == 3){
                                String equityValue = entity.getEquityValue();
                                String[] red = equityValue.split(",");
                                //红包模板id
                                List<Integer> redList = Stream.of(red).map(Integer::parseInt).collect(Collectors.toList());
                                Integer integer = redGrant(userInfo, redList);

                                HashMap<String, Object> map1 = new HashMap<>();
                                map1.put("userId", userId);
                                map1.put("redGrantmonth", month);
                                this.baseMapper.updateplusinfo(map1);

                                //发送站内信通知
                                StringBuffer Content = new StringBuffer(CommonType.plusThaliType.PLUS_CONTENT1.getDesc());
                                StringBuffer Content1 = new StringBuffer(CommonType.plusThaliType.PLUS_CONTENT2.getDesc());
                                Map send = new HashMap<>();
                                if (map.get("thaliType").equals(CommonType.plusThaliType.PLUS_THALI_MOON.getCode())){
                                    Content.append(CommonType.plusThaliType.PLUS_THALI_MOON.getDesc());
                                }else if (map.get("thaliType").equals(CommonType.plusThaliType.PLUS_THALI_SEASON.getCode())){
                                    Content.append(CommonType.plusThaliType.PLUS_THALI_SEASON.getDesc());
                                }else if (map.get("thaliType").equals(CommonType.plusThaliType.PLUS_THALI_YEAR.getCode())){
                                    Content.append(CommonType.plusThaliType.PLUS_THALI_YEAR.getDesc());
                                }
                                Content.append(Content1);
                                send.put("userId",userId);
                                send.put("messageTitle",CommonType.plusThaliType.PLUS_CONTENT3.getDesc());
                                send.put("messageContent",Content.toString());
                                sendmessage(send);

                                if (integer == 2){
                                    return ResponseResult.fail(2,"红包发放失败");
                                }
                            }else if (entity.getEquityType() == 4){
                                //发放积分
                                String equityValue = entity.getEquityValue();
                                Integer integer = updateIntegral(userInfo, Integer.parseInt(equityValue), String.valueOf(map.get("plusOrder")));
                                if (integer == 2){
                                    return ResponseResult.fail(2,"积分发放失败");
                                }

                                //修改获取积分
                                Map<String,Object> map1 = new HashMap<>();
                                map1.put("userId",userId);
                                map1.put("earnPoints",equityValue);
                                Integer integer4 = this.baseMapper.updateplusinfo(map1);
                                if (!(integer4 > 0)){
                                    return ResponseResult.fail(2,"积分发放失败");
                                }
                            }
                        }
                    }
                }

            }
            if (integer3 > 0){
                return ResponseResult.success(1,"续费成功",map.get("thaliType"));
            }
            if (integer1 > 0 && integer2 > 0){
                return ResponseResult.success(1,"开通成功",map.get("thaliType"));
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return ResponseResult.fail(2,"开通失败",map.get("thaliType"));
    }

    /**
     * 更新会员信息，包括会员状态和权益等。
     * 该方法主要处理对会员信息的查询、更新操作，以及根据会员权益发放红包和积分。
     */
    @Override
    public void plusMemberUpdate() {
        // 初始化信息映射表，用于存储会员信息和续费信息
        //查询过期会员信息
        Map infomap = new HashMap<>();
        Map renewmap = new HashMap<>();
        UserInfoVO userInfo = new UserInfoVO();
        // 查询所有Plus会员信息
        List<PlusMemberInfoVO> selectplus = this.baseMapper.selectplus();
        // 遍历所有会员信息
        for (PlusMemberInfoVO plusMemberInfo : selectplus) {
            // 设置当前处理的会员ID和用户名
            userInfo.setUserId(plusMemberInfo.getUserId());
            userInfo.setUserName(plusMemberInfo.getUserName());
            // 查询当前会员的续费信息
            //判断该用户是否有续费信息
            PlusRenewEntityVO plusRenewEntity = this.baseMapper.selectRenewNewUser(plusMemberInfo.getUserId());
            // 如果会员没有续费信息，则更新会员状态为未续费
            if (plusRenewEntity == null){
                //无续费信息 更新plusinfo会员状态
                infomap.put("thaliStatus",0);
                infomap.put("userId",plusMemberInfo.getUserId());
                this.baseMapper.updateplusinfo(infomap);
            } else {
                // 如果会员有续费信息，更新会员的权益信息
                //将续费信息更新到info
                String plusEquity = plusRenewEntity.getPlusEquity();
                renewmap.put("thaliType",plusRenewEntity.getThaliType());
                renewmap.put("thaliStarttime",plusRenewEntity.getRenewStarttime());
                renewmap.put("thaliEndtime",plusRenewEntity.getRenewEndtime());
                renewmap.put("plusEquity",plusEquity);
                renewmap.put("usedAmount",0);
                renewmap.put("redGrantnum",plusRenewEntity.getThaliType() - 1);
                renewmap.put("userId",plusMemberInfo.getUserId());
                this.baseMapper.updateplusinfo(renewmap);

                // 更新续费状态
                //更新续费状态
                userPlusRenewMapper.updaterenew(plusRenewEntity.getRenewId());

                // 根据权益类型发放红包或积分
                //开通成功发放红包和积分
                if (plusEquity != null && !plusEquity.isEmpty()) {
                    String thaliEquity = String.valueOf(plusEquity);
                    String[] split = thaliEquity.split(",");
                    List<Integer> appIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());

                    // 查询权益详情
                    //查询套餐
                    //PlusThaliEntity thali = plusMemberMapper.selectthalitype((Integer) map.get("thaliType"));
                    List<PlusEquityEntityVO> entities = this.baseMapper.selectplusequity(appIdList);
                    if (entities != null && !entities.isEmpty()) {
                        for (PlusEquityEntityVO entity : entities) {
                            // 发放红包
                            if (entity.getEquityType() == 2 || entity.getEquityType() == 3) {
                                String equityValue = entity.getEquityValue();
                                String[] red = equityValue.split(",");
                                List<Integer> redList = Stream.of(red).map(Integer::parseInt).collect(Collectors.toList());
                                redGrant(userInfo, redList);

                                // 更新已发放红包的月份
                                //更新红包发放月份
                                Calendar instance = Calendar.getInstance();
                                instance.setTime(new Date());
                                //当前月份
                                int month = instance.get(Calendar.MONTH) + 1;
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("userId", plusMemberInfo.getUserId());
                                map.put("redGrantmonth", month);
                                this.baseMapper.updateplusinfo(map);
                            }
                            // 发放积分
                            if (entity.getEquityType() == 4) {
                                String equityValue = entity.getEquityValue();
                                updateIntegral(userInfo, Integer.parseInt(equityValue), plusRenewEntity.getPlusOrderid());

                                // 更新获取的积分
                                //修改获取积分
                                Map<String,Object> map1 = new HashMap<>();
                                map1.put("userId",plusMemberInfo.getUserId());
                                map1.put("earnPoints",equityValue);
                                this.baseMapper.updateplusinfo(map1);
                            }
                        }
                    }
                }
            }
            // 清除会员首页缓存
            //清空首页缓存
            RedisUtil.remove(pcIndexConfig2 + "_" + plusMemberInfo.getUserId());
        }
    }

    /**
     * 更新会员限制信息。
     * 该方法用于在每个月的开始清零会员的消费额度，确保每个会员在新的月份开始时拥有全新的消费额度。
     * 它首先获取所有非零额度的会员信息，然后计算每个会员的额度是否需要清零。
     * 如果会员的额度应该被清零，它将更新该会员的消费额度为0。
     *
     * @return ResponseResult 表示操作的结果，成功或失败。
     */
    @Override
    public ResponseResult updateLimit() {
        // 定义日期格式化对象，用于处理日期字符串
        DateFormat dft = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
        // 创建一个映射，用于存储需要更新的会员信息
        Map infomap = new HashMap<>();
        // 开始时间
        Date star = null;
        // 结束时间
        Date endDay = null;
        // 查询所有具有非零额度的会员信息
        // 查询额度不为零的plus会员
        List<PlusMemberInfoVO> selectplusinfo = this.baseMapper.selectplusinfo();
        // 检查是否有会员信息需要处理
        if (!CollectionUtils.isEmpty(selectplusinfo)) {
            for (PlusMemberInfoVO plusMemberInfo : selectplusinfo) {
                try {
                    // 将会员的开始时间转换为Date对象
                    star = dft.parse(dft.format(plusMemberInfo.getThaliStarttime()));
                    // 获取当前日期作为结束时间
                    // 任务执行时间
                    endDay = dft.parse(dft.format(new Date()));
                    // 计算开始时间和当前时间之间的时间差（以毫秒为单位）
                    // 当前时间与其他时间相差的毫秒数
                    Long diff = endDay.getTime() - star.getTime();
                    // 将时间差转换为天数，并四舍五入到小数点后两位
                    BigDecimal divide = BigDecimal.valueOf(diff).divide
                            (BigDecimal.valueOf(60 * 60 * 1000 * 24), 2, RoundingMode.HALF_UP);
                    // 检查天数是否为整数，即是否没有小数部分
                    if (new BigDecimal(divide.intValue()).compareTo(divide) == 0) {
                        // 如果是整数天，计算月份的平均天数，并检查是否为整数
                        // 清空每月消费额度
                        BigDecimal divide1 = divide.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
                        if (new BigDecimal(divide1.intValue()).compareTo(divide1) == 0) {
                            // 如果月份平均天数为整数，准备更新会员的消费额度
                            // 更新基础额度
                            infomap.put("userId", plusMemberInfo.getUserId());
                            infomap.put("usedAmount", 0);
                            // 更新会员的消费额度为0
                            this.baseMapper.updateplusinfo(infomap);
                        }
                    }
                } catch (ParseException e) {
                    // 处理日期解析异常
                    e.printStackTrace();
                }

            }
        }
        // 返回操作成功的响应结果
        return ResponseResult.success();
    }


    /**
     * 实现PLUS权益的红包发放功能。
     * 在每个月的特定日期，对符合条件的用户发放PLUS权益所对应的红包。
     * 该方法主要负责检查当前月份是否有需要发放红包的用户，并执行发放操作。
     */
    @Override
    public void redgrant() {
        // 获取当前日期和时间
        //获取时间
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        System.out.println(instance.getTime());

        // 获取当前年份和月份
        //当前年份
        int year = instance.get(Calendar.YEAR);
        //当前月份
        int month = instance.get(Calendar.MONTH) + 1;
        //当前时间
        Date date = new Date();


        // 初始化用户信息和权益发放详情
        UserInfoVO userInfo = new UserInfoVO();
        StringBuffer Content = new StringBuffer("尊敬的用户，您的PLUS");
        StringBuffer Content1 = new StringBuffer("权益-红包券已到账，请查收！");
        //查询过期会员信息
        Map infomap = new HashMap<>();
        Map send = new HashMap<>();
        // 查询当前月份需要发放红包的PLUS会员信息
        List<PlusMemberInfoVO> selectplus = this.baseMapper.selectred(month);
        for (PlusMemberInfoVO plusMemberInfo : selectplus) {
            userInfo.setUserId(plusMemberInfo.getUserId());
            userInfo.setUserName(plusMemberInfo.getUserName());

            // 检查用户是否有未发放的红包权益
            //开通成功发放红包和积分
            //查询套餐
            String plusEquity = plusMemberInfo.getPlusEquity();
            if (plusEquity != null && !plusEquity.isEmpty()) {
                // 将权益字符串转换为可操作的列表
                String[] split = plusEquity.split(",");
                List<Integer> appIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());

                // 根据权益ID查询具体的权益详情
                List<PlusEquityEntityVO> entities = userPlusEquityMapper.selectplusequity(appIdList);
                for (PlusEquityEntityVO entity : entities) {
                    // 确认权益类型为红包，并检查是否达到发放日期
                    // 红包发放日
                    Integer redProvide = entity.getRedProvide();
                    if (entity.getEquityType() == 3) {
                        Calendar instance1 = Calendar.getInstance();
                        int i = instance1.get(Calendar.MONTH);
                        // 设置红包发放具体时间
                        // 红包发放时间
                        instance.set(year, i, redProvide, 00, 00, 00);
                        Date redtime = instance.getTime();

                        // 检查当前日期是否晚于或等于红包发放日期
                        // 到达红包发放日
                        if (date.compareTo(redtime) >= 0) {
                            // 发放红包逻辑
                            String equityValue = entity.getEquityValue();
                            String[] red = equityValue.split(",");
                            List<Integer> redList = Stream.of(red).map(Integer::parseInt).collect(Collectors.toList());
                            Integer integer = redGrant(userInfo, redList);

                            // 检查红包是否发放成功且满足发放周期条件
                            if (month >= plusMemberInfo.getRedGrantmonth() && integer == 1) {
                                // 更新用户红包发放记录
                                // 更新红包发放次数
                                infomap.put("userId", plusMemberInfo.getUserId());
                                infomap.put("redGrantnum", plusMemberInfo.getRedGrantnum() - 1);
                                infomap.put("redGrantmonth", month);
                                this.baseMapper.updateplusinfo(infomap);

                                // 根据会员类型发送不同的通知内容
                                // 发送站内信通知
                                if (plusMemberInfo.getThaliType() == 1) {
                                    Content.append("月卡");
                                } else if (plusMemberInfo.getThaliType() == 2) {
                                    Content.append("季卡");
                                } else if (plusMemberInfo.getThaliType() == 3) {
                                    Content.append("年卡");
                                }
                                Content.append(Content1);

                                // 发送通知
                                send.put("userId", plusMemberInfo.getUserId());
                                send.put("messageTitle", "红包到账通知");
                                send.put("messageContent", Content.toString());
                                sendmessage(send);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * 对plus订单进行补偿操作。
     * <p>
     * 本方法旨在处理特定情况下plus订单的补偿逻辑。通过传入的订单信息，查询相应的订单实体，
     * 并根据订单详情调用补偿接口。
     *
     * @param map 包含plus订单相关信息的映射表，其中必须包含plusOrder字段。
     * @return 返回执行补偿操作后的结果对象。
     */
    @Override
    public ResponseResult plusOrderCompensate(Map<String, Object> map) {
        // 从传入的map中获取plus订单号
        String plusorder = map.get("plusOrder").toString();
        // 根据plus订单号查询对应的订单实体
        UserPlusOrderEntity plusOrderEntity = userPlusOrderMapper.selectOne(new LambdaQueryWrapper<UserPlusOrderEntity>().eq(UserPlusOrderEntity::getPlusOrderid, plusorder));
        // 查询该订单所属用户的手机号
        //获取用户手机号
        String selectphone = this.baseMapper.selectPhone(plusOrderEntity.getUserId());
        // 初始化一个映射表用于存放需要传递给补偿接口的参数
        //封装参数
        HashMap<String, Object> returnmap = new HashMap<>();
        // 将订单相关的重要信息放入returnmap中
        returnmap.put("userId", plusOrderEntity.getUserId());
        returnmap.put("type", plusOrderEntity.getPayType());
        returnmap.put("payPrice", plusOrderEntity.getPayPrice());
        returnmap.put("thaliType", plusOrderEntity.getThaliType());
        returnmap.put("plusOrder", plusorder.toString());
        returnmap.put("thaliEquity", plusOrderEntity.getPlusEquity());
        returnmap.put("userMobile", selectphone);
        // 调用补偿接口，传入returnmap作为参数
        // 对异常plus会员进行补偿
        ResponseResult plusmemberopen = openPlusMember(returnmap);
        // 返回补偿操作的结果
        return plusmemberopen;
    }
}




