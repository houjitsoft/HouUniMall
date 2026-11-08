package com.houjiang.mall.service.org;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.dto.auth.MallPageResult;
import com.houjiang.mall.entity.org.MallAdminJobs;
import com.houjiang.mall.entity.org.MallJobs;
import com.houjiang.mall.mapper.org.MallAdminJobsMapper;
import com.houjiang.mall.mapper.org.MallJobsMapper;
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
public class MallOrgJobsService {

    private final MallJobsMapper mallJobsMapper;
    private final MallAdminJobsMapper mallAdminJobsMapper;

    public ResponseResult all(Map<String, Object> params) {
        QueryWrapper<MallJobs> wrapper = buildSearchWrapper(params);
        wrapper.orderByDesc("sort").orderByAsc("id");
        List<MallJobs> jobs = mallJobsMapper.selectList(wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (MallJobs job : jobs) {
            list.add(toJobsMap(job));
        }
        return ResponseResult.success(list);
    }

    public ResponseResult lists(Map<String, Object> params) {
        int pageNo = MallPermFormatUtil.pageNo(params);
        int pageSize = MallPermFormatUtil.pageSize(params);
        QueryWrapper<MallJobs> wrapper = buildSearchWrapper(params);
        wrapper.orderByDesc("sort").orderByAsc("id");
        Page<MallJobs> page = mallJobsMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (MallJobs job : page.getRecords()) {
            list.add(toJobsMap(job));
        }
        return ResponseResult.success(MallPageResult.of(page, list));
    }

    public ResponseResult detail(Integer id) {
        MallJobs job = findJobs(id);
        if (job == null) {
            return ResponseResult.fail(404, "岗位已不存在");
        }
        return ResponseResult.success(toJobsMap(job));
    }

    public ResponseResult add(Map<String, Object> params) {
        String name = strVal(params.get("name"));
        String code = strVal(params.get("code"));
        if (!StringUtils.hasText(name) || !StringUtils.hasText(code)) {
            return ResponseResult.fail(400, "岗位名称和编码不能为空");
        }
        if (existsCode(code, null)) {
            return ResponseResult.fail(400, "岗位编码已存在");
        }
        int now = currentTs();
        MallJobs job = new MallJobs();
        job.setName(name.trim());
        job.setCode(code.trim());
        job.setSort(intVal(params.get("sort"), 0));
        job.setStatus(intVal(params.get("status"), 1));
        job.setRemark(strVal(params.get("remark")));
        job.setCreateTime(now);
        job.setUpdateTime(now);
        job.setDeleteTime(0);
        mallJobsMapper.insert(job);
        return ResponseResult.success();
    }

    public ResponseResult edit(Map<String, Object> params) {
        Integer id = intVal(params.get("id"));
        if (id == null) {
            return ResponseResult.fail(400, "岗位ID不能为空");
        }
        MallJobs job = findJobs(id);
        if (job == null) {
            return ResponseResult.fail(404, "岗位已不存在");
        }
        if (params.containsKey("name")) {
            job.setName(strVal(params.get("name")).trim());
        }
        if (params.containsKey("code")) {
            String code = strVal(params.get("code")).trim();
            if (existsCode(code, id)) {
                return ResponseResult.fail(400, "岗位编码已存在");
            }
            job.setCode(code);
        }
        if (params.containsKey("sort")) {
            job.setSort(intVal(params.get("sort"), job.getSort()));
        }
        if (params.containsKey("status")) {
            job.setStatus(intVal(params.get("status"), job.getStatus()));
        }
        if (params.containsKey("remark")) {
            job.setRemark(strVal(params.get("remark")));
        }
        job.setUpdateTime(currentTs());
        mallJobsMapper.updateById(job);
        return ResponseResult.success();
    }

    public ResponseResult delete(Map<String, Object> params) {
        Integer id = intVal(params.get("id"));
        if (id == null) {
            return ResponseResult.fail(400, "岗位ID不能为空");
        }
        MallJobs job = findJobs(id);
        if (job == null) {
            return ResponseResult.fail(404, "岗位已不存在");
        }
        Long used = mallAdminJobsMapper.selectCount(new QueryWrapper<MallAdminJobs>().eq("jobs_id", id));
        if (used != null && used > 0) {
            return ResponseResult.fail(400, "该岗位已被管理员使用,请先移除");
        }
        job.setDeleteTime(currentTs());
        mallJobsMapper.updateById(job);
        return ResponseResult.success();
    }

    private QueryWrapper<MallJobs> buildSearchWrapper(Map<String, Object> params) {
        QueryWrapper<MallJobs> wrapper = notDeletedWrapper(new QueryWrapper<>());
        String name = strVal(params.get("name"));
        String code = strVal(params.get("code"));
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name.trim());
        }
        if (StringUtils.hasText(code)) {
            wrapper.like("code", code.trim());
        }
        Object status = params.get("status");
        if (status != null && StringUtils.hasText(String.valueOf(status))) {
            wrapper.eq("status", Integer.parseInt(String.valueOf(status)));
        }
        return wrapper;
    }

    private Map<String, Object> toJobsMap(MallJobs job) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", job.getId());
        map.put("name", job.getName());
        map.put("code", job.getCode());
        map.put("sort", job.getSort());
        map.put("status", job.getStatus());
        map.put("status_desc", job.getStatus() != null && job.getStatus() == 1 ? "正常" : "禁用");
        map.put("remark", job.getRemark() == null ? "" : job.getRemark());
        map.put("create_time", MallPermFormatUtil.formatTime(job.getCreateTime()));
        map.put("update_time", MallPermFormatUtil.formatTime(job.getUpdateTime()));
        return map;
    }

    private boolean existsCode(String code, Integer excludeId) {
        QueryWrapper<MallJobs> wrapper = notDeletedWrapper(new QueryWrapper<>());
        wrapper.eq("code", code.trim());
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        wrapper.last("limit 1");
        return mallJobsMapper.selectOne(wrapper) != null;
    }

    private MallJobs findJobs(Integer id) {
        QueryWrapper<MallJobs> wrapper = notDeletedWrapper(new QueryWrapper<>());
        wrapper.eq("id", id).last("limit 1");
        return mallJobsMapper.selectOne(wrapper);
    }

    private QueryWrapper<MallJobs> notDeletedWrapper(QueryWrapper<MallJobs> wrapper) {
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
}
