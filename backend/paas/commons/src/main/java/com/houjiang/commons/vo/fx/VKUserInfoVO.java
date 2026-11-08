package com.houjiang.commons.vo.fx;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName VKUserInfoVO
 * @Description 微客用户信息
 * @Author Amos
 * @Date 2023-06-13 09:05:20
 * @Version 1.0
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VKUserInfoVO {

    private Integer id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 一级邀请人
     */
    private String oneInviter;

    /**
     * 二级邀请人
     */
    private String twoInviter;

    /**
     * 是否有上级
     */
    private Integer isSuperior;

    /**
     * 一级邀请人是否是微客
     */
    private Boolean oneVkFlag;

    /**
     * 一级邀请人用户名
     */
    private String oneUserName;

    /**
     * 一级邀请人手机号
     */
    private String oneUserMobile;

    /**
     * 二级邀请人是否是微客
     */
    private Boolean twoVkFlag;

    /**
     * 二级邀请人用户名
     */
    private String twoUserName;

    /**
     * 二级邀请人手机号
     */
    private String twoUserMobile;

    /**
     * 用户是否是微客
     */
    private Boolean userVkFlag;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String userMobile;

    /**
     * 0非微客  1会员微客  2员工微客
     */
    private Integer vkState;

}
