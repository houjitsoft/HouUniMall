package com.houjiang.commons.vo.third;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Description:
 * @CreateDate: 2025/7/8 23:49
 * @author Amos
 */
@Data
public class PointsFlowDetailResVO implements Serializable {

    /**
     * 创建时间，LocalDateTime类型
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    /**
     * 流水金额，两位小数
     */
    private BigDecimal flowAmount;
    /**
     * 流水描述
     */
    private String flowDesc;
    /**
     * 流水标题
     */
    private String flowTitle;
    /**
     * 流水类型，1-收入；2-支出
     */
    private Integer flowType;
    /**
     * 流水主键id
     */
    private Integer id;
}
