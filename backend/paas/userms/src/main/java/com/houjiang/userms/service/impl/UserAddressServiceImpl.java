package com.houjiang.userms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.CheckPhoneUtil;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.entity.UserAddressEntity;
import com.houjiang.userms.feign.BaseServiceClient;
import com.houjiang.userms.mapper.UserAddressMapper;
import com.houjiang.userms.service.UserAddressService;
import com.houjiang.userms.vo.UserAddressVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 用户地址服务实现类
 * 用户地址相关业务逻辑的处理
 * @author Amos
 */
@Slf4j
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddressEntity>
        implements UserAddressService {

    @Resource
    private BaseServiceClient baseServiceClient;

    /**
     * 获取默认地址或第一条地址
     * @param paraMap 包含token的Map
     * @return 返回地址信息或失败结果
     */
    @Override
    public ResponseResult getDef(Map<String, String> paraMap) {
        if(StringUtils.isBlank(paraMap.get("token"))){
            return ResponseResult.fail(0, "没有地址");
        }
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));
        // 设置userId用于查询地址
        paraMap.put("userId", userInfo.getUserId());
        // 尝试获取默认地址
        UserAddressVO userAddress = this.baseMapper.getDefAddresses(paraMap);
        if (userAddress != null) {
            return ResponseResult.success(userAddress);
        }
        // 如果默认地址不存在，尝试获取第一条地址
        List<UserAddressVO> userAddressList = this.baseMapper.getAddresses(paraMap);
        if (userAddressList != null && !userAddressList.isEmpty()) {
            return ResponseResult.success(userAddressList.get(0));
        }
        // 如果地址列表为空，返回失败结果
        return ResponseResult.fail(0, "没有地址");
    }

    /**
     * 获取用户地址列表
     * @param paraMap 包含token的Map
     * @return 返回地址列表或失败结果
     */
    @Override
    public ResponseResult get(Map<String, String> paraMap) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));
        if (userInfo == null) {
            // 如果用户信息为空，返回失败结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        Integer internationalType = null;
        // 判断国际站是否开启
        String s = baseServiceClient.selectConfigValueFeign(CommonType.configKey.INTERNATIONALIZATION_SWITCH.getDesc());
        if("0".equals(s)){
            //国际站关闭 不查询国际收货地址
            //国际地址 1 是 2 否
            internationalType = 2;
        }
        // 根据用户ID查询地址列表
        List<UserAddressEntity> userAddressList = this.baseMapper.selectList(new LambdaQueryWrapper<UserAddressEntity>()
                .eq(UserAddressEntity::getUserId, userInfo.getUserId())
                .eq(null != internationalType, UserAddressEntity::getInternationalType, internationalType)
                .orderByDesc(UserAddressEntity::getUserAddressDefault, UserAddressEntity::getUserAddressTime));
        if (userAddressList != null) {
            // 如果地址列表不为空，返回成功结果
            return ResponseResult.success(userAddressList);
        }
        // 如果地址列表为空，返回失败结果
        return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED);
    }

    /**
     * 更新用户地址信息
     * @param paraMap 包含地址信息和token的Map
     * @return 返回操作结果
     */
    @Override
    public ResponseResult update(Map<String, String> paraMap) {
        // 从Redis中获取用户ID
        String token = paraMap.get("token");
        String userId = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USERID + token);
        paraMap.put("userId", userId);
        if (paraMap.get("userAddressProvinceId") == null || "".equals(paraMap.get("userAddressProvinceId")) &&
                paraMap.get("userAddressCityId") == null || "".equals(paraMap.get("userAddressCityId")) &&
                paraMap.get("userAddressAreaId") == null || "".equals(paraMap.get("userAddressAreaId"))) {
            paraMap.put("userAddressProvinceId", "0");
            paraMap.put("userAddressCityId", "0");
            paraMap.put("userAddressAreaId", "0");
        }
        // 设置默认地址逻辑
        if (Integer.parseInt(paraMap.get("userAddressDefault")) == 1) {
            this.baseMapper.update(null, new LambdaUpdateWrapper<UserAddressEntity>()
                    .eq(UserAddressEntity::getUserId, userId)
                    .eq(UserAddressEntity::getUserAddressDefault, CommonType.commonStatus.YES.getCode())
                    .set(UserAddressEntity::getUserAddressDefault, CommonType.commonStatus.NO.getCode()));
        }

        // 更新地址信息
        Integer torf = this.baseMapper.updateAddress(paraMap);
        if (CommonStatusEnum.SUCCESS.getCode() == torf) {
            return ResponseResult.success();
        }
        return ResponseResult.fail();
    }

    /**
     * 删除用户地址
     * @param paraMap 包含地址ID和token的Map
     * @return 返回操作结果
     */
    @Override
    public ResponseResult delete(Map<String, String> paraMap) {
        // 从Redis中获取用户ID
        String token = paraMap.get("token");
        String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
        if (userInfoStr == null || userInfoStr.isEmpty()) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        UserInfoDTO ui = JSON.parseObject(userInfoStr, UserInfoDTO.class);
        UserInfoVO userInfo = ui.getUser();
        String userId = userInfo.getUserId();
        paraMap.put("userId", userId);
        // 检查地址ID是否存在
        if (paraMap.get("userAddressId") == null) {
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER.getCode(), BizCodeEnume.MISSING_PARAMETER.getMsg());
        }
        // 根据用户ID和地址ID删除地址
        Integer torf = this.baseMapper.delete(new LambdaUpdateWrapper<UserAddressEntity>()
                .eq(UserAddressEntity::getUserId, userId)
                .eq(UserAddressEntity::getUserAddressId, paraMap.get("userAddressId")));
        if (CommonStatusEnum.SUCCESS.getCode() == torf) {
            return ResponseResult.success();
        }
        return ResponseResult.fail();
    }

    /**
     * 添加用户地址
     * @param paraMap 包含地址信息和token的Map
     * @return 返回操作结果
     */
    @Override
    public ResponseResult add(Map<String, String> paraMap) {
        // 从Redis中获取用户信息
        String token = paraMap.get("token");
        String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
        if (userInfoStr == null || userInfoStr.isEmpty()) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        UserInfoDTO ui = JSON.parseObject(userInfoStr, UserInfoDTO.class);
        UserInfoVO userInfo = ui.getUser();
        String userId = userInfo.getUserId();
        if (paraMap.get("userAddressProvinceName") != null && !"".equals(paraMap.get("userAddressProvinceName")) &&
                paraMap.get("userAddressCityName") != null && !"".equals(paraMap.get("userAddressCityName")) &&
                paraMap.get("userAddressAreaName") != null && !"".equals(paraMap.get("userAddressAreaName"))) {
            Integer addressProvinceId = baseServiceClient.getAddressIdByName(paraMap.get("userAddressProvinceName"),1);
            Integer addressCityId = baseServiceClient.getAddressIdByName(paraMap.get("userAddressCityName"),2);
            Integer addressAreaId = baseServiceClient.getAddressIdByName(paraMap.get("userAddressAreaName"),3);
            paraMap.put("userAddressProvinceId", addressProvinceId.toString());
            paraMap.put("userAddressCityId", addressCityId.toString());
            paraMap.put("userAddressAreaId", addressAreaId.toString());
        }
        if (paraMap.get("userAddressProvinceId") == null || "".equals(paraMap.get("userAddressProvinceId")) &&
                paraMap.get("userAddressCityId") == null || "".equals(paraMap.get("userAddressCityId")) &&
                paraMap.get("userAddressAreaId") == null || "".equals(paraMap.get("userAddressAreaId"))) {
            paraMap.put("userAddressProvinceId", "0");
            paraMap.put("userAddressCityId", "0");
            paraMap.put("userAddressAreaId", "0");
        }
        Integer userAddressDefault = 0;
        if (paraMap.get("userAddressDefault") != null && !paraMap.get("userAddressDefault").isEmpty()) {
            userAddressDefault = Integer.parseInt(paraMap.get("userAddressDefault"));
        }
        // 设置默认地址逻辑
        if (CommonType.commonStatus.YES.getCode().equals(userAddressDefault)) {
            this.baseMapper.update(null, new LambdaUpdateWrapper<UserAddressEntity>()
                    .eq(UserAddressEntity::getUserId, userId)
                    .eq(UserAddressEntity::getUserAddressDefault, CommonType.commonStatus.YES.getCode())
                    .set(UserAddressEntity::getUserAddressDefault, CommonType.commonStatus.NO.getCode()));
        }
        // 设置用户ID和默认地址值
        paraMap.put("userId", userId);
        paraMap.put("userAddressDefault", userAddressDefault.toString());
        // 添加地址信息
        Integer torf = this.baseMapper.addAddress(paraMap);
        if (CommonStatusEnum.SUCCESS.getCode() == torf) {
            return ResponseResult.success();
        }
        return ResponseResult.fail();
    }

    /**
     * 设置默认收货地址
     *
     * @param paraMap 参数映射表，包含token和userAddressId
     * @return 返回操作结果，成功或失败
     *
     * 本方法首先通过token获取用户ID，然后更新用户的所有非默认收货地址的默认状态为0，
     * 接着将指定的收货地址ID设为默认（默认状态为1）。如果操作成功，返回成功结果，
     * 否则返回失败结果。
     */
    @Override
    public ResponseResult setDefault(Map<String, String> paraMap) {
        // 从参数映射中获取token
        String token = paraMap.get("token");
        String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
        if (userInfoStr == null || userInfoStr.isEmpty()) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        UserInfoDTO ui = JSON.parseObject(userInfoStr, UserInfoDTO.class);
        UserInfoVO userInfo = ui.getUser();
        String userId = userInfo.getUserId();
        // 更新所有属于该用户且默认状态为1的收货地址的默认状态为0
        Integer oneInteger =this.baseMapper.update(null,new LambdaUpdateWrapper<UserAddressEntity>()
                .set(UserAddressEntity::getUserAddressDefault,0)
                .eq(UserAddressEntity::getUserId,userId)
                .eq(UserAddressEntity::getUserAddressDefault,1));
        // 将指定的收货地址ID设为默认（默认状态为1）
        Integer twoInteger = this.baseMapper.update(null,new LambdaUpdateWrapper<UserAddressEntity>()
                .set(UserAddressEntity::getUserAddressDefault,1)
                .eq(UserAddressEntity::getUserId,userId)
                .eq(UserAddressEntity::getUserAddressId,paraMap.get("userAddressId").toString()));

        // 检查操作是否成功：所有非默认地址被正确更新，且指定地址被设为默认
        if ((oneInteger == 1 ||  oneInteger == 0) && twoInteger == 1) {
            return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
        }
        // 如果操作失败，返回相应的错误信息
        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
    }
}
