package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.vo.order.OrderCommissionVO;
import com.houjiang.order.dto.UserFxOrderListDTO;
import com.houjiang.order.entity.OrderCommissionEntity;
import com.houjiang.order.vo.UserFxOrderGoodsVO;
import com.houjiang.order.vo.UserFxOrderListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderCommissionMapper
 * @Description 佣金订单
 * @Author Amos
 * @Date 2023-06-01 11:38:05
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrderCommissionMapper extends BaseMapper<OrderCommissionEntity> {

    /**
     * 插入分佣订单
     *
     * @param orderCommissionList
     */
    void insertBatch(@Param("record") List<OrderCommissionVO> orderCommissionList);

    /**
     * 查询分销订单
     *
     * @param orderId
     * @return
     */
    OrderCommissionVO selectByPrimaryKey(@Param("orderId") String orderId, @Param("returnGoods") Integer returnGoods);

    /**
     * 更新佣金订单
     *
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(@Param("param") OrderCommissionVO record);

    /**
     * 查询订单plus总折扣
     *
     * @param orderId
     * @param goodsId
     * @return
     */
    Map selectplusdis(@Param("orderId") String orderId, @Param("list") List goodsId);

    /**
     * 查询微客具体的分销订单。
     *
     * @param param 用户分销订单列表的查询参数
     * @return 用户分销订单列表
     */
    IPage<UserFxOrderListVO> queryUserFxOrderList(Page<UserFxOrderListVO> page, @Param("param") UserFxOrderListDTO param);

    /**
     * 查询用户分销订单的商品信息。
     *
     * @param orderId 分销订单ID
     * @return 用户分销订单的商品信息列表
     */
    List<UserFxOrderGoodsVO> queryUserShareGoods(@Param("orderId") String orderId);

    /**
     * 查看微客的累计推广商品
     *
     * @param param 包含查询条件的参数对象
     * @return 返回查询结果的响应对象
     */
    IPage<UserFxOrderGoodsVO> queryUserFxOrderGoodsList(Page<UserFxOrderGoodsVO> page, @Param("param") UserFxOrderListDTO param);

    /**
     * 查询今日推广商品
     */
    IPage<UserFxOrderGoodsVO> selectVkTodayShareGoods(Page<UserFxOrderGoodsVO> page, @Param("param") UserFxOrderListDTO param);

    /**
     * 查询累计推广订单
     */
    IPage<UserFxOrderListVO> queryVkTotalTgOrderList(Page<UserFxOrderListVO> page, @Param("param") UserFxOrderListDTO param);

    /**
     * 查询用户结算佣金订单
     */
    IPage<UserFxOrderListVO> selectVkSettlementOrder(Page<UserFxOrderListVO> page, @Param("param") UserFxOrderListDTO param);

    /**
     * 查询用户今日预估收益
     */
    IPage<UserFxOrderListVO> selectVkEstimated(Page<UserFxOrderListVO> page, @Param("param") UserFxOrderListDTO param);

    /**
     * 根据店铺ID查询用户的返现订单列表。
     */
    IPage<UserFxOrderListVO> queryUserFxOrderListByShop(Page<UserFxOrderListVO> page, @Param("param") UserFxOrderListDTO param);


    /**
     * 修改订单分佣状态
     * @param orderId
     */
    void updateOrderCommissionState(@Param("orderId") String orderId);

    /**
     * 查询佣金订单
     * @param orderId
     * @return
     */
    OrderCommissionVO selectOrderCommission(@Param("orderId") String orderId);
}
