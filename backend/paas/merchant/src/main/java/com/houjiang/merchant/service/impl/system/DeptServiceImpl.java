package com.houjiang.merchant.service.impl.system;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.validation.Assert;
import com.houjiang.merchant.service.system.IDeptService;
import com.houjiang.merchant.validate.system.DeptCreateValidate;
import com.houjiang.merchant.validate.system.DeptSearchValidate;
import com.houjiang.merchant.validate.system.DeptUpdateValidate;
import com.houjiang.merchant.vo.system.DeptVo;
import com.houjiang.merchant.common.entity.admin.Admin;
import com.houjiang.merchant.common.entity.admin.Dept;
import com.houjiang.merchant.common.mapper.admin.AdminMapper;
import com.houjiang.merchant.common.mapper.admin.DeptMapper;
import com.houjiang.merchant.common.util.ListUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * 系统部门服务实现类
 */
@Service
public class DeptServiceImpl implements IDeptService {

    @Resource
    DeptMapper deptMapper;

    @Resource
    AdminMapper adminMapper;

    /**
     * 岗位所有
     *
     * @author Amos
     * @return List<SystemAuthDeptVo>
     */
    @Override
    public JSONArray all() {
        QueryWrapper<Dept> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNull("delete_time");
        queryWrapper.orderByDesc(Arrays.asList("sort", "id"));
        queryWrapper.select(Dept.class, info ->
                !info.getColumn().equals("delete_time"));


        List<Dept> systemAuthDeptList = deptMapper.selectList(queryWrapper);

        List<DeptVo> list = new LinkedList<>();
        for (Dept systemAuthDept : systemAuthDeptList) {
            DeptVo vo = new DeptVo();
            BeanUtils.copyProperties(systemAuthDept, vo);
            vo.setStatusDesc(systemAuthDept.getStatus() != null && systemAuthDept.getStatus().intValue() == 1 ? "正常" : "禁用");
            vo.setCreateTime(TimeUtils.timestampToDate(systemAuthDept.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(systemAuthDept.getUpdateTime()));
            list.add(vo);
        }

        JSONArray jsonArray = JSONArray.parseArray(JSONArray.toJSONString(list));
        return ListUtils.listToTree(jsonArray, "id", "pid", "children");
    }

    /**
     *  部门列表
     *
     * @author Amos
     * @param searchValidate 搜索参数
     * @return JSONArray
     */
    @Override
    public JSONArray list(DeptSearchValidate searchValidate) {
        QueryWrapper<Dept> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNull("delete_time");
        queryWrapper.orderByDesc(Arrays.asList("sort", "id"));
        queryWrapper.select(Dept.class, info ->
                        !info.getColumn().equals("delete_time"));

        deptMapper.setSearch(queryWrapper, searchValidate, new String[]{
                "like:name:str",
                "=:status@status:int"
        });

        List<Dept> systemAuthDeptList = deptMapper.selectList(queryWrapper);

        List<DeptVo> list = new LinkedList<>();
        for (Dept systemAuthDept : systemAuthDeptList) {
            DeptVo vo = new DeptVo();
            BeanUtils.copyProperties(systemAuthDept, vo);
            vo.setStatusDesc(systemAuthDept.getStatus() != null && systemAuthDept.getStatus().intValue() == 1 ? "正常" : "禁用");
            vo.setCreateTime(TimeUtils.timestampToDate(systemAuthDept.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(systemAuthDept.getUpdateTime()));
            list.add(vo);
        }

        JSONArray jsonArray = JSONArray.parseArray(JSONArray.toJSONString(list));
        return ListUtils.listToTree(jsonArray, "id", "pid", "children");
    }

    /**
     * 部门详情
     *
     * @author Amos
     * @param id 主键
     * @return SystemDeptVo
     */
    @Override
    public DeptVo detail(Integer id) {
        Dept systemAuthDept = deptMapper.selectOne(
                new QueryWrapper<Dept>()
                        .select(Dept.class, info ->
                                        !info.getColumn().equals("delete_time"))
                        .eq("id", id)
                        .isNull("delete_time")
                        .last("limit 1"));

        Assert.notNull(systemAuthDept, "部门已不存在!");

        DeptVo vo  = new DeptVo();
        BeanUtils.copyProperties(systemAuthDept, vo);
        vo.setCreateTime(TimeUtils.timestampToDate(systemAuthDept.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(systemAuthDept.getUpdateTime()));

        return vo;
    }

    /**
     * 部门新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    @Override
    public void add(DeptCreateValidate createValidate) {
        if (createValidate.getPid().equals(0)) {
            Dept systemAuthDept = deptMapper.selectOne(
                    new QueryWrapper<Dept>()
                            .select("id,pid,name")
                            .eq("pid", 0)
                            .isNull("delete_time")
                            .last("limit 1"));

            Assert.isNull(systemAuthDept, "顶级部门只允许有一个");
        }

        Dept model = new Dept();
        model.setPid(createValidate.getPid());
        model.setName(createValidate.getName());
        model.setLeader(createValidate.getLeader());
        model.setMobile(createValidate.getMobile());
        model.setSort(createValidate.getSort());
        model.setStatus(createValidate.getStatus());
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        deptMapper.insert(model);
    }

    /**
     * 部门编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    @Override
    public void edit(DeptUpdateValidate updateValidate) {
        Dept model = deptMapper.selectOne(
                new QueryWrapper<Dept>()
                        .select(Dept.class, info ->
                                        !info.getColumn().equals("delete_time"))
                        .eq("id", updateValidate.getId())
                        .isNull("delete_time")
                        .last("limit 1"));

        Assert.notNull(model, "部门不存在");
        Assert.isFalse((model.getPid().equals(0) && updateValidate.getPid()>0), "顶级部门不能修改上级");
        Assert.isFalse(updateValidate.getId().equals(updateValidate.getPid()), "上级部门不能是自己");

        model.setPid(updateValidate.getPid());
        model.setName(updateValidate.getName());
        model.setLeader(updateValidate.getLeader());
        model.setMobile(updateValidate.getMobile());
        model.setSort(updateValidate.getSort());
        model.setStatus(updateValidate.getStatus());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        deptMapper.updateById(model);
    }

    /**
     * 部门删除
     *
     * @author Amos
     * @param id 主键
     */
    @Override
    public void del(Integer id) {
        Dept model = deptMapper.selectOne(
                new QueryWrapper<Dept>()
                        .select("id,pid,name")
                        .eq("id", id)
                        .isNull("delete_time")
                        .last("limit 1"));

        Assert.notNull(model, "部门不存在");
        Assert.isFalse((model.getPid() == 0), "顶级部门不能删除");

        Dept pModel = deptMapper.selectOne(
                new QueryWrapper<Dept>()
                        .select("id,pid,name")
                        .eq("pid", id)
                        .isNull("delete_time")
                        .last("limit 1"));

        Assert.isNull(pModel, "请先删除子级部门");

        List<Admin> admins = adminMapper.getByDept(id);

        Assert.isTrue(admins.isEmpty(), "该部门已被管理员使用,请先移除");

        model.setDeleteTime(System.currentTimeMillis() / 1000);
        deptMapper.updateById(model);
    }

}