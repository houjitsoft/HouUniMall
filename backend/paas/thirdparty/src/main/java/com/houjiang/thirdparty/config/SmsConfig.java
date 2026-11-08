package com.houjiang.thirdparty.config;

import com.houjiang.thirdparty.enums.SmsStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 配置类，用于定义策略 Map
 * @author Amos
 */
@Configuration
public class SmsConfig {

    /**
     * 定义并注册公众号策略的Bean
     * 该方法用于初始化并返回一个包含多种公众号策略的Map对象
     * 每个公众号策略由一个代码标识与其对应的SmsStrategy实例相关联
     *
     * @return 包含所有公众号策略的Map，键为策略代码，值为对应的策略实例
     */
    @Bean
    public Map<String, SmsStrategy> smsStrategies() {
        // 创建一个链接哈希映射以保持插入顺序
        Map<String, SmsStrategy> map = new LinkedHashMap<>();
        // 初始化映射，将不同的公众号策略代码与对应的策略实例相关联
        // 发送企业认证审核通过通知
        map.put("CODE_A", SmsStrategy.CODE_A);
        // 发送企业认证审核驳回通知
        map.put("CODE_B", SmsStrategy.CODE_B);
        // 发送订单审批通知
        map.put("CODE_C", SmsStrategy.CODE_C);
        // 发送订单退款成功通知
        map.put("CODE_D", SmsStrategy.CODE_D);
        // 发送用户申请退货通知
        map.put("CODE_E", SmsStrategy.CODE_E);
        // 发送商家入驻申请审核结果通知
        map.put("CODE_F", SmsStrategy.CODE_F);
        // 发送订单付款成功通知
        map.put("CODE_G", SmsStrategy.CODE_G);
        // 发送用户退款申请通知
        map.put("CODE_H", SmsStrategy.CODE_H);
        // 发送用户退货申请通知
        map.put("CODE_I", SmsStrategy.CODE_I);
        // 发送新店铺入驻待审核通知
        map.put("CODE_J", SmsStrategy.CODE_J);
        // 发送企业注册待审核提醒通知
        map.put("CODE_K", SmsStrategy.CODE_K);
        // 发送付款凭证待审核通知
        map.put("CODE_L", SmsStrategy.CODE_L);
        // 发送售后单介入待处理通知
        map.put("CODE_M", SmsStrategy.CODE_M);
        // 发送退款拒绝通知
        map.put("CODE_N", SmsStrategy.CODE_N);
        // 发送商家发货通知
        map.put("CODE_O", SmsStrategy.CODE_O);
        // 兑换券活动通知
        map.put("CODE_P", SmsStrategy.CODE_P);
        // 返回初始化完成的策略映射
        return map;
    }

}
