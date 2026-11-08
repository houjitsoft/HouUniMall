package com.houjiang.commons.vo.goods;

import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealerPutawayRecordVO {
    private Integer id;

    private Integer supplyCommonId;

    private Integer supplyGoodsId;

    private Integer dealerCommonId;

    private Integer dealerGoodsId;
    /**
     * 经销商id
     */
    private Integer dealerShopId;

    private String dealerShopName;
    /**
     * 供应商店铺id
     */
    private Integer supplierShopId;

    private String supplierShopName;

    private BigDecimal dealerPrice;

    private Integer dealerNum;

    private Date dealerTime;

    private Integer recordState;

    /**
     * 1一键上架  2立即购买
     */
    private Integer dealerType;

    /** 1 是 2 否   未修改价格的商品不展示 */
    private Integer isPriceUpdate;

    public DealerPutawayRecordVO( GdsBaseDetailResultVO supplyGoodsBase, UserInfoVO userInfo, DealerPutawayRecordVO record,ManageShopBaseQueryResultVO shopBaseQueryResult ) {
        this.supplyCommonId =  supplyGoodsBase.getCommonId();
        this.supplyGoodsId = supplyGoodsBase.getGoodsId();
        this.dealerCommonId = record.getDealerCommonId();
        this.dealerGoodsId = record.getDealerGoodsId();
        this.dealerShopId = Integer.valueOf(userInfo.getShopId());
        this.dealerShopName = shopBaseQueryResult.getShopName();
        this.supplierShopId = supplyGoodsBase.getShopId();
        this.supplierShopName = supplyGoodsBase.getShopName();
        this.dealerPrice = supplyGoodsBase.getGoodsPrice();
        this.dealerNum = supplyGoodsBase.getGoodsStock();
        this.recordState = CommonType.putawayState.NORMAL.getCode();
    }
    public DealerPutawayRecordVO(Integer dealerCommonId, Integer dealerGoodsId, String dealerShopId, String dealerShopName) {
        this.dealerCommonId = dealerCommonId;
        this.dealerGoodsId = dealerGoodsId;
        this.dealerShopId = Integer.valueOf(dealerShopId);
        this.dealerShopName = dealerShopName;
    }
    public DealerPutawayRecordVO(ManageGdsCommonResultVO supplyGoodsCommon, GdbMerchantGoodsCommonVO dealerGoodsCommon, UserInfoVO userInfo, ManageShopBaseQueryResultVO shopBaseQueryResult ) {
        this.supplyCommonId =  supplyGoodsCommon.getCommonId();
        this.dealerCommonId = dealerGoodsCommon.getCommonId();
        this.dealerShopId = Integer.valueOf(userInfo.getShopId());
        this.dealerShopName = shopBaseQueryResult.getShopName();
        this.supplierShopId = supplyGoodsCommon.getShopId();
        this.supplierShopName = supplyGoodsCommon.getShopName();
        this.dealerPrice = supplyGoodsCommon.getCommonPrice();
        this.dealerNum = supplyGoodsCommon.getCommonStock();
        this.recordState = CommonType.putawayState.NORMAL.getCode();
    }

    public DealerPutawayRecordVO(Integer supplyCommonId, Integer supplyGoodsId, String dealerShopId, Integer supplierShopId, Integer recordState, Integer dealerType) {
        this.supplyCommonId = supplyCommonId;
        this.supplyGoodsId = supplyGoodsId;
        this.dealerShopId = Integer.valueOf(dealerShopId);
        this.supplierShopId = supplierShopId;
        this.recordState = recordState;
        this.dealerType = dealerType;
    }

    public DealerPutawayRecordVO(Integer dealerShopId, Integer supplierShopId, Integer recordState, Integer dealerType) {
        this.dealerShopId = dealerShopId;
        this.supplierShopId = supplierShopId;
        this.recordState = recordState;
        this.dealerType = dealerType;
    }
}
