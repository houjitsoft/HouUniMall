package com.houjiang.userms.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
/**
 * @ClassName
 * @Description  用户地址表
 * @Author Amos
 * @Date 2023-06-16 15:26:49
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_user_address")
@Data
public class UserAddressEntity  implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer userAddressId;
    /**
     * 所属店铺
     */
    private String userId;
    /**
     * 所属联系地址
     */
    private String userAddressContact;
    /**
     * 所属地址区
     */
    private Integer userAddressProvinceId;
    /**
     * 所属地址城市
     */
    private Integer userAddressCityId;
    /**
     * 所属地址区
     */
    private Integer userAddressAreaId;
    /**
     * 所在地区-字符串组合
     */
    private String userAddressArea;
    /**
     * 街道地址-不必重复填写地区
     */
    private String userAddressAddress;
    /**
     * 联系电话
     */
    private String userAddressPhone;
    /**
     * 默认地址0不是1是
     */
    private Integer userAddressDefault;

    private Date userAddressTime;
    /**
     * 时间戳
     */
    private Date ts;
    /**
     * 国际地址 1 是 2 否
     */
    private Integer internationalType;
    /**
     * 国家id
     */
    private Integer countryId;
    /**
     * 国家名称
     */
    private String countryName;
    /**
     * 国际区号
     */
    private String internationalDialingCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
