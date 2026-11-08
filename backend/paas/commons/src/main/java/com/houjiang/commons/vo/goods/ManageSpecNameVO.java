package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ManageSpecNameVO
 * @Description 规格
 * @Author Amos
 * @Date 2023/6/27 14:38
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageSpecNameVO {
    private Integer specId;
    /**
     * 规格名称
     */
    private String specName;
    /**
     * 规格值
     */
    private List specValueNames;
}
