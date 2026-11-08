package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.goods.dto.PlatFormAllGoodsDTO;
import com.houjiang.goods.entity.GoodsDealerPutawayRecordEntity;
import com.houjiang.goods.vo.QueryDealerGoodsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName GoodsDealerPutawayRecordMapper
 * @Description 商品供应记录
 * @Author Amos
 * @Date 2023/6/26 11:24
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface GoodsDealerPutawayRecordMapper extends BaseMapper<GoodsDealerPutawayRecordEntity> {

    /**
     * 删除商品供应记录
     * @param commonIds
     * @return
     */
    int deleteRecordStateBatchAll(@Param("commonIds") List<Integer> commonIds);

    /**
     * 查询代发商品信息
     * @param page
     * @param platFormAllGoodsDTO
     * @return
     */
    IPage<QueryDealerGoodsVO> platFormQueryDealerGoods(IPage page, @Param("param") PlatFormAllGoodsDTO platFormAllGoodsDTO);

    /**
     * 查询代发数量
     * @param supplyCommonId
     * @return
     */
    Integer getDealerGoodsNum(@Param("supplyCommonId") String supplyCommonId);

    /**
     * 查询代发信息
     * @param page
     * @param platFormAllGoodsDTO
     * @return
     */
    IPage<QueryDealerGoodsVO> platFormQueryDealerShop(IPage page,  @Param("param")PlatFormAllGoodsDTO platFormAllGoodsDTO);
}
