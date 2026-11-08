package com.houjiang.mall.service.perms;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.dto.auth.MallPageResult;
import com.houjiang.mall.entity.auth.MallAdmin;
import com.houjiang.mall.entity.auth.MallAdminRole;
import com.houjiang.mall.entity.auth.MallSystemRole;
import com.houjiang.mall.entity.org.MallAdminDept;
import com.houjiang.mall.entity.org.MallAdminJobs;
import com.houjiang.mall.entity.org.MallDept;
import com.houjiang.mall.entity.org.MallJobs;
import com.houjiang.mall.mapper.auth.MallAdminMapper;
import com.houjiang.mall.mapper.auth.MallAdminRoleMapper;
import com.houjiang.mall.mapper.auth.MallSystemRoleMapper;
import com.houjiang.mall.mapper.org.MallAdminDeptMapper;
import com.houjiang.mall.mapper.org.MallAdminJobsMapper;
import com.houjiang.mall.mapper.org.MallDeptMapper;
import com.houjiang.mall.mapper.org.MallJobsMapper;
import com.houjiang.mall.util.MallPermFormatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Amos
 */
@RequiredArgsConstructor
@Service
public class MallPermAdminService {

    private final MallAdminMapper mallAdminMapper;
    private final MallAdminRoleMapper mallAdminRoleMapper;
    private final MallSystemRoleMapper mallSystemRoleMapper;
    private final MallAdminDeptMapper mallAdminDeptMapper;
    private final MallAdminJobsMapper mallAdminJobsMapper;
    private final MallDeptMapper mallDeptMapper;
    private final MallJobsMapper mallJobsMapper;

