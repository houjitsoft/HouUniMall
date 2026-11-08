package com.houjiang.goods.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName CommunityUserInfo
 * @Description 用户信息实体类
 * @Author Amos
 * @Date 2022/2/10 14:37
 * @Version 1.0
 * @author Amos
 */
@Data
public class CommunityUserInfoVO {
    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;
    /**
     * 社区个人页背景图
     */
    private String communityBackground;
    /**
     * 社区个性签名
     */
    private String userSignature;

    /**
     * 点赞数
     */
    private Integer likedNum;

    /**
     * 关注数
     */
    private Integer followNum;

    /**
     * 被关注数
     */
    private Integer followedNum;
    /**
     * 是否已关注:1-是;0-否
     */
    private Integer isFollowed;
    /**
     * 收藏数
     */
    private Integer starNum;

    /**
     * 心得数
     */
    private Integer articleNum;

    /**
     * 心得信息
     */
    private List<CommunityArticleSmallEntityVO> articleList;

    /**
     * 用户昵称
     */
    private String userNickName;

}
