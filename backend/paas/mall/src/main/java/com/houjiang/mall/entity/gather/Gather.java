package com.houjiang.mall.entity.gather;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 集采业务实体类
 * 对应数据库表：hou_mall_gather (假设存在)
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_gather")
public class Gather implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品 ID
     */
    private Long goodsId;

    /**
     * 店铺 ID
     */
    private Long shopId;

    /**
     * 集采价格
     */
    private BigDecimal gatherPrice;

    /**
     * 集采数量
     */
    private Integer gatherQuantity;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
