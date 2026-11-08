package com.houjiang.plugin.update;

import com.houjiang.mall.mapper.update.PluginUpdateTaskMapper;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginUpdateTaskMapperContractTest {

    @Test
    void mapperIsDiscoverableFromMallDependencyJar() {
        assertTrue(
            PluginUpdateTaskMapper.class.isAnnotationPresent(Mapper.class),
            "PluginUpdateTaskMapper must carry @Mapper so Mall can discover it from the nested dependency JAR"
        );
    }
}
