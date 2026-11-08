package com.houjiang.goods.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName CommunityLikeListResp
 * @Description
 * @Author Amos
 * @Date 2022/6/27 09:16
 * @Version 1.0
 * @author Amos
 */
@Data
public class CommunityLikeListVO {
    /**
     * 心得点赞
     */
    private List<CommunityLikeEntityVO> likeList;
    /**
     * 总数
     */
    private Integer totalCount;
}
