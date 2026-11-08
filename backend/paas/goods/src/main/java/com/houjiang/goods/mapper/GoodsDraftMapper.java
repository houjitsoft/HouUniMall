package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.commons.dto.goods.MerchantShopGoodsCatDTO;
import com.houjiang.commons.vo.goods.ManageShopBaseQueryResultVO;
import com.houjiang.commons.vo.goods.ManageTransportAreaResultVO;
import com.houjiang.commons.vo.goods.ManageTransportTemplateResultVO;
import com.houjiang.goods.entity.GoodsDraftEntity;
import com.houjiang.goods.vo.ManageGdsCommonQueryResultStrVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName GoodsDraftMapper
 * @Description 草稿箱
 * @Author Amos
 * @Date 2023/8/11 9:21
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface GoodsDraftMapper extends BaseMapper<GoodsDraftEntity> {

    /**
     * @return 处理结果
     */
    List<ManageGdsCommonQueryResultStrVO> queryDraft(@Param("shopId")String shopId, @Param("catId")Integer catId);

    /**
     * @return 处理结果
     */
    ManageTransportAreaResultVO selectAreaById(@Param("transportAreaId")Integer transportAreaId);

    /**
     * @return 处理结果
     */
    ManageTransportTemplateResultVO selectTemplateById(@Param("transportTemplate")Integer transportTemplate);

    /**
     * @return 处理结果
     */
    List<MerchantShopGoodsCatDTO> queryShopGoodsCatList(@Param("goodsCatIdList") List<Integer> goodsCatIdList);

    /**
     * @return 处理结果
     */
    String queryShopGoodsCatName(@Param("goodsCatId") Integer goodsCatId);

    /**
     * @return 处理结果
     */
    Integer queryDraftCount(@Param("shopId")String shopId, @Param("catId")Integer catId);

    /**
     * @return 处理结果
     */
    ManageShopBaseQueryResultVO selectByShopId2(@Param("shopId") String shopId);

    /**
     * @return 处理结果
     */
    int insertDraft(@Param("shopId") String shopId,@Param("catId") Integer catId,@Param("draftName") String draftName,@Param("draftContent") String draftContent);

    /**
     * @return 处理结果
     */
    ManageShopBaseQueryResultVO selectBySupplierShopId(@Param("shopId") String shopId);
}
