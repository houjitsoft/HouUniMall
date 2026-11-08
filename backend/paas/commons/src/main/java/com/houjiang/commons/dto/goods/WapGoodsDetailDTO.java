package com.houjiang.commons.dto.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 商品详情
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WapGoodsDetailDTO {

    /**
     * 商品id
     */
    private String goodsId;

    /**
     * token
     */
    private String token;

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 场次时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sessionTime;

    /**
     * 是否是搜索列表进入
     */
    private Integer isSearchList;

    /**
     * 商品来源:0.商家自营商品,1:代发平台供应链商品,2:怡亚通,
     */
    private Integer supplierGoods;

    private List<Map> specValues;

    public WapGoodsDetailDTO(String goodsId, String token, String userId) {
        this.goodsId = goodsId;
        this.token = token;
        this.userId = userId;
    }

    public WapGoodsDetailDTO(String goodsId, String token, Integer commonId) {
        this.goodsId = goodsId;
        this.token = token;
        this.commonId = commonId;
    }
}
