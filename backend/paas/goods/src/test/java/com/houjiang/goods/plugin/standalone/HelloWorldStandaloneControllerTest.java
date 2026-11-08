package com.houjiang.goods.plugin.standalone;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class HelloWorldStandaloneControllerTest {
    @Test
    void returnsGreetingWithoutBusinessSideEffects() {
        var response = new HelloWorldStandaloneController().hello(Map.of("ignored", true));

        assertEquals("Hello World", response.get("message"));
        assertEquals("standalone", response.get("mode"));
    }
}
