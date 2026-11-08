package com.houjiang.commons.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Amos
 */
@RestController
@RequestMapping("common/redis")
public class Demo {

    @Value("${spring.redis.host}")
    private String redishost;

    @Value("${spring.redis.port}")
    private Integer redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @GetMapping("/user")
    public ResponseResult queryUser(){
        return ResponseResult.success("Redis config: " + redishost + ":" + redisPort);
    }
}
