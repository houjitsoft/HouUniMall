package com.houjiang.payment.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.entity.PayCashierOrderEntity;
import com.houjiang.payment.mapper.PayCashierOrderMapper;
import com.houjiang.payment.util.ApiTokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 收银台订单查询 Controller
 * 对应前端 /api/payment/order/list、/api/payment/order/detail
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/order")
@RequiredArgsConstructor
public class CashierOrderController {

    private final PayCashierOrderMapper cashierOrderMapper;

    /**
     * 分页查询收银台订单列表
     * @param param {status, page/pageNum, size/pageSize}
     *              userId 由 Header token 解析（不再要求前端传）
     */
    @PostMapping("/list")
    public ResponseResult<IPage<PayCashierOrderEntity>> list(@RequestBody Map<String, Object> param,
                                                             HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        Integer status = param == null || param.get("status") == null
                ? null : Integer.valueOf(param.get("status").toString());
        long pageNum = 1L;
        if (param != null) {
            Object p = param.get("pageNum");
            if (p == null) p = param.get("page");
            if (p != null) pageNum = Long.parseLong(p.toString());
        }
        long pageSize = 20L;
        if (param != null) {
            Object s = param.get("pageSize");
            if (s == null) s = param.get("size");
            if (s != null) pageSize = Long.parseLong(s.toString());
        }

        IPage<PayCashierOrderEntity> page = cashierOrderMapper.selectPage(
                new Page<>(pageNum, pageSize),
                Wrappers.<PayCashierOrderEntity>lambdaQuery()
                        .eq(PayCashierOrderEntity::getUserId, userId)
                        .eq(status != null, PayCashierOrderEntity::getStatus, status)
                        .orderByDesc(PayCashierOrderEntity::getCreateTime));
        return ResponseResult.success(page);
    }

    /**
     * 收银台订单详情
     */
    @GetMapping("/detail/{cashierNo}")
    public ResponseResult<PayCashierOrderEntity> detail(@PathVariable("cashierNo") String cashierNo) {
        PayCashierOrderEntity order = cashierOrderMapper.selectByCashierNo(cashierNo);
        return ResponseResult.success(order);
    }
}
