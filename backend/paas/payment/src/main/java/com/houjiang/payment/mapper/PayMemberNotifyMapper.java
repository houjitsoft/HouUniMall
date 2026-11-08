package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayMemberNotifyEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员回调通知流水 Mapper。
 *
 * <p>幂等写入靠唯一键冲突：插入返回 0 行即视为重复通知。
 *
 * @author Amos
 */
@Mapper
public interface PayMemberNotifyMapper extends BaseMapper<PayMemberNotifyEntity> {
}
