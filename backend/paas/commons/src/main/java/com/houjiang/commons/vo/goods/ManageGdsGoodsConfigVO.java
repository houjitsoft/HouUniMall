package com.houjiang.commons.vo.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManageGdsGoodsConfigVO {

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 是否需要经销审核 1是 2否
     */
    private Integer sellCheck;

    /**
     * 允许编辑内容 1是 2否
     */
    private Integer sellEdit;

    /**
     * 允许修改价格 1是 2否
     */
    private Integer sellEditPrice;

    /**
     * 1一键上架  2立即购买
     */
    private Integer sellDeliver;

    /**
     * 说明
     */
    private String configRemark;

    /**
     * 商家货号
     */
    private String sellGoodsNum;

    /**
     * 暂时不用
     */
    private Integer sellType;


}