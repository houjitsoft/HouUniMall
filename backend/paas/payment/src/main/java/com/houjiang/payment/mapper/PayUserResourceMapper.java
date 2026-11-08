package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.vo.payment.CardMemberservicesVO;
import com.houjiang.payment.entity.PayConsumeWithdrawEntity;
import com.houjiang.payment.entity.PayUserResourceEntity;
import com.houjiang.payment.vo.AccountBalanceDisplayVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.payment.entity.PayUserResource
 * @author Amos
 */
@Mapper
public interface PayUserResourceMapper extends BaseMapper<PayUserResourceEntity> {

    /**
     * 查询账户余额
     *
     * @param userId
     * @return
     */
    AccountBalanceDisplayVO queryAccountBalance(@Param("userId") String userId);

    /**
     * 更新用户资源
     *
     * @param userId
     * @param money
     * @return
     */
    boolean updateUserResource(@Param("userId") String userId, @Param("money") String money);


    /**
     * 更新金额
     *
     * @param toUserId
     * @param money
     * @return
     */
    boolean updateToMoney(@Param("toUserId") String toUserId, @Param("money") String money);

    /**
     * 查询账户余额
     * @param userId
     * @return
     */
    BigDecimal queryBalanceMoney(@Param("userId")String userId);

    /**
     * 扣除购物金额
     * @param userId
     * @param amountActuallyPaids
     * @return
     */
    boolean updateMoney(@Param("userId") String userId, @Param("amountActuallyPaids") BigDecimal amountActuallyPaids);

    /**
     * @return 处理结果
     */
    int deductBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);

    /**
     * @return 处理结果
     */
    int creditBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);

    /**
     * 查询账户购物卡余额
     * @param userId
     * @return
     */
    BigDecimal queryGiftCardMoney(@Param("userId")String userId);

    /**
     * 扣除购物卡金额
     * @param userId
     * @param amountActuallyPaids
     * @return
     */
    boolean updateGiftCardMoney(@Param("userId") String userId, @Param("totalPrice") BigDecimal amountActuallyPaids);



    /**
     * 查询卡成员服务端口
     *
     * @param page
     * @param map
     * @return
     */
    IPage<CardMemberservicesVO> query(IPage<CardMemberservicesVO> page, @Param("param") Map<String, Object> map);


    /**
     * 更新扣除用户金额
     *
     * @param payUserId
     * @param money
     * @return
     */
    boolean updateDeductUserMoney(String payUserId, BigDecimal money);

    /**
     * 查询用户余额
     * @param userId
     * @return
     */
    String queryAccountBalanceFeign(@Param("userId") String userId);



    /**
     * 更新骑手余额
     * @param entity
     */
    void updateRiderMoney(@Param("param") PayConsumeWithdrawEntity entity);

    /**
     * 判断是否是品牌店铺的订单
     * @param outTradeNo
     * @return
     */
    Integer queryshopIdByOutTradeNo(@Param("outTradeNo") String outTradeNo);


    /**
     * 更新卡成员服务端口
     * @param manageMoney
     */
    void updateCardMemberDetailPortFeign(Map<String, Object> manageMoney);


    /**
     * 更新卡成员服务端口
     * @param manageMoney
     * @return
     */
    Integer updateCardMemberDetailPort2(Map<String, Object> manageMoney);
}




