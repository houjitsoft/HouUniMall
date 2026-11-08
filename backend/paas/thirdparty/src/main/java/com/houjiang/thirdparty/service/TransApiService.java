package com.houjiang.thirdparty.service;


import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @author Amos
 */
public interface TransApiService {

    /**
     * 处理翻译请求
     * <p>
     * 通过POST方法接收翻译请求参数，并将参数转发给transApiService进行处理
     *
     * @param map 翻译请求的参数，包含需要翻译的文本及其他相关参数
     * @return transApiService处理后的翻译结果
     */
    ResponseResult trans(Map<String, String> map);

    /**
     * 查询所有语言
     *
     * @return 返回包含所有语言的响应结果
     */
    ResponseResult queryLanguageAll();

    /**
     * 查询语言值
     *
     * @param map 包含查询语言值所需参数的键值对，其中键为参数名称，值为参数值
     * @return ResponseResult类型的结果对象，包含查询到的语言值信息
     */
    ResponseResult queryLanguageValue(Map<String, String> map);

    /**
     * 更新语言配置项的值
     *
     * @param map 包含语言配置项及其新值的映射，其中键为配置项标识，值为新的配置项值
     * @return ResponseResult类型的结果，包含更新操作的状态和结果信息
     */
    ResponseResult updateLanguageValue(Map<String, String> map);

}
