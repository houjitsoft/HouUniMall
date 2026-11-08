package com.houjiang.goods.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 商品版本(GoodsVersion)表实体类
 *
 * @author Amos
 * @since 2025-04-29 13:25:21
 */
@Data
@TableName("goods_version")
public class GoodsVersionEntity implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    
    /**
     * 版本id
     */
    @TableId(type = IdType.AUTO)
    private Integer verId;

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 商品名称
     */
    private String commonName;
    
    /**
     * 商品数据
     */
    private String verBody;

    /**
     * 商家Id
     */
    private Integer shopId;

    /**
     * 操作人id
     */
    private String operateUserId;
    
    /**
     * 操作人名称
     */
    private String operateUserName;
    
    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date operateTime;
    
    /**
     * 版本号
     */
    private String versionNo;

}
