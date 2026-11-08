package com.houjiang.merchant.service;

import java.util.List;
import java.util.Map;

/**
 * 存储配置服务接口类
 */
public interface ISettingStorageService {

    /**
     * 存储列表
     *
     * @author Amos
     * @return List<Map<String, Object>>
     */
    List<Map<String, Object>> list();

    /**
     * 存储详情
     *
     * @author Amos
     * @param alias 引擎别名
     * @return Map<String, Object>
     */
    Map<String, Object> detail(String engine);

    /**
     * 存储编辑
     *
     * @author Amos
     * @param params 参数
     */
    void setup(Map<String, String> params);

    /**
     * 存储切换
     *
     * @author Amos
     * @param alias 引擎别名
     * @param status 状态
     */
    void change(String alias, Integer status);

}
