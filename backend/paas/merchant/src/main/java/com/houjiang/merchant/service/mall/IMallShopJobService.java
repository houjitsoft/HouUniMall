package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopJobCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopJobSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopJobUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopJobDetailVo;
import com.houjiang.merchant.vo.mall.MallShopJobListedVo;

import java.util.List;

/**
 * 商家岗位服务接口类
 * @author Amos
 */
public interface IMallShopJobService {

    /**
     * 商家岗位列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallShopJobListedVo>
     */
    PageResult<MallShopJobListedVo> list(PageValidate pageValidate, MallShopJobSearchValidate searchValidate);

    /**
     * 商家岗位详情
     *
     * @author Amos
     * @param id 主键ID
     * @return MallShopJobDetailVo
     */
    MallShopJobDetailVo detail(Integer id);

    /**
     * 商家岗位新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(MallShopJobCreateValidate createValidate);

    /**
     * 商家岗位编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(MallShopJobUpdateValidate updateValidate);

    /**
     * 商家岗位删除
     *
     * @author Amos
     * @param id 主键ID
     */
    void del(Integer id);

    /**
     * 商家岗位列表
     * @param searchValidate
     * @return
     */
    List<MallShopJobListedVo> otherLists(MallShopJobSearchValidate searchValidate);
}
