package com.houjiang.base.dto;

import lombok.Data;

/**
 * @ClassName AdLiveRoomParamDTO
 * @Description
 * @Author Amos
 * @Date 2023/7/28 10:59
 * @Version 1.0
 * @author Amos
 */
@Data
public class AdLiveRoomParamDTO {

    private Integer id;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 房间名字
     */
    private String name;

    /**
     * 封面Img
     */
    private String coverImg;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 主播昵称
     */
    private String anchorName;

    /**
     * 主播微信号
     */
    private String anchorWechat;

    /**
     * 主播副号微信号
     */
    private String subAnchorWechat;

    /**
     * 创建者微信号
     */
    private String createrWechat;

    /**
     *共有1mg
     */
    private String shareImg;

    /**
     *饲料1mg
     */
    private String feedsImg;

    /**
     * 是否开启官方收录，1 开启，0 关闭
     */
    private Integer isFeedsPublic;

    /**
     * 直播类型，1 推流 0 手机直播
     */
    private Integer type;

    /**
     * 是否关闭点赞 1：关闭
     */
    private Integer closeLike;

    /**
     * 是否关闭商品货架，1：关闭
     */
    private Integer closeGoods;

    /**
     * 是否开启评论，1：关闭
     */
    private Integer closeComment;

    /**
     * 是否关闭回放 1 关闭
     */
    private Integer closeReplay;

    /**
     * 是否关闭分享 1 关闭
     */
    private Integer closeShare;

    /**
     * 是否关闭客服，1 关闭
     */
    private Integer closeKf;

    private String token;
}
