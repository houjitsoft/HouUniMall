package com.houjiang.thirdparty.service.esign;

import com.houjiang.thirdparty.service.esign.dto.EsignContractDTO;
import com.houjiang.thirdparty.service.esign.dto.EsignResultDTO;
import com.houjiang.thirdparty.service.esign.dto.EsignSignerDTO;

import java.util.List;

/**
 * 电子签名服务接口
 * 支持多种第三方电子签名服务（法大大、上上签、e签宝、腾讯电子签等）
 * 
 * @author Amos
 * @since 2025-01-07
 */
public interface EsignService {
    
    /**
     * 获取服务提供商代码
     * 
     * @return 提供商代码（fadada, shangshangqian, esign, tencent等）
     */
    String getProviderCode();
    
    /**
     * 创建合同
     * 
     * @param contractDTO 合同信息
     * @return 合同创建结果（包含合同ID、签署链接等）
     */
    EsignResultDTO createContract(EsignContractDTO contractDTO);
    
    /**
     * 添加签署人
     * 
     * @param contractId 合同ID
     * @param signers 签署人列表
     * @return 操作结果
     */
    EsignResultDTO addSigners(String contractId, List<EsignSignerDTO> signers);
    
    /**
     * 获取签署链接
     * 
     * @param contractId 合同ID
     * @param signerId 签署人ID
     * @return 签署链接
     */
    EsignResultDTO getSignUrl(String contractId, String signerId);
    
    /**
     * 查询合同状态
     * 
     * @param contractId 合同ID
     * @return 合同状态信息
     */
    EsignResultDTO getContractStatus(String contractId);
    
    /**
     * 下载合同文件
     * 
     * @param contractId 合同ID
     * @return 合同文件（Base64或文件流）
     */
    EsignResultDTO downloadContract(String contractId);
    
    /**
     * 撤销合同
     * 
     * @param contractId 合同ID
     * @param reason 撤销原因
     * @return 操作结果
     */
    EsignResultDTO revokeContract(String contractId, String reason);
}

