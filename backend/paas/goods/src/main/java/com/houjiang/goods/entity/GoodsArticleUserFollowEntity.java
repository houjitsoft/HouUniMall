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
 * @Description  社区用户关注表
 * @Author Amos
 * @Date 2023-07-18 13:55:33
 * @Version 1.0
 * @author Amos
 */
@TableName("goods_article_user_follow")
@Data
public class GoodsArticleUserFollowEntity  implements Serializable {
    /**
     * 关注ID
     */
    @TableId(type = IdType.AUTO)
    private Integer followId;
    /**
     * 关注用户ID
     */
    private Integer followUserId;
    /**
     * 被关注用户ID
     */
    private Integer followedUserId;
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
