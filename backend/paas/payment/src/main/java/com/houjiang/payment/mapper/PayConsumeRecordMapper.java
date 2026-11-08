package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.dto.order.RefundParamDTO;
import com.houjiang.commons.dto.payment.AccountPeriodParamDTO;
import com.houjiang.commons.dto.payment.GroupBookingParamDTO;
import com.houjiang.commons.dto.payment.GroupBookingTradeDTO;
import com.houjiang.commons.dto.payment.RefundAccountParamDTO;
import com.houjiang.commons.dto.shop.ShopSalesStatisticsDTO;
import com.houjiang.commons.vo.order.OrderBaseVO;
import com.houjiang.commons.vo.order.OrderSettlementVO;
import com.houjiang.commons.vo.payment.AccountShopBillDetailsVO;
import com.houjiang.commons.vo.shop.AccountPeriodVO;
import com.houjiang.payment.dto.FinancialDetailsDTO;
import com.houjiang.payment.dto.PayConsumeRecordDTO;
import com.houjiang.payment.entity.PayConsumeRecordEntity;
import com.houjiang.payment.vo.ComsumeRecordParaVO;
import com.houjiang.payment.vo.ComsumeRecordVO;
import com.houjiang.payment.vo.QueryGoodsVO;
import com.houjiang.payment.vo.RecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @ClassName PayConsumeRecordMapper
 * @Description 交易明细表-账户收支明细-资金流水表-账户金额变化流水
 * @Author Amos
 * @Date 2023-05-31 15:36:55
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface PayConsumeRecordMapper extends BaseMapper<PayConsumeRecordEntity> {


    /**
     * 向商家插入消费记录。
     *
     * @param paymentNumber    交易编号
     * @param sellerId         商家ID
     * @param userName         用户姓名
     * @param paymentAmout     交易金额
     * @param tradeDate        交易日期
     * @param tradeYear        交易年份
     * @param tradeMonth       交易月份
     * @param tradeDay         交易日
     * @param tradeTitle       交易标题
     * @param tradeDesc        交易描述
     * @param recordTime       记录时间
     * @param tradeTypeId      交易类型ID
     * @param userType         用户类型
     * @param recordStatus     记录状态
     * @param recordPayorderId 订单支付ID
     * @param isCurrentUser    当前用户标识
     * @return 插入操作是否成功
     */
    boolean insertConsumeRecordToSeller(@Param("paymentNumber") String paymentNumber, @Param("sellerId") String sellerId, @Param("userName") String userName, @Param("paymentAmout") BigDecimal paymentAmout, @Param("customerType") Integer customerType, @Param("tradeDate") String tradeDate, @Param("tradeYear") String tradeYear, @Param("tradeMonth") String tradeMonth, @Param("tradeDay") String tradeDay, @Param("tradeTitle") String tradeTitle, @Param("tradeDesc") String tradeDesc, @Param("recordTime") String recordTime, @Param("tradeTypeId") Integer tradeTypeId, @Param("userType") Integer userType, @Param("recordStatus") Integer recordStatus, @Param("recordPayorderId") String recordPayorderId, @Param("isCurrentUser") Integer isCurrentUser);

    /**
     * 向购买用户插入消费记录。
     *
     * @param paymentNumber    交易编号
     * @param userId           用户ID
     * @param userName         用户姓名
     * @param paymentAmout     交易金额
     * @param tradeDate        交易日期
     * @param tradeYear        交易年份
     * @param tradeMonth       交易月份
     * @param tradeDay         交易日
     * @param tradeTitle       交易标题
     * @param tradeDesc        交易描述
     * @param recordTime       记录时间
     * @param tradeTypeId      交易类型ID
     * @param userType         用户类型
     * @param recordStatus     记录状态
     * @param recordPayorderId 订单支付ID
     * @param isCurrentUser    当前用户标识
     * @return 插入操作是否成功
     */
    boolean insertBuyUserConsumeRecord(@Param("paymentNumber") String paymentNumber, @Param("userId") String userId, @Param("userName") String userName, @Param("paymentAmout") BigDecimal paymentAmout, @Param("customerType") Integer customerType, @Param("tradeDate") String tradeDate, @Param("tradeYear") String tradeYear, @Param("tradeMonth") String tradeMonth, @Param("tradeDay") String tradeDay, @Param("tradeTitle") String tradeTitle, @Param("tradeDesc") String tradeDesc, @Param("recordTime") String recordTime, @Param("tradeTypeId") Integer tradeTypeId, @Param("userType") Integer userType, @Param("recordStatus") Integer recordStatus, @Param("recordPayorderId") String recordPayorderId, @Param("isCurrentUser") Integer isCurrentUser);

    /**
     * 新增信息
     *
     * @param map
     * @return
     */
    Integer insertRecord(Map<String, String> map);

    /**
     * 查询用户最近的交易记录。
     *
     * @param userId      用户标识，用于指定查询的用户。
     * @param tradeTypeId 交易类型标识，用于过滤交易类型。
     * @return 返回符合条件的交易记录列表。
     */
    List<ComsumeRecordParaVO> queryRecentDealRecord(@Param("userId") String userId,
                                                    @Param("tradeTypeId") Integer tradeTypeId);

    /**
     * 删除交易记录
     *
     * @param userId
     * @param recordId
     */
    void updateRecordDelete(@Param("userId") String userId, @Param("recordId") Integer recordId);

    /**
     * 新增消费信息
     *
     * @param mapRecord
     * @return
     */
    boolean insertConsumeRecord(Map<String, Object> mapRecord);


    /**
     * 更新
     *
     * @param map
     * @return
     */
    Integer updateRecordRefuse(Map<String, String> map);


    /**
     * 查询订单金额及订单名称
     *
     * @param orderId
     * @return
     */
    QueryGoodsVO queryGoodsPriceAndName(@Param("orderId") String orderId, @Param("userId") String userId);

    /**
     * 修改record状态(纯数字订单)
     */
    boolean updateRecordByInt(Map<String, Object> mapRecord);

    /**
     * 修改商家record状态(纯数字订单)
     */
    boolean updateRecordBySellerInt(@Param("orderId") String orderId, @Param("paymentTime") String paymentTime);

    /**
     * 修改record状态(非纯数字订单)
     */
    boolean updateRecord(Map<String, Object> mapRecord);

    /**
     * record表卖家更新(非纯数字订单)
     */
    boolean updateRecordBySeller(@Param("orderId") String orderId, @Param("paymentTime") String paymentTime);

    /**
     * 根据商户订单号查询
     *
     * @param orderId
     * @return
     */
    List<RecordVO> queryRecordParaList(@Param("orderId") String orderId);

    /**
     * 更新消费信息
     *
     * @param mapPureDigital
     * @return
     */
    boolean updateComsumeRecordByPureDigital(Map<String, Object> mapPureDigital);

    /**
     * 查询状态
     *
     * @param outTradeNo
     * @return
     */
    List<Integer> queryRecodeStatus(@Param("outTradeNo") String outTradeNo);

    /**
     * 查询订单
     *
     * @param outTradeNo
     * @return
     */
    List<ComsumeRecordVO> queryorderNo(@Param("orderId") String outTradeNo);

    /**
     * 更新消费
     *
     * @param mapPureDigital
     * @return
     */
    boolean updateComsumeRecord(Map<String, Object> mapPureDigital);

    /**
     * 查询信息
     *
     * @param orderId
     * @return
     */
    RecordVO queryRecordPara(@Param("orderId") String orderId);

    /**
     * 查询商品订单号与订单金额
     *
     * @param outTradeNo
     * @return
     */
    List<PayConsumeRecordDTO> queryOrderNo(String outTradeNo);

    /**
     * 查询订单状态
     *
     * @param outTradeNo
     * @return
     */
    List<Integer> queryAllRecordStatus(@Param("orderId") String outTradeNo);

    /**
     * 查询实际支付单号
     *
     * @param orderId
     * @return
     */
    List<String> queryPayOrderId(@Param("orderId") String orderId);

    /**
     * 查询状态
     *
     * @param orderId
     * @param userId
     * @return
     */
    List<Integer> queryStatus(@Param("orderId") String orderId, @Param("userId") String userId);


    /**
     * 分页查询
     *
     * @param page
     * @param param
     * @return
     */
    IPage<ShopSalesStatisticsDTO> querySalesStatistics(IPage<ShopSalesStatisticsDTO> page, @Param("param") ShopSalesStatisticsDTO param);


    /**
     * 分页查询详情参数
     *
     * @param page
     * @param param
     * @return
     */
    IPage<PayConsumeRecordEntity> platformQueryDetails(IPage<PayConsumeRecordEntity> page, @Param("param") FinancialDetailsDTO param);


    /**
     * 新增消费端口
     *
     * @param comRecord
     */
    void insertConsumeRecordport(Map<String, Object> comRecord);


    /**
     * 更新订单记录结算
     *
     * @param list
     * @return
     */
    boolean updateOrderRecordSettlements(@Param("list") List<String> list);

    /**
     * 新增信息
     *
     * @param mapRecord
     * @return
     */
    boolean insertSettlementRecord(Map<String, Object> mapRecord);

    /**
     * 根据支付方式查询支付配置信息。
     *
     * @param paymentMode 支付方式代码
     * @return 支付配置信息的映射表
     */
    Map selectYfPayConfig(String paymentMode);

    /**
     * 查询指定订单的退款消费记录金额。
     *
     * @param orderId 订单号
     * @return 退款消费记录的列表
     */
    List<GroupBookingParamDTO> queryRefundConsumeRecordMoney(@Param("orderId") String orderId);

    /**
     * 计算所有订单的金额。
     *
     * @param uOrderId 用户订单号
     * @return 所有订单的总金额
     */
    BigDecimal calculateAllOrderMoney(@Param("uOrderId") String uOrderId);

    /**
     * 查询指定订单的所有交易支付充值卡金额。
     *
     * @param uOrderId 用户订单号
     * @return 所有交易支付充值卡的总金额
     */
    BigDecimal queryAllTradePaymentRechargeCard(@Param("uOrderId") String uOrderId);

    /**
     * 查询指定订单的退款交易信息。
     *
     * @param orderId 订单号
     * @return 退款交易的信息
     */
    GroupBookingTradeDTO queryRefundTrade(@Param("orderId") String orderId);

    /**
     * 更新用户资金资源。
     *
     * @param userId 用户ID
     * @param money  资金金额
     * @return 更新操作是否成功
     */
    boolean updateUserMoneyResource(@Param("userId") String userId, @Param("money") BigDecimal money);

    /**
     * 更新用户充值卡资金资源。
     *
     * @param userId 用户ID
     * @param money  资金金额
     * @return 更新操作是否成功
     */
    boolean updateUserRechargeCardMoneyResource(@Param("userId") String userId, @Param("money") BigDecimal money);


    /**
     * 更新订单退款信息。
     *
     * @param refundOrderId 退款订单号
     * @return 更新操作是否成功
     */
    boolean updateOrderRefund(@Param("refundOrderId") String refundOrderId);

    /**
     * 更新订单交易信息。
     *
     * @param refundOrderId 退款订单号
     * @return 更新操作是否成功
     */
    boolean updateOrderTrade(@Param("refundOrderId") String refundOrderId);


    /**
     * 分页查询店铺类型
     *
     * @param page
     * @param userId
     * @param settleType
     * @return
     */
    IPage<OrderSettlementVO> fkPayForBusinessWithdrawalByShopType(Page<OrderSettlementVO> page, String userId, Integer settleType);

    /**
     * 分页查询店铺
     *
     * @param page
     * @param userId
     * @return
     */
    IPage<OrderSettlementVO> fkPayForBusinessWithdrawal(Page<OrderSettlementVO> page, String userId);

    /**
     * 根据用户ID获取用户的总金额
     * 此方法用于计算特定用户的所有记录的总金额
     *
     * @param userId 用户的唯一标识符
     * @return 用户的所有记录的总金额如果用户没有记录，则返回零
     */
    BigDecimal getSumRecordMoney(String userId);

    /**
     * 查询记录金额
     *
     * @param list
     * @param userId
     * @return
     */
    BigDecimal queryCalculateRecordMoney(@Param("list") List<String> list, @Param("userId") String userId);

    /**
     * 查询记录佣金金额
     *
     * @param list
     * @param userId
     * @return
     */
    BigDecimal queryCalculateRecordCommission(@Param("list") List<String> list, @Param("userId") String userId);

    /**
     * 更新
     *
     * @param list
     * @return
     */
    boolean updateRecordWithdraw(List<String> list);

    /**
     * 查询用户所有的声明编号
     *
     * @param userId 用户ID，用于识别特定用户
     * @return 包含用户所有声明编号的列表
     */
    List<String> queryAllStatementNo(String userId);

    /**
     * 插入支付消费记录
     * <p>
     * 此方法用于将成功的支付消费记录插入数据库或其他持久化存储中
     *
     * @param mapRecordSuccess 包含支付消费记录详情的映射表，其中键为字段名，值为对应的字段值
     * @return 插入操作的成功与否，true表示成功插入，false表示插入失败
     */
    boolean insertPayConsumeRecord(Map<String, Object> mapRecordSuccess);


    /**
     * 插入消费记录
     *
     * @param comRecord
     */
    void insertConsumeRecordportFeign(Map<String, Object> comRecord);
}
