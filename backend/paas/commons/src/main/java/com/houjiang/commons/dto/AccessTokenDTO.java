package com.houjiang.commons.dto;


import lombok.Data;

/**
 * @author Amos
 * @version 1.0.0
 * @ClassName AccessTokenResult.java
 * @Description AccessTokenDTO
 * @createTime 2021年08月17日 18:49:00
 */
@Data
public class AccessTokenDTO {

    private String accessToken;

    private Integer expiresIn;

}
