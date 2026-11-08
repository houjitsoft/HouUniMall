package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 社区心得表
 * @TableName goods_article_info
 * @author Amos
 */
@TableName(value ="goods_article_info")
@Data
public class GoodsArticleInfoEntity implements Serializable {
    /**
     * 心得ID
     */
    @TableId
    private String articleId;

    /**
     * 作者ID
     */
    private String userId;

    /**
     * 心得标题
     */
    private String articleTitle;

    /**
     * 心得内容
     */
    private String articleContent;

    /**
     * 心得封面
     */
    private String articleCover;

    /**
     * 阅读量
     */
    private Integer readNum;

    /**
     * 评论量
     */
    private Integer remarkNum;

    /**
     * 审核状态:0-待审核,1-审核通过
     */
    private Integer auditState;

    /**
     * 审核备注
     */
    private String auditComment;

    /**
     * 商品ID
     */
    private Integer commonId;

    /**
     * 话题ID
     */
    private Integer tagId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 
     */
    private Date lastUpdatedTime;

    /**
     * 商品规格ID
     */
    private Integer goodsId;

    /**
     * 商品分类
     */
    private Integer catId;

    /**
     * 心得类型:1-图片,2-视频
     */
    private Integer articleType;

    /**
     * 点赞数
     */
    private Integer likeNum;

    /**
     * 收藏数
     */
    private Integer starNum;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}