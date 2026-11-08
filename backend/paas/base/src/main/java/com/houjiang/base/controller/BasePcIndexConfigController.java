package com.houjiang.base.controller;


import com.houjiang.base.dto.IndexConfigDTO;
import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName BasePcIndexConfigController
 * @Description
 * @Author Amos
 * @Date 2023-05-24 09:39:22
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/pcIndexConfig")
public class BasePcIndexConfigController extends BaseController {


    /**
     * pc 首页 天天特价 特色专题 为你推荐 添加 修改
     */
    @SensitiveCheck(field = {"pageName","pageTitle","pageTwoTitle"})
    @PostMapping("/updatePcIndexConfig")
    public ResponseResult UPdatePcIndexConfig(@RequestBody IndexConfigDTO pcIndexConfig) {
        ResponseResult result = basePcIndexConfigService.updatePcIndexConfig(pcIndexConfig);
        return result;
    }
}

