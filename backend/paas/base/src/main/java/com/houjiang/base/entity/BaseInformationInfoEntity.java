package com.houjiang.base.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@TableName("hou_base_information_info")
public class BaseInformationInfoEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 资讯id
     */
    @TableId(type = IdType.AUTO)
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
     * 设置的浏览量
     */
    private Integer pageViewSet;

    /**
     * 自然浏览量
     */
    private Integer pageViewNatural;

    /**
     * 是否推荐 0不推荐 1推荐
     */
    private Integer isRecommend;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 发布来源
     */
    private String releaseSource;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date releaseTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 资讯分类名称
     */
    @TableField(exist = false)
    private String classifyName;

    /**
     * 资讯分类父类id
     */
    @TableField(exist = false)
    private Integer classifyParentId;

    /**
     * 资讯封面
     */
    @TableField(exist = false)
    private List<String> informationCoverList;
}
