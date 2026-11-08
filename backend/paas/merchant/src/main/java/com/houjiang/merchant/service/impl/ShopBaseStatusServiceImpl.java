package com.houjiang.merchant.service.impl;

import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.merchant.service.ShopBaseStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * C 端商家入驻状态。
 * <p>
 * 注意：当前库表 {@code hou_merchant_mall_shop} 尚未补齐 owner_user_id 等字段，
 * 不能走 MallShop 实体全字段 SELECT；优先查入驻申请表，否则软返回「未开店」。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopBaseStatusServiceImpl implements ShopBaseStatusService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public ResponseResult queryShopJoinStatus(Map<String, Object> param) {
        UserInfoVO user = SecurityContextUtils.getCurrentUserOrNull();
        if (user == null || !StringUtils.isNotBlank(user.getUserId())) {
            return ResponseResult.fail(BizCodeEnume.USER_IS_NOT_LOGGED_IN);
        }

        Map<String, Object> data = emptyStatus(user);
        try {
            List<Map<String, Object>> applies = jdbcTemplate.queryForList(
                    "SELECT id, audit_status, delete_time FROM hou_merchant_mall_shop_apply "
                            + "WHERE user_id = ? AND (delete_time IS NULL OR delete_time = 0) "
                            + "ORDER BY id DESC LIMIT 1",
                    user.getUserId());
            if (!applies.isEmpty()) {
                Map<String, Object> apply = applies.get(0);
                Object audit = apply.get("audit_status");
                data.put("isExist", CommonType.commonStatus.YES.getCode());
                // apply.audit_status：约定 0待审 1通过 2拒绝（与商家后台常见枚举对齐）
                int auditStatus = audit == null ? 0 : Integer.parseInt(String.valueOf(audit));
                if (auditStatus == 1) {
                    data.put("shopStatus", CommonType.storeStatus.SUCCESSFULLY.getCode());
                    data.put("shopType", user.getShopType() != null ? user.getShopType() : 1);
                    data.put("shopBaseExist", CommonType.shopBaseExist.OPEN_STORE.getCode());
                } else if (auditStatus == 2) {
                    data.put("shopStatus", 4);
                    data.put("shopBaseExist", CommonType.shopBaseExist.NO_OPEN_STORE.getCode());
                } else {
                    data.put("shopStatus", 1);
                    data.put("shopBaseExist", CommonType.shopBaseExist.NO_OPEN_STORE.getCode());
                }
            }
        } catch (Exception e) {
            log.warn("查询商家入驻申请失败，软返回未开店 userId={}, err={}", user.getUserId(), e.getMessage());
        }
        return ResponseResult.success(data);
    }

    private static Map<String, Object> emptyStatus(UserInfoVO user) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("oldJoinId", null);
        data.put("newJoinId", null);
        data.put("shopBusiness", null);
        data.put("paymentVerifyStatus", null);
        data.put("shopVerifyReason", null);
        data.put("paymentVerifyReason", null);
        data.put("sellerisAdmin", user.getSellerIsAdmin());
        data.put("shopType", user.getShopType());
        data.put("shopStatus", null);
        data.put("isExist", CommonType.commonStatus.NO.getCode());
        data.put("shopBaseExist", CommonType.shopBaseExist.NO_OPEN_STORE.getCode());
        return data;
    }
}
