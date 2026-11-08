package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

/**
 * @ClassName BrandInfoVO
 * @Description
 * @Author Amos
 * @Date 2024/11/7 15:40
 * @Version 1.0
 * @author Amos
 */

@Data
public class BrandInfoVO {

    /**
     * 首字母
     */
    private String firstWord;

    /**
     * 品牌图片地址
     */
    private String logoPath;

    /**
     * 名称
     */
    private String name;

    /**
     * ID
     */
    private String id;
}
