package com.houjiang.goods.mapper;

import com.houjiang.commons.vo.goods.ManageShopBaseQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName ShopBaseMapper
 * @Description shopbase表
 * @Author Amos
 * @Date 2023/6/26 16:56
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ShopBaseMapper {

    /**
     * 获取店铺区域id
     * @param shopId
     * @return
     */
    ManageShopBaseQueryVO selectByShopId2(@Param("shopId") String shopId);

    /**
     * 查询店铺名称
     * @param shopId
     * @return
     */
    String selectShopName(@Param("shopId") Integer shopId);
}
