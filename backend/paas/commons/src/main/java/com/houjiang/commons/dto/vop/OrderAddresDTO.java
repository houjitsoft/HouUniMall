package com.houjiang.commons.dto.vop;

import lombok.Data;

/**
 * @ClassName OrderAddresDTO
 * @Author Amos
 * @Date 2025/6/17 15:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderAddresDTO {
    /**
     *  用户名
     */
    private String userName;
    /**
     * 联系方式
     */
    private String userContact;
    /**
     * 四级地址
     */
    private String address;
    /**
     * 详细地址
     */
    private String addressDetail;


}
