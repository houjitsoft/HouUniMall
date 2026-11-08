package com.houjiang.mall.controller.live;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.live.LiveAccount;
import com.houjiang.mall.entity.live.LiveActivity;
import com.houjiang.mall.entity.live.LiveProductAudit;
import com.houjiang.mall.service.live.LiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 直播互动Controller
 * 提供直播活动、商品审核、账号管理等17个接口
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/live")
@RequiredArgsConstructor
public class LiveController {

    private final LiveService liveService;

    /** ========== 直播活动（7个） ========== */

    @PostMapping("/list")
    public ResponseResult list(@RequestBody Map<String, Object> params) {
        try { return ResponseResult.success(liveService.pageList(params)); }
        catch (Exception e) { log.error("查询直播列表失败", e); return ResponseResult.fail(500, e.getMessage()); }
    }

    @PostMapping("/detail")
    public ResponseResult detail(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            LiveActivity a = liveService.getById(id);
            return a != null ? ResponseResult.success(a) : ResponseResult.fail(404, "直播不存在");
        } catch (Exception e) { log.error("查询直播详情失败", e); return ResponseResult.fail(500, e.getMessage()); }
    }

    @PostMapping("/create")
    public ResponseResult create(@RequestBody LiveActivity activity) {
        try { return liveService.save(activity) ? ResponseResult.success("创建成功") : ResponseResult.fail(500, "创建失败"); }
        catch (Exception e) { log.error("创建直播失败", e); return ResponseResult.fail(500, e.getMessage()); }
    }

    @PostMapping("/update")
    public ResponseResult update(@RequestBody LiveActivity activity) {
        try { return liveService.updateById(activity) ? ResponseResult.success("更新成功") : ResponseResult.fail(500, "更新失败"); }
        catch (Exception e) { log.error("更新直播失败", e); return ResponseResult.fail(500, e.getMessage()); }
    }

    @PostMapping("/delete")
    public ResponseResult delete(@RequestBody Map<String, Object> params) {
        try { Long id = Long.parseLong(params.get("id").toString()); return liveService.removeById(id) ? ResponseResult.success("删除成功") : ResponseResult.fail(500, "删除失败"); }
        catch (Exception e) { log.error("删除直播失败", e); return ResponseResult.fail(500, e.getMessage()); }
    }

    @PostMapping("/start")
    public ResponseResult start(@RequestBody Map<String, Object> params) {
        try { Long id = Long.parseLong(params.get("id").toString()); return liveService.startLive(id) ? ResponseResult.success("开播成功") : ResponseResult.fail(500, "操作失败"); }
        catch (Exception e) { log.error("开始直播失败", e); return ResponseResult.fail(500, e.getMessage()); }
    }

    @PostMapping("/end")
    public ResponseResult end(@RequestBody Map<String, Object> params) {
        try { Long id = Long.parseLong(params.get("id").toString()); return liveService.endLive(id) ? ResponseResult.success("停播成功") : ResponseResult.fail(500, "操作失败"); }
        catch (Exception e) { log.error("结束直播失败", e); return ResponseResult.fail(500, e.getMessage()); }
    }

    /** ========== 直播商品审核（3个） ========== */

    @PostMapping("/product/audit/list")
    public ResponseResult productAuditList(@RequestBody Map<String, Object> params) {
        try { return ResponseResult.success(liveService.productAuditPageList(params)); }
        catch (Exception e) { log.error("查询直播商品审核列表失败", e); return ResponseResult.fail(500, e.getMessage()); }
    }

    @PostMapping("/product/audit/detail")
    public ResponseResult productAuditDetail(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            LiveProductAudit a = liveService.getProductAuditDetail(id);
            return a != null ? ResponseResult.success(a) : ResponseResult.fail(404, "审核记录不存在");
        } catch (Exception e) { log.error("查询审核详情失败", e); return ResponseResult.fail(500, e.getMessage()); }
    }

    @PostMapping("/product/audit/submit")
    public ResponseResult productAuditSubmit(@RequestBody LiveProductAudit audit) {
        try { return liveService.submitProductAudit(audit) ? ResponseResult.success("提交成功") : ResponseResult.fail(500, "提交失败"); }
        catch (Exception e) { log.error("提交审核失败", e); return ResponseResult.fail(500, e.getMessage()); }
    }

    /** ========== 直播账号（7个） ========== */

    @PostMapping("/account/list")
    public ResponseResult accountList(@RequestBody Map<String, Object> params) {
        try { return ResponseResult.success(liveService.accountPageList(params)); }
        catch (Exception e) { log.error("查询直播账号列表失败", e); return ResponseResult.fail(500, e.getMessage()); }
    }

    @PostMapping("/account/detail")
    public ResponseResult accountDetail(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            LiveAccount a = liveService.getAccountDetail(id);
            return a != null ? ResponseResult.success(a) : ResponseResult.fail(404, "账号不存在");
        } catch (Exception e) { log.error("查询账号详情失败", e); return ResponseResult.fail(500, e.getMessage()); }
    }

    @PostMapping("/account/create")
    public ResponseResult accountCreate(@RequestBody LiveAccount account) {
        try { return liveService.createAccount(account) ? ResponseResult.success("创建成功") : ResponseResult.fail(500, "创建失败"); }
        catch (Exception e) { log.error("创建账号失败", e); return ResponseResult.fail(500, e.getMessage()); }
    }

    @PostMapping("/account/update")
    public ResponseResult accountUpdate(@RequestBody LiveAccount account) {
        try { return liveService.updateAccount(account) ? ResponseResult.success("更新成功") : ResponseResult.fail(500, "更新失败"); }
        catch (Exception e) { log.error("更新账号失败", e); return ResponseResult.fail(500, e.getMessage()); }
    }

    @PostMapping("/account/delete")
    public ResponseResult accountDelete(@RequestBody Map<String, Object> params) {
        try { Long id = Long.parseLong(params.get("id").toString()); return liveService.deleteAccount(id) ? ResponseResult.success("删除成功") : ResponseResult.fail(500, "删除失败"); }
        catch (Exception e) { log.error("删除账号失败", e); return ResponseResult.fail(500, e.getMessage()); }
    }

    @PostMapping("/account/toggle-status")
    public ResponseResult accountToggleStatus(@RequestBody Map<String, Object> params) {
        try { Long id = Long.parseLong(params.get("id").toString()); return liveService.toggleAccountStatus(id) ? ResponseResult.success("操作成功") : ResponseResult.fail(500, "操作失败"); }
        catch (Exception e) { log.error("切换账号状态失败", e); return ResponseResult.fail(500, e.getMessage()); }
    }
}
