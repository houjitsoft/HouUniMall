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
