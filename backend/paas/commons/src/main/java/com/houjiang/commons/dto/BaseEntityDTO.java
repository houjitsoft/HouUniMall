package com.houjiang.commons.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseEntityDTO extends UserTokenBeanDTO {

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 更新人
     */
    private String updateUser;

    /**
     * 是否删除
     */
    private Integer isDelete = 0;

    /**
     * 版本号
     */
    private Integer sysVersion = 0;

    /**
     * 时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date ts = new Date();
}
