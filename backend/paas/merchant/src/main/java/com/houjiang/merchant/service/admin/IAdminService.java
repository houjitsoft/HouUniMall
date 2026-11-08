package com.houjiang.merchant.service.admin;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.system.SystemAdminCreateValidate;
import com.houjiang.merchant.validate.system.SystemAdminSearchValidate;
import com.houjiang.merchant.validate.system.SystemAdminUpInfoValidate;
import com.houjiang.merchant.validate.system.SystemAdminUpdateValidate;
import com.houjiang.merchant.vo.auth.AdminMySelfVo;
import com.houjiang.merchant.vo.system.SystemAuthAdminDetailVo;
import com.houjiang.merchant.vo.system.SystemAuthAdminListedVo;
import com.houjiang.merchant.vo.system.SystemAuthAdminSelvesVo;
import com.houjiang.merchant.common.core.PageResult;

import java.util.List;

/**
 * 系统管理员服务接口类
 */
public interface IAdminService {

    /**
     * 管理员列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<SystemAuthAdminListedVo>
     */
    PageResult<SystemAuthAdminListedVo> list(PageValidate pageValidate, SystemAdminSearchValidate searchValidate);

    /**
     * 当前管理员
     *
     * @author Amos
     * @return SystemSelfVo
     */
    SystemAuthAdminSelvesVo self(Integer adminId);

    /**
     * 管理员详情
     *
     * @author Amos
     * @param id 主键参数
     * @return SystemAuthAdminDetailVo
     */
    SystemAuthAdminDetailVo detail(Integer id);

    /**
     * 管理员新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(SystemAdminCreateValidate createValidate);

    /**
     * 管理员编辑
     *
     * @author Amos
     * @param updateValidate 参数
     * @param adminId 管理员ID
     */
    void edit(SystemAdminUpdateValidate updateValidate, Integer adminId);

    /**
     * 当前管理员更新
     *
     * @author Amos
     * @param upInfoValidate 参数
     * @param adminId 管理员ID
     */
    void upInfo(SystemAdminUpInfoValidate upInfoValidate, Integer adminId);

    /**
     * 管理员删除
     *
     * @author Amos
     * @param id 主键参数
     * @param adminId 管理员ID
     */
    void del(Integer id, Integer adminId);

    /**
     * 管理员状态切换
     *
     * @author Amos
     * @param id 主键参数
     */
    void disable(Integer id, Integer adminId);

    /**
     *  查看管理员详情
     * @param id
     * @return
     */
    AdminMySelfVo mySelf(Integer id, List<Integer> roleIds);

    /**
     * 返回导出格式
     * @return
     */
    JSONObject getExportData(PageValidate pageValidate, SystemAdminSearchValidate searchValidate);

    /**
     * 导出
     */
    String export(SystemAdminSearchValidate searchValidate);
}
