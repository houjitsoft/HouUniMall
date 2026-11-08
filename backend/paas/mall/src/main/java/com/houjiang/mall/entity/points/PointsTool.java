package com.houjiang.mall.entity.points;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 积分商品实体类
 * 表名: hou_mall_points_tool
 *
 * @author Amos
 */
@Data
@TableName("hou_mall_points_tool")
public class PointsTool implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品名称 */
    private String name;

    /** 封面图 */
    private String coverImage;

    /** 商品图片JSON数组 */
    private String images;

    /** 商品说明 */
    private String description;

    /** 积分价格 */
    private Integer pointsPrice;

    /** 现金价格(积分+现金模式) */
    private BigDecimal cashPrice;

    /** 库存 */
    private Integer stock;

    /** 已兑换数量 */
    private Integer exchangeCount;

    /** 每人限兑数量(0=不限) */
    private Integer exchangeLimit;

    /** 排序 */
    private Integer sort;

    /** 状态: 0-下架 1-上架 */
    private Integer status;

    /** 兑换开始时间 */
    private LocalDateTime startTime;

    /** 兑换结束时间 */
    private LocalDateTime endTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否删除 */
    @TableLogic
    private Integer deleted;
}
