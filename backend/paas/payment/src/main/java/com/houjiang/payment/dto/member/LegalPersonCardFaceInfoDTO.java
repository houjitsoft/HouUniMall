package com.houjiang.payment.dto.member;

import lombok.Data;

import java.io.Serializable;

/**
 * 法人影印件肖像面识别信息DTO
 * 
 * @author Amos
 */
@Data
public class LegalPersonCardFaceInfoDTO implements Serializable {
    
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
     * 姓名
     */
    private String name;
    
    /**
     * 身份证号
     */
    private String idCard;
    
    /**
     * 性别
     */
    private String gender;
    
    /**
     * 民族
     */
    private String nation;
    
    /**
     * 生日
     * 格式如：1995/10/08
     */
    private String birthday;
    
    /**
     * 地址
     */
    private String address;
}
