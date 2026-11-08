package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.vo.order.InvoiceEntityInfoVO;
import com.houjiang.order.dto.InvoiceBankDTO;
import com.houjiang.order.entity.OrderInvoiceInfoEntity;
import com.houjiang.order.vo.OrderInvoiceDetailVO;
import com.houjiang.order.vo.PlatformOrderInvoiceDetailVO;
import com.houjiang.order.vo.PlatformOrderInvoiceVO;
import com.houjiang.order.vo.SellerOrderInvoiceDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @ClassName OrderInvoiceInfoMapper
 * @Description 订单发票表
 * @Author Amos
 * @Date 2023-05-31 11:59:35
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrderInvoiceInfoMapper extends BaseMapper<OrderInvoiceInfoEntity> {


    /**
     * 添加发票信息。
     * <p>
     * 该方法用于接收一个发票实体信息的视图对象，并将其添加到系统中。
     * 参数中的@param注释用于说明传入参数的意图和用途，增加了代码的可读性。
     * 由于方法本身不返回任何值，因此没有返回值的说明。
     *
     * @param invoiceInfo 发票实体信息的视图对象，包含了需要添加到系统中的发票详细信息。
     */
    void addInvoiceInfo(@Param("param") InvoiceEntityInfoVO invoiceInfo);

    /**
     * 查看申请发票状态
     * @param orderId
     * @return
     */
    Integer getInvoiceStatus(@Param("orderId")String orderId);

    /**
     * 查询订单实付价
     * @param param
     * @return
     */
    BigDecimal getInvoiceAmount(@Param("param")Map<Object, Object> param);

    /**
     * 根据订单号查询发票id
     * @param param
     * @return
     */
    Integer getInvoiceId(@Param("param") Map<Object, Object> param);

    /**
     * 修改发票状态
     * @param param
     */
    void updateInvoiceInfo(@Param("param") Map<Object, Object> param);

    /**
     * 新增发票信息
     *
     * @param param
     */
    void insertInvoiceInfo(@Param("param") Map<Object, Object> param);

    /**
     * 修改订单发票状态
     * @param param
     */
    void updateInvoiceStatus(@Param("param") Map<Object, Object> param);

    /**
     * 查询发票详情
     *
     * @param param
     * @return
     */
    OrderInvoiceDetailVO queryInvoiceDetail(@Param("param") Map<Object, Object> param);

    /**
     * 查询发票订单
     *
     * @param param
     * @return
     */
    IPage<PlatformOrderInvoiceVO> queryPlatformOrderInvoiceInfo(IPage<PlatformOrderInvoiceVO> page,@Param("param") Map<String, Object> param);

    /**
     * 平台查看发票详情
     */
    PlatformOrderInvoiceDetailVO queryPlatformInvoiceDetail(@Param("param") Map<Object, Object> param);

    /**
     * 修改审核状态和审核备注信息
     * @param param
     */
    void auditInvoiceForSeller(@Param("param") Map<Object, Object> param);

    /**
     * 修改发票状态
     * @param param
     */
    void updateOrderInvoiceStatus(@Param("param") Map<Object, Object> param);

    /**
     * 查询卖家发票详细信息
     * @param orderId
     * @return
     */
    SellerOrderInvoiceDetailVO querySellerInvoiceDetail(@Param("orderId") String orderId);

    /**
     * 获取发票类型
     *
     * @param param
     * @return
     */
    Integer getInvoiceType(Map<String, Object> param);

    /**
     * TODO 补充方法说明
     * @param orderId TODO
     * @return TODO
     */
    InvoiceBankDTO queryInvoiceInfo(String orderId);
}
