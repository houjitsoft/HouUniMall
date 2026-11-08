package com.houjiang.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.PayCardRecordDTO;
import com.houjiang.payment.entity.PayCardRecordEntity;

 /**
 * @ClassName PayCardRecordService
 * @Description  生成充值卡信息记录表
 * @Author Amos
 * @Date 2023-05-31 15:35:50
 * @Version 1.0
 * @author Amos
 */
public interface PayCardRecordService extends IService<PayCardRecordEntity> {

  /**
   * 平台 查询充值卡生成记录表
   *
   * @param payCardRecordDto
   * @return
   */
  ResponseResult query(PayCardRecordDTO payCardRecordDto);

     /**
      * 平台 生成充值卡
      *
      * @param payCardRecordDto
      * @return
      */
     ResponseResult add(PayCardRecordDTO payCardRecordDto);
}
