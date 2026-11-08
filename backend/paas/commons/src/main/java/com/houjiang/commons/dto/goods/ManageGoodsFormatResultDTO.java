package com.houjiang.commons.dto.goods;

import lombok.Data;

/**
 * @ClassName ManageGoodsFormatResultDTO
 * @Description 关联板式
 * @Author Amos
 * @Date 2023/6/26 15:10
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGoodsFormatResultDTO {
    private Integer id;

    /**
     * 版式名称
     */
    private String name;

    /**
     * 位置
     */
    private Integer position;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 内容
     */
    private String content;
}
