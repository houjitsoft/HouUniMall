package com.houjiang.base.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @ClassName GdbMerchantAgreementDTO
 * @Author Amos
 * @Date 2023/6/21 14:00
 * @Version 1.0
 * @author Amos
 */
@Data
public class GdbMerchantAgreementDTO {

    /**
     * 协议id
     */
    private Integer id;

    /**
     * 商家协议/用户协议
     */
    private String name;

    /**
     * url
     */
    private String url;

    /**
     * 标记
     */
    private String sign;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private OssUploadUpdateParamDTO ossUploadUpdateParam;
}
