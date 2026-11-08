package com.houjiang.base.dto;

import lombok.Data;

/**
 * 配置
 * @author Amos
 */
@Data
public class IndexConfigDTO {

    /**
     * id
     */
    private Integer id;

    /**
     * 页面名称
     */
    private String pageName;

    /**
     * 页面类型 1 天天特价 2 特色专题 3 为你推荐
     */
    private Integer pageType;

    /**
     * 页面标题
     */
    private String pageTitle;

    /**
     * 二级页面标题
     */
    private String pageTwoTitle;

    /**
     * 页面数据
     */
    private String pageData;

    /**
     * 页面状态 1 开启 2 关闭
     */
    private Integer pageStart;


}
