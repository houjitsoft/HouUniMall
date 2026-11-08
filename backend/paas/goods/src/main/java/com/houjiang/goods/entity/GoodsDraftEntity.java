package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName GoodsDraftEntity
 * @Description 草稿箱
 * @Author Amos
 * @Date 2023/8/11 9:16
 * @Version 1.0
 * @author Amos
 */
@TableName("goods_draft")
@Data
public class GoodsDraftEntity {

    /**
     * 草稿id
     */
    @TableId(type = IdType.AUTO)
    private Integer draftId;

    /**
     * 草稿内容
     */
    private String draftContent;

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 草稿标题
     */
    private String draftName;
}
