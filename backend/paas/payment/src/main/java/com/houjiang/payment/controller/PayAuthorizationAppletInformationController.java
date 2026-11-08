package com.houjiang.payment.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @ClassName PayAuthorizationAppletInformationController
 * @Description 授权小程序信息表
 * @Author Amos
 * @Date 2023-07-20 09:39:46
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("pay/authorizationAppletInformation")
public class PayAuthorizationAppletInformationController extends BaseController {

    /**
     * 查询品牌店铺小程序授权配置
     */
    @PostMapping("/queryConfigFeign")
    public ResponseResult queryShopAppletConfigFeign(@RequestBody Map<String, Object> map) {
        ResponseResult result = payAuthorizationAppletInformationService.queryShopAppletConfigFeign(map);
        return result;
    }

    /**
     * 小程序配置查询
     * 
     * @param map
     * @return
     */
    @PostMapping("/query/secret")
    public ResponseResult querysecret(@RequestBody Map<String, Object> map) {
        ResponseResult result = payAuthorizationAppletInformationService.querysecret(map);
        return result;
    }

    /**
     * 发布审核通过的小程序
     * 
     * @param map
     * @return
     */
    @PostMapping("/release")
    public ResponseResult release(@RequestBody Map<String, Object> map) {
        ResponseResult result = payAuthorizationAppletInformationService.release(map);
        return result;
    }

    /**
     * 设置小程序用户隐私指引
     * 
     * @param map
     * @return
     */
    @PostMapping("/setPrivacySetting")
    public ResponseResult setPrivacySetting(@RequestBody Map<String, Object> map) {
        ResponseResult result = payAuthorizationAppletInformationService.setPrivacySetting(map);
        return result;
    }

    /**
     * 填写小程序信息
     * 
     * @param map
     * @return
     */
    @PostMapping("/update/secret")
    public ResponseResult updatesecret(@RequestBody Map<String, Object> map) {
        ResponseResult result = payAuthorizationAppletInformationService.updatesecret(map);
        return result;
    }

    /**
     * 查看最近一次审核状态
     * 
     * @param map
     * @return
     */
    @PostMapping("/getLatestAuditStatus")
    public ResponseResult getLatestAuditStatus(@RequestBody Map<String, Object> map) {
        ResponseResult result = payAuthorizationAppletInformationService.getLatestAuditStatus(map);
        return result;
    }

    /**
     * 提交代码审核
     * 
     * @param map
     * @return
     */
    @PostMapping("/submitAudit")
    public ResponseResult submitAuditUrl(@RequestBody Map<String, Object> map) {
        ResponseResult result = payAuthorizationAppletInformationService.submitAuditUrl(map);
        return result;
    }

    /**
     * 上传提审素材
     * 
     * @param mediaFile type 上传类型 1 图片 2 视频
     * @param token
     * @return
     */
    @GetMapping("/upload/mediaToCodeAudit")
    public ResponseResult uploadMediaToCodeAudit(@RequestParam MultipartFile mediaFile, @RequestParam String token) {
        ResponseResult result = payAuthorizationAppletInformationService.uploadMediaToCodeAudit(mediaFile, token);
        return result;
    }

    /**
     * 获取体验二维码
     * 
     * @param map
     * @return
     */
    @PostMapping("/get/qrcode")
    public ResponseResult getqrcode(@RequestBody Map<String, Object> map) {
        ResponseResult result = payAuthorizationAppletInformationService.getqrcode(map);
        return result;
    }

    /**
     * 上传代码
     * 
     * @param map
     * @return
     */
    @PostMapping("/commit")
    public ResponseResult commit(@RequestBody Map<String, Object> map) {
        ResponseResult result = payAuthorizationAppletInformationService.commit(map);
        return result;
    }

    /**
     * 获取代码模板列表
     * 
     * @param map
     * @return
     */
    @PostMapping("/get/templateList")
    public ResponseResult addToTemplate(@RequestBody Map<String, Object> map) {
        ResponseResult result = payAuthorizationAppletInformationService.addToTemplate(map);
        return result;
    }

    /**
     * 获取pre_auth_code预授权码
     * 
     * @param map
     * @return
     */
    @PostMapping("/get/preauth/code")
    public ResponseResult getPreauthCode(@RequestBody Map<String, Object> map) {
        ResponseResult result = payAuthorizationAppletInformationService.getPreauthCode(map);
        return result;
    }

    /**
     * 查询该店铺是否授权过小程序
     * 
     * @param map
     * @return
     */
    @PostMapping("/checkAuth")
    public ResponseResult checkAuth(@RequestBody Map<String, Object> map) {
        ResponseResult result = payAuthorizationAppletInformationService.checkAuth(map);
        return result;
    }

    /**
     * 获得授权事件的票据.
     *
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param msgSignature 消息体签名
     * @param postData 消息体
     * @return 如果获得只需要返回 SUCCESS
     */
    @RequestMapping(value = "/ticket" ,produces = "application/json; charset=utf-8")
    public String ticket(@RequestParam("timestamp") String timestamp, @RequestParam("nonce") String nonce,
                         @RequestParam("msg_signature") String msgSignature, @RequestBody String postData) {
        String result = payAuthorizationAppletInformationService.ticket(timestamp,nonce,msgSignature,postData);
        return result;
    }

    /**
     * 回调callback.
     */
    @RequestMapping(value = "/callback")
    public Object callback(@RequestBody String xml,
                           @RequestParam(value = "signature", required = false) String signature,
                           @RequestParam(value = "timestamp", required = false) String timeStamp,
                           @RequestParam(value = "nonce", required = false) String nonce,
                           @RequestParam(value = "encrypt_type", required = false) String encryptType,
                           @RequestParam(value = "msg_signature", required = false) String msgSignature) throws Exception {
        return "success";
    }

    /**
     * 授权回调URL
     * 接收返回的授权码和过期时间(redirect_url?auth_code=xxx&expires_in=600)
     * 使用授权码获取授权信息authorization_info（包含接口调用令牌和刷新令牌）
     */
    @GetMapping("/authcode")
    public void authcode(HttpServletResponse response, @RequestParam("auth_code") String authCode, @RequestParam("expires_in") String expiresIn, @RequestParam("token")String token) {
        payAuthorizationAppletInformationService.authcode(response,authCode,expiresIn,token);
    }

}
