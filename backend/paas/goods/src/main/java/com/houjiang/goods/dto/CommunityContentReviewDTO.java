package com.houjiang.goods.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class CommunityContentReviewDTO extends PageHelper {
    /**
     * 心得id
     */
    private String articleId; 
    /**
     *用户id
     */
    private String userId;   
    /**
     *心得标题
     */
    private String articleTitle;
    /**
     *心得内容
     */
    private String articleContent;
    /**
     *心得封面
     */
    private String articlecover;
    /**
     *审核状态
     */
    private Integer auditState;
    /**
     *审核备注
     */
    private String auditComment; 
    /**
     *商品规格id
     */
    private Integer goodsId;
    /**
     *心得类型:1-图片,2-视频
     */
    private Integer articleType;
    /**
     *心得发布日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date postTime;
    /**
     *心得图片/视频
     */
    private List<String> resourceUrl;

    /**
     *心得集合
     */
    private List<String> articleIdList;

    /**
     *话题id
     */
    private Integer tagId;
    /**
     *话题标题
     */
    private String  tagContent;
    /**
     *商品名称
     */
    private String commonName;
    /**
     *商品图片
     */
    private String commonImage;
    /**
     *商品价格
     */
    private  String commonPrice;
    /**
     *商品id
     */
    private  Integer commonId ;
    /**
     *用户名称
     */
    private String userName;
    /**
     *用户电话
     */
    private String userMobile;
    /**
     *新增时间
     */
    private String createTime;
    /**
     *
     */
    private String userNickName;
}
