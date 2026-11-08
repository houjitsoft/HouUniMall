package com.houjiang.mall.controller.setting;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.setting.SettingNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商城后台 - 通知设置 Controller。
 * 对应 like-admin SettingNoticeController（/adminapi/notice.notice）。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/setting/notice")
@RequiredArgsConstructor
public class MallSettingNoticeController {

    private final SettingNoticeService noticeService;

    /**
     * 通知设置列表。
     * GET /mall/setting/notice/lists?recipient=1
     */
    @GetMapping("/lists")
    public ResponseResult<Map<String, Object>> lists(@RequestParam("recipient") Integer recipient) {
        List<Map<String, Object>> data = noticeService.list(recipient);
        Map<String, Object> wrapper = new java.util.LinkedHashMap<>();
        wrapper.put("lists", data);
        return ResponseResult.success(wrapper);
    }

    /**
     * 通知设置详情。
     * GET /mall/setting/notice/detail?id=1
     */
    @GetMapping("/detail")
    public ResponseResult<Map<String, Object>> detail(@RequestParam("id") Integer id) {
        return ResponseResult.success(noticeService.detail(id));
    }

    /**
     * 通知设置保存。
     * POST /mall/setting/notice/edit
     * body: {id, template:{system_notice, sms_notice, oa_notice, mnp_notice}}
     */
    @PostMapping("/edit")
    public ResponseResult<Void> edit(@RequestBody Map<String, Object> params) {
        noticeService.save(params);
        return ResponseResult.success();
    }
}
