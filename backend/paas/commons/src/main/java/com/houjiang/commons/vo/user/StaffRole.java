package com.houjiang.commons.vo.user;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Amos
 */
@Getter
@AllArgsConstructor
public enum StaffRole {
    /** 店长 */
    SHOP_KEEPER(1L,"店长"),
    /** 微信当面付 */
    CASHIER(2L,"微信当面付");

    @EnumValue
    @JsonValue
    private final Long code;

    private final String description;

    public static StaffRole of(Long code){
        for(StaffRole one:values()){
            if(one.getCode().equals(code)){
                return one;
            }
        }
        return null;
    }
}
