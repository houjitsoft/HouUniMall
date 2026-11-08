package com.houjiang.merchant.service.mall;

import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.validate.mall.MallShopDeptCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopDeptSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopDeptTopUpdateValidate;
import com.houjiang.merchant.validate.mall.MallShopDeptUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopDeptDetailVo;

/**
 * 商家部门服务接口类
 * @author Amos
 */
public interface IMallShopDeptService {

    /**
     * 商家部门列表
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallShopDeptListedVo>
     */
    JSONArray list(MallShopDeptSearchValidate searchValidate);

    /**
     * 商家部门详情
     *
     * @author Amos
     * @param id 主键ID
     * @return MallShopDeptDetailVo
     */
    MallShopDeptDetailVo detail(Integer id);

    /**
     * 商家部门新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(MallShopDeptCreateValidate createValidate);

    /**
     * 商家部门编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(MallShopDeptUpdateValidate updateValidate);

    /**
     * 顶级部门编辑（强制保持 pid=0）
     */
    void topEdit(MallShopDeptTopUpdateValidate updateValidate);

    /**
     * 商家部门删除
     *
     * @author Amos
     * @param id 主键ID
     */
    void del(Integer id);
}
