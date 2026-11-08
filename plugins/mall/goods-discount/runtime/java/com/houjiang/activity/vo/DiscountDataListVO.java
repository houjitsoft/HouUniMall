package com.houjiang.activity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName DiscountDataEntity
 * @Description
 * @Author Amos
 * @Date 2022/11/2 15:33
 * @Version 1.0
 * @author Amos
 */

@Data
public class DiscountDataListVO {
   /**
    * 数量
    */
   private Integer count;
   /**
    * 商品下单信息
    */
   private List<DiscountDataVO> dataEntityList;
   /**
    * 总成交额
    */
   private Integer allGoodsCount;
   /**
    * 下单数
    */
   private BigDecimal allDealAmount;
   /**
    * 新客户下单数
    */
   private Integer allNewerOrderNum;
   /**
    * 老客户下单数
    */
   private Integer allOlderOrderNum;

}
