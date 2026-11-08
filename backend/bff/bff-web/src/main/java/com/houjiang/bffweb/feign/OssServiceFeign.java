package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * OSS服务Feign客户端
/**
 * 
 * @author Amos
 * @since 2024-01-26
 */
@FeignClient(
    name = "thirdparty-oss",
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
}
