package com.houjiang.activity.dto;



import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class PiecingTogetherConfigDTO {
    /**
     * id
     */
    private Integer id; 
    /**
     *1 代表模块1 （不可新增 删除）  2 代表 模块 2（可新增 删除）
     */
    private Integer configType; 
    /**
     *模块名称
     */
    private String configName; 
    /**
     *标题 一
     */
    private String configTitleOne; 
    /**
     *标题 二
     */
    private String configTitleTwo; 
    /**
     *数据
     */
    private String configData;  
    /**
     *拼团信息
     */
    private List<GoodsPiecingTogetherDTO> goodsPinTuanResultList;


}
