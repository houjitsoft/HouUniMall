package com.houjiang.activity.dto;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class UserRedIdDTO {

    /**
     * 用户id
     */
    private String userId;
    /**
     *红包面额
     */
    private String redpacketTPrice;
    /**
     *手机
     */
    private String userMobile;


}
