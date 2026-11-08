package com.houjiang.goods.vo;

import lombok.Data;

/**
 * @ClassName ManageTransportGoodsVO
 * @Author Amos
 * @Date 2023/7/3 10:33
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageTransportGoodsVO {
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 商品名
     */
    private String commonName;
    /**
     * 商品图片
     */
    private String commonImage;
    /**
     * 商品状态 0下架（放入仓库），1正常，10违规（禁售）
     */
    private Integer commonState;
    /**
     *  商品审核 1通过，0未通过，10审核中
     */
    private Integer commonVerify;
}
