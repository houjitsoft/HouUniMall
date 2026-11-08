package com.houjiang.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.permission.dto.UserCreateRequest;
import com.houjiang.permission.dto.UserSearchRequest;
import com.houjiang.permission.dto.UserUpdateRequest;
import com.houjiang.permission.entity.User;
import com.houjiang.permission.entity.UserRoleRelation;
import com.houjiang.permission.mapper.UserMapper;
import com.houjiang.permission.mapper.UserRoleRelationMapper;
import com.houjiang.permission.service.RoleService;
import com.houjiang.permission.service.UserService;
import com.houjiang.permission.vo.PageResult;
import com.houjiang.permission.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Service
public class UserServiceImpl implements UserService {
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    
    @Resource
    private RoleService roleService;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public PageResult<UserVO> getUserList(UserSearchRequest searchRequest) {
        int page = searchRequest.getPageNo() != null ? searchRequest.getPageNo() : 1;
        int limit = searchRequest.getPageSize() != null ? searchRequest.getPageSize() : 15;

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", 0);

        if (StringUtils.hasText(searchRequest.getUsername())) {
            queryWrapper.and(w -> w.like("user_name", searchRequest.getUsername())
                    .or().like("user_mobile", searchRequest.getUsername()));
        }
        if (StringUtils.hasText(searchRequest.getRealName())) {
            queryWrapper.like("user_nickname", searchRequest.getRealName());
        }
        if (StringUtils.hasText(searchRequest.getMobile())) {
            queryWrapper.like("user_mobile", searchRequest.getMobile());
        }
        if (searchRequest.getStatus() != null) {
            queryWrapper.eq("user_state", searchRequest.getStatus());
        }
        if (StringUtils.hasText(searchRequest.getRegisterTerminal())) {
            queryWrapper.eq("register_terminal", normalizeRegisterTerminal(searchRequest.getRegisterTerminal()));
        }
        if (searchRequest.getRoleId() != null) {
            List<UserRoleRelation> relations = userRoleRelationMapper.selectList(
                    new QueryWrapper<UserRoleRelation>().eq("role_id", searchRequest.getRoleId()));
            if (relations.isEmpty()) {
                return PageResult.of(0L, (long) page, (long) limit, new ArrayList<>());
            }
            List<Long> userIds = relations.stream().map(UserRoleRelation::getUserId).collect(Collectors.toList());
            queryWrapper.in("user_id", userIds);
        }

        queryWrapper.orderByDesc("create_time");

        IPage<User> iPage = userMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<UserVO> list = new ArrayList<>();
        for (User user : iPage.getRecords()) {
            list.add(toUserVO(user));
        }

        return PageResult.of(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getUserId() != null ? String.valueOf(user.getUserId()) : null);
        vo.setUsername(user.getUsername());
        vo.setRealName(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername());
        vo.setMobile(user.getMobile());
        vo.setStatus(user.getStatus());
        vo.setStatusText(user.getStatus() != null && user.getStatus() == 1 ? "正常" : "禁用");
        if (user.getUserId() != null) {
            vo.setRoles(roleService.getRoleNamesByUserId(String.valueOf(user.getUserId())));
        } else {
            vo.setRoles(new ArrayList<>());
        }
        vo.setCreateTime(user.getCreateTime() != null ? user.getCreateTime().format(DATE_TIME_FORMATTER) : null);
        vo.setUpdateTime(user.getUpdateTime() != null ? user.getUpdateTime().format(DATE_TIME_FORMATTER) : null);
        vo.setLastLoginTime(user.getLastLoginTime() != null ? user.getLastLoginTime().format(DATE_TIME_FORMATTER) : null);
        String terminal = normalizeRegisterTerminal(user.getRegisterTerminal());
        vo.setRegisterTerminal(terminal);
        vo.setRegisterTerminalText(registerTerminalText(terminal));
        return vo;
    }

