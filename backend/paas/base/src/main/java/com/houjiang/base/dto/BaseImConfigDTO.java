package com.houjiang.base.dto;

import lombok.Data;

/**
 * 更新im 配置
 * @author Amos
 */
@Data
public class BaseImConfigDTO {

    /**
     * 请求token
     */
    private String token;

    /**
     * id
     */
    private Integer id;

    /**
     * im域名
     */
    private String imDomainName;

    /**
     * im app key
     */
    private String imAppKey;

    /**
     * im app Secret
     */
    private String imAppSecret;

    /**
     * im token
     */
    private String imToken;

    /**
     * 网易app key
     */
    private String wyAppKey;

    /**
     * 网易 app Secret
     */
    private String wyAppSecret;

}
