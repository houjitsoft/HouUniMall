package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.goods.StorageVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.dto.StorageGoodsBaseDTO;
import com.houjiang.goods.entity.StorageGoodsBaseEntity;
import com.houjiang.goods.feign.BaseServiceClient;
import com.houjiang.goods.mapper.StorageGoodsBaseMapper;
import com.houjiang.goods.service.StorageGoodsBaseService;
import com.houjiang.goods.vo.StorageGoodsBaseVO;
import com.houjiang.goods.vo.StorageGoodsVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 仓库中商品信息表(StorageGoodsBase)表服务实现类
 *
 * @author Amos
 * @since 2024-09-19 14:06:57
 */
@Service
public class StorageGoodsBaseServiceImpl extends ServiceImpl<StorageGoodsBaseMapper, StorageGoodsBaseEntity>
    implements StorageGoodsBaseService {

    @Resource
    private BaseServiceClient baseServiceClient;

    /**
     * 查询库存列表
     *
     * @param param 查询参数对象，包含与库存商品相关的查询条件
     * @return 返回一个ResponseResult对象，其中包含库存商品的基础信息列表
     * <p>
     * 该方法首先从Token中解析出用户信息，以确定当前操作的商户
     * 然后将商户ID设置到查询参数对象中，这样可以确保查询特定商户的库存信息
     * 最后，调用基础映射器的方法来执行查询，并将查询结果封装到ResponseResult对象中返回
     */
    @Override
    public ResponseResult<List<StorageGoodsBaseVO>> queryStorageList(StorageGoodsBaseDTO param) {
        // 从Token中获取商户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置商户ID到查询参数中
        param.setShopId(Integer.valueOf(ui.getShopId()));
        // 执行查询操作，获取库存商品列表
        List<StorageGoodsBaseVO> list = baseMapper.queryStorageList(param);
        // 将查询结果封装成成功响应并返回
        return ResponseResult.success(list);
    }

    /**
     * 查询库存商品列表
     *
     * @param param 查询参数，包含商户ID等信息
     * @return 返回库存商品列表的响应结果
     */
    @Override
    public ResponseResult<List<StorageGoodsVO>> queryGoodsStorageList(StorageGoodsBaseDTO param) {
        // 从Token中获取商户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置商户ID到查询参数中
        param.setShopId(Integer.valueOf(ui.getShopId()));
        // 执行查询操作，获取库存商品列表
        List<StorageGoodsVO> list = baseMapper.queryGoodsStorageList(param);
        // 遍历商品列表，填充地址信息和商品详情
        for (StorageGoodsVO vo : list) {
            // 通过调用基础服务客户端，根据ID获取省、市、区的名称
            vo.setStorageProvinceName(baseServiceClient.getAddressById(vo.getStorageProvince()));
            vo.setStorageCityName(baseServiceClient.getAddressById(vo.getStorageCity()));
            vo.setStorageAreaName(baseServiceClient.getAddressById(vo.getStorageArea()));
            // 设置仓库ID到查询参数中，用于查询特定仓库的商品信息
            param.setStorageId(vo.getStorageId());
            // 执行查询操作，获取特定仓库的商品详情列表
            List<StorageGoodsBaseVO> storageGoodsList = baseMapper.queryStorageGoodsBaseList(param);
            // 将商品详情列表设置到商品对象中
            vo.setStorageGoodsList(storageGoodsList);
        }
        // 将查询结果封装成成功响应并返回
        return ResponseResult.success(list);
    }
    /**
     * 根据商品ID查询商品库存信息
     *
     * @param goodsId 商品ID，用于查询特定商品的库存信息
     * @return 返回库存信息列表如果找到对应的库存信息，则返回成功响应；
     *         否则，返回失败响应
     */
    @Override
    public ResponseResult<List<StorageVO>> queryGoodsStorageByGoodsId(Integer goodsId) {
        List<StorageVO> storageGoodsVOList = baseMapper.queryGoodsStorageByGoodsId(goodsId);
        if(storageGoodsVOList != null && storageGoodsVOList.size() > 0 ){
            return ResponseResult.success(storageGoodsVOList);
        }else{
            return ResponseResult.fail();
        }
    }
}
