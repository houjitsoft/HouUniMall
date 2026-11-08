package com.houjiang.goods.feign;

import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Amos
 * @date 2023/3/24 9:30
 */
@FeignClient("thirdparty")
public interface ThirdPartyServiceClient {

    /**
     * 上传图片或文档 base64 格式传输
     *
     * @param param
     * @return
     */
    @PostMapping("/web/oss/upload")
    ResponseResult uploadToOssBase64(@RequestBody OssUploadUpdateParamDTO param);
}
