package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 社区心得评论表
 * @TableName goods_article_remark
 * @author Amos
 */
@TableName(value ="goods_article_remark")
@Data
public class GoodsArticleRemarkEntity implements Serializable {
    /**
     * 评论ID
     */
    @TableId(type = IdType.AUTO)
    private Integer remarkId;

    /**
     * 心得ID
     */
    private String articleId;

    /**
     * 评论内容
     */
    private String remarkContent;

    /**
     * 被评论ID
     */
    private String remarkedId;

    /**
     * 评论用户ID
     */
    private String remarkUser;

    /**
     * 被评论用户ID
     */
    private String remarkedUser;

    /**
     * 评论层级:1-一级;2-二级
     */
    private Integer remarkType;

    /**
     * 审核状态:0-待审核,1-审核通过
     */
    private Integer auditState;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 
     */
    private Date lastUpdatedTime;

    /**
     * 审核状态
     */
    private String auditComment;

    /**
     * 点赞量
     */
    private Integer likeNum;

    /**
     * 已读状态:1-已读,0-未读
     */
    private Integer isRead;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}