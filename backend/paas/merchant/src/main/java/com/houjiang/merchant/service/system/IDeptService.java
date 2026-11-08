package com.houjiang.merchant.service.system;

import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.validate.system.DeptCreateValidate;
import com.houjiang.merchant.validate.system.DeptSearchValidate;
import com.houjiang.merchant.validate.system.DeptUpdateValidate;
import com.houjiang.merchant.vo.system.DeptVo;

/**
 * 系统部门服务接口类
 */
public interface IDeptService {

    /**
     * 部门列表
     *
     * @author Amos
     * @param searchValidate 搜索参数
     * @return JSONArray
     */
    JSONArray list(DeptSearchValidate searchValidate);

    /**
     * 部门所有
     *
     * @author Amos
     * @return JSONArray
     */
    JSONArray all();

    /**
     * 部门详情
     *
     * @author Amos
     * @param id 主键
     * @return SysMenu
     */
    DeptVo detail(Integer id);

    /**
     * 部门新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(DeptCreateValidate createValidate);

    /**
     * 部门编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(DeptUpdateValidate updateValidate);

    /**
     * 部门删除
     *
     * @author Amos
     * @param id 主键
     */
    void del(Integer id);

}
