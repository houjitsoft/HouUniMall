package com.houjiang.mall.entity.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 询价单实体类
 * 表名: hou_mall_inquiry
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_inquiry")
public class Inquiry implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 询价单号
     */
    private String inquiryNo;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 买家名称
     */
    private String buyerName;

    /**
     * 买家电话
     */
    private String buyerPhone;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 规格
     */
    private String spec;

    /**
     * 询价数量
     */
    private Integer quantity;

    /**
     * 期望价格
     */
    private BigDecimal expectedPrice;

    /**
     * 询价备注
     */
    private String remark;

    /**
     * 报价
     */
    private BigDecimal quotedPrice;

    /**
     * 报价说明
     */
    private String quoteRemark;

    /**
     * 报价时间
     */
    private LocalDateTime quoteTime;

    /**
     * 状态: 0-待报价 1-已报价 2-已接受 3-已拒绝 4-已过期
     */
    private Integer status;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer deleted;
}
