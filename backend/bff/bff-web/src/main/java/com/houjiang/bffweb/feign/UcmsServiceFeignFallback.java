package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Ucms服务Feign降级处理类
/**
 * 
 * @description 当Ucms服务不可用时提供降级响应
 * @author Amos
 * @date 2024-12-29
 */
@Component
public class UcmsServiceFeignFallback implements UcmsServiceFeign {

    @Override
    public ResponseResult getUcmsStatistics(Map<String, Object> param) {
        return ResponseResult.fail("Ucms服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryUcmsList(Map<String, Object> param) {
        return ResponseResult.fail("Ucms服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getUcmsDetail(Map<String, Object> param) {
        return ResponseResult.fail("Ucms服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createUcms(Map<String, Object> param) {
        return ResponseResult.fail("Ucms服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateUcms(Map<String, Object> param) {
        return ResponseResult.fail("Ucms服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteUcms(Map<String, Object> param) {
        return ResponseResult.fail("Ucms服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult pauseUcms(Map<String, Object> param) {
        return ResponseResult.fail("Ucms服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult resumeUcms(Map<String, Object> param) {
        return ResponseResult.fail("Ucms服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult copyUcms(Map<String, Object> param) {
        return ResponseResult.fail("Ucms服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getUcmsTypes(Map<String, Object> param) {
        return ResponseResult.fail("Ucms服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult batchUpdateStatus(Map<String, Object> param) {
        return ResponseResult.fail("Ucms服务暂时不可用，请稍后重试");
    }
}
