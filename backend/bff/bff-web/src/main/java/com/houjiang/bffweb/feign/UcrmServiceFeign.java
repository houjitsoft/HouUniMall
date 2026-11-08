package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Ucrm服务Feign客户端
/**
 * 
 * @description 调用paas中台Ucrm服务(crm)的接口
 * @author Amos
 * @date 2024-12-29
 */
@FeignClient(name = "crm", path = "/plugin", fallback = UcrmServiceFeignFallback.class, contextId = "ucrm", url = "${feign.crm.url:}")
public interface UcrmServiceFeign {

    // ================= Ucrm管理接口 =================
    
    /**
     * 获取crm统计数据
     */
    @PostMapping("/crm/manage/statistics")
    ResponseResult getUcrmStatistics(@RequestBody Map<String, Object> param);
    
    /**
     * 分页查询crm列表
     */
    @PostMapping("/crm/manage/list")
    ResponseResult queryUcrmList(@RequestBody Map<String, Object> param);
    
    /**
     * 查询crm详情
     */
    @PostMapping("/crm/manage/detail")
    ResponseResult getUcrmDetail(@RequestBody Map<String, Object> param);
    
    /**
     * 创建crm
     */
    @PostMapping("/crm/manage/create")
    ResponseResult createUcrm(@RequestBody Map<String, Object> param);
    
    /**
     * 更新crm
     */
    @PostMapping("/crm/manage/update")
    ResponseResult updateUcrm(@RequestBody Map<String, Object> param);
    
    /**
     * 删除crm
     */
    @PostMapping("/crm/manage/delete")
    ResponseResult deleteUcrm(@RequestBody Map<String, Object> param);
    
    /**
     * 暂停crm
     */
    @PostMapping("/crm/manage/pause")
    ResponseResult pauseUcrm(@RequestBody Map<String, Object> param);
    
    /**
     * 恢复crm
     */
    @PostMapping("/crm/manage/resume")
    ResponseResult resumeUcrm(@RequestBody Map<String, Object> param);
    
    /**
     * 复制crm
     */
    @PostMapping("/crm/manage/copy")
    ResponseResult copyUcrm(@RequestBody Map<String, Object> param);
    
    /**
     * 获取crm类型列表
     */
    @GetMapping("/crm/manage/types")
    ResponseResult getUcrmTypes();
    
    /**
     * 批量操作crm状态
     */
    @PostMapping("/crm/manage/batch/status")
    ResponseResult batchUpdateStatus(@RequestBody Map<String, Object> param);
}