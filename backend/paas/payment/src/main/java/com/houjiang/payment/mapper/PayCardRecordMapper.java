package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.payment.dto.PayCardRecordDTO;
import com.houjiang.payment.entity.PayCardRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName PayCardRecordMapper
 * @Description  生成充值卡信息记录表
 * @Author Amos
 * @Date 2023-05-31 15:35:50
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface PayCardRecordMapper extends BaseMapper<PayCardRecordEntity> {

 /**
  * 分页查询支付卡
  *
  * @param page
  * @param payCardRecordDto
  * @return
  */
 IPage<PayCardRecordEntity> query(IPage<PayCardRecordEntity> page, @Param("param") PayCardRecordDTO payCardRecordDto);
}
