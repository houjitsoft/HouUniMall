package com.houjiang.goods.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName ManageGdsCommonQueryResultStrVO
 * @Description
 * @Author Amos
 * @Date 2023/8/11 13:30
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGdsCommonQueryResultStrVO {

    /** 草稿id */
    private Integer draftId;

    /** 草稿标题 */
    private String draftName;

    /** 草稿内容 */
    private String draftContent;

    /**  */
    private Integer catId;

    /**  */
    private Integer shopId;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
