package com.houjiang.business.dto;

import lombok.Data;

/**
 * @ClassName UserTokenBeanDTO
 * @Description
 * @Author cf
 * @Date 2023/6/6 15:06
 * @Version 1.0
 * @author Amos
 */
@Data
public class UserTokenBeanDTO {

    private String token;

    /**
     *  红包使用端口 0无限制
     */
    private String platForm;

    /**
     * 1PC 2wap 3安卓 5ios 6微信小程序 7wap浏览器
     */
    private String macAddress;
}
