package com.houjiang.base.vo;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 资讯文章表(BaseInformationInfo)表实体类
 *
 * @author Amos
 * @since 2024-01-18 15:31:10
 */
@Data
public class BaseInformationInfoVO implements Serializable {

    /**
     * 资讯id
     */
    private Integer informationId;


    /**
     * 资讯分类id
     */
    private Integer classifyId;

    /**
     * 资讯标题
     */
    private String informationTitle;

    /**
     * 资讯封面
     */
    private String informationCover;

    /**
     * 资讯封面
     */
    private List<String> informationCoverList;

    /**
     * 资讯内容
     */
    private String informationDesc;

    /**
     * 资讯状态 0已暂存 1已发布 2已下架
     */
    private Integer informationStatus;

    /**
     * 浏览量
     */
    private Integer pageView;
    /**
     * 设置游览量
     */
    private Integer pageViewSet;
    /**
     * 是否推荐 0不推荐 1推荐
     */
    private Integer isRecommend;

    /**
     * 发布来源
     */
    private String releaseSource;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 资讯分类id集合
     */
    private List<Integer> classifyIdList;

    /**
     * 发布时间
     */
    private Date releaseTime;
}
