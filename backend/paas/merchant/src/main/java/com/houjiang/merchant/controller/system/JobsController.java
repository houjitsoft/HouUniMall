package com.houjiang.merchant.controller.system;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.service.system.IJobsService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.system.JobsCreateValidate;
import com.houjiang.merchant.validate.system.JobsSearchValidate;
import com.houjiang.merchant.validate.system.JobsUpdateValidate;
import com.houjiang.merchant.vo.system.JobsVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.enums.ErrorEnum;
import com.houjiang.merchant.common.util.StringUtils;
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
@RequestMapping("/adminapi/dept.jobs")
public class JobsController {

    @Resource
    IJobsService iJobsService;

    @GetMapping("/all")
    @Operation(summary="所有岗位")
    public AjaxResult<List<JobsVo>> all() {
        List<JobsVo> list = iJobsService.all();
        return AjaxResult.success(list);
    }

    @GetMapping("/lists")
    @Operation(summary="岗位列表")
    public AjaxResult<Object> list(@Validated PageValidate pageValidate,
                                               @Validated JobsSearchValidate searchValidate) {
        if (StringUtils.isNotNull(searchValidate.getExport()) && searchValidate.getExport().equals(1)) {
            JSONObject ret = iJobsService.getExportData(pageValidate, searchValidate);
            return AjaxResult.success(ret);
        } else if (StringUtils.isNotNull(searchValidate.getExport()) && searchValidate.getExport().equals(2)) {
            String path = iJobsService.export(searchValidate);
            return AjaxResult.success(2, new JSONObject() {{
                put("url", path);
            }}, ErrorEnum.SHOW_MSG.getCode());
        } else {
            PageResult<JobsVo> list = iJobsService.list(pageValidate, searchValidate);
            return AjaxResult.success(list);
        }
    }

    @GetMapping("/detail")
    @Operation(summary="岗位详情")
    public AjaxResult<JobsVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        JobsVo vo = iJobsService.detail(id);
        return AjaxResult.success(vo);
    }

    @PostMapping("/add")
    @Operation(summary="岗位新增")
    public AjaxResult<Object> add(@Validated @RequestBody JobsCreateValidate createValidate) {
        iJobsService.add(createValidate);
        return AjaxResult.success();
    }

    @PostMapping("/edit")
    @Operation(summary="岗位编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody JobsUpdateValidate updateValidate) {
        iJobsService.edit(updateValidate);
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    @Operation(summary="岗位删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iJobsService.del(idValidate.getId());
        return AjaxResult.success();
    }

}
