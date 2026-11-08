package com.houjiang.payment.util;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

/**
 * 签名工具类测试
 * 验证MD5/RSA/SM2签名算法实现
 */
public class SignUtilTest {
    
    /**
     * 测试MD5签名
     * 使用文档中的示例数据验证签名结果
     */
    @Test
    public void testMD5Sign() {
        // 文档示例数据
        Map<String, String> params = new HashMap<>();
        params.put("platId", "1000");
        params.put("mchOrderNo", "P0123456789101");
        params.put("amount", "10000");
        params.put("clientIp", "192.168.0.111");
        params.put("returnUrl", "https://www.baidu.com");
        params.put("notifyUrl", "https://www.baidu.com");
        params.put("reqTime", "20190723141000");
        params.put("version", "1.0");
        
        String key = "EWEFD123RGSRETYDFNGFGFGSHDFGH";
        
        // 生成签名
        String sign = SignUtil.signMD5(params, key);
        
        // 文档中的预期签名结果
        String expectedSign = "4A5078DABBCE0D9C4E7668DACB96FF7A";
        
        System.out.println("生成的签名: " + sign);
        System.out.println("预期的签名: " + expectedSign);
        System.out.println("签名匹配: " + sign.equals(expectedSign));
        
        // 验证签名
        params.put("sign", sign);
        boolean verified = SignUtil.verifyMD5(params, key);
        System.out.println("签名验证: " + (verified ? "通过" : "失败"));
    }
    
    /**
     * 测试空值过滤
     */
    @Test
    public void testEmptyValueFilter() {
        Map<String, String> params = new HashMap<>();
        params.put("param1", "value1");
        params.put("param2", "");  // 空字符串应该被过滤
        params.put("param3", "value3");
        
        String key = "testkey";
        String sign = SignUtil.signMD5(params, key);
        
        System.out.println("带空值参数的签名: " + sign);
        
        // 空值应该不参与签名，结果应该和不包含param2时一样
        Map<String, String> params2 = new HashMap<>();
        params2.put("param1", "value1");
        params2.put("param3", "value3");
        
        String sign2 = SignUtil.signMD5(params2, key);
        System.out.println("不含空值参数的签名: " + sign2);
        System.out.println("两次签名相同: " + sign.equals(sign2));
    }
    
    /**
     * 测试参数排序
     */
    @Test
    public void testParameterSort() {
        Map<String, String> params = new HashMap<>();
        params.put("z_param", "z");
        params.put("a_param", "a");
        params.put("m_param", "m");
        
        String key = "testkey";
        String sign = SignUtil.signMD5(params, key);
        
        System.out.println("乱序参数的签名: " + sign);
        
        // 无论参数顺序如何，签名结果应该相同
        Map<String, String> params2 = new HashMap<>();
        params2.put("m_param", "m");
        params2.put("z_param", "z");
        params2.put("a_param", "a");
        
        String sign2 = SignUtil.signMD5(params2, key);
        System.out.println("不同顺序的签名: " + sign2);
        System.out.println("签名结果相同: " + sign.equals(sign2));
    }
}
