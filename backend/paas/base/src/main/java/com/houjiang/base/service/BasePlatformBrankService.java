package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.entity.BasePlatformBrankEntity;
import com.houjiang.commons.dto.base.BasePlatformBrankDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;

/**
 * @ClassName BasePlatformBrankService
 * @Description  平台收款银行卡表
 * @Author Amos
 * @Date 2023-07-03 09:56:25
 * @Version 1.0
 * @author Amos
 */
public interface BasePlatformBrankService extends IService<BasePlatformBrankEntity> {
    /**
     * 获取平台收款银行卡信息
     * @param param
     * @return
     */
    ResponseResult queryBrank(UserTokenBeanDTO param);

    /**
     * 更新或添加 银行卡信息
     *
     * @param basePlatformBrankDto
     * @return
     */
    ResponseResult updateBrank(BasePlatformBrankDTO basePlatformBrankDto);


    /**
     * 查看上传支付凭证 (获取收款银行卡信息）加密返回
     * @return
     */
    ResponseResult queryBrankEncryption(UserTokenBeanDTO param);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    ResponseResult queryBrankFeign();
}
