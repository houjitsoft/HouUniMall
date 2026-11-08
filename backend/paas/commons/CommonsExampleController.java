package com.houjiang.example.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.util.JwtUtil;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.UUIDUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.dto.order.OrderDTO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Commons 库使用示例控制器
 * 
 * @author Amos
 * @date 2025-09-05
 */
@RestController
@RequestMapping("/example")
public class CommonsExampleController {

    /**
     * 示例1: 使用 ResponseResult 统一响应格式
     */
    @GetMapping("/response-example")
    public ResponseResult<String> responseExample() {
        // 成功响应
        return ResponseResult.success("操作成功");
    }

    /**
     * 示例2: 带数据的成功响应
     */
    @GetMapping("/data-example")
    public ResponseResult<UserInfoVO> dataExample() {
        UserInfoVO user = new UserInfoVO();
        user.setId("123");
        user.setName("张三");
        user.setEmail("zhangsan@example.com");
        
        return ResponseResult.success("获取用户信息成功", user);
    }

    /**
     * 示例3: 错误响应
     */
    @GetMapping("/error-example")
    public ResponseResult<Void> errorExample() {
        return ResponseResult.error("操作失败");
    }

    /**
     * 示例4: 自定义错误码
     */
    @GetMapping("/custom-error-example")
    public ResponseResult<Void> customErrorExample() {
        return ResponseResult.error(1001, "用户不存在");
    }

    /**
     * 示例5: 使用 StringUtils 进行字符串处理
     */
    @PostMapping("/string-example")
    public ResponseResult<Map<String, Object>> stringExample(@RequestBody Map<String, String> input) {
        String text = input.get("text");
        
        if (StringUtils.isBlank(text)) {
            return ResponseResult.error("输入文本不能为空");
        }
        
        Map<String, Object> result = Map.of(
            "original", text,
            "trimmed", StringUtils.trim(text),
            "camelCase", StringUtils.toCamelCase(text),
            "snakeCase", StringUtils.toSnakeCase(text),
            "length", text.length(),
            "isEmpty", StringUtils.isEmpty(text),
            "isBlank", StringUtils.isBlank(text)
        );
        
        return ResponseResult.success("字符串处理完成", result);
    }

    /**
     * 示例6: 使用 JwtUtil 生成和验证 Token
     */
    @PostMapping("/jwt-example")
    public ResponseResult<Map<String, String>> jwtExample(@RequestBody Map<String, String> userInfo) {
        String userId = userInfo.get("userId");
        String secret = "your-secret-key";
        
        if (StringUtils.isBlank(userId)) {
            return ResponseResult.error("用户ID不能为空");
        }
        
        // 生成 Token
        String token = JwtUtil.generateToken(userId, secret);
        
        // 验证 Token
        boolean isValid = JwtUtil.validateToken(token, secret);
        
        Map<String, String> result = Map.of(
            "token", token,
            "userId", userId,
            "isValid", String.valueOf(isValid)
        );
        
        return ResponseResult.success("JWT Token 生成成功", result);
    }

    /**
     * 示例7: 使用 RedisUtil 进行缓存操作
     */
    @PostMapping("/redis-example")
    public ResponseResult<Map<String, Object>> redisExample(@RequestBody Map<String, String> data) {
        String key = data.get("key");
        String value = data.get("value");
        
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return ResponseResult.error("键和值不能为空");
        }
        
        // 设置缓存
        RedisUtil.set(key, value, 3600); // 1小时过期
        
        // 获取缓存
        String cachedValue = RedisUtil.get(key);
        
        // 检查键是否存在
        boolean exists = RedisUtil.hasKey(key);
        
        // 获取剩余过期时间
        long ttl = RedisUtil.getExpire(key);
        
        Map<String, Object> result = Map.of(
            "key", key,
            "value", value,
            "cachedValue", cachedValue,
            "exists", exists,
            "ttl", ttl
        );
        
        return ResponseResult.success("Redis 操作完成", result);
    }

    /**
     * 示例8: 使用 UUIDUtils 生成唯一标识
     */
    @GetMapping("/uuid-example")
    public ResponseResult<Map<String, String>> uuidExample() {
        Map<String, String> result = Map.of(
            "standardUuid", UUIDUtils.generateUUID(),
            "uuidWithoutDash", UUIDUtils.generateUUIDWithoutDash(),
            "shortUuid", UUIDUtils.generateShortUUID()
        );
        
        return ResponseResult.success("UUID 生成完成", result);
    }

    /**
     * 示例9: 使用 DTO 进行数据传输
     */
    @PostMapping("/dto-example")
    public ResponseResult<OrderDTO> dtoExample(@RequestBody OrderDTO orderDTO) {
        // 验证订单数据
        if (StringUtils.isBlank(orderDTO.getOrderId())) {
            return ResponseResult.error("订单ID不能为空");
        }
        
        if (orderDTO.getAmount() == null || orderDTO.getAmount().doubleValue() <= 0) {
            return ResponseResult.error("订单金额必须大于0");
        }
        
        // 设置默认值
        if (StringUtils.isBlank(orderDTO.getStatus())) {
            orderDTO.setStatus("PENDING");
        }
        
        // 模拟保存订单
        orderDTO.setCreateTime(System.currentTimeMillis());
        
        return ResponseResult.success("订单创建成功", orderDTO);
    }

    /**
     * 示例10: 文件上传处理
     */
    @PostMapping("/upload-example")
    public ResponseResult<Map<String, String>> uploadExample(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseResult.error("文件不能为空");
        }
        
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = StringUtils.substringAfterLast(originalFilename, ".");
            String fileName = UUIDUtils.generateUUID() + "." + extension;
            
            // 保存文件
            String uploadPath = "/tmp/uploads/";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            File destFile = new File(uploadPath + fileName);
            file.transferTo(destFile);
            
            Map<String, String> result = Map.of(
                "originalName", originalFilename,
                "fileName", fileName,
                "filePath", destFile.getAbsolutePath(),
                "fileSize", String.valueOf(file.getSize())
            );
            
            return ResponseResult.success("文件上传成功", result);
            
        } catch (IOException e) {
            return ResponseResult.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 示例11: 批量操作
     */
    @PostMapping("/batch-example")
    public ResponseResult<Map<String, Object>> batchExample(@RequestBody List<Map<String, String>> items) {
        if (items == null || items.isEmpty()) {
            return ResponseResult.error("批量数据不能为空");
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (Map<String, String> item : items) {
            try {
                // 模拟处理每个项目
                String name = item.get("name");
                if (StringUtils.isNotBlank(name)) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                failCount++;
            }
        }
        
        Map<String, Object> result = Map.of(
            "totalCount", items.size(),
            "successCount", successCount,
            "failCount", failCount
        );
        
        return ResponseResult.success("批量处理完成", result);
    }

    /**
     * 示例12: 分页查询
     */
    @GetMapping("/page-example")
    public ResponseResult<Map<String, Object>> pageExample(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        // 参数验证
        if (page < 1) {
            return ResponseResult.error("页码必须大于0");
        }
        
        if (size < 1 || size > 100) {
            return ResponseResult.error("每页大小必须在1-100之间");
        }
        
        // 模拟分页数据
        int total = 100; // 模拟总数
        int totalPages = (total + size - 1) / size;
        
        Map<String, Object> result = Map.of(
            "page", page,
            "size", size,
            "total", total,
            "totalPages", totalPages,
            "hasNext", page < totalPages,
            "hasPrev", page > 1,
            "keyword", StringUtils.isBlank(keyword) ? "" : keyword
        );
        
        return ResponseResult.success("分页查询成功", result);
    }
}
