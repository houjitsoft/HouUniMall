package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.util.ToolUtils;
import org.springframework.stereotype.Component;

/** 商家中心密码编码适配，确保平台开通账号与登录校验使用同一算法。 */
@Component
public class MerchantPasswordService {

    public String encode(String password) {
        return ToolUtils.makePassword(password);
    }
}
