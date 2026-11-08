package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShopGoodsCategory;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopGoodsCategoryCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopGoodsCategorySearchValidate;
import com.houjiang.merchant.validate.mall.MallShopGoodsCategoryStatusValidate;
import com.houjiang.merchant.validate.mall.MallShopGoodsCategoryUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopGoodsCategoryDetailVo;
import com.houjiang.merchant.vo.mall.MallShopGoodsCategoryListedVo;
import com.houjiang.merchant.vo.mall.MallShopGoodsCategoryOtherListedVo;

import java.util.List;

/**
 * 商家商品分类服务接口类
 * @author Amos
 */
public interface IMallShopGoodsCategoryService {

    /**
     * 商家商品分类列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallShopGoodsCategoryListedVo>
     */
    PageResult<MallShopGoodsCategoryListedVo> list(PageValidate pageValidate, MallShopGoodsCategorySearchValidate searchValidate);

    /**
     * 商家商品分类详情
     *
     * @author Amos
     * @param id 主键ID
     * @return MallShopGoodsCategoryDetailVo
     */
    MallShopGoodsCategoryDetailVo detail(Integer id);

    /**
     * 商家商品分类新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(MallShopGoodsCategoryCreateValidate createValidate);

    /**
     * 商家商品分类编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(MallShopGoodsCategoryUpdateValidate updateValidate);

    /**
     * 商家商品分类删除
     *
     * @author Amos
     * @param id 主键ID
     */
    void del(Integer id);

    /**
     * 检查名称是否唯一
     * @param title
     * @return
     */
    Boolean titleUnique(String title, Integer shopId);
    /**
     * 检查名称是否唯一
     * @param title
     * @return
     */
    Boolean titleUnique(String title, Integer shopId, Integer id);

    /**
     * 更新状态
     * @param id
     */
    void status(MallShopGoodsCategoryStatusValidate statusValidate);

    /**
     * 根据ID返回信息
     * @param id
     * @return
     */
    MallShopGoodsCategory getById(Integer id);

    /**
     * @notes 其他列表
     * @author Amos
     */
    List<MallShopGoodsCategoryOtherListedVo> otherList(Integer shopId);
}
