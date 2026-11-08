package com.houjiang.payment.controller.api;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.entity.PayBankcardEntity;
import com.houjiang.payment.mapper.PayBankcardMapper;
import com.houjiang.payment.util.ApiTokenUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户银行卡 Controller（Task 0 骨架）
 * 支持 list / add / delete，真实对接渠道绑卡鉴权放在后续 Task
 *
 * userId 统一从 Header Authorization 解析
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/bankcard")
@RequiredArgsConstructor
public class BankcardController {

    private final PayBankcardMapper bankcardMapper;

    /**
     * 查询银行卡列表
     */
    @PostMapping("/list")
    public ResponseResult<List<PayBankcardEntity>> list(HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        List<PayBankcardEntity> rows = bankcardMapper.selectList(
                Wrappers.<PayBankcardEntity>lambdaQuery()
                        .eq(PayBankcardEntity::getUserId, userId)
                        .eq(PayBankcardEntity::getStatus, 1)
                        .orderByDesc(PayBankcardEntity::getIsDefault)
                        .orderByDesc(PayBankcardEntity::getCreateTime));
        // 敏感字段回显脱敏
        rows.forEach(r -> r.setCardNoEncrypted(null));
        rows.forEach(r -> r.setHolderIdEncrypted(null));
        return ResponseResult.success(rows);
    }

    /**
     * 新增银行卡（Task 0 暂仅本地落库；真实四要素校验 + 预签约放在后续 Task）
     * 前端传：cardNo、bankName、cardHolder、phone、idCard
     */
    @PostMapping("/add")
    public ResponseResult<Long> add(@RequestBody Map<String, Object> body, HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        String cardNo = body == null ? null : (String) body.get("cardNo");
        String bankName = body == null ? null : (String) body.get("bankName");
        String cardHolder = body == null ? null : (String) body.get("cardHolder");
        String phone = body == null ? null : (String) body.get("phone");
        String idCard = body == null ? null : (String) body.get("idCard");
        if (!StringUtils.hasText(cardNo)) {
            return ResponseResult.fail(500, "cardNo 必填");
        }

        PayBankcardEntity entity = new PayBankcardEntity();
        entity.setUserId(userId);
        entity.setCardNoEncrypted(cardNo);
        entity.setCardNoTail(cardNo.length() >= 4 ? cardNo.substring(cardNo.length() - 4) : cardNo);
        entity.setBankName(bankName);
        entity.setHolderName(cardHolder);
        entity.setHolderMobile(phone);
        entity.setHolderIdEncrypted(idCard);
        entity.setCardType(1);
        entity.setIsDefault(0);
        entity.setStatus(1);
        entity.setVerifyStatus(0);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        bankcardMapper.insert(entity);
        log.info("[bankcard/add] userId={}, cardTail={}", userId, entity.getCardNoTail());
        return ResponseResult.success(entity.getId());
    }

    /**
     * 删除银行卡（软删）
     * 前端传：cardId
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> delete(@RequestBody Map<String, Object> body, HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        Object cardIdObj = body == null ? null : body.get("cardId");
        if (cardIdObj == null) {
            cardIdObj = body == null ? null : body.get("id");
        }
        if (cardIdObj == null) {
            return ResponseResult.fail(500, "cardId 必填");
        }
        Long cardId = Long.valueOf(cardIdObj.toString());
        int n = bankcardMapper.update(null,
                Wrappers.<PayBankcardEntity>lambdaUpdate()
                        .eq(PayBankcardEntity::getId, cardId)
                        .eq(PayBankcardEntity::getUserId, userId)
                        .set(PayBankcardEntity::getStatus, 0)
                        .set(PayBankcardEntity::getUpdateTime, new Date()));
        return ResponseResult.success(n > 0);
    }
}
