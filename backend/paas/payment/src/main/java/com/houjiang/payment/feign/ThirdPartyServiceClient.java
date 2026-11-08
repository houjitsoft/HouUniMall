package com.houjiang.payment.feign;

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
     * @param param 参数
     * @return 处理结果
     */
    @PostMapping("/web/oss/upload")
    ResponseResult uploadToOssBase64(@RequestBody OssUploadUpdateParamDTO param);

}
