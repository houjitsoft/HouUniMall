package com.houjiang.userms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName BusinessUserListEntity
 * @Description
 * @Author Amos
 * @Date 2022/5/7 11:15
 * @Version 1.0
 * @author Amos
 */
@Data
public class BusinessUserListVO {
    /**
     * 用户id
     */
    private String userId;
    /**
     *用户名
     */
    private String userName;
    /**
     *公司名
     */
    private String companyName;
    /**
     *公司税号
     */
    private String taxId;
    /**
     *营业执照
     */
    private String businessLicense;
    /**
     *创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


}
