package com.houjiang.job.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName AccountMapper
 * @Description 账号
 * @Author Amos
 * @Date 2023/5/17 10:05
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface AccountMapper {

    /**
     * 获取过期获取帐户
     *
     * @return
     */
    List<String> getAccountByOverdue();

    /**
     * 更新账号状态
     *
     * @param accountingPeriodId
     */
    void updateAccountStatus(@Param("accountingPeriodId") String accountingPeriodId);

    /**
     * 更新帐户Sue
     *
     * @param monthlyTime
     */
    void updateAccountIsSue(@Param("monthlyTime") String monthlyTime);

    /**
     * 更新账户月收款
     */
    void clearAccountMonthCollection();

    /**
     * 更新已使用的帐户月数
     */
    void clearAccountMonthAmountUsed();

    /**
     * 更新账户还款
     *
     * @param monthlyTime
     */
    void updateAccountIsRepayment(@Param("monthlyTime") String monthlyTime);
}
