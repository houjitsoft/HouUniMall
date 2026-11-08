package com.houjiang.userms.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.entity.UserBindConnectEntity;
import com.houjiang.userms.mapper.UserBindConnectMapper;
import com.houjiang.userms.service.UserBindConnectService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户绑定关联服务实现类
 * <p>
 * 该类继承自ServiceImpl，实现了UserBindConnectService接口，
 * 提供了针对用户绑定关联信息的增删改查等操作。
 * @author Amos
 */
@Service
public class UserBindConnectServiceImpl extends ServiceImpl<UserBindConnectMapper, UserBindConnectEntity>
    implements UserBindConnectService{

    /**
     * 根据openid查询用户ID
     *
     * @param bindOpenid 用户绑定的openid
     * @return 用户ID
     */
    @Override
    public String selectUserIdByOpenid(String bindOpenid) {
        return this.baseMapper.selectUserIdByOpenid(bindOpenid);
    }

    /**
     * 根据绑定ID查询用户信息
     *
     * @param bindId 用户的绑定ID
     * @return 用户信息VO
     */
    @Override
    public UserInfoVO getUserInfoByUserId(String bindId) {
        return this.baseMapper.getUserInfoByUserId(bindId);
    }

    /**
     * 更新自动登录状态
     *
     * @param paramMap 包含更新信息的参数映射
     * @return 更新操作的结果
     */
    @Override
    public ResponseResult updateAotuLogin(Map<String, Object> paramMap) {
        if(null != paramMap && null != paramMap.get("unionid")){
            UserBindConnectEntity entity = baseMapper.selectOne(new LambdaQueryWrapper<UserBindConnectEntity>()
                    .eq(UserBindConnectEntity::getBindUnionid, paramMap.get("unionid")));
            if (entity != null) {
                paramMap.put("officialAccountOpenid", entity.getOfficialAccountOpenid());
                if(entity.getBindId().contains("official_account_")){
                    baseMapper.deleteById(entity.getBindId());
                }
            }
        }
        this.baseMapper.updateAotuLogin(paramMap);
        return ResponseResult.success();
    }

    /**
     * 插入用户三方绑定关系
     *
     * @param userThreeBindMap 包含用户三方绑定信息的参数映射
     * @return 插入操作的结果
     */
    @Override
    public ResponseResult insertUserThreeBindMap(Map<String, Object> userThreeBindMap) {
        String threebindId = this.baseMapper.selectUserIdByBindId(userThreeBindMap.get("bindId").toString());

        if (null == threebindId) {
            this.baseMapper.insertUserThreeBindMap(userThreeBindMap);
        }
        return ResponseResult.success();
    }

    /**
     * 更新用户昵称和头像URL
     *
     * @param updateUserParam 包含更新昵称和头像URL信息的参数映射
     */
    @Override
    public void updateUserNickNameAndAvatarUrl(Map<String, Object> updateUserParam) {
        /**
         * 更新用户昵称和头像URL。
         *
         * @param updateUserParam 包含用户昵称和头像URL更新信息的参数对象。
         * @return 更新操作的结果，例如更新的行数。
         */
        this.baseMapper.updateUserNickNameAndAvatarUrl(updateUserParam);

    }

    /**
     * 查询用户的自动登录状态
     *
     * @param openId 用户的openid
     * @return 自动登录状态
     */
    @Override
    public Integer queryAutoLogin(String openId) {
        // 调用BaseMapper的queryAutoLogin方法，根据OpenID查询自动登录信息
        return this.baseMapper.queryAutoLogin(openId);
    }
    /**
     * 查询用户的OpenID信息
     * 该方法用于根据用户ID查询相关的OpenID信息，包括小程序OpenID、UnionID以及公众号OpenID
     * 主要涉及用户信息表和用户绑定关系表的查询
     *
     * @param userId 用户ID，用于定位特定用户的信息
     * @return 包含OpenID信息的Map，包括小程序OpenID、UnionID和公众号OpenID如果未找到绑定用户，则返回null
     */
    @Override
    public Map<String, String> queryUserOpenId(String userId) {
        // 初始化Map，用于存储OpenID信息
        Map<String, String> map = new HashMap<>(3);

        // 根据用户ID和绑定类型查询绑定信息此处绑定类型为7，可能代表某种特定的OpenID绑定
        UserBindConnectEntity bindUser = baseMapper.selectOne(new LambdaQueryWrapper<UserBindConnectEntity>()
                .eq(UserBindConnectEntity::getUserId, userId)
                .eq(UserBindConnectEntity::getBindType, 2));

        // 如果未找到绑定用户，则直接返回null
        if (bindUser == null) {
            return null;
        }

        // 将查询到的OpenID信息存入Map中
        map.put("openId", bindUser.getBindOpenid());
        map.put("unionId", bindUser.getBindUnionid());
        map.put("officialAccountOpenId", bindUser.getOfficialAccountOpenid());

        // 返回包含OpenID信息的Map
        return map;
    }

    @Override
    public void insertOpenIdByUnionId(String openId, String unionId) {
        UserBindConnectEntity base = baseMapper.selectOne(new LambdaQueryWrapper<UserBindConnectEntity>()
                .eq(UserBindConnectEntity::getBindUnionid, unionId));
        if (base != null) {
            baseMapper.update(null, new LambdaUpdateWrapper<UserBindConnectEntity>()
                    .set(UserBindConnectEntity::getOfficialAccountOpenid, openId)
                    .eq(UserBindConnectEntity::getBindUnionid, unionId));
        } else {
            UserBindConnectEntity entity = new UserBindConnectEntity();
            entity.setBindId("official_account_" + openId);
            entity.setBindType(Byte.parseByte("13"));
            entity.setUserId(DateUtils.getDateRandomSix() + RandomUtil.randomNumbers(2));
            entity.setBindTime(new Date());
            entity.setBindNickname("公众号XXXX");
            entity.setBindOpenid(openId);
            entity.setBindUnionid(unionId);
            entity.setOfficialAccountOpenid(openId);
            baseMapper.insert(entity);
        }
    }
}
