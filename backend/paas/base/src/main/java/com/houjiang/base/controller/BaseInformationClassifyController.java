package com.houjiang.base.controller;


import com.houjiang.base.dto.BaseInformationClassifyDTO;
import com.houjiang.base.service.BaseInformationClassifyService;
import com.houjiang.commons.annnotation.AdminTokenLimit;
import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import java.util.Map;

/**
 * 资讯分类表(BaseInformationClassify)表控制层
 *
 * @author Amos
 * @since 2024-01-17 13:50:42
 */
@RestController
@RequestMapping("baseInformationClassify")
public class BaseInformationClassifyController {
    /**
     * 服务对象
     */
    @Resource
    private BaseInformationClassifyService baseInformationClassifyService;

    /**
     * 查询资讯分类
     *
     * @return
     */
    @GetMapping("/queryAll")
    public ResponseResult queryAllClassify() {
        ResponseResult result = baseInformationClassifyService.queryAllClassify();
        return result;
    }

    /**
     * 平台查询资讯分类
     *
     * @return
     */
    @AdminTokenLimit
    @PostMapping("/queryAllClassifyByPlatform")
    public ResponseResult queryAllClassifyByPlatform(UserTokenBeanDTO dto) {
        ResponseResult result = baseInformationClassifyService.queryAllClassifyByPlatform(dto);
        return result;
    }

    /**
     * 平台添加资讯分类
     *
     * @param param
     * @return
     */
    @SensitiveCheck(field = {"classifyName"})
    @AdminTokenLimit
    @PostMapping("/add")
    public ResponseResult addGoodsCat(@RequestBody BaseInformationClassifyDTO param) {
        return baseInformationClassifyService.addInformationClassify(param);
    }

    /**
     * 平台删除资讯分类
     *
     * @param param
     * @return
     */
    @AdminTokenLimit
    @PostMapping("/delete")
    public ResponseResult deleteGoodsCat(@RequestBody BaseInformationClassifyDTO param) {
        return baseInformationClassifyService.deleteInformationClassify(param);
    }

    /**
     * 查询是否是自己经销商品
     *
     * @param param
     * @return
     */
    @PostMapping("/feign/checkSupplySelf")
    public Integer checkSupplySelf(@RequestBody Map<String, Object> param){
        Integer result=baseInformationClassifyService.checkSupplySelf(param);
        return result;
    }
}

