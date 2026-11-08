package com.houjiang.merchant.service.impl.admin;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.merchant.common.validation.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.houjiang.merchant.service.admin.*;
import com.houjiang.merchant.service.system.ISystemMenuService;
import com.houjiang.merchant.service.system.ISystemRoleMenuService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.system.SystemAdminCreateValidate;
import com.houjiang.merchant.validate.system.SystemAdminSearchValidate;
import com.houjiang.merchant.validate.system.SystemAdminUpInfoValidate;
import com.houjiang.merchant.validate.system.SystemAdminUpdateValidate;
import com.houjiang.merchant.vo.auth.AdminMySelfVo;
import com.houjiang.merchant.vo.auth.AuthMySelfVo;
import com.houjiang.merchant.vo.system.*;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.admin.Admin;
import com.houjiang.merchant.common.entity.admin.Dept;
import com.houjiang.merchant.common.entity.system.Jobs;
import com.houjiang.merchant.common.entity.system.SystemMenu;
import com.houjiang.merchant.common.entity.system.SystemRole;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.admin.AdminMapper;
import com.houjiang.merchant.common.mapper.admin.DeptMapper;
import com.houjiang.merchant.common.mapper.admin.JobsMapper;
import com.houjiang.merchant.common.mapper.system.SystemMenuMapper;
import com.houjiang.merchant.common.mapper.system.SystemRoleMapper;
import com.houjiang.merchant.common.util.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.*;

/**
 * 系统管理员服务实现类
 */
@Service
public class AdminServiceImpl implements IAdminService {

    @Resource
    AdminMapper systemAuthAdminMapper;

    @Resource
    SystemMenuMapper systemAuthMenuMapper;

    @Resource
    SystemRoleMapper systemAuthRoleMapper;

    @Resource
    ISystemRoleMenuService iSystemRoleMenuService;
    @Resource
    IAdminRoleService iAdminRoleService;
    @Resource
    IAdminDeptService iAdminDeptService;
    @Resource
    IAdminJobsService iAdminJobsService;

    @Resource
    JobsMapper jobsMapper;

    @Resource
    DeptMapper deptMapper;

    @Resource
    IAuthService iAuthService;

    @Resource
    ISystemMenuService systemMenuService;

