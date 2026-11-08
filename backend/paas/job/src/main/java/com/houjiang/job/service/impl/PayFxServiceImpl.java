package com.houjiang.job.service.impl;

import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.vo.order.CommissionSettleVO;
import com.houjiang.commons.vo.payment.PayFxRecordVO;
import com.houjiang.job.service.PayFxService;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName PayFxServiceImpl
 * @Description
 * @Author Amos
 * @Date 2023/9/5 13:42
 * @Version 1.0
 * @author Amos
 */
@Service
public class PayFxServiceImpl implements PayFxService {

    /**
     * 根据佣金结算信息和支付外汇账户信息，构建外汇记录VO对象。
     * 该方法用于在处理佣金结算时，生成相应的外汇交易记录，记录用户的佣金变化情况。
     *
     * @param commissionSettle 佣金结算信息，包含结算金额、用户信息和佣金类型等。
     * @return 返回构建完成的外汇记录VO对象，详细记录了佣金结算的相关信息。
     */
    @Override
    public List<PayFxRecordVO> buildFxRecord(CommissionSettleVO commissionSettle) {
        List<PayFxRecordVO> payFxRecordList = new ArrayList<>();
        if(!StringUtils.isEmpty(commissionSettle.getShareUserId())){
            payFxRecordList.add(buildFxRecord(commissionSettle.getShareUserId(), commissionSettle.getShareUserName(), commissionSettle.getFxCommissionMoney()));
        }
        if(!StringUtils.isEmpty(commissionSettle.getTgOneUserId())){
            payFxRecordList.add(buildFxRecord(commissionSettle.getTgOneUserId(), commissionSettle.getTgOneUserName(), commissionSettle.getOneTgCommissionMoney()));
        }
        if(!StringUtils.isEmpty(commissionSettle.getTgTwoUserId())){
            payFxRecordList.add(buildFxRecord(commissionSettle.getTgTwoUserId(), commissionSettle.getTgTwoUserName(), commissionSettle.getTwoTgCommissionMoney()));
        }
        if(!payFxRecordList.isEmpty()){
            Map<String, PayFxRecordVO> aggregatedStudents = payFxRecordList.stream()
                    .collect(Collectors.toMap(
                            PayFxRecordVO::getUserId,
                            payFxRecord -> payFxRecord,
                            (payFxRecord1, payFxRecord2) -> {
                                if (Objects.equals(payFxRecord1.getUserId(), payFxRecord2.getUserId())) {
                                    BigDecimal newScore = payFxRecord1.getRecordAmount().add(payFxRecord2.getRecordAmount()).setScale(2, RoundingMode.HALF_UP);
                                    payFxRecord1.setRecordAmount(newScore);
                                }
                                return payFxRecord1;
                            },
                            // Supplier for the map
                            HashMap::new
                    ));
            payFxRecordList = new ArrayList<>(aggregatedStudents.values());
        }
        return payFxRecordList;
    }

    private PayFxRecordVO buildFxRecord(String userId, String userName, BigDecimal commissionMoney) {
        // 生成唯一的记录ID
        String recordId = "R" + DateUtils.getTimeStamp() + RandomStringUtils.randomNumeric(8);

        // 初始化外汇记录VO对象
        PayFxRecordVO payFxRecord = new PayFxRecordVO();
        payFxRecord.setRecordId(recordId);
        payFxRecord.setUserId(userId);
        payFxRecord.setUserName(userName);
        payFxRecord.setRecordAmount(commissionMoney);

        // 设置记录的状态、交易类型、记录类型等信息
        payFxRecord.setRecordStatus(2);
        payFxRecord.setTradeTypeId(1);
        payFxRecord.setRecordType(1);
        payFxRecord.setPlusOrMinus(1);
        payFxRecord.setRecordName("结算佣金");
        payFxRecord.setRecordStatusName("结算成功");
        payFxRecord.setRecordDate(DateUtils.getNowDay());
        payFxRecord.setCreateTime(DateUtils.getNowDateToString());
        payFxRecord.setUpdateTime(DateUtils.getNowDateToString());

        // 返回构建完成的外汇记录VO对象
        return payFxRecord;
    }

}
