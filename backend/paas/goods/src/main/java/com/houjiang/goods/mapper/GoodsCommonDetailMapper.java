package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.commons.vo.goods.GdbMerchantGoodsCommonDetailVO;
import com.houjiang.commons.dto.goods.GdbMerchantGoodsCommonDetailDTO;
import com.houjiang.goods.entity.GoodsCommonDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName GoodsCommonDetailMapper
 * @Description 商品公共内容详情表
 * @Author Amos
 * @Date 2023-05-25 17:51:41
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface GoodsCommonDetailMapper extends BaseMapper<GoodsCommonDetailEntity> {
    /**
     * 查询商品详情
      * @param commonsId
     * @return
     */
    GdbMerchantGoodsCommonDetailVO selectByPrimaryKey(@Param("commonId")Integer commonsId);


    /**
     * 删除goods_common_detail商品
     * @param commonIds
     * @return
     */
    int deleteByPrimaryKey(@Param("commonIds")List<Integer> commonIds);

    /**
     * 添加商品详情
     * @param record
     * @return
     */
    int insertSelective(GdbMerchantGoodsCommonDetailDTO record);

    /**
     * 修改商品详情
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(GdbMerchantGoodsCommonDetailDTO record);

    /**
     * 插入商品详情
     * @param commonDetail
     */
    void insertSelective2(GdbMerchantGoodsCommonDetailVO commonDetail);
}
