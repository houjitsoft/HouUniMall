package com.houjiang.commons.dto.base;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 快递表
 *
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BaseExpressDTO extends PageHelper {

    private Long id;

    /**
     * 快递id
     */
    private Integer expressId;

    /**
     * 快递公司
     */
    private String expressName;

    /**
     * 物流
     */
    private String expressPinyin;

    /**
     * 状态0关闭1开启
     */
    private Integer expressStatus;

    /**
     * 是否常用0否1是
     */
    private Integer expressDisplayorder;

    /**
     * 是否常用
     */
    private Integer expressCommonorder;


}
