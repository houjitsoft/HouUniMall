package com.houjiang.commons.dto.goods;

import lombok.Data;

import java.util.List;

/**
 * @ClassName CatIdsDTO
 * @Description
 * @Author Amos
 * @Date 2025/2/27 17:10
 * @Version 1.0
 * @author Amos
 */
@Data
public class CatIdsDTO {

    // 存储猫的ID列表，用于后续的查询和处理
    List<Integer> catIdList;

    // 商品类型，用于区分不同的商品类别
    Integer goodsType;
}
