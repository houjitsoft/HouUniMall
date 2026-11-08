package com.houjiang.commons.dto.shop;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName
 * @Description
 * @Author Amos
 * @Date 2024-09-04 10:27:09
 * @Version 1.0
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ShopUserDTO extends PageUserTokenBeanDTO {

    /**
     * 用户id
     */
    private String userId;
    /**
     * 店铺id
     */
    private String shopId;
    /**
     * 会员账号
     */
    private String userMobile;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 是否企业:1-是;0-否
     */
    private Integer isBusiness;
    /**
     * 成为店铺会员时间
     */
    private Date createUserTime;
    /***
     * 会员等级id
     */
    private Integer gradeId;
}
