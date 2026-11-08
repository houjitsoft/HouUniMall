package com.houjiang.job.service.impl;


import com.alibaba.fastjson.JSON;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.job.AcquiescenceVO;
import com.houjiang.job.feign.OrderServiceClient;
import com.houjiang.job.feign.UserServiceClient;
import com.houjiang.job.mapper.JobOrderBaseMapper;
import com.houjiang.job.mapper.UpdateRecordStatusMapper;
import com.houjiang.job.service.UpdateRecordStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Amos
 */
@Service
public class UpdateRecordStatusServiceImpl implements UpdateRecordStatusService {


	@Autowired
	private OrderServiceClient orderServiceClient;

	@Autowired
	private UserServiceClient userServiceClient;

	@Autowired
	private JobOrderBaseMapper jobOrderBaseMapper;

	@Autowired
	private UpdateRecordStatusMapper updateReceiverdRecordMapper;

	/**
	 * 更新接收记录
	 * @param paymentNumber 付款编号
	 * @return 更新结果，返回受影响的行数
	 */
	@Override
	public Integer updateReceiverdRecord(String paymentNumber) {
	    return updateReceiverdRecordMapper.updateReceiverdRecord(paymentNumber);
	}

	/**
	 * 更新超时记录
	 * @param orderId 订单编号
	 * @return 更新结果，返回受影响的行数
	 */
	@Override
	public Integer updateTimeoutRecord(String orderId) {
	    return updateReceiverdRecordMapper.updateTimeoutRecord(orderId);
	}

	/**
	 * 更新超时交易记录
	 * @param orderId 订单编号
	 * @return 更新结果，返回受影响的行数
	 */
	@Override
	public Integer updateTimeoutTrade(String orderId) {
	    return updateReceiverdRecordMapper.updateTimeoutTrade(orderId);
	}

	/**
	 * 更新接收交易记录
	 * @param orderId 订单编号
	 * @return 更新结果，返回受影响的行数
	 */
	@Override
	public Integer updateReceiverdTrade(String orderId) {
	    return updateReceiverdRecordMapper.updateReceiverdTrade(orderId);
	}

	/**
	 * 查询未评价的订单
	 * @return 未评价订单的列表
	 */
	@Override
	public List<AcquiescenceVO> queryNotEvaluatedOrder() {
	    ResponseResult result = orderServiceClient.queryNotEvaluatedOrder();
	    if (result == null || result.getData() == null) {
	        return java.util.Collections.emptyList();
	    }
	    List<AcquiescenceVO> voList = JSON.parseArray(JSON.toJSONString(result.getData()), AcquiescenceVO.class);
	    return voList == null ? java.util.Collections.emptyList() : voList;
	}

	/**
	 * 根据用户ID查询用户信息
	 * @param userId 用户ID
	 * @return 用户信息
	 */
	@Override
	public AcquiescenceVO queryUserInfo(String userId) {
	    return userServiceClient.queryUserInfoByjob(userId);
	}

	/**
	 * 根据订单ID查询商品信息
	 * @param orderId 订单ID
	 * @return 商品信息列表
	 */
	@Override
	public List<AcquiescenceVO> queryGoodsInfo(String orderId) {
	    return orderServiceClient.queryGoodsInfoByjob(orderId);
	}

	/**
	 * 插入评价信息
	 * @param param 评价信息VO
	 * @return 操作结果，返回影响的行数
	 */
	@Override
	public Integer insertEvaluated(AcquiescenceVO param) {
	    return orderServiceClient.insertEvaluatedByJob(param).getCode();
	}

	/**
	 * 更新订单评价状态
	 * @param orderId 订单编号
	 * @return 更新结果，返回受影响的行数
	 */
	@Override
	public Integer updateOrderEvaluated(String orderId) {
	    return jobOrderBaseMapper.updateOrderEvaluated(orderId);
	}

	/**
	 * 查询收获时间
	 * @param orderId 订单编号
	 * @return 收货时间
	 */
	@Override
	public String queryShouHuoshijian(String orderId) {
	    return jobOrderBaseMapper.queryorderFinishedTime(orderId);
	}
}
