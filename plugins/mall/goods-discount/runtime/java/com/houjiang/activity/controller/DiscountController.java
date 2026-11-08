package com.houjiang.activity.controller;

import com.houjiang.activity.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 限时折扣控制器
 *
 * @author Amos
 * @date 2026-01-04
 */
@RestController
@RequestMapping("/discount")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping("/query")
    public Map<String, Object> queryDiscountList(@RequestBody Map<String, Object> param) {
        try {
            Map<String, Object> result = discountService.queryDiscountList(param);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", result);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    @PostMapping("/detail")
    public Map<String, Object> getDiscountDetail(@RequestBody Map<String, Object> param) {
        try {
            Map<String, Object> result = discountService.getDiscountDetail(param);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", result);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    @PostMapping("/add")
    public Map<String, Object> addDiscount(@RequestBody Map<String, Object> discountData) {
        try {
            boolean success = discountService.addDiscount(discountData);
            Map<String, Object> response = new HashMap<>();
            response.put("code", success ? 200 : 500);
            response.put("message", success ? "新增成功" : "新增失败");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "新增失败：" + e.getMessage());
            return response;
        }
    }

    @PostMapping("/update")
    public Map<String, Object> updateDiscount(@RequestBody Map<String, Object> discountData) {
        try {
            boolean success = discountService.updateDiscount(discountData);
            Map<String, Object> response = new HashMap<>();
            response.put("code", success ? 200 : 500);
            response.put("message", success ? "更新成功" : "更新失败");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新失败：" + e.getMessage());
            return response;
        }
    }

    @PostMapping("/delete")
    public Map<String, Object> deleteDiscount(@RequestBody Map<String, Object> param) {
        try {
            boolean success = discountService.deleteDiscount(param);
            Map<String, Object> response = new HashMap<>();
            response.put("code", success ? 200 : 500);
            response.put("message", success ? "删除成功" : "删除失败");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "删除失败：" + e.getMessage());
            return response;
        }
    }

    @PostMapping("/start")
    public Map<String, Object> startDiscount(@RequestBody Map<String, Object> param) {
        try {
            boolean success = discountService.startDiscount(param);
            Map<String, Object> response = new HashMap<>();
            response.put("code", success ? 200 : 500);
            response.put("message", success ? "启动成功" : "启动失败");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "启动失败：" + e.getMessage());
            return response;
        }
    }

    @PostMapping("/goods/query")
    public Map<String, Object> queryDiscountGoods(@RequestBody Map<String, Object> param) {
        try {
            Map<String, Object> result = discountService.queryDiscountGoods(param);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", result);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }
}
