package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.DingtalkServiceFeign;
import com.houjiang.bffweb.feign.OaServiceFeign;
import com.houjiang.bffweb.feign.OssServiceFeign;
import com.houjiang.bffweb.feign.ThirdpartyBaiduFeign;
import com.houjiang.bffweb.feign.ThirdpartyServiceFeign;
import com.houjiang.bffweb.dto.ThirdPartyConfigDTO;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 第三方服务Web控制器
/**
 * 
 * @author Amos
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/web/thirdparty")
public class WebThirdpartyController {

    @Autowired
    private ThirdpartyServiceFeign thirdpartyServiceFeign;

    @Autowired
    private ThirdpartyBaiduFeign thirdpartyBaiduFeign;

    @Autowired
    private DingtalkServiceFeign dingtalkServiceFeign;

    @Autowired
    private OaServiceFeign oaServiceFeign;

    @Autowired
    private OssServiceFeign ossServiceFeign;

    /**
     * 发送验证码
     */
    @PostMapping("/sms/register/selfshopphonecode")
    public ResponseResult sendCode(@RequestBody Map<String, String> paraMap) {
        return thirdpartyServiceFeign.sendCode(paraMap);
    }

    /**
     * 查询手机号当天的短信发送回执（运营商回执）
     * 用于“发送成功但收不到验证码”场景的用户自助排查，返回状态与友好错误文案（不含短信内容）
     */
    @PostMapping("/sms/deliveryDetails")
    public ResponseResult deliveryDetails(@RequestBody Map<String, String> paraMap) {
        return thirdpartyServiceFeign.deliveryDetails(paraMap);
    }

    /**
     * 百度翻译（商城 PC / H5 多语言 $t 使用，匿名可访问）
     */
    @PostMapping("/baidu/trans")
    public ResponseResult baiduTrans(@RequestBody Map<String, String> body) {
        return thirdpartyBaiduFeign.trans(body);
    }

    /**
     * 查询已启用语言列表（优先走 thirdparty；失败时由前端回退硬编码列表）
     */
    @PostMapping("/baidu/queryLanguageAll")
    public ResponseResult baiduQueryLanguageAll() {
        return thirdpartyBaiduFeign.queryLanguageAll();
    }

    /**
     * 上传文件到OSS
/**
     * 
     * @param file 上传的文件
     * @return 上传结果，包含文件URL
     */
    @PostMapping("/oss/uploadFile")
    public ResponseResult uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("开始上传文件: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
            ResponseResult result = ossServiceFeign.uploadFile(file);
            log.info("文件上传完成: {}", file.getOriginalFilename());
            return result;
        } catch (Exception e) {
            log.error("文件上传失败: {}", file.getOriginalFilename(), e);
            return ResponseResult.error("文件上传失败: " + e.getMessage());
        }
    }

    /** ================= 钉钉集成 ================= */

    /**
     * 获取 OSS 预签名直传地址（前端直传对象存储，不经应用服务器）。
     *
     * <p>2026-11-09 修复：商家入驻第 2 步的身份证正反面、合作协议、店铺图全部走这条预签名链路，
     * 但 BFF 侧此前没有这两个路由，前端 POST 过来一律被兜底成
     * {@code {"code":20001,"message":"请求的资源不存在"}}，表现是「选了文件、文件名也显示出来了，
     * 实际没传上去」。这里同时提供 POST（前端当前写法：body 传 fileName）与 GET（thirdparty 原生形态）
     * 两种入口，返回统一包装 {@code data = {uploadUrl, fileUrl}}，与前端 getPresignedUrl 的取值一致。
     *
     * @param body 含 fileName
     * @return data.uploadUrl 直传地址、data.fileUrl 访问地址
     */
    @PostMapping({"/oss/getPresignedUrl", "/web/oss/getPresignedUrl"})
    public ResponseResult getPresignedUrlByPost(@RequestBody Map<String, String> body) {
        return presignedUrl(body == null ? null : body.get("fileName"));
    }

    @GetMapping({"/oss/getPresignedUrl", "/web/oss/getPresignedUrl"})
    public ResponseResult getPresignedUrl(@RequestParam("fileName") String fileName) {
        return presignedUrl(fileName);
    }

    private ResponseResult presignedUrl(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return ResponseResult.error("fileName 不能为空");
        }
        try {
            Map<String, String> data = ossServiceFeign.getPresignedUrl(fileName.trim());
            return ResponseResult.success(data);
        } catch (Exception e) {
            log.error("获取 OSS 预签名地址失败: {}", fileName, e);
            return ResponseResult.error("获取上传地址失败: " + e.getMessage());
        }
    }

    @GetMapping("/dingtalk/config")
    public ResponseResult<ThirdPartyConfigDTO> getDingtalkConfig() {
        return dingtalkServiceFeign.getConfig();
    }

    @PostMapping("/dingtalk/config")
    public ResponseResult<String> saveDingtalkConfig(@RequestBody ThirdPartyConfigDTO config) {
        return dingtalkServiceFeign.saveConfig(config);
    }

    @PostMapping("/dingtalk/config/test")
    public ResponseResult<String> testDingtalkConfig(@RequestBody ThirdPartyConfigDTO config) {
        return dingtalkServiceFeign.testConfig(config);
    }

    @GetMapping("/dingtalk/health")
    public ResponseResult<Map<String, Object>> getDingtalkHealth() {
        return dingtalkServiceFeign.healthCheck();
    }

    @GetMapping("/dingtalk/attendance-platform")
    public ResponseResult<Map<String, Object>> getAttendancePlatformConfig() {
        return oaServiceFeign.getAttendancePlatformConfig();
    }

    @PostMapping("/dingtalk/attendance-platform")
    public ResponseResult<String> saveAttendancePlatformConfig(@RequestBody Map<String, Object> config) {
        return oaServiceFeign.saveAttendancePlatformConfig(config);
    }
}
