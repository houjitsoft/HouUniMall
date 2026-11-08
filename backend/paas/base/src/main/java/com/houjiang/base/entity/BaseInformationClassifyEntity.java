package com.houjiang.base.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 资讯分类表(BaseInformationClassify)表实体类
 *
 * @author Amos
 * @since 2024-01-17 13:50:42
 */
@Data
@TableName("hou_base_information_classify")
public class BaseInformationClassifyEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer classifyId;

    /**
     * 分类名称
     */
    private String classifyName;

    /**
     * 父类
     */
    private Integer classifyParentId;

    /**
     * 排序
     */
    private Integer classifySort;

    /**
     * 分类级别
     */
    private Integer classifyLevel;

    /**
     * 是否在前台显示
     */
    private Integer classifyShowStatus;

    /**
     * 商品分类菜单集合
     */
    @TableField(exist = false)
    private List<BaseInformationClassifyEntity> childMenu;

}
