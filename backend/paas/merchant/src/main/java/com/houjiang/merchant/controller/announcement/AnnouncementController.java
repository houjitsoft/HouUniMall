package com.houjiang.merchant.controller.announcement;

import com.houjiang.merchant.common.annotation.MerchantPermission;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.service.announcement.IAnnouncementService;
import com.houjiang.merchant.validate.announcement.AnnouncementSearchValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.announcement.AnnouncementDetailVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("merchant/mall.announcement")
@Tag(name = "平台公告管理")
public class AnnouncementController {

    @Resource
    IAnnouncementService iAnnouncementService;

    /**
     * @notes 获取平台公告最新一条
     * @return Json
     * @author Amos
     * @datetime 2024-11-21 15:11:03
     */
    @GetMapping("/newest")
    @MerchantPermission(anyOf = "mall.announcement/lists:detail")
    @Operation(summary="获取平台公告最新一条")
    public AjaxResult<AnnouncementDetailVo> newest() {
        AnnouncementDetailVo vo = iAnnouncementService.newest();
        return AjaxResult.success(vo);
    }

    @GetMapping("/lists")
    @MerchantPermission(anyOf = "mall.announcement/lists:detail")
    @Operation(summary="分类列表")
    public AjaxResult<PageResult<AnnouncementDetailVo>> lists(@Validated PageValidate pageValidate,
                                                      @Validated AnnouncementSearchValidate searchValidate) {
        PageResult<AnnouncementDetailVo> list = iAnnouncementService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @MerchantPermission(anyOf = "mall.announcement/lists:detail")
    @Operation(summary="分类详情")
    public AjaxResult<AnnouncementDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        AnnouncementDetailVo vo = iAnnouncementService.detail(id);
        return AjaxResult.success(vo);
    }
}
