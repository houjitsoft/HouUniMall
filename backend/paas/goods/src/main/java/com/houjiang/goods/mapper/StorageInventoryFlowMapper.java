package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.dto.goods.StorageInventoryFlowDTO;
import com.houjiang.commons.dto.goods.StorageInventoryFlowGoodsDTO;
import com.houjiang.goods.entity.StorageInventoryFlowEntity;
import com.houjiang.goods.vo.StorageInventoryFlowExpVO;
import com.houjiang.goods.vo.StorageInventoryFlowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 仓库库存流水表(StorageInventoryFlow)表数据库访问层
 *
 * @author Amos
 * @since 2024-09-20 16:51:29
 */
@Mapper
public interface StorageInventoryFlowMapper extends BaseMapper<StorageInventoryFlowEntity> {

    /**
     * 查询出库/入库/调拨单列表
     * @param param 查询参数
     * @return 出库/入库/调拨单列表
     */
    IPage<StorageInventoryFlowVO> queryStorageInventoryFlow(Page<StorageInventoryFlowVO> page,@Param("param") StorageInventoryFlowDTO param);

    /**
     * 查询调拨单商品信息
     *
     * @param param 查询参数，包含调拨单的相关信息
     * @return 返回调拨单的商品信息及处理结果
     */
    StorageInventoryFlowVO queryTransferOrderGoods(@Param("param") StorageInventoryFlowGoodsDTO param);

    /**
     * 查询仓库库存流水导出列表
     * @param param 查询参数
     * @return 仓库库库存流水导出列表
     */
    List<StorageInventoryFlowExpVO> queryStorageInventoryFlowExp(@Param("param")  StorageInventoryFlowDTO param);
}
