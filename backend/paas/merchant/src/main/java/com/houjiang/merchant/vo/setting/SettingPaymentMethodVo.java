package com.houjiang.merchant.vo.setting;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class SettingPaymentMethodVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String payWayName;

    private Integer payConfigId;

    private String icon;

    private Integer scene;

    private Integer status;

    private Integer isDefault;

}
