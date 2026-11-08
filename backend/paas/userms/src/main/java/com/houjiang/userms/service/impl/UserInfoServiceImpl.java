package com.houjiang.userms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.dto.shop.GateStoreUpdateDTO;
import com.houjiang.commons.vo.shop.GdsShopDetailResultVO;
import com.houjiang.commons.vo.shop.ShopDetailVO;
import com.houjiang.commons.dto.shop.ShopUserDTO;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.dto.userms.UserPointLogDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.RequestTokenUtils;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.util.secverifyapiutil.QRCodeUtil;
import com.houjiang.commons.vo.fx.VKUserInfoVO;
import com.houjiang.commons.vo.job.AcquiescenceVO;
import com.houjiang.commons.vo.user.*;
import com.houjiang.userms.context.LoginContext;
import com.houjiang.userms.dto.MemberMessageListDTO;
import com.houjiang.userms.dto.SubAccountDTO;
import com.houjiang.userms.dto.VkAddUserDTO;
import com.houjiang.userms.entity.*;
import com.houjiang.userms.feign.*;
import com.houjiang.userms.mapper.*;
import com.houjiang.userms.service.*;
import com.houjiang.userms.service.UserOrganizationService;
import com.houjiang.userms.vo.*;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import io.seata.tm.api.GlobalTransactionContext;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Amos
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfoEntity>
        implements UserInfoService {
    private final Lock lock = new ReentrantLock();

    @Autowired
    private BaseServiceClient baseServiceClient;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentServiceClient paymentServiceClient;

    @Autowired
    private UserInfoDetailService userInfoDetailService;

    @Autowired
    private UserBindConnectService userBindConnectService;

    @Autowired
    private UserPointsLogService userPointsLogService;
    @Autowired
    private UserSellerBaseService userSellerBaseService;
    @Autowired
    private UserOrganizationService userOrganizationService;

    @Autowired(required = false)
    private RedisCache redisCache;
    @Resource
    private UserBusinessInfoMapper userBusinessInfoMapper;
    @Resource
    private UserSellerGroupMapper userSellerGroupMapper;
    @Resource
    private UserPlatformMessageMapper userPlatformMessageMapper;
    @Autowired
    private com.houjiang.userms.feign.MerchantServiceClient merchantServiceClient;
    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;
    @Resource
    private UserInfoDetailMapper userInfoDetailMapper;
    @Autowired
    private UserLiveStatisticsService userLiveStatisticsService;

    /**
     * 协议同意记录服务：注册成功后落库用户同意的协议类型与版本号
     */
    @Autowired
    private UserAgreementConsentService userAgreementConsentService;

    @Resource
    private UserVkServiceImpl userVkService;
    @Resource
    private UserPlusInfoMapper userPlusInfoMapper;
    @Autowired
    private UserVkSetMapper userVkSetMapper;

    @Autowired
    private UserBindConnectMapper userBindConnectMapper;

    @Autowired
    private OrderServiceClient orderServiceClient;

    private static Pattern pattern1 = Pattern.compile("^[0-9]*$");
    private static Pattern pattern2 = Pattern.compile("^[A-Za-z]+$");
    private static Pattern pattern3 = Pattern.compile("^[A-Za-z0-9]+$");
    private static Pattern pattern4 = Pattern.compile("^[0-9A-Za-z^%&@',;=?$]*$");

    /**
     * 日志记录器，用于记录服务相关的日志信息。
     */
    private static final Logger logger = LoggerFactory.getLogger(UserInfoServiceImpl.class);
    @Autowired
    private UserSellerBaseMapper userSellerBaseMapper;

    /**
     * 通过用户手机号获取用户信息。
     *
     * @param userMobile 用户的手机号码。
     * @return 用户信息对象，包含用户的基本信息。
     */
    @Override
    public UserInfoVO getUserInfoByMobile(String userMobile) {
        return this.baseMapper.getUserInfoByPhoneNumber(userMobile);
    }

    /**
     * 通过用户手机号和店铺ID获取用户信息。
     *
     * @param userMobile 用户的手机号码。
     * @param shopId     店铺的ID。
     * @return 用户信息对象，包含用户与特定店铺相关的基本信息。
     */
    @Override
    public UserInfoVO getUserInfoByPhoneNumber1(String userMobile, String shopId) {
        return this.baseMapper.getUserInfoByPhoneNumber1(userMobile, shopId);
    }

    /**
     * 根据用户参数查询用户信息。
     *
     * @param vo 用户信息的VO对象，包含查询条件。
     * @return 查询到的用户信息对象。
     */
    @Override
    public ResponseResult querUserInfoByParams(UserInfoVO vo) {
        UserInfoVO userInfoVO = this.baseMapper.querUserInfoByParams(vo);
        // 登录链路走本接口，必须带上组织成员关系；否则 auth 会话里 orgList 为空，
        // 智能入库等按组织隔离的功能会误判「未加入任何组织」。
        if (userInfoVO != null && userInfoVO.getUserId() != null && !userInfoVO.getUserId().isBlank()) {
            try {
                List<Map<String, Object>> orgList = baseMapper.getUserOrgList(userInfoVO.getUserId());
                if (orgList == null) {
                    orgList = new ArrayList<>();
                }
                userInfoVO.setOrgList(orgList);
                if ((userInfoVO.getOrgId() == null || userInfoVO.getOrgId().isBlank()) && !orgList.isEmpty()) {
                    String primaryOrgId = null;
                    for (Map<String, Object> org : orgList) {
                        if (org == null) continue;
                        Object isPrimary = org.get("is_primary");
                        if (isPrimary != null && "1".equals(String.valueOf(isPrimary))) {
                            Object oid = org.get("org_id") != null ? org.get("org_id") : org.get("orgId");
                            if (oid != null) {
                                primaryOrgId = String.valueOf(oid);
                                break;
                            }
                        }
                    }
                    if (primaryOrgId == null) {
                        Object oid = orgList.get(0).get("org_id") != null
                                ? orgList.get(0).get("org_id") : orgList.get(0).get("orgId");
                        if (oid != null) primaryOrgId = String.valueOf(oid);
                    }
                    if (primaryOrgId != null) {
                        userInfoVO.setOrgId(primaryOrgId);
                    }
                }
            } catch (Exception e) {
                logger.warn("querUserInfoByParams 补全组织失败 userId={}: {}",
                        userInfoVO.getUserId(), e.getMessage());
            }
        }
        return ResponseResult.success(userInfoVO);
    }

    /**
     * 根据用户ID、手机号、店铺ID和用户状态查询用户信息。
     *
     * @param userInfoVO 用户信息的VO对象，包含查询条件。
     * @return 查询到的用户信息对象。
     */
    @Override
    public ResponseResult queryUserInfo(UserInfoVO userInfoVO) {
        long startTime = System.currentTimeMillis();

        String userId = RequestTokenUtils.getUserIdFromRequest();
        if (userId == null) {
            return ResponseResult.fail(401, "用户未登录或token已过期");
        }
        userInfoVO.setUserId(userId);

        String cacheKey = "userms:user:info:" + userId;
                
        // 1. 优先从 Redis 读取缓存（包含组织信息）
        if (redisCache != null) {
            try {
                Object cached = redisCache.getCacheObject(cacheKey);
                long step1End = System.currentTimeMillis();
                logger.info("queryUserInfo - Redis缓存查询耗时: {}ms, userId: {}", step1End - startTime, userId);
                if (cached != null) {
                    UserInfoEntity entity = deserializeUserInfoCache(cached);
                    if (entity != null) {
                        entity.setPassword(null);
                        // 缓存中包含组织信息，直接返回
                        if (entity.getOrgList() == null) {
                            // 旧缓存格式，补充组织信息
                            long orgStartFromCache = System.currentTimeMillis();
                            List<Map<String, Object>> orgList = baseMapper.getUserOrgList(userId);
                            long orgEndFromCache = System.currentTimeMillis();
                            long orgQueryTimeFromCache = orgEndFromCache - orgStartFromCache;
                            logger.info("queryUserInfo - 缓存命中补充getUserOrgList耗时: {}ms, userId: {}", orgQueryTimeFromCache, userId);
                            if (orgQueryTimeFromCache > 3000) {
                                logger.warn("queryUserInfo - getUserOrgList 查询过慢: {}ms, userId: {}", orgQueryTimeFromCache, userId);
                            }
                            entity.setOrgList(orgList != null ? orgList : new ArrayList<>());
                        }
                        logger.debug("queryUserInfo cache hit, userId={}", userId);
                        long totalTime = System.currentTimeMillis() - startTime;
                        if (totalTime > 5000) {
                            logger.warn("queryUserInfo 总耗时过长: {}ms, userId: {}", totalTime, userId);
                        } else {
                            logger.info("queryUserInfo 总耗时: {}ms, userId: {}", totalTime, userId);
                        }
                        return ResponseResult.success(entity);
                    }
                }
            } catch (Exception e) {
                logger.debug("queryUserInfo cache get fail, userId={}", userId, e);
            }
        }

        // 2. 数据库查询（简化查询条件，提升性能）
        LambdaQueryWrapper<UserInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(UserInfoEntity::getUserId, UserInfoEntity::getUserMobile, 
                           UserInfoEntity::getUserName, UserInfoEntity::getUserState,
                           UserInfoEntity::getUserIntegral, UserInfoEntity::getUserLastloginTime,
                           UserInfoEntity::getUserNickname, UserInfoEntity::getUserSignature,
                           UserInfoEntity::getIsBusiness, UserInfoEntity::getGradeId,
                           UserInfoEntity::getMemberCreateTime)
                   .eq(UserInfoEntity::getUserId, userId);
        
        // 仅在必要时添加额外条件（避免过度使用动态条件影响性能）
        if (userInfoVO.getUserMobile() != null && !userInfoVO.getUserMobile().trim().isEmpty()) {
            queryWrapper.eq(UserInfoEntity::getUserMobile, userInfoVO.getUserMobile());
        }
        
        UserInfoEntity userInfoEntity = this.baseMapper.selectOne(queryWrapper);
        long step2End = System.currentTimeMillis();
        logger.info("queryUserInfo - 数据库查询hou_user_info耗时: {}ms, userId: {}", step2End - startTime, userId);

        if (userInfoEntity != null) {
            // 3. 查询组织信息（潜在的性能瓶颈，需优化）
            long orgStart = System.currentTimeMillis();
            try {
                List<Map<String, Object>> orgList = baseMapper.getUserOrgList(userId);
                userInfoEntity.setOrgList(orgList != null ? orgList : new ArrayList<>());
            } catch (Exception e) {
                logger.error("getUserOrgList error, userId={}", userId, e);
                userInfoEntity.setOrgList(new ArrayList<>());
            }
            long orgEnd = System.currentTimeMillis();
            long orgQueryTime = orgEnd - orgStart;
            logger.info("queryUserInfo - getUserOrgList查询耗时: {}ms, userId: {}", orgQueryTime, userId);
            if (orgQueryTime > 3000) {
                logger.warn("queryUserInfo - getUserOrgList 查询过慢: {}ms, userId: {}", orgQueryTime, userId);
            }
                    
            // 4. 写入缓存（延长过期时间至 30 分钟，减少查库频率）
            if (redisCache != null) {
                try {
                    String jsonStr = objectMapper.writeValueAsString(userInfoEntity);
                    redisCache.setCacheObject(cacheKey, jsonStr, 30, TimeUnit.MINUTES);
                    logger.debug("queryUserInfo cache set success, userId={}", userId);
                } catch (Exception e) {
                    logger.debug("queryUserInfo cache set fail, userId={}", userId, e);
                }
            }
            long step4End = System.currentTimeMillis();
            logger.info("queryUserInfo - 缓存写入耗时: {}ms, userId: {}", step4End - orgEnd, userId);
        }

        long totalTime = System.currentTimeMillis() - startTime;
        if (totalTime > 5000) {
            logger.warn("queryUserInfo 总耗时过长: {}ms, userId: {}", totalTime, userId);
        } else {
            logger.info("queryUserInfo 总耗时: {}ms, userId: {}", totalTime, userId);
        }
        return ResponseResult.success(userInfoEntity);
    }

    /**
     * 反序列化用户信息缓存，兼容Jackson JSON字符串和FastJSON旧格式
     */
    private UserInfoEntity deserializeUserInfoCache(Object cached) {
        try {
            if (cached instanceof String) {
                String json = (String) cached;
                return json.isEmpty() ? null : objectMapper.readValue(json, UserInfoEntity.class);
            } else if (cached instanceof UserInfoEntity) {
                return (UserInfoEntity) cached;
            } else {
                return objectMapper.convertValue(cached, UserInfoEntity.class);
            }
        } catch (Exception e) {
            logger.debug("queryUserInfo cache deserialize fail: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 根据传入的Map参数查询用户信息。
     *
     * @param map 包含用户ID和手机号的Map对象。
     * @return 查询到的用户信息对象。
     */
    @Override
    public ResponseResult queryUserInfoByUserInfoVO(Map<String, String> map) {
        // 初始化查询条件包装器
        QueryWrapper<UserInfoEntity> queryWrapper = new QueryWrapper<>();

        // 如果映射中存在用户ID，添加到查询条件中
        if (null != map && null != map.get("userId")) {
            queryWrapper.eq("user_id", map.get("userId"));
        }

        // 如果映射中存在用户移动电话号码，添加到查询条件中
        if (null != map && null != map.get("userMobile")) {
            queryWrapper.eq("user_mobile", map.get("userMobile"));
        }

        // 根据查询条件查询用户信息
        UserInfoEntity userInfoEntity = this.baseMapper.selectOne(queryWrapper);

        // 如果用户信息存在，处理用户昵称为空的情况，使用用户名作为昵称
        if (null != userInfoEntity) {
            userInfoEntity.setUserNickname(userInfoEntity.getUserNickname() == null
                    ? userInfoEntity.getUserName() : userInfoEntity.getUserNickname());
            
            // 清除敏感信息
            // 清除密码
            userInfoEntity.setPassword(null);
        }

        // 返回查询结果，成功包含用户信息，失败包含空用户信息
        return ResponseResult.success(userInfoEntity);
    }

    /**
     * 通过用户ID查询用户信息。
     *
     * @param userId 用户的ID。
     * @return 用户信息对象。
     */
    @Override
    public ResponseResult queryUserInfoByUserId(String userId) {
        /**
         * 根据用户ID查询用户信息。
         * 使用了BaseMapper的扩展方法getUserInfoByUserId2，该方法具体实现未展示，但可推测其通过用户ID从数据库中检索用户信息。
         * @param userId 用户的唯一标识ID。
         * @return UserInfoVO 对象，包含查询到的用户信息。如果未找到对应用户，则UserInfoVO可能为null或包含空数据。
         */
        UserInfoVO userInfo = this.baseMapper.getUserInfoByUserId2(userId);

        // 清除敏感信息
        if (userInfo != null) {
            // 清除密码
            userInfo.setPassword(null);
            // 清除用户密码
            userInfo.setUserPassword(null);
        }

        /**
         * 构建并返回一个表示操作成功的响应结果，其中包含查询到的用户信息。
         * 使用了ResponseResult类的success方法，该方法用于构建一个表示成功结果的对象，并将用户信息作为数据部分进行封装。
         * @return ResponseResult 对象，包含成功标识和查询到的用户信息。
         */
        return ResponseResult.success(userInfo);

    }

    /**
     * 根据Map更新用户信息。
     *
     * @param map 包含用户信息的键值对，键为字段名，值为字段值。
     * @return 更新操作的结果，成功返回success，失败返回fail。
     */
    @Override
    public ResponseResult updateUserInfoByMap(Map<String, String> map) {
        // 创建查询条件
        QueryWrapper<UserInfoEntity> queryWrapper = new QueryWrapper<>();

        // 如果map中包含userId，添加到查询条件中
        if (null != map && null != map.get("userId")) {
            queryWrapper.eq("user_id", map.get("userId"));
        }
        // 如果map中包含userMobile，添加到查询条件中
        if (null != map && null != map.get("userMobile")) {
            queryWrapper.eq("user_mobile", map.get("userMobile"));
        }
        // 根据查询条件获取用户信息
        UserInfoEntity businessInfoEntity = this.baseMapper.selectOne(queryWrapper);

        // 如果map中包含password，更新用户密码
        if (null != map && null != map.get("password")) {
            businessInfoEntity.setPassword(map.get("password"));
        }
        // 更新用户最后登录时间和登录次数
        Date now = new Date();
        businessInfoEntity.setUserLastloginTime(now);
        businessInfoEntity.setUserCountLogin(businessInfoEntity.getUserCountLogin() + 1);

        // 更新用户信息
        int flag = this.baseMapper.updateById(businessInfoEntity);
        if (flag == CommonType.commonStatus.YES.getCode() && businessInfoEntity != null && businessInfoEntity.getUserId() != null) {
            clearUserInfoCache(businessInfoEntity.getUserId());
        }
        return flag == CommonType.commonStatus.YES.getCode() ? ResponseResult.success() : ResponseResult.fail();
    }

    /** 清除 queryUserInfo 缓存，在用户信息更新后调用 */
    private void clearUserInfoCache(String userId) {
        if (redisCache != null && userId != null) {
            try {
                redisCache.deleteObject("userms:user:info:" + userId);
            } catch (Exception e) {
                logger.debug("clearUserInfoCache fail, userId={}", userId, e);
            }
        }
    }

    /**
     * 更新会员积分。
     *
     * @param param 包含更新积分所需信息的DTO。
     * @return 更新操作的结果，成功返回success，失败返回fail。
     */
    @Override
    public ResponseResult updateMemberIntegral(MemberMessageListIntegralDTO param) {
        try {
            // 检查积分是否小于0
            if (param.getIntegral() < 0) {
                // 积分不能小于0
                return ResponseResult.fail(BizCodeEnume.INTEGRAL_CANNOT_BE_LESS_THAN_0.getCode(), BizCodeEnume.INTEGRAL_CANNOT_BE_LESS_THAN_0.getMsg());
            }
            // 检查用户ID列表是否为空
            if (param.getUserIdList().size() < 0) {
                // 用户id为空
                return ResponseResult.fail(BizCodeEnume.USER_ID_IS_NULL.getCode(), BizCodeEnume.USER_ID_IS_NULL.getMsg());

            }
            // 获取所有用户ID
            List<String> userIdList = param.getUserIdList();
            Integer userIntegral = null;
            for (String userId : userIdList) {
                // 根据用户ID查询用户信息
                QueryWrapper<UserInfoEntity> queryWrapper = new QueryWrapper<>();
                // 获取当前用户的积分
                queryWrapper.eq("user_id", userId);
                UserInfoEntity userInfoEntity = this.baseMapper.selectOne(queryWrapper);

                // 获取并保存用户当前积分
                userIntegral = userInfoEntity.getUserIntegral();
                // 根据类型更新积分
                // type 1是增加积分 0 是扣除积分
                // 便利 添加积分
                if (param.getType().equals(CommonType.commonStatus.YES.getCode())) {
                    userIntegral = userIntegral + param.getIntegral();
                    userInfoEntity.setUserIntegral(userIntegral);
                } else {
                    // 如果扣除后积分小于等于0，则设置为0
                    Integer temp = userIntegral - param.getIntegral();
                    userIntegral = temp <= 0 ? 0 : temp;
                    userInfoEntity.setUserIntegral(temp <= 0 ? 0 : temp);
                }
                // 更新用户积分
                this.baseMapper.update(userInfoEntity, queryWrapper);

                // 更新用户等级
                Map<String, Object> map = new HashMap<>();
                map.put("gradeStart", CommonType.commonStatus.YES.getCode());
                map.put("userIntegral", userIntegral);
                map.put("userId", userId);
                baseServiceClient.updateTagClassByUserId(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 返回操作成功结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL.getCode(), BizCodeEnume.OPERATION_SUCCESSFUL.getMsg());
    }

    /**
     * 更新用户的新旧状态。
     *
     * @param paraMap 参数映射，包含token等信息。
     * @return 更新操作的结果，可能为null。
     */
    @Override
    public Integer updateIsNew(Map<String, String> paraMap) {
        Integer torf = null;
        try {
            // 通过token获取用户信息。
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));
            // 更新用户的新旧状态。
            torf = this.baseMapper.updateUserIsNew(userInfo.getUserId());
            // 根据用户手机号获取更新后的用户信息。
            UserInfoVO newUserInfo = this.baseMapper.getUserInfoByPhoneNumber(userInfo.getUserMobile());
            // 将更新后的用户信息存储到Redis中，设置过期时间。
            RedisUtil.set(HouConfig.HOU_USER_TOKEN_USER + paraMap.get("token"), objectMapper.writeValueAsString(newUserInfo),
                    HouConfig.TOKEN_OUTTIME);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return torf;
    }

    /**
     * 用户注册接口。
     *
     * @param map 注册参数映射，包括手机号、验证码等。
     * @return 注册结果。
     * @throws UnknownHostException 如果获取IP地址失败。
     * @throws TransactionException 如果注册过程中发生事务异常。
     */
    @Override
    public ResponseResult register(Map<String, String> map) throws UnknownHostException, TransactionException {
        lock.lock();
        try {
            // 获取并校验用户手机号和验证码。
            String userMobile = map.get("userMobile");
            // 兼容两种参数名：code 和 phoneCode
            String phoneCode = map.get("phoneCode");
            if (com.houjiang.commons.util.StringUtils.isBlank(phoneCode)) {
                phoneCode = map.get("code");
            }
            
            // 参数校验：手机号和验证码不能为空
            if (com.houjiang.commons.util.StringUtils.isBlank(userMobile)) {
                return ResponseResult.fail(BizCodeEnume.INCORRECT_PHONE_NUMBER_FORMAT);
            }
            if (com.houjiang.commons.util.StringUtils.isBlank(phoneCode)) {
                return ResponseResult.fail(BizCodeEnume.VERIFICATION_CODE_CANNOT_BE_EMPTY);
            }
            
            //判断shopId是否为空
            String shopId = map.get("shopId");
            // 获取电话区号
            String interDialingCode = com.houjiang.commons.util.StringUtils.isNotBlank(map.get("interDialingCode"))?map.get("interDialingCode"):"+86";
            
            // 从Redis获取验证码（使用RedisUtil统一工具类）
            String sessionPhoneCode = RedisUtil.get(userMobile);
            if (StringUtils.isNotEmpty(sessionPhoneCode)) {
                // 验证码不匹配时返回错误信息。
                if (!phoneCode.equals(sessionPhoneCode)) {
                    return ResponseResult.fail(BizCodeEnume.INCORRECT_VERIFICATION_CODE);
                }
            } else {
                // 验证码过期或不存在时返回错误信息。
                return ResponseResult.fail(BizCodeEnume.VERIFICATION_CODE_OVERDUE);
            }

            // 根据手机号和shopId检查用户是否已注册。
            // shopId 为null 是平台用户 注册根据 手机号 和 shopid为空两个 条件查询 用户 是否在平台注册
            // shopId 不为null是品牌店铺注册 根据 手机号 和 前端传入shopid 两个 条件查询 用户 是否在品牌店铺注册
            //走pc注册
            Integer checkuserId = this.baseMapper.checkUserIsRegister(userMobile, shopId);
            if (checkuserId > 0) {
                if(CheckPhoneUtil.isValidEmail(userMobile)){
                    // 已存在时返回错误信息。
                    return ResponseResult.fail(BizCodeEnume.EMAIL_EXSIT_EXCEPTION);
                }else{
                    // 已存在时返回错误信息。
                    return ResponseResult.fail(BizCodeEnume.PHONE_EXSIT_EXCEPTION);
                }

            }

            // 校验密码是否为空。
            // 判断密码是否为空
            if (null == map.get("password") || "".equals(map.get("password"))) {
                return ResponseResult.fail(BizCodeEnume.PASSWORD_NOT_STANDARD);
            }
            // 将前端传入的密码解密（须在删除验证码之前，避免解密失败白白消耗验证码）
            String passwordstr = null;
            try {
                passwordstr = JSEncrypt.decrypt(map.get("password"));
            } catch (Exception e) {
                logger.error("注册密码RSA解密失败: {}", e.getMessage());
                return ResponseResult.fail("密码解密失败，请刷新页面后重试");
            }

            // 验证码与密码均校验通过后再删除缓存中的验证码。
            RedisUtil.remove(userMobile);

            // 加密密码。
            // 将密码加密
            String password = PasswordUtils.BCryptPasswordEncoder(passwordstr);

            // 获取用户IP地址。
            String actionIp = IpUtils.getIpAddress();

            // 构建用户信息对象并设置属性。
            // 用户信息
            UserInfoEntity record = new UserInfoEntity();
            record.setUserMobile(userMobile);
            record.setPassword(password);
            record.setUserLastloginTime(new Date());
            record.setUserLastloginIp(actionIp);
            record.setInternationalDialingCode(interDialingCode);
            // 设置用户名：优先使用传入的userName，否则使用手机号
            String userName = map.get("userName");
            if (com.houjiang.commons.util.StringUtils.isNotBlank(userName)) {
                record.setUserName(userName);
            } else {
                record.setUserName(userMobile);
            }
            if (!StrUtil.isEmpty(shopId)) {
                // 将shopid封装 进品牌用户注册信息中
                record.setShopId(Integer.valueOf(shopId));
            }
            record.setRegisterTerminal(resolveRegisterTerminal(map, shopId));

            // 构建用户详情对象并设置属性。
            // 用户详情
            UserInfoDetaileEntity insertUserInfoDetail = new UserInfoDetaileEntity();
            insertUserInfoDetail.setUserRegTime(System.currentTimeMillis() / 1000);
            insertUserInfoDetail.setUserRegIp(actionIp);
            // 获取平台类型，如果为空则使用默认值5（管理后台）
            String platFormStr = map.get("platForm");
            // 默认值：管理后台
            int platForm = 5;
            if (com.houjiang.commons.util.StringUtils.isNotBlank(platFormStr)) {
                try {
                    platForm = Integer.parseInt(platFormStr);
                } catch (NumberFormatException e) {
                    logger.warn("平台类型格式错误: {}, 使用默认值5", platFormStr);
                    platForm = 5;
                }
            }
            insertUserInfoDetail.setUserPlatform(platForm);
            insertUserInfoDetail.setUserParentId(map.get("shareUserId"));

            // 构建用户绑定信息对象并设置属性。
            UserBindConnectEntity userBindMap = new UserBindConnectEntity();
            String bindId = null;
            if (shopId == null || shopId == "") {
                bindId = "mobile_" + userMobile;
            } else {
                bindId = "mobile_" + userMobile + "_" + shopId;
            }
            userBindMap.setBindId(bindId);
            userBindMap.setBindType(Byte.parseByte(CommonType.bindType.MOBILE.getCode().toString()));

            // 构建用户积分日志对象并设置属性。
            // 向积分表插入积分
            UserPointsLogEntity userPointLog = new UserPointsLogEntity();
            userPointLog.setPointsLogType(CommonType.pointsLogType.GAIN.getCode());
            userPointLog.setClassId(CommonType.pointType.REGISTRATION.getCode());
            userPointLog.setPointsLogTime(new Date());
            userPointLog.setPointsLogDesc(BizCodeEnume.MEMBER_REGISTRATION.getMsg());
            userPointLog.setPointsLogFlag("reg");

            // 执行用户注册相关操作。
            if (Boolean.FALSE.equals(insertUserInfo(record, insertUserInfoDetail, userBindMap, userPointLog))) 
            {
                return ResponseResult.fail(BizCodeEnume.REGISTRATION_FAIL.getMsg());
            }
            // 从Redis中获取注册用户的ID。
            String userId = RedisUtil.get(HouConfig.HOU_USER_LOGON_USERID + userMobile);
            try {
                // 在支付服务中创建用户。
                paymentServiceClient.createUser(userId);
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
            
            // 查询用户信息
            UserInfoEntity userInfo = this.getById(userId);
            if (userInfo == null) {
                logger.error("注册成功但查询用户信息失败，userId: {}", userId);
                return ResponseResult.fail("注册成功，但获取用户信息失败，请重新登录");
            }

            // 记录注册时同意的协议（协议类型、版本号、同意时间、IP、UA 等）。
            // 说明：同意留痕失败不阻断注册——此时验证码已消费，若因此回滚会让用户白跑一次；
            // 失败仅记 ERROR 日志便于排查。协议「必选且已勾选」的校验在 bff-web 侧前置完成。
            try {
                recordAgreementConsents(userInfo, map);
            } catch (Exception e) {
                logger.error("记录注册协议同意行为失败, userId={}", userId, e);
            }

            
            // 创建用户token并设置到Redis
            String token = JwtUtil.createJWT(userId);
            UserInfoDTO dto = new UserInfoDTO();
            UserInfoVO userInfoVO = new UserInfoVO();
            BeanUtils.copyProperties(userInfo, userInfoVO);
            // 注册返回的 token 会立刻被使用：不带组织成员关系时，智能入库等按组织隔离的
            // 功能全部会被判为「组织未确定」。这里先保障个人空间组织并写进会话。
            ensurePersonalOrganization(userId, userInfo.getUserName(), userInfoVO);
            dto.setUser(userInfoVO);
            RedisUtil.set(HouConfig.HOU_USER_TOKEN_USER + token, JSON.toJSONString(dto),
                    HouConfig.TOKEN_OUTTIME);
            RedisUtil.set(HouConfig.HOU_USER_TOKEN_USERID + token, userId,
                    HouConfig.TOKEN_OUTTIME);
            
            // 更新用户活跃度
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("token", token);
            returnMap.put("userId", userId);
            returnMap.put("userMobile", userInfo.getUserMobile());
            returnMap.put("platForm", map.get("platForm") != null ? map.get("platForm") : "5");
            userLiveStatisticsService.saveLiveStatistics(returnMap);
            
            // 返回注册成功信息并附带token。
            return ResponseResult.success(BizCodeEnume.REGISTRATION_SUCCESS.getMsg(), token);
        }finally {
            lock.unlock();
        }
    }

    /**
     * 保障新注册用户拥有「个人空间」组织，并把组织成员关系写进会话。
     *
     * <p>组织成员关系是登录时才补全的（{@code LoginServiceImpl#enrichUserOrganizations}），
     * 而注册接口会直接下发 token 并写 Redis 会话；若不在这里补，用户拿到 token 后
     * 直到下次重新登录前，所有按组织隔离的功能都会因组织未确定被拒绝。</p>
     *
     * <p>失败一律只记 WARN，不阻断注册：验证码已消费，回滚等于让用户白跑一次。</p>
     *
     * @param userId      新用户ID
     * @param displayName 展示名（组织命名用，为空则回退成「用户+ID」）
     * @param session     即将写入 Redis 的会话；注册接口下发 token 时必传，其余入口传 null
     */
    private void ensurePersonalOrganization(String userId, String displayName, UserInfoVO session) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", userId);
            request.put("displayName", displayName == null ? userId : displayName);
            ResponseResult orgResult = baseServiceClient.ensurePersonalOrganization(request);
            if (orgResult == null || orgResult.getCode() != 1 || !(orgResult.getData() instanceof Map)) {
                logger.warn("保障个人空间组织失败，userId={}, code={}, message={}",
                        userId, orgResult == null ? "null" : orgResult.getCode(),
                        orgResult == null ? "" : orgResult.getMessage());
                return;
            }
            Map<?, ?> data = (Map<?, ?>) orgResult.getData();
            Object orgIdObj = data.get("orgId");
            if (orgIdObj == null) {
                logger.warn("保障个人空间组织返回缺少 orgId，userId={}", userId);
                return;
            }
            String orgId = String.valueOf(orgIdObj);

            Map<String, Object> bind = new HashMap<>();
            bind.put("userId", userId);
            bind.put("orgId", orgId);
            // 个人空间不抢占主组织：用户后续登记公司时，公司会按创建时间倒序成为默认组织，
            // 若这里设成主组织，企业用户的默认组织会一直停在个人空间。
            bind.put("isPrimary", 0);
            ResponseResult bindResult = userOrganizationService.createUserOrganization(bind);
            logger.info("绑定个人空间组织结果，userId={}, orgId={}, code={}, message={}",
                    userId, orgId, bindResult == null ? "null" : bindResult.getCode(),
                    bindResult == null ? "" : bindResult.getMessage());

            if (session == null) {
                return;
            }
            // 无论新建还是此前已存在，都以库里的成员关系为准写入会话
            ResponseResult orgsResult = userOrganizationService.getUserOrganizations(userId);
            if (orgsResult != null && orgsResult.getCode() == 1 && orgsResult.getData() instanceof List) {
                List<Map<String, Object>> orgList = (List<Map<String, Object>>) orgsResult.getData();
                if (!orgList.isEmpty()) {
                    session.setOrgList(orgList);
                    session.setOrgId(orgId);
                }
            }
        } catch (Exception e) {
            logger.warn("保障个人空间组织异常，userId={}（不影响注册结果）", userId, e);
        }
    }

    /**
     * 记录注册时用户同意的协议。
     *
     * <p>{@code agreementConsents} 由 bff-web 在完成「必选协议已全部勾选 + 版本号匹配当前生效版本」
     * 校验后透传，格式为 JSON 数组字符串：</p>
     * <pre>
     * [{"code":"PRIVACY_POLICY","agreementId":1,"agreementName":"隐私政策","versionId":1,"versionNo":"v1.0.0"}]
     * </pre>
     *
     * <p>{@code clientIp} / {@code clientUa} 同样由 bff-web 依据真实请求头注入
     * （paas 侧 {@code IpUtils.getIpAddress()} 取到的是本机网卡地址，不是客户端地址）。</p>
     *
     * @param userInfo 注册成功的用户信息
     * @param map      注册参数
     */
    private void recordAgreementConsents(UserInfoEntity userInfo, Map<String, String> map) {
        String raw = map.get("agreementConsents");
        if (com.houjiang.commons.util.StringUtils.isBlank(raw)) {
            return;
        }
        com.alibaba.fastjson.JSONArray array = JSON.parseArray(raw);
        if (array == null || array.isEmpty()) {
            return;
        }
        List<Map<String, Object>> consents = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            JSONObject item = array.getJSONObject(i);
            if (item != null) {
                consents.add(item);
            }
        }
        if (consents.isEmpty()) {
            return;
        }
        String username = userInfo.getUserName() != null ? userInfo.getUserName() : userInfo.getUserMobile();
        String source = com.houjiang.commons.util.StringUtils.isNotBlank(map.get("agreementSource"))
                ? map.get("agreementSource") : "web";
        userAgreementConsentService.recordConsents(
                userInfo.getUserId(),
                username,
                consents,
                map.get("clientIp"),
                map.get("clientUa"),
                source,
                "register");
    }

    /**
     * 解析注册终端：mall / houcloud / hougeo
     */
    private String resolveRegisterTerminal(Map<String, String> map, String shopId) {
        String raw = null;
        if (map != null) {
            raw = firstNonBlank(
                    map.get("registerTerminal"),
                    map.get("register_terminal"),
                    map.get("terminal")
            );
        }
        String normalized = normalizeRegisterTerminal(raw);
        if (normalized != null) {
            return normalized;
        }
        if (com.houjiang.commons.util.StringUtils.isNotBlank(shopId)) {
            return "mall";
        }
        return "houcloud";
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (com.houjiang.commons.util.StringUtils.isNotBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String normalizeRegisterTerminal(String raw) {
        if (com.houjiang.commons.util.StringUtils.isBlank(raw)) {
            return null;
        }
        String value = raw.trim().toLowerCase();
        // "houmall" 为品牌更名为 UniMall 前的历史取值，需继续识别，避免存量数据与老客户端注册终端归不上类
        if ("mall".equals(value) || "商城".equals(raw.trim()) || "unimall".equals(value) || "houmall".equals(value) || "bbc".equals(value)) {
            return "mall";
        }
        if ("houcloud".equals(value) || "cloud".equals(raw.trim()) || "厚匠云".equals(raw.trim())) {
            return "houcloud";
        }
        if ("hougeo".equals(value) || "geo".equals(value)) {
            return "hougeo";
        }
        // houocr：OCR 识别站点（ocr.houjit.com）扫码登录自动注册的会员，
        // 与商城 / 厚匠云 / GEO 站点并列，用于后台看清这批用户是从哪一站进来的
        if ("houocr".equals(value) || "ocr".equals(value) || "ocr站点".equals(raw.trim())) {
            return "houocr";
        }
        return null;
    }

    /**
     * 在用户注册时插入用户信息、用户详细信息、用户绑定信息，并记录用户积分。
     * 使用全局事务确保这些操作要么全部成功，要么全部回滚。
     *
     * @param userinfo             用户基本信息实体
     * @param insertUserInfoDetail 用户详细信息实体
     * @param userBindMap          用户绑定信息实体
     * @param userPointLog         用户积分日志实体
     * @return 注册成功返回true，否则返回false
     * @throws TransactionException 如果注册过程中发生异常
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean insertUserInfo(UserInfoEntity userinfo, UserInfoDetaileEntity insertUserInfoDetail,
                                  UserBindConnectEntity userBindMap, UserPointsLogEntity userPointLog) throws TransactionException {
        try {
            // 查询积分规则，用于后续设置用户积分
            //查询积分规则
            ResponseResult resultResp = baseServiceClient.queryIntegralRules();
            Map<String, Integer> integralRules = (Map<String, Integer>) resultResp.getData();

            // 使用雪花算法生成唯一的用户ID
            //雪花算法生成用户id
            String userId = SnowflakeIdWorker.getSnowId();

            // 设置用户基本信息，并插入用户表
            userinfo.setUserId(userId);
            userinfo.setUserName(userinfo.getUserMobile());
            userinfo.setUserCountLogin(1);
            userinfo.setUserIsNew(CommonType.commonStatus.YES.getCode());
            userinfo.setUserIntegral(integralRules.get("ruleRegister"));
            userinfo.setCreateTime(new Date());
            if (com.houjiang.commons.util.StringUtils.isBlank(userinfo.getRegisterTerminal())) {
                userinfo.setRegisterTerminal("houcloud");
            }
            //保存用户
            int one = this.baseMapper.insert(userinfo);

            // 获取默认用户头像，并设置用户详细信息
            ResponseResult avatorMap = baseServiceClient.selectByConfigKey("photo_user_logo");
            Map<String, Object> configMap = (Map<String, Object>) avatorMap.getData();
            String userAvator = configMap.get("configValue").toString();
            insertUserInfoDetail.setUserId(userId);
            insertUserInfoDetail.setUserAvatar(userAvator);
            insertUserInfoDetail.setUserMobileVerify(1);
            //保存用户详情
            Boolean two = userInfoDetailService.save(insertUserInfoDetail);


            // 设置用户绑定信息，并保存
            userBindMap.setUserId(userId);
            userBindMap.setCreateUser(userinfo.getUserMobile());
            //保存用户绑定表 - 先检查是否已存在，如果存在则更新，不存在则插入
            UserBindConnectEntity existingBind = userBindConnectService.getOne(
                new LambdaQueryWrapper<UserBindConnectEntity>()
                    .eq(UserBindConnectEntity::getBindId, userBindMap.getBindId())
                    .eq(UserBindConnectEntity::getBindType, userBindMap.getBindType())
            );
            Boolean three;
            if (existingBind != null) {
                // 如果已存在，更新记录
                existingBind.setUserId(userId);
                existingBind.setCreateUser(userinfo.getUserMobile());
                three = userBindConnectService.updateById(existingBind);
                logger.info("更新已存在的用户绑定记录: bindId={}, bindType={}, userId={}", 
                    userBindMap.getBindId(), userBindMap.getBindType(), userId);
            } else {
                // 如果不存在，插入新记录
                three = userBindConnectService.save(userBindMap);
            }

            // 记录用户注册积分
            //添加积分变更表
            userPointLog.setPointsLogPoints(integralRules.get("ruleRegister"));
            userPointLog.setPointsLogOperate("会员注册");
            userPointLog.setUserId(userId);
            Boolean four = userPointsLogService.save(userPointLog);

            // 根据注册积分修改用户等级
            //修改会员等级
            Map<String, Object> gradeMap = new HashMap<>();
            gradeMap.put("ruleRegister", integralRules.get("ruleRegister"));
            gradeMap.put("userId", userId);
            ResponseResult result = baseServiceClient.saveTagClass(gradeMap);

            // 绑定登录上下文
            UserInfoVO ui = new UserInfoVO();
            ui.setUserId(userId);
            LoginContext context = new LoginContext(ui);
            LoginContext.binding(context);

            // 检查所有操作是否成功，如果成功则返回true，否则回滚事务并返回false
            //判断是否添加成功
            if (CommonType.commonStatus.YES.getCode() == one
                    && two
                    && three
                    && four
                    && CommonStatusEnum.SUCCESS.getCode() == result.getCode()) {
                
                // 为新用户分配默认角色（普通用户，role_id=2）
                try {
                    this.baseMapper.insertUserRole(userId, 2);
                    logger.info("用户注册成功，已分配普通用户角色，user_id: {}", userId);
                } catch (Exception e) {
                    logger.error("分配用户角色失败，user_id: {}, error: {}", userId, e.getMessage());
                    // 角色分配失败不影响注册流程，记录日志即可
                }
                
                RedisUtil.set(HouConfig.HOU_USER_LOGON_USERID + userinfo.getUserMobile(), userId,RedisUtil.EXRP_HOUR);
                return true;
            } else {
                String xid = RootContext.getXID();
                if (xid != null) {
                    GlobalTransactionContext.reload(xid).rollback();
                }
                return false;
            }
        } catch (Exception e) {
            // 发生异常时回滚事务，并抛出运行时异常
            String xid = RootContext.getXID();
            if (xid != null) {
                GlobalTransactionContext.reload(xid).rollback();
            }
            throw new RuntimeException(e.getMessage());

        }

    }

    /**
     * 根据用户ID查询用户消息列表的积分总数。
     *
     * @param userId 用户ID。
     * @return 用户消息列表积分总数。
     */
    @Override
    public Integer queryMemberMessageListIntegral(String userId) {
        // 通过BaseMapper查询用户的积分总数
        //查询用户积分
        return this.baseMapper.queryMemberMessageListIntegral(userId);
    }

    /**
     * 查询当前用户冻结积分。
     *
     * @param userId 用户ID。
     * @return 用户消息列表积分总数。
     */
    @Override
    public Integer getMemberFrozenIntegraIntegralByUserId(String userId) {
        Integer frozenIntegra = this.baseMapper.queryMemberFrozenIntegral(userId);
        if (null == frozenIntegra) {
            frozenIntegra = 0;
        }
        return frozenIntegra;
    }

    /**
     * 根据用户令牌查询个人优惠信息，并包含用户积分。
     *
     * @param userTokenBeanDTO 用户令牌信息的数据传输对象。
     * @return 包含个人优惠信息和用户积分的响应结果。
     */
    @Override
    public ResponseResult queryPersonalDiscount(UserTokenBeanDTO userTokenBeanDTO) {
        // 通过令牌解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(userTokenBeanDTO.getToken());
        if (userInfo == null || userInfo.getUserId() == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 查询用户的个人优惠信息（无会员等级绑定时 SQL 会返回 null）
        Map<String, Object> stringObjectMap = this.baseMapper.queryPersonalDiscount(userInfo.getUserId());
        if (stringObjectMap == null) {
            stringObjectMap = new HashMap<>(4);
            stringObjectMap.put("tagId", null);
            stringObjectMap.put("memberClassId", null);
            stringObjectMap.put("gradeName", null);
            stringObjectMap.put("gradeRule", null);
            stringObjectMap.put("gradeDiscount", null);
        }
        // 查询用户的积分总数
        Integer integral = this.baseMapper.queryMemberMessageListIntegral(userInfo.getUserId());
        if (integral == null) {
            integral = 0;
        }
        // 查询用户冻结积分
        Integer frozenIntegral = this.baseMapper.queryMemberFrozenIntegral(userInfo.getUserId());
        if (null == frozenIntegral) {
            frozenIntegral = 0;
        }
        // 将用户的积分总数添加到个人优惠信息中
        stringObjectMap.put("userIntegral", integral - frozenIntegral);
        stringObjectMap.put("frozenIntegral", frozenIntegral);
        // 返回包含个人优惠信息和用户积分的响应结果
        return ResponseResult.success(stringObjectMap);
    }

    /**
     * 根据用户令牌获取用户会员信息。
     *
     * @param paraMap 参数映射表，包含用户令牌。
     * @return 用户会员信息的响应结果。
     */
    @Override
    public ResponseResult getUserMemberInformation(Map<String, String> paraMap) {
        // 通过令牌获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));
        // 如果用户信息为空，则返回令牌无效的响应
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 初始化会员信息对象
        MemberInformationVO memberInformation = new MemberInformationVO();
        // 设置会员的基本信息
        memberInformation.setUserName(userInfo.getUserName());
        memberInformation.setUserAvatar(userInfo.getUserAvatar());

        // 查询当前用户的积分
        // 查询用户冻结积分
        Integer frozenIntegral = this.baseMapper.queryMemberFrozenIntegral(userInfo.getUserId());
        if (null == frozenIntegral) {
            frozenIntegral = 0;
        }
        //获取当前积分
        Integer presentIntegral = this.baseMapper.queryMemberMessageListIntegral(userInfo.getUserId());
        memberInformation.setMemberIntegral(Math.max((presentIntegral - frozenIntegral), 0));
        memberInformation.setFrozenIntegra(frozenIntegral);
        // 查询用户的会员等级信息
        //获取会员等级名称
        Map<String, Object> memberClassIdByUserId = baseServiceClient.queryMemberShipGradeByUserId(userInfo.getUserId());
        if (null != memberClassIdByUserId) {
            // 设置会员等级名称和URL
            memberInformation.setMemberName(memberClassIdByUserId.get("grade_name") + "");
            memberInformation.setMemberUrl(memberClassIdByUserId.get("grade_url") + "");
            // 设置会员折扣，保留一位小数
            memberInformation.setGradeDiscount(String.format("%.1f", (Double) memberClassIdByUserId.get("grade_discount") * 10));
        }

        // 根据用户Plus会员状态，更新用户信息
        UserPlusInfoEntity entity = userPlusInfoMapper.selectOne(new LambdaUpdateWrapper<UserPlusInfoEntity>()
                .eq(UserPlusInfoEntity::getUserId, userInfo.getUserId()));
        if (entity == null) {
            // 未开通Plus会员
            memberInformation.setIsplusMember(2);
        } else if (entity.getThaliStatus() == 1) {
            // 开通Plus会员
            memberInformation.setIsplusMember(1);
            memberInformation.setThaliType(entity.getThaliStatus());
        } else if (entity.getThaliStatus() == 0) {
            // Plus会员已过期
            memberInformation.setIsplusMember(0);
        }

        // 查询最高会员积分
        Integer maxMemberIntegral = baseServiceClient.getMaxMemberIntegral();
        if (maxMemberIntegral == null || presentIntegral >= maxMemberIntegral) {
            // 如果当前积分达到或超过最高会员积分，设置会员信息状态为最高级，并返回响应
            memberInformation.setCode(2);
            return ResponseResult.success("您已经是最高等级的会员", memberInformation);
        }

        // 查询当前会员与下一级会员的积分差距
        //获取下一级积分
        ResponseResult memberIntegralDistance = baseServiceClient.getMemberIntegralDistance(userInfo.getUserId());
        Map<String, Object> memberMap = (HashMap) memberIntegralDistance.getData();

        // 计算与下一级会员积分的差距
        //下一级积分
        Integer grade_rule = (Integer) memberMap.get("grade_rule");
        Integer f = grade_rule - presentIntegral;
        memberInformation.setDistanceIntegral(f);
        // 设置下一级会员等级名称
        memberInformation.setMemberNameNext((String) memberMap.get("grade_name"));
        // 设置会员信息状态为非最高级
        memberInformation.setCode(1);
        // 设置下一级会员积分规则
        memberInformation.setNextGradeRule(grade_rule);

        // 返回包含会员信息的响应结果
        return ResponseResult.success(memberInformation);
    }

    /**
     * 根据传入的参数映射检查电话号码的唯一性。
     * 通过调用基础Mapper接口的方法，查询并返回对应的用户ID。
     * 如果查找不到匹配的用户，则返回空字符串。
     *
     * @param paraMap 包含检查电话号码所需的参数的映射。
     *                参数映射的具体内容取决于实际的业务需求。
     * @return 如果找到匹配的用户，则返回该用户的ID；否则返回空字符串。
     */
    @Override
    public String checkPhoneNumber(Map<String, String> paraMap) {
        // 调用基础Mapper接口的方法，查询电话号码对应的用户ID
        String userId = this.baseMapper.checkPhoneNumber(paraMap);
        // 返回查询结果，即用户的ID
        return userId;
    }


    /**
     * 用户注册接口
     *
     * @param map 包含用户注册信息的Map，键值对形式，如手机号、验证码、密码等
     * @return ResponseResult 注册结果，成功或失败的信息以及可能的错误码
     * @throws UnknownHostException 如果获取IP地址失败抛出此异常
     * @throws TransactionException 如果注册过程中发生事务异常抛出此异常
     */
    @Override
    public ResponseResult businessegInsert(Map<String, String> map) throws UnknownHostException, TransactionException {
        // 从map中获取用户手机号和验证码
        String userMobile = map.get("userMobile");
        String phoneCode = map.get("phoneCode");
        // 获取电话区号
        String interDialingCode = com.houjiang.commons.util.StringUtils.isNotBlank(map.get("interDialingCode"))?map.get("interDialingCode"):"+86";

        // 从Redis中获取用户输入的验证码
        String sessionPhoneCode = RedisUtil.get(userMobile);

        // 校验验证码，如果不匹配返回错误信息
        if (StringUtils.isNotEmpty(sessionPhoneCode)) {
            if (!phoneCode.equals(sessionPhoneCode)) {
                return ResponseResult.fail(BizCodeEnume.INCORRECT_VERIFICATION_CODE.getCode(), BizCodeEnume.INCORRECT_VERIFICATION_CODE.getMsg());
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.VERIFICATION_CODE_OVERDUE.getCode(), BizCodeEnume.VERIFICATION_CODE_OVERDUE.getMsg());
        }

        // 检查手机号是否已注册
        String checkuserId = this.baseMapper.checkPhoneNumber(map);
        if (null != checkuserId && !checkuserId.isEmpty()) {
            return ResponseResult.fail(BizCodeEnume.PHONE_EXSIT_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXSIT_EXCEPTION.getMsg());
        }

        // 验证码验证通过后，从Redis中删除验证码
        RedisUtil.remove(userMobile);

        // 检查密码是否为空
        if (null == map.get("password") || "".equals(map.get("password"))) {
            return ResponseResult.fail(BizCodeEnume.PASSWORD_NOT_STANDARD.getCode(), BizCodeEnume.PASSWORD_NOT_STANDARD.getMsg());
        }
        String passwordStr = null;
        try {
            passwordStr = JSEncrypt.decrypt(map.get("password"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 对密码进行加密处理
        String password = PasswordUtils.BCryptPasswordEncoder(passwordStr);

        // 获取用户IP地址
        String actionIp = IpUtils.getIpAddress();

        // 创建用户信息实体，设置用户基本信息
        UserInfoEntity record = new UserInfoEntity();
        record.setUserMobile(userMobile);
        record.setPassword(password);
        record.setUserLastloginTime(new Date());
        record.setUserLastloginIp(actionIp);
        record.setInternationalDialingCode(interDialingCode);
        record.setRegisterTerminal(resolveRegisterTerminal(map, null));

        // 创建用户详情实体，设置注册时间、IP等
        UserInfoDetaileEntity insertUserInfoDetail = new UserInfoDetaileEntity();
        insertUserInfoDetail.setUserRegTime(System.currentTimeMillis() / 1000);
        insertUserInfoDetail.setUserRegIp(actionIp);
        insertUserInfoDetail.setUserPlatform(Integer.parseInt(map.get("platForm")));
        insertUserInfoDetail.setUserParentId(map.get("shareUserId"));

        // 创建用户绑定实体，设置绑定ID和类型
        UserBindConnectEntity userBindMap = new UserBindConnectEntity();
        String bindId = "mobile_" + userMobile;
        userBindMap.setBindId(bindId);
        userBindMap.setBindType(Byte.parseByte(CommonType.bindType.MOBILE.getCode().toString()));

        // 创建积分日志实体，记录用户注册行为产生的积分
        UserPointsLogEntity userPointLog = new UserPointsLogEntity();
        userPointLog.setPointsLogType(CommonType.pointsLogType.GAIN.getCode());
        userPointLog.setClassId(CommonType.pointType.REGISTRATION.getCode());
        userPointLog.setPointsLogTime(new Date());
        userPointLog.setPointsLogDesc(BizCodeEnume.MEMBER_REGISTRATION.getMsg());
        userPointLog.setPointsLogFlag("reg");

        // 执行用户信息插入操作，并处理异常
        if (Boolean.FALSE.equals(insertUserInfo(record, insertUserInfoDetail, userBindMap, userPointLog))) {
            return ResponseResult.fail(BizCodeEnume.REGISTRATION_FAIL.getMsg());
        }

        // 从Redis中获取用户ID
        String userId = RedisUtil.get(HouConfig.HOU_USER_LOGON_USERID + userMobile);
        try {
            // 调用支付服务接口，创建用户
            ResponseResult torf = paymentServiceClient.createUser(userId);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

        // 更新用户为企业用户
        Map<String, Object> businessegMap = new HashMap<>();
        businessegMap.put("userId", userId);
        this.baseMapper.businessUpdate(businessegMap);

        // 插入企业用户相关信息
        businessegMap.put("companyName", map.get("companyName"));
        businessegMap.put("taxId", map.get("taxId"));
        businessegMap.put("businessLicense", map.get("businessLicense"));
        userBusinessInfoMapper.insertBusinessUser(businessegMap);
        // 发送消息模板
        Map<String, Object> messageMap = new HashMap<>();
        List<String> messageList = new ArrayList<>();
        messageList.add(map.get("companyName"));
        messageMap.put("list", messageList);
        messageMap.put("businessName", map.get("companyName"));
        messageMap.put("templateId", CommonType.messageTemplate.CODE_K.getCode());
        baseServiceClient.sendMessageTemplate(messageMap);
        // 企业注册同样补个人空间：登记公司前账号没有任何组织成员关系，
        // 按组织隔离的功能会全部拒绝访问。这里不写会话，登录后按库里的成员关系补全。
        ensurePersonalOrganization(userId, map.get("companyName"), null);
        // 返回注册成功信息，包括用户ID
        return ResponseResult.success(BizCodeEnume.REGISTRATION_SUCCESS.getMsg(), userId);
    }


    /**
     * 根据传入的参数更新Ucenter模块的用户信息。
     *
     * @param paraMap 包含更新用户信息所需参数的映射。
     * @return 更新操作的结果，成功或失败。
     */
    @Override
    public ResponseResult updateUcenterInfo(Map<String, Object> paraMap) {
        try {
            // 从参数映射中提取token字符串
            String tokenString = paraMap.get("token").toString();
            // 使用token解析出用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(tokenString);

            // 初始化日期格式化工具
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            // 初始化用户生日变量
            String userBirth = null;
            // 如果参数映射中包含userBirth且不为空，则进行日期格式处理
            if (null != paraMap.get("userBirth") && !"".equals(paraMap.get("userBirth"))) {
                // 将userBirth字符串按"-"分割成数组
                String[] dateStrs = paraMap.get("userBirth").toString().split("-");
                // 对日期字符串数组中的每个元素进行长度检查和补0操作
                for (int i = 0; i < dateStrs.length; i++) {
                    if (dateStrs[i].length() == 1) {
                        dateStrs[i] = "0".concat(dateStrs[i]);
                    }
                }
                // 将处理后的日期字符串重新组合
                String fomDate = dateStrs[0] + "-" + dateStrs[1] + "-" + dateStrs[2];
                // 将字符串日期转换为Date对象
                Date date = simpleDateFormat.parse(fomDate);
                // 将Date对象格式化为字符串
                userBirth = simpleDateFormat.format(date);
            }

            // 将用户ID和处理后的生日信息放入参数映射中
            paraMap.put("userId", userInfo.getUserId());
            paraMap.put("userBirth", userBirth);

            // 调用userInfoDetailMapper更新用户信息
            Integer torf = userInfoDetailMapper.updateUcenterInfo(paraMap);
            // 如果更新成功（影响行数为1），则更新缓存中的用户信息并返回成功结果
            if (1 == torf) {
                UserInfoVO ui = this.baseMapper.getUserInfoByUserId(userInfo.getUserId());
                RedisUtil.set(HouConfig.HOU_USER_TOKEN_USER + tokenString, objectMapper.writeValueAsString(ui),
                        HouConfig.TOKEN_OUTTIME);
                return ResponseResult.success("修改成功");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // 如果出现异常或更新失败，返回系统错误的失败结果
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
    }

    /**
     * 校验支付转账信息的合法性。
     * 通过传入的参数校验收款账号是否存在以及是否为本人账号，确保支付操作的正确性。
     *
     * @param map 包含转账相关信息的Map对象，其中必须包含token和payeeAccount字段。
     *            token用于验证用户身份，payeeAccount为收款账号。
     * @return 返回校验结果，如果收款账号不存在或为本人账号，则返回失败结果；
     * 否则，返回成功结果并携带收款用户ID。
     */
    @Override
    public ResponseResult checkPayTransferInfo(Map<Object, Object> map) {
        // 从map中获取token
        String token = (String) map.get("token");
        // 检查token是否存在
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExistToken) {
            // 通过token获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            // 获取当前用户ID
            String userId = userInfo.getUserId();
            // 从map中获取收款账号
            // 收款人账号
            String payeeAccount = (String) map.get("payeeAccount");
            // 查询收款账号对应的用户ID
            String rpcUserId = this.baseMapper.queryPayeeAccountUserId(payeeAccount);
            if (rpcUserId == null) {
                // 如果收款账号不存在，返回失败结果
                return ResponseResult.fail(0, "账号不存在，请输入正确账号或确认账号是否正确！");
            } else if (userId.equals(rpcUserId)) {
                // 如果收款账号为本人账号，返回失败结果
                return ResponseResult.fail(0, "请勿给本账号转账！");
            } else {
                // 如果收款账号存在且不是本人账号，返回成功结果并携带收款用户ID
                return ResponseResult.success(rpcUserId);
            }
        }
        // 如果token不存在，返回失败结果，提示token无效
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
    }

    /**
     * 处理用户登出请求。
     * <p>
     * 本函数旨在接收包含用户令牌（token）的参数映射，通过从Redis中移除相应的用户令牌，
     * 实现用户登出功能。用户令牌是用于识别和验证用户身份的关键信息，登出操作即为撤销此令牌。
     *
     * @param paraMap 包含请求参数的映射，其中必须包含键为"token"的条目，该条目的值是用户的令牌。
     * @return 返回一个表示操作成功的ResponseResult对象。
     */
    @Override
    public ResponseResult userLogOut(Map<String, String> paraMap) {
        // 从参数映射中获取用户令牌
        String token = paraMap.get("token");
        // 从Redis中移除用户的令牌，实现登出功能
        RedisUtil.remove(HouConfig.HOU_USER_TOKEN_USER + token);

        // 返回表示操作成功的响应结果
        return ResponseResult.success();
    }

    /**
     * 根据传入的参数获取用户信息。
     *
     * @param paraMap 包含请求参数的映射，预期包含token以验证用户身份。
     * @return 返回包含用户信息的响应结果，如果用户信息不可用，则返回失败响应。
     * @throws IllegalAccessException 如果访问参数失败，抛出此异常。
     */
    @Override
    public ResponseResult getUser(Map<String, String> paraMap) throws IllegalAccessException {
        // 从参数映射中提取token
        String token = paraMap.get("token");
        // 使用token解析用户信息，如果token无效，返回失败响应
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(token);
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 检查用户是否绑定了openid
        String bindOpenid = userInfo.getBindOpenid();
        if (!StringUtils.isEmpty(bindOpenid)) {
            // 根据openid获取用户昵称和头像
            Map<String, Object> outMap = userBindConnectService.getMap(new LambdaQueryWrapper<UserBindConnectEntity>()
                    .eq(UserBindConnectEntity::getBindOpenid, bindOpenid));
            if (outMap != null) {
                if (outMap.get("bind_nickname") != null) {
                    userInfo.setUserNickname(outMap.get("bind_nickname").toString());
                }
                if (null != outMap.get("bind_avator") && !StringUtils.isEmpty(outMap.get("bind_avator").toString())) {
                    userInfo.setUserAvatar(outMap.get("bind_avator").toString());
                }
            }
        }
        // 默认店铺类型为卖家店铺
        Integer shopType = CommonType.storeType.SELLER_STORE.getCode();
        // 如果用户有店铺，获取店铺类型
        if (userInfo.getShopId() != null) {
            Map<String, Object> map = this.baseMapper.selectByShopInfo(userInfo.getShopId());
            if (map != null) {
                shopType = Integer.valueOf(map.get("shopType").toString());
                userInfo.setShopType(shopType);
            }else{
                userInfo.setShopType(CommonType.storeType.SUPPLIER_STORE.getCode());
            }
        }
        // 构建包含店铺信息的映射，用于查询店铺状态
        Map<String, String> shopMap = new HashMap<>();
        shopMap.put("userId", userInfo.getUserId() + "");
        // 开店成功
        shopMap.put("shopStatus", CommonType.storeStatus.SUCCESSFULLY.getCode() + "");
        // 买家店铺
        shopMap.put("shopType", shopType + "");
        // 查询店铺状态
        //查询是否正常商家 或者 子账号
        ResponseResult shopResult = merchantServiceClient.queryShopStatus(shopMap);
        if ((null != shopResult.getData()) ) {
            ShopDetailVO shopDetailVO = JSON.parseObject(JSON.toJSONString(shopResult.getData()), ShopDetailVO.class);
            if (shopDetailVO != null) {
                // 根据店铺自建状态，调整用户权限
                if (Objects.equals(shopDetailVO.getShopSelfSupport(), "false")) {
                    // 根据店铺活动限制用户功能
                    List<String> marketingActivitiesList = merchantServiceClient.queryMarketingActivitiesList(shopDetailVO.getShopId());
                    List<String> routerList = getList(userInfo.getLimits(), marketingActivitiesList);
                    userInfo.setLimits(StringUtils.join(routerList, ","));
                }
                // 更新用户店铺等级和标记用户为店铺所有者
                userInfo.setShopGradeId(shopDetailVO.getShopGradeId());
                userInfo.setIsShop(1);
            } else {
                // 下游返回空店铺资料时，不能阻断用户信息接口，更不能被误判为登录失效
                userInfo.setIsShop(2);
            }
        } else if ( userInfo.getSellerIsAdmin() != null && StringUtils.isNotBlank(userInfo.getShopId()) && userInfo.getSellerIsAdmin() == 0 ) {
            GdsShopDetailResultVO shopDetailResultVO = merchantServiceClient.queryShopinfoByShopId(Integer.valueOf(userInfo.getShopId()));
            if ((null != shopDetailResultVO) ) {
                // 根据店铺自建状态，调整用户权限
                if (Objects.equals(shopDetailResultVO.getShopSelfSupport(), "false")) {
                    // 根据店铺活动限制用户功能
                    List<String> marketingActivitiesList = merchantServiceClient.queryMarketingActivitiesList(Integer.valueOf(userInfo.getShopId()));
                    List<String> routerList = getList(userInfo.getLimits(), marketingActivitiesList);
                    userInfo.setLimits(StringUtils.join(routerList, ","));
                }
                // 更新用户店铺等级和标记用户为店铺所有者
                userInfo.setShopGradeId(shopDetailResultVO.getShopGradeId());
                userInfo.setIsShop(1);
            }
        }else{
            // 如果查询店铺失败，标记用户不是店铺所有者
            userInfo.setIsShop(2);
        }
        // 判断用户是否为微客（表缺失或查询失败时不阻断 getuser）
        try {
            VKUserInfoVO vkUserInfo = this.baseMapper.queryVkUserInfo(userInfo.getUserId());
            if (null != vkUserInfo && vkUserInfo.getId() != null) {
                userInfo.setVkState(1);
            } else {
                userInfo.setVkState(2);
            }
        } catch (Exception e) {
            logger.warn("queryVkUserInfo failed, soft set vkState=2, userId={}, err={}",
                    userInfo.getUserId(), e.getMessage());
            userInfo.setVkState(2);
        }
        // 如果用户是品牌店铺管理员，调整用户权限
        //品牌店铺先隐藏 账号设置和门店  chainAccount 门店  permissionGroup账号
        if (userInfo.getShopType() != null && userInfo.getSellerIsAdmin() != null && userInfo.getSellerIsAdmin() == 1) {
            if (userInfo.getShopType() == 3) {
                String s = userSellerGroupMapper.selectById(2).getLimits();
                List<String> marketingActivitiesList = merchantServiceClient.queryMarketingActivitiesList(Integer.valueOf(userInfo.getShopId()));
                List<String> routerList = getList(s, marketingActivitiesList);
                userInfo.setLimits(StringUtils.join(routerList, ","));
            }
        }
        // 移除不适用于用户的角色的功能限制 webpos
        List<String> list = new ArrayList<>(Arrays.asList("webPos", "weighGoods", "webPosBalance", "webposSalesDetails"));
        removeLimit(list, userInfo);
        // 移除不适用于用户的角色的功能限制 仓储
        List<String> list2 = new ArrayList<>(Arrays.asList("storageInventory", "storageWarehousing", "storageOutbound", "storageAllot", "storageWarehouse"));
        removeLimit2(list2, userInfo,CommonType.configKey.STORAGE_SWITCH.getDesc());
        // 根据门店是否存在，调整骑手功能的可见性
        // 门店id 不为空 证明是门店 根据骑手是否开启 显示 或 隐藏 骑手权限
        if (null != userInfo.getShopGateId() && !userInfo.getShopGateId().isEmpty()) {
            removeRiderLimit(userInfo);
        }
        // 根据用户Plus会员状态，更新用户信息
        UserPlusInfoEntity entity = userPlusInfoMapper.selectOne(new LambdaUpdateWrapper<UserPlusInfoEntity>()
                .eq(UserPlusInfoEntity::getUserId, userInfo.getUserId()));
        if (entity == null) {
            // 未开通Plus会员
            userInfo.setPlusMember(2);
        } else if (entity.getThaliStatus() == 1) {
            // 开通Plus会员
            userInfo.setPlusMember(1);
            userInfo.setThaliType(entity.getThaliStatus());
        } else if (entity.getThaliStatus() == 0) {
            // Plus会员已过期
            userInfo.setPlusMember(0);
        }
        // 将用户信息转换为Map格式，方便扩展字段
        // UserInfo 转 map 方便扩展字段 不能在UserInfo中新增字段否则会影响其他服务token 解析
        //将对象转换为map
        Map data = ParseMapUtils.beanToMap(userInfo);
        // 添加未读消息数量到响应结果
        //未读消息数量
        data.put("unreadMessageNum", userPlatformMessageMapper.selectCount(new LambdaQueryWrapper<UserPlatformMessageEntity>()
                .eq(UserPlatformMessageEntity::getIsReader, CommonType.commonStatus.NO.getCode())
                .eq(UserPlatformMessageEntity::getUserId, userInfo.getUserId())));
        // 返回包含用户信息的响应结果
        return ResponseResult.success(data);
    }

    private static List<String> getList(String limits, List<String> marketingActivitiesList) {
        List<String> routerList = new ArrayList<>(Arrays.asList(limits.split(",")));
        if (!marketingActivitiesList.contains("1")) {
            routerList.remove("vouchermanage");
        }
        if (!marketingActivitiesList.contains("2")) {
            routerList.remove("discount");
        }
        if (!marketingActivitiesList.contains("3")) {
            routerList.remove("seckill");
        }
        if (!marketingActivitiesList.contains("4")) {
            routerList.remove("meetConditionGift");
        }
        return routerList;
    }

    /**
     * 移除用户的限制条件。
     * 如果系统配置中webPos开关为关闭状态，则根据传入的限制条件列表，从用户权限列表中移除相应限制条件。
     * 这样做是为了在特定情况下，动态调整用户的权限限制，提高系统的灵活性。
     *
     * @param limitList 限制条件列表，用于比对和移除用户的限制条件。
     * @param userInfo  用户信息对象，包含用户的权限列表等信息。
     */
    private void removeLimit(List<String> limitList, UserInfoVO userInfo) {
        // 检查用户是否有权限限制
        if (null != userInfo.getLimits()) {
            // 将用户的权限限制字符串转换为列表形式方便处理
            List<String> limits = new ArrayList<>(Arrays.asList(userInfo.getLimits().split(",")));
            // 通过基础服务客户端查询系统配置中webPos开关的状态
            ResponseResult map = baseServiceClient.detectingApplicationStatus();
            List<Map<String, Object>> configMapList = (List<Map<String, Object>>) map.getData();
            Boolean flag = false;
            Boolean imFlag = false;
            if (null != configMapList ) {
                for (Map<String, Object> stringObjectMap : configMapList) {
                    if (null != stringObjectMap && stringObjectMap.get("configKey").equals("门店收银")) {
                        flag = (Boolean) stringObjectMap.get("configStatus");
                    }
                    if (null != stringObjectMap && stringObjectMap.get("configKey").equals("IM客服")) {
                        imFlag = (Boolean) stringObjectMap.get("configStatus");
                    }
                }
            }

            if(CommonType.ShopType.BRAND_SHOP.getCode().equals(userInfo.getShopType())){
                // 品牌店铺移除 结算 批发市场
                List<String> list = new ArrayList<>(Arrays.asList("supplierGoods","supplierOrderList","invoiceManage","supplierStore","YYTorderDetails","settlementList","statementAccount","treatWithdrawalList","withdrawnListsupplierGoods","withdrawnList"));
                for (String limit : list) {
                    // 如果用户权限列表中包含当前限制条件
                    if (limits.contains(limit)) {
                        // 移除该限制条件
                        limits.remove(limit);
                        // 更新用户权限列表字符串形式
                        userInfo.setLimits(StringUtils.join(limits, ","));
                    }
                }
                // 品牌店铺 不展示 webpost
                flag = false;
            }
            // 如果webPos开关为关闭状态
            if (!flag) {
                // 遍历限制条件列表
                for (String limit : limitList) {
                    // 如果用户权限列表中包含当前限制条件
                    if (limits.contains(limit)) {
                        // 移除该限制条件
                        limits.remove(limit);
                        // 更新用户权限列表字符串形式
                        userInfo.setLimits(StringUtils.join(limits, ","));
                    }
                }
            }
            // 处理iM客服服务
            if(!imFlag){
                // 移除对应权限
                limits.remove("customerService");
                // 更新用户权限列表字符串形式
                userInfo.setLimits(StringUtils.join(limits, ","));
            }
        }
    }

    /**
     * 移除用户的限制条件。
     * 如果系统配置中webPos开关为关闭状态，则根据传入的限制条件列表，从用户权限列表中移除相应限制条件。
     * 这样做是为了在特定情况下，动态调整用户的权限限制，提高系统的灵活性。
     *
     * @param limitList 限制条件列表，用于比对和移除用户的限制条件。
     * @param userInfo  用户信息对象，包含用户的权限列表等信息。
     */
    private void removeLimit2(List<String> limitList, UserInfoVO userInfo, String key) {
        // 检查用户是否有权限限制
        if (null != userInfo.getLimits()) {
            // 将用户的权限限制字符串转换为列表形式方便处理
            List<String> limits = new ArrayList<>(Arrays.asList(userInfo.getLimits().split(",")));
            // 通过基础服务客户端查询系统配置中webPos开关的状态
            // 查询是否开启
            ResponseResult map = baseServiceClient.selectByConfigKey(key);
            // 提取配置值
            Map<String, Object> configMap = (Map<String, Object>) map.getData();
            String value = configMap.get("configValue").toString();
            // 如果开关为关闭状态
            if (value.equals("0")) {
                // 遍历限制条件列表
                for (String limit : limitList) {
                    // 如果用户权限列表中包含当前限制条件
                    if (limits.contains(limit)) {
                        // 移除该限制条件
                        limits.remove(limit);
                        // 更新用户权限列表字符串形式
                        userInfo.setLimits(StringUtils.join(limits, ","));
                    }
                }
            }
        }
    }

    /**
     * 移除用户骑手限制。
     * 如果当前业务环境未开启骑手功能，或者店铺本身不支持骑手配送，则移除用户权限列表中的骑手限制。
     * 这确保了用户不会在无法使用骑手功能的情况下看到相关的限制提示。
     *
     * @param userInfo 用户信息，包含用户的权限列表和店铺ID。
     */
    private void removeRiderLimit(UserInfoVO userInfo) {
        // 初始化一个列表，包含需要检查的权限名
        List<String> limitList = new ArrayList<>(Collections.singletonList("chain_rider"));
        // 检查用户是否有权限限制
        if (null != userInfo.getLimits()) {
            Boolean flag = false;
            // 将用户的权限限制转换为列表形式
            List<String> limits = new ArrayList<>(Arrays.asList(userInfo.getLimits().split(",")));
            // 查询当前业务环境是否开启了骑手功能
            // 查询是否开启骑手
            ResponseResult map = baseServiceClient.detectingApplicationStatus();
            List<Map<String, Object>> configMapList = (List<Map<String, Object>>) map.getData();
            // 遍历配置列表，查找骑手功能的开启状态
            for (Map<String, Object> stringObjectMap : configMapList) {
                if (null != stringObjectMap && stringObjectMap.get("configKey").equals("骑手配送")) {
                    flag = (Boolean) stringObjectMap.get("configStatus");
                }
            }
            // 查询店铺是否支持骑手配送
            // 查询当前店铺是否开启 骑手
            Boolean shopRiderSupport = this.baseMapper.queryshopRiderSupport(userInfo.getShopId());
            // 如果店铺支持骑手配送，则以店铺配置为准更新标志
            if (null != shopRiderSupport && flag) {
                flag = shopRiderSupport;
            }
            // 如果骑手功能未开启，则从用户权限列表中移除骑手限制
            if (!flag) {
                for (String limit : limitList) {
                    if (limits.contains(limit)) {
                        limits.remove(limit);
                        // 更新用户权限列表，并保存更改
                        userInfo.setLimits(StringUtils.join(limits, ","));
                    }
                }
            }
        }
    }


    /**
     * 上传图片方法
     *
     * @param file  需要上传的图片文件
     * @param token 用户的令牌，用于验证用户身份
     * @return 返回上传结果，包括图片的URL
     */
    @Override
    public ResponseResult uploadImg(MultipartFile file, String token) {
        try {
            // 从Redis中根据token获取用户信息
            String userInfoString = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
            UserInfoDTO userInfo = objectMapper.readValue(userInfoString, UserInfoDTO.class);

            // 调用第三方服务上传图片到OSS，并获取图片URL
            ResponseResult resultResp = thirdPartyServiceClient.uploadToOssFile(file);
            String photoUrl = (String) resultResp.getData();

            // 将图片URL保存到数据库，并更新用户信息
            Boolean torf = uploadPhoto(userInfo.getUser().getUserId(), photoUrl);
            if (torf) {
                String mobilePhone = userInfo.getUser().getUserMobile();
                UserInfoVO userInfoVO = new UserInfoVO();
                userInfoVO.setUserMobile(mobilePhone);
                userInfoVO.setLoginType(CommonType.commonStatus.NO.getCode());
                UserInfoVO newUserInfo = this.baseMapper.querUserInfoByParams(userInfoVO);

                // 初始化店铺相关信息
                Map<String, String> shopMap = new HashMap<>();
                shopMap.put("userId", userInfo.getUser().getUserId() + "");
                shopMap.put("shopStatus", CommonType.storeStatus.SUCCESSFULLY.getCode() + "");
                shopMap.put("shopType", CommonType.storeType.SELLER_STORE.getCode() + "");

                // 查询用户店铺状态
                ResponseResult shopResult = merchantServiceClient.queryShopStatus(shopMap);
                if (null != shopResult.getData()) {
                    newUserInfo.setIsShop(CommonType.commonStatus.YES.getCode());
                } else {
                    newUserInfo.setIsShop(CommonType.commonStatus.NO.getCode());
                }
                userInfo.setUser(newUserInfo);
                // 更新Redis中的用户信息
                RedisUtil.set(HouConfig.HOU_USER_TOKEN_USER + token, objectMapper.writeValueAsString(userInfo),
                        HouConfig.TOKEN_OUTTIME);
                return ResponseResult.success(photoUrl);
            }
            return ResponseResult.fail();
        } catch (Exception e) {
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 上传图片的接口，接受Base64编码的图片数据。
     *
     * @param paraMap 包含上传所需参数的映射，预期包含token和base64Str等关键信息。
     * @return 返回上传结果的响应体，包含成功与否的信息和图片URL。
     */
    @Override
    public ResponseResult uploadImgBase64(Map<String, String> paraMap) {
        try {
            // 从参数映射中提取token
            String tokenString = paraMap.get("token");
            // 从Redis中根据token获取用户信息
            String userInfoString = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + tokenString);
            // 反序列化用户信息
            UserInfoDTO userInfo = objectMapper.readValue(userInfoString, UserInfoDTO.class);

            // 初始化OSS上传参数
            OssUploadUpdateParamDTO oss = new OssUploadUpdateParamDTO();
            // 检查是否提供了Base64编码的图片数据
            if (paraMap.get("base64Str") == null) {
                return ResponseResult.fail(BizCodeEnume.PLEASE_SELECT_AN_IMAGE);
            }
            // 设置Base64编码的图片数据和默认的文件名后缀
            oss.setBase64Str(paraMap.get("base64Str"));
            oss.setStreamName(".png");
            // 调用第三方服务上传图片
            ResponseResult resultResp = thirdPartyServiceClient.uploadPic(oss);
            // 从上传结果中提取图片URL
            String photoUrl = (String) resultResp.getData();

            // 将图片URL上传到数据库
            Boolean torf = uploadPhoto(userInfo.getUser().getUserId(), photoUrl);
            if (torf) {
                // 构建更新后的用户信息VO
                String mobilePhone = userInfo.getUser().getUserMobile();
                UserInfoVO userInfoVO = new UserInfoVO();
                userInfoVO.setUserMobile(mobilePhone);
                userInfoVO.setLoginType(CommonType.commonStatus.NO.getCode());
                // 根据用户信息VO查询用户详情
                UserInfoVO newUserInfo = this.baseMapper.querUserInfoByParams(userInfoVO);

                // 准备店铺相关信息
                Map<String, String> shopMap = new HashMap<>();
                shopMap.put("userId", userInfo.getUser().getUserId() + "");
                shopMap.put("shopStatus", CommonType.storeStatus.SUCCESSFULLY.getCode() + "");
                shopMap.put("shopType", CommonType.storeType.SELLER_STORE.getCode() + "");

                // 查询用户店铺状态
                ResponseResult shopResult = merchantServiceClient.queryShopStatus(shopMap);
                // 根据查询结果更新用户是否为商家的标志
                if (null != shopResult.getData()) {
                    newUserInfo.setIsShop(CommonType.commonStatus.YES.getCode());
                } else {
                    newUserInfo.setIsShop(CommonType.commonStatus.NO.getCode());
                }
                // 更新用户信息并刷新token的过期时间
                userInfo.setUser(newUserInfo);
                RedisUtil.set(HouConfig.HOU_USER_TOKEN_USER + tokenString, objectMapper.writeValueAsString(userInfo),
                        HouConfig.TOKEN_OUTTIME);
                // 返回上传成功的响应，包含图片URL
                return ResponseResult.success(photoUrl);
            }
            // 如果上传失败，返回失败的响应
            return ResponseResult.fail();
        } catch (Exception e) {
            // 如果过程中发生异常，返回包含异常信息的失败响应
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 根据传入的Map检查支付转账信息。
     * 通过token验证用户身份，并根据收款账号查询用户信息，以确认收款人是否存在以及是否为同一用户。
     *
     * @param map 包含token和收款账号等信息的Map对象。
     * @return 根据查询结果返回相应的ResponseResult对象，包含成功或失败的信息。
     */
    @Override
    public ResponseResult checkPayTransferInfoByMap(Map<Object, Object> map) {
        // 从map中获取token
        String token = (String) map.get("token");
        // 检查token在Redis中的存在性
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExistToken) {
            try {
                // 使用ObjectMapper从Redis中获取并解析用户信息
                ObjectMapper mapper = new ObjectMapper();
                // 获取user信息
                String userinfo = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
                // String转object
                UserInfoDTO dto = mapper.readValue(userinfo, UserInfoDTO.class);
                // 获取当前用户ID
                String userId = dto.getUser().getUserId();
                // 从map中获取收款账号
                // 收款人账号
                String payeeAccount = (String) map.get("payeeAccount");
                // 根据收款账号查询用户信息
                UserInfoVO rpcUserInfo = this.baseMapper.queryUserId(payeeAccount);
                if (rpcUserInfo == null) {
                    // 如果收款账号不存在，返回相应的错误信息
                    return ResponseResult.fail(BizCodeEnume.ACCOUNT_IS_NOT_EXIST);
                } else if (userId.equals(rpcUserInfo.getUserId())) {
                    // 如果收款账号是当前用户，返回相应的错误信息
                    return ResponseResult.fail(BizCodeEnume.DANGEROUS_TRANSFER_OF_ACCOUNT);
                } else {
                    // 如果收款账号存在且不是当前用户，返回查询到的用户信息
                    return ResponseResult.success(rpcUserInfo);
                }
            } catch (Exception e) {
                // 记录异常信息，并返回失败的响应结果
                logger.info("houPayForTransferCheckPayeeInfoErrorResults：" + e.getMessage());
                return ResponseResult.fail(e.getMessage());
            }
        } else {
            // 如果token无效，返回相应的错误信息
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    /**
     * 根据用户ID获取用户中心设置信息。
     * <p>
     * 本方法通过用户ID从数据库中查询用户的设置信息。如果用户最近登录的IP地址被记录，
     * 尝试根据该IP地址查询并更新用户所在的省份、城市和区域信息。
     * </p>
     *
     * @param param 包含用户令牌的映射表，用于提取用户ID。
     * @return 如果查询成功，返回包含用户设置信息的ResponseResult对象；
     * 如果查询失败或更新地理位置信息时出现异常，返回失败的ResponseResult对象。
     */
    @Override
    public ResponseResult getUcenterSettingByUserId(Map<String, String> param) {
        // 从Redis中获取用户ID，用户ID由令牌(token)唯一标识
        String userId = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USERID + param.get("token"));
        // 根据用户ID查询用户设置信息
        UserSettingVO ucenterSettingInfo = this.baseMapper.getUcenterSettingByUserId(userId);
        // 如果用户设置信息不存在，返回失败结果
        if (null == ucenterSettingInfo) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), BizCodeEnume.QUERY_USER_FAIL.getMsg());
        }
        // 返回成功结果，包含查询到的用户设置信息
        return ResponseResult.success(ucenterSettingInfo);
    }

    /**
     * 更新用户手机号码。
     * 该方法用于验证用户输入的验证码是否正确，并在验证通过的情况下更新用户的绑定手机信息。
     *
     * @param paraMap 包含验证代码、用户手机号码、用户令牌等信息的参数映射。
     * @return 返回操作结果，成功或失败，并附带相应的错误信息。
     */
    @Override
    public ResponseResult updateUserMobile(Map<String, String> paraMap) {
        // 从参数映射中提取手机验证码和用户手机号码。
        String phoneCode = paraMap.get("phoneCode");
        String userMobile = paraMap.get("userMobile");

        // 从Redis中获取存储的手机验证码，并与用户输入的验证码进行比对。
        String redisPhoneCode = RedisUtil.get(userMobile);
        if (!phoneCode.equals(redisPhoneCode)) {
            // 验证码不匹配时，返回错误信息。
            return ResponseResult.fail(BizCodeEnume.INCORRECT_VERIFICATION_CODE);
        }

        // 从参数映射中提取用户令牌。
        String tokenString = paraMap.get("token");

        // 从Redis中获取存储的用户信息，基于用户令牌。
        String userinfoString = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + tokenString);
        UserInfoDTO dto = JSON.parseObject(userinfoString, UserInfoDTO.class);
        if (dto == null) {
            // 用户信息不存在时，返回错误信息。
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }

        // 获取用户信息，并从用户信息中提取当前绑定的手机号码。
        UserInfoVO userInfo = dto.getUser();
        String oldUserMobilePhone = userInfo.getUserMobile();
        // 将当前绑定的手机号码存入参数映射，以备后续使用。
        paraMap.put("oldUserMobilePhone", oldUserMobilePhone);

        // 当前绑定的手机号码为空时，返回错误信息。
        if (StringUtils.isEmpty(oldUserMobilePhone)) {
            return ResponseResult.fail(BizCodeEnume.ORIGINAL_PHONE_NUMBER_IS_EMPTY);
        }

        // 调用方法更新用户绑定的手机信息。
        Boolean torf = updateUserBindConnectByUserMobile(paraMap);
        if (torf) {
            // 更新成功时，从数据库中重新获取用户信息。
            UserInfoVO userInfoMapper = this.baseMapper.getUserInfoByPhoneNumber(userMobile);

            // 准备查询商店状态的参数。
            Map<String, String> shopMap = new HashMap<>();
            shopMap.put("userId", userInfo.getUserId());

            // 查询商店状态。
            ResponseResult status = merchantServiceClient.queryShopStatus(shopMap);
            if (null != status.getData()) {
                // 根据商店状态设置用户是否为商家。
                userInfoMapper.setIsShop(CommonType.commonStatus.YES.getCode());
            } else {
                userInfoMapper.setIsShop(CommonType.commonStatus.NO.getCode());
            }

            // 更新Redis中的用户信息。
            dto.setUser(userInfoMapper);
            RedisUtil.set(HouConfig.HOU_USER_TOKEN_USER + tokenString,
                    JSON.toJSONString(dto), 7200);

            // 返回操作成功的响应。
            return ResponseResult.success(BizCodeEnume.SUCCESSFULLY_CHANGED_THE_PHONE_NUMBER.getMsg());
        }
        // 更新失败时，返回操作失败的响应。
        return ResponseResult.success(BizCodeEnume.FAILED_CHANGED_THE_PHONE_NUMBER.getMsg());
    }


    /**
     * 生成用户二维码。
     * 通过传入的参数映射表，获取用户信息，并生成带有用户ID的二维码链接。
     * 最后返回包含二维码链接和Base64编码字符串的结果。
     *
     * @param paraMap 参数映射表，包含token信息。
     * @return 包含二维码链接和Base64编码字符串的响应结果。
     * @throws NotFoundException 如果根据token找不到用户信息，则抛出NotFoundException。
     * @throws IOException       如果生成二维码过程中发生I/O错误，则抛出IOException。
     * @throws WriterException   如果生成二维码过程中发生编码错误，则抛出WriterException。
     */
    @Override
    public ResponseResult qrUser(Map<String, String> paraMap) throws NotFoundException, IOException, WriterException {
        // 从参数映射表中获取token
        String token = paraMap.get("token");
        // 根据token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 获取用户ID，并转换为字符串
        String userId = userInfo.getUserId();
        // 构建二维码链接，包含用户ID
        String url = HouConfig.QR_CODE_REGISTER_USERID + userId;
        // 将二维码链接转换为Base64编码字符串
        String baseString = QRCodeUtil.text2Base64Str(url);
        // 创建返回的映射表，包含二维码链接和Base64编码字符串
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("url", url);
        returnMap.put("baseString", baseString);
        // 返回成功的响应结果，包含二维码链接和Base64编码字符串
        return ResponseResult.success(returnMap);
    }


    /**
     * 根据商店ID查询客户信息。
     * 该方法通过解析传入参数中的token，获取用户信息，并根据商店ID查询该商店的客户列表。
     * 对查询结果进行处理后，返回客户列表及总数。
     *
     * @param paraMap 包含请求参数的映射，其中必须包含token。
     * @return 返回查询结果的响应对象，包含客户列表和总数。
     */
    @Override
    public ResponseResult myCustomer(Map<String, Object> paraMap) {
        // 从参数映射中提取token
        String token = paraMap.get("token").toString();
        // 使用token解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 提取商店ID，并转换为字符串
        String shopId = userInfo.getShopId();
        // 将商店ID添加到参数映射中
        paraMap.put("shopId", shopId);
        Map map = null;
        try {
            // 将参数映射转换为bean的Map形式
            map = ParseMapUtils.beanToMap(paraMap);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        // 根据商店ID和分页信息查询客户列表
        IPage<MyCustomerVO> page = this.baseMapper.listCustomerByShopId(new Query<MyCustomerVO>().getPage(map),
                paraMap);
        // 获取查询结果的记录列表
        List<MyCustomerVO> listCustomerByShopId = page.getRecords();
        // 格式化日期的SimpleDateFormat实例
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 遍历客户列表，格式化注册时间
        for (MyCustomerVO myCustomer : listCustomerByShopId) {
            Long time = Long.valueOf(myCustomer.getUserRegTime());
            String d = sdf.format(time * 1000);
            myCustomer.setUserRegTime(d);
        }
        // 构建返回的映射，包含客户列表和总数
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("listCustomerByShopId", listCustomerByShopId);
        returnMap.put("countListCustomerByShopId", page.getTotal());
        // 返回成功的响应结果，包含客户列表和总数
        return ResponseResult.success(returnMap);
    }


    /**
     * 根据用户token获取用户信息。
     *
     * @param paraMap 参数映射表，包含token等必要信息。
     * @return 用户信息的响应结果。
     */
    @Override
    public ResponseResult getUserInfo(Map<String, Object> paraMap) {
        // 从参数映射中提取token
        String token = String.valueOf(paraMap.get("token"));
        // 通过token解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 根据用户ID从数据库中获取用户详细信息
        UserInfoForUpdateVO userInfoVo = this.baseMapper.getUserInfo(userInfo.getUserId());

        // 查询用户的会员等级信息
        Map<String, Object> memberClassIdByUserId = baseServiceClient.queryMemberShipGradeByUserId(userInfo.getUserId());
        // 如果会员等级信息存在
        if (null != memberClassIdByUserId) {
            // 如果等级名称存在，设置到用户信息中
            if (null != memberClassIdByUserId.get("grade_name")) {
                userInfoVo.setMemberName(memberClassIdByUserId.get("grade_name").toString());
            }
            // 如果等级URL存在，设置到用户信息中
            if (null != memberClassIdByUserId.get("grade_url")) {
                userInfoVo.setMemberUrl(memberClassIdByUserId.get("grade_url").toString());
            }
        }

        // 查询用户的组织信息
        // 从hou_user_organization表读取用户的所有组织关联，然后关联hou_base_organization表获取组织详细信息
        // 排序规则：is_primary=1的主组织排在前面，然后按创建时间倒序
        try {
            logger.info("开始查询用户组织信息，userId: " + userInfo.getUserId());
            List<Map<String, Object>> orgList = baseMapper.getUserOrgList(userInfo.getUserId());
            
            if (orgList != null && !orgList.isEmpty()) {
                // 将组织信息转换为List<Map<String, Object>>格式，保留所有字段包括is_primary
                // 已经按is_primary DESC排序，主组织（is_primary=1）会排在前面
                userInfoVo.setOrgList(orgList);
                logger.info("从hou_user_organization表查询的组织信息，数量: " + orgList.size() + ", 内容: " + orgList);
            } else {
                // 用户没有关联任何组织
                logger.info("用户没有关联任何组织，设置空的orgList");
                userInfoVo.setOrgList(new ArrayList<>());
            }
        } catch (Exception e) {
            logger.error("查询用户组织信息异常", e);
            // 组织信息查询失败不影响用户信息返回
        }

        // 返回成功的响应结果，包含用户信息
        return ResponseResult.success(userInfoVo);
    }

    /**
     * 更新用户信息。
     * 通过传入的Map参数更新用户的基本信息、详细信息以及绑定信息。涉及三个数据表的更新操作。
     * 在更新成功后，还会更新Redis中的用户信息缓存。
     *
     * @param map 包含需要更新的用户信息的键值对，如用户昵称、真实姓名、性别、生日等。
     * @return 更新操作的结果，成功返回success，失败返回fail，并可能包含错误信息。
     */
    @Override
    public ResponseResult updateUserInfo(Map<String, String> map) {
        // 从请求参数中获取用户token
        String token = map.get("token");
        // 通过token从Redis中获取用户ID
        String userId = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USERID + token);

        // 初始化日期格式化工具
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String userBirth = null;
        try {
            // 如果用户提交了生日信息，则进行处理并格式化
            if (null != map.get("userBirth") && !"".equals(map.get("userBirth"))) {
                // 将生日字符串按“-”分割，并确保日期部分的数字都是两位
                String[] dateStrs = map.get("userBirth").split("-");
                for (int i = 0; i < dateStrs.length; i++) {
                    if (dateStrs[i].length() == 1) {
                        dateStrs[i] = "0".concat(dateStrs[i]);
                    }
                }
                // 重新组合日期字符串，并解析为Date对象，再格式化为"yyyy-MM-dd"的字符串形式
                String fomDate = dateStrs[0] + "-" + dateStrs[1] + "-" + dateStrs[2];
                Date date = simpleDateFormat.parse(fomDate);
                userBirth = simpleDateFormat.format(date);
            }
            // 将处理后的生日信息放回map中
            map.put("userBirth", userBirth);

            // 更新用户基本信息表中的昵称
            int one = this.baseMapper.update(null, new LambdaUpdateWrapper<UserInfoEntity>()
                    .eq(UserInfoEntity::getUserId, userId)
                    .set(UserInfoEntity::getUserNickname, map.get("userNickname")));

            // 更新用户详细信息表中的生日、真实姓名、性别等信息
            int two = userInfoDetailMapper.update(null, new LambdaUpdateWrapper<UserInfoDetaileEntity>()
                    .eq(UserInfoDetaileEntity::getUserId, userId)
                    .set(UserInfoDetaileEntity::getUserBirth, userBirth)
                    .set(UserInfoDetaileEntity::getUserTruename, map.get("userTruename"))
                    .set(UserInfoDetaileEntity::getUserGender, map.get("userGender")));

            // 更新用户绑定信息表中的昵称
            boolean three = userBindConnectService.update(null, new LambdaUpdateWrapper<UserBindConnectEntity>()
                    .eq(UserBindConnectEntity::getUserId, userId)
                    .set(UserBindConnectEntity::getBindNickname, map.get("userNickname")));

            // 检查所有更新操作是否都成功，如果都成功，则更新Redis中的用户信息缓存并返回成功响应
            if (one > 0 && two > 0 && three) {
                String userInfo = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
                UserInfoDTO dto = JSON.parseObject(userInfo, UserInfoDTO.class);
                UserInfoVO ui = dto.getUser();
                ui.setUserNickname(map.get("userNickname"));
                ui.setUserName(map.get("userNickname"));
                dto.setUser(ui);
                RedisUtil.set(HouConfig.HOU_USER_TOKEN_USER + token, JSON.toJSONString(dto), HouConfig.TOKEN_OUTTIME);
                return ResponseResult.success();
            }
            // 如果有任何更新操作失败，则返回失败响应
            return ResponseResult.fail();
        } catch (Exception e) {
            // 如果更新过程中发生异常，则返回失败响应并包含异常信息
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 根据手机号查询用户状态在0到2之间的用户数量。
     * <p>
     * 该方法用于检查给定手机号是否存在状态为0、1或2的用户账户。
     * 状态0通常表示未激活，状态1表示正常，状态2可能表示已禁用。
     * 这种检查对于验证用户账户是否存在以及账户的状态是否符合特定条件非常重要，
     * 比如在注册新用户或尝试重置密码时。
     *
     * @param map 包含查询参数的映射，特别是用户手机号。
     * @return 符合条件的用户数量。如果map或userMobile为null，或查询结果为空，则返回0。
     */
    @Override
    public Integer checkRepeateUserAccount(Map<String, Object> map) {
        /* 初始化计数器为0，用于累计符合条件的用户数量 */
        int count = 0;
        /* 检查map和userMobile是否非空，以准备进行查询 */
        if (null != map && null != map.get("userMobile")) {
            /* 根据手机号和用户状态查询用户数量，状态在0到2之间 */
            count = Math.toIntExact(this.baseMapper.selectCount(new QueryWrapper<UserInfoEntity>().eq("user_mobile", map.get("userMobile")).between("user_state", "0", "2")));
        }
        /* 返回符合条件的用户数量 */
        return count;
    }


    /**
     * 重置密码接口。
     * 通过手机验证码和新密码来重置用户密码。
     * 验证用户手机号是否存在、验证码是否正确、新密码是否符合要求，并更新数据库中的用户密码。
     *
     * @param map 包含用户手机号、验证码、新密码等信息的参数映射。
     * @return 返回重置密码的结果，成功或失败及失败原因。
     */
    @Override
    @SneakyThrows
    public ResponseResult forgotPassword(Map<String, Object> map) {
        if (map == null) {
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
        }

        String userMobile = map.get("userMobile") == null ? null : map.get("userMobile").toString().trim();
        if (com.houjiang.commons.util.StringUtils.isBlank(userMobile)) {
            return ResponseResult.fail(BizCodeEnume.INCORRECT_PHONE_NUMBER_FORMAT);
        }

        String phoneCode = map.get("phoneCode") == null ? null : map.get("phoneCode").toString().trim();
        if (com.houjiang.commons.util.StringUtils.isBlank(phoneCode)) {
            return ResponseResult.fail(BizCodeEnume.VERIFICATION_CODE_CANNOT_BE_EMPTY);
        }

        String encryptedPassword = map.get("password") == null ? null : map.get("password").toString();
        if (com.houjiang.commons.util.StringUtils.isBlank(encryptedPassword)) {
            return ResponseResult.fail(BizCodeEnume.PASSWORD_CANNOT_BE_EMPTY);
        }

        // 初始化商店ID，用于后续查询和更新操作
        String shopId = null;
        // 如果map中包含shopId，则提取出来
        if (null != map.get("shopId")) {
            shopId = map.get("shopId").toString();
        }

        // 根据手机号和商店ID检查用户是否已注册
        Integer checkuserId = this.baseMapper.checkUserIsRegister(userMobile, shopId);
        // 如果用户不存在，则返回错误信息
        // 判断手机号码是否存在
        if (Objects.equals(checkuserId, CommonType.commonStatus.NO.getCode())) {
            return ResponseResult.fail(BizCodeEnume.USER_DOES_NOT_EXIST);
        }

        // 从Redis中获取存储的验证码
        String redisPhoneCode = RedisUtil.get(userMobile);
        if (com.houjiang.commons.util.StringUtils.isBlank(redisPhoneCode)) {
            return ResponseResult.fail(BizCodeEnume.VERIFICATION_CODE_OVERDUE);
        }
        // 如果输入的验证码与Redis中的不一致，则返回错误信息
        if (!phoneCode.equals(redisPhoneCode)) {
            return ResponseResult.fail(BizCodeEnume.INCORRECT_VERIFICATION_CODE);
        }

        String password;
        try {
            password = JSEncrypt.decrypt(encryptedPassword);
        } catch (Exception e) {
            log.warn("找回密码的 RSA 密文解密失败，userMobile=" + userMobile + "，原因=" + e.getMessage());
            return ResponseResult.fail(BizCodeEnume.PASSWORD_NOT_STANDARD);
        }
        if (com.houjiang.commons.util.StringUtils.isBlank(password)) {
            return ResponseResult.fail(BizCodeEnume.PASSWORD_CANNOT_BE_EMPTY);
        }
        // 查询用户当前密码
        // 查询旧密码
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("userMobile", userMobile);
        hashMap.put("shopId", shopId);
        String oldPassword = this.baseMapper.selectOldPasswordByUserMobile(hashMap);

        // 如果新密码与旧密码相同，则返回错误信息
        // 输入密码不能和原密码相同
        if (PasswordUtils.BCryptPasswordMatches(password, oldPassword)) {
            return ResponseResult.fail(BizCodeEnume.PASSWORD_CANNOT_BE_OLD_PASSWORD);
        }

        // 准备更新密码所需的数据
        Map<String, String> daoMap = new HashMap<>();
        daoMap.put("userMobile", userMobile);
        daoMap.put("password", PasswordUtils.BCryptPasswordEncoder(password));
        daoMap.put("shopId", shopId);
        // 更新用户密码
        // 修改密码
        Integer torf = this.baseMapper.updateUserPassword(daoMap);
        // 如果更新成功，则返回成功信息；否则返回失败信息
        if (torf > 0) {
            return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.PASSWORD_UPDATED_SUCCESSFULLY.getMsg());
        } else {
            return ResponseResult.fail(BizCodeEnume.PASSWORD_UPDATED_FAILED);
        }
    }

    /**
     * 根据传入的手机号和token检查用户状态和信息。
     *
     * @param map 包含token和手机号的映射表。
     * @return 根据用户状态返回不同的响应结果。
     */
    @Override
    public ResponseResult checkPhone(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        // 检查token是否存在
        if (map.get("token") != null && !map.get("token").equals("")) {
            // 从Redis中根据token获取用户信息
            String jsonUserinfo = RedisUtil.get(HouConfig.HOU_PLATFORM_TOKEN_USER + map.get("token").toString());
            // 如果用户信息不存在，则返回token无效的错误信息
            if (jsonUserinfo == null || jsonUserinfo.isEmpty()) {
                result.put("start", 2);
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getMsg(), result);
            }
            // 根据手机号查询用户是否注册以及是否有店铺
            Map<String, String> param = new HashMap<>();
            param.put("phone", map.get("userMobile").toString());
            param.put("shopType", "");
            ResponseResult rr = queryPhoneNumCount(param);
            // 如果用户未注册，则返回相应的错误信息
            if (rr != null && BizCodeEnume.USER_DOES_NOT_EXIST.getCode() == rr.getCode() && rr.getMessage().equals(BizCodeEnume.USER_DOES_NOT_EXIST.getMsg())) {
                result.put("start", 5);
                return ResponseResult.success("未注册账号", result);
            }
            // 如果用户已注册且有店铺，则返回相应的错误信息
            else if (rr != null && BizCodeEnume.USER_HAS_OPENED_A_STORE.getCode() == rr.getCode()) {
                result.put("start", 2);
                return ResponseResult.fail(rr.getMessage(), result);
            }
            // 查询用户的密码信息
            String userPwd = this.baseMapper.selectOne(new LambdaQueryWrapper<UserInfoEntity>()
                    .eq(UserInfoEntity::getUserMobile, map.get("userMobile").toString())).getPassword();
            // 查询手机号是否已被注册为门店电话
            Integer gataPhoneNum = this.baseMapper.queryGatePhoneNumCount(map.get("userMobile").toString());
            // 如果手机号已被注册为门店电话，则返回相应的错误信息
            if (gataPhoneNum > 0) {
                result.put("start", 2);
                return ResponseResult.fail(BizCodeEnume.REGISTERED_STORE_WITH_PHONE_NUMBER.getMsg(), result);
            }
            // 设置用户状态为已注册且无店铺
            result.put("start", 1);
            // 如果用户密码存在，则添加到结果中
            if (userPwd != null && !userPwd.isEmpty()) {
                result.put("password", userPwd);
            } else {
                result.put("password", "");
            }
            // 返回成功响应，包含用户状态和密码信息
            return ResponseResult.success(result);
        } else {
            // 如果token不存在，则返回token无效的错误信息
            result.put("start", 10086);
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getMsg(), result);
        }
    }

    /**
     * 根据业务需求检查手机号是否可用。
     * 该方法首先验证令牌的有效性，然后根据手机号和用户类型检查是否已注册或已有店铺/门店。
     * 如果手机号已用于注册店铺或门店，则返回不可用；否则，返回可用。
     *
     * @param map 包含令牌和手机号的映射表。
     * @return 根据检查结果返回相应的响应结果。
     */
    @Override
    public ResponseResult checkPhoneByBusiness(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        // 验证令牌是否有效
        if (map.get("token") != null && !map.get("token").equals("")) {
            // 从Redis中根据令牌获取用户信息
            //根据Token获取用戶信息(shopId)
            String jsonUserinfo = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + map.get("token").toString());
            // 如果用户信息不存在，则令牌无效
            if (jsonUserinfo == null || jsonUserinfo.isEmpty()) {
                result.put("start", 2);
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
            }
            // 解析令牌获取用户信息
            // 获取user信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
            // 根据手机号、店铺类型和店铺ID检查是否已注册或已有店铺/门店
            //校验登录名下是否：1.注册；2.有店铺；3有门店
            //根据手机号查用户表是否存在
            Map<String, String> param = new HashMap<>();
            param.put("phone", map.get("phone").toString());
            param.put("shopType", String.valueOf(userInfo.getShopType()));
            param.put("shopId", String.valueOf(userInfo.getShopId()));
            ResponseResult rr = queryPhoneNumCount(param);
            // 如果检查失败，则返回相应的错误信息
            if (rr == null || CommonStatusEnum.SUCCESS.getCode() != rr.getCode()) {
                result.put("start", 2);
                return ResponseResult.fail(rr.getMessage(), result);
            }
            // 根据店铺类型查询手机号在门店中的使用情况
            //根据手机号查门店表
            //判断是否为品牌店铺
            Integer gataPhoneNum = 0;
            if (userInfo.getShopType() == 3) {
                // 品牌店铺的查询逻辑
                gataPhoneNum = this.baseMapper.queryGatePhoneNumCount1(map.get("phone").toString(), userInfo.getShopId());
            } else {
                // 非品牌店铺的查询逻辑
                gataPhoneNum = this.baseMapper.queryGatePhoneNumCount(map.get("phone").toString());
            }
            // 如果手机号已用于注册门店，则返回不可用
            if (gataPhoneNum > 0) {
                result.put("start", 2);
                return ResponseResult.fail(BizCodeEnume.REGISTERED_STORE_WITH_PHONE_NUMBER);
            }
            // 手机号可用，返回成功结果
            result.put("start", 1);
            return ResponseResult.success(result);
        } else {
            // 令牌无效，返回错误结果
            result.put("start", 2);
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    /**
     * 查询审核列表信息
     *
     * @param param 查询参数，以Map形式传入，可包含多种条件。
     * @return 返回查询结果，封装在ResponseResult对象中，其中包含分页信息。
     * <p>
     * 本方法通过调用baseMapper的queryAuditList方法查询审核列表，并返回分页后的结果。
     * 如果查询过程中发生异常，将返回系统错误的响应结果。
     */
    @Override
    public ResponseResult queryAuditList(Map<String, Object> param) {
        try {
            // 根据查询参数获取分页对象，并调用queryAuditList方法进行查询
            IPage<BusinessUserListVO> page = this.baseMapper.queryAuditList(new Query<BusinessUserListVO>().getPage(param), param);
            // 返回查询结果，其中包含分页信息
            return ResponseResult.success(PageUtils.getPage(page));
        } catch (Exception e) {
            // 打印异常堆栈信息
            e.printStackTrace();
            // 返回系统错误的响应结果
            return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
        }
    }

    /**
     * 审核用户通过或不通过的接口。
     *
     * @param param 包含审核状态和其他必要信息的参数映射。
     * @return 根据审核结果返回相应的响应结果。
     */
    @Override
    public ResponseResult auditPass(Map<String, Object> param) {
        try {
            // 获取审核状态（通过或不通过）
            Integer isPass = (Integer) param.get("isPass");

            // 检查用户ID是否为空或为空字符串，如果是，则返回缺失参数的错误信息
            if (null == param.get("userId") || "".equals(param.get("userId"))) {
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER.getCode(), BizCodeEnume.MISSING_PARAMETER.getMsg());
            }

            // 根据用户ID查询用户信息
            UserInfoEntity info = this.baseMapper.selectOne(new LambdaQueryWrapper<UserInfoEntity>()
                    .eq(UserInfoEntity::getUserId, param.get("userId")));
            UserInfoDetaileEntity detaileEntity = userInfoDetailMapper.selectOne(new LambdaQueryWrapper<UserInfoDetaileEntity>()
                    .eq(UserInfoDetaileEntity::getUserId, param.get("userId")));
            BusinessUserInfoVO detail = userBusinessInfoMapper.queryInfo(param);
            // 准备发送消息的参数
            Map<String, Object> records = new HashMap<>();
            List<String> list = new ArrayList<>();

            // 如果审核通过
            // 审核通过
            if (1 == isPass) {
                // 更新用户状态为通过审核，并设置用户状态为正常
                param.put("isApproved", 1);
                param.put("userState", 2);
                // 更新业务用户状态和用户信息状态
                this.baseMapper.updateBusinessUserStatus(param);
                this.baseMapper.updateUserInfoStatus(param);
                // 设置审核结果为成功
                // 企业审核通过
                records.put("templateId", CommonType.messageTemplate.CODE_A.getCode());
                records.put("messageTitle", "企业审核通过");
            // 如果审核不通过
            } else if (0 == isPass) {
                // 更新用户状态为未通过审核，并设置用户状态为未激活
                param.put("isApproved", 2);
                param.put("userState", 0);
                // 更新业务用户状态和用户信息状态
                this.baseMapper.updateBusinessUserStatus(param);
                this.baseMapper.updateUserInfoStatus(param);
                // 删除用户账号
                deleteAccount(param);
                // 设置审核结果为失败，并附带失败原因
                // 企业审核不通过
                list.add(param.get("auditCommnet").toString());
                records.put("templateId", CommonType.messageTemplate.CODE_B.getCode());
                records.put("reason", param.get("auditCommnet").toString());
                records.put("messageTitle", "企业审核不通过");
            }
            records.put("list", list);
            records.put("userId", info.getUserId());
            // 设置短信接受者手机号
            records.put("mobile", info.getUserMobile());
            records.put("businessName", detail.getCompanyName());
            records.put("interDialingCode", info.getInternationalDialingCode());
            // 发送消息模板通知
            baseServiceClient.sendMessageTemplate(records);

            // 返回成功响应
            return ResponseResult.success();
        } catch (Exception e) {
            e.printStackTrace();
            // 返回系统错误的响应
            return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
        }
    }

    /**
     * 根据不同类型统计用户数量。
     *
     * @param map 包含token和type的Map对象，token用于验证用户身份，type指定统计类型。
     * @return 返回统计结果的ResponseResult对象。
     * 如果token验证失败，返回错误信息提示。
     * 如果统计过程中发生异常，返回系统错误信息。
     * 其他情况下，返回指定类型的用户数量。
     */
    @Override
    public ResponseResult userCount(Map<String, Object> map) {
        // 从map中获取token，用于用户身份验证
        String token = (String) map.get("token");
        // 根据token验证用户身份
        UserInfoVO info = ParseTokenUtils.getPlatformUser(token);
        if (info != null) {
            try {
                // 从map中获取统计类型
                // 类型:1、全部，2、当日，3、本周，4、本月，5、年度
                Integer type = (Integer) map.get("type");
                // 根据不同的统计类型，调用相应的方法查询用户数量
                Integer userCount = 0;
                if (type == 1) {
                    userCount = this.baseMapper.queryAllUserCount();
                } else if (type == 2) {
                    userCount = this.baseMapper.queryTodayUserCount();
                } else if (type == 3) {
                    userCount = this.baseMapper.queryWeekUserCount();
                } else if (type == 4) {
                    userCount = this.baseMapper.queryMonthUserCount();
                } else {
                    userCount = this.baseMapper.queryYearUserCount();
                }
                // 返回查询结果
                return ResponseResult.success(userCount);
            } catch (Exception e) {
                // 统计过程中发生异常，返回系统错误信息
                return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
            }
        } else {
            // token验证失败，返回相应的错误信息
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
    }

    /**
     * 根据用户token查询当天的用户数量。
     *
     * @param map 包含用户token的Map对象。
     * @return 返回查询结果，如果token无效或查询出错，则返回错误信息。
     */
    @Override
    public ResponseResult userTodayNumber(Map<String, Object> map) {
        // 从map中获取用户token
        String token = (String) map.get("token");

        // 使用token解析用户信息
        UserInfoVO info = ParseTokenUtils.getPlatformUser(token);

        // 如果token有效
        if (info != null) {
            try {
                // 查询当天的用户数量
                Integer userCount = this.baseMapper.queryTodayUserCount();
                // 返回查询成功的响应，包含用户数量
                return ResponseResult.success(userCount);
            } catch (Exception e) {
                // 如果查询出错，返回系统错误的响应
                return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
            }
        } else {
            // 如果token无效，返回token无效的响应
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
    }

    /**
     * 更新会员登录密码。
     * <p>
     * 本方法用于处理会员登录密码的更新请求。首先，它会检查请求中提供的令牌是否存在于Redis中，
     * 以此验证请求的有效性。如果令牌有效，那么它将使用BCrypt加密算法对新密码进行加密，
     * 并更新数据库中对应会员的密码。如果令牌无效，则表示请求者没有足够的权限进行此操作，
     * 方法将返回一个表示失败的响应结果。
     *
     * @param param 一个Map对象，包含更新密码所需的信息，如令牌（token）、新密码（password）和用户ID（userId）。
     * @return 如果密码更新成功，则返回一个表示成功的响应结果，其中包含操作成功的业务码；如果令牌无效，
     * 则返回一个表示失败的响应结果，其中包含权限不足的业务码。
     */
    @Override
    @SneakyThrows
    public ResponseResult updateMemberLoginPassword(Map<String, Object> param) {
        // 检查令牌是否存在于Redis中，以验证请求的有效性
        if (RedisUtil.exists(HouConfig.HOU_PLATFORM_TOKEN_USER + param.get("token"))) {
            String password = JSEncrypt.decrypt(param.get("password").toString());
            // 使用BCrypt加密算法对新密码进行加密
            password = PasswordUtils.BCryptPasswordEncoder(password);
            // 更新数据库中对应用户的密码
            this.baseMapper.update(null, new LambdaUpdateWrapper<UserInfoEntity>()
                    .set(UserInfoEntity::getPassword, password)
                    .eq(UserInfoEntity::getUserId, param.get("userId")));
            // 返回表示操作成功的响应结果
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        } else {
            // 返回表示权限不足的响应结果
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
    }

    /**
     * 更新用户信息的方法。
     * 通过传入的参数Map，更新对应用户的信息。支持更新用户ID和用户积分。
     *
     * @param paraMap 参数映射表，包含可能需要更新的用户信息字段。
     *                key为字段名，value为字段的新值。
     * @return 返回操作结果，如果操作成功，则返回带有成功代码和消息的ResponseResult对象。
     */
    @Override
    public ResponseResult updateUser(Map<String, Object> paraMap) {
        // 创建更新条件的包装对象
        UpdateWrapper<UserInfoEntity> updateWrapper = new UpdateWrapper<>();

        // 如果参数Map中包含userId，将其作为条件加入更新包装对象
        if (null != paraMap && null != paraMap.get("userId")) {
            updateWrapper.eq("user_id", paraMap.get("userId"));
        }

        // 如果参数Map中包含userIntegral，将其作为要更新的字段值加入更新包装对象
        if (null != paraMap && null != paraMap.get("userIntegral")) {
            updateWrapper.set("user_integral", paraMap.get("userIntegral"));
        }

        // 调用BaseMapper的update方法，执行更新操作。这里传入的null表示不更新实体类的全部字段，只更新包装对象中指定的字段。
        this.baseMapper.update(null, updateWrapper);

        // 返回操作成功的结果对象
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 查询用户真实口岸信息
     * 通过Map参数查询用户真实口岸信息，并返回分页结果。
     *
     * @param paraMap 查询参数，包含页码、页大小等分页信息以及用户相关查询条件。
     * @return 返回包含查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult queryUserTurePort(Map<String, Object> paraMap) {
        // 根据查询条件获取分页对象，并执行查询
        IPage<MemberMessageListVO> page = this.baseMapper.queryUserTurePort(new Query<MemberMessageListVO>().getPage(paraMap),
                paraMap);
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 查询共享用户列表
     * 通过Map参数查询共享用户列表，并返回分页结果。如果用户不存在，则返回错误信息。
     *
     * @param param 查询参数，包含页码、页大小等分页信息以及用户名称或手机号码等查询条件。
     * @return 返回包含查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult queryShareUserListPort(Map<String, Object> param) {
        // 根据用户名或手机号码查询用户ID
        String userId = this.baseMapper.selectOne(new LambdaQueryWrapper<UserInfoEntity>()
                .eq(param.containsKey("userName"), UserInfoEntity::getUserName, param.get("userName").toString())
                .eq(param.containsKey("userMobile"), UserInfoEntity::getUserMobile, param.get("userMobile").toString())
                .last("limit 1")).getUserId();
        // 检查用户ID是否存在
        if (null == userId || userId.isEmpty()) {
            return ResponseResult.fail(BizCodeEnume.USER_DOES_NOT_EXIST);
        }
        // 将用户ID添加到查询参数中
        param.put("shareUserId", userId);
        // 根据更新后的查询参数获取共享用户列表的分页结果
        IPage<MemberMessageListVO> page = this.baseMapper.queryShareUserListPort(new Query<MemberMessageListVO>().getPage(param),
                param);
        // 格式化用户注册时间
        for (MemberMessageListVO res : page.getRecords()) {
            if (null != res.getUserRegTime()) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Long time = Long.valueOf(res.getUserRegTime());
                String d = format.format(time * 1000);
                res.setUserRegTime(d);
            }
        }
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 更新会员消息详情端口信息。
     *
     * @param param 一个Map对象，包含所有要更新的参数。
     * @return 返回一个ResponseResult对象，表示操作的结果。
     */
    @Override
    public ResponseResult updateMemberMessageDetailPort(Map<String, Object> param) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.get("token").toString());

        // 检查用户是否有权限进行更新
        if (userInfo.getRightsGroupId() == 1) {
            // 如果用户支付密码存在且不为空，也不等于"******"，则加密支付密码并更新
            if (param.containsKey("userPayPasswd")
                    && param.get("userPayPasswd") != null
                    && !(param.get("userPayPasswd").equals(""))
                    && !(param.get("userPayPasswd").equals("******"))) {
                String pass = PasswordUtils.BCryptPasswordEncoder(param.get("userPayPasswd").toString());
                param.put("userPayPasswd", pass);
                this.baseMapper.updateuserPa(param);
            }

            // 如果密码存在且不为空，也不等于"******"，则加密密码并更新用户密码
            if (param.containsKey("password")
                    && param.get("password") != null
                    && !(param.get("password").equals(""))
                    && !(param.get("password").equals("******"))) {
                String spass = PasswordUtils.BCryptPasswordEncoder(param.get("password").toString());
                this.baseMapper.update(null, new LambdaUpdateWrapper<UserInfoEntity>()
                        .set(UserInfoEntity::getPassword, spass)
                        .eq(UserInfoEntity::getUserId, param.get("userId").toString()));
            }

            // 更新用户信息
            this.baseMapper.updateuserInfoPort(param);
            userInfoDetailMapper.updateUcenterInfo(param);

            // 如果标签参数存在，则转换为列表形式，并更新用户标签
            if (param.get("tags") != null) {
                List<Integer> tags = Collections.singletonList(Integer.valueOf(param.get("tags").toString()));
                String str = StringUtils.join(tags, ",");
                param.put("tagId", str);
                param.put("memberId", param.get("userId").toString());
                this.baseMapper.updateTag(param);
            }

        } else {
            // 如果用户没有权限，返回失败结果
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }

        // 操作成功，返回成功结果
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 更新用户个人基础信息。
     * <p>
     * 通过传入的用户令牌解析用户信息，如果用户信息有效，则根据传入的用户个人基础信息VO对象更新用户个人基础信息。
     * 如果用户令牌无效或解析失败，则返回相应的错误信息。
     *
     * @param param 用户个人基础信息VO对象，包含需要更新的信息。
     * @return 如果更新成功，返回成功的响应结果；如果更新失败或令牌非法，返回失败的响应结果。
     */
    @Override
    public ResponseResult updatePersonalBase(UserPersonalBaseVO param) {
        // 解析用户令牌，获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());

        // 如果用户信息有效
        if (null != userInfo) {
            // 设置更新参数中的用户ID，以便后续更新操作
            param.setUserId(userInfo.getUserId());
            // 调用基础Mapper接口，更新用户个人基础信息
            this.baseMapper.updatePersonalBase(param);
            // 返回操作成功的响应结果
            return ResponseResult.success();
        }
        // 如果用户信息无效，返回令牌非法的错误响应结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 更新用户是否为店铺的标识。
     *
     * @param userId 用户ID，用于指定要更新的用户。
     * @param shopId 店铺ID，用于查询与该店铺相关的用户。
     * @param isShop 标识用户是否为店铺，1表示是，0表示否。
     * @return 返回操作结果，如果操作成功，则返回一个成功的响应结果。
     */
    @Override
    public ResponseResult updateUserIsShop(String userId, Integer shopId, Integer isShop) {
        // 更新指定用户的是否为店铺的标识
        this.baseMapper.updateUserIsShop(userId, isShop);

        // 获取与指定店铺相关的用户信息
        List<UserSellerBaseVO> list = userSellerBaseService.getSellerBase(shopId);

        // 如果列表不为空，遍历列表并更新每个用户的是否为店铺的标识
        if (!list.isEmpty()) {
            for (UserSellerBaseVO userSellerBase : list) {
                this.baseMapper.updateUserIsShop(userSellerBase.getUserId(), isShop);
            }
        }

        // 返回操作成功的响应结果
        return ResponseResult.success();
    }

    /**
     * 添加商家信息。根据商家ID检查商家是否已存在，如果存在，则更新商家信息；如果不存在，则新增商家信息。
     * 同时，更新用户信息与Redis中的用户令牌。
     *
     * @param paraMap 包含商家ID、用户ID等信息的参数映射。
     * @return 成功添加或更新商家信息并更新用户信息后返回成功响应，否则返回失败响应。
     */
    @Override
    public ResponseResult addSeller(Map<String, Object> paraMap) {
        // 检查商家ID是否为空，如果为空，则返回参数异常响应。
        if (StringUtils.isEmpty((String) paraMap.get("shopId"))) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
        }

        boolean flag;
        // 根据参数映射创建商家基础信息对象。
        // 查询是否注册过商户
        UserSellerBaseEntity sellerBase = ParseMapUtils.parseMap2Object(paraMap, UserSellerBaseEntity.class);

        // 查询是否有相同的商家ID且不是管理员的商家存在。
        Integer integer = Math.toIntExact(userSellerBaseService.getBaseMapper().selectCount(new LambdaQueryWrapper<UserSellerBaseEntity>()
                .eq(UserSellerBaseEntity::getShopId, Integer.valueOf(paraMap.get("shopId").toString()))
                .ne(UserSellerBaseEntity::getSellerIsAdmin, 1)));

        if (integer > 0) {
            // 如果存在，则更新商家信息。
            // 注册过修改商户信息
            flag = userSellerBaseService.update(sellerBase, new LambdaQueryWrapper<UserSellerBaseEntity>()
                    .eq(UserSellerBaseEntity::getShopId, Integer.valueOf(paraMap.get("shopId").toString())));
        } else {
            // 如果不存在，则设置商家基础信息的默认值，并新增商家信息。
            // 未注册过新增商户信息
            sellerBase.setRightsGroupId(1);
            sellerBase.setSellerIsAdmin((byte) 1);
            sellerBase.setSellerGroupId(1);
            flag = userSellerBaseService.save(sellerBase);
        }

        if (flag) {
            // 更新成功后，更新用户信息（注释掉的部分）。

            // 从参数映射中获取用户ID。
            // 插入成功，更新 用户信息
            String userId = (String) paraMap.get("userId");

            // 通过用户ID获取用户信息，更新Redis中的用户令牌信息。
            UserInfoVO userInfo = this.baseMapper.getUserInfoByUserIdNoState(userId);
            String userInfoStr = RedisUtil.get(HouConfig.LOGIN + userId);
            if (null != userInfoStr) {
                UserInfoDTO dto = JSON.parseObject(userInfoStr, UserInfoDTO.class);
                String token = dto.getUser().getToken();
                dto.setUser(userInfo);
                RedisUtil.set(HouConfig.HOU_USER_TOKEN_USER + token, JSON.toJSONString(dto), HouConfig.TOKEN_OUTTIME);
                RedisUtil.set(HouConfig.HOU_USER_TOKEN_USERID + token, userInfo.getUserId(), HouConfig.TOKEN_OUTTIME);
            }

            // 返回成功响应。
            return ResponseResult.success();
        }

        // 如果更新失败，则返回商家注册失败响应。
        return ResponseResult.fail(BizCodeEnume.SHOP_REGISTRATION_FAIL);
    }


    /**
     * 根据传入的参数查询用户真实名单列表。
     *
     * @param paraMap 包含查询参数的映射，其中必须包含token以获取用户信息。
     * @return 返回查询结果的响应对象，包含分页信息和查询数据。
     */
    @Override
    public ResponseResult queryUserTrueListPort(Map<String, Object> paraMap) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(paraMap.get("token").toString());
        // 如果用户信息存在
        if (null != userInfo) {
            // 格式化日期字符串
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 如果传入了结束时间和开始时间
            if (paraMap.containsKey("finishTime") && !"".equals(paraMap.containsKey("startTime"))) {
                Long finishTime = null;
                Long startTime = null;
                try {
                    // 将日期字符串转换为时间戳
                    String finishTime1 = paraMap.get("finishTime").toString();
                    String startTime1 = paraMap.get("startTime").toString();
                    if (!StringUtils.isEmpty(finishTime1) && !StringUtils.isEmpty(startTime1)) {
                        finishTime = format.parse(finishTime1).getTime() / 1000;
                        startTime = format.parse(startTime1).getTime() / 1000;
                        // 更新参数映射中的时间戳
                        paraMap.put("finishTime", finishTime.toString());
                        paraMap.put("startTime", startTime.toString());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            try {
                // 执行查询，分页结果由PageUtils处理
                IPage<MemberMessageListVO> page = this.baseMapper.queryUserTrueListPort(new Query<MemberMessageListVO>().getPage(paraMap), paraMap);
                // 获取脱敏开关配置值
                String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
                // 对查询结果进行脱敏处理
                for (MemberMessageListVO res : page.getRecords()) {
                    if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                        BeanUtils.copyProperties(DesensitizationUtil.Desensitization(res, MemberMessageListVO.class), res);
                    }
                    // 将用户注册时间转换为字符串格式
                    if (null != res.getUserRegTime()) {
                        Long time = Long.valueOf(res.getUserRegTime());
                        String d = format.format(time * 1000);
                        res.setUserRegTime(d);
                    }
                }
                // 返回成功的响应结果，包含分页信息
                return ResponseResult.success(PageUtils.getPage(page));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 如果用户信息不存在或查询出现异常，返回失败的响应结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 根据传入的会员消息列表积分DTO，查询会员消息详情端口。
     *
     * @param dto 会员消息列表积分DTO，包含用户token和用户ID等信息。
     * @return 返回查询结果的响应结果。
     */
    @Override
    public ResponseResult queryMemberMessageDetailPort(MemberMessageListIntegralDTO dto) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(dto.getToken());
        // 如果用户信息不为空
        if (null != userInfo) {
            // 根据用户ID查询会员消息详情
            MemberMessageDetailVO result = this.baseMapper.queryMemberMessageDetailPort(dto.getUserId());
            // 查询脱敏开关配置值
            String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
            // 如果脱敏开关打开
            if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                // 对查询结果进行脱敏处理
                BeanUtils.copyProperties(DesensitizationUtil.Desensitization(result, MemberMessageDetailVO.class), result);
            }
            // 如果用户注册时间不为空，转换时间格式
            if (null != result.getUserRegTime()) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Long time = Long.valueOf(result.getUserRegTime());
                String d = format.format(time * 1000);
                result.setUserRegTime(d);
            }
            // 查询用户标签ID
            String tagId = this.baseMapper.getTagId(dto.getUserId());
            // 如果标签ID不为空
            if (StringUtils.isNotBlank(tagId)) {
                // 根据标签ID分割并查询标签信息
                String[] split = tagId.split(",");
                List<TagInfoVO> tagList = new ArrayList<>();
                for (String s : split) {
                    TagInfoVO tagInfoByid = this.baseMapper.getTagInfoByid(s);
                    if (null != tagInfoByid) {
                        tagList.add(tagInfoByid);
                        result.setTagInfos(tagList);
                    }
                }
            }
            // 查询用户积分
            Integer integer = this.baseMapper.queryMemberMessageListIntegral(dto.getUserId());
            // 如果积分不为空，设置到查询结果中
            if (integer != null) {
                result.setMemberIntegral(integer);
            }
            // 查询企业详细信息
            // 新增企业信息
            Map<String, Object> map = new HashMap<>();
            map.put("token", dto.getToken());
            map.put("userId", dto.getUserId());
            BusinessUserInfoDetailVO detail = userBusinessInfoMapper.queryDetail(map);
            result.setBusinessDetail(detail);
            // 返回查询结果的成功响应
            return ResponseResult.success(result);
        }
        // 如果用户信息为空，返回token非法的失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }


    /**
     * 根据传入的会员消息列表积分DTO，查询会员消息详情的端口编码。
     *
     * @param dto 会员消息列表积分DTO，包含用户token和用户ID等信息。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult queryMemberMessageDetailPortEncoded(MemberMessageListIntegralDTO dto) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(dto.getToken());
        // 如果用户信息存在
        if (null != userInfo) {
            // 根据用户ID查询会员消息详情
            MemberMessageDetailVO result = this.baseMapper.queryMemberMessageDetailPort(dto.getUserId());
            // 如果注册时间存在
            if (null != result.getUserRegTime()) {
                // 格式化注册时间
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Long time = Long.valueOf(result.getUserRegTime());
                String d = format.format(time * 1000);
                result.setUserRegTime(d);
            }
            // 根据用户区域ID获取地址信息
            String address = baseServiceClient.getAddressString(Integer.valueOf(result.getUserProvinceid()),
                    Integer.valueOf(result.getUserCityid()), Integer.valueOf(result.getUserAreaid())).getData().toString();
            result.setArea(address);

            // 查询用户标签ID
            String tagId = this.baseMapper.getTagId(dto.getUserId());
            // 如果标签ID不为空
            if (StringUtils.isNotBlank(tagId)) {
                // 根据标签ID数组查询标签信息
                String[] split = tagId.split(",");
                List<TagInfoVO> tagList = new ArrayList<>();
                for (String s : split) {
                    TagInfoVO tagInfoByid = this.baseMapper.getTagInfoByid(s);
                    if (null != tagInfoByid) {
                        tagList.add(tagInfoByid);
                        result.setTagInfos(tagList);
                    }
                }
            }
            // 查询用户积分
            Integer integer = this.baseMapper.queryMemberMessageListIntegral(dto.getUserId());
            // 如果积分存在
            if (integer != null) {
                result.setMemberIntegral(integer);
            }
            // 查询企业详细信息
            // 新增企业信息
            Map<String, Object> map = new HashMap<>();
            map.put("token", dto.getToken());
            map.put("userId", dto.getUserId());
            BusinessUserInfoDetailVO detail = userBusinessInfoMapper.queryDetail(map);
            result.setBusinessDetail(detail);
            // 将查询结果进行Base64编码返回
            String s = Base64.getEncoder().encodeToString(JSONObject.toJSONString(result).getBytes());
            return ResponseResult.success(s);
        }
        // 如果用户信息不存在，返回非法token的错误响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }


    /**
     * 更新会员消息积分
     *
     * @param dto 数据传输对象，包含需要更新的积分信息和用户ID列表
     * @return 返回操作结果，成功或失败及原因
     */
    @Override
    public ResponseResult updateMemberMessageIntegral(MemberMessageListIntegralDTO dto) {
        try {
            // 检查积分是否小于0，如果小于0则操作失败
            if (dto.getIntegral() < 0) {
                return ResponseResult.fail(BizCodeEnume.INTEGRAL_CANNOT_BE_LESS_THAN_0);
            }
            // 检查用户ID列表是否为空，如果为空则操作失败
            if (null == dto.getUserIdList() || dto.getUserIdList().size() == 0) {
                return ResponseResult.fail(BizCodeEnume.USER_ID_IS_NULL);
            }
            // 获取用户ID列表
            //获取所有用户id
            List<String> userIdList = dto.getUserIdList();
            // 初始化用户积分变量
            Integer userIntegral = null;
            // 遍历用户ID列表，更新每个用户的积分
            for (String userId : userIdList) {
                // 初始化用户积分日志实体
                UserPointsLogEntity userPointLogParam = new UserPointsLogEntity();
                // 设置积分日志的默认值
                userPointLogParam.setClassId(5);
                userPointLogParam.setPointsLogTime(new Date());
                userPointLogParam.setPointsLogFlag("reg");
                userPointLogParam.setPointsLogDesc("平台操作");
                userPointLogParam.setPointsLogOperate(dto.getDesc());
                // 获取当前用户的积分
                //获取当前用户的积分
                userIntegral = this.baseMapper.selectById(userId).getUserIntegral();
                // 根据类型更新用户积分
                //type 1是增加积分
                if (dto.getType() == 1) {
                    // 更新用户积分并保存积分日志
                    Integer temp = userIntegral + dto.getIntegral();
                    this.baseMapper.update(null, new LambdaUpdateWrapper<UserInfoEntity>()
                            .eq(UserInfoEntity::getUserId, userId)
                            .set(UserInfoEntity::getUserIntegral, temp));
                    // 类型 1，表示积分增加
                    //类型 1获取积分2积分消费
                    userPointLogParam.setPointsLogType(1);
                    userPointLogParam.setPointsLogPoints(dto.getIntegral());
                    userPointLogParam.setUserId(userId);
                    if (!StringUtils.isNotEmpty(dto.getDesc())) {
                        userPointLogParam.setPointsLogOperate("平台添加" + dto.getIntegral() + "积分");
                    }
                    userPointsLogService.save(userPointLogParam);
                } else {
                    // 更新用户积分并保存积分日志
                    //type 0 是扣除积分
                    //如果当前用户的积分 小于 等于 平台扣除的积分直接赋值为 0
                    Integer temp = userIntegral - dto.getIntegral();
                    if (temp <= 0) {
                        temp = 0;
                    }
                    this.baseMapper.update(null, new LambdaUpdateWrapper<UserInfoEntity>()
                            .eq(UserInfoEntity::getUserId, userId)
                            .set(UserInfoEntity::getUserIntegral, temp));
                    // 类型 2，表示积分减少
                    //类型 1获取积分2积分消费
                    userPointLogParam.setPointsLogType(2);
                    userPointLogParam.setPointsLogPoints(dto.getIntegral());
                    userPointLogParam.setUserId(userId);
                    if (!StringUtils.isNotEmpty(dto.getDesc())) {
                        userPointLogParam.setPointsLogOperate("平台扣除" + dto.getIntegral() + "积分");
                    }
                    userPointsLogService.save(userPointLogParam);
                }
                // 根据更新后的积分查询并更新会员等级
                //积分修改完成 修改会员等级
                Map<String, Object> map = new HashMap<>();
                map.put("gradeStart", CommonType.commonStatus.YES.getCode());
                map.put("userIntegral", userIntegral);
                map.put("userId", userId);
                baseServiceClient.queryMembershipGradeByGradeRule(map);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 操作成功返回
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }

    /**
     * 根据会员信息查询条件导出会员数据列表为Excel。
     *
     * @param param 查询条件参数，包含分页、筛选条件等。
     * @return 返回导出Excel的URL地址。
     */
    @Override
    public ResponseResult queryMemberMessageListPortExcel(MemberMessageListDTO param) {
        // 解析用户token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        // 检查用户信息是否有效
        if (null != userInfo) {
            // 定义Excel表格的标题
            String[] title = {"会员名称", "会员ID", "手机号码", "注册时间", "性别", "最后登陆时间", "购买次数", "支付总额"};
            // 初始化数据列表
            List<Map<String, String>> list = Lists.newArrayList();
            // 定义日期格式化对象
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                // 处理查询时间范围，转换为时间戳
                if (!"".equals(param.getFinishTime()) && !"".equals(param.getStartTime())) {
                    Long finishTime = null;
                    Long startTime = null;
                    try {
                        finishTime = dateformat.parse(param.getFinishTime()).getTime() / 1000;
                        startTime = dateformat.parse(param.getStartTime()).getTime() / 1000;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    param.setFinishTime(finishTime.toString());
                    param.setStartTime(startTime.toString());
                }
                // 调整分页参数，计算起始位置
                param.setPage((param.getPage() - 1) * param.getLimit());
                // 查询会员数据
                List<MemberMessageListVO> result = this.baseMapper.queryMemberMessageListPortExp(param);
                // 检查查询结果数量是否超过限制
                if (result.size() >= 30000) {
                    return ResponseResult.fail(BizCodeEnume.PLEASE_BE_LESS_THAN_30000_PIECES);
                }
                // 获取脱敏开关配置值
                String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
                // 对查询结果进行脱敏处理
                for (MemberMessageListVO res : result) {
                    if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
                        BeanUtils.copyProperties(DesensitizationUtil.Desensitization(res, MemberMessageListVO.class), res);
                    }
                    // 根据性别代码转换为文字描述
                    if (res.getUserGender() != null) {
                        switch (res.getUserGender()) {
                            default:
                            case "0":
                                res.setUserGender("保密");
                                break;
                            case "1":
                                res.setUserGender("男");
                                break;
                            case "2":
                                res.setUserGender("女");
                                break;
                        }
                    }
                    // 格式化注册时间
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (!StringUtils.isEmpty(res.getUserRegTime())) {
                        Long time = Long.valueOf(res.getUserRegTime());
                        String d = format.format(time * 1000);
                        res.setUserRegTime(d);
                    }
                    // 构建数据行
                    HashMap<String, String> map = Maps.newHashMap();
                    map.put("会员名称", res.getUserName());
                    map.put("会员ID", res.getUserId() + "");
                    map.put("手机号码", res.getUserMobile() + "");
                    map.put("注册时间", res.getUserRegTime() + "");
                    map.put("性别", res.getUserGender() + "");
                    map.put("最后登陆时间", res.getUserLastloginTime() + "");
                    map.put("购买次数", res.getOrderCount() + "");
                    map.put("支付总额", res.getSumAmount() + "");
                    list.add(map);
                }
                // 构建Excel表格数据结构
                // excel文件名
                Map<String, List<Map<String, String>>> map = Maps.newHashMap();
                map.put("会员数据导出", list);
                // 生成Excel文件并返回下载URL
                String url = ExcelsUtil.createExcel(title, map, new int[]{0});
                return ResponseResult.success(url);
            } catch (Exception e) {
                logger.info(e.getMessage());
                return ResponseResult.fail(e.getMessage());
            }
        }
        // 用户token非法，返回错误响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 查询会员消息列表接口
     *
     * @param dto 查询条件的数据传输对象，包含会员消息的过滤条件和分页信息
     * @return 返回会员消息列表的响应结果
     */
    @Override
    public ResponseResult queryMemberMessageListPort(MemberMessageListDTO dto) {
        // 通过token解析获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(dto.getToken());
        if (userInfo != null) {
            try {
                // 格式化日期字符串为时间戳
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                // 如果起止时间不为空，则转换为时间戳
                if (!dto.getFinishTime().isEmpty() && !dto.getStartTime().isEmpty()) {
                    Long finishTime = null;
                    Long startTime = null;
                    try {
                        finishTime = format.parse(dto.getFinishTime()).getTime() / 1000;
                        startTime = format.parse(dto.getStartTime()).getTime() / 1000;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    dto.setFinishTime(finishTime.toString());
                    dto.setStartTime(startTime.toString());
                }
                // 将查询条件的DTO转换为MAP，用于MyBatis的动态SQL
                // 将对象转换为map
                Map data = ParseMapUtils.beanToMap(dto);
                // 根据店铺ID是否存在来决定使用哪个查询方法
                // 查询平台会员列表
                String shopId = String.valueOf(dto.getShopId());
                IPage<MemberMessageListVO> page;
                if ("null".equals(shopId) || shopId.isEmpty()) {
                    page = this.baseMapper.queryMemberMessageListPort(new Query<MemberMessageListVO>().getPage(data), dto);
                } else {
                    page = this.baseMapper.queryMemberMessageListPort1(new Query<MemberMessageListVO>().getPage(data), dto);
                }
                // 处理查询结果，包括脱敏、时间格式化、会员等级和标签的查询和填充
                List<MemberMessageListVO> records = page.getRecords();
                logger.info("会员列表查询结果：{}", records);
                if (!records.isEmpty()) {
//                    String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
                    for (MemberMessageListVO result : records) {
//                        if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString())) {
//                            BeanUtils.copyProperties(DesensitizationUtil.Desensitization(result, MemberMessageListVO.class), result);
//                        }
                        if (result.getUserRegTime() != null) {
                            Long time = Long.valueOf(result.getUserRegTime());
                            String d = format.format(time * 1000);
                            result.setUserRegTime(d);
                        }
                        // 获取会员等级名称
                        // 获取会员等级
                        MembershipGradeVO membershipGrade = new MembershipGradeVO();
                        membershipGrade.setId(result.getMemberClassId());
                        List<MembershipGradeVO> membershipGrades = this.baseMapper.queryMembershipGradeById(membershipGrade);
                        if (membershipGrades != null && !membershipGrades.isEmpty()) {
                            result.setMemberClass(membershipGrades.get(0).getGradeName());
                        }
                        Integer frozenIntegra = this.baseMapper.queryMemberFrozenIntegral(result.getUserId());
                        if (null == frozenIntegra) {
                            frozenIntegra = 0;
                        }
                        Integer integral = result.getUserIntegral() - frozenIntegra;
                        if (integral < 0) {
                            integral = 0;
                        }
                        result.setUserIntegral(integral);
                        // 获取会员标签名称
                        // 获取会员标签
                        String tagId = result.getTagId();
                        if (StringUtils.isNotBlank(tagId)) {
                            String[] split = tagId.split(",");
                            List<String> tagList = new ArrayList<>();
                            for (String s : split) {
                                TagInfoVO tagInfoByid = this.baseMapper.getTagInfoByid(s);
                                if (tagInfoByid != null) {
                                    tagList.add(tagInfoByid.getTagName());
                                    result.setTagList(tagList);
                                }
                            }
                        }
                    }
                }
                // 返回处理后的查询结果
                return ResponseResult.success(PageUtils.getPage(page));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        // 如果用户信息解析失败，返回非法token的错误响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 删除用户相关信息的函数。
     * 该函数通过调用不同类型的删除方法，来彻底删除用户在系统中的信息。
     * 这包括用户的基本信息、详细信息、绑定信息、商家基础信息、店铺VIP用户信息等。
     *
     * @param param 一个Map对象，包含用于删除操作的条件。
     */
    /**
     * 删除用户信息
     */
    private void deleteAccount(Map<String, Object> param) {
        this.baseMapper.deleteUserInfo(param);
        this.baseMapper.deleteUserDetailInfo(param);
        this.baseMapper.deleteUserBindConnect(param);
        this.baseMapper.deleteUserSellerBase(param);
        this.baseMapper.deleteShopVkUser(param);
        this.baseMapper.deleteBusinessInfo(param);
    }

    /**
     * 查询指定电话号码的注册状态和店铺开设情况。
     * 该方法首先尝试根据电话号码查询用户ID，然后判断用户是否开设了店铺。
     * 如果用户不存在，或者没有开设店铺，返回相应的成功或失败信息。
     *
     * @param paraMap 包含查询条件的Map对象，其中必须包含电话号码。
     * @return 返回一个ResponseResult对象，其中包含了查询结果的状态和信息。
     */
    public ResponseResult queryPhoneNumCount(Map<String, String> paraMap) {
        String phone = paraMap.get("phone");
        //根据手机号判断当前用户是否注册
        String userId = null;

        userId = this.baseMapper.queryPhoneNumCount(phone);
        if (null == userId) {
            return ResponseResult.fail(BizCodeEnume.USER_DOES_NOT_EXIST);
        }
        //查询当前用户是否开通店铺 或者是其他店铺的子账号
        Integer shopCount = Math.toIntExact(userSellerBaseService.count(new LambdaQueryWrapper<UserSellerBaseEntity>()
                .eq(UserSellerBaseEntity::getUserId, userId)));
        //查询当前用户是否有正在开通店铺
        Integer shopJoinBase = this.baseMapper.queryShopJoinBase(userId);
        if ((0 == shopCount) && (null == shopJoinBase || 0 == shopJoinBase)) {

            return ResponseResult.success(BizCodeEnume.USER_HAS_NOT_OPENED_A_STORE);
        } else {
            return ResponseResult.fail(BizCodeEnume.USER_HAS_OPENED_A_STORE);
        }
    }

    /**
     * 添加积分日志。
     * 该方法通过调用baseMapper的addPointsLog方法来添加用户的积分日志。
     * 如果添加成功，返回成功结果；否则，返回失败结果。
     *
     * @param param 包含积分日志信息的UserPointLogDTO对象。
     * @return 返回一个ResponseResult对象，表示添加操作的结果状态。
     */
    @Override
    public ResponseResult feginAddpointsLog(UserPointLogDTO param) {
        Integer integer = this.baseMapper.addPointsLog(param);
        if (integer > 0) {
            return ResponseResult.success();
        } else {
            return ResponseResult.fail();
        }
    }


    /**
     * 更新用户绑定关系和手机号码。
     * 通过修改UserInfoEntity和UserBindConnectEntity中的用户手机号相关字段，
     * 实现用户手机号更新的同时，更新用户的绑定关系ID。
     *
     * @param paraMap 包含旧手机号和新手机号的映射表。
     * @return 更新操作成功与否的布尔值。
     */
    @Transactional
    public Boolean updateUserBindConnectByUserMobile(Map<String, String> paraMap) {
        // 获取电话区号
        String interDialingCode = com.houjiang.commons.util.StringUtils.isNotBlank(paraMap.get("interDialingCode"))?paraMap.get("interDialingCode"):"+86";
        // 更新UserInfoEntity中的用户手机号。
        Integer userInfoInteger = this.baseMapper.update(null, new LambdaUpdateWrapper<UserInfoEntity>()
                .eq(UserInfoEntity::getUserMobile, paraMap.get("oldUserMobilePhone"))
                .set(UserInfoEntity::getUserMobile, paraMap.get("userMobile"))
                .set(UserInfoEntity::getInternationalDialingCode, interDialingCode));

        // 构造旧的和新的绑定ID。
        String oldBindId = "mobile_" + paraMap.get("oldUserMobilePhone");
        paraMap.put("oldBindId", oldBindId);
        String newBindId = "mobile_" + paraMap.get("userMobile");
        paraMap.put("newBindId", newBindId);

        // 更新UserBindConnectEntity中的绑定关系ID。
        Boolean userBindInteger = userBindConnectService.update(null, new LambdaUpdateWrapper<UserBindConnectEntity>()
                .eq(UserBindConnectEntity::getBindId, oldBindId)
                .set(UserBindConnectEntity::getBindId, newBindId));

        // 返回更新操作是否全部成功的判断结果。
        return 1 == userInfoInteger && userBindInteger;
    }

    /**
     * 上传用户头像。
     * 通过更新UserInfoDetaileEntity和UserBindConnectEntity中的用户头像URL，
     * 实现用户头像的更新。
     *
     * @param userId   用户ID。
     * @param photoUrl 用户新的头像URL。
     * @return 更新操作成功与否的布尔值。
     */
    public Boolean uploadPhoto(String userId, String photoUrl) {

        // 更新UserInfoDetaileEntity中的用户头像URL。
        Boolean torf = userInfoDetailService.update(null, new LambdaUpdateWrapper<UserInfoDetaileEntity>()
                .eq(UserInfoDetaileEntity::getUserId, userId)
                .set(UserInfoDetaileEntity::getUserAvatar, photoUrl));

        // 更新UserBindConnectEntity中的用户头像URL。
        Boolean torf2 = userBindConnectService.update(null, new LambdaUpdateWrapper<UserBindConnectEntity>()
                .eq(UserBindConnectEntity::getUserId, userId).set(UserBindConnectEntity::getBindAvator, photoUrl));

        // 返回更新操作是否成功的判断结果。
        return torf2;
    }

    /**
     * 根据店铺ID查询店铺相关信息。
     * 通过查询UserInfoVO获取与指定店铺ID关联的用户信息。
     *
     * @param shopId 店铺ID。
     * @return 包含店铺用户信息的ResponseResult对象。
     */
    @Override
    public ResponseResult queryShopUserInfo(Integer shopId) {
        UserInfoVO userInfoVO = this.baseMapper.queryShopUserInfo(shopId);
        return ResponseResult.success(userInfoVO);
    }

    /**
     * 根据token更新文章相关信息。
     * 通过token获取用户信息，并更新指定文章的用户相关信息。
     *
     * @param param 包含更新参数的映射表，其中必须包含token。
     * @return 更新操作成功与否的ResponseResult对象。
     */
    @Override
    public ResponseResult updateArticleUserInfo(Map<Object, Object> param) {
        UserInfoVO info = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
        param.put("userId", info.getUserId());
        this.baseMapper.updateArticleUserInfo(param);
        return ResponseResult.success();
    }

    /**
     * 根据电话号码查询用户信息。
     * 通过查询UserInfoVO获取与指定电话号码关联的用户信息。
     *
     * @param checkPhoneMap 包含电话号码的映射表。
     * @return 包含查询结果的UserInfoVO对象。
     */
    @Override
    public UserInfoVO getUserInfoByPhoneNum(Map<String, String> checkPhoneMap) {
        return this.baseMapper.getUserInfoByPhoneNum(checkPhoneMap);
    }

    /**
     * 更新用户昵称和头像URL。
     * 更新UserInfoEntity、UserBindConnectEntity和UserInfoDetaileEntity中的用户昵称和头像URL。
     *
     * @param updateUserParam 包含更新参数的映射表。
     * @return 更新操作成功与否的ResponseResult对象。
     */
    @Override
    public ResponseResult updateUserNickNameavAtarUrl(Map<String, Object> updateUserParam) {
        this.baseMapper.updateUserNickName(updateUserParam);
        userBindConnectService.updateUserNickNameAndAvatarUrl(updateUserParam);
        userInfoDetailService.updateUserAtarUrl(updateUserParam);
        return ResponseResult.success();
    }


    /**
     * 第三方注册接口
     * 通过传入的参数映射对象进行用户注册逻辑处理，包括老用户存在时的更新操作和新用户创建操作。
     * 对于老用户，更新其第三方绑定信息和用户详情；对于新用户，创建用户相关实体并绑定第三方信息。
     *
     * @param map 参数映射对象，包含openid、nickname、avatarUrl等用户信息和第三方绑定信息。
     * @return 注册结果，包含用户token等信息。
     * @throws UnknownHostException 如果获取主机名失败。
     * @throws TransactionException 如果交易异常。
     */
    @Override
    public ResponseResult thirDregister(Map<String, String> map) throws UnknownHostException, TransactionException {
        // 日志记录传入参数
        logger.info("paraMap{}", JSON.toJSONString(map));

        // 从参数映射中提取用户和第三方信息
        String openid = map.get("openid");
        String nickname = map.get("nickName");
        String avatarUrl = map.get("avatarUrl");
        String shopId = map.get("shopId");
        String userMobile = map.get("userMobile");
        String bindType = map.get("bindType");
        String bindId = map.get("bindId");
        String platForm = map.get("platForm");

        // 构建用于查询用户信息的映射对象
        Map<String, String> checkPhoneMap = new HashMap<>();
        checkPhoneMap.put("userMobile", userMobile);
        if (shopId != null) {
            checkPhoneMap.put("shopId", shopId);
        }

        // 根据手机号和店铺ID查询用户信息
        UserInfoVO userInfo = this.baseMapper.getUserInfoByPhoneNum(checkPhoneMap);

        // 初始化用户DTO对象
        UserInfoDTO dto = new UserInfoDTO();

        if (null != userInfo) {
            if(userInfo.getBindOpenid() != null && !userInfo.getBindOpenid().isEmpty() && !userInfo.getBindOpenid().equals(openid)){
                return ResponseResult.fail(BizCodeEnume.MOBILE_NUMBER_HAS_BEEN_USED);
            }
            // 构建用户第三方绑定信息并插入数据库
            Map<String, Object> userThreeBindMap = new HashMap<>();
            userThreeBindMap.put("bindId", bindId);
            userThreeBindMap.put("bind_type", bindType);
            userThreeBindMap.put("bind_nickname", nickname);
            userThreeBindMap.put("bind_avator", avatarUrl);
            userThreeBindMap.put("bind_openid", openid);
            userThreeBindMap.put("bind_unionid", map.get("unionid") == null ? null : map.get("unionid"));
            userThreeBindMap.put("user_id", userInfo.getUserId());
            if (map.get("unionid") != null) {
                UserBindConnectEntity entity = userBindConnectMapper.selectOne(new LambdaQueryWrapper<UserBindConnectEntity>()
                        .eq(UserBindConnectEntity::getBindUnionid, map.get("unionid").toString()));
                if (entity != null) {
                    userThreeBindMap.put("officialAccountOpenid",entity.getOfficialAccountOpenid());
                    userBindConnectMapper.deleteById(entity.getBindId());
                }else{
                    userThreeBindMap.put("officialAccountOpenid","");
                }
            }
            userBindConnectService.insertUserThreeBindMap(userThreeBindMap);

            // 如果用户没有绑定店铺ID，尝试从业务服务获取
            if (null == userInfo.getShopId() && null != userInfo.getUserMobile()) {
                Map<String, String> gateMap = new HashMap<>();
                gateMap.put("userId", userInfo.getUserId());
                ResponseResult shopResult = merchantServiceClient.queryGateShopIdByUserId(gateMap);
                if (null != shopResult.getData()) {
                    Map<String, Integer> shopMap = (Map<String, Integer>) shopResult.getData();
                    userInfo.setShopId(shopMap.get("shopGateId").toString());
                }
            }

            // 创建用户token并设置到Redis
            String token = JwtUtil.createJWT(userInfo.getUserId());
            dto.setUser(userInfo);
            RedisUtil.set(HouConfig.HOU_USER_TOKEN_USER + token, JSON.toJSONString(dto),
                    HouConfig.TOKEN_OUTTIME);
            RedisUtil.set(HouConfig.HOU_USER_TOKEN_USERID + token, userInfo.getUserId(),
                    HouConfig.TOKEN_OUTTIME);

            // 构建返回结果映射对象并更新用户活跃度
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("token", token);
            returnMap.put("userId", userInfo.getUserId());
            returnMap.put("userMobile", userInfo.getUserMobile());
            returnMap.put("platForm", platForm);
            userLiveStatisticsService.saveLiveStatistics(returnMap);

            // 更新用户昵称和头像信息
            Map<String, Object> updateUserParam = new HashMap<>();
            updateUserParam.put("userId", userInfo.getUserId());
            updateUserParam.put("avatarUrl", avatarUrl);
            updateUserParam.put("nickname", nickname);
            updateUserNickNameavAtarUrl(updateUserParam);

            // 判断并处理用户登录积分逻辑
            memberPointsService(userInfo);
            if (shopId != null) {
                // 店铺id 不为空证明是品牌店铺 注册成为店铺会员
                ShopUserDTO shopUserDTO = new ShopUserDTO();
                shopUserDTO.setUserId(userInfo.getUserId());
                shopUserDTO.setShopId(shopId);
                merchantServiceClient.addShopUser(shopUserDTO);
            }
            return ResponseResult.success(token);
        } else {
            // 构建新用户信息对象
            UserInfoEntity record = new UserInfoEntity();
            record.setUserMobile(userMobile);
            record.setPassword("");
            record.setUserLastloginTime(DateUtils.getNowDateToDf());
            record.setUserName(nickname);
            record.setUserNickname(nickname);
            record.setShopId((shopId != null && !shopId.isEmpty()) ? Integer.parseInt(shopId) : null);
            record.setUserLastloginIp(IpUtils.getIpAddress());
            record.setRegisterTerminal(resolveRegisterTerminal(map, shopId));

            // 构建新用户详情对象
            UserInfoDetaileEntity insertUserInfoDetail = new UserInfoDetaileEntity();
            insertUserInfoDetail.setUserRegTime(System.currentTimeMillis() / 1000);
            insertUserInfoDetail.setUserRegIp(IpUtils.getIpAddress());
            insertUserInfoDetail.setUserPlatform(Integer.valueOf(platForm));
            insertUserInfoDetail.setUserAvatar(avatarUrl);
            insertUserInfoDetail.setUserParentId(map.get("shareUserId"));

            // 构建手机号绑定信息对象
            UserBindConnectEntity userBindMap = new UserBindConnectEntity();
            String bindUserMobile = (shopId != null && !shopId.isEmpty()) ? "mobile_" + userMobile + "_" + shopId : "mobile_" + userMobile;
            userBindMap.setBindId(bindUserMobile);
            userBindMap.setBindType(Byte.parseByte(bindType));

            // 构建第三方绑定信息对象
            UserBindConnectEntity userBindOpenidMap = new UserBindConnectEntity();
            userBindOpenidMap.setBindId(bindId);
            userBindOpenidMap.setBindType(Byte.parseByte(platForm));
            userBindOpenidMap.setBindNickname(nickname);
            userBindOpenidMap.setBindAvator(avatarUrl);
            userBindOpenidMap.setBindOpenid(openid);
            if (map.get("unionid") != null) {
                UserBindConnectEntity entity = userBindConnectMapper.selectOne(new LambdaQueryWrapper<UserBindConnectEntity>()
                        .eq(UserBindConnectEntity::getBindUnionid, map.get("unionid").toString()));
                if (entity != null) {
                    userBindOpenidMap.setOfficialAccountOpenid(entity.getOfficialAccountOpenid());
                    userBindConnectMapper.deleteById(entity.getBindId());
                }
            }
            userBindOpenidMap.setBindUnionid(map.get("unionid") == null ? null : map.get("unionid"));

            // 构建用户积分记录对象
            UserPointsLogEntity userPointLog = new UserPointsLogEntity();
            userPointLog.setPointsLogType(CommonType.pointsLogType.GAIN.getCode());
            userPointLog.setClassId(CommonType.pointType.REGISTRATION.getCode());
            userPointLog.setPointsLogTime(new Date());
            userPointLog.setPointsLogDesc(BizCodeEnume.MEMBER_REGISTRATION.getMsg());
            userPointLog.setPointsLogFlag("reg");

            // 生成用户ID并插入用户及相关信息到数据库
            String userId = SnowflakeIdWorker.getSnowId();
            Boolean bool = insertThreeInfoTwo(record, insertUserInfoDetail, userBindMap, userPointLog,
                    userBindOpenidMap, userId);
            if (!bool) {
                String xid = RootContext.getXID();
                if (xid != null) {
                    GlobalTransactionContext.reload(xid).rollback();
                }
                return ResponseResult.fail(BizCodeEnume.REGISTRATION_FAIL);
            }

            // 重新查询用户信息以获取完整数据
            userInfo = this.baseMapper.getUserInfoByPhoneNum(checkPhoneMap);
            // 创建用户token并设置到Redis
            String token = JwtUtil.createJWT(record.getUserId());
            paymentServiceClient.createUser(record.getUserId());
            if (null == userInfo.getShopId() && null != userInfo.getUserMobile()) {
                Map<String, String> gateMap = new HashMap<>();
                gateMap.put("userId", userInfo.getUserId());
                ResponseResult shopResult = merchantServiceClient.queryGateShopIdByUserId(gateMap);
                if (null != shopResult.getData()) {
                    Map<String, String> shopMap = (Map<String, String>) shopResult.getData();
                    userInfo.setShopId(shopMap.get("shopGateId"));
                }
            }
            // 第三方注册同样补个人空间：这里直接下发 token，不带组织成员关系的话
            // 用户拿到 token 后按组织隔离的功能会全部被拒。
            ensurePersonalOrganization(record.getUserId(), map.get("nickName"), userInfo);
            dto.setUser(userInfo);
            RedisUtil.set(HouConfig.HOU_USER_TOKEN_USER + token, JSON.toJSONString(dto),
                    HouConfig.TOKEN_OUTTIME);
            RedisUtil.set(HouConfig.HOU_USER_TOKEN_USERID + token, userInfo.getUserId(),
                    HouConfig.TOKEN_OUTTIME);

            // 构建并返回用户注册成功的结果映射对象
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("token", token);
            returnMap.put("userId", userInfo.getUserId());
            returnMap.put("userMobile", userInfo.getUserMobile());
            returnMap.put("platForm", platForm);
            userLiveStatisticsService.saveLiveStatistics(returnMap);
            if (shopId != null) {
                // 店铺id 不为空证明是品牌店铺 注册成为店铺会员
                ShopUserDTO shopUserDTO = new ShopUserDTO();
                shopUserDTO.setUserId(userInfo.getUserId());
                shopUserDTO.setShopId(shopId);
                merchantServiceClient.addShopUser(shopUserDTO);
            }
            return ResponseResult.success(token);
        }
    }


    /**
     * 用户注册时插入三方信息的逻辑处理。
     *
     * @param userinfo             用户基本信息实体类，包含用户注册时的基本信息。
     * @param insertUserInfoDetail 用户详细信息实体类，包含用户的详细信息。
     * @param userBindMap          用户绑定关系实体类，用于记录用户与手机号、openid等的绑定关系。
     * @param userPointLog         用户积分日志实体类，用于记录用户的积分变动情况。
     * @param userBindOpenidMap    用户与openid的绑定关系实体类，用于记录用户与openid的绑定关系。
     * @param userId               用户ID，用于标识用户。
     * @return 返回一个布尔值，表示用户信息插入是否成功。
     */
    public Boolean insertThreeInfoTwo(UserInfoEntity userinfo, UserInfoDetaileEntity insertUserInfoDetail,
                                      UserBindConnectEntity userBindMap, UserPointsLogEntity userPointLog,
                                      UserBindConnectEntity userBindOpenidMap, String userId) {
        try {
            // 查询积分规则，用于后续设置用户的初始积分。
            //查询积分规则
            ResponseResult resultResp = baseServiceClient.queryIntegralRules();
            Map<String, Integer> integralRules = (Map<String, Integer>) resultResp.getData();

            // 设置用户基本信息，并插入用户表。
            userinfo.setUserId(userId);
            userinfo.setUserName(userinfo.getUserMobile());
            userinfo.setUserCountLogin(1);
            userinfo.setUserIsNew(CommonType.commonStatus.YES.getCode());
            userinfo.setUserIntegral(integralRules.get("ruleRegister"));
            userinfo.setCreateTime(new Date());
            if (com.houjiang.commons.util.StringUtils.isBlank(userinfo.getRegisterTerminal())) {
                userinfo.setRegisterTerminal("houcloud");
            }
            //保存用户
            int one = this.baseMapper.insert(userinfo);

            // 如果用户未设置头像，则使用默认头像。
            if (null == insertUserInfoDetail.getUserAvatar()) {
                ResponseResult avatorMap = baseServiceClient.selectByConfigKey("photo_user_logo");
                Map<String, Object> configMap = (Map<String, Object>) avatorMap.getData();
                String userAvator = configMap.get("configValue").toString();
                insertUserInfoDetail.setUserAvatar(userAvator);
            }
            // 设置用户详细信息，并保存。
            insertUserInfoDetail.setUserId(userId);
            insertUserInfoDetail.setUserMobileVerify(1);
            //保存用户详情
            Boolean two = userInfoDetailService.save(insertUserInfoDetail);

            // 设置用户绑定关系，并保存手机号和openid的绑定信息。
            userBindMap.setUserId(userId);
            userBindMap.setCreateUser(userinfo.getUserMobile());
            //保存用户绑定表  手机号  openid
            Boolean three = userBindConnectService.save(userBindMap);
            userBindOpenidMap.setUserId(userId);
            Boolean five = userBindConnectService.save(userBindOpenidMap);

            // 记录用户注册积分，并保存积分日志。
            //添加积分变更表
            userPointLog.setUserId(userId);
            userPointLog.setPointsLogPoints(integralRules.get("ruleRegister"));
            userPointLog.setPointsLogOperate("会员注册");
            Boolean four = userPointsLogService.save(userPointLog);

            // 根据注册积分修改用户等级。
            // 修改会员等级
            Map<String, Object> gradeMap = new HashMap<>();
            gradeMap.put("ruleRegister", integralRules.get("ruleRegister"));
            gradeMap.put("userId", userId);
            ResponseResult result = baseServiceClient.saveTagClass(gradeMap);

            // 绑定用户信息到登录上下文。
            UserInfoVO ui = new UserInfoVO();
            ui.setUserId(userId);
            LoginContext context = new LoginContext(ui);
            LoginContext.binding(context);

            // 判断所有操作是否成功，如果成功则缓存用户ID并返回true，否则返回false。
            // 判断是否添加成功
            if (CommonType.commonStatus.YES.getCode() == one
                    && two
                    && three
                    && four
                    && five
                    && CommonStatusEnum.SUCCESS.getCode() == result.getCode()) {
                RedisUtil.set(HouConfig.HOU_USER_LOGON_USERID + userinfo.getUserMobile(), userId,RedisUtil.EXRP_HOUR);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    /**
     * 用户登录后处理会员积分服务。
     * 该方法用于检查用户当天是否登录过，如果没有登录过，则根据积分规则给用户增加登录积分，并记录积分变更。
     *
     * @param userInfo 用户信息，包含用户的唯一标识符。
     */
    /** 用户会员,积分,等级方法 */
    public void memberPointsService(UserInfoVO userInfo) {
        // 初始化请求参数，用于查询用户当天是否已登录
        //判断当前用户当天是否已经登录过，登录过不添加积分
        Map<String, String> param = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        param.put("startTime", sdf.format(DateUtils.getStartTime()));
        param.put("endTime", sdf.format(DateUtils.getEndTime()));
        param.put("userId", userInfo.getUserId());
        param.put("classId", "2");

        // 查询用户当天是否已登录
        ResponseResult data = userPointsLogService.queryLoginExit(param);

        // 如果当天用户未登录，则进行积分增加操作
        if (data.getData() == null) {
            // 查询积分规则，获取登录积分值
            //查询积分规则
            ResponseResult resultResp = baseServiceClient.queryIntegralRules();
            Map<String, Integer> integralRules = (Map<String, Integer>) resultResp.getData();

            // 准备积分更新信息，设置用户ID、积分值和积分类型（增加）
            //修改会员积分和会员等级
            MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
            ArrayList<String> list = new ArrayList<>();
            list.add(userInfo.getUserId());
            updateMemberMessageListIntegral.setUserIdList(list);
            updateMemberMessageListIntegral.setIntegral(integralRules.get("ruleLogin"));
            //1是增加积分 0 是扣除积分
            updateMemberMessageListIntegral.setType(1);

            // 更新用户积分
            updateMemberIntegral(updateMemberMessageListIntegral);

            // 记录积分变更信息
            //添加积分修改记录
            Map<String, Object> pointParam = new HashMap<>();
            pointParam.put("PointsLogType", CommonType.pointsLogType.GAIN.getCode());
            pointParam.put("ClassId", CommonType.pointType.LOGIN.getCode());
            pointParam.put("PointsLogTime", new Date());
            pointParam.put("PointsLogDesc", CommonType.pointType.LOGIN.getDesc());
            pointParam.put("PointsLogFlag", "reg");
            pointParam.put("UserId", userInfo.getUserId());
            pointParam.put("PointsLogPoints", integralRules.get("ruleLogin"));
            pointParam.put("PointsLogOperate", CommonType.pointType.LOGIN.getDesc());

            // 插入积分变更记录
            userPointsLogService.insertByMap(pointParam);
        }
    }


    /**
     * 添加门面店信息
     *
     * @param gateAddStoreDTO 包含门面店详细信息的数据传输对象
     * @return 添加操作的结果，成功或失败及原因
     */
    @Override
    public ResponseResult addGateStore(GateStoreUpdateDTO gateAddStoreDTO) {
        Map<String, Object> result = new HashMap<>();
        // 验证token是否有效
        if (gateAddStoreDTO.getToken() != null && !gateAddStoreDTO.getToken().isEmpty()) {
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(gateAddStoreDTO.getToken());
            // 检查用户是否有权限添加门面店
            if (userInfo.getShopId() == null) {
                return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
            }
            // 验证电话号码格式
            // 校验登录名是否符合要求
            if (!VerificationUtils.isPhonePattern(gateAddStoreDTO.getPhone())) {
                result.put("start", 2);
                return ResponseResult.fail(BizCodeEnume.INCORRECT_PHONE_NUMBER_FORMAT);
            }
            // 检查该电话号码是否已注册店铺或门店
            // 校验登录名
            // 校验登录名下是否：1.注册；2.有店铺；3有门店
            // 根据手机号查用户表是否存在
            Map<String, String> param = new HashMap<>();
            param.put("phone", gateAddStoreDTO.getPhone());
            param.put("shopType", String.valueOf(userInfo.getShopType()));
            param.put("shopId", String.valueOf(userInfo.getShopId()));
            ResponseResult rr = queryPhoneNumCount(param);
            if (rr == null || CommonStatusEnum.SUCCESS.getCode() != rr.getCode()) {
                result.put("start", 2);
                return ResponseResult.fail(rr.getMessage(), result);
            }
            // 根据用户类型检查该电话号码是否已注册门面店
            // 根据手机号查门店表
            // 判断是否为品牌店铺
            Integer gataPhoneNum = 0;
            if (userInfo.getShopType() == 3) {
                // 品牌店铺逻辑
                gataPhoneNum = this.baseMapper.queryGatePhoneNumCount1(gateAddStoreDTO.getPhone(), userInfo.getShopId());
            } else {
                // 非品牌店铺逻辑
                gataPhoneNum = this.baseMapper.queryGatePhoneNumCount(gateAddStoreDTO.getPhone());
            }
            if (gataPhoneNum > 0) {
                result.put("start", 2);
                return ResponseResult.fail(BizCodeEnume.REGISTERED_STORE_WITH_PHONE_NUMBER);
            }
            // 插入门面店信息
            ////////////////////////////////////////////////////////////////////////////////////////////////////
            // 存储信息
            Map<String, Object> params = new HashMap<>();
            // 查询门店用户的userid
            String userId = this.baseMapper.queryGateUserId(gateAddStoreDTO.getPhone());
            if (userId != null) {
                params.put("userId", userId);
            }
            if (userInfo != null && userInfo.getShopId() != null) {
                params.put("shopId", userInfo.getShopId());
            }
            // 设置门面店信息
            setGateStoreParams(params, gateAddStoreDTO);
            params.put("state", 1);
            params.put("limits", "chain_stockList,chain_self,chain_out,webPos,chain_rider");
            params.put("shopType", userInfo.getShopType());
            Integer register = this.baseMapper.registerGateStore(params);
            if (register == 1) {
                result.put("start", 1);
                return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
            } else {
                result.put("start", 2);
                return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
            }
        } else {
            result.put("start", 2);
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    /**
     * 设置门面店参数
     *
     * @param params          存储门面店信息的参数映射
     * @param gateAddStoreDTO 包含门面店详细信息的数据传输对象
     */
    private void setGateStoreParams(Map<String, Object> params, GateStoreUpdateDTO gateAddStoreDTO) {
        if (gateAddStoreDTO.getPhone() != null) {
            params.put("loginName", gateAddStoreDTO.getPhone());
        }
        if (gateAddStoreDTO.getGatestoreName() != null) {
            params.put("gatestoreName", gateAddStoreDTO.getGatestoreName());
        }
        if (gateAddStoreDTO.getProvince() != null) {
            params.put("province", gateAddStoreDTO.getProvince());
        }
        if (gateAddStoreDTO.getCity() != null) {
            params.put("city", gateAddStoreDTO.getCity());
        }
        if (gateAddStoreDTO.getCounty() != null) {
            params.put("county", gateAddStoreDTO.getCounty());
        }
        if (gateAddStoreDTO.getDetailAddress() != null) {
            params.put("detailAddress", gateAddStoreDTO.getDetailAddress());
        }
        if (gateAddStoreDTO.getLongitude() != null) {
            params.put("longitude", gateAddStoreDTO.getLongitude().toString());
        }
        if (gateAddStoreDTO.getLatitude() != null) {
            params.put("latitude", gateAddStoreDTO.getLatitude().toString());
        }
        if (gateAddStoreDTO.getGatePhone() != null) {
            params.put("gatePhone", gateAddStoreDTO.getGatePhone());
        }
        if (gateAddStoreDTO.getBusinessTime() != null) {
            params.put("businessTime", gateAddStoreDTO.getBusinessTime());
        }
        if (gateAddStoreDTO.getTrafficRoute() != null) {
            params.put("trafficRoute", gateAddStoreDTO.getTrafficRoute());
        }
        if (gateAddStoreDTO.getGatePic() != null) {
            params.put("gatePic", gateAddStoreDTO.getGatePic());
        }
    }

    /**
     * 注册并创建二维码。
     * 该方法用于处理注册时生成特定商店二维码的请求。它首先验证用户的token，然后根据用户的商店ID生成一个二维码URL，
     * 最后将这个URL及其Base64编码字符串作为响应返回。
     *
     * @param paraMap 包含请求参数的映射，其中必须包含token。
     * @return 返回一个包含二维码URL和其Base64编码字符串的响应结果。
     * @throws NotFoundException 如果根据token找不到用户信息，则抛出NotFoundException。
     * @throws IOException       如果在处理图像时发生I/O错误，则抛出IOException。
     * @throws WriterException   如果在生成二维码时发生错误，则抛出WriterException。
     */
    @Override
    public ResponseResult registerCreateqr(Map<String, String> paraMap) throws NotFoundException, IOException, WriterException {
        // 从请求参数中获取用户token
        String token = paraMap.get("token");
        // 根据token获取用户信息，包括用户的商店ID
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 将商店ID转换为字符串，用于构建二维码URL
        String shopId = userInfo.getShopId().toString();
        // 构建二维码URL，包含商店ID
        String url = HouConfig.QR_CODE_REGISTER_SHOPID + shopId;
        // 将二维码URL转换为Base64编码字符串
        String baseString = QRCodeUtil.text2Base64Str(url);
        // 准备返回的数据映射，包含二维码URL和Base64编码字符串
        Map<String, String> returnMap = new HashMap<String, String>();
        returnMap.put("url", url);
        returnMap.put("baseString", baseString);
        // 构建成功响应结果，并返回
        return ResponseResult.success(returnMap);
    }


    /**
     * 根据传入的参数查询会员信息。
     *
     * @param param 查询参数，包含token等信息。
     * @return 返回查询结果，包含分页信息和会员信息。
     */
    @Override
    public ResponseResult queryMember(Map<String, Object> param) {
        // 从参数中获取token
        String token = (String) param.get("token");
        // 初始化一个空的Map用于后续操作
        HashMap<Object, Object> map = new HashMap<>();

        // 判断用户权限
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(token);

        // 初始化shopId和权限判断标志
        Integer integer = null;
        String shopId = null;

        // 如果权限判断为真，则获取用户信息
        if (userPermissionsResult.getFlag()) {
            // 根据token获取用户信息
            // 根据code存储用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            // 从用户信息中获取shopId
            shopId = userInfo.getShopId();
        }

        // 将shopId加入查询参数
        param.put("shopId", shopId);

        // 执行查询，获取分页的会员信息
        IPage<BrandMemberVO> page = this.baseMapper.querymember(new Query<BrandMemberVO>().getPage(param), param);

        // 返回查询结果
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 查询会员信息列表，并导出为Excel文件。
     *
     * @param param 查询参数，包含token等信息。
     * @return 返回查询结果的URL，用于下载Excel文件。
     */
    @Override
    public ResponseResult memberMessageListExp(Map<String, Object> param) {
        // 校验token是否为空
        if (null == param.get("token")) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_NULL);
        }
        // 从Redis中根据token获取用户信息
        String userinfo = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + param.get("token"));
        // 校验token是否有效
        if (userinfo == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 定义Excel表格的标题
        String[] title = {"会员名称", "会员ID", "手机号码", "注册时间", "性别", "最后登陆时间", "购买次数", "支付总额"};
        // 初始化数据列表
        List<Map<String, String>> list = Lists.newArrayList();
        // 执行查询操作
        List<MemberMessageListVO> result = querymember1(param);
        // 检查查询结果数量是否超过限制
        if (result.size() >= 30000) {
            return ResponseResult.fail(0, "请小于三万条");
        }
        // 遍历查询结果，处理并添加到数据列表
        for (MemberMessageListVO res : result) {
            // 将性别转换为中文描述
            if (res.getUserGender() != null) {
                switch (res.getUserGender()) {
                    case "0":
                        res.setUserGender("保密");
                        break;
                    case "1":
                        res.setUserGender("男");
                        break;
                    case "2":
                        res.setUserGender("女");
                        break;
                    default:
                        res.setUserGender("");
                        break;
                }
            } else {
                res.setUserGender("");
            }
            // 处理注册时间为空的情况
            if (null != res.getUserRegTime()) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            } else {
                res.setUserRegTime("");
            }
            // 处理支付总额为空的情况
            if (null == res.getSumAmount()) {
                res.setSumAmount(new BigDecimal("0.00"));
            }
            // 将单条会员信息转换为Map格式，并添加到数据列表
            HashMap<String, String> map = com.google.common.collect.Maps.newHashMap();
            map.put("会员名称", res.getUserName());
            map.put("会员ID", res.getUserId() + "");
            map.put("手机号码", res.getUserMobile() + "");
            map.put("注册时间", res.getUserRegTime() + "");
            map.put("最后登陆时间", res.getUserLastloginTime() + "");
            map.put("支付总额", res.getSumAmount() + "");
            list.add(map);
        }
        // 构建Excel表格数据结构
        // excel文件名
        Map<String, List<Map<String, String>>> map = Maps.newHashMap();
        map.put("数据", list);
        // 生成Excel文件，并返回文件的URL
        String url = ExcelsUtil.createExcel(title, map, new int[]{0});
        return ResponseResult.success(url);
    }

    /**
     * 根据传入的参数更新用户信息中的删除状态。
     *
     * @param param 包含用户token、用户ID和删除状态的参数映射。
     * @return 返回操作结果，成功或失败，并附带相应的错误码。
     */
    @Override
    public ResponseResult updatememberstatus(Map<String, Object> param) {
        // 初始化操作标志为0，表示默认失败
        Integer returnFlag = 0;
        // 检查用户token是否存在，如果存在表示用户验证通过
        if (RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + param.get("token"))) {
            // 使用LambdaUpdateWrapper更新用户信息，主要更新用户的删除状态
            Integer integer = this.baseMapper.update(null, new LambdaUpdateWrapper<UserInfoEntity>()
                    .eq(UserInfoEntity::getUserId, param.get("userId").toString())
                    .set(UserInfoEntity::getIsDelete, param.get("isDelete").toString()));
            // 如果更新条数大于0，表示更新成功
            if (integer > 0) {
                return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
            } else {
                // 更新失败
                return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
            }
        }
        // 用户token验证失败
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
    }

    /**
     * 根据用户手机号码检查用户状态和账号审核状态。
     *
     * @param userMobile 用户的手机号码。
     * @return 根据用户状态和账号审核状态返回不同的结果。
     */
    @Override
    public ResponseResult checkphone(String userMobile) {
        // 通过手机号码查询用户状态和用户ID
        Map map = this.baseMapper.checkphone(userMobile);

        // 检查查询结果是否为空
        if (!CollectionUtils.isEmpty(map)) {
            // 获取用户状态
            Integer userState = (Integer) map.get("user_state");
            // 获取用户ID字符串
            String userId = String.valueOf(map.get("user_id"));

            // 判断用户状态是否为正常或已冻结
            if (userState == 1 || userState == 2) {
                // 返回账号正常的结果
                return ResponseResult.success(1, "账号正常", 1);
            } else {
                // 查询账号的审核状态
                // 查看审核状态
                Integer integer = this.baseMapper.checkbusinessphone(userId);

                // 根据账号审核状态返回不同的结果
                if (integer != null && integer == 0) {
                    // 账号审核中
                    return ResponseResult.success(1, "账号审核中", 2);
                } else if (integer != null && integer == 1) {
                    // 账号正常
                    return ResponseResult.success(1, "账号正常", 1);
                } else if (integer != null && integer == 2) {
                    // 账号审核失败
                    return ResponseResult.success(1, "账号审核失败", 3);
                }
            }
        }
        // 如果查询结果为空或未找到对应账号，返回无账号信息的结果
        return ResponseResult.success(1, "无账号信息", 0);
    }

    /**
     * 根据传入的参数更新用户生日和性别信息。
     * <p>
     * 本方法通过解析token获取用户信息，然后根据参数Map中的键值对更新对应的用户生日和性别信息。
     * 如果token无效或解析失败，将返回错误信息。
     * 成功更新后，返回成功消息。
     *
     * @param paraMap 包含更新参数的Map，其中键包括"token"、"userBirth"和"userGender"。
     *                "token"用于验证用户身份。
     *                "userBirth"为可选参数，用于更新用户的生日信息。
     *                "userGender"为可选参数，用于更新用户的性别信息。
     * @return ResponseResult 对象，表示更新操作的结果。如果更新失败，返回错误代码和消息；
     * 如果更新成功，返回成功代码和消息。
     */
    @Override
    public ResponseResult updateUserBirthAndGender(Map<String, String> paraMap) {
        // 从参数Map中获取token
        String token = paraMap.get("token");
        // 使用token解析用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 检查用户信息是否有效，如果用户ID为空，则更新操作失败
        if (null == userInfo.getUserId()) {
            return ResponseResult.fail(2, "用户信息为空!");
        }
        // 创建UpdateWrapper用于构建更新条件和设置更新字段
        UpdateWrapper<UserInfoDetaileEntity> updateWrapper = new UpdateWrapper();
        // 设置更新条件为用户ID等于解析出的用户ID
        updateWrapper.eq("user_id", userInfo.getUserId());
        // 如果参数Map中包含"userBirth"且其值不为空，则将用户生日更新为该值
        if (StringUtils.isNotBlank(paraMap.get("userBirth"))) {
            updateWrapper.set("user_birth", paraMap.get("userBirth"));
        }
        // 如果参数Map中包含"userGender"且其值不为空，则将用户性别更新为该值
        if (StringUtils.isNotBlank(paraMap.get("userGender"))) {
            updateWrapper.set("user_gender", paraMap.get("userGender"));
        }
        // 调用mapper的update方法，使用null作为更新对象（因为只更新条件字段），和updateWrapper作为更新条件和设置
        userInfoDetailMapper.update(null, updateWrapper);
        // 更新成功后，返回成功结果
        return ResponseResult.success(1, "修改用户信息成功");
    }

    /**
     * 更新验证信息。
     * 该方法用于根据传入的参数更新用户的验证信息。其中，通过token获取用户ID，并对参数进行处理和补充，最后调用数据库操作类更新用户信息。
     * 如果更新成功，则认证状态更新为已认证，并在缓存中更新用户信息；如果更新失败，则返回认证失败的信息。
     *
     * @param paraMap 包含更新信息的参数映射表，其中必须包含token信息。
     * @return ResponseResult 返回一个表示操作结果的响应对象，包含成功与否的状态码和消息。
     */
    @Override
    public ResponseResult updateVerifiedInfo(Map<String, Object> paraMap) {
        // 从参数映射中提取token
        String token = String.valueOf(paraMap.get("token"));
        // 通过token从Redis缓存中获取用户ID
        String userId = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USERID + token);
        // 将用户ID添加到参数映射中
        paraMap.put("userId", userId);
        // 将面部标志和字体标志添加到参数映射中，用于更新用户信息
        paraMap.put("userIdentityFaceLogo", paraMap.get("faceLogo"));
        paraMap.put("userIdentityFontLogo", paraMap.get("fontLogo"));
        // 根据用户认证结束时间判断是否需要长期验证，并在参数映射中设置相应的值
        if (paraMap.get("validateLongTerm") == null) {
            paraMap.put("validateLongTerm", paraMap.get("userIdentityEndTime") != null && !paraMap.get("userIdentityEndTime").equals("") ? 1 : 0);
        } else {
            paraMap.put("validateLongTerm", paraMap.get("validateLongTerm"));
        }
        // 调用userInfoDetailMapper更新用户中心信息
        Integer integer = userInfoDetailMapper.updateUcenterInfo(paraMap);
        // 如果更新条数为1，表示更新成功
        if (1 == integer) {
            // 更新用户状态为已认证
            this.baseMapper.updateUserStart(userId);
            // 通过用户ID获取用户信息
            UserInfoVO userInfo = this.baseMapper.getUserInfoByUserId(userId);
            // 从Redis缓存中获取token对应的用户信息DTO
            String user = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
            UserInfoDTO userInfoDTO = JSON.parseObject(user, UserInfoDTO.class);
            // 如果缓存中没有用户信息DTO，则创建一个新的
            if (userInfoDTO == null) {
                userInfoDTO = new UserInfoDTO();
            }
            // 设置用户信息到用户信息DTO
            userInfoDTO.setUser(userInfo);
            // 将更新后的用户信息DTO缓存起来
            RedisUtil.set(HouConfig.HOU_USER_TOKEN_USER + token, JSON.toJSONString(userInfoDTO), HouConfig.TOKEN_OUTTIME);
            // 返回认证成功的响应结果
            return ResponseResult.success(1, "认证成功");
        }
        // 如果更新不成功，则返回认证失败的响应结果
        return ResponseResult.fail(0, "认证失败");
    }

    /**
     * 根据手机号通过平台检查用户是否已注册。
     *
     * @param map 包含手机号的映射表。
     * @return 如果用户已注册，返回注册成功的响应结果；否则，返回注册失败的响应结果。
     */
    @Override
    public ResponseResult checkPhoneByPlatform(Map<String, Object> map) {
        // 通过手机号查询用户信息，检查用户是否已注册
        // 根据手机号查询用户 id
        String phone = this.baseMapper.selectOne(new LambdaQueryWrapper<UserInfoEntity>()
                .eq(UserInfoEntity::getUserMobile, String.valueOf(map.get("phone")))).getUserId();

        // 如果查询结果不为空，说明用户已注册
        if (phone != null) {
            return ResponseResult.success("已注册");
        }
        // 如果查询结果为空，说明用户未注册
        return ResponseResult.fail(0, "未注册");
    }

    /**
     * 根据用户ID查询用户信息。
     *
     * @param userId 用户ID
     * @return 用户信息的AcquiescenceVO对象
     */
    @Override
    public AcquiescenceVO queryUserInfoByjob(String userId) {
        AcquiescenceVO vo = this.baseMapper.queryUserInfoByjob(userId);
        return vo;
    }

    /**
     * 根据传入的参数查询成员消息列表。
     *
     * @param param 查询参数，包括token等信息
     * @return 成员消息列表
     */
    public List<MemberMessageListVO> querymember1(Map<String, Object> param) {
        // 从参数中获取token
        String token = (String) param.get("token");
        HashMap<Object, Object> map = new HashMap<>();
        // 判断用户权限
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(token);
        Integer integer = null;
        String shopId = null;
        // 如果权限判断成功，获取用户信息和店铺ID
        if (userPermissionsResult.getFlag()) {
            // 从Redis中获取用户信息
            // 根据code存储用户信息
            String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            shopId = userInfo.getShopId();
        }
        // 将店铺ID加入查询参数
        param.put("shopId", shopId);
        // 执行查询
        IPage<MemberMessageListVO> page = this.baseMapper.querymember1(new Query<MemberMessageListVO>().getPage(param),
                param);

        return page.getRecords();
    }

    /**
     * 更新用户的登录时间计数。
     *
     * @param userId 用户ID
     * @return 更新结果的ResponseResult对象
     */
    @Override
    public ResponseResult updateLoginTimeCount(String userId) {
        Integer num = this.baseMapper.updateLoginTimeCount(userId);
        return ResponseResult.success();
    }


    /**
     * 校验用户密码是否符合规则。
     * 通过调用基础服务客户端获取密码长度和格式的配置信息，进而对用户输入的密码进行校验。
     *
     * @param password 用户输入的密码字符串
     * @return 返回校验结果，如果密码不符合规则，则返回失败结果并指出具体问题；如果密码符合规则，则返回成功结果。
     */
    public ResponseResult checkPassword(String password) {
        // 用于存储校验结果的映射表
        Map<String, String> returnMap = new HashMap<String, String>();

        // 从配置中获取密码长度要求
        String configLength = baseServiceClient.selectConfigValueFeign(CommonType.configKey.PASSWORD_LENGTH.getDesc());
        // 如果密码长度不符合要求，则返回失败结果并提示密码长度不满足要求
        if (password.length() < Integer.parseInt(configLength)) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "密码长度不满足要求！必须为" + configLength + "位以上");
        }

        // 从配置中获取密码格式要求
        // 1-纯数字;2-纯英文;3-数字英文;4-数字英文符号
        String configFormat = baseServiceClient.selectConfigValueFeign(CommonType.configKey.PASSWORD_FORMAT.getDesc());
        // 根据不同的密码格式要求，初始化正则表达式模式
        Pattern pattern = null;
        String desc = "";
        if ("1".equals(configFormat)) {
            pattern = pattern1;
            desc = "密码只能为纯数字！";
        } else if ("2".equals(configFormat)) {
            pattern = pattern2;
            desc = "密码只能为纯英文！";
        } else if ("3".equals(configFormat)) {
            pattern = pattern3;
            desc = "密码只能为数字和英文的组合！";
        } else if ("4".equals(configFormat)) {
            pattern = pattern4;
            desc = "密码只能为数字、英文和符号的组合！";
        }

        // 使用匹配器对密码进行正则匹配
        Matcher match = pattern.matcher(password);
        // 如果密码格式不符合要求，则返回失败结果并提示密码格式错误
        if (!match.matches()) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), desc);
        }

        // 如果密码长度和格式都符合要求，则返回成功结果
        return ResponseResult.success(BizCodeEnume.VERIFICATION_SUCCESSFUL);
    }

    /**
     * 查询商铺用户信息
     *
     * 本方法的目的是根据商铺用户提供的信息查询并返回详细的用户资料它通过调用底层数据库映射功能，
     * 使用提供的商铺用户信息（如商铺ID或用户ID）作为查询条件来获取匹配的用户详细信息如果查询成功，
     * 它将包装查询结果，并以成功响应的形式返回
     *
     * @param shopUserDTO 包含查询条件（如商铺ID或用户ID）的商铺用户DTO对象
     * @return 返回一个ResponseResult对象，其中包含查询到的商铺用户信息如果查询成功，状态为成功
     */
    @Override
    public ResponseResult shopUserQueryUserInfo(ShopUserDTO shopUserDTO) {
        // 调用基础映射器的方法，根据提供的商铺用户DTO查询详细的用户信息
        shopUserDTO = this.baseMapper.shopUserQueryUserInfo(shopUserDTO);
        // 返回一个成功响应，将查询到的商铺用户信息作为响应数据
        return ResponseResult.success(shopUserDTO);
    }

    /**
     * 根据用户ID查询用户的手机号码
     *
     * @param userId 用户ID，用于标识查询的用户
     * @return 用户的手机号码如果查询不到，则返回空字符串
     */
    @Override
    public Map<String,String> queryUserMobileByuserId(String userId) {
        Map<String,String> result = this.baseMapper.queryUserMobileByuserId(userId);
        return result;
    }

    /**
     * 查询用户信息
     * 该方法通过用户ID查询用户的相关信息，包括会员等级和消费信息
     *
     * @param paraMap 包含查询参数的映射，主要包含用户ID
     * @return 返回包含用户信息的响应结果
     */
    @Override
    public ResponseResult imQueryUserInfo(Map<String, String> paraMap) {
        if(!StringUtils.isNotBlank(paraMap.get("mallUserId"))){
            return ResponseResult.success();
        }
        // 根据用户ID查询用户信息
        ImUserInfoVO imUserInfoVO = this.baseMapper.imQueryUserInfo(paraMap.get("mallUserId"));

        // 查询会员等级
        Map<String, Object> mallUserId = baseServiceClient.queryMemberShipGradeByUserId(paraMap.get("mallUserId"));
        if (null != mallUserId) {
            // 设置会员等级名称和URL
            imUserInfoVO.setMemberName(mallUserId.get("grade_name") + "");
        }

        // 查询用户 消费金额 下单数
        Map<String, Object> stringObjectMap = orderServiceClient.queryUserConsumption(paraMap);
        if(null != stringObjectMap){
            // 设置用户的消费金额和下单数
            imUserInfoVO.setConsumeAmount(stringObjectMap.get("consumeAmount") != null ? new BigDecimal(stringObjectMap.get("consumeAmount").toString()) : BigDecimal.ZERO);
            imUserInfoVO.setOrderNum(stringObjectMap.get("orderNum") != null ? Integer.valueOf(stringObjectMap.get("orderNum").toString()) : 0);
        }

        // 返回包含用户信息的响应结果
        return ResponseResult.success(imUserInfoVO);
    }

    @Override
    public ResponseResult getUserInfoByMapStr(Map<String, String> map) {
        // 初始化查询条件
        QueryWrapper<UserInfoEntity> queryWrapper = new QueryWrapper<>();

        // 如果map不为空且包含userId，添加查询条件：用户ID
        if (null != map && null != map.get("userId")) {
            queryWrapper.eq("user_id", map.get("userId"));
        }

        // 如果map不为空且包含userMobile，添加查询条件：用户手机号
        if (null != map && null != map.get("userMobile")) {
            queryWrapper.eq("user_mobile", map.get("userMobile"));
        }

        // 根据查询条件查询用户信息
        UserInfoEntity userInfoEntity = this.baseMapper.selectOne(queryWrapper);

        // 如果用户信息存在，尝试设置用户的显示名（昵称或用户名）
        if (null != userInfoEntity) {
            userInfoEntity.setUserNickname(userInfoEntity.getUserNickname() == null
                    ? userInfoEntity.getUserName() : userInfoEntity.getUserNickname());
            UserInfoDetaileEntity details = userInfoDetailMapper.selectOne(new LambdaQueryWrapper<UserInfoDetaileEntity>()
                    .select(UserInfoDetaileEntity::getUserAvatar)
                    .eq(UserInfoDetaileEntity::getUserId, userInfoEntity.getUserId()));
            userInfoEntity.setUserAvatar(details.getUserAvatar());
            UserSellerBaseEntity sellerBase = userSellerBaseMapper.selectOne(new LambdaQueryWrapper<UserSellerBaseEntity>()
                    .select(UserSellerBaseEntity::getShopId)
                    .eq(UserSellerBaseEntity::getUserId, userInfoEntity.getUserId()));
            if(sellerBase != null && sellerBase.getShopId() != null){
                userInfoEntity.setShopId(sellerBase.getShopId());
                userInfoEntity.setIsShop(1);
            }
        }

        // 返回查询结果，成功则包含用户信息，失败则为空
        return ResponseResult.success(userInfoEntity);
    }

    /**
     * 商家创建子账号时校验账号
     *
     * @param param 子账号信息，包括待校验的用户名
     * @return 校验结果，成功或失败以及可能的用户ID
     */
    @Override
    public ResponseResult shopCheckUserName(SubAccountDTO param) {
        // 根据用户名查询用户信息
        UserInfoEntity userEntity = baseMapper.selectOne(new LambdaQueryWrapper<UserInfoEntity>()
                .eq(UserInfoEntity::getUserName, param.getUserName()));

        // 如果用户不存在，则校验通过
        if (userEntity == null) {
            return ResponseResult.success();
        }
        // 获取当前商家信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        List<String> userSellerUserIdList = userSellerBaseMapper.selectList(new LambdaQueryWrapper<>()).stream()
                .map(UserSellerBaseEntity::getUserId).toList();
        if(!userSellerUserIdList.contains(userEntity.getUserId())){
            return ResponseResult.success();
        }
        // 如果当前商家ID为空，则校验失败
        return ResponseResult.fail(BizCodeEnume.MOBILE_NUMBER_HAS_BEEN_USED);
    }

    @Override
    public void updateIsNewFeign(Map<String, String> paraMap) {
        this.baseMapper.updateIsNewFeign(paraMap);
    }

    @Override
    public Integer forgotPasswordFeign(Map<String, Object> paraMap) {
        Integer result= this.baseMapper.forgotPasswordFeign(paraMap);
        return result;
    }

    @Override
    public String queryUserInfoUserId(String userMobile) {
        String result= this.baseMapper.queryUserInfoUserId(userMobile);
        return result;
    }

    @Override
    public UserInfoVO getUserInfoByPhoneNumber(String userId) {
        UserInfoVO result = this.baseMapper.getUserInfoByPhoneNumberFeign(userId);
        return result;
    }

    @Override
    public Integer checkPhoneNumberFeign(Map<String, Object> param) {
        Integer result = this.baseMapper.checkPhoneNumberFeign(param);
        return result;
    }

    @Override
    public String getPrimaryAccountId(String userId) {
        String result = this.baseMapper.getPrimaryAccountId(userId);
        return result;
    }

    @Override
    public String selectuserphone(String userId) {
        String result = this.baseMapper.selectuserphone(userId);
        return result;
    }

    @Override
    public UserInfoVO getUserInfoByMobileFeign(String userMobile) {
        UserInfoVO result = this.baseMapper.getUserInfoByMobileFeign(userMobile);
        return result;
    }

    /**
     * 根据用户 ID 查找用户名称
     *
     * @param userId 用户 Id
     * @return 用户名称
     */
    @Override
    public String queryUserNameByUserId(String userId) {
        return baseMapper.queryUserNameByUserId(userId);
    }

    /**
     * 根据用户 ID 获取用户的组织列表
     *
     * @param userId 用户 ID
     * @return 组织列表
     */
    @Override
    public List<Map<String, Object>> getUserOrgList(String userId) {
        return baseMapper.getUserOrgList(userId);
    }

}
