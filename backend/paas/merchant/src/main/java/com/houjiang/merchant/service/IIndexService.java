package com.houjiang.merchant.service;

import com.alibaba.fastjson2.JSONObject;

import java.util.Map;

/**
 * 主页服务接口类
 */
public interface IIndexService {

    /**
     * 控制台数据
     *
     * @author Amos
     * @return Map<String, Object>
     */
    Map<String, Object> index();

    /**
     * 公共配置
     */
    Map<String, Object> config();

    /**
     * @notes 获取信息
     * @return array
     * @author Amos
     * @datetime 2024-12-01 15:04:16
     */
    JSONObject mySelf();
}
