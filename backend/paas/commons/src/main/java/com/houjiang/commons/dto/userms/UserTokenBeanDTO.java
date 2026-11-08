package com.houjiang.commons.dto.userms;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenBeanDTO extends PageHelper {

    /**
     * token
     */
    private String token;

    /**
     * 红包使用端口 0无限制 1PC 2wap 3安卓 5ios 6微信小程序 7wap浏览器
     */
    private String platForm;

    /**
     * 类型
     */
    private String albumType;

    /**
     * 地址
     */
    private String macAddress;

    /**
     * 店铺id
     */
    private String shopId;

    public UserTokenBeanDTO(String token, String shopId) {
        this.token = token;
        this.shopId = shopId;
    }

    public UserTokenBeanDTO(String token) {
        this.token = token;
    }
}
