package com.houjiang.thirdparty.config;

import com.houjiang.thirdparty.enums.OfficialAccountStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 配置类，用于定义策略 Map
 * @author Amos
 */
@Configuration
public class OfficialAccountConfig {

    /**
     * 定义并注册公众号策略的Bean
     * 该方法用于初始化并返回一个包含多种公众号策略的Map对象
     * 每个公众号策略由一个代码标识与其对应的OfficialAccountStrategy实例相关联
     *
     * @return 包含所有公众号策略的Map，键为策略代码，值为对应的策略实例
     */
    @Bean
    public Map<String, OfficialAccountStrategy> officialAccountStrategies() {
        // 创建一个链接哈希映射以保持插入顺序
        Map<String, OfficialAccountStrategy> map = new LinkedHashMap<>();
        // 初始化映射，将不同的公众号策略代码与对应的策略实例相关联
        // 发送企业认证审核通过通知
        map.put("CODE_A", OfficialAccountStrategy.CODE_A);
        // 发送企业认证审核驳回通知
        map.put("CODE_B", OfficialAccountStrategy.CODE_B);
        // 发送订单审批通知
        map.put("CODE_C", OfficialAccountStrategy.CODE_C);
        // 发送订单退款成功通知
        map.put("CODE_D", OfficialAccountStrategy.CODE_D);
        // 发送用户申请退货通知
        map.put("CODE_E", OfficialAccountStrategy.CODE_E);
        // 发送商家入驻申请审核结果通知
        map.put("CODE_F", OfficialAccountStrategy.CODE_F);
        // 发送订单付款成功通知
        map.put("CODE_G", OfficialAccountStrategy.CODE_G);
        // 发送用户退款申请通知
        map.put("CODE_H", OfficialAccountStrategy.CODE_H);
        // 发送用户退货申请通知
        map.put("CODE_I", OfficialAccountStrategy.CODE_I);
        // 发送退款拒绝通知
        map.put("CODE_N", OfficialAccountStrategy.CODE_N);
        // 发送商家发货通知
        map.put("CODE_O", OfficialAccountStrategy.CODE_O);
        // 返回初始化完成的策略映射
        return map;
    }

}
