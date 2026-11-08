package com.houjiang.merchant.service.system;

import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.system.SystemRoleCreateValidate;
import com.houjiang.merchant.validate.system.SystemRoleUpdateValidate;
import com.houjiang.merchant.vo.system.SystemAuthRoleVo;
import com.houjiang.merchant.common.core.PageResult;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 系统角色服务接口类
 */
public interface ISystemRoleService {

    /**
     * 角色所有
     *
     * @author Amos
     * @return List<SystemAuthRoleVo>
     */
    List<SystemAuthRoleVo> all();

    /**
     * 角色列表
     *
     * @author Amos
     * @param pageValidate 参数
     * @return PageResult<SysRoleListVo>
     */
    PageResult<SystemAuthRoleVo> list(@Validated PageValidate pageValidate);

    /**
     * 角色详情
     *
     * @author Amos
     * @param id 主键参数
     * @return SysRole
     */
    SystemAuthRoleVo detail(Integer id);

    /**
     * 角色新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(SystemRoleCreateValidate createValidate);

    /**
     * 角色更新
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(SystemRoleUpdateValidate updateValidate);

    /**
     * 角色删除
     *
     * @author Amos
     * @param id 主键参数
     */
    void del(Integer id);

    /**
     * 根据adminID 返回角色名称
     * @param adminId
     */
    List<String> getRoleNameByAdminId(Integer adminId);

}
