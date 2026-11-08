package com.houjiang.goods.plugin.standalone;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Standalone plugin example: the host service owns and serves this route. */
@RestController
@RequestMapping("/plugin/hello-world-standalone")
public class HelloWorldStandaloneController {
    @PostMapping("/hello")
    public Map<String, String> hello(@RequestBody(required = false) Map<String, Object> ignored) {
        return Map.of("message", "Hello World", "mode", "standalone");
    }
}
