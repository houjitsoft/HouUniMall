package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户基本信息表
 * @TableName user_info
 * @author Amos
 */
@TableName(value ="hou_user_info")
@Data
public class UserInfoEntity implements Serializable {
    /**
     * 用户ID
     */
    @TableId
    private String userId;

    /**
     * 手机号码
     */
    private String userMobile;

    /**
     * 密码
     */
    private String password;
    /**
     * 国际区号
     */
    private String internationalDialingCode;

    /**
     * 用户昵称 不唯一
     */
    private String userName;

    /**
     * 状态(0:未激活,1:未认证,2:已认证,3:锁定4.多账号)
     */
    private Integer userState;

    /**
     * 用户积分
     */
    private Integer userIntegral;

    /**
     * 上次登录时间
     */
    private Date userLastloginTime;

    /**
     * 上次登录IP
     */
    private String userLastloginIp;

    /**
     * 登录次数 
     */
    private Integer userCountLogin;

    /**
     * 是否为新用户 0 不是  1是
     */
    private Integer userIsNew;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 创建者
     */
    private String createUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改人
     */
    private String updateUser;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 是否删除0:否;1:是
     */
    private Integer isDelete;

    /**
     * 时间戳
     */
    private Date ts;

    /**
     * 社区个人页背景图
     */
    private String communityBackground;

    /**
     * 社区个性签名
     */
    private String userSignature;

    /**
     * 是否企业:1-是;0-否
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
     * 会员等级id
     */
    private Integer gradeId;

    /**
     * 成为会员时间
     */
    private Date memberCreateTime;

    /**
     * 注册终端：mall / houcloud / hougeo
     */
    private String registerTerminal;

    /**
     * 组织 ID（已废弃，不再从 hou_user_info 表获取，改为从 hou_user_organization 表获取）
     * 使用@TableField(exist = false) 标记为非数据库字段，避免 MyBatis-Plus 自动查询
     */
    @TableField(exist = false)
    private String orgId;

    /**
     * 组织信息列表 (可以是orgId:orgName格式的Map或包含完整信息的List<Map<String, Object>>)
     */
    @TableField(exist = false)
    private Object orgList;

    /**
     * 是否是商户 0否 1是
     */
    @TableField(exist = false)
    private Integer isShop;

    /**
     * 头像地址
     */
    @TableField(exist = false)
    private String userAvatar;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;




    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", userId=").append(userId);
        sb.append(", userMobile=").append(userMobile);
        sb.append(", password=").append(password);
        sb.append(", userName=").append(userName);
        sb.append(", userState=").append(userState);
        sb.append(", userIntegral=").append(userIntegral);
        sb.append(", userLastloginTime=").append(userLastloginTime);
        sb.append(", userLastloginIp=").append(userLastloginIp);
        sb.append(", userCountLogin=").append(userCountLogin);
        sb.append(", userIsNew=").append(userIsNew);
        sb.append(", userNickname=").append(userNickname);
        sb.append(", createUser=").append(createUser);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateUser=").append(updateUser);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", isDelete=").append(isDelete);
        sb.append(", ts=").append(ts);
        sb.append(", communityBackground=").append(communityBackground);
        sb.append(", userSignature=").append(userSignature);
        sb.append(", isBusiness=").append(isBusiness);
        sb.append(", followerNum=").append(followerNum);
        sb.append(", followNum=").append(followNum);
        sb.append(", gradeId=").append(gradeId);
        sb.append(", memberCreateTime=").append(memberCreateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    // ==================== 以下为废弃字段的兼容方法 ====================
    
    /**
     * 店铺 id（已废弃）
     * @deprecated 数据库表中已删除该字段
     */
    @Deprecated
    public Integer getShopId() {
        // 已废弃，始终返回 null
        return null;
    }

    /**
     * 店铺 id（已废弃）
     * @deprecated 数据库表中已删除该字段
     */
    @Deprecated
    public void setShopId(Integer shopId) {
        // 已废弃，不执行任何操作
    }
}