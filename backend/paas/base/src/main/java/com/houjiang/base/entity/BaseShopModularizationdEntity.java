package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName base_shop_modularizationd
 * @author Amos
 */
@TableName(value ="hou_base_shop_modularizationd")
@Data
public class BaseShopModularizationdEntity implements Serializable {
    /**
     * 页面id
     */
    @TableId
    private Integer pageId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 页面名称
     */
    private String pageName;

    /**
     * 页面内容
     */
    private String pageData;

    /**
     * 0未发布 1已发布
     */
    private Integer pageType;

    /**
     * 0未使用 1已使用
     */
    private Integer pageStart;

    /**
     * 是否是首页 1 是 2二级页  3 底部导航
     */
    private Integer pageIsIndex;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 当前商品所有的id
     */
    private String goodsallidjson;

    /**
     * 二级页面地址
     */
    @TableField(exist = false)
    private String pageUrl;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}