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
 * @Description  社区心得收藏表
 * @Author Amos
 * @Date 2023-07-18 13:35:57
 * @Version 1.0
 * @author Amos
 */
@TableName("goods_article_star")
@Data
public class GoodsArticleStarEntity  implements Serializable {
    /**
     * 收藏ID
     */
    @TableId(type = IdType.AUTO)
    private Integer starId;
    /**
     * 心得ID
     */
    private String articleId;
    /**
     * 收藏用户ID
     */
    private Integer starUser;
    /**
     * 被收藏用户ID
     */
    private Integer starredUser;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 最近更新时间
     */
    private Date lastUpdatedTime;
    /**
     * 已读状态:1-已读,0-未读
     */
    private Integer isRead;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
