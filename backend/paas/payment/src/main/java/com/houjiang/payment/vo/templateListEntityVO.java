package com.houjiang.payment.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class templateListEntityVO {
     /**
      * 创建时间
      */
     private Long createTime;

     /**
      * 用户版本
      */
     private String userVersion;

     /**
      * 用户描述
      */
     private String userDesc;

     /**
      * 模板ID
      */
     private Integer templateId;

     /**
      * 草稿ID
      */
     private Integer draftId;

     /**
      * 来源小程序AppID
      */
     private String source_miniprogramAppid;

     /**
      * 来源小程序
      */
     private String sourceMiniprogram;

     /**
      * 开发者
      */
     private String developer;

     /**
      * 模板类型
      */
     private Integer templateType;

     /**
      * 类别列表
      */
     private List categoryList;

}
