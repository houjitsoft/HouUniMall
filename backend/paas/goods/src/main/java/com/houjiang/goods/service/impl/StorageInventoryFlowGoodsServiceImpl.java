package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.goods.StorageInventoryFlowDTO;
import com.houjiang.commons.dto.goods.StorageInventoryFlowGoodsDTO;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.vo.goods.PurchaseOrderVO;
import com.houjiang.commons.vo.goods.StorageInventoryFlowGoodsVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.entity.StorageInventoryFlowGoodsEntity;
import com.houjiang.goods.feign.OrderServiceClient;
import com.houjiang.goods.mapper.*;
import com.houjiang.goods.service.StorageInventoryFlowGoodsService;
import com.houjiang.goods.vo.InventoryFlowVO;
import com.houjiang.goods.vo.StorageGoodsStockVO;
import com.houjiang.goods.vo.StorageInventoryFlowVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 仓库库存流水商品表(StorageInventoryFlowGoods)表服务实现类
 *
 * @author Amos
 * @since 2024-09-20 16:51:54
 */
@Service
public class StorageInventoryFlowGoodsServiceImpl extends ServiceImpl<StorageInventoryFlowGoodsMapper, StorageInventoryFlowGoodsEntity>
    implements StorageInventoryFlowGoodsService {

    @Resource
    private GoodsBaseMapper goodsBaseMapper;

    @Resource
    private StorageInventoryFlowMapper storageInventoryFlowMapper;

    @Resource
    private StorageInfoMapper storageInfoMapper;

    @Resource
    private StorageGoodsBaseMapper storageGoodsBaseMapper;

    @Resource
    private OrderServiceClient orderServiceClient;

    /**
     * 查询库存流水商品列表
     *
     * @param param 查询参数，包含分页信息和筛选条件
     * @return 返回分页查询结果，包含库存流水商品的详细信息
     */
    @Override
    public ResponseResult<PageUtils<StorageInventoryFlowGoodsVO>> queryStorageInventoryFlowGoods(StorageInventoryFlowGoodsDTO param) {
        // 从token中获取店铺信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置查询参数中的店铺ID
        param.setShopId(Integer.valueOf(ui.getShopId()));
        // 根据分页参数创建Page对象
        Page<StorageInventoryFlowGoodsVO> page = new Page<>(param.getPage(), param.getLimit());
        // 调用Mapper层的查询方法，执行分页查询
        IPage<StorageInventoryFlowGoodsVO> result = goodsBaseMapper.queryStorageInventoryFlowGoods(page, param);
        // 遍历查询结果，设置额外的库存信息
        for (StorageInventoryFlowGoodsVO vo : result.getRecords()){
            setStorageGoodsStock(param, vo);
        }
        // 返回分页查询结果
        return ResponseResult.success(PageUtils.getPage(result));
    }


    /**
     * 查询调拨单商品信息
     *
     * @param param 查询参数，包含调拨单的相关信息
     * @return 返回调拨单的商品信息及处理结果
     */
    @Override
    public ResponseResult<StorageInventoryFlowVO> queryTransferOrderGoods(StorageInventoryFlowGoodsDTO param) {
        // 从token中获取店铺信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置查询参数中的店铺ID
        param.setShopId(Integer.valueOf(ui.getShopId()));
        // 调用持久层接口查询调拨单商品信息
        StorageInventoryFlowVO vo = storageInventoryFlowMapper.queryTransferOrderGoods(param);
        // 如果查询结果为空，直接返回成功响应
        if (vo == null){
            return ResponseResult.success();
        }
        // 设置仓库名称信息
        StorageInventoryFlowServiceImpl.setStorageName(ui, vo, storageInfoMapper);
        // 查询调拨单下的商品列表
        List<StorageInventoryFlowGoodsVO> goodsList = baseMapper.selectGoodsList(vo.getInventoryFlowId());
        // 为商品列表中的每个商品设置库存信息
        for (StorageInventoryFlowGoodsVO goodsVo : goodsList){
            param.setOutStorageId(goodsVo.getOutStorageId());
            param.setPutStorageId(goodsVo.getPutStorageId());
            setStorageGoodsStock(param, goodsVo);
        }
        // 设置查询结果中的商品列表
        vo.setGoodsList(goodsList);
        // 返回包含调拨单商品信息的响应
        return ResponseResult.success(vo);
    }


    /**
     * 查询采购单信息
     * 此方法用于查询给定采购单的商品信息，并根据店铺信息补充库存数据
     *
     * @param param 查询参数，包含商品流动信息
     * @return 包含采购单及商品详情的响应结果
     */
    @Override
    public ResponseResult<PurchaseOrderVO> queryPurchaseOrderGoods(StorageInventoryFlowGoodsDTO param) {
        // 从token中获取店铺信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置查询参数中的店铺ID
        param.setShopId(Integer.valueOf(ui.getShopId()));
        // 调用远程服务查询采购单商品信息
        PurchaseOrderVO vo = orderServiceClient.queryPurchaseOrderGoods(param);
        // 如果返回的结果为空，则直接返回成功响应
        if (vo == null){
            return ResponseResult.success();
        }
        // 获取采购单中的商品列表
        List<StorageInventoryFlowGoodsVO> goodsList = vo.getGoodsList();
        // 遍历商品列表，补充每个商品的信息
        for (StorageInventoryFlowGoodsVO goodsVo : goodsList){
            // 根据商品ID查询商品详细信息
            StorageInventoryFlowGoodsVO goods = goodsBaseMapper.selectGoodsInfo(goodsVo.getGoodsId());
            // 补充商品名称、规格、通用ID和单位
            goodsVo.setGoodsName(goods.getGoodsName());
            goodsVo.setGoodsSpec(goods.getGoodsSpec());
            goodsVo.setCommonId(goods.getCommonId());
            goodsVo.setDw(goods.getDw());
            // 设置商品的库存信息
            setStorageGoodsStock(param, goodsVo);
        }
        // 更新采购单中的商品列表
        vo.setGoodsList(goodsList);
        // 返回包含采购单及商品详情的响应结果
        return ResponseResult.success(vo);
    }


    /**
     * 设置库存货物的库存量
     * 该方法根据提供的参数更新货物的入库库存量和出库库存量
     *
     * @param param 包含货物入库和出库仓库ID的参数对象
     * @param goodsVo 货物信息对象，将被更新库存量
     */
    public void setStorageGoodsStock(StorageInventoryFlowGoodsDTO param, StorageInventoryFlowGoodsVO goodsVo) {
        // 如果入库仓库ID不为空，则设置入库库存量
        if(param.getPutStorageId() != null) {
            // 通过数据库查询获取入库库存量，并设置到货物信息对象中
            goodsVo.setPutStorageStock(storageGoodsBaseMapper.selectStorageGoodsStock(goodsVo.getGoodsId(), param.getPutStorageId()));
        }else {
            // 如果入库仓库ID为空，则将入库库存量设置为0
            goodsVo.setPutStorageStock(0);
        }
        // 如果出库仓库ID不为空，则设置出库库存量
        if(param.getOutStorageId() != null) {
            // 通过数据库查询获取出库库存量，并设置到货物信息对象中
            goodsVo.setOutStorageStock(storageGoodsBaseMapper.selectStorageGoodsStock(goodsVo.getGoodsId(), param.getOutStorageId()));
        }else {
            // 如果出库仓库ID为空，则将出库库存量设置为0
            goodsVo.setOutStorageStock(0);
        }
    }

    /**
     * 查询商品库存
     * 该方法用于根据给定的查询参数，查询特定仓库中各商品的库存信息
     * 通过调用数据库操作获取商品在不同仓库中的库存数量，并封装进列表返回
     *
     * @param param 查询参数对象，包含商品ID列表及可选的入库仓库ID和出库仓库ID
     * @return 包含商品库存信息列表的响应结果对象
     */
    @Override
    public ResponseResult<List<StorageGoodsStockVO>> queryStorageGoodsStock(StorageInventoryFlowGoodsDTO param) {
        // 从token中获取店铺信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置查询参数中的店铺ID
        param.setShopId(Integer.valueOf(ui.getShopId()));
        // 初始化列表存储查询结果
        List<StorageGoodsStockVO> list = new ArrayList<>();
        // 遍历查询参数中的商品ID列表
        for (Integer goodsId : param.getGoodsIdList()){
            // 初始化商品库存视图对象
            StorageGoodsStockVO vo = new StorageGoodsStockVO();
            // 设置商品ID
            vo.setGoodsId(goodsId);
            // 如果有指定的入库仓库ID，则查询该仓库中的商品库存，否则设置为0
            if(param.getPutStorageId() != null) {
                vo.setPutStorageStock(storageGoodsBaseMapper.selectStorageGoodsStock(goodsId, param.getPutStorageId()));
            }else {
                vo.setPutStorageStock(0);
            }
            // 如果有指定的出库仓库ID，则查询该仓库中的商品库存，否则设置为0
            if(param.getOutStorageId() != null) {
                vo.setOutStorageStock(storageGoodsBaseMapper.selectStorageGoodsStock(goodsId, param.getOutStorageId()));
            }else {
                vo.setOutStorageStock(0);
            }
            // 将商品库存信息添加到结果列表中
            list.add(vo);
        }
        // 返回查询结果
        return ResponseResult.success(list);
    }


    /**
     * 查询库存流水列表
     * 该方法用于根据提供的查询参数，检索与库存变动相关的流水记录，并返回分页的结果
     * 它包含了设置查询条件、执行查询、以及对查询结果的处理等过程
     *
     * @param param 查询参数对象，包含了库存流水的查询条件和分页信息
     * @return 包含分页库存流水数据的响应结果对象
     */
    @Override
    public ResponseResult<PageUtils<InventoryFlowVO>> queryInventoryFlowList(StorageInventoryFlowDTO param) {
        // 从token中获取店铺信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置查询参数中的店铺ID
        param.setShopId(Integer.valueOf(ui.getShopId()));
        // 如果查询条件中包含了仓库名称，则进一步获取仓库ID列表
        if(!StringUtils.isEmpty(param.getStorageName())) {
            List<Integer> storageIdList = storageInfoMapper.selectStorageIdListByStorageName(param.getStorageName(), param.getShopId());
            if(storageIdList.isEmpty()){
                return ResponseResult.success(PageUtils.getPage(new Page<>()));
            }
            param.setStorageIdList(storageIdList);
        }
        if(param.getStartTime() != null && param.getEndTime() != null){
            param.setStartTime(DateUtils.parseDateStartTime(param.getStartTime()));
            param.setEndTime(DateUtils.parseDateEndTime(param.getEndTime()));
        }
        // 初始化分页对象，准备进行分页查询
        Page<InventoryFlowVO> page = new Page<>(param.getPage(), param.getLimit());
        // 执行库存流水查询，并获取结果
        IPage<InventoryFlowVO> result = baseMapper.queryInventoryFlowList(page, param);
        // 遍历查询结果，为每个库存流水项添加额外处理
        for (InventoryFlowVO vo : result.getRecords()){
            // 通过商品ID查询总库存，并设置到商品对象中
            vo.setTotalStorageStock(storageGoodsBaseMapper.selectTotalStorageStock(vo.getGoodsId()));
            // 根据类型详情判断库存变动的方向，增加或减少
            List<Integer> addList = Arrays.asList(1,2,3,4);
            if(addList.contains(vo.getTypeDetails())){
                vo.setGoodsNum("+" + vo.getGoodsNum());
            } else {
                vo.setGoodsNum("-" + vo.getGoodsNum());
            }
            // 设置仓库名称
            vo.setPutStorageName(storageInfoMapper.selectStorageNameByStorageId(vo.getPutStorageId()));
            vo.setOutStorageName(storageInfoMapper.selectStorageNameByStorageId(vo.getOutStorageId()));
        }
        // 返回分页处理后的库存流水数据
        return ResponseResult.success(PageUtils.getPage(result));
    }


    /**
     * 导出库存流水列表
     *
     * @param param 查询参数对象，用于查询库存流水信息
     * @return 返回包含导出Excel文件URL的响应结果
     */
    @Override
    public ResponseResult<String> exportInventoryFlowList(StorageInventoryFlowDTO param) {
        // 定义导出表格的标题
        String[] title = {"编号", "商品名称", "出入库类型", "出入库", "在仓剩余库存", "提交时间", "操作人"};
        // 初始化数据列表
        List<Map<String, String>> list = Lists.newArrayList();
        // 查询库存流水列表
        ResponseResult<PageUtils<InventoryFlowVO>> resp = queryInventoryFlowList(param);
        List<InventoryFlowVO> result = (List<InventoryFlowVO>) resp.getData().getList();
        // 遍历查询结果，组装数据
        for (InventoryFlowVO item : result){
            HashMap<String, String> map = new HashMap<>();
            // 填充商品信息到数据列表
            map.put("编号", item.getInventoryFlowId());
            map.put("商品名称", item.getGoodsName());
            // 根据类型代码获取出入库类型的描述
            map.put("出入库类型", CommonType.inventoryFlowTypeDetail.getDescByCode(item.getTypeDetails()));
            map.put("出入库", item.getGoodsNum());
            // 查询并设置商品的实际总库存
            map.put("实际总库存", storageGoodsBaseMapper.selectTotalStorageStock(item.getGoodsId()) + "");
            map.put("提交时间", DateUtils.formateForSdfCn(item.getCreateTime()));
            map.put("操作人", item.getOperatorName());
            // 将组装好的数据添加到列表
            list.add(map);
        }
        // 构建并返回导出的Excel文件URL
        Map<String, List<Map<String, String>>> map = Maps.newHashMap();
        map.put("数据", list);
        String url = ExcelsUtil.createExcel(title, map, new int[]{0});
        return ResponseResult.success(url);
    }
}
