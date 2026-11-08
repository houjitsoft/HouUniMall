package com.houjiang.goods.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.dto.StorageInfoDTO;
import com.houjiang.goods.entity.StorageGoodsBaseEntity;
import com.houjiang.goods.entity.StorageInfoEntity;
import com.houjiang.goods.feign.BaseServiceClient;
import com.houjiang.goods.mapper.StorageGoodsBaseMapper;
import com.houjiang.goods.mapper.StorageInfoMapper;
import com.houjiang.goods.service.StorageInfoService;
import com.houjiang.goods.vo.StorageInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品仓库表(StorageInfo)表服务实现类
 *
 * @author Amos
 * @since 2024-09-19 14:06:31
 */
@Service
public class StorageInfoServiceImpl extends ServiceImpl<StorageInfoMapper, StorageInfoEntity>
        implements StorageInfoService {

    @Resource
    private BaseServiceClient baseServiceClient;

    @Resource
    private StorageGoodsBaseMapper storageGoodsBaseMapper;

    /**
     * 查询库存信息列表
     *
     * @param param 查询参数对象，包含库存信息查询所需的条件
     * @return 包含分页库存信息的结果对象
     */
    @Override
    public ResponseResult<PageUtils<StorageInfoVO>> queryStorageInfoList(StorageInfoDTO param) {
        // 从token中获取店铺信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置查询参数中的店铺ID
        param.setShopId(Integer.valueOf(ui.getShopId()));
        // 创建分页对象，指定当前页和每页数量
        Page<StorageInfoVO> page = new Page<>(param.getPage(), param.getLimit());
        // 调用baseMapper查询库存信息列表，并应用分页和查询条件
        IPage<StorageInfoVO> result = baseMapper.queryStorageInfoList(page, param);
        // 遍历查询结果，为每个库存信息对象填充省、市、区名称
        for (StorageInfoVO item : result.getRecords()) {
            item.setStorageProvinceName(baseServiceClient.getAddressById(item.getStorageProvince()));
            item.setStorageCityName(baseServiceClient.getAddressById(item.getStorageCity()));
            item.setStorageAreaName(baseServiceClient.getAddressById(item.getStorageArea()));
        }
        // 返回分页查询结果
        return ResponseResult.success(PageUtils.getPage(result));
    }

    /**
     * 更新库存信息
     *
     * @param param 库存信息DTO参数，包含需要更新的库存信息
     * @return 返回操作结果，成功或失败
     */
    @Override
    public ResponseResult updateStorageInfo(StorageInfoDTO param) {
        // 从token中解析店铺信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置店铺ID，确保库存信息关联正确的店铺
        param.setShopId(Integer.valueOf(ui.getShopId()));

        // 创建实体对象，用于数据库操作
        StorageInfoEntity entity = new StorageInfoEntity();
        // 将DTO中的属性值复制到实体对象
        BeanUtils.copyProperties(param, entity);

        int count;
        // 判断是否存在存储ID，决定是更新还是插入操作
        if (param.getStorageId() != null) {
            // 更新操作，设置最新的修改时间
            entity.setUpdateTime(DateUtils.getNowDateToDf());
            // 执行更新操作
            count = baseMapper.updateById(entity);
        } else {
            // 插入操作，生成唯一的库存代码
            entity.setStorageCode("P" + DateUtils.nowtime() + RandomUtil.randomInt(4));
            // 设置创建时间
            entity.setCreateTime(DateUtils.getNowDateToDf());
            // 执行插入操作
            count = baseMapper.insert(entity);
        }

        // 根据操作结果返回成功或失败的响应
        return count > 0 ? ResponseResult.success() : ResponseResult.fail();
    }


    /**
     * 删除仓储信息
     *
     * @param param 仓储信息DTO，包含仓储ID
     * @return 删除成功返回成功响应结果，否则返回失败响应结果
     */
    @Override
    public ResponseResult deleteStorageInfo(StorageInfoDTO param) {
        // 查询仓储下关联的商品数量
        Integer goodsCount = Math.toIntExact(storageGoodsBaseMapper.selectCount(new LambdaQueryWrapper<StorageGoodsBaseEntity>()
                .eq(StorageGoodsBaseEntity::getStorageId, param.getStorageId())));
        // 如果仓储下有商品，则不允许删除
        if (goodsCount > 0) {
            return ResponseResult.fail(BizCodeEnume.HAS_STORAGE_GOODS);
        }
        // 执行删除操作
        int count = baseMapper.deleteById(param.getStorageId());
        // 根据删除结果返回相应响应
        return count > 0 ? ResponseResult.success() : ResponseResult.fail();
    }


    /**
     * 查询仓库信息列表并生成Excel文件
     *
     * @param param 查询参数对象，包含分页和筛选条件
     * @return 响应结果，包含生成的Excel文件URL
     */
    @Override
    public ResponseResult<String> queryListExp(StorageInfoDTO param) {
        // 从token中获取店铺信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置查询参数中的店铺ID
        param.setShopId(Integer.valueOf(ui.getShopId()));

        // 定义导出表格的标题
        String[] title = {"仓库编码", "仓库名称", "仓库地址", "仓库联系人", "联系电话", "状态", "创建时间", "修改时间"};

        // 初始化数据列表
        List<Map<String, String>> list = Lists.newArrayList();

        // 创建分页对象并设置分页参数
        Page<StorageInfoVO> page = new Page<>(param.getPage(), param.getLimit());

        // 调用Mapper方法查询仓库信息列表
        IPage<StorageInfoVO> result = baseMapper.queryStorageInfoList(page, param);

        // 遍历查询结果，组装数据
        for (StorageInfoVO item : result.getRecords()) {
            HashMap<String, String> map = new HashMap<>();

            // 拼接完整的仓库地址
            String address = baseServiceClient.getAddressString(item.getStorageProvince(), item.getStorageCity(), item.getStorageArea()).getData().toString();

            // 将各个字段信息添加到map中
            map.put("仓库编码", item.getStorageCode());
            map.put("仓库名称", item.getStorageName());
            map.put("仓库地址", address + item.getStorageAddress());
            map.put("仓库联系人", item.getStorageContacts());
            map.put("联系电话", item.getStorageMobile());
            map.put("状态", item.getStorageStatus() == 1 ? "启用" : "禁用");
            map.put("创建时间", DateUtils.formateForSdfCn(item.getCreateTime()));
            map.put("修改时间", DateUtils.formateForSdfCn(item.getUpdateTime()));

            // 将map添加到列表中
            list.add(map);
        }

        // 构建并返回导出的Excel文件URL
        Map<String, List<Map<String, String>>> map = Maps.newHashMap();
        map.put("数据", list);

        String url = ExcelsUtil.createExcel(title, map, new int[]{0});
        return ResponseResult.success(url);
    }

    @Override
    public ResponseResult<StorageInfoVO> queryDetail(String storageId) {
        StorageInfoVO result = new StorageInfoVO();
        // 调用baseMapper查询库存信息列表，并应用分页和查询条件
        StorageInfoEntity entity = baseMapper.selectById(storageId);
        // 为库存信息对象填充省、市、区名称
        BeanUtils.copyProperties(entity, result);
        result.setStorageProvinceName(baseServiceClient.getAddressById(result.getStorageProvince()));
        result.setStorageCityName(baseServiceClient.getAddressById(result.getStorageCity()));
        result.setStorageAreaName(baseServiceClient.getAddressById(result.getStorageArea()));
        // 返回分页查询结果
        return ResponseResult.success(result);
    }
}
