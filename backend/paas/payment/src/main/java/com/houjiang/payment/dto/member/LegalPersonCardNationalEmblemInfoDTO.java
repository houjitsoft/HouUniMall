package com.houjiang.payment.dto.member;

import lombok.Data;

import java.io.Serializable;

/**
 * 法人影印件国徽面识别信息DTO
 * 
 * @author Amos
 */
@Data
public class LegalPersonCardNationalEmblemInfoDTO implements Serializable {
    
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
     * 有效期
     * 格式：yyyy/MM/dd-yyyy/MM/dd,如果长期则显示中文
     */
    private String validDate;
    
    /**
     * 所属公安单位
     */
    private String unitName;
    
    /**
     * 是否过期
     * 1-未过期
     * 0-已过期
     */
    private String isValid;
}
