package com.houjiang.commons.vo.shop;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 运费模板
 *
 * @author Amos
 */
@Data
public class TransportTemplateVO {

    /**
     * 运费模板id
     */
    private Integer id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 状态，1开启，0关闭
     */
    private Integer status;

    /**
     * 固定运费
     */
    private BigDecimal constPrice;

    /**
     * 是否包邮 0不包邮 1全国包邮 2自定义
     */
    private Integer isFreePostage;

    /**
     * 规则类型,1重量2件数
     */
    private Integer ruleType;

    /**
     * 满多少kg/件包邮
     */
    private Double freePostageCond;

    /**
     * 包邮区域
     */
    private String freePostageAreas;

    /**
     * 区域城市查询条件返回结果
     */
    private List<MerchantTransportRuleVO> transportRules;

}
