package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.base.entity.BaseSearchWordEntity;
import com.houjiang.commons.dto.goods.GoodsMerchantSearchWordDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 搜索热门词(BaseSearchWord)
 *
 * @since 2023-03-28 14:08:28
 * @author Amos
 */
@Mapper
public interface BaseSearchWordMapper extends BaseMapper<BaseSearchWordEntity> {

    /**
     * 根据热搜词查询
     * @param commonName
     * @return
     */
    GoodsMerchantSearchWordDTO selectBySearchName(@Param("commonName")String commonName);
    /**
     * 更新
     * @param record
     * @return
     */
    Integer updateByPrimaryKeySelective(GoodsMerchantSearchWordDTO record);
    /**
     * 插入
     * @param record
     * @return
     */
    Integer insertSelective(GoodsMerchantSearchWordDTO record);


    /**
     * 分页查询热搜词
     *
     * @param page
     * @param param
     * @return
     */
    IPage<BaseSearchWordEntity> querySearchWord(IPage page, @Param("param") GoodsMerchantSearchWordDTO param);

    /**
     * 移动端查询热搜词
     * @return
     */
    List<String> queryKeyWords();

}

