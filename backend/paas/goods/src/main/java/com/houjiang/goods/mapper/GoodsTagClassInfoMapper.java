package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.entity.GoodsTagClassInfoEntity;
import com.houjiang.goods.vo.CommunityArticleTagClassVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Entity com.houjiang.goods.entity.GoodsTagClassInfo
 * @author Amos
 */
@Mapper
public interface GoodsTagClassInfoMapper extends BaseMapper<GoodsTagClassInfoEntity> {

    /**
     * 发表社区心得-查询话题分类
     * @return
     */
    List<CommunityArticleTagClassVO> queryTagClass();

}




