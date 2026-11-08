package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import com.houjiang.order.dto.OrderReturnPlatformListDTO;
import com.houjiang.order.dto.ResourceInfoDTO;
import com.houjiang.order.entity.OrderReturnPlatformEntity;
import com.houjiang.order.vo.OrderReturnPlatformVO;
import com.houjiang.order.vo.ReturnOrderDetailVO;
import com.houjiang.order.vo.ReturnOrderDetailsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.order.entity.OrderReturnPlatform
 * @author Amos
 */
@Mapper
public interface OrderReturnPlatformMapper extends BaseMapper<OrderReturnPlatformEntity> {

    /**
     * 更新返回订单
     *
     * @param orderReturnPlatform
     * @return
     */
    Integer updateOrderReturnBuyerVoucher(@Param("param") OrderReturnPlatformVO orderReturnPlatform);

    /**
     * 新增返回订单
     *
     * @param orderReturnPlatform
     * @return
     */
    Integer insertOrderReturnPlatform(@Param("param") OrderReturnPlatformVO orderReturnPlatform);

    /**
     * 查询订单返回平台
     *
     * @param orderId
     * @return
     */
    OrderReturnPlatformVO selectOrderReturnPlatform(String orderId);

    /**
     * 查询商品返回详情
     *
     * @param orderId
     * @return
     */
    ReturnOrderDetailVO queryOrderReturnDetail(@Param("orderId") String orderId);

    /**
     * 查询商品集合
     *
     * @param orderId
     * @return
     */
    List<GoodsDetailsListVO> queryGoodsList1(@Param("orderId") String orderId);

    /**
     * 通过店铺id查询数量
     *
     * @param shopId
     * @return
     */
    Integer queryShopIdByCount(@Param("shopId") Integer shopId);

    /**
     * 检查干预
     *
     * @param platform
     * @return
     */
    Integer examineIntervention(@Param("param") OrderReturnPlatformVO platform);

    /**
     * 分页查询交易维权列表
     * @param page
     * @param platformListr
     * @return
     */
    IPage<OrderReturnPlatformListDTO> selectreturnplatform(Page<OrderReturnPlatformListDTO> page,@Param("param") OrderReturnPlatformListDTO platformListr);

    /**
     * 查询订单返回数量
     * @param orderId
     * @return
     */
    Integer selectOrderReturnPlatform1(@Param("orderId") String orderId);

    /**
     * 查询用户售后商品
     * @param orderId
     * @return
     */
    List<Integer> queryReturnGoods(@Param("orderId")String orderId);


    /**
     * 查询平台介入 商家超时未处理 退货申请
     *
     * @return
     */
    List<ReturnOrderDetailsVO> updateTimeOutReturnplatform();


    /**
     * 更新微客用户佣金业绩
     *
     * @param param
     * @return
     */
    int updateVkCommissionReturn(@Param("param") Map<String, Object> param);


    /**
     * 更新超时未处理的退款平台信息。
     * <p>
     * 本方法用于查询并更新那些超过预设时间限制仍未处理的退款平台订单详情。这可能涉及到从外部系统获取最新状态，
     * 或者标记这些订单为异常以便进一步的手动干预。
     * </p>
     * <p>
     * 注意：该方法的具体实现未在此处展示，因此实际的更新逻辑（如如何处理超时订单、是否涉及数据库操作等）
     * 需要根据实际业务逻辑来确定。
     * </p>
     *
     * @return 返回一个包含更新后的退款平台订单详情的列表。每个订单详情都包含了与退款相关的各种信息，
     *         如订单状态、退款金额、处理时间等。
     */
    List<ReturnOrderDetailsVO> updateTimeOutReturnfundplatform();

    /**
     * 获取返回信息
     *
     * @param returnCode
     * @return
     */
    ResourceInfoDTO getReturnInfo(@Param("returnCode") String returnCode);

    /**
     * 根据orderId删除平台介入数据
     *
     * @param orderId
     */
    void deletePlatform(String orderId);
}




