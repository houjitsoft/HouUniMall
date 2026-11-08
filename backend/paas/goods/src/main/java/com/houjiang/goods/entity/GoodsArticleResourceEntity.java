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
 * @Description  社区心得资源表
 * @Author Amos
 * @Date 2023-07-18 10:39:00
 * @Version 1.0
 * @author Amos
 */
@TableName("goods_article_resource")
@Data
public class GoodsArticleResourceEntity  implements Serializable {
    /**
     * 资源ID
     */
    @TableId(type = IdType.AUTO)
    private Integer resourceId;
    /**
     * 心得ID
     */
    private String articleId;
    /**
     * 资源链接
     */
    private String resourceUrl;
    /**
     * 资源类型:1-图片,2-视频
     */
    private Integer resourceType;
    /**
     * 创建时间
     */
    private Date createTime;

    private Date lastUpdatedTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
