package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * OSS服务Feign客户端
/**
 * 
 * @author Amos
 * @since 2024-01-26
 */
/**
 * OSS 服务 Feign 客户端（paas thirdparty 的 /web/oss/*）。
 *
 * <p>2026-11-09 修正服务名：原先写成 {@code name = "thirdparty-oss"}，而 Nacos 里注册的服务名是
 * {@code thirdparty}（与 {@link ThirdpartyServiceFeign} 一致），调用时直接报
 * {@code Load balancer does not contain an instance for the service thirdparty-oss}，
 * 导致商家入驻的预签名上传地址永远取不到。这里对齐为 {@code thirdparty}，
 * 认证上下文仍用独立的 {@code contextId = "ossServiceFeign"}，不影响其它 Feign 客户端配置。
 */
@FeignClient(
    name = "thirdparty",
    url = "${feign.thirdparty.url:http://127.0.0.1:8194}",
    path = "/web",
    contextId = "ossServiceFeign"
)
public interface OssServiceFeign {

    /**
     * 上传文件到OSS
/**
     * 
     * @param file 上传的文件
     * @return 上传结果，包含文件URL
     */
    @PostMapping(value = "/oss/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseResult uploadFile(@RequestPart("file") MultipartFile file);

    /**
     * 获取 OSS 预签名上传地址（商家入驻证件、合作协议等前端直传场景使用）。
     *
     * <p>thirdparty 侧该接口是 GET + fileName 查询参数，返回 {@code {uploadUrl, fileUrl}}，
     * 不含统一响应包装，因此这里保持原始 Map 返回，由 Controller 包一层 ResponseResult。
     *
     * @param fileName 文件名（带后缀，服务端据此生成对象名与目录）
     * @return uploadUrl 直传地址、fileUrl 访问地址
     */
    @GetMapping("/oss/getPresignedUrl")
    Map<String, String> getPresignedUrl(@RequestParam("fileName") String fileName);
}
