package com.houjiang.mall.service.setting.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.mall.entity.setting.SettingMallPay;
import com.houjiang.mall.mapper.setting.SettingMallPayMapper;
import com.houjiang.mall.service.setting.SettingPayConfigService;
import com.houjiang.mall.validator.setting.PayConfigUpdateParam;
import com.houjiang.mall.vo.setting.SettingMallPayVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 支付配置 Service 实现。
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingPayConfigServiceImpl implements SettingPayConfigService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final SettingMallPayMapper mallPayMapper;

    @Override
    public List<SettingMallPayVo> list() {
        QueryWrapper<SettingMallPay> wrapper = new QueryWrapper<>();
        wrapper.isNull("delete_time");
        wrapper.orderByDesc(Arrays.asList("sort", "id"));

        List<SettingMallPay> records = mallPayMapper.selectList(wrapper);
        List<SettingMallPayVo> result = new LinkedList<>();
        for (SettingMallPay record : records) {
            // list 接口不返回 config 详情，前端在 detail 时再取
            result.add(toVo(record, false));
        }
        return result;
    }

    @Override
    public SettingMallPayVo detail(Integer id) {
        SettingMallPay record = mallPayMapper.selectOne(new QueryWrapper<SettingMallPay>()
                .eq("id", id)
                .isNull("delete_time")
                .last("limit 1"));
        if (record == null) {
            throw new IllegalArgumentException("支付配置不存在");
        }
        return toVo(record, true);
    }

    @Override
    public void edit(PayConfigUpdateParam param) {
        SettingMallPay model = mallPayMapper.selectOne(new QueryWrapper<SettingMallPay>()
                .eq("id", param.getId())
                .isNull("delete_time")
                .last("limit 1"));
        if (model == null) {
            throw new IllegalArgumentException("支付配置不存在");
        }

        model.setName(param.getName());
        model.setIcon(param.getIcon());
        model.setSort(param.getSort() == null ? 0 : param.getSort());
        model.setRemark(param.getRemark());

        // 余额支付（payType=1）配置固定为空对象
        if (model.getPayType() != null && model.getPayType() == 1) {
            model.setConfig("{}");
        } else if (param.getConfig() != null) {
            try {
                model.setConfig(OBJECT_MAPPER.writeValueAsString(param.getConfig()));
            } catch (Exception e) {
                throw new IllegalArgumentException("config 序列化失败: " + e.getMessage());
            }
        } else {
            model.setConfig("{}");
        }

        model.setUpdateTime(System.currentTimeMillis() / 1000L);
        mallPayMapper.updateById(model);
    }

    /** Entity → Vo（withConfig=true 时反序列化 config） */
    private SettingMallPayVo toVo(SettingMallPay entity, boolean withConfig) {
        SettingMallPayVo vo = new SettingMallPayVo();
        BeanUtils.copyProperties(entity, vo, "config");
        vo.setPayTypeName(getPayTypeName(entity.getPayType()));
        if (withConfig) {
            vo.setConfig(parseConfig(entity.getConfig()));
        } else {
            // 列表接口返回空对象，避免下发大段配置
            vo.setConfig(parseConfig("{}"));
        }
        return vo;
    }

    /** 把 JSON 字符串反序列化为 Object（解析失败时返回原字符串） */
    private Object parseConfig(String configStr) {
        if (configStr == null || configStr.isEmpty()) {
            return null;
        }
        // 兼容 like-admin 把 [] 当空对象的历史脏数据
        String normalized = configStr.replace("[]", "{}");
        try {
            JsonNode node = OBJECT_MAPPER.readTree(normalized);
            return OBJECT_MAPPER.convertValue(node, Object.class);
        } catch (Exception e) {
            log.warn("[pay_config] 反序列化失败，返回原字符串: {}", e.getMessage());
            return configStr;
        }
    }

    /** 支付方式名称（与 like-admin PaymentEnum.getPayWayMsg 等价） */
    private String getPayTypeName(Integer code) {
        if (code == null) {
            return "未知";
        }
        switch (code) {
            case 0: return "其他";
            case 1: return "余额支付";
            case 2: return "微信支付";
            case 3: return "支付宝支付";
            default: return "未知";
        }
    }
}
