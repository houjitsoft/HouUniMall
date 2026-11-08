package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.goods.dto.StorageInfoDTO;
import com.houjiang.goods.entity.StorageInfoEntity;
import com.houjiang.goods.vo.StorageInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品仓库表(StorageInfo)表数据库访问层
 *
 * @author Amos
 * @since 2024-09-19 14:06:31
 */
@Mapper
public interface StorageInfoMapper extends BaseMapper<StorageInfoEntity> {

    /**
     * 查询库存信息列表
     * @param page 分页对象
     * @param param 查询条件
     * @return 库存信息列表
     */
    IPage<StorageInfoVO> queryStorageInfoList(Page<StorageInfoVO> page,@Param("param") StorageInfoDTO param);

    /**
     * 根据仓库名称查询仓库id列表
     * @param storageName 仓库名称
     * @param shopId 店铺id
     * @return 仓库id列表
     */
    List<Integer> selectStorageIdListByStorageName(String storageName, Integer shopId);

    /**
     * 根据仓库id查询仓库名称
     * @param storageId 仓库id
     * @return 仓库名称
     */
    String selectStorageNameByStorageId(Integer storageId);
}
