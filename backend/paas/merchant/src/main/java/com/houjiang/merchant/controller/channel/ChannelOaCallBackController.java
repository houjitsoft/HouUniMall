package com.houjiang.merchant.controller.channel;

import com.houjiang.merchant.service.IChannelOaCallBackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 公众号服务器验证及消息回复控制器
 * 回调接口已在SecurityConfig白名单中配置，无需认证
 * 
 * @author Amos
 * @since 2026-04-28
 */
@RestController
@RequestMapping("/adminapi/channel/oa/callback")
@Tag(name = "公众号服务器验证及消息回复")
public class ChannelOaCallBackController {

    @Resource
    private IChannelOaCallBackService iChannelOaCallBackService;

    /**
     * 公众号服务器地址验证（白名单接口，无需认证）
     * 
     * @param signature 微信签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param echostr 随机字符串
     * @return String 验证结果
     */
    @GetMapping(produces = "text/plain;charset=utf-8")
    @Operation(summary="公众号服务器地址验证")
    public String authGet(@RequestParam(name = "signature", required = false) String signature,
                          @RequestParam(name = "timestamp", required = false) String timestamp,
                          @RequestParam(name = "nonce", required = false) String nonce,
                          @RequestParam(name = "echostr", required = false) String echostr) {
        return iChannelOaCallBackService.checkSignature(signature, timestamp, nonce, echostr);
    }

    /**
     * 公众号消息回复（白名单接口，无需认证）
     * 
     * @param requestBody 请求体
     * @param signature 微信签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param encType 加密类型
     * @param msgSignature 消息签名
     * @return String 回复消息XML
     */
    @PostMapping(produces = "application/xml; charset=UTF-8")
    @Operation(summary="公众号消息回复")
    public String post(@RequestBody String requestBody,
                       @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature) {
        return iChannelOaCallBackService.post(requestBody, signature, timestamp, nonce, encType, msgSignature);
    }

}
