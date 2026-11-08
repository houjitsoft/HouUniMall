package com.houjiang.base.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 修改 oss 配置
 * @author Amos
 */
@Data
public class BaseOssConfigDTO {

    /**
     * token
     */
    private String token;

    /**
     * 自增主键
     */
    private Integer ossId;

    /**
     * API的bucket名称
     */
    private String ossBacketName;

    /**
     * API的文件夹名称
     */
    private String ossFolder;

    /**
     * 服务器集群地址
     */
    private String ossRegion;

    /**
     * 域名
     */
    private String ossEndpoint;

    /**
     * 密钥
     */
    private String ossAccessKeyId;

    /**
     * 私钥
     */
    private String ossAccessKeySecret;

    /**
     * 状态值 00禁用  01启用，默认启用
     */
    private String ossStates;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date ossCreatedDate;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String ossUpdateDate;

    /**
     * 配置的备注信息
     */
    private String ossRemark;

    /**
     * oss类型 默认 aliyun
     */
    private String ossType;


}
