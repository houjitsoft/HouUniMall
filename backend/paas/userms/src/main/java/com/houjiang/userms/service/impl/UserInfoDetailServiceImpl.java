package com.houjiang.userms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.shop.ShopPersonInfoVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.entity.UserInfoDetaileEntity;
import com.houjiang.userms.mapper.UserInfoDetailMapper;
import com.houjiang.userms.service.UserInfoDetailService;
import com.houjiang.userms.service.UserVkService;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 用户信息详情服务实现类
 * @author Amos
 */
@Service
public class UserInfoDetailServiceImpl extends ServiceImpl<UserInfoDetailMapper, UserInfoDetaileEntity>
    implements UserInfoDetailService{

    @Autowired
    private UserVkService userVkService;

    /**
     * 用户分组统计
     * @param map 包含token的Map
     * @return 统计结果的ResponseResult
     */
    @Override
    public ResponseResult userGroupingStatistics(Map<String, Object> map) {
        // 从map中获取token字符串
        String token = (String) map.get("token");
        // 检查Redis中是否存在该token，判断用户登录状态
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_PLATFORM_TOKEN_USER + token);
        if (isExistToken) {
            try {
                // 查询用户的商品分组信息
                List<Map<String,Object>> goodsGroupingList = this.baseMapper.queryUserGrouping();
                // 如果查询结果不为空，返回成功响应并带上查询数据
                if (!goodsGroupingList.isEmpty()) {
                   return ResponseResult.success(goodsGroupingList);
                } else {
                    // 如果查询结果为空，返回失败响应，表示未获取到信息
                    return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED);
                }
            } catch (Exception e) {
                // 查询过程中发生异常，返回失败响应并附带异常信息
                return ResponseResult.fail( e.getMessage());
            }
        } else {
            // token不存在，返回失败响应，表示token无效
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }

    }

    /**
     * 更新用户头像URL
     * @param updateUserParam 包含更新信息的Map
     */
    @Override
    public void updateUserAtarUrl(Map<String, Object> updateUserParam) {
        /**
         * 更新用户头像URL。
         * 调用此方法是为了通过传递UpdateUserParam参数来更新用户信息中的头像URL。
         * 这里的this.baseMapper是一个继承自BaseMapper的Mapper接口实例，它提供了CRUD操作的基本方法。
         * updateUserInfo方法封装了更新用户头像URL的具体逻辑，通过调用this.baseMapper.updateUserAtarUrl方法来实现。
         *
         * @param updateUserParam 包含用户头像URL更新信息的参数对象。
         */
        this.baseMapper.updateUserAtarUrl(updateUserParam);

    }

    /**
     * 获取用户安全等级
     * @param paraMap 包含token的Map
     * @return 用户安全等级的ResponseResult
     */
    @Override
    public ResponseResult getUserSecurityLevel(Map<String, String> paraMap) {
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));
        if (null == userInfo){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        Integer gradeId = 0;
        Map<String, Object> map = new HashedMap();
        // 查询用户是否实名认证
        Integer shiming = this.baseMapper.queryShiming(userInfo.getUserId());
        if (0 != shiming) {
            gradeId++;
            map.put("shiMing", true);
        } else {
            map.put("shiMing", false);
        }
        //查询用户是否设置登录密码
        Integer loginPassword = this.baseMapper.queryLoginPassword(userInfo.getUserId());
        if(0 != loginPassword){
            gradeId++;
            map.put("loginPassword",true);
        }else{
            map.put("loginPassword",false);
        }
        //查询用户是否设置支付密码
        Integer payPassword = this.baseMapper.queryPayPassword(userInfo.getUserId());
        if (0 != payPassword) {
            gradeId++;
            map.put("payPassword", true);
        } else {
            map.put("payPassword", false);
        }
        //查询用户手机号
        map.put("userMobile",userInfo.getUserMobile());
        map.put("gradeId",gradeId == 0 ? 1 : gradeId);
        return ResponseResult.success(map);
    }

    /**
     * 删除用户信息
     * @param userId 用户ID
     * @return 删除结果的ResponseResult
     */
    @Override
    public ResponseResult delUserInfo(String userId) {
        /**
         * 删除用户信息的操作步骤：
         * 此部分代码负责删除与用户相关的三个实体：用户信息、用户详情和用户连接信息。
         * 每个操作都通过调用baseMapper的相应delete方法来执行。
         * 删除的顺序按照用户信息、用户详情、用户连接信息的逻辑进行，旨在彻底清除用户数据。
         *
         * @param userId 用户ID，用于指定要删除的用户数据。
         */
        this.baseMapper.deleteUserInfo(userId);

        /**
         * 继续删除用户详细信息。
         * 此步骤是删除用户数据的第二步，目的是确保与用户相关的所有详细信息都被清除。
         * 使用相同的userId参数来定位并删除特定用户的详细信息记录。
         *
         * @param userId 用户ID，用于指定要删除的用户详细信息。
         */
        this.baseMapper.deleteUserDetail(userId);

        /**
         * 最后，删除用户连接信息。
         * 这一步是删除用户数据的最后一步，它处理与用户账户连接相关的信息，如第三方账户绑定。
         * 通过删除用户的连接信息，可以确保用户在系统中的所有痕迹都被彻底清除。
         *
         * @param userId 用户ID，用于指定要删除的用户连接信息。
         */
        this.baseMapper.deleteUserConnect(userId);

        //删除微客上下级信息
        userVkService.delUserVk(userId);

        /**
         * 删除VIP用户的信息。
         *
         * 本方法通过调用BaseMapper的deleteVkUser方法，删除指定VIP用户的信息。
         * 这是清除VIP用户相关数据的最后一步，确保所有与该VIP用户相关的信息都被彻底删除。
         *
         * @param userId 用户ID，用于指定要删除的VIP用户的信息。
         */
        this.baseMapper.deleteVkUser(userId);

        /**
         * 返回操作成功的结果。
         *
         * 本方法返回一个表示操作成功的ResponseResult对象，用于向调用方指示删除VIP用户及相关数据的操作已经成功完成。
         *
         * @return 返回一个表示操作成功的ResponseResult对象。
         */
        return ResponseResult.success();

    }

    /**
     * 更新用户昵称
     * @param paraMap 包含更新信息的Map，包括token和新的昵称
     * @return 更新结果的ResponseResult
     */
    @Override
    public ResponseResult updateUserNickname(Map<String, String> paraMap) {
        // 从参数映射中获取token
        String token = paraMap.get("token");
        // 从Redis中根据token获取用户信息的JSON字符串
        String json = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
        // 如果用户信息为空或为空字符串，则返回令牌无效的响应
        if (null == json || "".equals(json)) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 解析token获取用户信息，不抛出异常时说明token有效
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(token);
        // 将用户ID添加到参数映射中，为后续更新操作准备
        paraMap.put("userId", userInfo.getUserId());
        // 调用更新性别方法，并返回更新结果
        Integer torf = this.updateGender(paraMap);
        // 根据更新结果判断，如果更新成功（返回1），则进行后续操作
        if (1 == torf) {
            // 获取用户的手机号
            String mobilePhone = userInfo.getUserMobile();
            // 根据手机号查询最新的用户信息
            UserInfoVO newUserInfo = this.baseMapper.getUserInfoByPhoneNumber(mobilePhone);
            // 获取用户的openid
            String bindOpenid = userInfo.getBindOpenid();
            // 如果openid不为空，尝试根据openid获取用户的昵称和头像信息
            if (StringUtils.isNotEmpty(bindOpenid)) {
                // 根据openid获取用户昵称
                Map<String, String> outMap = this.baseMapper.selectInfo(bindOpenid);
                // 如果获取到头像信息，更新用户信息中的头像字段
                if (StringUtils.isNotEmpty(outMap.get("bind_avator"))) {
                    newUserInfo.setUserAvatar(outMap.get("bind_avator"));
                }
            }
            // 查询用户的店铺状态，用于更新用户信息中的店铺状态字段
            Integer status = this.baseMapper.queryShopStatus(userInfo.getUserId());
            // 根据店铺状态设置用户信息中的店铺状态字段
            if (status > 0) {
                newUserInfo.setIsShop(1);
            } else {
                newUserInfo.setIsShop(2);
            }
            // 尝试从Redis中获取更新后的用户信息JSON字符串
            try {
                String ui = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
                // 将JSON字符串反序列化为UserInfoDTO对象
                UserInfoDTO dto = JSON.parseObject(ui, UserInfoDTO.class);
                // 更新用户信息对象中的用户字段
                dto.setUser(newUserInfo);
                // 将更新后的用户信息对象序列化为JSON字符串，并设置回Redis
                RedisUtil.set(HouConfig.HOU_USER_TOKEN_USER + token, JSON.toJSONString(dto),
                        HouConfig.TOKEN_OUTTIME);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 返回更新成功的响应
            return ResponseResult.success(1, "更新成功");
        }
        // 如果更新失败，则返回相应的失败响应
        return ResponseResult.fail(2, "更新失败");

    }

    /**
     * 更新用户性别或昵称
     * @param paraMap 包含更新信息的Map
     * @return 更新结果
     */
    public Integer updateGender(Map<String, String> paraMap) {
        // 初始化更新标志为1，表示默认更新操作成功
        Integer torf = 1;

        // 检查用户性别信息是否存在，如果存在，则更新用户性别
        if (paraMap.get("userGender") != null) {
            torf = this.baseMapper.updateGender(paraMap);
        }

        // 检查用户昵称信息是否存在，如果存在，则更新用户昵称及关联信息
        if (paraMap.get("userNickname") != null) {
            torf = this.baseMapper.updateUserName(paraMap);
            this.baseMapper.updateUserBindConnect2(paraMap);
        }

        // 返回更新操作的结果
        return torf;
    }

    /**
     * 根据请求参数查询店铺个人信息。
     * @param map 包含查询参数的映射。
     * @return 店铺个人信息的结果。
     */
    @Override
    public ResponseResult queryShopPersonInfo(Map<String, Object> map) {
        ShopPersonInfoVO personInfo = this.baseMapper.queryShopPersonInfo(map);
        return ResponseResult.success(personInfo);
    }

    @Override
    public Map<String, Object> queryShopPersonInfoFeign(Map<String, Object> param) {
        Map<String, Object> result = this.baseMapper.queryShopPersonInfoFeign(param);
        return result;
    }
}
