package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.MallStyleDefaults;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 商城配置 BFF（商城风格：显式转发 Authorization，供 mall MALL_ADMIN / Cloud token 鉴权）
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/mall/config")
@RequiredArgsConstructor
public class MallConfigBffController {

    private final MallServiceFeign mallServiceFeign;

    @GetMapping("/get_mall_style")
    @SuppressWarnings("unchecked")
    public ResponseResult getMallStyle(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) Long shop_id) {
        log.info("get_mall_style likeshop-v2, shop_id={}", shop_id);
        try {
            ResponseResult result = mallServiceFeign.getMallStyleConfig(authorization);
            if (result != null && result.getCode() == 1 && MallStyleDefaults.isLikeshopFormat(result.getData())) {
                Object raw = result.getData();
                if (raw instanceof Map<?, ?> map) {
                    Map<String, Object> style = new LinkedHashMap<>();
                    map.forEach((k, v) -> style.put(String.valueOf(k), v));
                    return ResponseResult.success(MallStyleDefaults.ensurePcFields(style));
                }
                return result;
            }
        } catch (Exception e) {
            log.warn("getMallStyleConfig 失败，使用默认商城风格: {}", e.getMessage());
        }
        return ResponseResult.success(MallStyleDefaults.all());
    }

    @PostMapping("/set_mall_style")
    @SuppressWarnings("unchecked")
    public ResponseResult setMallStyle(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> params) {
        Map<String, Object> body = params != null ? new LinkedHashMap<>(params) : new LinkedHashMap<>();
        // 保存前补齐 PC 楼层等字段，避免只改主题色时把楼层冲掉
        Object dataObj = body.get("data");
        if (dataObj instanceof Map<?, ?> dataMap) {
            Map<String, Object> data = new LinkedHashMap<>();
            dataMap.forEach((k, v) -> data.put(String.valueOf(k), v));
            body.put("data", MallStyleDefaults.ensurePcFields(data));
        }
        try {
            ResponseResult result = mallServiceFeign.setMallStyleConfig(body, authorization);
            if (result != null && result.getCode() == 1) {
                return result;
            }
            if (result != null) {
                return result;
            }
        } catch (Exception e) {
            log.warn("setMallStyleConfig 不可用，尝试旧接口: {}", e.getMessage());
        }
        return mallServiceFeign.setMallStyle(body, authorization);
    }
}