    /**
     * 管理员列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<SystemAuthAdminListedVo>
     */
    @Override
    public PageResult<SystemAuthAdminListedVo> list(PageValidate pageValidate, SystemAdminSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        MPJQueryWrapper<Admin> mpjQueryWrapper = new MPJQueryWrapper<>();
        mpjQueryWrapper.select("distinct t.id,t.account,t.name,t.avatar," +
                "t.multipoint_login," +
                "t.disable,t.login_ip,t.login_time,t.create_time,t.update_time")
                .leftJoin("hou_merchant_admin_role lar ON lar.admin_id = t.id")
            .isNull("t.delete_time")
            .orderByDesc(Arrays.asList("t.id"));


        systemAuthAdminMapper.setSearch(mpjQueryWrapper, searchValidate, new String[]{
                "like:account:str",
                "like:name:str"
        });

        if (StringUtils.isNotNull(searchValidate.getRole_id())) {
            mpjQueryWrapper.in("lar.role_id", searchValidate.getRole_id());
        }

        IPage<SystemAuthAdminListedVo> iPage = systemAuthAdminMapper.selectJoinPage(
                new Page<>(page, limit),
                SystemAuthAdminListedVo.class,
                mpjQueryWrapper);

        for (SystemAuthAdminListedVo vo : iPage.getRecords()) {
            if (vo.getId().equals(1)) {
                vo.setRoleName("系统管理员");
                vo.setDeptName("-");
            } else {

                List<SystemRole> roles = systemAuthRoleMapper.getByAdminId(vo.getId());
                List<Integer> roleIds = new ArrayList<>();
                List<String> roleNames = new ArrayList<>();
                if (!roles.isEmpty()) {
                    roles.forEach(item-> {
                        roleIds.add(item.getId());
                        roleNames.add(item.getName());

                    });
                    vo.setRoleId(roleIds);
                    vo.setRoleName(StringUtils.join(roleNames, ","));
                }


                List<Jobs> jobs = jobsMapper.getByAdminId(vo.getId());
                List<Integer> jobsId = new ArrayList<>();
                List<String> jobsNames = new ArrayList<>();
                if (!jobs.isEmpty()) {
                    jobs.forEach(item-> {
                        jobsId.add(item.getId());
                        jobsNames.add(item.getName());
                    });
                    vo.setJobsId(jobsId);
                    vo.setJobsName(StringUtils.join(jobsNames, ","));
                }


                List<Dept> depts = deptMapper.getByAdminId(vo.getId());
                List<Integer> deptIds = new ArrayList<>();
                List<String> deptNames = new ArrayList<>();
                if (!depts.isEmpty()) {
                    depts.forEach(item-> {
                        deptIds.add(item.getId());
                        deptNames.add(item.getName());
                    });
                    vo.setDeptId(deptIds);
                    vo.setDeptName(StringUtils.join(deptNames, ","));

                }
            }


            vo.setDisableDesc(vo.getDisable() != null && vo.getDisable().equals(0) ? "启用" : "禁用");
            vo.setAvatar(UrlUtils.toShopAbsoluteUrl(vo.getAvatar()));
            vo.setCreateTime(TimeUtils.timestampToDate(vo.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(vo.getUpdateTime()));
            vo.setLoginTime(TimeUtils.timestampToDate(vo.getLoginTime()));
        }

        return PageResult.iPageHandle(iPage);
    }

    /**
     * 当前管理员
     *
     * @author Amos
     * @param adminId 管理员ID
     * @return SystemAuthAdminSelvesVo
     */
    @Override
    public SystemAuthAdminSelvesVo self(Integer adminId) {
        // 管理员信息
        Admin sysAdmin = systemAuthAdminMapper.selectOne(new QueryWrapper<Admin>()
                .select(Admin.class, info->
                    !info.getColumn().equals("password") &&
                    !info.getColumn().equals("delete_time"))
                .isNull("delete_time")
                .eq("id", adminId)
                .last("limit 1"));

        SystemAuthAdminInformVo systemAuthAdminInformVo = new SystemAuthAdminInformVo();
        BeanUtils.copyProperties(sysAdmin, systemAuthAdminInformVo);
        systemAuthAdminInformVo.setAvatar(UrlUtils.toShopAbsoluteUrl(sysAdmin.getAvatar()));
        systemAuthAdminInformVo.setUpdateTime(TimeUtils.timestampToDate(sysAdmin.getUpdateTime()));
        systemAuthAdminInformVo.setCreateTime(TimeUtils.timestampToDate(sysAdmin.getCreateTime()));
        systemAuthAdminInformVo.setLastLoginTime(TimeUtils.timestampToDate(sysAdmin.getLoginTime()));

        // 角色权限
        List<String> auths = new LinkedList<>();
        if (adminId > 1) {
            List<Integer> roleIds = new ArrayList<Integer>();
            List<Integer> menuIds = iSystemRoleMenuService.selectMenuIdsByRoleId(roleIds);
            if (menuIds.size() > 0) {
                List<SystemMenu> systemAuthMenus = systemAuthMenuMapper.selectList(new QueryWrapper<SystemMenu>()
                        .eq("is_disable", 0)
                        .in("id", menuIds)
                        .in("menu_type", Arrays.asList("C", "A"))
                        .orderByAsc(Arrays.asList("menu_sort", "id")));

                // 处理权限
                for (SystemMenu item : systemAuthMenus) {
                    if (StringUtils.isNotNull(item.getPerms()) && StringUtils.isNotEmpty(item.getPerms())) {
                        auths.add(item.getPerms().trim());
                    }
                }
            }
            // 没有权限
            if (auths.size() <= 0) {
                auths.add("");
            }
        } else {
            // 所有权限
            auths.add("*");
        }

        // 返回数据
        SystemAuthAdminSelvesVo vo = new SystemAuthAdminSelvesVo();
        vo.setUser(systemAuthAdminInformVo);
        vo.setPermissions(auths);
        return vo;
    }

    /**
     * 管理员详细
     *
     * @author Amos
     * @param id 主键
     * @return SystemAuthAdminDetailVo
     */
    @Override
    public SystemAuthAdminDetailVo detail(Integer id) {
        Admin sysAdmin = systemAuthAdminMapper.selectOne(new QueryWrapper<Admin>()
                .eq("id", id)
                .isNull("delete_time")
                .last("limit 1"));

        Assert.notNull(sysAdmin, "账号已不存在！");

        SystemAuthAdminDetailVo vo = new SystemAuthAdminDetailVo();
        BeanUtils.copyProperties(sysAdmin, vo);
        vo.setAvatar(UrlUtils.toShopAbsoluteUrl(sysAdmin.getAvatar()));
        vo.setCreateTime(TimeUtils.timestampToDate(sysAdmin.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(sysAdmin.getUpdateTime()));
        vo.setLoginTime(TimeUtils.timestampToDate(sysAdmin.getLoginTime()));
        vo.setRoleId(iAdminRoleService.getRoleIdAttr(sysAdmin.getId()));
        vo.setJobsId(iAdminJobsService.getJobIdAttr(sysAdmin.getId()));
        vo.setDeptId(iAdminDeptService.getDeptIdAttr(sysAdmin.getId()));
        return vo;
    }

    /**
     * 管理员新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    @Override
    public void add(SystemAdminCreateValidate createValidate) {
        String[] field = {"id", "account", "name"};
        Assert.isNull(systemAuthAdminMapper.selectOne(new QueryWrapper<Admin>()
                .select(field)
                .isNull("delete_time")
                .eq("account", createValidate.getAccount())
                .last("limit 1")), "账号已存在换一个吧！");

        Assert.isNull(systemAuthAdminMapper.selectOne(new QueryWrapper<Admin>()
                .select(field)
                .isNull("delete_time")
                .eq("name", createValidate.getName())
                .last("limit 1")), "昵称已存在换一个吧！");

        String pwd  = ToolUtils.makePassword(createValidate.getPassword().trim());

        String createAvatar  = createValidate.getAvatar();
        String defaultAvatar = "/api/static/backend_avatar.png";
        String avatar = StringUtils.isNotEmpty(createValidate.getAvatar()) ? UrlUtils.toShopRelativeUrl(createAvatar) : defaultAvatar;

        Admin model = new Admin();
        model.setAccount(createValidate.getAccount());
        model.setName(createValidate.getName());
        model.setAvatar(avatar);
        model.setPassword(pwd);
        model.setMultipointLogin(createValidate.getMultipointLogin());
        model.setDisable(createValidate.getDisable());
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        systemAuthAdminMapper.insert(model);

        List<Integer> deptIds = createValidate.getDeptId();
        List<Integer> jobsIds = createValidate.getJobsId();
        List<Integer> roleIds = createValidate.getRoleId();

        this.iAdminDeptService.batchInsert(model.getId(), deptIds);

        this.iAdminJobsService.batchInsert(model.getId(), jobsIds);

        this.iAdminRoleService.batchInsert(model.getId(), roleIds);

    }

    /**
     * 管理员更新
     *
     * @author Amos
     * @param updateValidate 参数
     * @param adminId 管理员ID
     */
    @Override
    public void edit(SystemAdminUpdateValidate updateValidate, Integer adminId) {
        if (!adminId.equals(1) && updateValidate.getId().equals(1)) {
            throw new OperateException("您无权限编辑系统管理员!");
        }

        boolean isEditInfo = false;
        if (updateValidate.getPassword() != null) {
            isEditInfo = true;
        }


        String[] field = {"id", "account", "name"};
        Assert.notNull(systemAuthAdminMapper.selectOne(new QueryWrapper<Admin>()
                .select(field)
                .eq("id", updateValidate.getId())
                .isNull("delete_time")
                .last("limit 1")), "账号不存在了!");

        Assert.isNull(systemAuthAdminMapper.selectOne(new QueryWrapper<Admin>()
                .select(field)
                .isNull("delete_time")
                .eq("account", updateValidate.getAccount())
                .ne("id", updateValidate.getId())
                .last("limit 1")), "账号已存在换一个吧!");

        Assert.isNull(systemAuthAdminMapper.selectOne(new QueryWrapper<Admin>()
                .select(field)
                .isNull("delete_time")
                .eq("name", updateValidate.getName())
                .ne("id", updateValidate.getId())
                .last("limit 1")), "昵称已存在换一个吧!");

        Admin admin = systemAuthAdminMapper.selectOne(new QueryWrapper<Admin>().eq("id", updateValidate.getId()).isNull("delete_time"));

        if (admin.getRoot().equals(1) && updateValidate.getDisable().equals(1)) {
            throw new OperateException("超级管理员不能设为停用");
        }

        Admin model = new Admin();

        model.setId(updateValidate.getId());

        if (isEditInfo) {

            model.setName(updateValidate.getName());
            model.setAvatar(UrlUtils.toShopRelativeUrl(updateValidate.getAvatar()));
            model.setMultipointLogin(updateValidate.getMultipointLogin());
            model.setUpdateTime(System.currentTimeMillis() / 1000);

            if (!updateValidate.getId().equals(1)) {
                model.setAccount(updateValidate.getAccount());
            }

            if (StringUtils.isNotNull(updateValidate.getPassword()) && StringUtils.isNotEmpty(updateValidate.getPassword())) {
                String pwd = ToolUtils.makePassword(updateValidate.getPassword().trim());
                model.setPassword(pwd);
            }
        } else {
            model.setDisable(updateValidate.getDisable());
        }

        systemAuthAdminMapper.updateById(model);

        if (isEditInfo) {

            if (StringUtils.isNotNull(updateValidate.getPassword()) && StringUtils.isNotEmpty(updateValidate.getPassword())) {
                // TODO: 瀵嗙爜淇敼鍚庡簲韪㈠嚭鐢ㄦ埛锛岄渶瑕侀€氳繃Redis娓呴櫎token瀹炵幇
            }

            List<Integer> deptIds = updateValidate.getDeptId();
            List<Integer> jobsIds = updateValidate.getJobsId();
            List<Integer> roleIds = updateValidate.getRoleId();

            this.iAdminDeptService.batchInsert(updateValidate.getId(), deptIds);

            this.iAdminJobsService.batchInsert(updateValidate.getId(), jobsIds);

            this.iAdminRoleService.batchInsert(updateValidate.getId(), roleIds);
        }



    }

    /**
     * 当前管理员更新
     *
     * @author Amos
     * @param upInfoValidate 参数
     */
    @Override
    public void upInfo(SystemAdminUpInfoValidate upInfoValidate, Integer adminId) {
        Admin model = systemAuthAdminMapper.selectOne(new QueryWrapper<Admin>()
                .select("id,username,nickname,password,salt")
                .eq("id", adminId)
                .isNull("delete_time")
                .last("limit 1"));

        Assert.notNull(model, "账号不存在了!");

        String createAvatar  = upInfoValidate.getAvatar();
        String defaultAvatar = "/api/static/backend_avatar.png";
        String avatar = StringUtils.isNotEmpty(upInfoValidate.getAvatar()) ? UrlUtils.toShopRelativeUrl(createAvatar) : defaultAvatar;

        model.setAvatar(avatar);
        model.setName(upInfoValidate.getNickname());
        model.setUpdateTime(System.currentTimeMillis() / 1000);

        if (StringUtils.isNotNull(upInfoValidate.getPassword()) && StringUtils.isNotEmpty(upInfoValidate.getPassword())) {
            String currPassword = ToolUtils.makePassword(upInfoValidate.getCurrPassword());
            Assert.isFalse(!currPassword.equals(model.getPassword()), "当前密码不正确!");
            if (upInfoValidate.getPassword().length() > 64) {
                throw new OperateException("密码不能超出64个字符");
            }
            String pwd    = ToolUtils.makePassword( upInfoValidate.getPassword().trim());
            model.setPassword(pwd);
        }

        systemAuthAdminMapper.updateById(model);
        if (StringUtils.isNotNull(upInfoValidate.getPassword()) && StringUtils.isNotEmpty(upInfoValidate.getPassword())) {
            // TODO: 瀵嗙爜淇敼鍚庡簲韪㈠嚭鐢ㄦ埛锛岄渶瑕侀€氳繃Redis娓呴櫎token瀹炵幇
        }
    }

    /**
     * 管理员删除
     *
     * @author Amos
     * @param id 主键
     * @param adminId 管理员ID
     */
    @Override
    public void del(Integer id, Integer adminId) {
        String[] field = {"id", "account", "name"};
        Assert.notNull(systemAuthAdminMapper.selectOne(new QueryWrapper<Admin>()
                .select(field)
                .eq("id", id)
                .isNull("delete_time")
                .last("limit 1")), "账号已不存在!");

        Assert.isFalse(id.equals(1), "系统管理员不允许删除!");
        Assert.isFalse(id.equals(adminId) , "不能删除自己!");

        Admin model = new Admin();
        model.setId(id);
        model.setDeleteTime(System.currentTimeMillis() / 1000);
        systemAuthAdminMapper.updateById(model);

        this.iAdminRoleService.deleteByAdminId(id);
        this.iAdminJobsService.deleteByAdminId(id);
        this.iAdminDeptService.deleteByAdminId(id);
        // TODO: 搴旇涪鍑虹敤鎴凤紝闇€瑕侀€氳繃Redis娓呴櫎token瀹炵幇
    }

    /**
     * 管理员状态切换
     *
     * @author Amos
     * @param id 主键参数
     * @param adminId 管理员ID
     */
    @Override
    public void disable(Integer id, Integer adminId) {
        Admin systemAuthAdmin = systemAuthAdminMapper.selectOne(new QueryWrapper<Admin>()
                .select("id,username,nickname,is_disable")
                .eq("id", id)
                .isNull("delete_time")
                .last("limit 1"));

        Assert.notNull(systemAuthAdmin, "账号已不存在!");
        Assert.isFalse(id.equals(adminId) , "不能禁用自己!");

        Integer disable = systemAuthAdmin.getDisable() == 1 ? 0 : 1;
        systemAuthAdmin.setDisable(disable);
        systemAuthAdmin.setUpdateTime(TimeUtils.timestamp());
        systemAuthAdminMapper.updateById(systemAuthAdmin);

        if (disable.equals(1)) {
            // TODO: 搴旇涪鍑虹敤鎴凤紝闇€瑕侀€氳繃Redis娓呴櫎token瀹炵幇
        }
    }

    @Override
    public AdminMySelfVo mySelf(Integer id, List<Integer> roleIds) {
        AdminMySelfVo ret = new AdminMySelfVo();
        // 当前管理员角色拥有的菜单
        ret.setMenu(systemMenuService.selectMenuByRoleId(roleIds));
        // 当前管理员橘色拥有的按钮权限
        ret.setPermissions(iAuthService.getBtnAuthByRoleId(id));
        SystemAuthAdminDetailVo admin = detail(id);
        AuthMySelfVo user = new AuthMySelfVo();
        BeanUtils.copyProperties(admin, user);
        user.setDeptId(admin.getDeptId());
        user.setJobsId(admin.getJobsId());
        user.setRoleId(admin.getRoleId());
        ret.setUser(user);
        return ret;
    }

    @Override
    public JSONObject getExportData(PageValidate pageValidate, SystemAdminSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();
        PageResult<SystemAuthAdminListedVo> userVoPageResult = this.list(pageValidate, searchValidate);
        JSONObject ret  = ToolUtils.getExportData(userVoPageResult.getCount(), limit, searchValidate.getPage_start(), searchValidate.getPage_end(),"管理员记录列表");
        return ret;
    }

    @Override
    public String export(SystemAdminSearchValidate searchValidate) {
        PageValidate pageValidate = new PageValidate();
        if (StringUtils.isNotNull(searchValidate.getPage_start())) {
            pageValidate.setPage_no(searchValidate.getPage_start());
        } else {
            pageValidate.setPage_no(1);
        }

        if (StringUtils.isNotNull(searchValidate.getPage_end()) && StringUtils.isNotNull(searchValidate.getPage_size())) {
            pageValidate.setPage_size(searchValidate.getPage_end() * searchValidate.getPage_size());
        } else {
            pageValidate.setPage_size(20);
        }
        Boolean isAll = StringUtils.isNull(searchValidate.getPage_type()) || searchValidate.getPage_type().equals(0) ? true : false;
        List<SystemAuthAdminListedExportVo> excellist = this.getExcellist(isAll, pageValidate, searchValidate);
        String fileName = StringUtils.isNull(searchValidate.getFile_name()) ? ToolUtils.makeUUID() : searchValidate.getFile_name();
        String folderPath = "/excel/export/"+ TimeUtils.timestampToDay(System.currentTimeMillis() / 1000) +"/" ;
        String path =  folderPath +  fileName +".xlsx";
        String filePath =  YmlUtils.get("merchant.upload-directory") + path;
        File folder = new File(YmlUtils.get("merchant.upload-directory") + folderPath);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new OperateException("创建文件夹失败");
            }
        }
        EasyExcel.write(filePath)
                .head(SystemAuthAdminListedExportVo.class)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet("管理员记录")
                .doWrite(excellist);
        return UrlUtils.toShopAbsoluteUrl(path);
    }

