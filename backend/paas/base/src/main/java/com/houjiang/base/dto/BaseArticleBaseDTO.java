package com.houjiang.base.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.util.Date;

/**
 * 文章
 * @author Amos
 */
@Data
public class BaseArticleBaseDTO extends PageHelper {

    /**
     * token
     */
    private String token;

    /**
     * 文章ID
     */
    private Integer articleId;

    /**
     * 描述
     */
    private String articleDesc;

    /**
     * 标题
     */
    private String articleTitle;

    /**
     * 调用网址-url，默认为本页面构造的网址，可填写其它页面
     */
    private String articleUrl;

    /**
     * 组
     */
    private Integer articleGroupId;

    /**
     * 类型-0文章1公告
     */
    private Integer articleType;

    /**
     * 排序
     */
    private Integer articleSort;

    /**
     * 状态 1:启用  2:关闭
     */
    private Integer articleStatus;

    /**
     * 添加时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date articleAddTime;

}
