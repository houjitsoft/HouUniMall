package com.houjiang.merchant.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.merchant.common.validation.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.service.admin.IAdminRoleService;
import com.houjiang.merchant.service.system.ISystemRoleMenuService;
import com.houjiang.merchant.service.system.ISystemRoleService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.system.SystemRoleCreateValidate;
import com.houjiang.merchant.validate.system.SystemRoleUpdateValidate;
import com.houjiang.merchant.vo.system.SystemAuthRoleVo;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.admin.Admin;
import com.houjiang.merchant.common.entity.admin.AdminRole;
import com.houjiang.merchant.common.entity.system.SystemRole;
import com.houjiang.merchant.common.mapper.admin.AdminMapper;
import com.houjiang.merchant.common.mapper.system.SystemRoleMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * 系统角色服务实现类
 */
@Service
public class SystemRoleServiceImpl implements ISystemRoleService {

    @Resource
    AdminMapper adminMapper;

    @Resource
    SystemRoleMapper systemRoleMapper;

    @Resource
    ISystemRoleMenuService iSystemRoleMenuService;
    @Resource
    IAdminRoleService iAdminRoleService;

    /**
     * 角色所有
     *
     * @author Amos
     * @return List<SystemAuthRoleVo>
     */
    @Override
    public List<SystemAuthRoleVo> all() {
        QueryWrapper<SystemRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id,name,sort,create_time,update_time");
        queryWrapper.orderByDesc(Arrays.asList("sort", "id"));
        queryWrapper.isNull("delete_time");
        List<SystemRole> systemAuthRoles = systemRoleMapper.selectList(queryWrapper);

        List<SystemAuthRoleVo> list = new ArrayList<>();
        for (SystemRole systemAuthRole : systemAuthRoles) {
            SystemAuthRoleVo vo = new SystemAuthRoleVo();

            vo.setId(systemAuthRole.getId());
            vo.setName(systemAuthRole.getName());
            vo.setSort(systemAuthRole.getSort());
            vo.setCreateTime(TimeUtils.timestampToDate(systemAuthRole.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(systemAuthRole.getUpdateTime()));
            vo.setNum(0);
            vo.setDesc("");
            vo.setMenusId(Collections.EMPTY_LIST);
            list.add(vo);
        }

        return list;
    }

    /**
     * 角色列表
     *
     * @author Amos
     * @param pageValidate 参数
     * @return PageResult<SysRoleListVo>
     */
    @Override
    public PageResult<SystemAuthRoleVo> list(@Validated PageValidate pageValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<SystemRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(Arrays.asList("sort", "id"));
        queryWrapper.isNull("delete_time");

        IPage<SystemRole> iPage = systemRoleMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<SystemAuthRoleVo> list = new ArrayList<>();
        for (SystemRole systemAuthRole : iPage.getRecords()) {
            SystemAuthRoleVo vo = new SystemAuthRoleVo();
            BeanUtils.copyProperties(systemAuthRole, vo);
            vo.setMenusId(new ArrayList<>());
            vo.setNum(adminMapper.getCountByRoleId(systemAuthRole.getId()));
            vo.setCreateTime(TimeUtils.timestampToDate(systemAuthRole.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(systemAuthRole.getUpdateTime()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 角色详情
     *
     * @author Amos
     * @param id 主键参数
     * @return SysRole
     */
    @Override
    public SystemAuthRoleVo detail(Integer id) {
        SystemRole systemAuthRole = systemRoleMapper.selectOne(new QueryWrapper<SystemRole>()
                .eq("id", id)
                .last("limit 1"));

        Assert.notNull(systemAuthRole, "角色已不存在!");

        List<Integer> roleIds = new LinkedList<>();
        roleIds.add(systemAuthRole.getId());

        SystemAuthRoleVo vo = new SystemAuthRoleVo();
        BeanUtils.copyProperties(systemAuthRole, vo);
        vo.setNum(0);
        vo.setMenusId(iSystemRoleMenuService.selectMenuIdsByRoleId(roleIds));
        vo.setCreateTime(TimeUtils.timestampToDate(systemAuthRole.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(systemAuthRole.getUpdateTime()));

        return vo;
    }

    /**
     * 新增角色
     *
     * @author Amos
     * @param createValidate 参数
     */
    @Override
    @Transactional
    public void add(SystemRoleCreateValidate createValidate) {
        Assert.isNull(systemRoleMapper.selectOne(new QueryWrapper<SystemRole>()
                .select("id,name")
                .eq("name", createValidate.getName().trim()).isNull("delete_time")
                .last("limit 1")), "角色名称已存在!");

        SystemRole model = new SystemRole();
        model.setName(createValidate.getName().trim());
        model.setDesc(createValidate.getDesc());
        model.setSort(createValidate.getSort());
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        systemRoleMapper.insert(model);
//        iSystemAuthPermService.batchSaveByMenuIds(model.getId(), createValidate.getMenuIds());
    }

    /**
     * 编辑角色
     *
     * @author Amos
     * @param updateValidate 参数
     */
    @Override
    @Transactional
    public void edit(SystemRoleUpdateValidate updateValidate) {
        Assert.notNull(systemRoleMapper.selectOne(new QueryWrapper<SystemRole>()
                .select("id,name")
                .eq("id", updateValidate.getId()).isNull("delete_time")
                .last("limit 1")), "角色已不存在!");

        Assert.isNull(systemRoleMapper.selectOne(new QueryWrapper<SystemRole>()
                .select("id,name")
                .ne("id", updateValidate.getId())
                .eq("name", updateValidate.getName().trim()).isNull("delete_time")
                .last("limit 1")), "角色名称已存在!");

        SystemRole model = new SystemRole();
        model.setId(updateValidate.getId());
        model.setName(updateValidate.getName().trim());
        model.setDesc(updateValidate.getDesc());
        model.setSort(updateValidate.getSort());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        systemRoleMapper.updateById(model);

        iSystemRoleMenuService.batchDeleteByRoleId(updateValidate.getId());
        iSystemRoleMenuService.batchSaveByMenuIds(updateValidate.getId(), updateValidate.getMenuId());
    }

    /**
     * 删除角色
     *
     * @author Amos
     * @param id 主键参数
     */
    @Override
    @Transactional
    public void del(Integer id) {
        Assert.notNull(
                systemRoleMapper.selectOne(new QueryWrapper<SystemRole>()
                    .select("id", "name")
                    .eq("id", id)
                    .last("limit 1")),
                "角色已不存在!");


        List<AdminRole> adminRoles = iAdminRoleService.getAdminIdByRoleId(id);
        Assert.isTrue( adminRoles != null && adminRoles.isEmpty(),
                "角色已被管理员使用,请先移除");

        systemRoleMapper.deleteById(id);
        iSystemRoleMenuService.batchDeleteByRoleId(id);
    }

    @Override
    public List<String> getRoleNameByAdminId(Integer adminId) {
        List<String> ret = new ArrayList<>();
        Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("id", adminId).isNull("delete_time"));
        if (StringUtils.isNull(admin)) {
            return ret;
        } else {
            if (admin.getRoot().equals(1)) {
                ret.add("系统管理员");
            } else {
                List<Integer> roleIds = iAdminRoleService.getRoleIdAttr(adminId);
                if (roleIds.size() > 0) {
                    ret = getNamesByIds(roleIds);
                } else {
                    return  ret;
                }
            }
        }
        return ret;
    }

    /**
     * 根据ids 返回
     * @param ids
     * @return
     */
    private List<String> getNamesByIds(List<Integer> ids) {
        List<String> ret = new ArrayList<String>();
        List<SystemRole> adminRoleList = systemRoleMapper.selectList(new QueryWrapper<SystemRole>().in("id", ids).isNull("delete_time"));
        if (adminRoleList.size() > 0) {
            for (SystemRole item : adminRoleList) {
                ret.add(item.getName());
            }
        }
        return ret;
    }
}
