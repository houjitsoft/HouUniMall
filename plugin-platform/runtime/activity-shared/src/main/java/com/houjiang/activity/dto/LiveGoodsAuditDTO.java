package com.houjiang.activity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 直播商品审核 DTO
 * @author Amos
 */
@Data
public class LiveGoodsAuditDTO implements Serializable {

    private Long id;

    private Long roomId;

    private Long goodsId;

    private String goodsName;

    private Integer auditStatus;

    private String rejectReason;

    private Integer page = 1;

    private Integer size = 10;
}
