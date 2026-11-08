package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.order.dto.ReturnOrderDTO;
import com.houjiang.order.dto.handleBarterOrderDTO;
import com.houjiang.order.entity.OrderBarterEntity;
import com.houjiang.order.vo.BarterShippingInfoVO;
import com.houjiang.order.vo.ReturnOrderDetailVO;
import com.houjiang.order.vo.ReturnOrderVO;
import com.houjiang.order.vo.SellerOrderDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单换货表(OrderBarter)表数据库访问层
 *
 * @author Amos
 * @since 2025-05-07 09:36:29
 */
@Mapper
public interface OrderBarterMapper extends BaseMapper<OrderBarterEntity> {


    /**
     * TODO 补充方法说明
     * @param page TODO
     * @return TODO
     */
    IPage<ReturnOrderVO> selectBarterOrderList(Page<ReturnOrderVO> page, @Param("param") ReturnOrderDTO dto);

    /**
     * TODO 补充方法说明
     * @param returnCode TODO
     * @return TODO
     */
    SellerOrderDetailVO queryBarterOrderDetails(String returnCode);

    /**
     * TODO 补充方法说明
     * @param returnCode TODO
     * @return TODO
     */
    Integer getBarterStateByReturnCode(String returnCode);

    /**
     * TODO 补充方法说明
     * @param page TODO
     * @return TODO
     */
    IPage<ReturnOrderDetailVO> selectBarterOrderListToPlatform(Page<ReturnOrderDetailVO> page, @Param("param") ReturnOrderDTO dto);

    /**
     * TODO 补充方法说明
     * @param dto TODO
     * @return TODO
     */
    ReturnOrderDetailVO selectBarterOrderDetail(ReturnOrderDTO dto);

    /**
     * TODO 补充方法说明
     * @param returnCode TODO
     * @return TODO
     */
    List<BarterShippingInfoVO> selectBarterShippingInfoList(String returnCode);

    /**
     * TODO 补充方法说明
     * @param page TODO
     * @return TODO
     */
    IPage<ReturnOrderVO> selectBarterDropShippingList(Page<ReturnOrderVO> page, @Param("param") ReturnOrderDTO dto);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<handleBarterOrderDTO> handleBarterOrderApplyToJob();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<handleBarterOrderDTO> handleBarterOrderToJob();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<String> afterSalesOutOrderToJob();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<String> buyTimeOutReturnGoodsToJob();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<handleBarterOrderDTO> platformApproveToJob();
}
