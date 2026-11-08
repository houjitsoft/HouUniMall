package com.houjiang.base.controller;

import com.alibaba.fastjson.JSON;
import com.houjiang.base.dto.BaseWebConfigDTO;
import com.houjiang.base.entity.BaseWebConfigEntity;
import com.houjiang.commons.dto.base.DeliveryWebConfigDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName BaseWebConfigController
 * @Description 网站配置接口
 * @Author Amos
 * @Date 2024/4/29 10:56
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/base/webConfig")
public class BaseWebConfigController extends BaseController {

    /**
     * 保存配置
     * @param param
     * @return
     */
    @PostMapping("/config")
    public ResponseResult config(@RequestBody BaseWebConfigDTO param){
        return baseWebConfigService.editConfig(param);
    }

    /**
     * 查询配置
     * @param configKey
     * @return
     */
    @GetMapping("/config/{configKey}")
    public ResponseResult configDetail(@PathVariable("configKey") String configKey){
        BaseWebConfigEntity serviceById = baseWebConfigService.getById(configKey);
        return ResponseResult.success(serviceById);
    }

    /**
     * 保存配置
     * @param param
     * @return
     */
    @PostMapping("delivery/config")
    public ResponseResult deliveryConfig(@RequestBody DeliveryWebConfigDTO param){
        BaseWebConfigDTO form = new BaseWebConfigDTO();
        form.setConfigKey(param.getConfigKey());
        form.setConfigValue(JSON.toJSONString(param));
        form.setToken(param.getToken());
        return baseWebConfigService.editConfig(form);
    }

    /**
     * 查询配置
     * @param configKey
     * @return
     */
    @GetMapping("/delivery/config/{configKey}")
    public ResponseResult deliveryDetail(@PathVariable("configKey") String configKey){
        BaseWebConfigEntity serviceById = baseWebConfigService.getById(configKey);
        DeliveryWebConfigDTO houDeliveryWebConfigVo = JSON.parseObject(serviceById.getConfigValue(),DeliveryWebConfigDTO.class);
        return ResponseResult.success(houDeliveryWebConfigVo);
    }
}
