package com.houjiang.base.vo.index;

import lombok.Data;

/**
 * @ClassName GdbPcIndexConfigVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class GdbPcIndexConfigVO {

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
    /**
     * 排序
     */
    private Integer pageSort;

}
