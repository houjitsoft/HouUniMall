package com.houjiang.commons.vo.shop;

import lombok.Data;

/**
 * @ClassName ShopPersonInfo
 * @Description
 * @Author Amos
 * @Date 2022/9/13 16:52
 * @Version 1.0
 * @author Amos
 */
@Data
public class ShopPersonInfoVO {

    /**
     * 证件类型
     */
    private Integer idType;

    /**
     * 真实姓名
     */
    private String trueName;

    /**
     * 身份证号
     */
    private String idNo;

    /**
     * 证件图片1
     */
    private String idPicOne;

    /**
     * 证件图片2
     */
    private String idPicTwo;

    /**
     * 证件有效期 营业执照有效期
     */
    private String validateStartTime;

    /**
     * 证件有效期结束时间
     */
    private String validateEndTime;

    /**
     * 法定经营范围结束时间
     */
    private Integer validateLongTerm;

    /**
     * 是否实名认证
     */
    private Integer isCertified;
}
