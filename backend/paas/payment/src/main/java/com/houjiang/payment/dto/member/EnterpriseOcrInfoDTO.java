package com.houjiang.payment.dto.member;

import lombok.Data;

import java.io.Serializable;

/**
 * 企业影印件识别信息DTO
 * 
 * @author Amos
 */
@Data
public class EnterpriseOcrInfoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 返回码
     * 000000 成功,其他失败
     */
    private String respCode;
    
    /**
     * 失败原因
     */
    private String respMsg;
    
    /**
     * 识别结果
     * 0-查询到数据
     * 1-未查到数据
     */
    private String result;
    
    /**
     * 统一社会信用代码
     */
    private String creditCode;
    
    /**
     * 企业名称
     */
    private String corpName;
    
    /**
     * 有效期是否永久
     * true/false
     */
    private String forever;
    
    /**
     * 有效期
     * 该字段当forever为false时有效
     * 格式：yyyy/MM/dd,如：2999/12/31
     */
    private String validate;
}
