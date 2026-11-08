package com.houjiang.goods.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class CommunityCommentsVO extends PageHelper {
    /**
     * 评论ID
     */
    private Integer remarkId;
    /**
     * 心得id
     */
    private String articleId;
    /**
     * 评论内容
     */
    private String remarkContent;
    /**
     * 评论用户ID
     */
    private String remarkUser;
    /**
     * 被评论用户ID
     */
    private String remarkedUser;
    /**
     *评论用户昵称
     */
    private String remarkUserNickName;
    /**
     * 被评论用户昵称
     */
    private String remarkedUserNickName;
    /**
     * 评论用户手机号
     */
    private String remarkUserPhone;
    /**
     * 被评论用户手机号
     */
    private String remarkedUserPhone;
    /**
     * '评论层级:1-一级;2-二级'
     */
    private Integer remarkType;
    /**
     * '审核状态:0-待审核,1-审核通过
     */
    private Integer auditState;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /**
     * 
     */
    private Date postDate;
    /**
     * 心得内容
     */
    private String articleContent;
    /**
     * 心得封面
     */
    private String articleCover;



}
