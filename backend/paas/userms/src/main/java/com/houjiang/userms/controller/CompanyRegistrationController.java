package com.houjiang.userms.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.service.CompanyRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 公司登记控制器
 * @author Amos
 * @date 2025/01/08
 */
@Slf4j
@RestController
@RequestMapping("/user/ms/company")
public class CompanyRegistrationController {
    
    @Autowired
    private CompanyRegistrationService companyRegistrationService;
    
    /**
     * 提交公司登记申请
     * @param companyData 公司登记信息
     * @return 提交结果
     */
    @PostMapping("/register")
    public ResponseResult register(@RequestBody Map<String, Object> companyData) {
        log.info("收到公司登记申请：{}", companyData);
        return companyRegistrationService.submitCompanyRegistration(companyData);
    }
}
