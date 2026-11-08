package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 社区话题表
 * @TableName goods_tag_info
 * @author Amos
 */
@TableName(value ="goods_tag_info")
@Data
public class GoodsTagInfoEntity implements Serializable {
    /**
     * 话题ID
     */
    @TableId(type = IdType.AUTO)
    private Integer tagId;

    /**
     * 所属分类
     */
    private Integer classId;

    /**
     * 话题内容
     */
    private String tagContent;

    /**
     * 话题副标题
     */
    private String tagSubtitle;

    /**
     * 话题封面
     */
    private String tagCover;

    /**
     * 是否启用:1-开启,0-关闭
     */
    private Integer isActive;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date lastUpdatedTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}