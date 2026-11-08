package com.houjiang.goods.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class CommunityCommentsDetailsVO {

    /**
     * 心得id
     */
    private String articleId;
    /**
     * 评论内容
     */
    private String remarkContent;
    /**
     * 被评论用户ID
     */
    private String remarkUser;
    /**
     * 评论层级:1-一级;2-二级'
     */
    private Integer remarkType;
    /**
     * '审核状态:0-待审核,1-审核通过'
     */
    private Integer auditState;
    /**
     * 心得标题
     */
    private String articleTitle;
    /**
     * 心得内容
     */
    private String articleContent;
    /**
     *心得封面
     */
    private String articlecover;
    /**
     * 审核备注
     */
    private String auditComment; 

    /**
     * 
     */
    private List<Integer>remarkIdList;

   
}
