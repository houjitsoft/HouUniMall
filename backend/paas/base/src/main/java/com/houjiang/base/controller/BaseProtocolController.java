package com.houjiang.base.controller;

import com.houjiang.base.service.BaseProtocolService;
import com.houjiang.commons.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 协议管理Controller
 * @author Amos
 */
@RestController
@RequestMapping("base/protocol")
@RequiredArgsConstructor
public class BaseProtocolController {
    
    private final BaseProtocolService baseProtocolService;
    
    /**
     * 查询协议列表
     */
    @PostMapping("/queryList")
    public ResponseResult queryList(@RequestBody Map<String, Object> param) {
        return baseProtocolService.queryList(param);
    }
    
    /**
     * 保存或更新协议
     */
    @PostMapping("/saveOrUpdate")
    public ResponseResult saveOrUpdate(@RequestBody Map<String, Object> param) {
        return baseProtocolService.saveOrUpdate(param);
    }
    
    /**
     * 删除协议
     */
    @PostMapping("/delete")
    public ResponseResult delete(@RequestBody Map<String, Object> param) {
        return baseProtocolService.deleteById(param);
    }
}
