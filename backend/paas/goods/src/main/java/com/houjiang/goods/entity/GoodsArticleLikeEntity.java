package com.houjiang.goods.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
/**
 * @ClassName
 * @Description  社区心得点赞表
 * @Author Amos
 * @Date 2023-07-18 10:43:58
 * @Version 1.0
 * @author Amos
 */
@TableName("goods_article_like")
@Data
public class GoodsArticleLikeEntity  implements Serializable {
    /**
     * 点赞ID
     */
    @TableId(type = IdType.AUTO)
    private Integer likeId;
    /**
     * 心得ID
     */
    private String articleId;
    /**
     * 被点赞ID,可以是心得ID,可以是评论ID
     */
    private String likedId;
    /**
     * 点赞用户ID
     */
    private String likeUser;
    /**
     * 被点赞用户ID
     */
    private String likedUser;
    /**
     * 点赞类型:1-心得;2-评论
     */
    private Integer likeType;
    /**
     * 创建时间
     */
    private Date createTime;

    private Date lastUpdatedTime;
    /**
     * 已读状态:1-已读,0-未读
     */
    private Integer isRead;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
