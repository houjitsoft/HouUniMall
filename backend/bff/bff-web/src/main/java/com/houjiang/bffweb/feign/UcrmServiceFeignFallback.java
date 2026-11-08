package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Ucrm服务Feign降级处理类
/**
 * 
 * @description 当Ucrm服务不可用时提供降级响应
 * @author Amos
 * @date 2024-12-29
 */
@Component
public class UcrmServiceFeignFallback implements UcrmServiceFeign {

    @Override
    public ResponseResult getUcrmStatistics(Map<String, Object> param) {
        return ResponseResult.fail("Ucrm服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryUcrmList(Map<String, Object> param) {
        return ResponseResult.fail("Ucrm服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getUcrmDetail(Map<String, Object> param) {
        return ResponseResult.fail("Ucrm服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createUcrm(Map<String, Object> param) {
        return ResponseResult.fail("Ucrm服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateUcrm(Map<String, Object> param) {
        return ResponseResult.fail("Ucrm服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteUcrm(Map<String, Object> param) {
        return ResponseResult.fail("Ucrm服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult pauseUcrm(Map<String, Object> param) {
        return ResponseResult.fail("Ucrm服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult resumeUcrm(Map<String, Object> param) {
        return ResponseResult.fail("Ucrm服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult copyUcrm(Map<String, Object> param) {
        return ResponseResult.fail("Ucrm服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getUcrmTypes() {
        return ResponseResult.fail("Ucrm服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult batchUpdateStatus(Map<String, Object> param) {
        return ResponseResult.fail("Ucrm服务暂时不可用，请稍后重试");
    }
}