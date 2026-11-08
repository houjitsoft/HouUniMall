package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.entity.GoodsBrandTypeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 品牌类型Mapper
 * @author Amos
 */
@Mapper
public interface GoodsBrandTypeMapper extends BaseMapper<GoodsBrandTypeEntity> {
    // MyBatis Plus 已提供基本CRUD，无需额外定义
}
