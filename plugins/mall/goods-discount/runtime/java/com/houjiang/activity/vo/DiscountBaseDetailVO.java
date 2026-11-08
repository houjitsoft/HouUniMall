package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.goods.DiscountGoodsListVO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class DiscountBaseDetailVO {
    /**
     * 限时编号
     */
    private Integer discountId;
    /**
     * 活动名称
     */
    private String discountName;
    /**
     * 活动标题
     */
    private String discountTitle;
    /**
     * 活动说明
     */
    private String discountExplain;
    /**
     * 活动开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date discountStartTime;
    /**
     * 活动结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date discountEndTime;
    /**
     * 购买下限，1为不限制
     */
    private Integer discountLowerLimit;
    /**
     * 状态， 1-正常/2-结束/3-管理员关闭
     */
    private Integer discountState;
    /**
     * 购买上限，1为不限制
     */
    private Integer discountUpperLimit;
    /**
     * 预热时间
     */
    private Integer preStartHour;

    /**
     *每种商品限购 限购天数
     */
    private Integer discountUpperTime;

    /**
     * 折扣商品信息
     */
    private List<DiscountGoodsListVO> discountGoodsList;

    /**
     * 折扣商品数量
     */
    private Integer discountGoodsListCount;

}
