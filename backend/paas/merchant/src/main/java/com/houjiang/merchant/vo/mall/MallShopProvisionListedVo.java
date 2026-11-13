package com.houjiang.merchant.vo.mall;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/** 平台后台商家列表行。 */
@Data
public class MallShopProvisionListedVo implements Serializable {
    private Integer id;
    private Long createTime;
    private Integer entryType;
    private Integer shopLevelId;
    private String levelName;
    private String previousName;
    private Integer auditStatus;
    private String name;
    private String logo;
    private Integer type;
    private String typeText;
    private Integer shopStatus;
    private Integer businessStatus;
    private String contact;
    private String mobile;
    private String addressInfo;
    private String lat;
    private String lng;
    private Map<String, Object> account;
    private Map<String, Object> shopCategory;
    /** 入驻申请（尚未开通店铺）的 join_id，已开通店铺为 null。 */
    private String joinId;
    /** 数据来源：joinApply = hou_shop_join_base 入驻申请；null = 已开通店铺。 */
    private String source;
    /** 审核时间（秒级时间戳），仅入驻申请有值。 */
    private Long auditTime;
    /** 审核备注 / 拒绝理由，仅入驻申请有值。 */
    private String auditComment;
}
