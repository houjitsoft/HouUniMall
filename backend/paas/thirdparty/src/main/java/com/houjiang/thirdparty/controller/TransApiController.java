package com.houjiang.thirdparty.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.service.TransApiService;
import com.houjiang.thirdparty.utils.TransApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/baidu")
@RefreshScope
public class TransApiController {
    @Resource
    private TransApiService transApiService;

    @PostMapping("trans")
    public ResponseResult trans(@RequestBody Map<String, String> map) {
        return transApiService.trans(map);
    }
    /**
     * 查询所有语言
     *
     * @return 返回包含所有语言的响应结果
     */
    @PostMapping("queryLanguageAll")
    public ResponseResult queryLanguageAll() {
        return transApiService.queryLanguageAll();
    }

    /**
     * 查询语言值
     *
     * @param map 包含查询语言值所需参数的键值对，其中键为参数名称，值为参数值
     * @return ResponseResult类型的结果对象，包含查询到的语言值信息
     */
    @PostMapping("queryLanguageValue")
    public ResponseResult queryLanguageValue(@RequestBody Map<String, String> map) {
        return transApiService.queryLanguageValue(map);
    }

    /**
     * 更新语言配置项的值
     *
     * @param map 包含语言配置项及其新值的映射，其中键为配置项标识，值为新的配置项值
     * @return ResponseResult类型的结果，包含更新操作的状态和结果信息
     */
    @PostMapping("updateLanguageValue")
    public ResponseResult updateLanguageValue(@RequestBody Map<String, String> map) {
        return transApiService.updateLanguageValue(map);
    }


}
