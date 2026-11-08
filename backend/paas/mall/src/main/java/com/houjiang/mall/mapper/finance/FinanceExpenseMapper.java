package com.houjiang.mall.mapper.finance;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.finance.FinanceExpense;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支出记录Mapper
 *
 * @author Amos
 */
@Mapper
public interface FinanceExpenseMapper extends BaseMapper<FinanceExpense> {
}
