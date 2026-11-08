package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 模板装修分类页面
 * @TableName base_modularizationd_cat
 * @author Amos
 */
@TableName(value ="hou_base_modularizationd_cat")
@Data
public class BaseModularizationdCatEntity implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
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
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}