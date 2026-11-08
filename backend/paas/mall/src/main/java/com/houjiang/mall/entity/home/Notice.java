package com.houjiang.mall.entity.home;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告快报实体类
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_notice")
public class Notice {
    
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 店铺ID(NULL表示平台级) */
    private Long shopId;
    
    /** 公告标题 */
    private String title;
    
    /** 公告内容 */
    private String content;
    
    /** 类型: 1-快报 2-公告 3-通知 */
    private Integer noticeType;
    
    /** 封面图片 */
    private String coverImage;
    
    /** 详情链接 */
    private String linkUrl;
    
    /** 是否置顶 */
    private Integer isTop;
    
    /** 浏览次数 */
    private Integer viewCount;
    
    /** 开始时间 */
    private LocalDateTime startTime;
    
    /** 结束时间 */
    private LocalDateTime endTime;
    
    /** 状态: 0-草稿 1-已发布 2-已下线 */
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
