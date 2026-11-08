package com.houjiang.merchant.controller;

import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.IArtCateService;
import com.houjiang.merchant.validate.article.ArtCateCreateValidate;
import com.houjiang.merchant.validate.article.ArtCateUpdateValidate;
import com.houjiang.merchant.validate.article.ArtCateSearchValidate;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.article.ArticleCateVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @author Amos
 */
@RestController
@RequestMapping({"/adminapi/article/articleCate", "/adminapi/article.articleCate"})
public class ArtCateController {

    @Resource
    IArtCateService iArtCateService;

    @GetMapping("/all")
    @Operation(summary="所有分类")
    public AjaxResult<List<ArticleCateVo>> all() {
        List<ArticleCateVo> list = iArtCateService.all();
        return AjaxResult.success(list);
    }

    @GetMapping("/lists")
    @Operation(summary="分类列表")
    public AjaxResult<PageResult<ArticleCateVo>> list(@Validated PageValidate pageValidate,
                                                      @Validated ArtCateSearchValidate searchValidate) {
        PageResult<ArticleCateVo> list = iArtCateService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary="分类详情")
    public AjaxResult<ArticleCateVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        ArticleCateVo vo = iArtCateService.detail(id);
        return AjaxResult.success(vo);
    }

    @Log(title = "文章分类新增")
    @PostMapping("/add")
    @Operation(summary="分类新增")
    public AjaxResult<Object> add(@Validated @RequestBody ArtCateCreateValidate createValidate) {
        iArtCateService.add(createValidate);
        return AjaxResult.success();
    }

    @Log(title = "文章分类编辑")
    @PostMapping("/edit")
    @Operation(summary="分类编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody ArtCateUpdateValidate updateValidate) {
        iArtCateService.edit(updateValidate);
        return AjaxResult.success();
    }

    @Log(title = "文章分类删除")
    @PostMapping("/delete")
    @Operation(summary="分类删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iArtCateService.del(idValidate.getId());
        return AjaxResult.success();
    }

    @Log(title = "文章分类状态")
    @PostMapping("/updateStatus")
    @Operation(summary="分类状态")
    public AjaxResult<Object> change(@Validated @RequestBody IdValidate idValidate) {
        iArtCateService.change(idValidate.getId());
        return AjaxResult.success();
    }

}
