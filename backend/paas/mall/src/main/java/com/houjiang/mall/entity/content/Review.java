package com.houjiang.mall.entity.content;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体类
 * 表名: hou_mall_review
 *
 * @author Amos
 */
@Data
@TableName("hou_mall_review")
public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 评论目标类型: selection/mood */
    private String targetType;

    /** 目标ID */
    private Long targetId;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String userName;

    /** 评论内容 */
    private String content;

    /** 父评论ID */
    private Long parentId;

    /** 点赞数 */
    private Integer likeCount;

    /** 状态: 0-待审核 1-已通过 2-已拒绝 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否删除 */
    @TableLogic
    private Integer deleted;
}
