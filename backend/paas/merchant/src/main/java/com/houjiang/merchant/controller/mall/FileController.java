package com.houjiang.merchant.controller.mall;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.IFileService;
import com.houjiang.merchant.validate.file.FileCateValidate;
import com.houjiang.merchant.validate.file.FileMoveValidate;
import com.houjiang.merchant.validate.file.FileRenameValidate;
import com.houjiang.merchant.validate.file.FileSearchValidate;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.IdsValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.album.FileVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/merchant/file")
public class FileController {

    @Resource
    IFileService fileService;

    @GetMapping("/lists")
    @Operation(summary="文件列表")
    public AjaxResult<PageResult<FileVo>> fileList(@Validated PageValidate pageValidate,
                                                   @Validated FileSearchValidate searchValidate) {
        PageResult<FileVo> voPageResult = fileService.fileList(pageValidate, searchValidate);
        return AjaxResult.success(voPageResult);
    }

    @Log(title = "文件重命名")
    @PostMapping("/rename")
    @Operation(summary="文件重命名")
    public AjaxResult<Object> rename(@Validated @RequestBody FileRenameValidate renameValidate) {
        fileService.fileRename(renameValidate.getId(), renameValidate.getName());
        return AjaxResult.success();
    }

    @Log(title = "文件移动")
    @PostMapping("/move")
    @Operation(summary="批量文件移动")
    public AjaxResult<Object> move(@Validated @RequestBody FileMoveValidate moveValidate) {
        fileService.fileMove(moveValidate.getIds(), moveValidate.getCid());
        return AjaxResult.success();
    }

    @Log(title = "文件删除")
    @PostMapping("/delete")
    @Operation(summary="批量文件删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdsValidate idsValidate) {
        fileService.fileDel(idsValidate.getIds());
        return AjaxResult.success();
    }

    @GetMapping("/listCate")
    @Operation(summary="文件分类列表")
    public AjaxResult<JSONObject> cateList(@Validated FileSearchValidate searchValidate) {
        JSONObject result = fileService.cateList(searchValidate);
        return AjaxResult.success(result);
    }

    @Log(title = "新增分类")
    @PostMapping("/addCate")
    @Operation(summary="新增分类")
    public AjaxResult<Object> cateAdd(@Validated @RequestBody FileCateValidate cateValidate) {
        fileService.cateAdd(cateValidate);
        return AjaxResult.success();
    }

    @Log(title = "分类重命名修改")
    @PostMapping("/editCate")
    @Operation(summary="分类重命名修改")
    public AjaxResult<Object> cateRename(@Validated @RequestBody FileRenameValidate renameValidate) {
        fileService.cateRename(renameValidate.getId(), renameValidate.getName());
        return AjaxResult.success();
    }

    @Log(title = "分类删除")
    @PostMapping("/delCate")
    @Operation(summary="分类删除")
    public AjaxResult<Object> cateDel(@Validated @RequestBody IdValidate idValidate) {
        fileService.cateDel(idValidate.getId());
        return AjaxResult.success();
    }

}
