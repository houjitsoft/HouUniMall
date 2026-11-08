package com.houjiang.mall.entity.decoration;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 底部 tabbar 导航配置（映射 hou_decorate_tabbar 表）
 *
 * 字段：id, name, selected, unselected, link, is_show, create_time, update_time
 *
 * @author Amos
 */
@Data
@TableName("hou_decorate_tabbar")
public class LikeDecorateTabbar implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 导航名称（如"首页"、"分类"等） */
    private String name;

    /** 选中状态图标路径 */
    private String selected;

    /** 未选中状态图标路径 */
    private String unselected;

    /** 链接配置 JSON（如 {"path":"/pages/index/index","name":"商城首页","type":"shop"} ） */
    private String link;

    /** 是否显示：1=显示 0=隐藏 */
    @TableField("is_show")
    private Integer isShow;

    /** 创建时间（Unix 时间戳） */
    @TableField("create_time")
    private Long createTime;

    /** 更新时间（Unix 时间戳） */
    @TableField("update_time")
    private Long updateTime;
}
