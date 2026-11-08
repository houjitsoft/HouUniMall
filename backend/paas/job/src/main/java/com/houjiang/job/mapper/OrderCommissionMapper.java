package com.houjiang.job.mapper;

import com.houjiang.commons.vo.order.CommissionSettleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName OrderCommissionMapper
 * @Description
 * @Author Amos
 * @Date 2023/9/5 13:21
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrderCommissionMapper {

    /**
     * 查询订单结算信息。
     *
     * @return 返回符合条件的订单结算信息列表。
     */
    List<CommissionSettleVO> queryOrderSettle();

    /**
     * 批量更新订单结算状态。
     *
     * @param orderId 订单ID
     * @param settleRecordId 结算记录ID，用于唯一标识一条结算记录。
     * @return 返回更新影响的记录数。
     */
    int updateOrderSettleBatch(@Param("orderId") String orderId,@Param("settleRecordId") String settleRecordId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    CommissionSettleVO queryWaitSettleAmount(@Param("orderId") String orderId);

}
