package com.houjiang.merchant.service;

import com.alibaba.fastjson2.JSONArray;

import java.util.List;


/**
 * 公众号菜单服务接口类
 */
public interface IChannelOaMenusService {

    /**
     * 菜单详情
     *
     * @author Amos
     * @return JSONArray
     */
    JSONArray detail();

    /**
     * 菜单保存
     *
     * @author Amos
     * @param params 参数
     * @param isPublish 是否发布
     */
    void save(List<Object> params, Boolean isPublish);

}
