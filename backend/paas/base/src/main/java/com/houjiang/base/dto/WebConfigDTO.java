package com.houjiang.base.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * (WebConfig)实体类
 *
 * @author Amos
 * @since 2022-08-24 09:50:33
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebConfigDTO {

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 状态值，1可能，0不可用
     */
    private String configEnable;

    /**
     * 备注
     */
    private String configComment;

    /**
     * 备注需求
     */
    private String configRemarks;

    /**
     * 颜色数组
     */
    private List<String> colorList;

    /**
     * 系统还是自定义数组
     */
    private List<Integer> setList;

    /**
     * token
     */
    private String token;

}

