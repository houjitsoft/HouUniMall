package com.houjiang.example.test;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.util.JwtUtil;
import com.houjiang.commons.util.UUIDUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Commons 库功能测试示例
 * 
 * @author Amos
 * @date 2025-09-05
 */
@SpringBootTest
public class CommonsTestExample {

    /**
     * 测试 ResponseResult 功能
     */
    @Test
    public void testResponseResult() {
        // 测试成功响应
        ResponseResult<String> successResult = ResponseResult.success("操作成功");
        assertEquals(200, successResult.getCode());
        assertEquals("操作成功", successResult.getMessage());
        assertNull(successResult.getData());

        // 测试带数据的成功响应
        UserInfoVO user = new UserInfoVO();
        user.setId("123");
        user.setName("张三");
        
        ResponseResult<UserInfoVO> dataResult = ResponseResult.success("获取用户成功", user);
        assertEquals(200, dataResult.getCode());
        assertEquals("获取用户成功", dataResult.getMessage());
        assertNotNull(dataResult.getData());
        assertEquals("123", dataResult.getData().getId());
        assertEquals("张三", dataResult.getData().getName());

        // 测试错误响应
        ResponseResult<Void> errorResult = ResponseResult.error("操作失败");
        assertEquals(500, errorResult.getCode());
        assertEquals("操作失败", errorResult.getMessage());
        assertNull(errorResult.getData());

        // 测试自定义错误码
        ResponseResult<Void> customErrorResult = ResponseResult.error(1001, "用户不存在");
        assertEquals(1001, customErrorResult.getCode());
        assertEquals("用户不存在", customErrorResult.getMessage());
    }

    /**
     * 测试 StringUtils 功能
     */
    @Test
    public void testStringUtils() {
        // 测试空字符串判断
        assertTrue(StringUtils.isEmpty(""));
        assertTrue(StringUtils.isEmpty(null));
        assertFalse(StringUtils.isEmpty("hello"));
        assertTrue(StringUtils.isNotEmpty("hello"));
        assertFalse(StringUtils.isNotEmpty(""));

        // 测试空白字符串判断
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank("   "));
        assertTrue(StringUtils.isBlank(null));
        assertFalse(StringUtils.isBlank("hello"));
        assertTrue(StringUtils.isNotBlank("hello"));
        assertFalse(StringUtils.isNotBlank("   "));

        // 测试字符串转换
        assertEquals("userName", StringUtils.toCamelCase("user_name"));
        assertEquals("user_name", StringUtils.toSnakeCase("userName"));
        assertEquals("UserName", StringUtils.toPascalCase("user_name"));

        // 测试字符串截取
        assertEquals("hello", StringUtils.substring("hello world", 0, 5));
        assertEquals("world", StringUtils.substring("hello world", 6));

        // 测试字符串替换
        assertEquals("hello java", StringUtils.replace("hello world", "world", "java"));

        // 测试字符串分割
        String[] parts = StringUtils.split("a,b,c", ",");
        assertEquals(3, parts.length);
        assertEquals("a", parts[0]);
        assertEquals("b", parts[1]);
        assertEquals("c", parts[2]);

        // 测试字符串连接
        String joined = StringUtils.join(new String[]{"a", "b", "c"}, ",");
        assertEquals("a,b,c", joined);

