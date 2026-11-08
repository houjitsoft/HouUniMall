package com.houjiang.auth.config;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MyBatisConfigTest {

    @Test
    void shouldScanAuthPermissionMappers() {
        MapperScan mapperScan = MyBatisConfig.class.getAnnotation(MapperScan.class);

        assertTrue(Arrays.asList(mapperScan.value()).contains("com.houjiang.auth.mapper"));
    }
}
