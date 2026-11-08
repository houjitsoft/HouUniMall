package com.houjiang.order.dto;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Created by Amos
 * Date: 2021/5/15
 * Time: 下午4:09
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FxGoodsInfoDTO extends PageHelper {

    /**
     * 商品的唯一标识符
     */
    private int id;

    /**
     * 商品的名称
     */
    private String goodsName;

    /**
     * 商品的单价
     */
    private String scPrice;

    /**
     * 会员价
     */
    private String cbPrice;

    /**
     * 市场价
     */
    private String pcPrice;

    /**
     * 商品的状态
     */
    private Integer goodsStatus;

    /**
     * 销售数量
     */
    private String saleNums;

    /**
     * 库存数量
     */
    private String goodsNums;

    /**
     * 奖励的类型
     */
    private Integer rewardType;

    /**
     * 与供应商的分成比例
     */
    private String tgRate;

    /**
     * 与分销商的分成比例
     */
    private String fxRate;

    /**
     * 供应商的佣金
     */
    private BigDecimal tgCommission;

    /**
     * 分销商的佣金
     */
    private BigDecimal fxCommission;

    /**
     * 订单数量
     */
    private int orderNum;

    /**
     * 商店的ID
     */
    private Integer shopId;

    /**
     * 商店的名称
     */
    private String shopName;

    /**
     * 商店用户的ID
     */
    private String shopUserId;

    /** var1, var2, var3, var4都是私有的字符串变量，其具体用途根据实际业务逻辑而定 */
    private String var1;
    private String var2;
    private String var3;
    private String var4;

    /**
     * 创建的时间
     */
    private String createTime;

    /**
     * 更新的时间
     */
    private String updateTime;

    /**
     * 数据的状态
     */
    private int dataStatus;

    /**
     * 商品的ID
     */
    private Integer goodsId;

    /**
     * 资源的ID
     */
    private Integer commonId;

    public FxGoodsInfoDTO(String saleNums,Integer shopId, Integer goodsId) {
        this.saleNums = saleNums;
        this.shopId = shopId;
        this.goodsId = goodsId;
    }

}
