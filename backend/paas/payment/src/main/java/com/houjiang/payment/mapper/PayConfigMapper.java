package com.houjiang.payment.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 支付配置信息表(PayConfig)
 * @author Amos
 */
@Mapper
public interface PayConfigMapper extends BaseMapper<PayConfigEntity> {
    /**
     * 查询支付信息
     *
     * @param paymentMode
     * @return
     */
    Map<String, Object> selectPayConfig(String paymentMode);

    /**
     * 查询支付信息
     *
     * @param paymentMode
     * @return
     */
    PayConfigEntity selectPayConfigEntity(String paymentMode);

    /**
     * 查询品牌店铺 支付信息
     * @param shopId
     * @return
     */
    Map<String,String> selectYfPayConfigShop(Integer shopId);

    /**
     * 获取支付配置ID
     *
     * 此方法用于获取当前环境或实例的支付配置ID支付配置ID是用于确定
     * 特定支付方式的配置信息的关键字，它可以帮助系统识别和应用正确的支付参数
     * 和设置在实现接口的类中，应提供获取支付配置ID的具体逻辑
     *
     * @return 支付配置ID，这是一个唯一的标识符，用于指定支付配置
     */
    int getPayConfigID();


    /**
     * wap端更新支付配置信息
     *
     * @return
     */
    int updatePayConfigWap(Map<String,Object> data);


    /**
     * app端更新支付配置信息
     * @param data
     * @return
     */
    int updatePayConfigAPP(Map<String,Object> data);

    /**
     * 小程序端更新支付配置信息
     * @param data
     * @return
     */
    int updatePayConfigAPPlet(Map<String,Object> data);

    /**
     * 支付宝端更新支付配置信息
     * @param data
     * @return
     */
    int updatePayConfigALi(Map<String,Object> data);

    /**
     * 新增支付宝端配置信息
     * @param
     * @return
     */
    int insertPayInfo();


    /**
     * 更新微信支付信息
     *
     * @param map
     */
    void updateWXPayConfig(Map<String, Object> map);

    /**
     * 更新微信支付信息
     *
     * @param map
     */
    void updateWXPayConfig2(Map<String, Object> map);
    /**
     * 更新微信支付信息
     *
     * @param map
     */
    void updateWXPayConfig3(Map<String, Object> map);

    /**
     * 查询提现配置
     * @return
     */
    List<BigDecimal> queryWithdrawConfig();

    /**
     * 删除用户分销佣金账户
     * @param userId
     */
    void deletePdbFxAccount(@Param("userId") String userId);

    /**
     * 删除用户分销佣金交易记录
     * @param userId
     */
    void deletePdbFxRecord(@Param("userId") String userId);

    /**
     * 删除分销佣金提现第三方交易记录
     * @param userId
     */
    void deletePdbFxTrade(@Param("userId") String userId);


    /**
     * 查询支付配置信息
     * @param paymentMode
     * @return
     */
    Map<String, String> selectYfPayConfig(String paymentMode);
}

