package com.houjiang.base.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName ModularizationdCatParam
 * @Description TODO
 * @Author Amos
 * @Date 2023/6/8 11:51
 * @Version 1.0
 * @author Amos
 */
@Data
public class ModularizationdCatDTO extends PageHelper {
    private Integer id;
    /**
     * 页面名称
     */
    private String pageName;
    /**
     * 页面类型 1 三级分类页 2 二级分类页 3 二级分类页样式二 4一级分类样式一 5 一级分类样式二
     */
    private Integer pageType;
    /**
     * 页面数据
     */
    private String pageData;
    /**
     * 页面状态 1 使用中2 未使用
     */
    private Integer pageStart;
    /**
     * 时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
