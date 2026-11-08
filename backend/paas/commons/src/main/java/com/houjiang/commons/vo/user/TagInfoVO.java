package com.houjiang.commons.vo.user;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class TagInfoVO {
    /**
     * 会员标签id
     */
    private Integer tagId;
    /**
     * 会员标签名称
     */
    private String tagName;
    /**
     * 会员标签权重
     */
    private Integer tagWeight;
    /**
     * 会员标签图片
     */
    private String tagImage;
    /**
     * 会员标签描述
     */
    private String tagDescribe;



}
