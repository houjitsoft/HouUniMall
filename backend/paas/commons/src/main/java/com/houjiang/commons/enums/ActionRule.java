package com.houjiang.commons.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 状态规则枚举

/**
 * @author Amos
 */
@Getter
@AllArgsConstructor
public enum ActionRule {
    /** 暂无轨迹信息 */
    RULE_0("0", "0", "暂无轨迹信息"),
    /** 待揽收 */
    RULE_10("10", "10", "待揽收"),
    /** 已揽收 */
    RULE_1("1", "1", "已揽收"),
    /** 在途中 */
    RULE_2("2", "2", "在途中"),
    /** 签收 */
    RULE_3("3", "3", "签收"),
    /** 问题件 */
    RULE_4("4", "4", "问题件"),
    /** 转寄 */
    RULE_5("5", "5", "转寄"),
    /** 清关 */
    RULE_6("6", "6", "清关");

    /** 匹配前缀 */
    private final String prefix;
    /** 目标Action */
    private final String targetAction;
    /** 站点描述 */
    private final String stationDesc;
}
