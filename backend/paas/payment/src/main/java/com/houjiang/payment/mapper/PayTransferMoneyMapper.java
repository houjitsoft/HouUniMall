package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayTransferMoneyEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * @Entity com.houjiang.payment.entity.PayTransferMoney
 * @author Amos
 */
@Mapper
public interface PayTransferMoneyMapper extends BaseMapper<PayTransferMoneyEntity> {

    /**
     * 新增传输
     *
     * @param mapTransfer
     * @return
     */
    boolean insertTransfer(Map<String, Object> mapTransfer);
}




