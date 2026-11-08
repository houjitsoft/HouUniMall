package com.houjiang.base.controller;


import com.houjiang.base.dto.BaseSensitiveConfigDTO;
import com.houjiang.commons.annnotation.AdminTokenLimit;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName BaseSensitiveConfigController
 * @Description  敏感词配置
 * @Author Amos
 * @Date 2024-01-30 16:48:22
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("baseSensitiveConfig")
public class BaseSensitiveConfigController extends BaseController {

    /**
     * 平台 查询敏感词配置
     * @param param
     * @return
     */
    @AdminTokenLimit
    @PostMapping("/query")
    public ResponseResult query(@RequestBody BaseSensitiveConfigDTO param) {
        return baseSensitiveConfigService.querySensitiveConfig(param);
    }
    /**
     * 平台 修改敏感词配置
     */
    @AdminTokenLimit
    @PostMapping("/update")
    public ResponseResult update(@RequestBody BaseSensitiveConfigDTO param) {
        return baseSensitiveConfigService.updateSensitiveConfig(param);
    }
}
