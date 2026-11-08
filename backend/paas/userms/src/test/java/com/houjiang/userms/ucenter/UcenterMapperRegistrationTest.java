package com.houjiang.userms.ucenter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.houjiang.userms.config.MyBatisConfig;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class UcenterMapperRegistrationTest {

    @Test
    void myBatisConfigurationRegistersUcenterMappers() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(MyBatisConfig.class);
            context.addBeanFactoryPostProcessor(beanFactory ->
                    Arrays.stream(beanFactory.getBeanDefinitionNames())
                            .forEach(name -> beanFactory.getBeanDefinition(name).setLazyInit(true)));
            context.refresh();

            assertTrue(context.containsBeanDefinition("ucenterSpaceMapper"),
                    "MyBatis must scan the ucenter mapper package before the service starts");
        }
    }
}