    private String normalizeRegisterTerminal(String raw) {
        if (!StringUtils.hasText(raw)) {
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
        return value;
    }

    private String registerTerminalText(String terminal) {
        if (!StringUtils.hasText(terminal)) {
            return "-";
        }
        if ("mall".equals(terminal)) {
            return "商城";
        }
        if ("houcloud".equals(terminal)) {
            return "houcloud";
        }
        if ("hougeo".equals(terminal)) {
            return "hougeo";
        }
        if ("houocr".equals(terminal)) {
            return "OCR 站点";
        }
        return terminal;
    }
    
    @Override
    public UserVO getUserDetail(String id) {
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("user_id", Long.valueOf(id))
                .eq("is_delete", 0)
                .last("limit 1"));

        Assert.notNull(user, "用户不存在!");
        return toUserVO(user);
    }
    
    @Override
    @Transactional
    public void createUser(UserCreateRequest request) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(request.getUsername());
        Assert.isNull(existingUser, "用户名已存在!");
        
        // 检查手机号是否已存在
        if (StringUtils.hasText(request.getMobile())) {
            existingUser = userMapper.selectByMobile(request.getMobile());
            Assert.isNull(existingUser, "手机号已存在!");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setMobile(request.getMobile());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        user.setIsDelete(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        
        // 分配角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Integer roleId : request.getRoleIds()) {
                UserRoleRelation relation = new UserRoleRelation();
                relation.setUserId(user.getUserId());
                relation.setRoleId(roleId);
                userRoleRelationMapper.insert(relation);
            }
        }
    }
    
    @Override
    @Transactional
    public void updateUser(UserUpdateRequest request) {
        Long userId = Long.valueOf(request.getId());
        User existingUser = userMapper.selectOne(new QueryWrapper<User>()
                .eq("user_id", userId)
                .eq("is_delete", 0)
                .last("limit 1"));
        Assert.notNull(existingUser, "用户不存在!");

        User duplicateUser = userMapper.selectByUsername(request.getUsername());
        if (duplicateUser != null && !userId.equals(duplicateUser.getUserId())) {
            throw new IllegalArgumentException("用户名已存在!");
        }

        if (StringUtils.hasText(request.getMobile())) {
            duplicateUser = userMapper.selectByMobile(request.getMobile());
            if (duplicateUser != null && !userId.equals(duplicateUser.getUserId())) {
                throw new IllegalArgumentException("手机号已存在!");
            }
        }

        User user = new User();
        user.setUserId(userId);
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setMobile(request.getMobile());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);

        // 省略 roleIds 表示只改用户资料；显式空数组才表示清空角色。
        if (request.getRoleIds() != null) {
            userRoleRelationMapper.delete(new QueryWrapper<UserRoleRelation>().eq("user_id", userId));
            for (Integer roleId : request.getRoleIds()) {
                UserRoleRelation relation = new UserRoleRelation();
                relation.setUserId(userId);
                relation.setRoleId(roleId);
                userRoleRelationMapper.insert(relation);
            }
        }
    }
    
    @Override
    @Transactional
    public void deleteUser(String id) {
        // 检查用户是否存在
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("user_id", Long.valueOf(id))
                .last("limit 1"));
        Assert.notNull(user, "用户不存在!");

        user.setIsDelete(1);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        // 删除用户角色关系
        userRoleRelationMapper.delete(new QueryWrapper<UserRoleRelation>().eq("user_id", Long.valueOf(id)));
    }
    
    @Override
    @Transactional
    public void resetPassword(String id, String newPassword) {
        // 检查用户是否存在
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("user_id", Long.valueOf(id))
                .eq("is_delete", 0)
                .last("limit 1"));
        Assert.notNull(user, "用户不存在!");

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
    
    @Override
    @Transactional
    public void updateUserStatus(String id, Integer status) {
        // 检查用户是否存在
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("user_id", Long.valueOf(id))
                .eq("is_delete", 0)
                .last("limit 1"));
        Assert.notNull(user, "用户不存在!");

        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
}
