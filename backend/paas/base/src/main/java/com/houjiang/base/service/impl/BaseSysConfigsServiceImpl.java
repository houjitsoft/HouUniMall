package com.houjiang.base.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.NavSortLogoVO;
import com.houjiang.base.dto.SystemConfigHallDTO;
import com.houjiang.base.dto.WebConfigDTO;
import com.houjiang.base.entity.BaseSysConfigsEntity;
import com.houjiang.base.entity.BaseSystemWebConfigEntity;
import com.houjiang.base.entity.BaseWebConfigEntity;
import com.houjiang.base.feign.BusinessClient;
import com.houjiang.base.feign.GoodsClient;
import com.houjiang.base.feign.OrderClient;
import com.houjiang.base.feign.UsermsClient;
import com.houjiang.base.mapper.BaseSysConfigsMapper;
import com.houjiang.base.mapper.BaseSystemWebConfigMapper;
import com.houjiang.base.mapper.BaseWebConfigMapper;
import com.houjiang.base.service.BaseSysConfigsService;
import com.houjiang.base.service.RSAKeyManagementService;
import com.houjiang.commons.vo.base.PointsCashRatioVo;
import com.houjiang.base.vo.SystemConfigHallSlideVO;
import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.config.ThreadPoolConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.goods.NavSortLogoDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.*;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.secverifyapiutil.Base64Utils;
import com.houjiang.commons.vo.base.BaseSysConfigsVO;
import com.houjiang.commons.vo.base.PlatformoperationVO;
import com.houjiang.commons.vo.base.WabSystemConfigHallVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @ClassName BaseSysConfigsServiceImpl
 * @Description
 * @Author Amos
 * @Date 2023-06-13 09:05:20
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseSysConfigsServiceImpl extends ServiceImpl<BaseSysConfigsMapper, BaseSysConfigsEntity>
        implements BaseSysConfigsService {
    /** 引入线程池 */
    private static final ExecutorService executorService = ThreadPoolConfig.executorService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RSAKeyManagementService rsaKeyManagementService;

    @Autowired
    private BaseSysConfigsMapper baseSysConfigsMapper;
    @Autowired
    private BaseWebConfigMapper baseWebConfigMapper;
    @Autowired
    private BaseSystemWebConfigMapper baseSystemWebConfigMapper;
    @Autowired
    private UsermsClient usermsClient;
    @Value("${domainName}")
    private String domain;
    @Autowired
    private OrderClient orderClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private BusinessClient businessClient;


    @Override
    public String selectConfigValueFeign(String configKey) {
        return baseSysConfigsMapper.selectConfigValue(configKey);
    }

    /**
     * 根据导航排序和Logo信息查询导航Logo配置。
     *
     * @param gdbNavSortLogoDTO 导航排序和Logo的DTO，包含配置键值和类型。
     * @return 查询结果的响应对象，包含导航Logo的配置列表。
     */
    @Override
    public ResponseResult quertNavLogo(NavSortLogoDTO gdbNavSortLogoDTO) {
        // 初始化存储配置信息的列表
        List<BaseSysConfigsEntity> webNavLogoConbfigDTOList = new ArrayList<>();
        // 从Redis缓存中尝试获取配置信息，如果配置键不为空
        // 如果ConfigKey 不是null那证明是单查 从redis获取redis中不存在查询数据库
        if (null != gdbNavSortLogoDTO && StrUtil.isNotBlank(gdbNavSortLogoDTO.getConfigKey())) {
            // 从Redis中获取配置值
            String s = (String) redisCache.getCacheObject("SYSTEM_" + gdbNavSortLogoDTO.getConfigKey());
            if (null != s) {
                // 创建一个配置实体，填充键值和配置值
                BaseSysConfigsEntity webNavLogoConbfigDTO = new BaseSysConfigsEntity();
                webNavLogoConbfigDTO.setConfigKey(gdbNavSortLogoDTO.getConfigKey());
                webNavLogoConbfigDTO.setConfigValue(s);
                // 将配置实体添加到列表中
                webNavLogoConbfigDTOList.add(webNavLogoConbfigDTO);
                // 返回查询结果
                return ResponseResult.success(webNavLogoConbfigDTOList);
            }
        }
        // 从数据库中查询配置信息，如果配置类型和键不为空
        webNavLogoConbfigDTOList = this.baseMapper.selectList(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                .eq(!StringUtils.isEmpty(gdbNavSortLogoDTO.getConfigType()), BaseSysConfigsEntity::getConfigType, gdbNavSortLogoDTO.getConfigType())
                .eq(!StringUtils.isEmpty(gdbNavSortLogoDTO.getConfigKey()), BaseSysConfigsEntity::getConfigKey, gdbNavSortLogoDTO.getConfigKey())
                .orderByAsc(BaseSysConfigsEntity::getConfigSort));
        // 使用单线程执行器来异步更新Redis缓存
        List<BaseSysConfigsEntity> finalWebNavLogoConbfigDTOList = webNavLogoConbfigDTOList;
        executorService.submit(() -> {
            if (null != finalWebNavLogoConbfigDTOList) {
                for (BaseSysConfigsEntity webNavLogoConbfigDTO : finalWebNavLogoConbfigDTOList) {
                    // 将配置信息存入Redis缓存
                    redisCache.setCacheObject("SYSTEM_" + webNavLogoConbfigDTO.getConfigKey(), webNavLogoConbfigDTO.getConfigValue());
                }
            }
        });
        // 关闭执行器
        // 返回查询结果
        return ResponseResult.success(webNavLogoConbfigDTOList);
    }

    /**
     * 根据配置键获取图片配置URL。
     *
     * @param configKey 配置的键。
     * @return 查询结果的响应对象，包含图片配置的URL。
     */
    @Override
    public ResponseResult getImageConfigUrl(String configKey) {
        // 检查配置键是否为空
        if (configKey == null || "".equals(configKey)) {
            // 如果为空，返回失败响应
            return ResponseResult.fail(BizCodeEnume.KEY_VALUE_CANNOT_BE_EMPTY.getMsg());
        }
        // 根据配置键查询配置实体
        BaseSysConfigsEntity merchantSysConfigsEntity = new BaseSysConfigsEntity();
        merchantSysConfigsEntity = this.baseMapper.selectOne(new QueryWrapper<BaseSysConfigsEntity>().eq("config_key", configKey));
        // 获取配置实体的值
        String imageValue = merchantSysConfigsEntity.getConfigValue();
        // 返回成功响应，包含配置值
        return ResponseResult.success(imageValue);
    }

    /**
     * 根据配置键查询配置信息。
     *
     * @param configKey 配置的键，用于唯一标识配置项。
     * @return 查询结果，如果键为空或不存在，则返回失败结果；否则返回对应的配置实体。
     */
    @Override
    public ResponseResult selectByConfigKey(String configKey) {
        // 校验配置键是否为空
        if (configKey == null || "".equals(configKey)) {
            return ResponseResult.fail(BizCodeEnume.KEY_VALUE_CANNOT_BE_EMPTY.getMsg());
        }
        // 根据配置键查询配置实体
        BaseSysConfigsEntity entity = this.baseMapper.selectOne(new QueryWrapper<BaseSysConfigsEntity>()
                .eq("config_key", configKey));
        // 对特定配置键进行额外处理
        if (CommonType.configKey.THEME_WEB_COLOR.getDesc().equals(configKey)) {
            entity.setConfigComment(domain + "/pc/");
        }

        return ResponseResult.success(entity);
    }

    /**
     * 查询积分指数信息。
     *
     * @param param 查询参数，包括token等信息。
     * @return 积分指数及相关信息的查询结果。
     */
    @Override
    public ResponseResult queryIntegralIndex(Map<String, String> param) {
        Map<String, Object> resMap = new HashMap<>();
        Map<String, Object> resultmap = new HashMap<>();
        try {
            // 查询积分图片信息
            List<BaseSysConfigsEntity> list = this.baseMapper.queryIntegralIndex("integral_image");
            // 如果参数中包含token，进一步查询用户相关信息
            if (null != param && null != param.get("token")) {
                // 通过token获取用户信息
                UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token"));
                // 查询用户会员信息
                ResponseResult userMemberInformation = usermsClient.getUserMemberInformation(param);
                if (null != userMemberInformation) {
                    resultmap = (Map<String, Object>) userMemberInformation.getData();
                    // 优化积分显示名称
                    resultmap.put("distanceIntegral", resultmap.get("nextGradeRule"));
                    // 如果用户昵称存在，则替换用户名显示
                    if (null != userInfo.getUserNickname() && !"".equals(userInfo.getUserNickname())) {
                        resultmap.put("userName", userInfo.getUserNickname());
                    }
                }
                // 查询用户订单数量
                if (null != userInfo) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", userInfo.getUserId());
                    ResponseResult resp1 = orderClient.queryIntegraOrdersCount(map);
                    if (null != resp1.getData()) {
                        Integer remap = (Integer) resp1.getData();
                        resultmap.put("orderCount", remap);
                    }
                }
            }
            // 组装最终的查询结果
            resMap.put("resultmap", resultmap);
            resMap.put("list", list);
            return ResponseResult.success(resMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 如果发生异常，返回系统错误信息
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), BizCodeEnume.SYSTEM_ERROR.getMsg());
    }

    /**
     * 查询电话和邮箱配置信息。
     *
     * @param param 查询参数，目前未使用。
     * @return 配置信息列表，包含电话和邮箱等信息。
     */
    @Override
    public ResponseResult queryPhoneAndMail(UserTokenBeanDTO param) {
        List<SystemConfigHallSlideVO> slides = null;
        // 查询电话和邮箱配置信息
        slides = baseSysConfigsMapper.selectPhoneAndMail();
        return ResponseResult.success(slides);
    }

    /**
     * 根据配置键更新配置的启用状态。
     *
     * @param configKey    配置的键。
     * @param configEnable 配置的启用状态。
     * @return 操作结果。
     */
    @Override
    public ResponseResult updateConfigEnableByConfigKey(String configKey, String configEnable) {
        this.baseMapper.update(null, new LambdaUpdateWrapper<BaseSysConfigsEntity>()
                .set(BaseSysConfigsEntity::getConfigEnable, configEnable)
                .eq(BaseSysConfigsEntity::getConfigKey, configKey));
        return ResponseResult.success();
    }

    /**
     * 根据键更新系统配置。
     *
     * @param dto 更新配置的数据传输对象，包含配置信息和用户信息。
     * @return 操作结果。
     */
    @Override
    public ResponseResult updateSysConfigByKey(SystemConfigHallDTO dto) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(dto.getToken());
        List<BaseSysConfigsEntity> configs = dto.getSystemConfigAddParam();
        // 验证用户信息并更新配置
        if (null != userInfo) {
            for (BaseSysConfigsEntity config : configs) {
                // 校验配置值的合法性
                if ("2".equals(config.getOpType()) && config.getOpValue().contains(",")) {
                    return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION);
                }
                config.setCreateDate(new Date());
                config.setUpdateDate(new Date());
                config.setCreateBy(userInfo.getUserId());
                config.setUpdateBy(userInfo.getUserId());
            }
            this.baseMapper.updateSetting(configs);
            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 根据配置键查询系统配置信息。
     *
     * @param map 查询参数映射表，其中包含键值对。
     * @return 如果键为主题统一颜色，则返回WebConfigDTO对象，否则返回配置信息列表。
     */
    @Override
    public ResponseResult querySysConfigByKey(Map<String, Object> map) {
        // 根据配置类型查询系统配置列表，并按配置排序升序排列
        List<BaseSysConfigsEntity> slides = this.baseMapper.selectList(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                .eq(BaseSysConfigsEntity::getConfigType, map.get("key").toString())
                .orderByAsc(BaseSysConfigsEntity::getConfigSort));
        // 判断查询的键是否为主题统一颜色
        if (CommonType.configKey.THEME_UNI_COLOR.getDesc().equals(map.get("key").toString())) {
            WebConfigDTO webConfig = new WebConfigDTO();
            // 将配置信息复制到WebConfigDTO对象
            BeanUtils.copyProperties(slides.get(0), webConfig);
            // 将配置值和操作值转换为字符串和整数列表
            List<String> wapColorList = ObjectChangeList.castObjToList(JSON.parse(slides.get(0).getConfigValue()), String.class);
            List<Integer> setList = ObjectChangeList.castObjToList(JSON.parse(slides.get(0).getOpValue()), Integer.class);
            // 设置颜色和设置列表
            webConfig.setColorList(wapColorList);
            webConfig.setSetList(setList);
            // 设置配置评论
            webConfig.setConfigComment(domain + "/h5/");
            // 返回成功结果，包含WebConfigDTO对象
            return ResponseResult.success(webConfig);
        } else {
            // 返回成功结果，包含配置信息列表
            return ResponseResult.success(slides);
        }
    }

    /**
     * 根据配置键和配置类型查询配置值。
     *
     * @param configKey  配置键。
     * @param configType 配置类型。
     * @return 如果查询条件为空，则返回失败结果；否则返回配置值。
     */
    @Override
    public ResponseResult getConfigValue(String configKey, String configType) {
        // 判断查询条件是否为空
        if (configKey == null || "".equals(configType)) {
            return ResponseResult.fail(BizCodeEnume.KEY_VALUE_CANNOT_BE_EMPTY.getMsg());
        }
        // 根据配置键和配置类型查询配置信息
        BaseSysConfigsEntity merchantSysConfigsEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                .eq(BaseSysConfigsEntity::getConfigKey, configKey).eq(BaseSysConfigsEntity::getConfigType, configType));
        // 获取配置值
        String configValue = merchantSysConfigsEntity.getConfigValue();
        // 返回成功结果，包含配置值
        return ResponseResult.success(configValue);
    }

    /**
     * 根据参数查询主题H5颜色设置。
     *
     * @param param 查询参数映射表，其中包含键值对。
     * @return 如果键为主题统一颜色，则返回WebConfigDTO对象，否则返回配置信息列表。
     */
    @Override
    public ResponseResult queryThemeH5ColorSetting(Map<String, Object> param) {
        // 根据配置类型查询系统配置列表，并按配置排序升序排列
        List<BaseSysConfigsEntity> slides = this.baseMapper.selectList(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                .eq(BaseSysConfigsEntity::getConfigType, param.get("key").toString())
                .orderByAsc(BaseSysConfigsEntity::getConfigSort));
        // 判断查询的键是否为主题统一颜色
        if (CommonType.configKey.THEME_UNI_COLOR.getDesc().equals(param.get("key").toString())) {
            WebConfigDTO webConfig = new WebConfigDTO();
            // 将配置信息复制到WebConfigDTO对象
            BeanUtils.copyProperties(slides.get(0), webConfig);
            // 将配置值和操作值转换为字符串和整数列表
            List<String> wapColorList = ObjectChangeList.castObjToList(JSON.parse(slides.get(0).getConfigValue()), String.class);
            List<Integer> setList = ObjectChangeList.castObjToList(JSON.parse(slides.get(0).getOpValue()), Integer.class);
            // 设置颜色和设置列表
            webConfig.setColorList(wapColorList);
            webConfig.setSetList(setList);
            // 设置配置评论
            webConfig.setConfigComment(domain + "/h5/");
            // 返回成功结果，包含WebConfigDTO对象
            return ResponseResult.success(webConfig);
        } else {
            // 返回成功结果，包含配置信息列表
            return ResponseResult.success(slides);
        }
    }

    /**
     * 更新主题颜色页面设置。
     *
     * @param webConfig 包含配置信息的WebConfigDTO对象。
     * @return 更新成功则返回成功结果。
     */
    @Override
    public ResponseResult updateThemeColorPageSetting(WebConfigDTO webConfig) {
        // 设置更新条件和更新值
        // 设置主题色的key和值
        LambdaUpdateWrapper<BaseSysConfigsEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(BaseSysConfigsEntity::getConfigValue, webConfig.getConfigValue());
        if (CommonType.configKey.THEME_UNI_COLOR.getDesc().equals(webConfig.getConfigKey())) {
            updateWrapper.set(BaseSysConfigsEntity::getOpValue, webConfig.getConfigRemarks());
        }
        updateWrapper.eq(BaseSysConfigsEntity::getConfigKey, webConfig.getConfigKey());
        // 执行更新操作
        this.baseMapper.update(null, updateWrapper);
        // 返回成功结果
        return ResponseResult.success();
    }

    /**
     * 更新导航栏常用排序标志和图标。
     *
     * @param gdbNavSortLogoDTOs 包含导航栏配置信息的DTO对象列表。
     * @return 更新成功则返回成功结果。
     */
    @Override
    public ResponseResult updateNavCommonSortLogo(NavSortLogoVO gdbNavSortLogoDTOs) {
        List<NavSortLogoDTO> logos = gdbNavSortLogoDTOs.getLogos();
        if (logos != null) {
            for (NavSortLogoDTO logo : logos) {
                // 更新配置信息
                //update
                Integer updateNav = this.baseMapper.update(null, new LambdaUpdateWrapper<BaseSysConfigsEntity>()
                        .set(BaseSysConfigsEntity::getConfigValue, logo.getConfigValue())
                        .eq(BaseSysConfigsEntity::getConfigKey, logo.getConfigKey()));
                // 如果更新成功，则更新Redis中的配置值
                if (updateNav == 1) {
                    redisCache.setCacheObject("SYSTEM_" + logo.getConfigKey(), logo.getConfigValue());
                    redisCache.deleteObject("SYSTEM_logo_copywriting");
                }
            }
            // 查询并更新导航栏图标配置到Redis
            List<BaseSysConfigsEntity> webNavLogoConbfigDTOList = new ArrayList<>();
            webNavLogoConbfigDTOList = this.baseMapper.selectList(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                    .eq(BaseSysConfigsEntity::getConfigType, "h5_haibao")
                    .orderByAsc(BaseSysConfigsEntity::getConfigSort));
            redisCache.setCacheObject("SYSTEM_h5_haibao", JSON.toJSONString(webNavLogoConbfigDTOList));
        }
        // 返回成功结果
        return ResponseResult.success();
    }

    /**
     * 更新导航栏常见排序标志的Logo。
     * 从Base64编码的字符串中解析出Logo信息，并更新到数据库和Redis中。
     *
     * @param map 包含Base64编码的Logo信息的参数映射。
     * @return 更新操作的结果。
     */
    @Override
    public ResponseResult updateNavCommonSortLogo2(Map<String, String> map) {
        // 检查参数是否存在
        if (null != map && map.get("logos") != null) {
            String fromBase64 = null;
            try {
                // 从Base64字符串中解码数据
                String data = map.get("logos");
                fromBase64 = Base64Utils.getFromBase64(data);
            } catch (Exception e) {
                // 解码失败返回错误信息
                e.printStackTrace();
                return ResponseResult.fail(BizCodeEnume.DECODING_FAILED.getCode(), BizCodeEnume.DECODING_FAILED.getMsg());
            }
            // 将解码后的JSON字符串转换为对象列表
            List<NavSortLogoDTO> logos = JsonUtils.string2Obj(fromBase64, List.class, NavSortLogoDTO.class);
            if (null != logos && logos.size() > 0) {
                for (NavSortLogoDTO logo : logos) {
                    // 更新数据库中的配置值
                    int update = this.baseMapper.update(null, new LambdaUpdateWrapper<BaseSysConfigsEntity>()
                            .set(BaseSysConfigsEntity::getConfigValue, logo.getConfigValue())
                            .eq(BaseSysConfigsEntity::getConfigKey, logo.getConfigKey()));
                    if (update == 1) {
                        // 更新Redis中的配置值
                        redisCache.setCacheObject("SYSTEM_" + logo.getConfigKey(), logo.getConfigValue());
                        // 删除缓存中的旧标志复制信息
                        redisCache.deleteObject("SYSTEM_logo_copywriting");
                    }
                }
            }
            // 操作成功返回结果
            return ResponseResult.success();
        } else {
            // 参数缺失返回错误信息
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER.getCode(), BizCodeEnume.MISSING_PARAMETER.getMsg());
        }
    }

    /**
     * 查询广告信息。
     * 根据用户权限和店铺ID查询特定广告的配置信息。
     *
     * @param param 包含查询参数的映射，包括用户令牌。
     * @return 查询结果，如果找不到广告则返回空结果。
     */
    @Override
    public ResponseResult queryadvertising(Map<String, Object> param) {
        // 获取用户令牌
        String token = (String) param.get("token");
        // 判断用户权限
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(token);
        Integer shopId = null;
        if (userPermissionsResult.getFlag()) {
            // 根据令牌获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
            if (StringUtils.isNotEmpty(userInfo.getShopId())) {
                shopId = Integer.valueOf(userInfo.getShopId());
            }
        }
        // 构建广告配置的键名
        String configKey = CommonType.configKey.WEB_ME_PAGE_ADVERTISING.getDesc() + "_" + shopId;
        // 从数据库中查询广告配置
        BaseSysConfigsEntity systemConfigHallSlideResults = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                .eq(BaseSysConfigsEntity::getConfigKey, configKey));
        if (systemConfigHallSlideResults != null) {
            // 返回查询结果
            return ResponseResult.success(systemConfigHallSlideResults);
        }
        // 如果找不到广告配置，返回空结果
        return ResponseResult.success(2);
    }

    /**
     * 更新广告信息。
     * 根据用户权限和店铺ID更新特定广告的配置信息。
     *
     * @param param 包含更新参数的映射，包括用户令牌和广告配置信息。
     * @return 更新操作的结果。
     */
    @Override
    public ResponseResult updateAdvertising(Map<String, Object> param) {
        // 获取用户令牌
        String token = (String) param.get("token");
        // 判断用户权限
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(token);
        Integer shopId = null;
        if (userPermissionsResult.getFlag()) {
            // 根据令牌获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
            if (StringUtils.isNotEmpty(userInfo.getShopId())) {
                shopId = Integer.valueOf(userInfo.getShopId());
            }
        }
        // 构建广告配置的键名
        String configKey = CommonType.configKey.WEB_ME_PAGE_ADVERTISING.getDesc() + "_" + shopId;
        // 从数据库中查询现有的广告配置
        BaseSysConfigsEntity systemConfigHallSlideResults = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                .eq(BaseSysConfigsEntity::getConfigKey, configKey));
        // 将参数映射转换为广告配置对象
        BaseSysConfigsEntity configs = ParseMapUtils.parseMap2Object(param, BaseSysConfigsEntity.class);
        configs.setConfigKey(configKey);
        configs.setConfigType(configKey);
        if (systemConfigHallSlideResults != null) {
            // 更新现有的广告配置
            this.baseMapper.update(configs, new LambdaUpdateWrapper<BaseSysConfigsEntity>()
                    .eq(BaseSysConfigsEntity::getConfigKey, configKey));
            return ResponseResult.success();
        } else {
            // 如果不存在，则插入新的广告配置
            this.baseMapper.insert(configs);
            return ResponseResult.success();
        }
    }

    /**
     * 更新热搜词。
     * 根据用户权限和店铺ID更新热搜词配置。
     *
     * @param param 包含更新参数的映射，包括用户令牌和热搜词配置。
     * @return 更新操作的结果。
     */
    @Override
    public ResponseResult updatehotword(Map<String, Object> param) {
        // 获取用户令牌
        String token = (String) param.get("token");
        // 判断用户权限
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(token);
        Integer shopId = null;
        if (userPermissionsResult.getFlag()) {
            // 根据令牌获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
            if (StringUtils.isNotEmpty(userInfo.getShopId())) {
                shopId = Integer.valueOf(userInfo.getShopId());
            }
            // 构建热搜词配置的键名
            String configKey = "brandshop_hot_word_" + shopId;
            // 从数据库中查询现有的热搜词配置
            BaseSysConfigsEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                    .eq(BaseSysConfigsEntity::getConfigKey, configKey));
            // 获取并处理配置值
            String configValue = String.valueOf(param.get("configValue"));
            List<String> result1 = Arrays.asList(StringUtils.split(configValue, ","));
            if (result1.size() > 9) {
                // 如果热搜词数量超过限制，返回错误
                return ResponseResult.fail(BizCodeEnume.PLATFROM_SEARCH_UPDATE_ERROP);
            }
            if (entity == null) {
                // 如果不存在，则插入新的热搜词配置
                BaseSysConfigsEntity shophot1 = new BaseSysConfigsEntity();
                shophot1.setConfigValue(configValue);
                shophot1.setConfigKey(configKey);
                shophot1.setConfigComment("品牌店铺热搜词");
                shophot1.setOpType("5");
                shophot1.setOpValue("");
                shophot1.setConfigSort(1);
                shophot1.setConfigType("search");
                shophot1.setConfigEnable(1);
                shophot1.setTerminalType(2);
                shophot1.setCreateBy(userInfo.getUserName());
                shophot1.setUpdateBy(userInfo.getUserName());
                this.baseMapper.insert(shophot1);
            } else {
                // 如果存在，则更新现有的热搜词配置
                entity.setConfigValue(configValue);
                this.baseMapper.updateById(entity);
            }
        }
        // 操作成功返回结果
        if (shopId != null) {
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        } else {
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        }
    }


    /**
     * 更新广告页面信息。
     * 通过遍历传入的配置项列表，更新相应的配置项数据。
     *
     * @param param 包含系统配置添加参数的DTO对象。
     * @return 返回操作结果的响应对象。
     */
    @Override
    public ResponseResult updateAdPage(SystemConfigHallDTO param) {
        // 获取系统配置添加参数列表
        List<BaseSysConfigsEntity> configs = param.getSystemConfigAddParam();
        // 遍历配置项列表，设置更新时间、创建时间、创建人、更新人、启用状态等信息，并进行更新
        for (BaseSysConfigsEntity config : configs) {
            config.setCreateDate(new Date());
            config.setUpdateDate(new Date());
            config.setCreateBy("0");
            config.setUpdateBy("0");
            config.setConfigEnable(1);
            this.baseMapper.update(config, new LambdaUpdateWrapper<BaseSysConfigsEntity>()
                    .eq(BaseSysConfigsEntity::getConfigKey, config.getConfigKey()));
        }
        // 返回操作成功的响应结果
        return ResponseResult.success(1);
    }

    /**
     * 查询我的页面广告信息。
     * 根据传入的参数查询特定商店的广告信息，如果没有指定商店，则查询通用的广告信息。
     *
     * @param paraMap 包含查询参数的映射对象。
     * @return 返回广告信息的响应对象。
     */
    @Override
    public ResponseResult queryMePage(Map<String, String> paraMap) {
        // 定义配置项的类型键
        String key = CommonType.configKey.WEB_ME_PAGE_ADVERTISING.getDesc();
        // 如果指定了商店ID，则拼接商店ID到键值中
        if (null != paraMap.get("shopId") && StringUtils.isNotBlank(paraMap.get("shopId"))) {
            key = key + "_" + paraMap.get("shopId");
        }
        // 根据键值查询广告信息列表，并按排序顺序返回第一条广告信息
        List<BaseSysConfigsEntity> slides = this.baseMapper.selectList(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                .eq(BaseSysConfigsEntity::getConfigType, key)
                .orderByAsc(BaseSysConfigsEntity::getConfigSort));
        if (null != slides) {
            // 如果查询结果存在，返回第一条广告信息
            return ResponseResult.success(slides.get(0));
        }
        // 如果查询结果为空，返回空的响应结果
        return ResponseResult.success();
    }

    /**
     * 为uni-app查询wap主题颜色。
     * 从系统配置中获取wap主题颜色和设置列表，并返回给uni-app应用。
     *
     * @return 返回包含wap主题颜色和设置列表的响应对象。
     */
    @Override
    public ResponseResult queryWapColorForUniApp() {
        // 根据配置键查询wap主题颜色配置
        BaseWebConfigEntity webConfig = baseWebConfigMapper.selectById("uni_color");
        // 将配置值解析为字符串列表，配置备注解析为整数列表
        List<String> wapColorList = ObjectChangeList.castObjToList(JSON.parse(webConfig.getConfigValue()), String.class);
        List<Integer> setList = ObjectChangeList.castObjToList(JSON.parse(webConfig.getConfigRemarks()), Integer.class);
        // 设置主题颜色和设置列表到配置对象，并设置配置备注为H5路径
        webConfig.setColorList(wapColorList);
        webConfig.setSetList(setList);
        webConfig.setConfigComment(domain + "/h5/");
        // 返回处理后的配置对象
        return ResponseResult.success(webConfig);
    }

    /**
     * 查询平台操作规范配置。
     * 从系统配置中查询平台操作规范的配置项，并返回配置内容。
     *
     * @return 返回包含平台操作规范的响应对象。
     */
    @Override
    public ResponseResult queryplatformoperation() {
        // 根据配置类型查询平台操作规范的配置项
        List<BaseSysConfigsEntity> pos = this.baseMapper.selectList(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                .eq(BaseSysConfigsEntity::getConfigType, CommonType.configType.PLATFORM_OPERATION_SPECIFICATIONS.getDesc()));
        // 创建平台操作规范的VO对象，并根据查询结果设置配置内容
        PlatformoperationVO platformoperationEntity = new PlatformoperationVO();
        if (pos != null && pos.size() > 0) {
            String configValue = pos.get(0).getConfigValue();
            platformoperationEntity = JSONObject.parseObject(configValue, PlatformoperationVO.class);
        }
        // 设置配置键为平台操作规范的描述
        platformoperationEntity.setConfigKey(CommonType.configType.PLATFORM_OPERATION_SPECIFICATIONS.getDesc());
        // 返回平台操作规范的响应对象
        return ResponseResult.success(platformoperationEntity);

    }

    /**
     * 更新平台操作信息。
     * 校验token存在性后，更新平台操作的解释说明和操作信息。
     *
     * @param map 包含更新参数的映射对象。
     * @return 返回操作结果的响应对象。
     */
    @Override
    public ResponseResult updateoperation(Map<String, Object> map) {
        // 校验token是否存在
        boolean isExistToken = redisCache.hasKey(HouConfig.HOU_PLATFORM_TOKEN_USER + String.valueOf(map.get("token")));
        if (!isExistToken) {
            // 如果token不存在，返回权限不足的错误响应
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        // 解码解释说明内容
        String fromBase64 = null;
        if (map.get("explanation") != null && map.get("explanation") != "") {
            fromBase64 = Base64Utils.getFromBase64(String.valueOf(map.get("explanation")));
        }
        // 解析操作信息的VO对象
        PlatformoperationVO platformoperationEntity =
                JSONObject.parseObject(JSONObject.toJSONString(map.get("operation")), PlatformoperationVO.class);
        // 设置解码后的解释说明内容
        platformoperationEntity.setExplanation(fromBase64);

        // 将操作信息序列化为JSON字符串，用于更新系统配置
        String operation = JSONObject.toJSONString(platformoperationEntity);
        // 创建新的系统配置项对象，并设置更新后的操作信息
        BaseSysConfigsEntity sys = new BaseSysConfigsEntity();
        List<BaseSysConfigsEntity> configs = new ArrayList<>();
        sys.setConfigKey(String.valueOf(map.get("configKey")));
        sys.setConfigValue(operation);
        sys.setConfigComment("平台管理规范");
        sys.setOpValue("");
        sys.setUpdateDate(new Date());
        configs.add(sys);
        // 根据配置键查询现有的系统配置项，如果存在，则更新配置项，否则新建配置项
        sys = this.baseMapper.selectById(String.valueOf(map.get("configKey")));
        if (sys != null) {
            this.updateBatchById(configs);
        } else {
            this.saveBatch(configs);
        }
        // 返回操作成功的响应结果
        return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
    }

    /**
     * 更新猫粮金额
     *
     * @param map 包含更新金额的映射
     * @return 更新结果的响应对象
     */
    @Override
    public ResponseResult updatecatmoney(Map<String, Object> map) {
        // 检查令牌是否存在
        boolean isExistToken = redisCache.hasKey(HouConfig.HOU_PLATFORM_TOKEN_USER + String.valueOf(map.get("token")));

        // 如果令牌不存在，返回权限不足的错误
        if (!isExistToken) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        // 设置系统配置项，更新猫粮金额
        BaseSysConfigsEntity sys = new BaseSysConfigsEntity();
        sys.setConfigKey(CommonType.configKey.RETENTION_MONEY_DEFAULT_LIMIT.getDesc());
        sys.setConfigValue(String.valueOf(map.get("amount")));
        this.baseMapper.updateById(sys);

        // 返回更新成功的响应
        return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
    }

    /**
     * 查询猫粮金额
     *
     * @return 查询结果的响应对象，包含猫粮金额
     */
    @Override
    public ResponseResult querycatmoney() {
        // 根据配置类型查询系统配置项
        List<BaseSysConfigsEntity> pos =
                this.baseMapper.selectList(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                        .eq(BaseSysConfigsEntity::getConfigType, CommonType.configKey.RETENTION_MONEY_DEFAULT_LIMIT.getDesc()));
        String amount = null;
        // 如果查询结果存在，获取第一个配置项的金额值
        if (pos != null && pos.size() > 0) {
            amount = pos.get(0).getConfigValue();
        }
        // 返回查询成功的响应，包含猫粮金额
        return ResponseResult.success("质保金默认额度", amount);
    }

    /**
     * 根据商店ID查询热门关键词
     *
     * @param param 包含商店ID和令牌的映射
     * @return 查询结果的响应对象，包含热门关键词
     */
    @Override
    public ResponseResult selecthotword(Map<String, Object> param) {
        // 从参数中尝试获取商店ID
        Integer shopId = null;
        if (null != param.get("shopId") && StringUtils.isNotBlank(param.get("shopId").toString())) {
            shopId = (Integer) param.get("shopId");
        }
        // 如果商店ID不存在，尝试从令牌中获取用户信息并提取商店ID
        if (null == shopId && StringUtils.isNotBlank(param.get("token").toString())) {
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.get("token").toString());
            if (StringUtils.isNotEmpty(userInfo.getShopId())) {
                shopId = Integer.valueOf(userInfo.getShopId());
            }
        }
        // 如果商店ID仍然不存在，返回失败的响应
        if (null == shopId) {
            return ResponseResult.fail();
        }
        // 根据商店ID构建配置项的键
        String config_key = "brandshop_hot_word_" + shopId;

        // 根据键查询系统配置项
        BaseSysConfigsEntity merchantSysConfigsEntity = this.baseMapper.selectOne(new QueryWrapper<BaseSysConfigsEntity>().eq("config_key", config_key));

        // 如果查询结果存在，返回成功的响应，包含热门关键词
        if (merchantSysConfigsEntity != null) {
            return ResponseResult.success(merchantSysConfigsEntity.getConfigValue());
        }
        // 如果查询结果不存在，返回失败的响应，提示店铺没有设置
        return ResponseResult.fail(0, "店铺没有设置");
    }

    /**
     * 配置业务密码规则
     *
     * @return 配置结果的响应对象，包含密码规则的详细信息
     */
    @Override
    public ResponseResult businessPasswordConfig() {
        // 初始化返回映射
        Map<String, String> returnMap = new HashMap<>();
        // 分别查询密码长度和格式的系统配置项（配置缺失时回落到默认值，避免 NPE 冒泡成 20010）
        BaseSystemWebConfigEntity merchantSysConfigsEntity;
        merchantSysConfigsEntity = baseSystemWebConfigMapper.selectOne(new QueryWrapper<BaseSystemWebConfigEntity>().eq("config_key", "password_length"));
        String passwordLength = (merchantSysConfigsEntity == null
                || merchantSysConfigsEntity.getConfigValue() == null
                || merchantSysConfigsEntity.getConfigValue().trim().isEmpty())
                ? "8" : merchantSysConfigsEntity.getConfigValue().trim();
        merchantSysConfigsEntity = baseSystemWebConfigMapper.selectOne(new QueryWrapper<BaseSystemWebConfigEntity>().eq("config_key", "password_format"));
        String passwordFormat = (merchantSysConfigsEntity == null
                || merchantSysConfigsEntity.getConfigValue() == null
                || merchantSysConfigsEntity.getConfigValue().trim().isEmpty())
                ? "3" : merchantSysConfigsEntity.getConfigValue().trim();
        // 构建密码规则描述：文案必须与实际校验口径一致，且不再使用「纯数字！」这类误导性表述
        String desc;
        if ("1".equals(passwordFormat)) {
            desc = "密码长度不少于" + passwordLength + "位，且仅限数字";
        } else if ("2".equals(passwordFormat)) {
            desc = "密码长度不少于" + passwordLength + "位，且仅限字母";
        } else if ("3".equals(passwordFormat)) {
            desc = "密码长度不少于" + passwordLength + "位，需同时包含字母和数字";
        } else if ("4".equals(passwordFormat)) {
            desc = "密码长度不少于" + passwordLength + "位，需包含字母、数字和符号";
        } else {
            desc = "密码长度不少于" + passwordLength + "位";
        }
        // 将密码规则信息添加到返回映射
        returnMap.put("passwordFormat", passwordFormat);
        returnMap.put("passwordLength", passwordLength);
        returnMap.put("desc", desc);
        // 返回成功的响应，包含密码规则信息
        return ResponseResult.success(returnMap);
    }

    /**
     * 查询业务开关配置
     *
     * @return 查询结果的响应对象，包含业务开关的状态
     */
    @Override
    public ResponseResult queryBusinessConfig() {
        // 查询业务开关的系统配置项
        BaseSystemWebConfigEntity merchantSysConfigsEntity = baseSystemWebConfigMapper.selectOne(
                new QueryWrapper<BaseSystemWebConfigEntity>().eq("config_key", "business_switch"));
        // 返回成功的响应，包含业务开关的状态
        return ResponseResult.success(merchantSysConfigsEntity.getConfigValue());
    }

    /**
     * 查询信息滑块配置
     *
     * @param param 查询参数对象，目前未使用
     * @return 查询结果的响应对象，包含信息滑块的列表
     */
    @Override
    public ResponseResult queryInformationSlider(NavSortLogoDTO param) {
        // 根据配置类型查询信息滑块的系统配置项
        List<BaseSysConfigsEntity> slides = this.baseMapper.selectList(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                .eq(BaseSysConfigsEntity::getConfigType, CommonType.configType.INFORMATION_SLIDER.getDesc())
                .orderByAsc(BaseSysConfigsEntity::getConfigSort));
        // 返回成功的响应，包含信息滑块的列表
        return ResponseResult.success(slides);
    }

    /**
     * 更新信息滑块配置
     *
     * @param gdbNavSortLogoDTOs 更新参数对象，包含滑块信息的列表
     * @return 更新结果的响应对象
     */
    @Override
    public ResponseResult updateInformationSlider(NavSortLogoVO gdbNavSortLogoDTOs) {
        // 遍历滑块信息列表，更新相应的系统配置项
        List<NavSortLogoDTO> logos = gdbNavSortLogoDTOs.getLogos();
        if (logos != null) {
            for (NavSortLogoDTO logo : logos) {
                //update
                Integer updateNav = this.baseMapper.update(null, new LambdaUpdateWrapper<BaseSysConfigsEntity>()
                        .set(BaseSysConfigsEntity::getConfigValue, logo.getConfigValue())
                        .set(BaseSysConfigsEntity::getConfigKey, logo.getConfigKey())
                        .set(BaseSysConfigsEntity::getOpValue, logo.getOpValue())
                        .set(BaseSysConfigsEntity::getConfigEnable, logo.getConfigEnable())
                        .eq(BaseSysConfigsEntity::getConfigKey, logo.getConfigKey()));
            }
        }
        // 返回成功的响应
        return ResponseResult.success();
    }

    /**
     * 根据配置键查询配置值
     *
     * @param configKey 配置项的键
     * @return 查询结果的响应对象，包含配置项的值
     */
    @Override
    public String selectConfigValueByKey(String configKey) {
        try {
            // 检查配置键对应的值是否存在于Redis中
            if (redisCache != null) {
                boolean flag = redisCache.hasKey(configKey);
                if (flag) {
                    // 如果存在，直接从Redis返回配置值
                    Object cacheObj = redisCache.getCacheObject(configKey);
                    if (cacheObj != null) {
                        // 处理不同类型的返回值（FastJson序列化可能返回不同类型）
                        if (cacheObj instanceof String) {
                            return (String) cacheObj;
                        } else {
                            return cacheObj.toString();
                        }
                    }
                }
            }
            // 如果不存在，从数据库查询配置项，并将配置值存储到Redis中，然后返回配置值
            BaseSysConfigsEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                    .eq(BaseSysConfigsEntity::getConfigKey, configKey));
            if (entity == null) {
                return null;
            } else {
                String configValue = entity.getConfigValue();
                // 如果Redis可用，将配置值存储到Redis中
                if (redisCache != null && configValue != null) {
                    redisCache.setCacheObject(configKey, configValue);
                }
                return configValue;
            }
        } catch (Exception e) {
            // 如果Redis查询失败，直接查询数据库
            try {
                BaseSysConfigsEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                        .eq(BaseSysConfigsEntity::getConfigKey, configKey));
                if (entity != null) {
                    return entity.getConfigValue();
                }
            } catch (Exception dbException) {
                // 记录数据库查询异常
                System.err.println("=== 查询配置值失败: configKey=" + configKey + ", error=" + dbException.getMessage());
                dbException.printStackTrace();
            }
            return null;
        }
    }

    /**
     * 上传语言配置
     *
     * @param map 包含配置键和配置值的映射
     * @return 更新结果的响应对象
     */
    @Override
    public ResponseResult uploadLanguage(Map<String, Object> map) {
        // 检查映射中是否包含配置键
        if (!map.containsKey("configKey")) {
            // 如果不包含，返回失败的响应
            return ResponseResult.fail(BizCodeEnume.KEY_VALUE_CANNOT_BE_EMPTY);
        }
        // 更新相应的系统配置项
        this.baseMapper.update(null, new LambdaUpdateWrapper<BaseSysConfigsEntity>()
                .eq(BaseSysConfigsEntity::getConfigKey, map.get("configKey").toString())
                .set(BaseSysConfigsEntity::getConfigValue, map.get("configValue").toString()));
        // 判断是否是修改的国际化开关 如果是 级联修改 stripe支付开关  paypal开关 多语言开关
        if (CommonType.configKey.INTERNATIONALIZATION_SWITCH.getDesc().equals(map.get("configKey").toString())) {
            //stripe支付开关
            this.baseMapper.update(null, new LambdaUpdateWrapper<BaseSysConfigsEntity>()
                    .eq(BaseSysConfigsEntity::getConfigKey, CommonType.configKey.STRIPE_SWITCH.getDesc())
                    .set(BaseSysConfigsEntity::getConfigValue, map.get("configValue").toString()));
            //多语言开关
            this.baseMapper.update(null, new LambdaUpdateWrapper<BaseSysConfigsEntity>()
                    .eq(BaseSysConfigsEntity::getConfigKey, CommonType.configKey.LANGUAGE_SWITCH.getDesc())
                    .set(BaseSysConfigsEntity::getConfigValue, map.get("configValue").toString()));
            //paypal开关
            this.baseMapper.update(null, new LambdaUpdateWrapper<BaseSysConfigsEntity>()
                    .eq(BaseSysConfigsEntity::getConfigKey, CommonType.configKey.PAYPAL_SWITCH.getDesc())
                    .set(BaseSysConfigsEntity::getConfigValue, map.get("configValue").toString()));
        }
        redisCache.deleteObject(map.get("configKey").toString());
        // 返回更新成功的响应
        return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
    }

    /**
     * 根据配置键查询订单配置信息。
     * <p>
     * 该方法通过指定的配置键列表，从数据库中查询相应的订单配置信息。这些配置键包括自动发货时间、延长发货时间、售后服务时间等关键配置。
     * 方法返回这些配置信息的集合，以供前端展示或进一步处理。
     *
     * @param map 一个包含查询参数的Map对象，当前未使用但保留以支持扩展。
     * @return 返回包含查询到的订单配置信息的ResponseResult对象，如果查询成功，则状态码为200，数据部分包含配置信息集合。
     */
    @Override
    public ResponseResult selectOrderConfig(Map<String, Object> map) {
        // 定义需要查询的配置键列表
        List<String> configKeys = Arrays.asList("automatic_delivery_time", "extend_the_delivery_time", "after_sales_time");

        // 使用Lambda表达式构建查询条件，查询配置键在指定列表中的配置信息
        List<BaseSysConfigsEntity> baseSysConfigsEntities = baseMapper.selectList(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                .in(BaseSysConfigsEntity::getConfigKey, configKeys));

        // 删除相关缓存
        redisCache.deleteObject(CommonType.configKey.STRIPE_SWITCH.getDesc());
        redisCache.deleteObject(CommonType.configKey.LANGUAGE_SWITCH.getDesc());
        redisCache.deleteObject(CommonType.configKey.PAYPAL_SWITCH.getDesc());

        // 构建并返回查询成功的响应结果，包含查询到的配置信息集合
        return ResponseResult.success(baseSysConfigsEntities);
    }

    /**
     * 更新订单配置信息。
     * 该方法通过接收一个包含配置信息的Map集合，遍历集合中的每个配置项，然后更新数据库中对应配置项的值。
     * 这种设计允许批量更新配置信息，提高了配置管理的效率。
     *
     * @param map 包含待更新的配置项的Map集合。其中，每个配置项作为一个Map，包含"configKey"和"configValue"两个键值对，
     *            分别表示配置项的键和值。
     * @return 返回一个表示操作结果的ResponseResult对象。如果操作成功，返回一个包含成功标记的ResponseResult对象。
     */
    @Override
    public ResponseResult updateOrderConfig(Map<String, Object> map) {
        // 从传入的map中获取待更新的配置项列表
        List<Map<String, Object>> baseSysConfigsEntityList = (List<Map<String, Object>>) map.get("list");
        // 遍历配置项列表，对每个配置项执行更新操作
        baseSysConfigsEntityList.forEach(entityMap -> {
            // 使用Lambda表达式和MyBatis的更新包装器，构建更新语句
            // 通过配置项的键(configKey)来定位特定的配置项，并将其值(configValue)更新为新的值
            baseMapper.update(null, new LambdaUpdateWrapper<BaseSysConfigsEntity>()
                    .eq(BaseSysConfigsEntity::getConfigKey, entityMap.get("configKey"))
                    .set(BaseSysConfigsEntity::getConfigValue, entityMap.get("configValue")));
        });
        // 返回一个表示操作成功的ResponseResult对象
        return ResponseResult.success();
    }

    /**
     * 查询供应端首页滑块配置。
     *
     * @param param 查询参数，包含配置类型。
     * @return 配置列表。
     */
    @Override
    public ResponseResult querySupplyIndexSlide(Map<String, Object> param) {
        List<BaseSysConfigsEntity> slides = this.baseMapper.selectList(new LambdaQueryWrapper<BaseSysConfigsEntity>()
                .eq(BaseSysConfigsEntity::getConfigType, param.get("configType").toString())
                .orderByAsc(BaseSysConfigsEntity::getConfigSort));
        for (BaseSysConfigsEntity slide : slides) {
            // 根据操作类型优化配置值
            if (slide.getOpType().equals("2")) {
                // 优化前的代码，现注释掉
                String goodsId = String.valueOf(slide.getOpValue());
                slide.setOpValue(goodsId);
            }

            if (slide.getOpType().equals("3")) {
                String url = slide.getOpValue();
                slide.setOpValue(url);
            }

            String opValue = slide.getOpValue();
            Map map;
            // 根据配置类型和操作类型补充配置的额外信息
            if ((slide.getConfigType().equals(CommonType.configType.SUPPLY_PC_INDEX_AD.getDesc())
                    || slide.getConfigType().equals(CommonType.configType.PC_INDEX_AD.getDesc()))
                    && slide.getOpType().equals("2")) {
                map = JSON.parseObject(opValue, Map.class);
                String commonName = goodsClient.selectCommonName(Integer.valueOf(String.valueOf(map.get("commonId"))));
                slide.setCommonName(commonName);
            }
            if (slide.getConfigType().equals(CommonType.configType.INDEX_SLIDER.getDesc())
                    && slide.getOpType().equals("2")) {
                String commonName = goodsClient.selectCommonName(Integer.valueOf(slide.getOpValue()));
                slide.setCommonName(commonName);
            } else if ((slide.getConfigType().equals(CommonType.configType.SUPPLY_PC_INDEX_AD.getDesc()) || slide.getConfigType().equals(CommonType.configType.PC_INDEX_AD.getDesc())) && slide.getOpType().equals("4")) {
                map = JSON.parseObject(opValue, Map.class);
                String shopName = businessClient.selectByShopName(Integer.valueOf(String.valueOf(map.get("shopId"))));
                slide.setShopName(shopName);
            } else if ((slide.getConfigType().equals(CommonType.configType.SUPPLY_PC_INDEX_AD.getDesc()) || slide.getConfigType().equals(CommonType.configType.PC_INDEX_AD.getDesc())) && slide.getOpType().equals("6")) {
                map = JSON.parseObject(opValue, Map.class);
                String catName = goodsClient.selectByCatNameString(Integer.valueOf(String.valueOf(map.get("catId"))));
                slide.setCatName(catName);
            }
        }

        return ResponseResult.success(slides);
    }

    /**
     * 根据配置类型选择系统配置信息
     *
     * @param configKey 配置类型的键值
     * @return 返回匹配的系统配置信息对象，如果没有找到匹配项，则返回null
     */
    @Override
    public BaseSysConfigsVO selectByConfigType(String configKey) {
        BaseSysConfigsVO baseSysConfigsVO = this.baseMapper.selectByConfigTypeFeign(configKey);
        return baseSysConfigsVO;
    }

    /**
     * 插入新的系统配置信息
     *
     * @param baseSysConfigsVO 包含要插入的系统配置信息的对象
     */
    @Override
    public void insertByPrimaryConfigKey(BaseSysConfigsVO baseSysConfigsVO) {
        this.baseMapper.insertByPrimaryConfigKey(baseSysConfigsVO);
    }

    /**
     * 批量插入配置键值
     *
     * @param configKey 包含多个配置键值的列表
     * @return 返回成功插入的配置键值数量
     */
    @Override
    public int insertConfigKey(List<String> configKey) {
        int count = this.baseMapper.insertConfigKey(configKey);
        return count;
    }

    /**
     * 查询系统配置键值列表
     *
     * @param configKey 包含多个配置键值的列表，用于查询
     * @return 返回查询到的配置键值列表
     */
    @Override
    public List<String> queryConfigKey(List<String> configKey) {
        List<String> list = this.baseMapper.queryConfigKey(configKey);
        return list;
    }

    /**
     * 查询系统配置大厅信息
     *
     * @param configKey 配置类型的键值，用于查询系统配置大厅信息
     * @return 返回系统配置大厅信息列表，当前实现直接返回空列表
     */
    @Override
    public List<WabSystemConfigHallVO> querySystemConfigHall(String configKey) {
        return this.baseMapper.querySystemConfigHall(configKey);
    }

    @Override
    public void addOrUpdatePointsCashRatio(PointsCashRatioVo req) {
        BaseSysConfigsEntity config = baseSysConfigsMapper.selectByConfigKey(CommonType.configKey.POINTS_CAHS_EXCHANGE_RATIO.getDesc());
        if (BeanUtil.isEmpty(config)) {
            config = new BaseSysConfigsEntity();
            config.setConfigKey(CommonType.configKey.POINTS_CAHS_EXCHANGE_RATIO.getDesc());
            config.setConfigValue(JSONUtil.toJsonStr(req));
            baseSysConfigsMapper.insert(config);
        } else {
            config.setConfigValue(JSONUtil.toJsonStr(req));
            baseSysConfigsMapper.update(null, new UpdateWrapper<BaseSysConfigsEntity>()
                    .eq("config_key", CommonType.configKey.POINTS_CAHS_EXCHANGE_RATIO.getDesc())
                    .set("config_value", config.getConfigValue()));
        }
    }

    @Override
   public PointsCashRatioVo getPointsCashRatio() {
        BaseSysConfigsEntity config = baseSysConfigsMapper.selectByConfigKey(CommonType.configKey.POINTS_CAHS_EXCHANGE_RATIO.getDesc());
        if (BeanUtil.isEmpty(config)) {
            return null;
        }
        String configValue = config.getConfigValue();
        return JSONUtil.toBean(configValue, PointsCashRatioVo.class);
    }

    @Override
    public ResponseResult<String> getRSAPublicKey() {
        // 委托给 RSAKeyManagementService 实现
        return rsaKeyManagementService.getRSAPublicKey();
    }

    @Override
    public ResponseResult<Map<String, String>> getRSAKeys() {
        // 委托给 RSAKeyManagementService 实现
        return rsaKeyManagementService.getRSAKeys();
    }

    @Override
    public ResponseResult<String> saveRSAKeys(Map<String, String> keys) {
        // 委托给 RSAKeyManagementService 实现
        return rsaKeyManagementService.saveRSAKeys(keys);
    }

    @Override
    public ResponseResult<Map<String, String>> generateRSAKeys(int keySize) {
        // 委托给 RSAKeyManagementService 实现
        return rsaKeyManagementService.generateRSAKeys(keySize);
    }

    @Override
    public ResponseResult<Map<String, Object>> testRSAEncryption(Map<String, String> keys) {
        // 委托给 RSAKeyManagementService 实现
        return rsaKeyManagementService.testRSAEncryption(keys);
    }
}
