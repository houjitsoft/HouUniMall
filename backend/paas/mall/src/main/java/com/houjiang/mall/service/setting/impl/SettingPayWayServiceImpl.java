package com.houjiang.mall.service.setting.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.mall.entity.setting.SettingMallPay;
import com.houjiang.mall.entity.setting.SettingMallPayScene;
import com.houjiang.mall.mapper.setting.SettingMallPayMapper;
import com.houjiang.mall.mapper.setting.SettingMallPaySceneMapper;
import com.houjiang.mall.service.setting.SettingPayWayService;
import com.houjiang.mall.vo.setting.SettingPaymentMethodVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城后台 - 支付方式 Service 实现。
 * 对应 like-admin SettingPaymentServiceImpl#getPayWay / setPayWay。
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingPayWayServiceImpl implements SettingPayWayService {

    private final SettingMallPaySceneMapper paySceneMapper;
    private final SettingMallPayMapper mallPayMapper;

    /**
     * 按场景分组返回支付方式列表。
     * 关联 hou_merchant_mall_pay 取 payType（派生 payTypeName）和 icon。
     */
    @Override
    public Map<Integer, List<SettingPaymentMethodVo>> list() {
        List<SettingMallPayScene> scenes = paySceneMapper.selectList(
                new QueryWrapper<SettingMallPayScene>()
                        .isNull("delete_time")
                        .orderByAsc("id")
        );

        Map<Integer, List<SettingPaymentMethodVo>> result = new LinkedHashMap<>();
        // 默认输出微信小程序/微信公众号/H5 三档（即使无数据也保持 key 存在）
        result.put(1, new ArrayList<>());
        result.put(2, new ArrayList<>());
        result.put(3, new ArrayList<>());

        for (SettingMallPayScene scene : scenes) {
            SettingMallPay mallPay = null;
            if (scene.getMallPayId() != null) {
                mallPay = mallPayMapper.selectOne(new QueryWrapper<SettingMallPay>()
                        .eq("id", scene.getMallPayId())
                        .isNull("delete_time")
                        .last("limit 1"));
            }

            SettingPaymentMethodVo vo = new SettingPaymentMethodVo();
            vo.setId(scene.getId());
            vo.setPayConfigId(scene.getMallPayId());
            vo.setScene(scene.getScene());
            vo.setStatus(scene.getStatus());
            vo.setIsDefault(scene.getIsDefault());
            if (mallPay != null) {
                vo.setIcon(mallPay.getIcon());
                vo.setPayTypeName(buildPayTypeName(mallPay.getPayType()));
                vo.setPayMethod(buildPayMethod(mallPay.getPayType()));
            }

            Integer sceneCode = scene.getScene() == null ? 0 : scene.getScene();
            result.computeIfAbsent(sceneCode, k -> new ArrayList<>()).add(vo);
        }
        return result;
    }

    private String buildPayMethod(Integer payType) {
        if (payType == null) return null;
        return switch (payType) {
            case 1 -> "BALANCE";
            case 2 -> "WECHAT";
            case 3 -> "ALIPAY";
            case 4 -> "STRIPE";
            default -> null;
        };
    }

    /**
     * 批量更新支付方式：按入参 Map 遍历每个场景下的列表，仅更新 isDefault / status / updateTime。
     * 校验：id 必填且必须存在。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(Map<Integer, List<SettingPaymentMethodVo>> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis() / 1000L;
        for (Map.Entry<Integer, List<SettingPaymentMethodVo>> entry : data.entrySet()) {
            List<SettingPaymentMethodVo> list = entry.getValue();
            if (list == null || list.isEmpty()) {
                continue;
            }
            for (SettingPaymentMethodVo vo : list) {
                if (vo.getId() == null) {
                    throw new IllegalArgumentException("支付方式 id 不能为空");
                }
                SettingMallPayScene update = new SettingMallPayScene();
                update.setIsDefault(vo.getIsDefault());
                update.setStatus(vo.getStatus());
                update.setUpdateTime(now);
                int rows = paySceneMapper.update(update,
                        new QueryWrapper<SettingMallPayScene>()
                                .eq("id", vo.getId())
                                .isNull("delete_time"));
                if (rows == 0) {
                    log.warn("[B1-4 PayWay] update miss scene id={}", vo.getId());
                }
            }
        }
    }

    /**
     * 由支付类型派生中文名（避免依赖 like-common 的 PaymentEnum）。
     */
    private String buildPayTypeName(Integer payType) {
        if (payType == null) return "";
        switch (payType) {
            case 1: return "余额支付";
            case 2: return "微信支付";
            case 3: return "支付宝支付";
            case 4: return "Stripe支付";
            default: return "未知支付方式";
        }
    }
}
