package com.houjiang.userms.dto;

import lombok.Data;

/**
 * 会员画像查询条件
 *
 * <p>数据来源全部为库内真实数据：会员主表、订单主表、登录信息、商品足迹、手工标签。
 * 不做任何模拟或补数，缺失维度由接口显式返回可用性标记。</p>
 *
 * @author Amos
 */
@Data
public class MemberPortraitQueryDTO {

    /** 昵称 / 手机号 / 用户ID 模糊匹配 */
    private String keyword;

    /** 分层筛选：core / growth / edge，空表示全部 */
    private String layer;

    /** 最少订单数，空表示不限制 */
    private Integer minOrders;

    /** 页码，从 1 开始 */
    private Integer page = 1;

    /** 每页条数 */
    private Integer size = 20;
}
