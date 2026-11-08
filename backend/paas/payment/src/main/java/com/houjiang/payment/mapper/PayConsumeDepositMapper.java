package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayConsumeDepositEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * @Entity com.houjiang.payment.entity.PayConsumeDeposit
 * @author Amos
 */
@Mapper
public interface PayConsumeDepositMapper extends BaseMapper<PayConsumeDepositEntity> {

    /**
     * 新增消费信息
     *
     * @param mapDeposit
     * @return
     */
    boolean insertByConsumeDeposit2(Map<String, Object> mapDeposit);



    /**
     * 新增支付信息
     *
     * @param comRecord
     */
    void insertConsumeDepositport(Map<String, Object> comRecord);
}




