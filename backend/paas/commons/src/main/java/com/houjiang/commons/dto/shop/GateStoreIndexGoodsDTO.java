package com.houjiang.commons.dto.shop;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * @ClassName GateStoreIndexGoodsDTO
 * @Description 门店首页商品查询
 * @Author Amos
 * @Date 2024/5/8 19:36
 * @Version 1.0
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GateStoreIndexGoodsDTO extends PageHelper {
    /**
     * 店铺门店ID (不能为空)
     */
    private Integer shopGateId;
    /**
     * 店铺分类id
     */
    private Integer shopGoodsCatId;
    /**
     * 店铺分类id集合
     */
    private List<Integer> shopGoodsCatIdList;
    /**
     * 商品名称
     */
    private String commonName;
    /**
     * 商品id
     */
    private String commonId;
    /**
     * 规格
     */
    private List<Map> specValues;
}
