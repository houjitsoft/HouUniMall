package com.houjiang.mall.entity.home;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页楼层配置实体
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_home_floor")
public class HomeFloor {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 店铺ID(NULL表示平台级)
     */
    private Long shopId;

    /**
     * 楼层名称
     */
    private String floorName;

    /**
     * 楼层类型: banner/seckill/recommend/brand/category
     */
    private String floorType;

    /**
     * 楼层配置JSON
     */
    private String floorData;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态: 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer deleted;
}
