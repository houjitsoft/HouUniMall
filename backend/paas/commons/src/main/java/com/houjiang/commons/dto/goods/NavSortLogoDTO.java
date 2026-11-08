package com.houjiang.commons.dto.goods;

import lombok.Data;
/**
 * @ClassName NavSortLogoDTO
 * @Description
 * @Author Amos
 * @Date 2023-06-13 09:05:20
 * @Version 1.0
 * @author Amos
 */
@Data
public class NavSortLogoDTO {

    /**
     * 配置键
     */
    private String configKey;

    /**
     //商品id2:店铺id3:链接4:分类10:红包11:美食中国12
/**
     * 配置值:imageType;
     */
    private String configValue;

    /**
     * 配置类型
     */
    private String configType;

    /**
     * 链接
     */
    private String opValue;
    /**
     * 状态值，1可用，0不可用
     */
    private Integer configEnable;
}
