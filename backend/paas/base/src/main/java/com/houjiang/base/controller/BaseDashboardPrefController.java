package com.houjiang.base.controller;

import com.houjiang.base.dto.BaseDashboardPrefDTO;
import com.houjiang.base.service.BaseDashboardPrefService;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard 用户偏好 Controller
 *
 * @author Amos
 * @since 2026-05-13
 */
@RestController
@RequestMapping("/base/dashboard/pref")
public class BaseDashboardPrefController extends BaseController {

    @Autowired
    private BaseDashboardPrefService baseDashboardPrefService;

    /**
     * 查询当前登录用户的 Dashboard 偏好
     */
    @PostMapping("/get")
    public ResponseResult get() {
        String token = getTokenFromRequest();
        return baseDashboardPrefService.getPref(token);
    }

    /**
     * 保存当前登录用户的 Dashboard 偏好（全量覆盖 items）
     */
    @PostMapping("/save")
    public ResponseResult save(@RequestBody BaseDashboardPrefDTO dto) {
        String token = getTokenFromRequest();
        return baseDashboardPrefService.savePref(token, dto);
    }

    /**
     * 重置当前登录用户的 Dashboard 偏好（恢复默认）
     */
    @PostMapping("/reset")
    public ResponseResult reset() {
        String token = getTokenFromRequest();
        return baseDashboardPrefService.resetPref(token);
    }
}
