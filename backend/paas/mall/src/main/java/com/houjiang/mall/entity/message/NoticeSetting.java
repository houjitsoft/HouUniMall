package com.houjiang.mall.entity.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知设置实体类
 * 对应数据库表：hou_mall_notice_setting
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_notice_setting")
public class NoticeSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 店铺 ID
     */
    private Long shopId;

    /**
     * 通知类型：order-pay 订单支付 success 交易成功 refund 退款
     */
    private String notifyType;

    /**
     * 是否启用短信：0-否 1-是
     */
    private Integer smsEnabled;

    /**
     * 是否启用 APP 推送：0-否 1-是
     */
    private Integer appPushEnabled;

    /**
     * 是否启用站内信：0-否 1-是
     */
    private Integer inSiteEnabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
