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
}
