package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.payment.entity.PayAssetDetailsEntity;
import com.houjiang.payment.vo.AssetInfoVO;
import com.houjiang.payment.vo.AssetPlatformQueryExpVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.payment.entity.PayAssetDetails
 * @author Amos
 */
@Mapper
public interface PayAssetDetailsMapper extends BaseMapper<PayAssetDetailsEntity> {

    /**
     * 新增详情
     *
     * @param map
     * @return
     */
    boolean insertDetail(@Param("param") Map<Object, Object> map);

    /**
     * 查询集合
     *
     * @param page
     * @param map
     * @return
     */
    IPage<AssetInfoVO> queryAssetList(IPage<AssetInfoVO> page, @Param("param") Map<String, Object> map);


    /**
     * 查询详情
     *
     * @param detailId
     * @return
     */
    AssetInfoVO queryAssetDetail(@Param("detailId") Integer detailId);

    /**
     * 平台-明细
     *
     * @return
     */
    List<AssetPlatformQueryExpVO> queryExpPlatformAssetList(@Param("param") Map<Object, Object> map);


    /**
     * 新增用户详情
     *
     * @param assetDetailMap
     */
    void insertUserMoneyDetail(@Param("param") Map<String, Object> assetDetailMap);
}




