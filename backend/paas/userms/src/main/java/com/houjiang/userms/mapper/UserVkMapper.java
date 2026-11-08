package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.userms.dto.VkAddUserDTO;
import com.houjiang.userms.dto.VkGiftOrderDTO;
import com.houjiang.userms.entity.UserVkEntity;
import com.houjiang.userms.vo.VkCustomerInfoVO;
import com.houjiang.userms.vo.VkTgUserListVO;
import com.houjiang.userms.vo.VkUserListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.houjiang.commons.vo.user.UserVkVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @ClassName UserVkMapper
 * @Description 店铺微客会员表
 * @Author Amos
 * @Date 2023-06-05 11:00:51
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface UserVkMapper extends BaseMapper<UserVkEntity> {

    /**
     * 添加微客推广用户数量
     * @param userIdList 微客用户id列表
     */
    void addVkTgNumByList(@Param("userIdList") List<String> userIdList);

    /**
     * 减少微客推广用户数量
     * @param userIdList 微客用户id列表
     */
    void subVkTgNumByList(@Param("userIdList") List<String> userIdList);

    /**
     * 根据参数查询VK用户列表，并包含用户的结算订单数量、一级和二级佣金总额以及商品数量。
     *
     * @param param 查询参数，包含分页信息和过滤条件。
     * @return 返回查询结果，包含分页信息和用户列表。
     */
    IPage<VkUserListVO> selectVkUserList(Page<VkUserListVO> page, @Param("param") VkAddUserDTO param);

    /**
     * 查询礼物订单数量
     */
    Integer selectGiftOrderCount(@Param("userId") String userId);

    /**
     * 查询订单用户信息
     */
    VkGiftOrderDTO queryUserInfoByOrderId(@Param("orderId") String orderId);

    /**
     * 将固定奖励添加指定用户的余额
     * @param userId 用户id
     * @param fixedRewards 固定奖励
     */
    void addBalance(@Param("userId") String userId, @Param("fixedRewards") BigDecimal fixedRewards);

    /**
     * 根据指定的参数分页查询Vk平台的用户信息
     * 此方法主要负责调用底层数据访问层的方法来获取Vk用户的数据，并对每个用户的相关数据进行补充查询
     *
     * @param param 查询参数对象，包含页码和每页数量等分页信息
     * @return 包含查询结果的响应对象
     */
    IPage<VkTgUserListVO> selectVkTotalTgUser(Page<VkTgUserListVO> page, @Param("param") VkAddUserDTO param);

    /**
     * 查询分销订单数量
     */
    Integer selectFxOrderNum(@Param("userId") String userId);

    /**
     * 查询分销订单金额
     */
    BigDecimal selectFxOrderAmount(@Param("userId") String userId);

    /**
     * 添加支付资产明细记录
     */
    void addPayAssetDetails(@Param("detailType") int detailType, @Param("assetType") int assetType, @Param("assetAmount") BigDecimal assetAmount,
                            @Param("assetDirection") String assetDirection, @Param("orderId") String orderId, @Param("userId") String userId,
                            @Param("payType") Integer payType, @Param("detailComment") String detailComment);

    /**
     * 查询今日结算金额
     * @param userId 用户id
     * @return 今日结算金额
     */
    BigDecimal selectTodaySettlement(@Param("userId") String userId);

    /**
     * 查询今日待结算金额
     * @param userId 用户id
     * @return 今日待结算金额
     */
    BigDecimal selectTodayEstimatedIncome(@Param("userId") String userId);

    /**
     * 查询累计预估金额
     * @param userId 用户id
     * @return 今日待结算金额
     */
    BigDecimal selectTotalEstimatedIncome(@Param("userId") String userId);

    /**
     * 根据用户token查询相应的客户信息。
     *
     * @param param 包含用户token和其他查询参数的DTO。
     * @return 返回查询结果，如果用户不存在，则返回失败响应。
     */
    IPage<VkCustomerInfoVO> selectVkCustomerInfo(Page<VkCustomerInfoVO> page, @Param("param") VkAddUserDTO param);

    /**
     * 根据客户ID和当前用户ID查询客户的订单信息
     * @param userId 用户id
     * @param inviterUserId 邀请人id
     * @return 订单信息
     */
    VkCustomerInfoVO selectVkOrderInfo(@Param("userId") String userId, @Param("inviterUserId") String inviterUserId);

    /**
     * 查询用户冻结佣金
     * @param userId 用户id
     * @return 冻结佣金
     */
    BigDecimal selectVkFreezeCommission(@Param("userId") String userId);

    /**
     * 查询用户分销订单信息
     */
    VkUserListVO selectVkUserOrder(@Param("userId") String userId);

    /**
     * 查询用户分销订单商品数量
     */
    Integer selectVkOrderGoodsCount(@Param("userId") String userId);

    /**
     * 查询账号信息
     * @param userId
     * @return
     */
    UserVkVO queryUserAccount(@Param("userId") String userId);


    /**
     * 更新用户分销佣金账户表
     * @param paraMap
     * @return
     */
    int subUserAccount(@Param("param") Map<String, String> paraMap);

}