    private List<SystemAuthAdminListedExportVo> getExcellist(boolean isAll, PageValidate pageValidate, SystemAdminSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        MPJQueryWrapper<Admin> mpjQueryWrapper = new MPJQueryWrapper<>();
        mpjQueryWrapper.select("distinct t.id,t.account,t.name,t.avatar," +
                        "t.multipoint_login," +
                        "t.disable,t.login_ip,t.login_time,t.create_time,t.update_time")
                .leftJoin("hou_merchant_admin_role lar ON lar.admin_id = t.id")
                .isNull("t.delete_time")
                .orderByDesc(Arrays.asList("t.id"));


        systemAuthAdminMapper.setSearch(mpjQueryWrapper, searchValidate, new String[]{
                "like:account:str",
                "like:name:str"
        });

        if (StringUtils.isNotNull(searchValidate.getRole_id())) {
            mpjQueryWrapper.in("lar.role_id", searchValidate.getRole_id());
        }

        List<SystemAuthAdminListedExportVo> retList = new ArrayList<>();
        if (isAll) {
            retList = systemAuthAdminMapper.selectJoinList(SystemAuthAdminListedExportVo.class, mpjQueryWrapper);
        } else {
            IPage<SystemAuthAdminListedExportVo> iPage = systemAuthAdminMapper.selectJoinPage(
                    new Page<>(page, limit),
                    SystemAuthAdminListedExportVo.class,
                    mpjQueryWrapper);
            retList = iPage.getRecords();
        }

        for (SystemAuthAdminListedExportVo vo : retList) {
            if (vo.getId().equals(1)) {
                vo.setRoleName("系统管理员");
                vo.setDeptName("-");
            } else {

                List<SystemRole> roles = systemAuthRoleMapper.getByAdminId(vo.getId());
                List<Integer> roleIds = new ArrayList<>();
                List<String> roleNames = new ArrayList<>();
                if (!roles.isEmpty()) {
                    roles.forEach(item-> {
                        roleIds.add(item.getId());
                        roleNames.add(item.getName());

                    });
                    vo.setRoleId(roleIds);
                    vo.setRoleName(StringUtils.join(roleNames, ","));
                }


                List<Jobs> jobs = jobsMapper.getByAdminId(vo.getId());
                List<Integer> jobsId = new ArrayList<>();
                List<String> jobsNames = new ArrayList<>();
                if (!jobs.isEmpty()) {
                    jobs.forEach(item-> {
                        jobsId.add(item.getId());
                        jobsNames.add(item.getName());
                    });
                    vo.setJobsId(jobsId);
                    vo.setJobsName(StringUtils.join(jobsNames, ","));
                }


                List<Dept> depts = deptMapper.getByAdminId(vo.getId());
                List<Integer> deptIds = new ArrayList<>();
                List<String> deptNames = new ArrayList<>();
                if (!depts.isEmpty()) {
                    depts.forEach(item-> {
                        deptIds.add(item.getId());
                        deptNames.add(item.getName());
                    });
                    vo.setDeptId(deptIds);
                    vo.setDeptName(StringUtils.join(deptNames, ","));

                }
            }


            vo.setDisableDesc(vo.getDisable() != null && vo.getDisable().equals(0) ? "启用" : "禁用");
            vo.setAvatar(UrlUtils.toShopAbsoluteUrl(vo.getAvatar()));
            vo.setCreateTime(TimeUtils.timestampToDate(vo.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(vo.getUpdateTime()));
            vo.setLoginTime(TimeUtils.timestampToDate(vo.getLoginTime()));
        }
        return retList;
    }
}
