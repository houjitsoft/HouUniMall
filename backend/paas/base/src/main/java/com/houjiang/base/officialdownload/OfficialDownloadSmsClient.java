package com.houjiang.base.officialdownload;

import com.houjiang.commons.util.ResponseResult;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "thirdparty", path = "/sms")
interface OfficialDownloadSmsClient {
    @PostMapping("/sendCode")
    ResponseResult<String> sendCode(@RequestBody Map<String, String> request);
}
