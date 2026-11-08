package com.houjiang.goods.vo;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* @ClassName
/**
* @Description  分销商申请信息表
* @Author Amos
* @Date 2023-05-24 09:51:09
* @Version 1.0
* @author Amos
*/

@Data
public class BaseDealerApplyInfoVO implements Serializable {
   /**
    * 主键id
    */
   private String id;
   /**
    * 供应商店铺id
    */
   private Integer supplierShopId;
   /**
    * 供应商店铺名
    */
   private String supplierShopName;
   /**
    * 供应商用户id
    */
   private Integer supplierUserId;
   /**
    * 供应商用户名
    */
   private String supplierUserName;
   /**
    * 经销商id
    */
   private Integer dealerShopId;
   /**
    * 经销商店铺名
    */
   private String dealerShopName;
   /**
    * 经销商用户id
    */
   private Integer dealerUserId;
   /**
    * 经销商用户名
    */
   private String dealerUserName;
   /**
    * 经销商手机号
    */
   private String dealerUserPhone;
   /**
    * 分销状态 1待审核  2审核通过 3审核不通过  4已删除
    */
   private Integer dealerStatus;
   /**
    * 申请审核说明
    */
   private String dealerRemark;
   /**
    * 经销等级
    */
   private Integer dealerLevelId;
   /**
    * 经销等级名称
    */
   private String dealerLevelName;
   /**
    * 经销折扣
    */
   private Integer dealerDiscount;
   /**
    * 创建时间
    */
   private Date createTime;


}

