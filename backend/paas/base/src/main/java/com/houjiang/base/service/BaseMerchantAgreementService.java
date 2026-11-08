package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.GdbMerchantAgreementDTO;
import com.houjiang.base.entity.BaseMerchantAgreementEntity;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @ClassName BaseMerchantAgreementService
 * @Description 入驻协议
 * @Author Amos
 * @Date 2023/6/21 14:03
 * @Version 1.0
 * @author Amos
 */
public interface BaseMerchantAgreementService extends IService<BaseMerchantAgreementEntity> {

    /**
     * 查询入驻协议
     * @param gdbMerchantAgreement
     * @return
     */
    ResponseResult selectAllAgreement(GdbMerchantAgreementDTO gdbMerchantAgreement);


    /**
     * 修改或添加入驻协议
     *
     * @param file
     * @param sign
     * @return
     */
    ResponseResult updateAgreement(MultipartFile file, String sign);


    /**
     * 查询入驻协议
     *
     * @param map
     * @return
     */
    ResponseResult queryAgreement(Map<String, Object> map);
}
