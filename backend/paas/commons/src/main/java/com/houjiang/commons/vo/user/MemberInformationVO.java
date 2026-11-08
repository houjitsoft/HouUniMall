package com.houjiang.commons.vo.user;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class MemberInformationVO {

    /** 最高等级时返回code */
    private Integer code;
    /** 用户会员等级名称 */
    private String memberName;
    /** 用户会员等级下一级名称 */
    private String memberNameNext;
    /** 距离下次升级还需多少积分 */
    private Integer distanceIntegral;
    /** 当前的会员积分 */
    private Integer memberIntegral;
    /** 用户姓名 */
    private String userName;
    /** 用户头像 */
    private String userAvatar;
    /** 下一级的积分 */
    private Integer nextGradeRule;
    /** 下一级的积分 */
    private String memberUrl;
    /** 是否开通plus会员 */
    private Integer isplusMember;
    /** plus会员 套餐 */
    private Integer thaliType;
    /** 会员折扣 */
    private String gradeDiscount;
    /** 冻结积分 */
    private Integer frozenIntegra;
}
