package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.userms.BusinessUserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.entity.UserBusinessInfoEntity;
import com.houjiang.userms.feign.BaseServiceClient;
import com.houjiang.userms.mapper.UserBusinessInfoMapper;
import com.houjiang.userms.service.UserBusinessInfoService;
import com.houjiang.userms.service.UserInfoService;
import com.houjiang.userms.vo.BusinessUserInfoVO;
import com.houjiang.userms.vo.BusinessUserListVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户业务信息服务实现类
 * @author Amos
 */
@Service
public class UserBusinessInfoServiceImpl extends ServiceImpl<UserBusinessInfoMapper, UserBusinessInfoEntity>
implements UserBusinessInfoService{

    private final static Logger log = LoggerFactory.getLogger(UserBusinessInfoServiceImpl.class);

    @Autowired
    private UserBusinessInfoMapper userBusinessInfoMapper;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private BaseServiceClient baseServiceClient;

    /**
     * 根据条件查询用户企业认证信息
     * @param map 查询条件
     * @return 用户企业认证信息
     */
    @Override
    public ResponseResult isPassedEnt(Map<String, String> map) {
        // 初始化查询条件
        QueryWrapper<UserBusinessInfoEntity> queryWrapper = new QueryWrapper<>();

        // 如果map不为空且包含userId，添加userId作为查询条件
        if (null != map && null != map.get("userId")) {
            queryWrapper.eq("user_id", map.get("userId"));
        }

        // 如果map不为空且包含isApproved，且其值为1，添加审批状态作为查询条件
        if (null != map && null != map.get("isApproved")) {
            queryWrapper.eq("is_approved", "1");
        }

        // 根据构建的查询条件查询用户业务信息
        UserBusinessInfoEntity businessInfoEntity = this.baseMapper.selectOne(queryWrapper);

        // 如果查询结果不为空，返回成功的响应结果，其中包含查询到的业务信息
        if(businessInfoEntity!=null){
            return ResponseResult.success(businessInfoEntity);
        }

        // 如果查询结果为空，返回失败的响应结果，指示未获取到信息
        return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED.getMsg());

    }

    /**
     * 通过Feign查询用户是否为企业
     * @param userId 用户ID
     * @return 是否为企业
     */
    @Override
    public boolean isBusinessFegin(String userId) {
        /**
         * 根据用户ID查询用户业务信息实体，并检查是否已批准。
         *
         * @param userId 用户ID，用于查询特定用户的业务信息。
         * @return 如果用户已批准，则返回true；否则返回false。
         */
        UserBusinessInfoEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<UserBusinessInfoEntity>()
                        .eq(UserBusinessInfoEntity::getUserId, userId));
                if (entity != null) {
                    return entity.getIsApproved() == 1;
                }
                return false;
    }

    /**
     * 根据token验证用户是否为企业用户
     * @param param 查询参数，包含token
     * @return 验证结果
     */
    @Override
    public ResponseResult isBusiness(Map<String, Object> param) {
        // 根据传入的参数验证token的有效性，并返回相应的用户业务信息。
        try {
            // 检查token参数是否为空，如果为空，则返回错误信息。
            if (null == param.get("token")) {
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_NULL);
            }
            // 获取token字符串。
            String token = (String) param.get("token");
            // 检查token在Redis中是否存在，如果存在，则继续验证。
            boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
            if (isExist) {
                // 通过token获取用户信息。
                UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
                // 根据用户ID和审批状态查询用户业务信息。
                UserBusinessInfoEntity entity=userBusinessInfoMapper.selectOne(new LambdaQueryWrapper<UserBusinessInfoEntity>()
                                              .eq(UserBusinessInfoEntity::getUserId,ui.getUserId())
                                              .eq(UserBusinessInfoEntity::getIsApproved,CommonType.commonStatus.YES.getCode()));
                // 如果查询到用户业务信息，则返回成功结果，否则返回不是企业用户的错误信息。
                if(entity!=null){
                    return ResponseResult.success(entity);
                }
                return ResponseResult.fail(0, "不是企业用户");
            } else {
                // 如果token不存在于Redis中，则返回无效token的错误信息。
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
            }
        } catch (Exception e) {
            // 捕获异常，打印异常信息，并返回操作失败的错误信息。
            e.printStackTrace();
            log.error("查询失败", e);
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);

        }

    }

    /**
     * 根据token查询用户企业信息
     * @param param 查询参数，包含token
     * @return 用户企业信息
     */
    @Override
    public ResponseResult queryInfo(Map<String, Object> param) {
        // 初始化响应结果对象
        ResponseResult result = new ResponseResult();
        try {
            // 检查参数中是否包含token，如果不包含，则返回失败结果
            if (null == param.get("token")) {
                result.setCode(CommonStatusEnum.FAIL.getCode());
                result.setMessage(BizCodeEnume.TOKEN_IS_NULL.getMsg());
                return result;
            }
            // 获取token
            String token = (String) param.get("token");
            // 根据token解析用户信息
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
            // 如果解析成功，获取用户ID
            String userId;
            if (ui != null) {
                // 发送人id
                userId = ui.getUserId();
            } else {
                // 如果token无效，返回失败结果
                result.setCode(BizCodeEnume.TOKEN_IS_INVALID.getCode());
                result.setMessage(BizCodeEnume.TOKEN_IS_INVALID.getMsg());
                return result;
            }
            // 将用户ID添加到参数中
            param.put("userId", userId);
            // 根据参数查询业务用户信息
            BusinessUserInfoVO detail = userBusinessInfoMapper.queryInfo(param);
            // 组合地址信息
            String address = getAddress(detail.getCompanyProvinceId(), detail.getCompanyCityId(), detail.getCompanyDistrictId());
            address = address + detail.getCompanyDetailAddress();
            detail.setCompanyAddress(address);
            // 设置成功响应结果
            result.setCode(CommonStatusEnum.SUCCESS.getCode());
            result.setData(detail);
            result.setMessage(BizCodeEnume.OPERATION_SUCCESSFUL.getMsg());
        } catch (Exception e) {
            // 记录异常信息
            e.printStackTrace();
            log.error("查询失败", e);
            // 设置失败响应结果
            result.setCode(CommonStatusEnum.FAIL.getCode());
            result.setMessage(BizCodeEnume.OPERATION_FAILED.getMsg());
        }

        // 返回响应结果
        return result;


    }

    /**
     * 根据省、市、区ID获取地址
     * @param provinceId 省ID
     * @param cityId 市ID
     * @param districtId 区ID
     * @return 地址
     */
    private String getAddress(String provinceId, String cityId, String districtId) {
        // 检查省、市、区ID是否均为空，如果都为空，则直接返回空字符串
        if ((provinceId == null || provinceId.equals("")|| provinceId.equals("null"))
                && (cityId == null || cityId.equals("") || cityId.equals("null"))
                && (districtId == null || districtId.equals("") || districtId.equals("null"))) {
            return "";
        }

        // 根据省ID查询省名
        String provinceName = baseServiceClient.queryProvinceport(provinceId);
        // 根据市ID查询市名
        String cityName = baseServiceClient.queryProvinceport(cityId);
        // 根据区ID查询区名
        String districtName = baseServiceClient.queryProvinceport(districtId);

        // 返回省、市、区名的组合字符串
        return provinceName + cityName + districtName;

    }

    /**
     * 根据用户ID查询企业信息
     * @param userId 用户ID
     * @return 企业信息
     */
    @Override
    public ResponseResult queryBusiness(String userId) {
        // 初始化参数映射表
        Map<String, Object> param = new HashMap<>();

        try {
            // 尝试根据用户ID查询用户业务信息
            UserBusinessInfoEntity businessDetail = this.baseMapper.selectOne(new LambdaQueryWrapper<UserBusinessInfoEntity>()
                    .eq(UserBusinessInfoEntity::getUserId, userId));

            // 如果查询结果为空，则尝试根据用户创建者ID查询业务信息
            if (businessDetail == null) {
                String createUser = userInfoService.getById(userId).getCreateUser();

                businessDetail = this.baseMapper.selectOne(new LambdaQueryWrapper<UserBusinessInfoEntity>()
                        .eq(UserBusinessInfoEntity::getUserId, createUser));
            }

            // 将查询到的业务信息转换为Map格式
            param = ParseMapUtils.beanToMap(businessDetail);
        } catch (Exception e) {
            // 如果查询过程中出现异常，返回失败的响应结果，并包含异常信息
            return ResponseResult.fail(e.getMessage());
        }

        // 返回成功包含查询结果的响应结果
        return ResponseResult.success(param);
    }

    /**
     * 更新业务信息。
     * 通过传入的映射对象更新用户业务信息。首先，从映射对象中解析出用户令牌，进而获取用户信息。
     * 如果映射对象中包含用户ID，则使用之，否则使用解析出的用户ID。然后，将映射对象转换为用户业务信息实体。
     * 根据用户ID查询现有的用户业务信息，如果存在，则更新该信息；如果不存在，则根据业务逻辑处理。
     * 此方法特别关注电话号码的格式，如果格式不正确，则返回错误结果。
     *
     * @param map 包含更新信息的映射对象，其中必须包含令牌和用户ID。
     * @return 如果更新成功，则返回成功的响应结果，包含更新后的实体；如果更新失败，则返回失败的响应结果。
     */
    @Override
    public ResponseResult updateBusinessInfo(Map<String, Object> map) {
        // 从令牌中解析出用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());
        // 获取用户ID，优先使用map中传递的ID，若不存在则使用解析出的用户ID
        String userId=map.get("userId")==null?userInfo.getUserId():map.get("userId").toString();
        // 将映射对象转换为用户业务信息实体
        UserBusinessInfoEntity businessInfoEntity = ParseMapUtils.parseMap2Object(map, UserBusinessInfoEntity.class);
        // 根据用户ID查询现有的用户业务信息
        UserBusinessInfoEntity entity=this.baseMapper.selectOne(new LambdaQueryWrapper<UserBusinessInfoEntity>()
                                                      .eq(UserBusinessInfoEntity::getUserId, userId));
        // 设置实体ID，准备更新
        businessInfoEntity.setId(entity.getId());
        // 更新用户业务信息，并判断更新是否成功
        boolean flag;
        flag = this.baseMapper.updateById(businessInfoEntity)>0;
        // 根据更新结果返回相应的响应结果
        if (flag) {
            return ResponseResult.success(businessInfoEntity);
        } else {
            return ResponseResult.fail(BizCodeEnume.REGISTRATION_FAIL);
        }
    }

    /**
     * 查询业务用户列表。
     *
     * @param param 查询参数，以Map形式传递，包含分页信息和过滤条件。
     * @return 返回查询结果，包含分页信息和查询到的业务用户列表。
     */
    @Override
    public ResponseResult queryList(Map<String, Object> param) {
        // 根据查询参数获取分页信息，并结合查询条件调用底层Mapper查询业务用户列表
        IPage<BusinessUserListVO> page = this.baseMapper.queryList(new Query<BusinessUserListVO>().getPage(param), param);
        // 将查询结果包装成响应结果返回
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 注册业务用户。
     *
     * @param map 注册信息，以Map形式传递，包含业务用户的所有必要字段。
     * @return 根据注册结果返回相应的响应结果，成功则返回注册的业务用户信息，失败则返回错误信息。
     */
    @Override
    public ResponseResult registerBusiness(Map<String, Object> map) {
        try {
            // 将Map中的数据转换为UserBusinessInfoEntity对象，为数据库操作做准备
            UserBusinessInfoEntity businessInfoEntity = ParseMapUtils.parseMap2Object(map, UserBusinessInfoEntity.class);
            // 将业务用户信息插入数据库，并根据操作结果返回相应的响应结果
            boolean flag = this.baseMapper.insertBusinessUser(map);

            // 发送消息模板
            Map<String, Object> messageMap = new HashMap<>();
            List<String> messageList = new ArrayList<>();
            messageList.add(businessInfoEntity.getCompanyName());
            messageMap.put("list", messageList);
            messageMap.put("businessName", businessInfoEntity.getCompanyName());
            messageMap.put("templateId", CommonType.messageTemplate.CODE_K.getCode());
            baseServiceClient.sendMessageTemplate(messageMap);
            if (flag) {
                return ResponseResult.success(businessInfoEntity);
            } else {
                return ResponseResult.fail(BizCodeEnume.REGISTRATION_FAIL);
            }
        } catch (Exception e) {
            // 注册过程中发生异常，返回失败响应并附带异常信息
            return ResponseResult.fail(e.getMessage());
        }
    }
    /**
     * 根据参数查询企业用户数量，检查用户名是否已存在。
     *
     * @param paraMap 查询参数映射表，包含用户名等信息。
     * @return 如果用户存在，则返回失败结果；否则返回成功结果。
     */
    @Override
    public ResponseResult selectBusinessUser(Map<String, String> paraMap) {
        // 查询企业用户数量
        Integer num = this.baseMapper.selectBusinessUser(paraMap);
        // 如果数量大于0，说明用户名已存在
        if (num > 0) {
            return ResponseResult.fail(0, "该名称已存在", 1);
        }
        // 用户名不存在，返回成功结果
        return ResponseResult.success("名称不存在", 0);
    }

    @Override
    public BusinessUserInfoDTO queryInfoFeign(Map<String, Object> param) {
        BusinessUserInfoDTO businessUserInfoDTO = this.baseMapper.queryInfoFeign(param);
        return businessUserInfoDTO;
    }

}
