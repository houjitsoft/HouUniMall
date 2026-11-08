package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 直播互动BFF Controller
/**
 * 通过MallServiceFeign调用mall服务直播相关17个接口
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/mall/live")
@RequiredArgsConstructor
public class MallLiveController {

    private final MallServiceFeign mallServiceFeign;

    /** ========== 直播活动 ========== */
    @PostMapping("/list")
    public ResponseResult list(@RequestBody Map<String, Object> p) { return mallServiceFeign.getLiveList(p); }
    @PostMapping("/detail")
    public ResponseResult detail(@RequestBody Map<String, Object> p) { return mallServiceFeign.getLiveDetail(p); }
    @PostMapping("/create")
    public ResponseResult create(@RequestBody Map<String, Object> p) { return mallServiceFeign.createLive(p); }
    @PostMapping("/update")
    public ResponseResult update(@RequestBody Map<String, Object> p) { return mallServiceFeign.updateLive(p); }
    @PostMapping("/delete")
    public ResponseResult delete(@RequestBody Map<String, Object> p) { return mallServiceFeign.deleteLive(p); }
    @PostMapping("/start")
    public ResponseResult start(@RequestBody Map<String, Object> p) { return mallServiceFeign.startLive(p); }
    @PostMapping("/end")
    public ResponseResult end(@RequestBody Map<String, Object> p) { return mallServiceFeign.endLive(p); }

    /** ========== 直播商品审核 ========== */
    @PostMapping("/product/audit/list")
    public ResponseResult productAuditList(@RequestBody Map<String, Object> p) { return mallServiceFeign.getLiveProductAuditList(p); }
    @PostMapping("/product/audit/detail")
    public ResponseResult productAuditDetail(@RequestBody Map<String, Object> p) { return mallServiceFeign.getLiveProductAuditDetail(p); }
    @PostMapping("/product/audit/submit")
    public ResponseResult productAuditSubmit(@RequestBody Map<String, Object> p) { return mallServiceFeign.submitLiveProductAudit(p); }

    /** ========== 直播账号 ========== */
    @PostMapping("/account/list")
    public ResponseResult accountList(@RequestBody Map<String, Object> p) { return mallServiceFeign.getLiveAccountList(p); }
    @PostMapping("/account/detail")
    public ResponseResult accountDetail(@RequestBody Map<String, Object> p) { return mallServiceFeign.getLiveAccountDetail(p); }
    @PostMapping("/account/create")
    public ResponseResult accountCreate(@RequestBody Map<String, Object> p) { return mallServiceFeign.createLiveAccount(p); }
    @PostMapping("/account/update")
    public ResponseResult accountUpdate(@RequestBody Map<String, Object> p) { return mallServiceFeign.updateLiveAccount(p); }
    @PostMapping("/account/delete")
    public ResponseResult accountDelete(@RequestBody Map<String, Object> p) { return mallServiceFeign.deleteLiveAccount(p); }
    @PostMapping("/account/toggle-status")
    public ResponseResult accountToggleStatus(@RequestBody Map<String, Object> p) { return mallServiceFeign.toggleLiveAccountStatus(p); }
}
