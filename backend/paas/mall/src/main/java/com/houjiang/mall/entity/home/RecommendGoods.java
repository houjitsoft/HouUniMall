package com.houjiang.mall.entity.home;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 推荐商品实体类
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_recommend_goods")
public class RecommendGoods {
    
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 店铺ID(NULL表示平台级) */
    private Long shopId;
    
    /** 商品ID */
    private Long goodsId;
    
    /** 商品名称（冗余字段） */
    private String goodsName;
    
    /** 商品图片（冗余字段） */
    private String goodsImage;
    
    /** 商品价格（冗余字段） */
    private BigDecimal goodsPrice;
    
    /** 市场价格（冗余字段） */
    private BigDecimal marketPrice;
    
    /** 推荐类型: 1-热门推荐 2-新品推荐 3-猜你喜欢 4-人气榜单 */
    private Integer recommendType;
    
    /** 推荐位置: home-首页 category-分类页 detail-详情页 */
    private String position;
    
    /** 排序值 */
    private Integer sort;
    
    /** 状态: 0-禁用 1-启用 */
    private Integer status;
    
    /** 开始时间 */
    private LocalDateTime startTime;
    
    /** 结束时间 */
    private LocalDateTime endTime;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    /** 是否删除 */
    @TableLogic
    private Integer deleted;
}