        // 测试字符串去除空格
        assertEquals("hello", StringUtils.trim("  hello  "));
        assertEquals("hello world", StringUtils.trim("  hello world  "));
    }

    /**
     * 测试 JwtUtil 功能
     */
    @Test
    public void testJwtUtil() {
        String userId = "user123";
        String secret = "test-secret";

        // 测试生成 Token
        String token = JwtUtil.generateToken(userId, secret);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // 测试验证 Token
        boolean isValid = JwtUtil.validateToken(token, secret);
        assertTrue(isValid);

        // 测试从 Token 中获取用户ID
        String extractedUserId = JwtUtil.getUserIdFromToken(token);
        assertEquals(userId, extractedUserId);

        // 测试无效 Token
        boolean isInvalid = JwtUtil.validateToken("invalid-token", secret);
        assertFalse(isInvalid);
    }

    /**
     * 测试 UUIDUtils 功能
     */
    @Test
    public void testUUIDUtils() {
        // 测试生成标准 UUID
        String uuid1 = UUIDUtils.generateUUID();
        assertNotNull(uuid1);
        assertTrue(uuid1.contains("-"));
        assertEquals(36, uuid1.length());

        // 测试生成不带横线的 UUID
        String uuid2 = UUIDUtils.generateUUIDWithoutDash();
        assertNotNull(uuid2);
        assertFalse(uuid2.contains("-"));
        assertEquals(32, uuid2.length());

        // 测试生成短 UUID
        String shortUuid = UUIDUtils.generateShortUUID();
        assertNotNull(shortUuid);
        assertTrue(shortUuid.length() < 32);

        // 测试 UUID 唯一性
        String uuid3 = UUIDUtils.generateUUID();
        String uuid4 = UUIDUtils.generateUUID();
        assertNotEquals(uuid3, uuid4);
    }

    /**
     * 测试 UserInfoVO 功能
     */
    @Test
    public void testUserInfoVO() {
        UserInfoVO user = new UserInfoVO();
        user.setId("123");
        user.setName("张三");
        user.setEmail("zhangsan@example.com");
        user.setPhone("13800138000");

        assertEquals("123", user.getId());
        assertEquals("张三", user.getName());
        assertEquals("zhangsan@example.com", user.getEmail());
        assertEquals("13800138000", user.getPhone());

        // 测试 toString 方法
        String userString = user.toString();
        assertNotNull(userString);
        assertTrue(userString.contains("123"));
        assertTrue(userString.contains("张三"));
    }

    /**
     * 测试综合使用场景
     */
    @Test
    public void testIntegratedUsage() {
        // 模拟用户注册场景
        String userId = UUIDUtils.generateUUID();
        String userName = "test_user";
        String userEmail = "test@example.com";

        // 验证输入参数
        assertTrue(StringUtils.isNotBlank(userId));
        assertTrue(StringUtils.isNotBlank(userName));
        assertTrue(StringUtils.isNotBlank(userEmail));

        // 创建用户信息
        UserInfoVO user = new UserInfoVO();
        user.setId(userId);
        user.setName(userName);
        user.setEmail(userEmail);

        // 生成 JWT Token
        String token = JwtUtil.generateToken(userId, "secret");
        assertTrue(JwtUtil.validateToken(token, "secret"));

        // 返回成功响应
        ResponseResult<UserInfoVO> result = ResponseResult.success("用户注册成功", user);
        assertEquals(200, result.getCode());
        assertEquals("用户注册成功", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(userId, result.getData().getId());
        assertEquals(userName, result.getData().getName());
        assertEquals(userEmail, result.getData().getEmail());
    }

    /**
     * 测试错误处理场景
     */
    @Test
    public void testErrorHandling() {
        // 测试空值处理
        String nullValue = null;
        assertTrue(StringUtils.isBlank(nullValue));
        
        ResponseResult<Void> nullResult = ResponseResult.error("参数不能为空");
        assertEquals(500, nullResult.getCode());
        assertEquals("参数不能为空", nullResult.getMessage());

        // 测试无效 Token
        boolean isValid = JwtUtil.validateToken("invalid-token", "secret");
        assertFalse(isValid);

        // 测试自定义错误码
        ResponseResult<Void> customError = ResponseResult.error(1001, "用户不存在");
        assertEquals(1001, customError.getCode());
        assertEquals("用户不存在", customError.getMessage());
    }

    /**
     * 测试链式调用
     */
    @Test
    public void testChainedCalls() {
        // ResponseResult 支持链式调用
        ResponseResult<String> result = new ResponseResult<String>()
                .setCode(200)
                .setMessage("操作成功")
                .setData("test data");

        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals("test data", result.getData());
    }
}
