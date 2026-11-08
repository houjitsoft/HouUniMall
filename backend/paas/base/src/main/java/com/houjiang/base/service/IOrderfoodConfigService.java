package com.houjiang.base.service;

import java.util.Map;

/**
 * @author Amos
 */
public interface IOrderfoodConfigService {

    /**
     * TODO 补充方法说明
     * @param key TODO
     * @return TODO
     */
    Object get(String key);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Map<Integer,Integer> getUpgrade();
}
