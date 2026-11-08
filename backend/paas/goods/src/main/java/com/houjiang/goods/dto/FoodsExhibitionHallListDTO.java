package com.houjiang.goods.dto;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.util.Date;

/**
 * 特色馆集合
 * @author Amos
 */
@Data
public class FoodsExhibitionHallListDTO extends PageHelper {

    /**
     * 展馆名称
     */
    private String exhibitionHallName;

    /**
     * 展馆ID
     */
    private Integer exhibitionHallId;


    /**
     * 展馆面积
     */
    private Integer exhibitionHallArea;

    /**
     * 展馆图片
     */
    private String exhibitionHallImage;

    /**
     * 展馆标志
     */
    private String exhibitionHallLogo;

    /**
     * 移动端展馆标志
     */
    private String exhibitionHallLogoWap;

    /**
     * 展馆描述1
     */
    private String exhibitionHallDesc1;

    /**
     * 展馆描述2
     */
    private String exhibitionHallDesc2;

    /**
     * 展馆描述3
     */
    private String exhibitionHallDesc3;

    /**
     * 商品ID
     */
    private String goodsId;

    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 展馆状态
     */
    private Integer exhibitionHallStatus;

}
