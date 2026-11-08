package com.houjiang.goods.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName CommunityRemarkEntity
 * @Description 收到的评论通知实体类
 * @Author Amos
 * @Date 2022/6/23 15:31
 * @Version 1.0
 * @author Amos
 */
@Data
public class CommunityRemarkVO {

    private Integer remarkId;
    /**
     * 心得id
     */
    private String articleId;
    /**
     * 被评论ID
     */
    private String remarkedId;
    /**
     * 被评论的内容
     */
    private String remarkedContent;
    /**
     * 评论内容
     */
    private String remarkContent;
    /**
     * 评论层级:1-一级;2-二级'
     */
    private Integer remarkType;

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date postDate;
    /**
     * 心得内容
     */
    private String articleContent;
    /**
     *心得封面
     */
    private String articleCover;


}
