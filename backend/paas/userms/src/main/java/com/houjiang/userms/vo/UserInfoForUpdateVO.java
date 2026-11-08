package com.houjiang.userms.vo;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class UserInfoForUpdateVO {

    /**
     * 用户昵称
     */
    private String userNickname;
    /**
     *性别
     */
    private String userGender;
    /**
     *手机
     */
    private String userMobile;
    /**
     *真实姓名
     */
    private String userTruename;
    /**
     *生日
     */
    private String userBirth;
    /**
     *会员名
     */
    private String memberName;
    /**
     *用户头像
     */
    private String userAvatar;
    /**
     *最后登录时间
     */
    private String userLastloginTime;
    /**
     *下一级的积分
     */
    private String memberUrl;
    /**
     * 国际区号
     */
    private String internationalDialingCode;

    /**
     * 用户组织列表
     */
    private java.util.List<java.util.Map<String, Object>> orgList;

    /**
     * 组织ID（hou_user_info表中的org_id字段，可能是逗号分隔的字符串，如"1,2"）
     */
    private String orgId;

}
