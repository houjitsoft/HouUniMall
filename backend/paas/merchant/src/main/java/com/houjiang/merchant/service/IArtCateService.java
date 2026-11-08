package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.article.ArtCateCreateValidate;
import com.houjiang.merchant.validate.article.ArtCateUpdateValidate;
import com.houjiang.merchant.validate.article.ArtCateSearchValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.article.ArticleCateVo;
import com.houjiang.merchant.common.core.PageResult;

import java.util.List;

/**
 * 文章分类服接口务类
 */
public interface IArtCateService {

    /**
     * 分类所有
     *
     * @author Amos
     * @return List<CategoryVo>
     */
    List<ArticleCateVo> all();

    /**
     * 分类列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<CategoryVo>
     */
    PageResult<ArticleCateVo> list(PageValidate pageValidate, ArtCateSearchValidate searchValidate);

    /**
     * 分类详情
     *
     * @author Amos
     * @param id 分类ID
     */
    ArticleCateVo detail(Integer id);

    /**
     * 分类新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(ArtCateCreateValidate createValidate);

    /**
     * 分类编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(ArtCateUpdateValidate updateValidate);

    /**
     * 分类删除
     *
     * @author Amos
     * @param id 分类ID
     */
    void del(Integer id);

    /**
     * 分类状态
     *
     * @author Amos
     * @param id 分类ID
     */
    void change(Integer id);

}
