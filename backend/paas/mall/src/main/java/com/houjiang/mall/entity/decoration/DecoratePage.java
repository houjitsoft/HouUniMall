package com.houjiang.mall.entity.decoration;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 店铺装修页面实体类
 * 对应数据库表：hou_mall_decorate_page
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_decorate_page")
public class DecoratePage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页面 ID
     */
    @TableId(value = "page_id", type = IdType.AUTO)
    private Long pageId;

    /**
     * 店铺 ID
     */
    private Long shopId;

    /**
     * 页面名称
     */
    private String pageName;

    /**
     * 页面类型：1-首页 2-自定义页
     */
    private Integer pageType;

    /**
     * 页面设计数据 JSON
     */
    private String pageData;

    /**
     * 是否设为首页：0-否 1-是
     */
    private Integer isHome;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
