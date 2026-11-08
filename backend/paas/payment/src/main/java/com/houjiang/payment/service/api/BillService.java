package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.bill.BillImportRequest;
import com.houjiang.payment.dto.bill.BillImportResponse;

/**
 * 账单服务接口
 * 
 * @author Amos
 */
public interface BillService {
    
    /**
     * 10. 账单导入
     * 通过接口导入账单，员工可通过通企付企业服务助手查询账单并分享账单码牌用于收款
     * 
     * @param request 账单导入请求
     * @return 账单导入响应
     */
    BillImportResponse importBill(BillImportRequest request);
}
