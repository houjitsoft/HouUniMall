package com.houjiang.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.service.IArtCateService;
import com.houjiang.merchant.validate.article.ArtCateCreateValidate;
import com.houjiang.merchant.validate.article.ArtCateUpdateValidate;
import com.houjiang.merchant.validate.article.ArtCateSearchValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.article.ArticleCateVo;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.article.Article;
import com.houjiang.merchant.common.entity.article.ArticleCate;
import com.houjiang.merchant.common.mapper.article.ArticleCateMapper;
import com.houjiang.merchant.common.mapper.article.ArticleMapper;
import com.houjiang.merchant.common.util.TimeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文章分类服务实现类
 */
@Service
public class ArtCateServiceImpl implements IArtCateService {

    @Resource
    ArticleCateMapper articleCategoryMapper;

    @Resource
    ArticleMapper articleMapper;

    /**
     * 分类所有
     *
     * @author Amos
     * @return List<CategoryVo>
     */
    @Override
    public List<ArticleCateVo> all() {
        QueryWrapper<ArticleCate> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "name", "sort", "is_show", "create_time", "update_time")
                .isNull("delete_time")
                .orderByDesc(Arrays.asList("sort", "id"));

        List<ArticleCate> lists = articleCategoryMapper.selectList(queryWrapper);

        List<ArticleCateVo> vos = new ArrayList<>();
        for (ArticleCate category : lists) {
            ArticleCateVo vo = new ArticleCateVo();
            BeanUtils.copyProperties(category, vo);

            vo.setCreateTime(TimeUtils.timestampToDate(vo.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(vo.getUpdateTime()));
            vos.add(vo);
        }

        return vos;
    }

    /**
     * 分类列表
     *
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<CategoryVo>
     */
    @Override
    public PageResult<ArticleCateVo> list(PageValidate pageValidate, ArtCateSearchValidate searchValidate) {
        Integer pageNo   = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        QueryWrapper<ArticleCate> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "name", "sort", "is_show", "create_time", "update_time")
                .isNull("delete_time")
                .orderByDesc(Arrays.asList("sort", "id"));

        articleCategoryMapper.setSearch(queryWrapper, searchValidate, new String[]{
                "like:name:str",
                "=:isShow@is_show:int"
        });

        IPage<ArticleCate> iPage = articleCategoryMapper.selectPage(new Page<>(pageNo, pageSize), queryWrapper);

        List<ArticleCateVo> list = new ArrayList<>();
        for (ArticleCate category : iPage.getRecords()) {
            ArticleCateVo vo = new ArticleCateVo();
            BeanUtils.copyProperties(category, vo);

            Long number = articleMapper.selectCount(new QueryWrapper<Article>()
                    .eq("cid", category.getId())
                    .isNull("delete_time"));

            vo.setArticleCount(number);
            vo.setCreateTime(TimeUtils.timestampToDate(vo.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(vo.getUpdateTime()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 分类详情
     *
     * @author Amos
     * @param id 分类ID
     * @return CategoryVo
     */
    @Override
    public ArticleCateVo detail(Integer id) {
        ArticleCate model = articleCategoryMapper.selectOne(
                new QueryWrapper<ArticleCate>()
                        .select(ArticleCate.class, info->
                          !info.getColumn().equals("delete_time"))
                        .eq("id", id)
                        .isNull("delete_time"));

        Assert.notNull(model, "分类不存在");

        ArticleCateVo vo = new ArticleCateVo();
        BeanUtils.copyProperties(model, vo);
        vo.setCreateTime(TimeUtils.timestampToDate(model.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(model.getUpdateTime()));

        return vo;
    }

    /**
     * 分类新增
     *
     * @author Amos
     * @param createValidate 分类参数
     */
    @Override
    public void add(ArtCateCreateValidate createValidate) {
        ArticleCate model = new ArticleCate();
        model.setName(createValidate.getName());
        model.setSort(createValidate.getSort());
        model.setIsShow(createValidate.getIsShow());
        model.setCreateTime(TimeUtils.timestamp());
        model.setUpdateTime(TimeUtils.timestamp());
        articleCategoryMapper.insert(model);
    }

    /**
     * 分类编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    @Override
    public void edit(ArtCateUpdateValidate updateValidate) {
        ArticleCate model = articleCategoryMapper.selectOne(
                new QueryWrapper<ArticleCate>()
                        .select(ArticleCate.class, info->
                           !info.getColumn().equals("delete_time"))
                        .eq("id", updateValidate.getId())
                        .isNull("delete_time"));

        Assert.notNull(model, "分类不存在");

        model.setName(updateValidate.getName());
        model.setSort(updateValidate.getSort());
        model.setIsShow(updateValidate.getIsShow());
        model.setUpdateTime(TimeUtils.timestamp());
        articleCategoryMapper.updateById(model);
    }

    /**
     * 分类参数
     *
     * @author Amos
     * @param id 分类ID
     */
    @Override
    public void del(Integer id) {
        ArticleCate model = articleCategoryMapper.selectOne(
                new QueryWrapper<ArticleCate>()
                        .select("id,is_show")
                        .eq("id", id)
                        .isNull("delete_time"));

        Assert.notNull(model, "分类不存在");

        Article article = articleMapper.selectOne(new QueryWrapper<Article>()
                .eq("cid", id)
                .isNull("delete_time")
                .last("limit 1"));

        Assert.isNull(article, "当前分类已被文章使用,请先移除!");

        model.setDeleteTime(TimeUtils.timestamp());
        articleCategoryMapper.updateById(model);
    }

    /**
     * 分类状态
     *
     * @author Amos
     * @param id 分类ID
     */
    @Override
    public void change(Integer id) {
        ArticleCate model = articleCategoryMapper.selectOne(
                new QueryWrapper<ArticleCate>()
                        .select("id,is_show")
                        .eq("id", id)
                        .isNull("delete_time"));

        Assert.notNull(model, "分类不存在");

        model.setIsShow(model.getIsShow()==0?1:0);
        model.setUpdateTime(TimeUtils.timestamp());
        articleCategoryMapper.updateById(model);
    }

}
