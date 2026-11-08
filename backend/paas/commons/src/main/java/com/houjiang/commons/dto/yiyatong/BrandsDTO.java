package com.houjiang.commons.dto.yiyatong;

import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * @ClassName BrandsDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/6 14:49
 * @Version 1.0
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BrandsDTO extends PageBaseHelper {

    /**
     * 第一页
     */
    private Integer pageIndex;

    /**
     * 品牌ID
     */
    private Integer brandId;

    /**
     * 每页显示条数
     */
    private Integer pageSize;

    /**
     * 排序字段(支持多字段，传入数据格式：name.ascend-age.descend)
     */
    private Integer sort;
}
