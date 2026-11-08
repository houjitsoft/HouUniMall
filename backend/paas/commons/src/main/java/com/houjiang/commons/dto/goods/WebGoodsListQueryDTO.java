package com.houjiang.commons.dto.goods;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WebGoodsListQueryDTO extends ListPageQueryDTO {

    /**
     * 评论数 1降序，2升序
     */
    private Integer commonEvaluate;

    /**
     * token
     */
    private String token;

    /**
     * 是否推荐0 是 1 否
     */
    private Integer commonIsRecommend;

    private Integer attributeId;
}
