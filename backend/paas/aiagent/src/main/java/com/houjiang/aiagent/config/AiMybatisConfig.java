package com.houjiang.aiagent.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分页插件。MyBatis-Plus 3.5.9 起 PaginationInnerInterceptor 依赖 jsqlparser 独立模块，
 * pom 里已显式引入 mybatis-plus-jsqlparser，缺它这里会编译不过。
 */
@Configuration
public class AiMybatisConfig {

    @Bean
    public MybatisPlusInterceptor aiMybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
