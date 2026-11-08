package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.order.dto.PaymentVocherDTO;
import com.houjiang.order.entity.OrderPaymentVocherEntity;
import com.houjiang.order.vo.OdbPaymentVocherVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity com.houjiang.order.entity.OrderPaymentVocher
 * @author Amos
 */
@Mapper
public interface OrderPaymentVocherMapper extends BaseMapper<OrderPaymentVocherEntity> {

    /**
     * 更新付款
     *
     * @param param
     * @return
     */
    Integer updatePaymentVocher(@Param("param") PaymentVocherDTO param);

    /**
     * 新增付款
     *
     * @param param
     * @return
     */
    Integer insertPaymentVocher(@Param("param") PaymentVocherDTO param);

    /**
     * 查询线下支付的审核状态
     * @param orderId
     * @return
     */
    OdbPaymentVocherVO queryPaymentVocher(@Param("orderId")String orderId);
}




