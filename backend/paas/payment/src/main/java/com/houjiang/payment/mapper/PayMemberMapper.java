package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayMemberEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员开户记录 Mapper。
 *
 * @author Amos
 */
@Mapper
public interface PayMemberMapper extends BaseMapper<PayMemberEntity> {
}
