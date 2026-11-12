package com.houjiang.userms.dto;

import lombok.Data;

/**
 * 用户行为与留存分析 · 统一埋点上报
 *
 * <p>供 PC / H5 / 小程序 / 管理端把行为事件写入 hou_user_behavior_event。
 * eventTime 为空时由 Service 取当前时间；eventType 必须在六类白名单内。</p>
 *
 * @author Amos
 */
@Data
public class BehaviorTrackDTO {

    /** 会员ID，必填 */
    private String userId;

    /** 行为类型：login / view / search / cart / order / share */
    private String eventType;

    /** 行为发生时间 yyyy-MM-dd HH:mm:ss，可空 */
    private String eventTime;

    /** 来源端：pc / h5 / uniapp / miniapp / admin */
    private String source;

    /** 业务对象ID：商品ID / 订单号 / 搜索词 */
    private String refId;

    /** 扩展信息 */
    private String ext;
}
