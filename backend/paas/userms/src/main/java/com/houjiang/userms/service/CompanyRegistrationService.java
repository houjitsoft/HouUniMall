package com.houjiang.userms.service;

import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * 公司登记服务接口
 * @author Amos
 * @date 2025/01/08
 */
public interface CompanyRegistrationService {
    
    /**
     * 提交公司登记申请
     * @param companyData 公司登记信息
     * @return 提交结果
     */
    ResponseResult submitCompanyRegistration(Map<String, Object> companyData);
}
