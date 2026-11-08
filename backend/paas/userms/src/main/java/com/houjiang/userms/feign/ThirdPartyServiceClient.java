package com.houjiang.userms.feign;

import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author Amos
 * @date 2023/3/24 9:30
 */
@FeignClient("thirdparty")
public interface ThirdPartyServiceClient {

    /**
     * 上传
     *
     * @param param
     * @return
     */
    @PostMapping("/web/oss/upload")
    ResponseResult uploadPic(@RequestBody OssUploadUpdateParamDTO param);

    /**
     * plus 会员 充值短信
     */
    @PostMapping(value = "sms/plusmemberopenSend")
    ResponseResult plusmemberopenSend(@RequestBody Map<String,String> map);


    /**
     * 开通plus会员短信
     * @param paraMap
     * @return
     */
    @PostMapping("/sms/userPlus/renew")
    ResponseResult userPlusRenew(@RequestBody Map<String, String> paraMap);

    /**
     * 发送店铺资质短信
     */
    @RequestMapping("/sms/storeQualification")
    ResponseResult storeQualification(@RequestBody Map map);

    /**
     * 上传图片或视频  文件 格式传输
     *
     * @return
     * @RequestParam file
     */
    @PostMapping(value = "/web/oss/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseResult uploadToOssFile(@RequestPart("file") MultipartFile file);
}
