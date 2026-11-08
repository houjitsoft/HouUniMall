package com.houjiang.mall.entity.home;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Banner轮播图实体类
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_banner")
public class Banner {
    
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 店铺ID(NULL表示平台级) */
    private Long shopId;
    
    /** Banner标题 */
    private String title;
    
    /** 图片URL */
    private String imageUrl;
    
    /** 跳转链接 */
    private String linkUrl;
    
    /** 链接类型: 1-商品 2-分类 3-活动 4-外链 5-无跳转 */
    private Integer linkType;
    
    /** 关联ID(商品ID/分类ID/活动ID) */
    private Long linkId;
    
    /** 位置: home-首页 category-分类页 */
    private String position;
    
    /** 背景色 */
    private String bgColor;
    
    /** 角标文字 */
    private String badge;
    
    /** 排序(越大越前) */
    private Integer sort;
    
    /** 开始时间 */
    private LocalDateTime startTime;
    
    /** 结束时间 */
    private LocalDateTime endTime;
    
    /** 状态: 0-禁用 1-启用 */
    private Integer status;
    
    /** 创建人 */
    private Long createBy;
    
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /** 是否删除 */
    @TableLogic
    private Integer deleted;
}
