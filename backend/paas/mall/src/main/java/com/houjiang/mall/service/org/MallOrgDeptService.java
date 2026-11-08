package com.houjiang.mall.service.org;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.org.MallAdminDept;
import com.houjiang.mall.entity.org.MallDept;
import com.houjiang.mall.mapper.org.MallAdminDeptMapper;
import com.houjiang.mall.mapper.org.MallDeptMapper;
import com.houjiang.mall.util.MallPermFormatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author Amos
 */
@RequiredArgsConstructor
@Service
public class MallOrgDeptService {

    private final MallDeptMapper mallDeptMapper;
    private final MallAdminDeptMapper mallAdminDeptMapper;

    public ResponseResult all() {
        return ResponseResult.success(buildDeptTree(Collections.emptyMap()));
    }

    public ResponseResult lists(Map<String, Object> params) {
        return ResponseResult.success(buildDeptTree(params));
    }

    public ResponseResult detail(Integer id) {
        MallDept dept = findDept(id);
        if (dept == null) {
            return ResponseResult.fail(404, "部门已不存在");
        }
        return ResponseResult.success(toDeptMap(dept));
    }

    public ResponseResult add(Map<String, Object> params) {
        String name = strVal(params.get("name"));
        if (!StringUtils.hasText(name)) {
            return ResponseResult.fail(400, "部门名称不能为空");
        }
        Long pid = longVal(params.get("pid"), 0L);
        if (pid == 0) {
            QueryWrapper<MallDept> top = notDeletedWrapper(new QueryWrapper<>());
            top.eq("pid", 0).last("limit 1");
            if (mallDeptMapper.selectOne(top) != null) {
                return ResponseResult.fail(400, "顶级部门只允许有一个");
            }
        }
        int now = currentTs();
        MallDept dept = new MallDept();
        dept.setPid(pid);
        dept.setName(name.trim());
        dept.setLeader(strVal(params.get("leader")));
        dept.setMobile(strVal(params.get("mobile")));
        dept.setSort(intVal(params.get("sort"), 0));
        dept.setStatus(intVal(params.get("status"), 1));
        dept.setCreateTime(now);
        dept.setUpdateTime(now);
        dept.setDeleteTime(0);
        mallDeptMapper.insert(dept);
        return ResponseResult.success();
    }

    public ResponseResult edit(Map<String, Object> params) {
        Integer id = intVal(params.get("id"));
        if (id == null) {
            return ResponseResult.fail(400, "部门ID不能为空");
        }
        MallDept dept = findDept(id);
        if (dept == null) {
            return ResponseResult.fail(404, "部门不存在");
        }
        Long pid = longVal(params.get("pid"), dept.getPid() == null ? 0L : dept.getPid());
        if (dept.getPid() != null && dept.getPid() == 0 && pid > 0) {
            return ResponseResult.fail(400, "顶级部门不能修改上级");
        }
        if (id.longValue() == pid) {
            return ResponseResult.fail(400, "上级部门不能是自己");
        }
        if (params.containsKey("name")) {
            dept.setName(strVal(params.get("name")).trim());
        }
        dept.setPid(pid);
        if (params.containsKey("leader")) {
            dept.setLeader(strVal(params.get("leader")));
        }
        if (params.containsKey("mobile")) {
            dept.setMobile(strVal(params.get("mobile")));
        }
        if (params.containsKey("sort")) {
            dept.setSort(intVal(params.get("sort"), dept.getSort()));
        }
        if (params.containsKey("status")) {
            dept.setStatus(intVal(params.get("status"), dept.getStatus()));
        }
        dept.setUpdateTime(currentTs());
        mallDeptMapper.updateById(dept);
        return ResponseResult.success();
    }

    public ResponseResult delete(Map<String, Object> params) {
        Integer id = intVal(params.get("id"));
        if (id == null) {
            return ResponseResult.fail(400, "部门ID不能为空");
        }
        MallDept dept = findDept(id);
        if (dept == null) {
            return ResponseResult.fail(404, "部门不存在");
        }
        if (dept.getPid() != null && dept.getPid() == 0) {
            return ResponseResult.fail(400, "顶级部门不能删除");
        }
        QueryWrapper<MallDept> child = notDeletedWrapper(new QueryWrapper<>());
        child.eq("pid", id).last("limit 1");
        if (mallDeptMapper.selectOne(child) != null) {
            return ResponseResult.fail(400, "请先删除子级部门");
        }
        Long used = mallAdminDeptMapper.selectCount(new QueryWrapper<MallAdminDept>().eq("dept_id", id));
        if (used != null && used > 0) {
            return ResponseResult.fail(400, "该部门已被管理员使用,请先移除");
        }
        dept.setDeleteTime(currentTs());
        mallDeptMapper.updateById(dept);
        return ResponseResult.success();
    }

    private List<Map<String, Object>> buildDeptTree(Map<String, Object> params) {
        QueryWrapper<MallDept> wrapper = notDeletedWrapper(new QueryWrapper<>());
        String name = strVal(params.get("name"));
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name.trim());
        }
        Object status = params.get("status");
        if (status != null && StringUtils.hasText(String.valueOf(status))) {
            wrapper.eq("status", Integer.parseInt(String.valueOf(status)));
        }
        wrapper.orderByDesc("sort").orderByAsc("id");
        List<MallDept> depts = mallDeptMapper.selectList(wrapper);
        List<Map<String, Object>> flat = new ArrayList<>();
        for (MallDept dept : depts) {
            flat.add(toDeptMap(dept));
        }
        return MallPermFormatUtil.buildTree(flat);
    }

    private Map<String, Object> toDeptMap(MallDept dept) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", dept.getId());
        map.put("pid", dept.getPid() == null ? 0 : dept.getPid().intValue());
        map.put("name", dept.getName());
        map.put("leader", dept.getLeader() == null ? "" : dept.getLeader());
        map.put("mobile", dept.getMobile() == null ? "" : dept.getMobile());
        map.put("sort", dept.getSort());
        map.put("status", dept.getStatus());
        map.put("status_desc", dept.getStatus() != null && dept.getStatus() == 1 ? "正常" : "禁用");
        map.put("create_time", MallPermFormatUtil.formatTime(dept.getCreateTime()));
        map.put("update_time", MallPermFormatUtil.formatTime(dept.getUpdateTime()));
        return map;
    }

    private MallDept findDept(Integer id) {
        QueryWrapper<MallDept> wrapper = notDeletedWrapper(new QueryWrapper<>());
        wrapper.eq("id", id).last("limit 1");
        return mallDeptMapper.selectOne(wrapper);
    }

    private QueryWrapper<MallDept> notDeletedWrapper(QueryWrapper<MallDept> wrapper) {
        wrapper.and(w -> w.isNull("delete_time").or().eq("delete_time", 0));
        return wrapper;
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

    private Long longVal(Object value, long defaultValue) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return defaultValue;
        }
        return Long.parseLong(String.valueOf(value));
    }
}
