package com.houjiang.commons.vo.third;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description: 新增流水
 * @CreateDate: 2025/7/8 23:43
 * @author Amos
 */
@Data
public class AddPointsReqVO implements Serializable {

    /**
     * 业务uuid，请保证新增积分和消耗积分唯一性
     */
    private String businessUuid;
    /**
     * 货币id
     */
    private Integer currencyId;
    /**
     * 货币名称
     */
    private String currencyName;
    /**
     * 新增标题，（字段不超过255）
     */
    private String modifyTitle;
    /**
     * 货币数量，BigDecimal类型
     */
    private BigDecimal points;
    /**
     * 店铺id，唯一标识，跳转h5链接会带过来
     */
    private Integer shopId;
    /**
     * 用户id，唯一标识，跳转h5链接会带过来
     */
    private Integer userId;
}
