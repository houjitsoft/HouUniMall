package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RequestTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.entity.UserDeptEntity;
import com.houjiang.userms.mapper.UserDeptMapper;
import com.houjiang.userms.mapper.UserInfoMapper;
import com.houjiang.userms.service.UserDeptService;
import com.houjiang.userms.service.UserOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门管理服务实现类
 * 
 * @author Amos
 * @since 2025-01-22
 */
@Slf4j
@Service
public class UserDeptServiceImpl extends ServiceImpl<UserDeptMapper, UserDeptEntity> implements UserDeptService {

    @Autowired
    private UserOrganizationService userOrganizationService;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public ResponseResult getDeptListByCurrentUser() {
        try {
            // 从当前请求中获取token
            String token = RequestTokenUtils.getTokenFromRequest();
            if (token == null) {
                log.error("获取当前请求token失败，用户未登录");
                return ResponseResult.fail("用户未登录");
            }

            // 从token中获取当前用户信息
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
            if (userInfo == null) {
                log.error("获取当前用户信息失败，用户未登录");
                return ResponseResult.fail("用户未登录");
            }

            // 获取用户的主组织ID
            Long orgId = getPrimaryOrgId(userInfo);
            if (orgId == null) {
                log.warn("用户没有关联任何组织，返回空部门列表，userId: {}", userInfo.getUserId());
                return ResponseResult.success(new ArrayList<>());
            }
            return getDeptListByOrgId(orgId);

        } catch (Exception e) {
            log.error("获取当前用户部门列表异常", e);
            return ResponseResult.fail("获取部门列表失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseResult getDeptListByOrgId(Long orgId) {
        try {
            if (orgId == null) {
                return ResponseResult.fail("组织ID不能为空");
            }

            log.info("根据orgId获取部门列表，orgId: {}", orgId);
            List<UserDeptEntity> deptList = baseMapper.selectDeptsByOrgId(orgId);
            
            if (deptList == null || deptList.isEmpty()) {
                log.info("组织下没有部门数据，orgId: {}", orgId);
                return ResponseResult.success(new ArrayList<>());
            }

            log.info("成功获取部门列表，orgId: {}, 部门数量: {}", orgId, deptList.size());
            return ResponseResult.success(deptList);

        } catch (Exception e) {
            log.error("根据orgId获取部门列表异常，orgId: {}", orgId, e);
            return ResponseResult.fail("获取部门列表失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseResult getDeptTreeByCurrentUser() {
        try {
            // 从当前请求中获取token
            String token = RequestTokenUtils.getTokenFromRequest();
            if (token == null) {
                log.error("获取当前请求token失败，用户未登录");
                return ResponseResult.fail("用户未登录");
            }

            // 从token中获取当前用户信息
            UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
            if (userInfo == null) {
                log.error("获取当前用户信息失败，用户未登录");
                return ResponseResult.fail("用户未登录");
            }

            // 获取用户的主组织ID
            Long orgId = getPrimaryOrgId(userInfo);
            if (orgId == null) {
                log.warn("用户没有关联任何组织，返回空部门树，userId: {}", userInfo.getUserId());
                return ResponseResult.success(new ArrayList<>());
            }

            log.info("获取当前用户的部门树，userId: {}, orgId: {}", userInfo.getUserId(), orgId);
            return getDeptTreeByOrgId(orgId);

        } catch (Exception e) {
            log.error("获取当前用户部门树异常", e);
            return ResponseResult.fail("获取部门树失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseResult getDeptTreeByOrgId(Long orgId) {
        try {
            if (orgId == null) {
                return ResponseResult.fail("组织ID不能为空");
            }

            log.info("根据orgId获取部门树，orgId: {}", orgId);
            List<Map<String, Object>> deptList = baseMapper.selectDeptTreeByOrgId(orgId);
            
            if (deptList == null || deptList.isEmpty()) {
                log.info("组织下没有部门数据，orgId: {}", orgId);
                return ResponseResult.success(new ArrayList<>());
            }

            // 构建树形结构
            List<Map<String, Object>> deptTree = buildDeptTree(deptList);
            
            log.info("成功获取部门树，orgId: {}, 部门数量: {}, 树节点数量: {}", 
                    orgId, deptList.size(), deptTree.size());
            return ResponseResult.success(deptTree);

        } catch (Exception e) {
            log.error("根据orgId获取部门树异常，orgId: {}", orgId, e);
            return ResponseResult.fail("获取部门树失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseResult getDeptDetailById(Long deptId) {
        try {
            if (deptId == null) {
                return ResponseResult.fail("部门ID不能为空");
            }

            log.info("根据deptId获取部门详细信息，deptId: {}", deptId);
            Map<String, Object> deptDetail = baseMapper.selectDeptDetailById(deptId);
            
            if (deptDetail == null || deptDetail.isEmpty()) {
                log.info("未找到部门信息，deptId: {}", deptId);
                return ResponseResult.fail("部门不存在");
            }

            log.info("成功获取部门详细信息，deptId: {}", deptId);
            return ResponseResult.success(deptDetail);

        } catch (Exception e) {
            log.error("根据deptId获取部门详细信息异常，deptId: {}", deptId, e);
            return ResponseResult.fail("获取部门详细信息失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> buildDeptTree(List<Map<String, Object>> deptList) {
        if (deptList == null || deptList.isEmpty()) {
            return new ArrayList<>();
        }

        // 创建部门ID到部门的映射
        Map<Long, Map<String, Object>> deptMap = deptList.stream()
                .collect(Collectors.toMap(
                        dept -> Long.valueOf(dept.get("dept_id").toString()),
                        dept -> dept
                ));

        // 创建结果列表
        List<Map<String, Object>> result = new ArrayList<>();

        // 遍历所有部门，构建树形结构
        for (Map<String, Object> dept : deptList) {
            Long deptId = Long.valueOf(dept.get("dept_id").toString());
            Long parentId = dept.get("parent_id") != null ? 
                    Long.valueOf(dept.get("parent_id").toString()) : null;

            // 如果是根节点（parent_id 为空或 0），直接添加到结果中
            if (parentId == null || parentId == 0L) {
                dept.put("children", new ArrayList<Map<String, Object>>());
                result.add(dept);
            } else {
                // 如果有父节点，找到父节点并添加到其children中
                Map<String, Object> parentDept = deptMap.get(parentId);
                if (parentDept != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = 
                            (List<Map<String, Object>>) parentDept.get("children");
                    if (children == null) {
                        children = new ArrayList<>();
                        parentDept.put("children", children);
                    }
                    dept.put("children", new ArrayList<Map<String, Object>>());
                    children.add(dept);
                } else {
                    dept.put("children", new ArrayList<Map<String, Object>>());
                    result.add(dept);
                }
            }
        }

        return result;
    }

    /**
     * 获取用户的主组织ID
     * 
     * @param userInfo 用户信息
     * @return 主组织ID
     */
    private Long getPrimaryOrgId(UserInfoVO userInfo) {
        if (userInfo == null || userInfo.getUserId() == null) {
            return null;
        }

        String userId = userInfo.getUserId();
        try {
            List<Map<String, Object>> orgList = userInfoMapper.getUserOrgList(userId);
            Long orgId = pickPrimaryOrgId(orgList);
            if (orgId != null) {
                return orgId;
            }

            ResponseResult orgResult = userOrganizationService.getUserOrganizations(userId);
            if (orgResult.getCode() == 1 && orgResult.getData() != null) {
                Object data = orgResult.getData();
                if (data instanceof List<?> list) {
                    List<Map<String, Object>> normalized = new ArrayList<>();
                    for (Object item : list) {
                        if (item instanceof Map<?, ?> map) {
                            Map<String, Object> org = new LinkedHashMap<>();
                            map.forEach((k, v) -> org.put(String.valueOf(k), v));
                            normalized.add(org);
                        }
                    }
                    orgId = pickPrimaryOrgId(normalized);
                    if (orgId != null) {
                        return orgId;
                    }
                }
            }

            List<String> legacyOrgIds = baseMapper.selectOrgIdsByUserId(userId);
            if (legacyOrgIds != null) {
                for (String raw : legacyOrgIds) {
                    if (raw == null || raw.isBlank()) {
                        continue;
                    }
                    for (String part : raw.split(",")) {
                        Long parsed = parseOrgId(part);
                        if (parsed != null) {
                            return parsed;
                        }
                    }
                }
            }

            return null;
        } catch (Exception e) {
            log.error("获取用户主组织ID失败，userId: {}", userId, e);
            return null;
        }
    }

    /**
     * 从组织列表中选取主组织ID
     *
     * @param orgList 组织列表
     * @return 主组织ID
     */
    private Long pickPrimaryOrgId(List<Map<String, Object>> orgList) {
        if (orgList == null || orgList.isEmpty()) {
            return null;
        }
        if (orgList.size() == 1) {
            return parseOrgId(orgList.get(0).get("org_id"));
        }
        for (Map<String, Object> org : orgList) {
            Object isPrimary = org.get("is_primary");
            if (isPrimary != null && "1".equals(String.valueOf(isPrimary))) {
                Long orgId = parseOrgId(org.get("org_id"));
                if (orgId != null) {
                    return orgId;
                }
            }
        }
        return parseOrgId(orgList.get(0).get("org_id"));
    }

    /**
     * 解析组织ID
     *
     * @param raw 原始值
     * @return Long 类型组织ID
     */
    private Long parseOrgId(Object raw) {
        if (raw == null) {
            return null;
        }
        String text = String.valueOf(raw).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
