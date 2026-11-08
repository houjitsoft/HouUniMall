package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.vo.user.ShopGateInfo;
import com.houjiang.order.dto.OrderBaseRiderDTO;
import com.houjiang.order.vo.OrderDetailGoodsItemVO;
import com.houjiang.order.vo.OrderRiderBasePageVO;
import com.houjiang.order.vo.OrderRiderBaseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName OrderRiderBaseMapper
 * @Description 骑手订单Mapper
 * @Author Amos
 * @Date 2024/4/29 14:08
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrderRiderBaseMapper {

    /**
     * 分页查询订单管理列表-商家确认
     * @param page
     * @param param
     * @return
     */
    IPage<OrderRiderBasePageVO> queryMerchantConfirmationPage(Page<OrderRiderBasePageVO> page, @Param("param") OrderBaseRiderDTO param);

    /**
     * 分页查询订单管理列表-商家不确认
     * @param page
     * @param param
     * @return
     */
    IPage<OrderRiderBasePageVO> queryMerchantNotConfirmationPage(Page<OrderRiderBasePageVO> page, @Param("param") OrderBaseRiderDTO param);

    /**
     * 查询订单信息
     * @param orderId
     * @return
     */
    OrderRiderBaseVO queryOrderRiderBase(@Param("orderId") String orderId);

    /**
     * 查询订单商品详情
     * @param orderId
     * @return
     */
    List<OrderDetailGoodsItemVO> queryOrderGoodsItem(@Param("orderId") String orderId);

    /**
     * 查询门店信息
     * @param orderGateShopId
     * @return
     */
    List<ShopGateInfo> shopGateInfoItem(@Param("orderGateShopId") Integer orderGateShopId);

    /**
     * 修改骑手接单时间
     * @param format
     * @param orderId
     */
    void updateDeliveryTime(@Param("format") String format, @Param("orderId") String orderId);

    /**
     * 查询提货编号
     * @param orderCreateTime
     * @return
     */
    String querySerial(@Param("orderCreateTime") String orderCreateTime);

    /**
     * 修改提货编号
     * @param serialCode
     * @param orderId
     */
    void updatePickupNum(@Param("serialCode") String serialCode, @Param("orderId") String orderId);

    /**
     * 加配送费
     * @param totalPrice
     * @param userId
     */
    void updateTotalPrice(@Param("totalPrice") String totalPrice, @Param("userId") String userId);

    /**
     * 查询骑手 处理中 各种状态订单数量
     * @param userId
     * @param orderStatus
     * @param pickup
     * @return
     */
    Integer queryRiderProcessingOrderNum(@Param("userId")String userId, @Param("orderStatus")Integer orderStatus, @Param("pickup")Integer pickup);
}
