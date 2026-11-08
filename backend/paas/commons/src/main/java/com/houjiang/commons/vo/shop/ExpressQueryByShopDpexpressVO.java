package com.houjiang.commons.vo.shop;

import lombok.Data;

/**
 * @ClassName ExpressQueryByShopDpexpressVO
 * @Description
 * @Author Amos
 * @Date 2023/8/10 14:55
 * @Version 1.0
 * @author Amos
 */
@Data
public class ExpressQueryByShopDpexpressVO {

    /**
     * 快递公司id
     */
    private Integer expressId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 发件名称
     */
    private String customerName;

    /**
     * 发件密码
     */
    private String customerPwd;

    /**
     * 月code
     */
    private String monthCode;

    /**
     * 邮寄标题
     */
    private String sendSite;

    /**
     * 邮寄对象
     */
    private String sendStaff;

    /**
     * 物流代码
     */
    private String logisticCode;

    /**
     * id
     */
    private Integer id;

    /**
     * 快递公司
     */
    private String expressName;
}
