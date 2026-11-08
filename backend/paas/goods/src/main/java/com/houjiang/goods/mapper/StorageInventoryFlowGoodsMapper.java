package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.dto.goods.StorageInventoryFlowDTO;
import com.houjiang.commons.vo.goods.StorageInventoryFlowGoodsVO;
import com.houjiang.goods.entity.StorageInventoryFlowGoodsEntity;
import com.houjiang.goods.vo.InventoryFlowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 仓库库存流水商品表(StorageInventoryFlowGoods)表数据库访问层
 *
 * @author Amos
 * @since 2024-09-20 16:51:54
 */
@Mapper
public interface StorageInventoryFlowGoodsMapper extends BaseMapper<StorageInventoryFlowGoodsEntity> {

    /**
     * 查询库存流水关联的商品列表
     * @param inventoryFlowId 库存流水id
     * @return 商品列表
     */
    List<StorageInventoryFlowGoodsVO> selectGoodsList(String inventoryFlowId);


    /**
     * 查询库存流水列表
     * @param page 分页对象
     * @param param 筛选条件
     * @return 库存流水列表
     */
    IPage<InventoryFlowVO> queryInventoryFlowList(Page<InventoryFlowVO> page,
                                                  @Param("param") StorageInventoryFlowDTO param);
}
