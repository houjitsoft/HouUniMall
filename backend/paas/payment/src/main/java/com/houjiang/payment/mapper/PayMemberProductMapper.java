package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayMemberProductEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 自研会员产品开通记录 Mapper
 * @author Amos
 */
@Mapper
public interface PayMemberProductMapper extends BaseMapper<PayMemberProductEntity> {
}
