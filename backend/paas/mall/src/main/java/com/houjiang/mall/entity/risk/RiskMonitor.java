package com.houjiang.mall.entity.risk;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风控监控实体类
 * 表名: hou_mall_risk_monitor
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_risk_monitor")
public class RiskMonitor implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 监控类型 1-库存监控 2-价格监控 3-订单监控 4-交易监控 5-账户监控
     */
    private Integer monitorType;

    /**
     * 监控对象ID（商品ID/订单ID/用户ID等）
     */
    private Long targetId;

    /**
     * 监控对象类型
     */
    private String targetType;

    /**
     * 监控对象名称
     */
    private String targetName;

    /**
     * 当前值
     */
    private BigDecimal currentValue;

    /**
     * 基准值
     */
    private BigDecimal baseValue;

    /**
     * 变化率
     */
    private BigDecimal changeRate;

    /**
     * 监控状态 0-正常 1-预警 2-异常
     */
    private Integer monitorStatus;

    /**
     * 最后检查时间
     */
    private LocalDateTime lastCheckTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;
}
