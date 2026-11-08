package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.base.entity.BaseWxLiveGoodsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.base.entity.BaseWxLiveGoods
 * @author Amos
 */
@Mapper
public interface BaseWxLiveGoodsMapper extends BaseMapper<BaseWxLiveGoodsEntity> {

    /**
     * 分页查询直播商品
     *
     * @param page
     * @param param
     * @return
     */
    IPage<BaseWxLiveGoodsEntity> queryLiveCommons(IPage<BaseWxLiveGoodsEntity> page, @Param("param") Map<String, Object> param);

    /**
     * 批量新增直播商品
     * @param wxLiveGoodsList
     * @return
     */
    Integer insertLiveGoods(@Param("wxLiveGoods") List<BaseWxLiveGoodsEntity> wxLiveGoodsList);
}




