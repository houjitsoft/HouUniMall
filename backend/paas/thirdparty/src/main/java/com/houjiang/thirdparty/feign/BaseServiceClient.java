package com.houjiang.thirdparty.feign;

import com.houjiang.commons.dto.goods.NavSortLogoDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 调用 paas base（短信/OSS 配置等）。
 * <p>
 * 必须配置直连 URL：线上曾出现 Nacos 解析 {@code base} 后 Connect timed out（约 10s×重试），
 * 叠加在发短信链路里会把 BFF Feign（60s）打满，前端表现为 {@code timeout of 60000ms exceeded}。
 * 未配置时默认本机 8214（与 bff-web {@code feign.base.url} 一致）。
 * @author Amos
 */
@FeignClient(
        name = "base",
        url = "${feign.base.url:http://127.0.0.1:8214}",
        fallback = BaseServiceClientFallback.class
)
public interface BaseServiceClient {


    /**
     * 查询oss 配置
     *
     * @param ossType
     * @return
     */
    @PostMapping("/base/ossConfig/feign/oss/selectOSSInfo")
    ResponseResult selectOSSInfo(@RequestParam("ossType") String ossType);

    /**
     * 查询oss 配置
     *
     * @return
     */
    @PostMapping("/base/ossConfig/queryOssStates")
    String queryOssStates();

    /**
     * 查询开启的sms配置
     * @return 开启的sms配置
     */
    @PostMapping("/base/sms/querySmsStates")
    ResponseResult<String> querySmsStates();

    /**
     * 根据短信类型查询sms配置
     * @return 开启的sms配置
     */
    @GetMapping("/base/sms/selectSMSInfoByType")
    ResponseResult<Map<String, Object>> selectSMSInfoByType(@RequestParam("type") String type);

    /**
     * 查询配置
     */
    @PostMapping("base/sysConfig/quertNavLogo")
    ResponseResult quertNavLogo(@RequestBody NavSortLogoDTO gdbNavSortLogoDTO);

    /**
     * 查询消息模板列表
     *
     * @param templateId 查询参数
     * @return 消息模板列表
     */
    @PostMapping("/baseMessageTemplate/selectListByTemplateId")
    ResponseResult selectListByTemplateId(@RequestParam Integer templateId);
    
    /**
     * 查询短信设置（验证码有效期、记录有效期）
     * 
     * @return 短信设置
     */
    @GetMapping("/base/sms/getSmsSetting")
    ResponseResult getSmsSetting();
}
