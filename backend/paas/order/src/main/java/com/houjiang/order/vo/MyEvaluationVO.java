package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 * @date 2023/3/23 11:48
 */
@Data
public class MyEvaluationVO {

    /**
     * 用户名
     */
    private String UserName;

    /**
     * 用户头像
     */
    private String UserAvatar;

    /**
     * 店铺id
     */
    private Integer ShopId;

    /**
     * 店铺名
     */
    private String ShopName;

    /**
     * 商品公共表id
     */
    private Integer CommonId;

    /**
     * 商品id
     */
    private Integer GoodsId;

    /**
     * 商品名
     */
    private String GoodsName;

    /**
     * 商品规格-JSON存储
     */
    private String GoodsSpec;

    /**
     * 1-5分
     */
    private Integer Scores;

    /**
     * 结果
     */
    private String Result;

    /**
     * 内容
     */
    private String Content;

    /**
     * 追评内容
     */
    private String AdditionalContent;

    /**
     * 追评回复时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date AdditionalContentTime;

    /**
     * 晒单图片
     */
    private String Image;

    /**
     * 追评图片
     */
    private String AdditionalImage;

    /**
     * 图片
     */
    private String[] ImagesList;

    /**
     * 追评图片
     */
    private String[] AdditionalImagesList;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date CreateTime;

    /**
     * 解释内容
     */
    private String ExplainContent;

    /**
     * 商家回复追评内容
     */
    private String AdditionalExplainContent;

    /**
     * 评价时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date UpdateTime;

    /**
     * 商家回复追评时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date AdditionalReplyTime;

    /**
     * 追评天数
     */
    private Integer AdditionalDay;

    /**
     * 订单id
     */
    private String OrderId;

    /**
     * 商品图片
     */
    private String GoodsImage;


}
