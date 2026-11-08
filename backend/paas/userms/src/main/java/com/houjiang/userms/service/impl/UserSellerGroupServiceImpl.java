package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.errorcode.domain.AuthErrorCode;
import com.houjiang.commons.errorcode.domain.PermissionErrorCode;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.entity.UserSellerBaseEntity;
import com.houjiang.userms.entity.UserSellerGroupEntity;
import com.houjiang.userms.mapper.UserSellerBaseMapper;
import com.houjiang.userms.mapper.UserSellerGroupMapper;
import com.houjiang.userms.service.UserSellerGroupService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 用户商家权限组服务实现类
 * @author Amos
 */
@Service
public class UserSellerGroupServiceImpl extends ServiceImpl<UserSellerGroupMapper, UserSellerGroupEntity>
        implements UserSellerGroupService {

    @Resource
    private UserSellerBaseMapper userSellerBaseMapper;

    /**
     * 获取用户商家权限组信息
     * @param paraMap 包含token等参数的映射
     * @return 返回权限组信息列表或错误信息
     */
    @Override
    public ResponseResult get(Map<String, String> paraMap) {
        // 通过token获取用户信息
        String token = paraMap.get("token");
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(token);
        if (!userPermissionsResult.getFlag()) {
            // 旧写法返回裸码 10086。10086 已认领为 AUTH 域 30801「登录失败，请重新登录」，
            // 前端据此清 token 跳登录页 —— 没权限会被误当成没登录。改用 PERMISSION 域 31005。
            return ResponseResult.fail(PermissionErrorCode.PERMISSION_ACCESS_DENIED.code(),
                    PermissionErrorCode.PERMISSION_ACCESS_DENIED.meta().userMessage());
        }
        // 查询条件：商家ID
        QueryWrapper<UserSellerGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_id", userInfo.getShopId());
        List<UserSellerGroupEntity> entityList = this.baseMapper.selectList(queryWrapper);
        if (null == entityList) {
            return ResponseResult.fail(2, "没有权限组信息");
        }
        return ResponseResult.success(entityList);
    }

    /**
     * 添加用户商家权限组
     * @param paraMap 包含token、groupName等参数的映射
     * @return 返回添加结果或错误信息
     */
    @Override
    public ResponseResult add(Map<String, String> paraMap) {
        // 通过token获取用户信息
        String token = paraMap.get("token");
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 权限判断
        if (userInfo == null) {
            // 旧写法返回裸码 10086「没有权限」，但这里是「凭 token 解析不到用户」，属登录态问题
            // 不是权限问题 —— 按「只有真·认证失效才判 AUTH、真·权限不足才判 PERMISSION」的原则，
            // 改用 AUTH 域 30802「请先登录后再操作」。
            return ResponseResult.fail(AuthErrorCode.USER_NOT_LOGGED_IN.code(),
                    AuthErrorCode.USER_NOT_LOGGED_IN.meta().userMessage());
        }
        // 检查是否已存在相同的权限组名称
        QueryWrapper<UserSellerGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_id", userInfo.getShopId())
                .eq("group_name", paraMap.get("groupName"));
        UserSellerGroupEntity entity = this.baseMapper.selectOne(queryWrapper);
        if (entity != null && null != entity.getGroupId()) {
            return ResponseResult.fail(2, "权限组已存在");
        }
        // 创建新的权限组并插入
        UserSellerGroupEntity userSellerGroupEntity = new UserSellerGroupEntity();
        userSellerGroupEntity.setGroupName(paraMap.get("groupName"));
        userSellerGroupEntity.setLimits(paraMap.get("limits"));
        userSellerGroupEntity.setShopId(Integer.valueOf(userInfo.getShopId()));
        userSellerGroupEntity.setSmtLimits("1");
        Integer torfInteger = this.baseMapper.insert(userSellerGroupEntity);
        if (1 == torfInteger) {
            return ResponseResult.success(1, "添加权限成功");
        }
        return ResponseResult.fail(2, "添加权限失败");
    }

    /**
     * 删除用户商家权限组
     * @param paraMap 包含token、groupId等参数的映射
     * @return 返回删除结果或错误信息
     */
    @Override
    public ResponseResult delete(Map<String, String> paraMap) {
        // 通过token获取用户信息
        String token = paraMap.get("token");
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(token);
        if (!userPermissionsResult.getFlag()) {
            // 裸码 10086（登录语义）→ PERMISSION 域 31005，理由同上
            return ResponseResult.fail(PermissionErrorCode.PERMISSION_ACCESS_DENIED.code(),
                    PermissionErrorCode.PERMISSION_ACCESS_DENIED.meta().userMessage());
        }
        // 检查该权限组下是否还有非管理员用户
        String groupId = paraMap.get("groupId");
        QueryWrapper<UserSellerBaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("seller_group_id", groupId).ne("seller_is_admin", 1);
        Integer count = Math.toIntExact(userSellerBaseMapper.selectCount(queryWrapper));
        if (count > 0) {
            return ResponseResult.fail(2, "删除权限组信息失败，该权限组下还有用户！");
        }
        // 删除权限组
        Integer torfInteger = this.baseMapper.deleteById(groupId);
        if (1 == torfInteger) {
            return ResponseResult.success(1, "删除成功");
        }
        return ResponseResult.fail(2, "删除失败");
    }

    /**
     * 更新用户商家权限组信息
     * @param paraMap 包含token、groupId等参数的映射
     * @return 返回更新结果或错误信息
     */
    @Override
    public ResponseResult update(Map<String, String> paraMap) {
        // 通过token获取用户信息
        String token = paraMap.get("token");
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(token);
        if (!userPermissionsResult.getFlag()) {
            // 裸码 10086（登录语义）→ PERMISSION 域 31005，理由同上
            return ResponseResult.fail(PermissionErrorCode.PERMISSION_ACCESS_DENIED.code(),
                    PermissionErrorCode.PERMISSION_ACCESS_DENIED.meta().userMessage());
        }
        paraMap.put("shopId", userInfo.getShopId());
        UserSellerGroupEntity userSellerGroupEntity = new UserSellerGroupEntity();
        userSellerGroupEntity.setGroupId(Integer.valueOf(paraMap.get("groupId")));
        userSellerGroupEntity.setGroupName(paraMap.get("groupName"));
        userSellerGroupEntity.setLimits(paraMap.get("limits"));
        Integer torfInteger = this.baseMapper.updateById(userSellerGroupEntity);
        if (1 == torfInteger) {
            return ResponseResult.success(1, "修改权限成功");
        }
        return ResponseResult.fail(2, "修改权限失败");
    }
}
