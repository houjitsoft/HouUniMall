package com.houjiang.mall.entity.live;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 直播商品审核实体类
 * 表名: hou_mall_live_product_audit
 * @author Amos
 */
@Data
@TableName("hou_mall_live_product_audit")
public class LiveProductAudit implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long liveId;
    private Long goodsId;
    private String goodsName;
    private String goodsImage;
    private BigDecimal price;
    /** 审核状态: 0-待审核 1-通过 2-拒绝 */
    private Integer auditStatus;
    private String auditRemark;
    private LocalDateTime submitTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
