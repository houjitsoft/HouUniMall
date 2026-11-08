package com.houjiang.order.mapper;

import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.order.dto.OrderBalanceRechargeDTO;
import com.houjiang.order.entity.OrderBalanceRechargeEntity;
import com.houjiang.commons.vo.order.OrderBalanceRechargeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName OrderBalanceRechargeMapper
 * @Description  余额充值订单表
 * @Author Amos
 * @Date 2024-09-19 09:35:39
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrderBalanceRechargeMapper extends BaseMapper<OrderBalanceRechargeEntity> {

    /**
     * 创建余额充值订单
     * @param orderBalanceRechargeEntity
     * @return
     */
    OrderBalanceRechargeEntity createBalanceRechargeOrder(@Param("param") OrderBalanceRechargeEntity orderBalanceRechargeEntity);

    /**
     * 修改余额充值订单
     * @param map
     * @return
     */
    Integer updateBalanceRechargeOrder(@Param("param")Map<Object, Object> map);

    /**
     * 查询离线充值订单
     * @param page
     * @param data
     * @return
     */
    IPage<OrderBalanceRechargeVO> queryOfflineRechargeOrder(IPage page, @Param("param") Map data);

    /**
     * 修改充值状态
     * @param orderBalanceRechargeEntity
     */
    void updateOfflineRechargeOrder(@Param("param") OrderBalanceRechargeEntity orderBalanceRechargeEntity);

    /**
     * 根据充值单号查询充值单详情
     * @param rechargeOrderId
     * @return
     */
    OrderBalanceRechargeVO queryRechargeOrderById(@Param("rechargeOrderId") String rechargeOrderId);
}
