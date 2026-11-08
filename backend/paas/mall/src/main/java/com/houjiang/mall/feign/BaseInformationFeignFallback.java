package com.houjiang.mall.feign;

import com.houjiang.commons.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * base 服务不可达时快速降级，避免 H5 首页装修接口被资讯查询拖死。
 * @author Amos
 */
@Slf4j
@Component
public class BaseInformationFeignFallback implements BaseInformationFeign {

    @Override
    public ResponseResult userInformation() {
        log.warn("[BaseInformationFeign] userInformation 降级，资讯模块本次为空");
        return ResponseResult.success(java.util.Collections.emptyList());
    }
}
