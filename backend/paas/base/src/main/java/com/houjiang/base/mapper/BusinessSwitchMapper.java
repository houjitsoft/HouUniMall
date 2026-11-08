package com.houjiang.base.mapper;


import org.apache.ibatis.annotations.Mapper;

import java.util.Map;
/**
 * @ClassName BusinessSwitchMapper
 * @Description
 * @Author Amos
 * @Date 2023-05-22 16:51:47
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BusinessSwitchMapper {

    /**
     * 查询企业开关
     * @param param
     * @return
     */
    Integer query(Map<String, Object> param);

    /**
     * 更新企业开关
     * @param param
     * @return
     */
    Integer updateValue(Map<String, Object> param);

    /**
     * 查询注册密码配置
     **/
    /**
     * TODO 补充方法说明
     * @param param TODO
     * @return TODO
     */
    String queryValue(Map<String, Object> param);

}
