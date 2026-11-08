package com.houjiang.base.feign;


import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
     * 上传图片或文档 base64 格式传输
     *
     * @param param
     * @return
     */
    @PostMapping("/web/oss/upload")
    ResponseResult uploadToOssBase64(@RequestBody OssUploadUpdateParamDTO param);



    /**
     * 上传图片或视频  文件 格式传输
     *
     * @return
     * @RequestParam file
     */
    @PostMapping(value = "/web/oss/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseResult uploadToOssFile(@RequestPart("file") MultipartFile file);

    /**
     * 平台端发送库存不足短信
     *
     * @param paraMap
     */
    @PostMapping("/sms/dealerReview")
    ResponseResult sendDealerReview(@RequestBody Map<String, String> paraMap);
    /**
     * 使用POST方法通过HTTP协议发送短信
     * 该方法委托给SendSmsService进行具体的短信发送操作
     *
     * @param paraMap 包含所有请求参数的映射对象，具体参数说明由SendSmsService定义
     * @return 返回一个ResponseResult对象，其中包含操作结果
     * <p>
     * 为什么使用PostMapping：需要通过POST方式提交数据，因为可能涉及敏感信息且数据量可能较大
     * 为什么是sendSms：明确该方法的功能是发送短信
     * 为什么是Map类型：为了灵活接收各种可能的短信发送参数，不局限于固定的几个字段
     */
    @PostMapping("/sms/sendSms")
    ResponseResult sendSms(@RequestBody Map<String, Object> paraMap);
    /**
     * 使用POST方法通过指定路径发送通知
     *
     * @param param 包含通知相关参数的Map对象，通过请求体传递
     * @return 返回处理结果，具体类型由officialAccountService定义
     * <p>
     * 该方法的主要作用是调用officialAccountService的sendOfficialAccountNotice方法
     * 以发送官方账号通知它不直接处理请求参数，而是将参数转发给服务层方法
     */
    @PostMapping("/officialAccount/sendNotice")
    ResponseResult sendNotice(@RequestBody Map<String, Object> param);
}
