package com.houjiang.job.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderConsignerMapper
 * @Description
 * @Author Amos
 * @Date 2023/5/17 14:38
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrderConsignerMapper {

    /**
     * 查询虚拟订单时间
     *
     * @return
     */
    List<String> queryVirtualOrderTime();

    /**
     * 上传虚拟订单
     *
     * @param order
     */
    void upVirtualOrder(String order);

    /**
     * 根据订单id查订单金额
     *
     * @param orderId
     * @return
     */
    Map<String, Object> selectUserMoneyByOrderId(String orderId);

    /**
     * 通过主键选择订单信息。
     *
     * @param orderId 订单的唯一标识符。使用此标识符从数据库中检索特定订单的信息。
     * @return 返回匹配主键的订单对象。如果找不到匹配的订单，则返回null。
     */
    Integer selectByPrimaryKey(@Param("orderId") String orderId);

    /**
     * 更新订单的销售时间后的信息。
     * <p>
     * 此方法用于在订单的销售时间之后更新订单的状态或相关信息。
     *
     * @param orderId    订单的唯一标识符。用于定位需要更新的订单。
     * @param returnTime 返回时间，表示订单的退货或退款时间。此参数用于更新订单的返回时间信息。
     * @return 返回一个布尔值，表示更新操作是否成功。成功则返回true，失败则返回false。
     */
    boolean updateOrderAfterSaleTime(@Param("orderId") String orderId, @Param("returnTime") String returnTime);
}

