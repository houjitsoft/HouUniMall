package com.houjiang.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.houjiang.activity.vo.GoodsPinTuanResultVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @TableName activity_piecing_together_config
 * @author Amos
 */
@TableName(value = "hou_addon_activity_piecing_together_config")
@Data
public class ActivityPiecingTogetherConfigEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 模块
     */
    private String configName;

    /**
     * 1 代表模块1 （不可新增 删除）  2 代表 模块 2（可新增 删除）
     */
    private String configType;

    /**
     * 一级标题
     */
    private String configTitleOne;

    /**
     * 二级标题
     */
    private String configTitleTwo;

    /**
     * 页面数据
     */
    private String configData;

    @TableField(exist = false)
    private List<GoodsPinTuanResultVO> goodsPinTuanResultList;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}