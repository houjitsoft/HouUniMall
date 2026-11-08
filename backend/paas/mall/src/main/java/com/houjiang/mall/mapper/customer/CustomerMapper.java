package com.houjiang.mall.mapper.customer;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.customer.Customer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户信息Mapper
 *
 * @author Amos
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
