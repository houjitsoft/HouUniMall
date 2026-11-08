package com.houjiang.commons.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * @Description:
 * @CreateDate: 2025/7/12 17:24
  * @author Amos
 */
@Getter
public enum CustomerTypeEnum {


    /** Yong Jia */
    YONG_JIA(1,"用家"),
    /** Pi Fa */
    PI_FA(1,"用家"),
    ;
    private final Integer type;

    private final String desc;

    CustomerTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static Boolean isYongjia(Integer type) {
        return Objects.equals(YONG_JIA.getType(), type);
    }

    public static Boolean isPifa(Integer type) {
        return Objects.equals(PI_FA.getType(), type);
    }
}
