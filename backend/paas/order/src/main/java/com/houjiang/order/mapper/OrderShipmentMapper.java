package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.order.entity.OrderShipmentEntity;
import com.houjiang.order.vo.LogisticsMessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单发货表(OrderShipment)表数据库访问层
 *
 * @author Amos
 * @since 2024-01-30 15:46:43
 */
@Mapper
public interface OrderShipmentMapper extends BaseMapper<OrderShipmentEntity> {


    /**
     * 查询物流消息
     *
     * @param orderId
     * @return
     */
    List<LogisticsMessageVO> queryLogisticsMessage(String orderId);

    /**
     * 查询订单第一次发货时的仓库id
     * @param orderId 订单id
     * @return 仓库id
     */
    Integer queryStorageId(@Param("orderId") String orderId);

    /**
     * 根据订单id和商品id查询订单发货记录
     * @param orderId 订单id
     * @param goodsId 商品id
     * @return 订单发货记录
     */
    List<OrderShipmentEntity> selectListByGoodsId(@Param("orderId") String orderId,@Param("goodsId") String goodsId);
}
