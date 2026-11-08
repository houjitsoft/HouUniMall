package com.houjiang.goods.mapper;

import com.houjiang.commons.vo.goods.ManageTransportAreaVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName ShopTransportAreaMapper
 * @Description 售卖区域
 * @Author Amos
 * @Date 2023/6/27 15:35
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ShopTransportAreaMapper {

    /**
     * 查询售卖区域
     * @param transportAreaId
     * @return
     */
    ManageTransportAreaVO selectById(@Param("transportAreaId")Integer transportAreaId);
}
