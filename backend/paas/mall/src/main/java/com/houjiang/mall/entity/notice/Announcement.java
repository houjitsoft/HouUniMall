package com.houjiang.mall.entity.notice;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统公告实体类
 * 对应数据库表：hou_mall_announcement
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_announcement")
public class Announcement implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公告 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 公告类型：1-系统通知 2-活动公告 3-规则变更
     */
    private Integer type;

    /**
     * 是否置顶：0-否 1-是
     */
    private Integer isTop;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
