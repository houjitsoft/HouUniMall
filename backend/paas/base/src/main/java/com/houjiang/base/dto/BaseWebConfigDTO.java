package com.houjiang.base.dto;

import lombok.Data;

/**
 * @ClassName BaseWebConfigDTO
 * @Description 网站配置入参
 * @Author Amos
 * @Date 2024/4/29 10:59
 * @Version 1.0
 * @author Amos
 */
@Data
public class BaseWebConfigDTO {

    /**
     * token
     */
    private String token;

    /**
     * key
     */
    private String configKey;

    /**
     * 类型
     */
    private String configType;

    /**
     * 值
     */
    private String configValue;
}
