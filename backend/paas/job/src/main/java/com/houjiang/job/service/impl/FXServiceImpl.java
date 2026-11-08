package com.houjiang.job.service.impl;

import com.houjiang.commons.vo.order.CommissionSettleVO;
import com.houjiang.job.mapper.OrderCommissionMapper;
import com.houjiang.job.service.FXService;
import com.houjiang.job.service.PayFxTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @ClassName FXServiceImpl
 * @Description
 * @Author Amos
 * @Date 2023/9/5 13:19
 * @Version 1.0
 * @author Amos
 */
@Service
public class FXServiceImpl implements FXService {

    @Autowired
    private PayFxTaskService payFxTaskService;

    @Autowired
    private OrderCommissionMapper orderCommissionMapper;

    /**
     * 处理微客订单佣金结算。
     * 本方法用于查询所有过退货期且未结算的微客订单，并计算每个订单的佣金总额，
     * 最后调用支付服务进行佣金结算。
     *
     * @see CommissionSettleVO
     */
    @Override
    public void vkOrderCommission() {
        // 查询所有过退货期且未结算的微客佣金订单
        List<CommissionSettleVO> commissionSettleList = orderCommissionMapper.queryOrderSettle();
        // 检查查询结果是否存在
        if (null != commissionSettleList && !commissionSettleList.isEmpty()) {
            // 遍历每个订单，计算佣金总额
            for (CommissionSettleVO commissionSettle : commissionSettleList) {
                CommissionSettleVO vo = orderCommissionMapper.queryWaitSettleAmount(commissionSettle.getOrderId());
                // 如果查询结果为空，设置为0
                if (vo != null) {
                    commissionSettle.setFxCommissionMoney(vo.getFxCommissionMoney() != null ? vo.getFxCommissionMoney().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                    commissionSettle.setOneTgCommissionMoney(vo.getOneTgCommissionMoney() != null ? vo.getOneTgCommissionMoney().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                    commissionSettle.setTwoTgCommissionMoney(vo.getTwoTgCommissionMoney() != null ? vo.getTwoTgCommissionMoney().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                    BigDecimal commissionMoney = commissionSettle.getFxCommissionMoney().add(commissionSettle.getOneTgCommissionMoney())
                            .add(commissionSettle.getTwoTgCommissionMoney()).setScale(2, RoundingMode.HALF_UP);
                    commissionSettle.setCommissionMoney(commissionMoney);
                } else {
                    commissionSettle.setFxCommissionMoney(BigDecimal.ZERO);
                    commissionSettle.setOneTgCommissionMoney(BigDecimal.ZERO);
                    commissionSettle.setTwoTgCommissionMoney(BigDecimal.ZERO);
                    commissionSettle.setCommissionMoney(BigDecimal.ZERO);
                }
            }
        }
        System.out.println("commissionSettleList:   " + commissionSettleList);
        // 调用支付服务进行佣金结算
        payFxTaskService.vkOrderCommission(commissionSettleList, 1);
    }

}
