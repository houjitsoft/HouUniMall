package com.houjiang.commons.dto.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import java.util.List;

/**
 * @ClassName DiscountBaseAddParamDTO
 * @Description 新增限时折扣
 * @Author Amos
 * @Date 2023/7/3 16:09
 * @Version 1.0
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DiscountBaseAddParamDTO extends UserTokenBeanDTO {

    /**
     * 限时编号
     */
    private Integer discountId;
    /**
     * 活动名称
     */
    private String discountName;
    /***
     * 活动标题
     */
    private String discountTitle;
    /**
     * 活动说明
     */
    private String discountExplain;
    /**
     * 套餐编号
     */
    private Integer comboId;
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
     * 用户编号
     */
    private String userId;
    /**
     * 店铺编号
     */
    private String shopId;
    /**
     * 用户名
     */
    private String userNickName;
    /**
     * 店铺名称
     */
    private String shopName;
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
    private Integer discountHour;
    /**
     * 活动开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date discountCreateTime;

    /**
     * 预热时间
     */
    private Integer preStartHour;

    /**
     *  每种商品限购 限购天数
     */
    private Integer discountUpperTime;

    /**
     * 预热开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date preStartTime;

    /**
     * 活动商品
     */
    private List<DiscountGoodsListDTO> discountGoodsList;


}
