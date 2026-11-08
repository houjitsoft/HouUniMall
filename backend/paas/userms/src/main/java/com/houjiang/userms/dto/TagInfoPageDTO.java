package com.houjiang.userms.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * 会员标签分页信息
 * @author Amos
 */
@Data
public class TagInfoPageDTO extends PageHelper {

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

    /**
     * 会员标签修改时间
     */
    private String tagUpdData;

}
