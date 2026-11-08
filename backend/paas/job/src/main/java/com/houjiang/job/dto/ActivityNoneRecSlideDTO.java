package com.houjiang.job.dto;

import lombok.Data;

/**
 * @ClassName ActivityNoneRecSlideDTO
 * @Description 活动无回收幻灯片
 * @Author Amos
 * @Date 2023/5/17 14:12
 * @Version 1.0
 * @author Amos
 */
@Data
public class ActivityNoneRecSlideDTO {

    /**
     * 跳转类型:1-商品goodsid,2:店铺shopId,3:链接
     */
    private Integer actionType;

    /**
     * 活动轮播
     */
    private String activitySlide;

    /**
     * 跳转内容
     */
    private String actionContent;

    /**
     * 排序
     */
    private Integer displayOrder;

    /**
     * 活动类型
     * 1特产推荐;
     * 2新品推荐;
     * 3为你推荐;
     * 4蜂狂特价;
     * 5新人专区;
     * 6平台活动专区;
     * 7:9.9包邮专区;
     * 8:品牌生活;
     * 9:好货溯源;
     * 10:猜你喜欢;
     * 11:时令果蔬;
     * 12:节日钜惠;
     * 13:限时折扣;
     * 14:超级拼团;
     */
    private Integer activityNoneType;

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 秒杀库存
     */
    private Integer seckillNum;
}
