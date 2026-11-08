package com.houjiang.userms.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.VkQuerySettingDTO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName UserVkSetController
 * @Description
 * @Author Amos
 * @Date 2023/7/14 10:21
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/vkSet")
public class UserVkSetController extends BaseController{

    /**
     * 开启或者关闭微客
     * @param dto
     * @return
     */
    @Transactional
    @PostMapping("/enableVk")
    public ResponseResult enableVk(@RequestBody VkQuerySettingDTO dto){
        ResponseResult result=userVkSetService.enableVk(dto);
        return result;
    }

    /**
     * 添加或者修改微客设置
     */
    @Transactional
    @PostMapping("/addOrUpdateVkSetting")
    public ResponseResult addOrUpdateVkQuerySetting(@RequestBody VkQuerySettingDTO param){
        ResponseResult result=userVkSetService.addOrUpdateVkQuerySetting(param);
        return result;
    }

    /**
     * 查询微客的设置
     */
    @GetMapping("/vkQuerySetting")
    public ResponseResult vkQuerySetting(){
        ResponseResult result=userVkSetService.vkQuerySetting();
        return result;
    }
}
