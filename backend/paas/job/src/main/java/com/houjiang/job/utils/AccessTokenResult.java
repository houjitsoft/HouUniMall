package com.houjiang.job.utils;


import lombok.Data;

/**
 * 访问令牌
 * @author Amos
 */
@Data
public class AccessTokenResult {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 过期
     */
    private Integer expiresIn;

}
