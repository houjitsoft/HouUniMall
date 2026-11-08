package com.houjiang.mall.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会员信息实体类
 * 对应数据库表：hou_user_info
 * 
 * @author Amos
 */
@Data
@TableName("hou_user_info")
public class Member implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long userId;

    /**
     * 手机号码
     */
    private String userMobile;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 状态 (0:未认证 1:已认证 2:锁定)
     */
    private Integer userState;

    /**
     * 用户积分
     */
    private Integer userIntegral;

    /**
     * 上次登录时间
     */
    private LocalDateTime userLastloginTime;

    /**
     * 上次登录 IP
     */
    private String userLastloginIp;

    /**
     * 登录次数
     */
    private Integer userCountLogin;

    /**
     * 是否为新用户 0-否 1-是
     */
    private Integer userIsNew;

    /**
     * 社区个人页背景图
     */
    private String communityBackground;

    /**
     * 社区个性签名
     */
    private String userSignature;

    /**
     * 是否企业：1-是 0-否
     */
    private Integer isBusiness;

    /**
     * 粉丝数
     */
    private Integer followerNum;

    /**
     * 关注数
     */
    private Integer followNum;

    /**
     * 会员等级 id
     */
    private Long gradeId;

    /**
     * 成为会员时间
     */
    private LocalDateTime memberCreateTime;

    /**
     * 店铺 id（用于区分平台用户和品牌店铺用户）
     */
    private Integer shopId;

    /**
     * 是否删除 0-否 1-是
     */
    private Integer isDelete;

    /**
     * 是否为最高管理员：1-是 0-否
     */
    private Integer isAdmin;

    /**
     * 创建者
     */
    private String createUser;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    private String updateUser;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}
