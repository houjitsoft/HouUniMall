package com.houjiang.job.service;



import com.houjiang.commons.vo.job.AcquiescenceVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Amos
 */
@Service
public interface UpdateRecordStatusService {


	/**
	 * 更新所有超时订单相关的记录
	 * @param paymentNumber
	 * @return
	 */
	 Integer updateReceiverdRecord(String paymentNumber);

	/**
	 * 根据paymentOrder更新所有超时订单记录
	 * @param orderId
	 * @return
	 */
	 Integer updateTimeoutRecord(String orderId);

	/**
	 * 更新所有超时未支付订单Trade
	 * @param orderId
	 * @return
	 */
	 Integer updateTimeoutTrade(String orderId);

	/**
	 * 更新所有超时未确认收货订单相关Trade
	 * @param orderId
	 * @return
	 */
	 Integer updateReceiverdTrade(String orderId);


	/**
	 * 查订单数据
	 *
	 *
	 * @return
	 */
	 List<AcquiescenceVO> queryNotEvaluatedOrder();

	/**
	 * 根据userId查用户信息
	 *
	 *
	 * @return
	 */
	 AcquiescenceVO queryUserInfo(String userId);

	/**
	 * 根据订单id商品信息
	 *
	 * @param orderId
	 * @return
	 */
	 List<AcquiescenceVO> queryGoodsInfo(String orderId);

	/**
	 * 确认收货超过14天默认默认好评
	 *
	 * @param param
	 * @return
	 */
	 Integer insertEvaluated(AcquiescenceVO param);

	/**
	 * 订单完成时间
	 * @param orderId
	 * @return
	 */
	 Integer updateOrderEvaluated(String orderId);

	/**
	 * 收货时间
	 * @param orderId
	 * @return
	 */
	 String  queryShouHuoshijian(String orderId);



}
