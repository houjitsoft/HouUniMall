package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.article.ArticleCreateValidate;
import com.houjiang.merchant.validate.article.ArticleSearchValidate;
import com.houjiang.merchant.validate.article.ArticleUpdateValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.article.ArticleDetailVo;
import com.houjiang.merchant.vo.article.ArticleListedVo;
import com.houjiang.merchant.common.core.PageResult;

/**
 * 文章服务接口类
 */
public interface IArticleService {

    /**
     * 文章列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<ArticleListVo>
     */
    PageResult<ArticleListedVo> list(PageValidate pageValidate, ArticleSearchValidate searchValidate);

    /**
     * 文章详情
     *
     * @author Amos
     * @param id 主键ID
     */
    ArticleDetailVo detail(Integer id);

    /**
     * 文章新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(ArticleCreateValidate createValidate);

    /**
     * 文章编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(ArticleUpdateValidate updateValidate);

    /**
     * 文章删除
     *
     * @author Amos
     * @param id 文章主键
     */
    void del(Integer id);

    /**
     * 文章状态
     *
     * @author Amos
     * @param id 文章主键
     */
    void change(Integer id);

}
