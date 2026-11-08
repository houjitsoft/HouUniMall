package com.houjiang.payment.controller;

import cn.hutool.crypto.SecureUtil;
import com.houjiang.payment.service.WeChatPayServices;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Arrays;

/**
 * @author Amos
 */
@RestController
@RequestMapping("weChat")
public class WeChatController {

    @Resource
    private WeChatPayServices weChatPayServices;

    @GetMapping("/callback")
    public String wechatServer(@RequestParam(name = "signature") String signature,
                               @RequestParam(name = "timestamp") String timestamp,
                               @RequestParam(name = "nonce") String nonce,
                               @RequestParam(name = "echostr") String echostr) {
        System.out.println("signature：    " + signature);
        String token = "zxcvbnmasdfghjklqwertyuiop";
        String[] arr = new String[]{token, timestamp, nonce};
        Arrays.sort(arr);
        String s = SecureUtil.sha1(arr[0] + arr[1] + arr[2]);
        return signature.equals(s) ? echostr : "";
    }


    @PostMapping(value = "/callback", produces = "application/xml; charset=UTF-8")
    public String wechatPost(@RequestBody String requestBody) throws Exception {
        System.out.println("requestBody：    " + requestBody);
        return weChatPayServices.wechatPost(requestBody);
    }

}