    public ResponseResult lists(Map<String, Object> params) {
        int pageNo = MallPermFormatUtil.pageNo(params);
        int pageSize = MallPermFormatUtil.pageSize(params);

        QueryWrapper<MallAdmin> wrapper = notDeletedWrapper(new QueryWrapper<>());
        String account = strVal(params.get("account"));
        String name = strVal(params.get("name"));
        if (StringUtils.hasText(account)) {
            wrapper.like("account", account.trim());
        }
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name.trim());
        }
        Integer roleId = intVal(params.get("role_id"));
        if (roleId != null && roleId > 0) {
            List<Integer> adminIds = selectAdminIdsByRole(roleId);
            if (adminIds.isEmpty()) {
                MallPageResult<Map<String, Object>> empty = new MallPageResult<>();
                empty.setCount(0L);
                empty.setPage_no(pageNo);
                empty.setPage_size(pageSize);
                empty.setLists(Collections.emptyList());
                return ResponseResult.success(empty);
            }
            wrapper.in("id", adminIds);
        }
        wrapper.orderByDesc("id");

        Page<MallAdmin> page = mallAdminMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (MallAdmin admin : page.getRecords()) {
            list.add(toAdminListMap(admin));
        }
        return ResponseResult.success(MallPageResult.of(page, list));
    }

    public ResponseResult all() {
        QueryWrapper<MallAdmin> wrapper = notDeletedWrapper(new QueryWrapper<>());
        wrapper.orderByDesc("id");
        List<MallAdmin> admins = mallAdminMapper.selectList(wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (MallAdmin admin : admins) {
            list.add(toAdminListMap(admin));
        }
        return ResponseResult.success(list);
    }

    public ResponseResult detail(Integer id) {
        MallAdmin admin = findAdmin(id);
        if (admin == null) {
            return ResponseResult.fail(404, "账号已不存在");
        }
        Map<String, Object> map = toAdminListMap(admin);
        map.put("role_id", selectRoleIds(id));
        map.put("dept_id", selectDeptIds(id));
        map.put("jobs_id", selectJobsIds(id));
        return ResponseResult.success(map);
    }

    @Transactional
    public ResponseResult add(Map<String, Object> params) {
        String account = strVal(params.get("account"));
        String name = strVal(params.get("name"));
        String password = strVal(params.get("password"));
        if (!StringUtils.hasText(account) || !StringUtils.hasText(name) || !StringUtils.hasText(password)) {
            return ResponseResult.fail(400, "账号、名称和密码不能为空");
        }
        if (existsAccount(account, null)) {
            return ResponseResult.fail(400, "账号已存在换一个吧");
        }
        if (existsName(name, null)) {
            return ResponseResult.fail(400, "昵称已存在换一个吧");
        }
        int now = currentTs();
        MallAdmin admin = new MallAdmin();
        admin.setAccount(account.trim());
        admin.setName(name.trim());
        admin.setAvatar(strVal(params.get("avatar")));
        admin.setPassword(md5(password));
        admin.setRoot(0);
        admin.setMultipointLogin(intVal(params.get("multipoint_login"), 1));
        admin.setDisable(intVal(params.get("disable"), 0));
        admin.setCreateTime(now);
        admin.setUpdateTime(now);
        admin.setDeleteTime(0);
        mallAdminMapper.insert(admin);
        saveAdminRoles(admin.getId(), params.get("role_id"));
        saveAdminDepts(admin.getId(), params.get("dept_id"));
        saveAdminJobs(admin.getId(), params.get("jobs_id"));
        return ResponseResult.success();
    }

    @Transactional
    public ResponseResult edit(Map<String, Object> params) {
        Integer id = intVal(params.get("id"));
        if (id == null) {
            return ResponseResult.fail(400, "管理员ID不能为空");
        }
        MallAdmin admin = findAdmin(id);
        if (admin == null) {
            return ResponseResult.fail(404, "账号已不存在");
        }

        // 编辑弹窗会带 password 字段（可为空）；状态切换仅传 disable 等字段
        boolean editInfo = params.containsKey("password");

        if (editInfo) {
            String password = strVal(params.get("password"));
            String account = strVal(params.get("account"));
            String name = strVal(params.get("name"));
            if (StringUtils.hasText(account) && !admin.getId().equals(1)) {
                if (existsAccount(account, id)) {
                    return ResponseResult.fail(400, "账号已存在换一个吧");
                }
                admin.setAccount(account.trim());
            }
            if (StringUtils.hasText(name)) {
                if (existsName(name, id)) {
                    return ResponseResult.fail(400, "昵称已存在换一个吧");
                }
                admin.setName(name.trim());
            }
            if (params.containsKey("avatar")) {
                admin.setAvatar(strVal(params.get("avatar")));
            }
            if (params.containsKey("multipoint_login")) {
                admin.setMultipointLogin(intVal(params.get("multipoint_login"), admin.getMultipointLogin()));
            }
            if (StringUtils.hasText(password)) {
                admin.setPassword(md5(password));
            }
            admin.setUpdateTime(currentTs());
            mallAdminMapper.updateById(admin);
            if (params.containsKey("role_id")) {
                saveAdminRoles(id, params.get("role_id"));
            }
            if (params.containsKey("dept_id")) {
                saveAdminDepts(id, params.get("dept_id"));
            }
            if (params.containsKey("jobs_id")) {
                saveAdminJobs(id, params.get("jobs_id"));
            }
        } else {
            if (admin.getRoot() != null && admin.getRoot() == 1 && intVal(params.get("disable"), 0) == 1) {
                return ResponseResult.fail(400, "超级管理员不能设为停用");
            }
            if (params.containsKey("disable")) {
                admin.setDisable(intVal(params.get("disable"), admin.getDisable()));
                admin.setUpdateTime(currentTs());
                mallAdminMapper.updateById(admin);
            }
        }
        return ResponseResult.success();
    }

    @Transactional
    public ResponseResult delete(Map<String, Object> params) {
        Integer id = intVal(params.get("id"));
        if (id == null) {
            return ResponseResult.fail(400, "管理员ID不能为空");
        }
        if (id.equals(1)) {
            return ResponseResult.fail(400, "不能删除超级管理员");
        }
        MallAdmin admin = findAdmin(id);
        if (admin == null) {
            return ResponseResult.fail(404, "账号已不存在");
        }
        admin.setDeleteTime(currentTs());
        mallAdminMapper.updateById(admin);
        mallAdminRoleMapper.delete(new QueryWrapper<MallAdminRole>().eq("admin_id", id));
        mallAdminDeptMapper.delete(new QueryWrapper<MallAdminDept>().eq("admin_id", id));
        mallAdminJobsMapper.delete(new QueryWrapper<MallAdminJobs>().eq("admin_id", id));
        return ResponseResult.success();
    }

    private Map<String, Object> toAdminListMap(MallAdmin admin) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", admin.getId());
        map.put("root", admin.getRoot());
        map.put("account", admin.getAccount());
        map.put("name", admin.getName());
        map.put("avatar", admin.getAvatar());
        map.put("disable", admin.getDisable());
        map.put("multipoint_login", admin.getMultipointLogin());
        map.put("login_ip", admin.getLoginIp() == null ? "" : admin.getLoginIp());
        map.put("create_time", MallPermFormatUtil.formatTime(admin.getCreateTime()));
        map.put("update_time", MallPermFormatUtil.formatTime(admin.getUpdateTime()));
        map.put("login_time", MallPermFormatUtil.formatTime(admin.getLoginTime()));

        if (admin.getRoot() != null && admin.getRoot() == 1) {
            map.put("role_id", Collections.singletonList(0));
            map.put("role_name", "系统管理员");
            map.put("dept_name", "-");
            map.put("jobs_name", "");
        } else {
            List<Integer> roleIds = selectRoleIds(admin.getId());
            map.put("role_id", roleIds);
            map.put("role_name", joinRoleNames(roleIds));
            List<Integer> deptIds = selectDeptIds(admin.getId());
            List<Integer> jobsIds = selectJobsIds(admin.getId());
            map.put("dept_name", joinDeptNames(deptIds));
            map.put("jobs_name", joinJobsNames(jobsIds));
        }
        return map;
    }

    private String joinRoleNames(List<Integer> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return "";
        }
        QueryWrapper<MallSystemRole> wrapper = new QueryWrapper<>();
        wrapper.in("id", roleIds);
        wrapper.and(w -> w.isNull("delete_time").or().eq("delete_time", 0));
        List<MallSystemRole> roles = mallSystemRoleMapper.selectList(wrapper);
        return roles.stream().map(MallSystemRole::getName).collect(Collectors.joining(","));
    }

    private List<Integer> selectRoleIds(Integer adminId) {
        List<MallAdminRole> refs = mallAdminRoleMapper.selectList(
                new QueryWrapper<MallAdminRole>().eq("admin_id", adminId));
        if (refs == null || refs.isEmpty()) {
            return Collections.emptyList();
        }
        return refs.stream().map(MallAdminRole::getRoleId).collect(Collectors.toList());
    }

    private List<Integer> selectAdminIdsByRole(Integer roleId) {
        List<MallAdminRole> refs = mallAdminRoleMapper.selectList(
                new QueryWrapper<MallAdminRole>().eq("role_id", roleId));
        if (refs == null || refs.isEmpty()) {
            return Collections.emptyList();
        }
        return refs.stream().map(MallAdminRole::getAdminId).collect(Collectors.toList());
    }

    private List<Integer> selectDeptIds(Integer adminId) {
        List<MallAdminDept> refs = mallAdminDeptMapper.selectList(
                new QueryWrapper<MallAdminDept>().eq("admin_id", adminId));
        if (refs == null || refs.isEmpty()) {
            return Collections.emptyList();
        }
        return refs.stream().map(MallAdminDept::getDeptId).collect(Collectors.toList());
    }

    private List<Integer> selectJobsIds(Integer adminId) {
        List<MallAdminJobs> refs = mallAdminJobsMapper.selectList(
                new QueryWrapper<MallAdminJobs>().eq("admin_id", adminId));
        if (refs == null || refs.isEmpty()) {
            return Collections.emptyList();
        }
        return refs.stream().map(MallAdminJobs::getJobsId).collect(Collectors.toList());
    }

    private void saveAdminDepts(Integer adminId, Object deptIdsRaw) {
        mallAdminDeptMapper.delete(new QueryWrapper<MallAdminDept>().eq("admin_id", adminId));
        for (Integer deptId : MallPermFormatUtil.intList(deptIdsRaw)) {
            if (deptId == null || deptId <= 0) {
                continue;
            }
            MallAdminDept ref = new MallAdminDept();
            ref.setAdminId(adminId);
            ref.setDeptId(deptId);
            mallAdminDeptMapper.insert(ref);
        }
    }

    private void saveAdminJobs(Integer adminId, Object jobsIdsRaw) {
        mallAdminJobsMapper.delete(new QueryWrapper<MallAdminJobs>().eq("admin_id", adminId));
        for (Integer jobsId : MallPermFormatUtil.intList(jobsIdsRaw)) {
            if (jobsId == null || jobsId <= 0) {
                continue;
            }
            MallAdminJobs ref = new MallAdminJobs();
            ref.setAdminId(adminId);
            ref.setJobsId(jobsId);
            mallAdminJobsMapper.insert(ref);
        }
    }

    private String joinDeptNames(List<Integer> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return "-";
        }
        QueryWrapper<MallDept> wrapper = new QueryWrapper<>();
        wrapper.in("id", deptIds);
        wrapper.and(w -> w.isNull("delete_time").or().eq("delete_time", 0));
        List<MallDept> depts = mallDeptMapper.selectList(wrapper);
        String names = depts.stream().map(MallDept::getName).collect(Collectors.joining(","));
        return StringUtils.hasText(names) ? names : "-";
    }

    private String joinJobsNames(List<Integer> jobsIds) {
        if (jobsIds == null || jobsIds.isEmpty()) {
            return "";
        }
        QueryWrapper<MallJobs> wrapper = new QueryWrapper<>();
        wrapper.in("id", jobsIds);
        wrapper.and(w -> w.isNull("delete_time").or().eq("delete_time", 0));
        List<MallJobs> jobs = mallJobsMapper.selectList(wrapper);
        return jobs.stream().map(MallJobs::getName).collect(Collectors.joining(","));
    }

    private void saveAdminRoles(Integer adminId, Object roleIdsRaw) {
        mallAdminRoleMapper.delete(new QueryWrapper<MallAdminRole>().eq("admin_id", adminId));
        if (adminId.equals(1)) {
            return;
        }
        List<Integer> roleIds = MallPermFormatUtil.intList(roleIdsRaw);
        for (Integer roleId : roleIds) {
            if (roleId == null || roleId <= 0) {
                continue;
            }
            MallAdminRole ref = new MallAdminRole();
            ref.setAdminId(adminId);
            ref.setRoleId(roleId);
            mallAdminRoleMapper.insert(ref);
        }
    }

    private MallAdmin findAdmin(Integer id) {
        QueryWrapper<MallAdmin> wrapper = notDeletedWrapper(new QueryWrapper<>());
        wrapper.eq("id", id).last("limit 1");
        return mallAdminMapper.selectOne(wrapper);
    }

    private boolean existsAccount(String account, Integer excludeId) {
        QueryWrapper<MallAdmin> wrapper = notDeletedWrapper(new QueryWrapper<>());
        wrapper.eq("account", account.trim());
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        wrapper.last("limit 1");
        return mallAdminMapper.selectOne(wrapper) != null;
    }

    private boolean existsName(String name, Integer excludeId) {
        QueryWrapper<MallAdmin> wrapper = notDeletedWrapper(new QueryWrapper<>());
        wrapper.eq("name", name.trim());
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        wrapper.last("limit 1");
        return mallAdminMapper.selectOne(wrapper) != null;
    }

    private QueryWrapper<MallAdmin> notDeletedWrapper(QueryWrapper<MallAdmin> wrapper) {
        wrapper.and(w -> w.isNull("delete_time").or().eq("delete_time", 0));
        return wrapper;
    }

    private String md5(String raw) {
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }

    private int currentTs() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    private String strVal(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Integer intVal(Object value) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return null;
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private Integer intVal(Object value, int defaultValue) {
        Integer parsed = intVal(value);
        return parsed == null ? defaultValue : parsed;
    }
}
