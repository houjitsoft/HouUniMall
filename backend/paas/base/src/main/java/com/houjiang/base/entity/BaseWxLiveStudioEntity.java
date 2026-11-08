package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName BaseWxLiveStudioEntity
 * @Description
 * @Author Amos
 * @Date 2023/7/28 10:48
 * @Version 1.0
 * @author Amos
 */
@Data
@TableName("hou_base_wx_live_studio")
public class BaseWxLiveStudioEntity {

    /**
     *活动ID
     */
    private Integer id;

    /**
     * 商店ID
     */
    private Integer shopId;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 活动封面图片
     */
    private String coverImg;

    /**
     * 活动开始时间
     */
    private Date startTime;

    /**
     * 活动结束时间
     */
    private Date endTime;

    /**
     * 主播姓名
     */
    private String anchorName;

    /**
     * 主播微信
     */
    private String anchorWechat;

    /**
     * 辅助主播微信
     */
    private String subAnchorWechat;

    /**
     * 创建者微信
     */
    private String createrWechat;

    /**
     * 分享图片
     */
    private String shareImg;

    /**
     * 动态图片
     */
    private String feedsImg;

    /**
     * 是否公开动态
     */
    private Integer isFeedsPublic;

    /**
     * 活动类型
     */
    private Integer type;

    /**
     * 关闭点赞功能
     */
    private Integer closeLike;

    /**
     * 关闭商品功能
     */
    private Integer closeGoods;

    /**
     * 关闭评论功能
     */
    private Integer closeComment;

    /**
     * 关闭回复功能
     */
    private Integer closeReplay;

    /**
     * 关闭分享功能
     */
    private Integer closeShare;

    /**
     * 关闭客服功能
     */
    private Integer closeKf;

    /**
     * 运营状态
     */
    private Integer operation;

    /**
     * 是否经过验证
     */
    private Integer isverify;

}
