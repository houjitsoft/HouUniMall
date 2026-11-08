package com.houjiang.merchant.controller;

import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.IArticleService;
import com.houjiang.merchant.validate.article.ArticleCreateValidate;
import com.houjiang.merchant.validate.article.ArticleSearchValidate;
import com.houjiang.merchant.validate.article.ArticleUpdateValidate;

import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.article.ArticleDetailVo;
import com.houjiang.merchant.vo.article.ArticleListedVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping({"adminapi/article/article", "adminapi/article.article"})
public class ArticleController {

    @Resource
    IArticleService iArticleService;

    @GetMapping("/lists")
    @Operation(summary="文章列表")
    public AjaxResult<PageResult<ArticleListedVo>> list(@Validated PageValidate pageValidate,
                                                        @Validated ArticleSearchValidate searchValidate) {
        PageResult<ArticleListedVo> vos = iArticleService.list(pageValidate, searchValidate);
        return AjaxResult.success(vos);
    }

    @GetMapping("/detail")
    @Operation(summary="文章详情")
    public AjaxResult<ArticleDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        ArticleDetailVo vo = iArticleService.detail(id);
        return AjaxResult.success(vo);
    }

    @Log(title = "文章新增")
    @PostMapping("/add")
    @Operation(summary="文章新增")
    public AjaxResult<Object> add(@Validated @RequestBody ArticleCreateValidate createValidate) {
        iArticleService.add(createValidate);
        return AjaxResult.success();
    }

    @Log(title = "文章编辑")
    @PostMapping("/edit")
    @Operation(summary="文章编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody ArticleUpdateValidate updateValidate) {
        iArticleService.edit(updateValidate);
        return AjaxResult.success();
    }

    @Log(title = "文章删除")
    @PostMapping("/delete")
    @Operation(summary="文章删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iArticleService.del(idValidate.getId());
        return AjaxResult.success();
    }

    @Log(title = "文章状态")
    @PostMapping("/updateStatus")
    @Operation(summary="文章状态")
    public AjaxResult<Object> change(@Validated @RequestBody IdValidate idValidate) {
        iArticleService.change(idValidate.getId());
        return AjaxResult.success();
    }

}
