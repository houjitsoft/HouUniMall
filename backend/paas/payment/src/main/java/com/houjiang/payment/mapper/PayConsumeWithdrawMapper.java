package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.vo.order.OrderSettlementVO;
import com.houjiang.payment.dto.ConsumeWithdrawDTO;
import com.houjiang.payment.entity.PayConsumeWithdrawEntity;
import com.houjiang.payment.vo.WithdrawRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.payment.entity.PayConsumeWithdraw
 * @author Amos
 */
@Mapper
public interface PayConsumeWithdrawMapper extends BaseMapper<PayConsumeWithdrawEntity> {

    /**
     * 用户提现记录
     *
     * @param mapWithd
     * @return
     */
    boolean insertConsumeWithdraw(Map<String, Object> mapWithd);

    /**
     * 分页查询结算订单
     * @param page
     * @param userId
     * @param settleType
     * @return
     */
    IPage<OrderSettlementVO> querySettlementOrder(Page<OrderSettlementVO> page,@Param("userId") String userId,@Param("settleType") Integer settleType);

    /**
     * 通过用户id获取总金额
     * @param userId
     * @return
     */
    BigDecimal getSumRecordMoney(@Param("userId") String userId,@Param("settleType") Integer settleType);

    /**
     * 获取所有金额
     * @param userId
     * @return
     */
    BigDecimal queryAllMoney(@Param("userId") String userId);

    /**
     * 获取金额
     * @param recordIdList
     * @param userId
     * @return
     */
    BigDecimal queryRecordMoney(@Param("recordIdList") List<Integer> recordIdList,@Param("userId") String userId);

    /**
     * 获取计算金额
     * @param recordIdList
     * @param userId
     * @return
     */
    BigDecimal queryCalculateRecordMoney(@Param("recordIdList") List<String> recordIdList,@Param("userId") String userId);

    /**
     * 更新记录撤消
     * @param recordIdList
     * @return
     */
    boolean updateRecordWithdraw(@Param("recordIdList") List<String> recordIdList);

    /**
     *查询所有声明编号
/**
     * @param userId
     * @return
     */
    List<String> queryAllStatementNo(@Param("userId") String userId);


    /**
     * 新增卖家提取消费
     * @param paraMap
     * @return
     */
    boolean insertConsumeWithdrawBySeller(@Param("param") Map<String,Object> paraMap);

    /**
     * 新增记录
     * @param paraMap
     * @return
     */
    boolean insertRecord(@Param("param") Map<String,Object> paraMap);

    /**
     * 新增详情
     * @param paraMap
     * @return
     */
    boolean insertDetail(@Param("param") Map<String,Object> paraMap);


    /**
     * 分页查询提款记录
     * @param page
     * @param paraMap
     * @return
     */
    IPage<WithdrawRecordVO> queryWithdrawRecord(Page<WithdrawRecordVO> page,@Param("paraMap") Map<String,Object> paraMap);


    /**
     * 查询撤销记录列表
     *
     * @param consumeWithdrawDto
     * @return
     */
    List<WithdrawRecordVO> queryWithdrawRecordListExcel(@Param("param") ConsumeWithdrawDTO consumeWithdrawDto);


    /**
     * 更新提款审核
     *
     * @param mapAudit
     * @return
     */
    boolean updateWithdrawAudit(Map<String, Object> mapAudit);


    /**
     * 分页查询撤销记录列表
     *
     * @param page
     * @param consumeWithdrawDto
     * @return
     */
    IPage<WithdrawRecordVO> queryWithdrawRecordList(IPage<WithdrawRecordVO> page, @Param("param") ConsumeWithdrawDTO consumeWithdrawDto);

    /**
     * 更新record表提现状态
     * @param settlementsIds
     * @return
     */
    boolean updateOrderRecordWithdraw(@Param("settlementsIds")String[] settlementsIds);


}




