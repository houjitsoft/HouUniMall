package com.houjiang.job.vo;

import lombok.Data;

/**
 * @ClassName DcenterGoodsCatVO
 * @Description
 * @Author Amos
 * @Date 2025/1/14 13:30
 * @Version 1.0
 * @author Amos
 */
@Data
public class DcenterGoodsCatVO {

    /**
     *
     */

    private Integer catId;

    /**
     *  分类名称
     */
    private String catName;

    /**
     * 父类
     */
    private Integer catParentId;


}
