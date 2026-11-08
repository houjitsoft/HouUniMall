package com.houjiang.goods.vo;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class CommunityTopicVO extends PageHelper {
    /**
     * 话题ID
     */
    private Integer tagId;
    /**
     *所属分类
     */
    private Integer classId;
    /**
     *话题内容
     */
    private String tagContent;
    /**
     *话题副标题
     */
    private String tagSubtitle;
    /**
     *话题封面
     */
    private String tagCover ;
    /**
     *分类名称
     */
    private String className;
    /**
     *新增时间
     */
    private String createTime;
    /**
     *审核状态
     */
    private Integer isActive;



}
