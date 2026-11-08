package com.houjiang.bffweb.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * OA合同服务Feign客户端
/**
 * 用于调用OA模块的合同相关接口
 * 
 * @author Amos
 * @since 1.0.0
 */
@FeignClient(name = "oa", url = "${feign.oa.url:}", contextId = "oaContractServiceFeign", path = "/plugin/oa/contract")
public interface OaContractServiceFeign {
    
    /**
     * 保存或更新合同信息（项目关联）
/**
     * 
     * @param params 合同信息参数
     * @return 保存结果
     */
    @PostMapping("/save-or-update-by-project")
    Object saveOrUpdateByProject(@RequestBody Map<String, Object> params);
    
    /**
     * 根据项目ID查询合同信息
/**
     * 
     * @param params 查询参数，包含projectId
     * @return 合同信息
     */
    @PostMapping("/get-by-project")
    Object getByProject(@RequestBody Map<String, Object> params);
}
