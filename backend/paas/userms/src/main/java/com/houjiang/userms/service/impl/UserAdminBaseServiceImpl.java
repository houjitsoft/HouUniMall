package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.entity.UserAdminBaseEntity;
import com.houjiang.userms.entity.UserAdminDepartmentEntity;
import com.houjiang.userms.feign.BaseServiceClient;
import com.houjiang.userms.feign.PaymentServiceClient;
import com.houjiang.userms.mapper.UserAdminBaseMapper;
import com.houjiang.userms.mapper.UserAdminDepartmentMapper;
import com.houjiang.userms.service.UserAdminBaseService;
import com.houjiang.userms.vo.UserPersonalBaseVO;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 用户管理服务实现类
 * 提供用户相关服务的实现方法
 * @author Amos
 */
@Service
public class UserAdminBaseServiceImpl extends ServiceImpl<UserAdminBaseMapper, UserAdminBaseEntity>
        implements UserAdminBaseService {

    @Autowired
    private PaymentServiceClient paymentServiceClient;

    @Autowired
    private UserAdminDepartmentMapper userAdminDepartmentMapper;

    @Autowired
    private BaseServiceClient baseServiceClient;
    /**
     * 根据token获取用户信息
     * @param paraMap 包含token的参数映射
     * @return 用户信息的响应结果
     */
    @Override
    public ResponseResult getLimit(Map<String, String> paraMap) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(paraMap.get("token"));
        if (userInfo == null ) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 判读国际站功能是否开通  以及收货地址是否 非中国
        String s = baseServiceClient.selectConfigValueFeign(CommonType.configKey.INTERNATIONALIZATION_SWITCH.getDesc());
        if("0".equals(s)){
            List<String> limits = new ArrayList<>(Arrays.asList(userInfo.getRightsLimitsAll().split(",")));
            if(limits.contains("currency")){
                // 移除该限制条件
                limits.remove("currency");
                // 更新用户权限列表字符串形式
                userInfo.setRightsLimitsAll(StringUtils.join(limits, ","));
            }
        }
        return ResponseResult.success(userInfo);
    }

    /**
     * 删除管理员用户密码
     * @param param 包含用户ID和token的参数
     * @return 删除操作的响应结果
     */
    @Override
    public ResponseResult deleteAdminUserPass(UserPersonalBaseVO param) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            this.baseMapper.delete(new LambdaQueryWrapper<UserAdminBaseEntity>()
                    .eq(UserAdminBaseEntity::getUserId, param.getUserId()));
            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
    }

    /**
     * 获取个人基础信息
     * @param param 包含用户ID和token的参数
     * @return 用户个人基础信息的响应结果
     */
    @Override
    public ResponseResult getPersonalBase(UserPersonalBaseVO param) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            UserAdminBaseEntity personalBase = this.baseMapper.selectById(userInfo.getUserId());
            personalBase.setUserPassword(null);
            UserAdminDepartmentEntity userAdminDepartmentEntity = userAdminDepartmentMapper.selectById(personalBase.getUserDepartment());
            if(null != userAdminDepartmentEntity){
                personalBase.setUserDepartmentName(userAdminDepartmentEntity.getUserDepartment());
            }
            return ResponseResult.success(personalBase);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 获取平台用户信息
     * @param map 包含请求参数的映射
     * @return 平台用户信息的响应结果
     * @throws IOException 如果获取信息过程中发生IO异常
     */
    @Override
    public ResponseResult getPlatformUserInfo(Map<String, String> map) throws IOException {
        UserInfoVO platformUserInfo = this.baseMapper.getPlatformUserInfo(map);
        return ResponseResult.success(platformUserInfo);
    }

    /**
     * 添加管理员用户
     * @param param 包含用户信息的参数映射
     * @return 添加操作的响应结果
     */
    @Override
    @SneakyThrows
    public ResponseResult addAdminUser(Map<String, String> param) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.get("token"));
        if (null != userInfo) {
            // 判断用户名是否重复
            if (this.baseMapper.selectCount(new LambdaQueryWrapper<UserAdminBaseEntity>().eq(UserAdminBaseEntity::getUserAccount,param.get("userAccount"))) >0) {
                return ResponseResult.fail(BizCodeEnume.ADD_ADMIN_USER_ERROR);
            }
            String password =  JSEncrypt.decrypt(param.get("password").toString());
            param.put("password",PasswordUtils.BCryptPasswordEncoder(password));
            this.baseMapper.addAdminUser(param);
            String userId = this.baseMapper.selectOne(new LambdaQueryWrapper<UserAdminBaseEntity>()
                            .eq(UserAdminBaseEntity::getUserAccount,param.get("userAccount"))).getUserId();
            // 在支付服务中创建用户
            paymentServiceClient.createUser(userId);
            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 更新管理员用户密码
     * @param param 包含新密码和token的参数映射
     * @return 更新操作的响应结果
     */
    @Override
    @SneakyThrows
    public ResponseResult updateAdminUserPass(Map<String, String> param) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.get("token"));
        if (null != userInfo) {
            if (param.get("password") != null && !"".equals(param.get("password").trim())) {
                // 对新密码进行加密
                String password =  JSEncrypt.decrypt(param.get("password").toString());
                param.put("password",PasswordUtils.BCryptPasswordEncoder(password));
            }
            this.baseMapper.updateAdminUser(param);
            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 查询管理员用户列表
     * @param param 包含分页信息和查询条件的参数
     * @return 查询结果的响应结果
     */
    @Override
    public ResponseResult queryAdminUserList(UserPersonalBaseVO param) {
        /**
         * 根据token获取用户信息。
         * 解析token以获取用户信息，如果用户信息有效，则进一步查询管理员用户列表。
         * 如果token无效或解析失败，则返回相应的错误信息。
         *
         * @param param 查询参数，包含分页信息和token。
         * @return 如果token有效并查询到用户列表，则返回成功的响应结果；否则返回失败的响应结果。
         */
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            // 设置分页默认值，防止前端未传参导致NPE
            Integer page = param.getPage() != null ? param.getPage() : 1;
            Integer limit = param.getLimit() != null ? param.getLimit() : 10;
            // 初始化分页参数
            Page<UserPersonalBaseVO> pageObj = new Page<>(page, limit);
            // 执行管理员用户列表查询
            IPage<UserPersonalBaseVO> results = this.baseMapper.queryAdminUserList(pageObj, param);
            return ResponseResult.success(results);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }
}
