package com.houjiang.job.mapper;

import com.houjiang.commons.vo.order.CommissionSettleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * @ClassName PayFxAccountMapper
 * @Description
 * @Author Amos
 * @Date 2023/9/5 13:32
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface PayFxAccountMapper {

    /**
     * TODO 补充方法说明
     */
    void updateVkUserFxCommission(@Param("totalShareCommission") BigDecimal totalShareCommission,
                                  @Param("pendingWithdrawnCommission") BigDecimal pendingWithdrawnCommission,
                                  @Param("shareUserId") String shareUserId);

    /**
     * TODO 补充方法说明
     */
    void updateVkUserTgCommission(@Param("totalTgCommission") BigDecimal totalTgCommission,
                                  @Param("pendingWithdrawnCommission") BigDecimal pendingWithdrawnCommission,
                                  @Param("userId") String userId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    CommissionSettleVO selectVkUserFxCommission(@Param("userId") String userId);
}
