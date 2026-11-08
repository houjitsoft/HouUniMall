package com.houjiang.base.controller;

import com.houjiang.base.dto.BaseSystemPlatformNavDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName BaseSystemPlatformNavController
 * @Description
 * @Author Amos
 * @Date 2023/7/25 9:15
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/systemPlatformNav")
public class BaseSystemPlatformNavController extends BaseController{



    /**
     * 修改 或 添加 页面导航信息
     *
     * @param baseSystemPlatformNavDto
     * @return
     */
    @PostMapping("/updatePlatformNav")
    public ResponseResult updatePlatformNav(@RequestBody BaseSystemPlatformNavDTO baseSystemPlatformNavDto) {
        ResponseResult result=baseSystemPlatformNavService.updatePlatformNav(baseSystemPlatformNavDto);
        return result;
    }


    /**
     * 删除页面导航信息
     *
     * @param baseSystemPlatformNavDto
     * @return
     */
    @PostMapping("/deletePlatformNav")
    public ResponseResult deletePlatformNav(@RequestBody BaseSystemPlatformNavDTO baseSystemPlatformNavDto) {
        ResponseResult result=baseSystemPlatformNavService.deletePlatformNav(baseSystemPlatformNavDto);
        return result;
    }

    /**
     * 查询页面导航信息
     *
     * @param baseSystemPlatformNavDto
     * @return
     */
    @PostMapping("/queryInfo")
    public ResponseResult querySystemPlatformNavById(@RequestBody BaseSystemPlatformNavDTO baseSystemPlatformNavDto) {
        ResponseResult result=baseSystemPlatformNavService.querySystemPlatformNavById(baseSystemPlatformNavDto);
        return result;
    }

    /**
     * 页面导航 查询列表
     *
     * @param baseSystemPlatformNavDto
     * @return
     */
    @PostMapping("/queryAll")
    public ResponseResult querySystemPlatformNavList(@RequestBody BaseSystemPlatformNavDTO baseSystemPlatformNavDto) {
        ResponseResult result=baseSystemPlatformNavService.querySystemPlatformNavList(baseSystemPlatformNavDto);
        return result;
    }

}
