package com.houjiang.job.job;

import com.houjiang.commons.dto.order.AddInventoryDTO;
import com.houjiang.commons.dto.order.CancelParaEntityDTO;
import com.houjiang.commons.dto.order.RefundParamDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.util.TimeStampUtil;
import com.houjiang.commons.vo.goods.WapGoodsSaleSumUpdateVO;
import com.houjiang.job.dto.BusinessOvertimeOrderDTO;
import com.houjiang.job.dto.RefundParamTwoDTO;
import com.houjiang.job.feign.PayMentServiceClient;
import com.houjiang.job.mapper.UpdateOrderStatusMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @ClassName UpdateOrderStatusService
 * @Description 更新订单状态服务
 * @Author Amos
 * @Date 2023/5/17 17:01
 * @Version 1.0
 * @author Amos
 */
@Service
public class UpdateOrderStatusService {
    private static final Logger logger = LoggerFactory.getLogger(UpdateOrderStatusService.class);

    @Resource
    private UpdateOrderStatusMapper updateOrderStatusMapper;

    @Autowired
    private PayMentServiceClient payMentServiceClient;

    /**
     * 更新退款超过24小时商家未处理订单为商家同意
     * 该方法用于处理超过24小时未被商家处理的退款订单，自动将这些订单的状态更新为商家同意退款。
     * 这样做的目的是为了提高订单处理的效率和自动化程度，避免订单长时间滞留未处理。
     */
    public void updateBusinessOvertimeOrder() {
        // 查询超过24小时未处理的商家退款订单
        List<BusinessOvertimeOrderDTO> pojoList = updateOrderStatusMapper.queryBusinessOvertimeOrder();
        // 获取当前时间，用于设置订单的处理时间和完成时间
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String finishTime = dateformat.format(new Date());
        // 如果查询到的订单数量大于等于1，则对每个订单进行处理
        if (pojoList.size() >= 1) {
            for (BusinessOvertimeOrderDTO pojo : pojoList) {
                // 设置订单的相关状态和信息，表示商家同意退款
                pojo.setReturnShopHandle(5);
                pojo.setReturnState(5);
                pojo.setReturnShopMessage("超时未处理，自动同意");
                pojo.setReturnShopTime(finishTime);
                pojo.setReturnFinishTime(finishTime);
                pojo.setOrderStatus(6);
                pojo.setOrderRefundStatus(2);
                pojo.setOrderFinishedTime(finishTime);
                // 根据订单信息查询支付单号
                String uOrderId = updateOrderStatusMapper.queryPaymentNumber(pojo);
                // 查询订单的交易状态
                List<Integer> orderTradeStatus = updateOrderStatusMapper.queryOrderRecordStatus(uOrderId);
                // 准备退款参数
                RefundParamDTO refundPara = new RefundParamDTO();
                boolean isRefundSuccess = false;
                String orderId = pojo.getOrderId();
                RefundParamTwoDTO refundParaTwo = updateOrderStatusMapper.queryRefundPara(orderId);
                // 设置退款参数
                refundPara.setOrderFrom(refundParaTwo.getOrderFrom());
                refundPara.setOrderId(refundParaTwo.getOrderId());
                refundPara.setOrderTradeStatus(orderTradeStatus);
                refundPara.setPayCode(refundParaTwo.getPayCode());
                refundPara.setReturnCode(refundParaTwo.getReturnCode());
                refundPara.setPaymentOtherNumber(refundParaTwo.getPaymentOtherNumber());
                refundPara.setReturnCash(pojo.getReturnCash());
                // 调用退款接口
                isRefundSuccess = payMentServiceClient.waitShipmentsRefund(refundPara);
                // 如果退款成功，更新订单和退款状态
                if (isRefundSuccess == false) {
                    logger.info("====退款false 跳过====");
                    continue;
                } else {
                    logger.info("====更新订单状态====");
                    // 更新订单表状态
                    Integer torf = updateOrderStatusMapper.updateBusinessOvertimeOrderBase(pojo);
                    // 更新退款表状态
                    updateOrderStatusMapper.updateBusinessOvertimeOrderReturn(pojo);
                }
            }
        }
        // 记录更新操作的时间
        logger.info("更新超过24小时商家未处理订单  更新时间" + TimeStampUtil.getNowDate());
    }


    /**
     * 查询需要取消的未支付订单。
     * <p>
     * 本方法通过调用updateOrderStatusMapper的queryUnpaidOrdersToBeCancelled方法，
     * 来获取所有处于未支付状态且需要被取消的订单。这通常发生在订单创建后的一段时间内，
     * 如果用户没有完成支付，系统可能会自动取消这些订单，以释放库存或避免订单状态长时间悬挂。
     * </p>
     *
     * @return List<String> 返回一个字符串列表，包含所有需要取消的未支付订单的ID。
     */
    public List<String> queryUnpaidOrdersToBeCancelled() {
        // 调用updateOrderStatusMapper的queryUnpaidOrdersToBeCancelled方法查询需要取消的未支付订单
        return updateOrderStatusMapper.queryUnpaidOrdersToBeCancelled();
    }


    /**
     * 根据订单ID查询订单中的商品详情。
     * <p>
     * 本方法通过调用updateOrderStatusMapper的queryGoodsDetails方法，传入订单ID来查询该订单下所有商品的详细信息。
     * 这里的商品详情是通过AddInventoryDTO对象来表示的，包含了添加库存时所需的商品相关信息。
     * </p>
     *
     * @param orderId 订单ID，是查询商品详情的关键标识。
     * @return 返回一个AddInventoryDTO对象的列表，包含了查询到的商品详细信息。
     */
    public List<AddInventoryDTO> queryGoodsDetails(String orderId) {
        // 调用updateOrderStatusMapper的queryGoodsDetails方法查询商品详情，并返回查询结果
        return updateOrderStatusMapper.queryGoodsDetails(orderId);
    }

