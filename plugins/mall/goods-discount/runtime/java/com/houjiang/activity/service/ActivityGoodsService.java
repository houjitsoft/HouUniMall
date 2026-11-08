package com.houjiang.activity.service;

import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @ClassName ActivityGoodsService
 * @Description
 * @Author Amos
 * @Date 2023-12-06 14:13:09
 * @Version 1.0
 * @author Amos
 */
public interface ActivityGoodsService {
    /**
     * 查看 购买上限
     * @param param
     * @return
     */
    ResponseResult checkGoodsLimit(Map<String, Object> param);

}

