package com.houjiang.mall.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 调用 base 服务用户端资讯接口（免登录内部接口），供 H5 装修桥填充「最新资讯」模块。
 * @author Amos
 */
@FeignClient(name = "base", url = "${feign.base.url:}", contextId = "baseInformationFeign", fallback = BaseInformationFeignFallback.class)
public interface BaseInformationFeign {

    /**
     * 用户端最新 20 条已发布资讯。
     * @return ResponseResult&lt;List&lt;BaseInformationInfoVO&gt;&gt;
     */
    @PostMapping("/baseInformationInfo/userQueryInformationInfo")
    ResponseResult userInformation();
}
