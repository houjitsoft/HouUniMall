package com.houjiang.payment.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.entity.PayReconDiffEntity;
import com.houjiang.payment.entity.PayReconFileEntity;
import com.houjiang.payment.entity.PayReconReportEntity;
import com.houjiang.payment.service.api.ReconService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 自研对账管理 Controller（完整版）
 *
 * 平台定位：自研独立支付平台对账域，对标通企付对账能力但完全自研。
 * 接口清单（7 个）：
 *   /pullChannelBill  拉取渠道账单
 *   /diffCheck        对账核对
 *   /diffList         差异列表
 *   /diffHandle       差异处理
 *   /reportGen        月度报表生成
 *   /reportList       报表分页查询
 *   /trigger          手动触发对账
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/recon")
@RequiredArgsConstructor
public class ReconController {

    private final ReconService reconService;

    /** 1. 拉取渠道账单文件 */
    @PostMapping("/pullChannelBill")
    public ResponseResult<PayReconFileEntity> pullChannelBill(@RequestBody Map<String, Object> req) {
        try {
            return ResponseResult.success(reconService.pullChannelBill(req));
        } catch (IllegalArgumentException ex) {
            return ResponseResult.fail(400, ex.getMessage());
        } catch (Exception ex) {
            log.error("[Recon] pullChannelBill 失败", ex);
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 2. 差异列表分页 */
    @PostMapping("/diffList")
    public ResponseResult<IPage<PayReconDiffEntity>> diffList(@RequestBody Map<String, Object> req) {
        try {
            return ResponseResult.success(reconService.diffList(req));
        } catch (Exception ex) {
            log.error("[Recon] diffList 失败", ex);
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 3. 差异处理 */
    @PostMapping("/diffHandle")
    public ResponseResult<Boolean> diffHandle(@RequestBody Map<String, Object> req) {
        try {
            return ResponseResult.success(reconService.diffHandle(req));
        } catch (IllegalArgumentException ex) {
            return ResponseResult.fail(400, ex.getMessage());
        } catch (Exception ex) {
            log.error("[Recon] diffHandle 失败", ex);
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 4. 月度报表生成 */
    @PostMapping("/reportGen")
    public ResponseResult<PayReconReportEntity> reportGen(@RequestBody Map<String, Object> req) {
        try {
            return ResponseResult.success(reconService.reportGen(req));
        } catch (IllegalArgumentException ex) {
            return ResponseResult.fail(400, ex.getMessage());
        } catch (Exception ex) {
            log.error("[Recon] reportGen 失败", ex);
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 5. 对账核对（比对本地订单与渠道账单） */
    @PostMapping("/diffCheck")
    public ResponseResult<Integer> diffCheck(@RequestBody Map<String, Object> req) {
        try {
            String billDate = req.get("billDate") != null ? req.get("billDate").toString() : null;
            String channel = req.get("channel") != null ? req.get("channel").toString() : null;
            return ResponseResult.success(reconService.diffCheck(billDate, channel));
        } catch (IllegalArgumentException ex) {
            return ResponseResult.fail(400, ex.getMessage());
        } catch (Exception ex) {
            log.error("[Recon] diffCheck 失败", ex);
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 6. 报表分页查询 */
    @PostMapping("/reportList")
    public ResponseResult<IPage<PayReconReportEntity>> reportList(@RequestBody Map<String, Object> req) {
        try {
            return ResponseResult.success(reconService.reportList(req));
        } catch (Exception ex) {
            log.error("[Recon] reportList 失败", ex);
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 7. 手动触发对账（拉取账单 + 差异核对） */
    @PostMapping("/trigger")
    public ResponseResult<Map<String, Object>> trigger(@RequestBody(required = false) Map<String, Object> req) {
        try {
            return ResponseResult.success(reconService.trigger(req == null ? new HashMap<>() : req));
        } catch (Exception ex) {
            log.error("[Recon] trigger 失败", ex);
            return ResponseResult.fail(500, ex.getMessage());
        }
    }
}