    /**
     * 更新商品销售总量和库存信息。
     * 通过调用updateSaleSum2方法，可以批量更新商品的销售总量和库存数量。此方法适用于在处理订单状态变更时，
     * 需要同步更新商品相关信息的场景。使用WapGoodsSaleSumUpdateVO作为参数，可以一次性传递多个商品的更新信息，
     * 提高了数据更新的效率和批量处理的能力。
     *
     * @param wgssu WapGoodsSaleSumUpdateVO对象，包含需要更新的商品销售和库存信息。
     */
    public void updateSaleSum2(WapGoodsSaleSumUpdateVO wgssu) {
        // 调用updateOrderStatusMapper的updateSaleSum2方法，实际执行销售总量和库存数量的更新操作。
        updateOrderStatusMapper.updateSaleSum2(wgssu);
    }

    /**
     * 根据订单ID和相关状态信息更新订单基础信息。
     * 本方法通过调用updateByOrderBase方法，更新订单的状态、取消标识、取消原因和取消日期等信息。
     * 这是对订单信息进行修改的重要操作，用于反映订单的最新状态。
     *
     * @param orderId             订单的唯一标识符，用于定位需要更新的订单。
     * @param orderStatus         更新后的订单状态，表示订单的当前状态，例如待支付、已支付、已发货等。
     * @param orderCancelIdentity 订单的取消标识，用于标记订单是否被取消。
     * @param orderCancelReason   如果订单被取消，此参数表示取消的原因。
     * @param orderCancelDate     订单的取消日期，记录订单取消的具体时间。
     * @return 更新操作的执行结果，true表示更新成功，false表示更新失败。
     */
    public boolean updateByOrderBase(String orderId, Integer orderStatus, Integer orderCancelIdentity, String orderCancelReason, String orderCancelDate) {
        // 调用updateByOrderBase方法，通过传递的参数更新订单基础信息，并返回更新结果。
        return updateOrderStatusMapper.updateByOrderBase(orderId, orderStatus, orderCancelIdentity, orderCancelReason, orderCancelDate);
    }

    /**
     * @param orderId     订单ID，作为更新订单商品状态的依据。
     * @param orderStatus 新的订单商品状态，需要将订单商品的状态更新为该状态。
     * @return 返回boolean值，表示更新操作是否成功。true表示成功更新了订单商品的状态，false表示更新失败。
     */
    public boolean updateByOrderGoods(String orderId, Integer orderStatus) {
        // 调用updateOrderStatusMapper的updateByOrderGoods方法更新订单商品状态
        return updateOrderStatusMapper.updateByOrderGoods(orderId, orderStatus);
    }

    /**
     * 根据状态和订单ID更新订单状态。
     * 此方法用于批量更新订单的状态，基于特定的状态和订单ID列表。
     *
     * @param status  新的订单状态。
     * @param orderId 订单ID，用于指定哪些订单的状态需要被更新。
     */
    public void updateOrderStatus(Integer status, String orderId) {
        // 调用updateOrderStatusMapper的updateOrderStatus方法，更新指定订单的状态
        updateOrderStatusMapper.updateOrderStatus(status, orderId);
    }


    /**
     * 根据订单ID查询支付编号和红包ID。
     * 此方法通过调用updateOrderStatusMapper的queryPaymentNumberAndRedpacketId方法，根据订单ID查询相应的支付编号和红包ID。
     * 这对于处理订单的取消或退款流程非常重要，因为需要这些信息来执行相应的退款或撤销操作。
     *
     * @param orderId 订单ID，是查询支付编号和红包ID的关键标识。
     * @return CancelParaEntityDTO 返回一个包含支付编号和红包ID等信息的数据传输对象。
     */
    public CancelParaEntityDTO queryPaymentNumberAndRedpacketId(String orderId) {
        // 调用updateOrderStatusMapper的queryPaymentNumberAndRedpacketId方法，查询订单的支付编号和红包ID
        return updateOrderStatusMapper.queryPaymentNumberAndRedpacketId(orderId);
    }

    /**
     * 根据订单ID查询商品的销售汇总信息。
     * 本方法通过调用updateOrderStatusMapper的queryGoodsXinXi方法，传入订单ID来查询相应订单下的商品销售汇总信息。
     * 主要用于前端展示订单的商品详情，或者在更新订单状态时获取商品的最新销售情况。
     *
     * @param orderId 订单ID，是查询商品销售信息的关键标识。
     * @return 返回一个WapGoodsSaleSumDTO对象的列表，包含订单中的所有商品的销售汇总信息。
     */
    public List<WapGoodsSaleSumDTO> queryGoodsXinXi(String orderId) {
        // 调用updateOrderStatusMapper的queryGoodsXinXi方法查询订单商品信息并返回
        return updateOrderStatusMapper.queryGoodsXinXi(orderId);
    }

    /**
     * 根据支付编号查询订单状态。
     * 此方法通过支付编号查询订单的状态，支付编号是订单在支付过程中生成的唯一标识。
     * 方法调用时，会查询数据库中与该支付编号对应的订单状态，并返回一个包含状态码的列表。
     * 注意，返回的状态码列表的具体含义依赖于业务逻辑的定义。
     *
     * @param paymentNumber 支付编号，是查询订单状态的关键依据。
     * @return List<Integer> 返回一个整数列表，每个整数代表一个订单的状态码。
     */
    public List<Integer> queryOrderStatus(String paymentNumber) {
        // 调用 updateOrderStatusMapper 的 queryOrderStatus 方法，根据支付编号查询订单状态
        return updateOrderStatusMapper.queryOrderStatus(paymentNumber);
    }

}
