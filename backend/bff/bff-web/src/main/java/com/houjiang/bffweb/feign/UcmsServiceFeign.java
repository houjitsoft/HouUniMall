package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Ucms服务Feign客户端
/**
 * 
 * @description 调用paas中台Ucms服务(cms)的接口
 * @author Amos
 * @date 2024-12-29
 */
@FeignClient(name = "cms", url = "${feign.cms.url:}", path = "/plugin/cms", fallback = UcmsServiceFeignFallback.class, contextId = "ucms")
public interface UcmsServiceFeign {

    // ================= Ucms管理接口 =================
    
    /**
     * 获取cms统计数据
     */
    @PostMapping("/statistics/dashboard")
    ResponseResult getUcmsStatistics(@RequestBody Map<String, Object> param);
    
    /**
     * 分页查询cms列表
     */
    @PostMapping("/archives/list")
    ResponseResult queryUcmsList(@RequestBody Map<String, Object> param);
    
    /**
     * 查询cms详情
     */
    @PostMapping("/archives/detail")
    ResponseResult getUcmsDetail(@RequestBody Map<String, Object> param);
    
    /**
     * 创建cms
     */
    @PostMapping("/archives/create")
    ResponseResult createUcms(@RequestBody Map<String, Object> param);
    
    /**
     * 更新cms
     */
    @PostMapping("/archives/update")
    ResponseResult updateUcms(@RequestBody Map<String, Object> param);
    
    /**
     * 删除cms
     */
    @PostMapping("/archives/delete")
    ResponseResult deleteUcms(@RequestBody Map<String, Object> param);
    
    /**
     * 暂停cms
     */
    @PostMapping("/archives/update-status")
    ResponseResult pauseUcms(@RequestBody Map<String, Object> param);
    
    /**
     * 恢复cms
     */
    @PostMapping("/archives/update-status")
    ResponseResult resumeUcms(@RequestBody Map<String, Object> param);
    
    /**
     * 复制cms
     */
    @PostMapping("/archives/create")
    ResponseResult copyUcms(@RequestBody Map<String, Object> param);
    
    /**
     * 获取cms类型列表
     */
    @PostMapping("/model/list")
    ResponseResult getUcmsTypes(@RequestBody(required = false) Map<String, Object> param);
    
    /**
     * 批量操作cms状态
     */
    @PostMapping("/archives/batch-update-status")
    ResponseResult batchUpdateStatus(@RequestBody Map<String, Object> param);
}
