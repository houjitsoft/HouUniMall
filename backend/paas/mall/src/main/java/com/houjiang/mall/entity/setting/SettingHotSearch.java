package com.houjiang.mall.entity.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 热门搜索关键词实体（hou_merchant_hot_search）。
 * 对应 like-common HotSearch。
 *
 * @author Amos
 */
@Data
@TableName("hou_merchant_hot_search")
public class SettingHotSearch implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 关键词 */
    private String name;

    /** 排序（越大越靠前） */
    private Integer sort;
}
