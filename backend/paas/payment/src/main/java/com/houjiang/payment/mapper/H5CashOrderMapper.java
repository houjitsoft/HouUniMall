package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.H5CashOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * 通企付 H5 收银台订单 Mapper
 *
 * @author Amos
 */
@Mapper
public interface H5CashOrderMapper extends BaseMapper<H5CashOrderEntity> {

    /**
     * 根据 商户号 + 商户订单号 查询订单
     *
     * @param mchNo      商户号
     * @param mchOrderNo 商户订单号
     * @return 订单实体（可能为空）
     */
    H5CashOrderEntity selectByMchOrderNo(@Param("mchNo") String mchNo,
                                         @Param("mchOrderNo") String mchOrderNo);

    /**
     * 根据通企付订单号查询订单
     *
     * @param payOrderId 通企付订单号
     * @return 订单实体（可能为空）
     */
    H5CashOrderEntity selectByPayOrderId(@Param("payOrderId") String payOrderId);

    /**
     * 更新订单状态（轻量级 set，避免触发 update_time 之外的列）
     *
     * @param id              主键
     * @param state           目标状态：0/1/2/3/4/5
     * @param payOrderId      通企付订单号（可空）
     * @param h5Url           H5 Url（可空）
     * @param paySuccessTime  支付成功时间（仅在 state=2 时有效，可空）
     * @return 影响行数
     */
    int updateState(@Param("id") Long id,
                    @Param("state") Integer state,
                    @Param("payOrderId") String payOrderId,
                    @Param("h5Url") String h5Url,
                    @Param("paySuccessTime") Date paySuccessTime);
}
