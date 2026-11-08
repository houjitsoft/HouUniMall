package com.houjiang.goods.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品多语言内容（表 hou_goods_common_i18n）
 * common_id 对应 mall 商品 id（或经典 common_id）
 */
@Data
@TableName("hou_goods_common_i18n")
public class GoodsCommonI18n implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer commonId;
    private String langCode;
    private String commonName;
    private String promotionTips;
    private String commonBody;
    private String specNameJson;
    private String specValueJson;
    /** manual / ai / corrected */
    private String source;
    /** draft / published / stale */
    private String status;
    private Date createTime;
    private Date updateTime;
}
