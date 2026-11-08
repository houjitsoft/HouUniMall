package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.errorcode.domain.PermissionErrorCode;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.JSEncrypt;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.PasswordUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.vo.user.UserSellerBaseVO;
import com.houjiang.userms.dto.UserSellerBaseDTO;
import com.houjiang.userms.entity.UserSellerBaseEntity;
import com.houjiang.userms.mapper.UserInfoMapper;
import com.houjiang.userms.mapper.UserSellerBaseMapper;
import com.houjiang.userms.service.UserSellerBaseService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户商家基础信息Service实现类
 * @author Amos
 */
@Service
public class UserSellerBaseServiceImpl extends ServiceImpl<UserSellerBaseMapper, UserSellerBaseEntity>
    implements UserSellerBaseService{

    @Autowired
    private UserInfoMapper userInfoMapper;

    /**
     * 根据shopId获取商家基础信息
     * @param returnMap 包含shopId的Map
     * @return 返回商家基础信息的ResponseResult
     */
    @Override
    public ResponseResult getSellerInfo(Map<String, String> returnMap) {
        UserSellerBaseEntity ucenterSellerBaseEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<UserSellerBaseEntity>()
                .eq(UserSellerBaseEntity::getShopId, returnMap.get("shopId"))
                .eq(UserSellerBaseEntity::getSellerIsAdmin, CommonType.commonStatus.YES.getCode()));
        return ResponseResult.success(ucenterSellerBaseEntity);
    }

    /**
     * 添加商家用户
     * @param userSellerBase 商家用户信息DTO
     * @return 添加结果的ResponseResult
     */
    @Override
    @SneakyThrows
    public ResponseResult add(UserSellerBaseDTO userSellerBase) {
        String token = userSellerBase.getToken();
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        if (null == userInfo.getShopId()) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        //获取shopId
        String shopId = userInfo.getShopId();
        //判断是否为品牌店铺
        UserSellerBaseEntity entity = this.baseMapper.selectOne(new LambdaQueryWrapper<UserSellerBaseEntity>()
                .eq(UserSellerBaseEntity::getShopId, shopId)
                .eq(UserSellerBaseEntity::getSellerIsAdmin, CommonType.commonStatus.YES.getCode()));
        if (entity != null) {

            UserInfoVO toAddUserInfo = null;
            Integer num = 0;
            if (entity.getShopType() == 3) {
                //走品牌查询
                toAddUserInfo = userInfoMapper.getUserInfoByPhoneNumber1(userSellerBase.getUserMobile(), shopId);

                //判断该手机号是否已被其他的商家绑为子账号
                num = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<UserSellerBaseEntity>()
                        .eq(UserSellerBaseEntity::getSellerName, userSellerBase.getUserMobile())
                        .eq(UserSellerBaseEntity::getShopType, CommonType.ShopType.BRAND_SHOP.getCode())
                        .eq(UserSellerBaseEntity::getShopId, shopId)));
                if (null == toAddUserInfo) {
                    toAddUserInfo = userInfoMapper.getUserInfoByPhoneNumber(userSellerBase.getUserMobile());
                }
            } else {
                toAddUserInfo = userInfoMapper.getUserInfoByPhoneNumber(userSellerBase.getUserMobile());

                //判断该手机号是否已被其他的商家绑为子账号
                QueryWrapper<UserSellerBaseEntity> objectQueryWrapper = new QueryWrapper<>();
                objectQueryWrapper.eq("seller_name", userSellerBase.getUserMobile());
                num = Math.toIntExact(this.baseMapper.selectCount(objectQueryWrapper));
            }
            if (null == toAddUserInfo) {
                return ResponseResult.fail(2, "用户未注册");
            }
            if (0 != num) {
                return ResponseResult.fail(2, "该账号已被绑定");
            } else {
                Integer count = this.baseMapper.selectShopGateCount(userSellerBase.getUserMobile());
                if(count > 0){
                    return ResponseResult.fail(2, "该账号已被绑定");
                }
            }
            String paramPasswordString = toAddUserInfo.getPassword();
            String password =  JSEncrypt.decrypt(userSellerBase.getPassword());
            if (!PasswordUtils.BCryptPasswordMatches(password,paramPasswordString)) {
                return ResponseResult.fail(2, "账号或者密码错误");
            }
            if (null == userInfo.getShopId()) {
                return ResponseResult.fail(2, "ShopId为null");
            }
            UserSellerBaseEntity baseEntity = new UserSellerBaseEntity();
            baseEntity.setSellerName(toAddUserInfo.getUserName());
            baseEntity.setShopId(Integer.valueOf(userInfo.getShopId()));
            baseEntity.setUserId(toAddUserInfo.getUserId());
            baseEntity.setRightsGroupId(userSellerBase.getGroupId());
            baseEntity.setSellerIsAdmin((byte) 0);
            baseEntity.setSellerLoginTime(new Date());
            baseEntity.setSellerGroupId(userSellerBase.getGroupId());
            baseEntity.setShopType(entity.getShopType());
            Integer torfInteger = this.baseMapper.insert(baseEntity);
            if (1 == torfInteger) {
                return ResponseResult.success(1, "添加账户成功");
            }

        }
        return ResponseResult.fail(2, "添加账户失败");
    }

    /**
     * 根据传入的参数映射表获取卖家账户信息。
     * 此方法首先通过token验证用户权限，只有具备相应权限的用户才能查询卖家账户信息。
     * @param paraMap 参数映射表，包含请求所需的各类参数，其中"token"用于用户身份验证。
     * @return 返回查询结果，如果查询成功，返回包含卖家账户信息的列表；如果查询失败或无权限，返回相应的错误信息。
     */
    @Override
    public ResponseResult get(Map<String, String> paraMap) {
        // 从参数映射表中获取token
        String token = paraMap.get("token");
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 判断用户是否具有查询权限
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(token);
        // 如果用户无权限，返回错误信息
        if (!userPermissionsResult.getFlag()) {
            // 旧写法返回裸码 10086。10086 已认领为 AUTH 域 30801「登录失败，请重新登录」，
            // 前端据此清 token 跳登录页 —— 没权限会被误当成没登录。改用 PERMISSION 域 31005。
            return ResponseResult.fail(PermissionErrorCode.PERMISSION_ACCESS_DENIED.code(),
                    PermissionErrorCode.PERMISSION_ACCESS_DENIED.meta().userMessage());
        }
        // 将用户的shopId添加到参数映射表中，用于后续的查询
        paraMap.put("shopId", userInfo.getShopId());
        // 根据更新后的参数映射表查询卖家账户信息
        List<UserSellerBaseDTO> list = this.baseMapper.getSellerAccount(paraMap);
        // 如果查询结果不为空，返回查询结果
        if (null != list) {
            return ResponseResult.success(list);
        }
        // 如果查询结果为空，返回查询失败的错误信息
        return ResponseResult.fail(2, "查询账号失败");
    }
    /**
     * 删除卖家账号
     *
     * @param paraMap 参数映射表，包含token和sellerId
     * @return 删除操作的结果，成功或失败及原因
     */
    @Override
    public ResponseResult delete(Map<String, String> paraMap) {
        // 从参数映射中获取token
        String token = paraMap.get("token");
        // 判断用户是否有权限操作
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(token);
        if (!userPermissionsResult.getFlag()) {
            // 若无权限，则返回失败结果
            // 裸码 10086（登录语义）→ PERMISSION 域 31005，理由同 get()
            return ResponseResult.fail(PermissionErrorCode.PERMISSION_ACCESS_DENIED.code(),
                    PermissionErrorCode.PERMISSION_ACCESS_DENIED.meta().userMessage());
        }
        // 构建查询条件，根据sellerId删除卖家信息
        QueryWrapper<UserSellerBaseEntity> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("seller_id", paraMap.get("sellerId"));
        // 执行删除操作
        Integer torfInteger = this.baseMapper.delete(objectQueryWrapper);
        if (1 == torfInteger) {
            // 若删除成功，则返回成功结果
            return ResponseResult.success(1, "删除账号成功");
        }
        // 若删除失败，则返回失败结果
        return ResponseResult.fail(2, "删除账号失败");
    }

    /**
     * 更新卖家权限
     *
     * @param paraMap 参数映射表，包含token、sellerId和sellerGroupId
     * @return 更新操作的结果，成功或失败及原因
     */
    @Override
    public ResponseResult update(Map<String, String> paraMap) {
        // 从参数映射中获取token
        String token = paraMap.get("token");
        // 判断用户是否有权限操作
        // 权限判断
        UserPermissionsResult userPermissionsResult = UserService.judgePermissions(token);
        if (!userPermissionsResult.getFlag()) {
            // 若无权限，则返回失败结果
            // 裸码 10086（登录语义）→ PERMISSION 域 31005，理由同 get()
            return ResponseResult.fail(PermissionErrorCode.PERMISSION_ACCESS_DENIED.code(),
                    PermissionErrorCode.PERMISSION_ACCESS_DENIED.meta().userMessage());
        }
        // 创建卖家基础信息实体，设置sellerId和sellerGroupId
        UserSellerBaseEntity baseEntity = new UserSellerBaseEntity();
        baseEntity.setSellerId(paraMap.get("sellerId"));
        baseEntity.setSellerGroupId(Integer.valueOf(paraMap.get("sellerGroupId")));
        // 执行更新操作
        Integer returnTorf = this.baseMapper.updateById(baseEntity);
        if (1 == returnTorf) {
            // 若更新成功，则返回成功结果
            return ResponseResult.success(1, "修改权限成功");
        }
        // 若更新失败，则返回失败结果
        return ResponseResult.fail(2, "修改失败");
    }

    /**
     * 获取卖家基础信息列表
     *
     * @param shopId 店铺ID
     * @return 卖家基础信息列表
     */
    @Override
    public List<UserSellerBaseVO> getSellerBase(Integer shopId) {
        // 通过shopId获取卖家基础信息列表
        List<UserSellerBaseVO> list = this.baseMapper.getSellerBase(shopId);
        return list;
    }

    /**
     * 查询店铺卖家的基础信息
     *
     * @param userId 用户ID
     * @return 若用户是店铺管理员，则返回成功结果；否则，返回失败结果
     */
    @Override
    public ResponseResult queryShopSellerBase(String userId) {
        // 根据userId查询卖家基础信息
        UserSellerBaseEntity baseEntity = baseMapper.selectOne(new LambdaQueryWrapper<UserSellerBaseEntity>()
                .eq(UserSellerBaseEntity::getUserId, userId));
        if (baseEntity == null) {
            // 若未查询到信息，则视为成功，可能是因为用户不是卖家
            return ResponseResult.success();
        }
        if (baseEntity.getSellerIsAdmin() == 1) {
            // 若用户是店铺管理员，则返回成功结果
            return ResponseResult.success();
        }
        if (baseEntity.getSellerIsAdmin() == 0) {
            // 用户是子账号，则返回成功结果
            return ResponseResult.success();
        }
        // 若用户不是店铺管理员，则返回失败结果
        return ResponseResult.fail();
    }

    /**
     * 根据用户ID查询对应的店铺管理员用户ID
     *
     * @param returnMap 查询操作的参数，包含用户ID
     * @return 店铺管理员用户ID
     */
    @Override
    public String getSellerAdmin(Map<String, String> returnMap) {
        UserSellerBaseEntity ucenterSellerBaseEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<UserSellerBaseEntity>()
                .eq(UserSellerBaseEntity::getUserId, returnMap.get("userId")));
        if (ucenterSellerBaseEntity != null) {
            UserSellerBaseEntity baseEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<UserSellerBaseEntity>()
                    .eq(UserSellerBaseEntity::getSellerIsAdmin, CommonType.commonStatus.YES.getCode())
                    .eq(UserSellerBaseEntity::getShopId, ucenterSellerBaseEntity.getShopId())
                    .eq(UserSellerBaseEntity::getShopType, ucenterSellerBaseEntity.getShopType())
            );
            if (baseEntity != null) {
                return baseEntity.getUserId();
            }
        }
        return null;
    }

    /**
     * 根据店铺ID获取店铺子账号列表
     *
     * @param shopId 店铺ID
     * @return 店铺子账号列表
     */
    @Override
    public List<UserSellerBaseVO> getSubAccount(String shopId) {
        return baseMapper.getSubAccount(shopId);
    }

    @Override
    public Integer insertSellerbase(Map<String, Object> map) {
        Integer count = this.baseMapper.insertSellerbase(map);
        return count;
    }

    @Override
    public String getShopAdmin(String shopId) {
        String userId = this.baseMapper.getShopAdmin(shopId);
        return userId;
    }

    @Override
    public String getUserIDByShopId(String shopId) {
        String userId = this.baseMapper.getUserIDByShopId(shopId);
        return userId;
    }
}
