package com.houjiang.commons.vo.activity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 红包模版表
 * @TableName activity_redpacket_template
 * @author Amos
 */

@Data
public class ActivityRedpacketTemplateVO implements Serializable {
    /**
     * 红包模版编号
     */
    @TableId(type = IdType.AUTO)
    private Integer redpacketTId;

    /**
     * 红包类型，1-新人注册红包，2-普通红包，3-多品红包 4-店铺红包 5-单品多品红包 6-专属红包默认2
     */
    private Boolean redpacketTType;

    /**
     * 红包模版名称
     */
    private String redpacketTTitle;

    /**
     * 红包模版描述
     */
    private String redpacketTDesc;

    /**
     * 红包模版有效期开始时间
     */
    private Date redpacketTStartDate;

    /**
     * 红包模版有效期结束时间
     */
    private Date redpacketTEndDate;

    /**
     * 红包模版面额
     */
    private Integer redpacketTPrice;

    /**
     * 红包使用店铺显示，默认为空，不限店铺使用，多个店铺ID之间用逗号分隔
     */
    private String shopLimit;

    /**
     * 红包使用商品分类限制，默认为空，不限分类，多个商品分类ID之间用逗号分隔
     */
    private String goodsCatLimit;

    /**
     * 红包使用时的消费限额
     */
    private BigDecimal redpacketTOrderlimit;

    /**
     * 红包是否允许转增，1-是，0-否，默认0
     */
    private Boolean isAllowTransfer;

    /**
     * 红包模版状态(1-有效,2-失效,3-删除)
     */
    private Byte redpacketTState;

    /**
     * 模版可发放的红包总数  0 不限制
     */
    private Integer redpacketTTotal;

    /**
     * 模版已发放的红包数量
     */
    private Integer redpacketTGiveout;

    /**
     * 模版已经使用过的红包
     */
    private Integer redpacketTUsed;

    /**
     * 模版的创建时间
     */
    private Date redpacketTAddDate;

    /**
     * 模版的最后修改时间
     */
    private Date redpacketTUpdateDate;

    /**
     * 兑换所需积分
     */
    private Integer redpacketTPoints;

    /**
     * 每人限领张数 0 不限制
     */
    private Integer redpacketTEachlimit;

    /**
     * 领取红包的用户等级限制
     */
    private Byte redpacketTUserGradeLimit;

    /**
     * 红包图片
     */
    private String redpacketTImg;

    /**
     * 红包领取方式，1-积分兑换(默认)，2-卡密兑换，3-免费领取
     */
    private Boolean redpacketTAccessMethod;

    /**
     * 推荐状态，0-为不推荐，1-推荐
     */
    private Boolean redpacketTRecommend;

    /**
     * 红包使用端口 0无限制  1PC 2wap 3安卓 5ios 6微信小程序 7wap浏览器
     */
    private String redpacketUsePlatform;

    /**
     * 红包领用端口 0无限制  1PC 2wap 3安卓 5ios 6微信小程序 7wap浏览器
     */
    private String redpacketGetPlatform;

    /**
     * 多品红包类型：1多品配置2专区
     */
    private Integer manyRedpacketType;

    /**
     * 多品红包跳转类型3:链接;12:为你推荐13:特产推荐;14:新品推荐;19:新人专区;20:平台活动专区;21:9.9包邮专区;22:品牌生活;23:好货溯源;24:蜂狂特价;25:猜你喜欢;26:时令果蔬;27:节日钜惠;28:限时折扣;29:超级拼团;34:多品红包列表
     */
    private Integer actionType;

    /**
     * 多品红包跳转地址
     */
    private String manyRedpacketSkipUrl;

    /**
     * 操作人
     */
    private String orderOperator;

    /**
     * 操作时间
     */
    private Date orderOperatorTime;

    /**
     * 跳转名称
     */
    private String actionName;

    /**
     * 审批
     */
    private String redpacketRemark;

    /**
     * 前端是否显示1显示2不显示
     */
    private Byte frontEndIsDisplay;

    /**
     * 红包是否倒计时1-否2-是
     */
    private Byte redpacketIsBooking;

    /**
     * 抢红包开始时间
     */
    private Date robRedpacketStartDate;

    /**
     * 抢红包结束时间
     */
    private Date robRedpacketEndDate;

    /**
     * 自红包发放起，有效天数
     */
    private Integer redTermofvalidity;


}