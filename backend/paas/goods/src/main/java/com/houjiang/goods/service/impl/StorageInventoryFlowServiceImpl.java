package com.houjiang.goods.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.goods.StorageInventoryFlowDTO;
import com.houjiang.commons.dto.goods.StorageInventoryFlowGoodsDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.vo.goods.StorageInventoryFlowGoodsVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.entity.*;
import com.houjiang.goods.mapper.*;
import com.houjiang.goods.service.StorageInventoryFlowService;
import com.houjiang.goods.vo.StorageInventoryFlowExpVO;
import com.houjiang.goods.vo.StorageInventoryFlowVO;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

;

/**
 * 仓库库存流水表(StorageInventoryFlow)表服务实现类
 *
 * @author Amos
 * @since 2024-09-20 16:51:29
 */
@Service
public class StorageInventoryFlowServiceImpl extends ServiceImpl<StorageInventoryFlowMapper, StorageInventoryFlowEntity>
        implements StorageInventoryFlowService {

    @Resource
    private StorageGoodsBaseMapper storageGoodsBaseMapper;

    @Resource
    private StorageInventoryFlowGoodsMapper storageInventoryFlowGoodsMapper;

    @Resource
    private GoodsBaseMapper goodsBaseMapper;

    @Resource
    private StorageInfoMapper storageInfoMapper;

    @Resource
    private StorageInventoryFlowGoodsServiceImpl storageInventoryFlowGoodsService;

    /**
     * 插入出库/入库/调拨单接口
     * <p>
     * 通过POST请求接收客户端发送的库存流水信息，并委托服务层插入数据库
     *
     * @param param 经过校验的库存流水数据传输对象，用于接收客户端请求参数
     * @return ResponseResult 包含插入结果的响应对象
     */
    @Override
    public ResponseResult insertStorageInventoryFlow(StorageInventoryFlowDTO param) {
        Integer shopId = null;
        String userName = null;
        String userId = null;
        // 从token中获取店铺信息
        if(StringUtils.isNotBlank(param.getToken())){
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
            // 获取店铺ID，并设置到入库流水DTO中
            shopId = StringUtils.isEmpty(ui.getShopId()) ? param.getShopId() : Integer.valueOf(ui.getShopId());
            if(shopId == null){
                return ResponseResult.fail(BizCodeEnume.SHOP_ID_IS_NULL);
            }
            userName = ui.getUserName();
            userId = ui.getUserId();
        }else{
            shopId = param.getShopId();
            userName = param.getUserName();
            userId = param.getUserId();

        }

        param.setShopId(shopId);
        // 获取当前时间
        Date now = DateUtils.getNowDate();
        // 创建实体对象并复制属性
        StorageInventoryFlowEntity entity = new StorageInventoryFlowEntity();
        BeanUtils.copyProperties(param, entity);
        // 根据类型设置流水ID前缀
        if (param.getType() == 1) {
            entity.setInventoryFlowId("RK" + DateUtils.dateTimeNow() + RandomUtil.randomInt(2));
        } else if (param.getType() == 2) {
            entity.setInventoryFlowId("CK" + DateUtils.dateTimeNow() + RandomUtil.randomInt(2));
        } else {
            entity.setInventoryFlowId("DB" + DateUtils.dateTimeNow() + RandomUtil.randomInt(2));
        }
        // 初始化入库数量
        Integer inventoryFlowNum = 0;
        // 遍历商品列表，插入入库流水商品信息
        for (StorageInventoryFlowGoodsDTO goods : param.getGoodsList()) {
            StorageInventoryFlowGoodsEntity goodsEntity = new StorageInventoryFlowGoodsEntity();
            BeanUtils.copyProperties(goods, goodsEntity);
            // 设置关联的入库流水ID和店铺ID
            goodsEntity.setInventoryFlowId(entity.getInventoryFlowId());
            goodsEntity.setShopId(shopId);
            // 设置创建时间
            goodsEntity.setCreateTime(now);
            storageInventoryFlowGoodsMapper.insert(goodsEntity);
            // 累加入库数量
            inventoryFlowNum += goods.getGoodsNum();
            // 更新库存
            updateStorageGoodsStock(param, shopId, goods, now);
        }
        // 设置总入库数量、操作员信息和时间
        entity.setInventoryFlowNum(inventoryFlowNum);
        entity.setOperatorId(userId);
        entity.setOperatorName(userName);
        entity.setOperatorTime(now);
        entity.setCreateTime(now);
        // 插入入库流水主表记录
        baseMapper.insert(entity);
        // 返回成功结果
        return ResponseResult.success();
    }

    /**
     * 更新仓库商品库存
     * 根据不同的操作类型（入库/出库），更新商品在相应仓库的库存数量
     *
     * @param param 入库/出库操作的相关信息，包含操作类型、仓库ID等
     * @param shopId 店铺ID，用于定位特定店铺的库存信息
     * @param goods 商品信息，包含商品ID和数量等
     * @param now 当前时间，用于更新库存记录的时间戳
     */
    private void updateStorageGoodsStock(StorageInventoryFlowDTO param, Integer shopId, StorageInventoryFlowGoodsDTO goods, Date now) {
        GoodsBaseEntity goodsEntity = goodsBaseMapper.selectOne(new LambdaQueryWrapper<GoodsBaseEntity>()
                .select(GoodsBaseEntity::getCommonId)
                .eq(GoodsBaseEntity::getGoodsId, goods.getGoodsId()));
        // 入库操作
        if (param.getType() == 1) {
            // 查询商品在目标仓库的当前库存信息
            StorageGoodsBaseEntity storageGoodsEntity = storageGoodsBaseMapper.selectOne(new LambdaQueryWrapper<StorageGoodsBaseEntity>()
                    .eq(StorageGoodsBaseEntity::getGoodsId, goods.getGoodsId())
                    .eq(StorageGoodsBaseEntity::getShopId, shopId)
                    .eq(StorageGoodsBaseEntity::getStorageId, param.getPutStorageId()));
            if (storageGoodsEntity != null) {
                // 商品已存在，更新库存数量和最后更新时间
                storageGoodsBaseMapper.update(null, new LambdaUpdateWrapper<StorageGoodsBaseEntity>()
                        .set(StorageGoodsBaseEntity::getStorageStock, storageGoodsEntity.getStorageStock() + goods.getGoodsNum())
                        .set(StorageGoodsBaseEntity::getUpdateTime, now)
                        .eq(StorageGoodsBaseEntity::getGoodsId, goods.getGoodsId())
                        .eq(StorageGoodsBaseEntity::getShopId, shopId)
                        .eq(StorageGoodsBaseEntity::getStorageId, param.getPutStorageId()));
            } else {
                // 商品不存在，创建新商品库存记录并插入数据库
                storageGoodsEntity = new StorageGoodsBaseEntity();
                storageGoodsEntity.setStorageStock(goods.getGoodsNum());
                storageGoodsEntity.setShopId(shopId);
                storageGoodsEntity.setStorageId(param.getPutStorageId());
                storageGoodsEntity.setGoodsId(goods.getGoodsId());
                storageGoodsEntity.setCommonId(goodsEntity.getCommonId());
                storageGoodsEntity.setCreateTime(now);
                storageGoodsBaseMapper.insert(storageGoodsEntity);
            }
        } else if (param.getType() == 2) {
            // 出库操作
            // 查询商品在源仓库的当前库存信息
            StorageGoodsBaseEntity storageGoodsEntity = storageGoodsBaseMapper.selectOne(new LambdaQueryWrapper<StorageGoodsBaseEntity>()
                    .eq(StorageGoodsBaseEntity::getGoodsId, goods.getGoodsId())
                    .eq(StorageGoodsBaseEntity::getShopId, shopId)
                    .eq(StorageGoodsBaseEntity::getStorageId, param.getOutStorageId()));
            if (storageGoodsEntity != null) {
                // 商品已存在，更新库存数量和最后更新时间
                storageGoodsBaseMapper.update(null, new LambdaUpdateWrapper<StorageGoodsBaseEntity>()
                        .set(StorageGoodsBaseEntity::getStorageStock, storageGoodsEntity.getStorageStock() - goods.getGoodsNum())
                        .set(StorageGoodsBaseEntity::getUpdateTime, now)
                        .eq(StorageGoodsBaseEntity::getGoodsId, goods.getGoodsId())
                        .eq(StorageGoodsBaseEntity::getShopId, shopId)
                        .eq(StorageGoodsBaseEntity::getStorageId, param.getOutStorageId()));
            } else {
                // 商品不存在，抛出异常
                throw new DefinedException(BizCodeEnume.STORAGE_NOT_HAS_GOODS.getMsg() + ": " + param.getGoodsName());
            }
        }
    }

    /**
     * 查询出库/入库/调拨单列表
     * <p>
     * 通过POST请求接收客户端的查询请求，并将查询参数进行校验和解析，最终调用服务层方法进行库存流水的查询操作
     *
     * @param param 请求体，包含查询条件和分页参数，经过Select组的校验
     * @return 返回库存流水的分页查询结果，封装在ResponseResult中
     * 该方法首先将HTTP请求中的JSON数据解析为StorageInventoryFlowDTO对象，然后调用storageInventoryFlowService
     * 的queryStorageInventoryFlow方法执行查询操作，将查询到的库存流水分页结果封装后返回给客户端
     */
    @Override
    public ResponseResult<PageUtils<StorageInventoryFlowVO>> queryStorageInventoryFlow(StorageInventoryFlowDTO param) {
        // 从token中获取店铺信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置查询参数中的店铺ID
        param.setShopId(Integer.valueOf(ui.getShopId()));

        // 根据商品名称查询商品ID列表
        if(!StringUtils.isEmpty(param.getGoodsName())) {
            List<Integer> goodsIdList = goodsBaseMapper.selectGoodsIdListByGoodsName(param.getGoodsName(), param.getShopId());
            param.setGoodsIdList(goodsIdList);
        }

        // 根据仓库名称查询仓库ID列表
        if(!StringUtils.isEmpty(param.getStorageName())) {
            List<Integer> storageIdList = storageInfoMapper.selectStorageIdListByStorageName(param.getStorageName(), param.getShopId());
            if(storageIdList.isEmpty()){
                return ResponseResult.success(PageUtils.getPage(new Page<>()));
            }
            param.setStorageIdList(storageIdList);
        }

        // 创建分页对象
        Page<StorageInventoryFlowVO> page = new Page<>(param.getPage(),param.getLimit());
        // 执行查询操作
        IPage<StorageInventoryFlowVO> result = baseMapper.queryStorageInventoryFlow(page, param);

        // 为查询结果设置仓库名称
        for (StorageInventoryFlowVO vo : result.getRecords()){
            setStorageName(ui, vo, storageInfoMapper);
        }

        // 返回分页查询结果
        return ResponseResult.success(PageUtils.getPage(result));
    }


    public static void setStorageName(UserInfoVO ui, StorageInventoryFlowVO vo, StorageInfoMapper storageInfoMapper) {
        if(vo.getOutStorageId() != null){
            String storageName = storageInfoMapper.selectOne(new LambdaQueryWrapper<StorageInfoEntity>()
                    .select(StorageInfoEntity::getStorageName)
                    .eq(StorageInfoEntity::getStorageId, vo.getOutStorageId())
                    .eq(StorageInfoEntity::getShopId, ui.getShopId())).getStorageName();
            vo.setOutStorageName(storageName);
        }
        if(vo.getPutStorageId() != null){
            String storageName = storageInfoMapper.selectOne(new LambdaQueryWrapper<StorageInfoEntity>()
                    .select(StorageInfoEntity::getStorageName)
                    .eq(StorageInfoEntity::getStorageId, vo.getPutStorageId())
                    .eq(StorageInfoEntity::getShopId, ui.getShopId())).getStorageName();
            vo.setPutStorageName(storageName);
        }
    }

    /**
     * 查询出库/入库/调拨单详情
     * <p>
     * 通过POST请求接收客户端发送的库存流水查询参数，以查询特定的库存流水详情
     * 此方法委托给storageInventoryFlowService中的queryDetailsInventoryFlow方法来处理具体的业务逻辑
     *
     * @param param 包含查询条件的对象，遵循Select组的验证规则，确保传入的参数符合预期格式
     * @return 返回包含库存流水详细信息的响应结果，使用StorageInventoryFlowVO作为返回类型
     */
    @Override
    public ResponseResult<StorageInventoryFlowVO> queryDetailsInventoryFlow(StorageInventoryFlowDTO param) {
        // 从token中获取店铺信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置查询参数中的店铺ID
        param.setShopId(Integer.valueOf(ui.getShopId()));
        // 根据库存流水ID和店铺ID查询库存流水实体
        StorageInventoryFlowEntity entity = baseMapper.selectOne(new LambdaQueryWrapper<StorageInventoryFlowEntity>()
                .eq(StorageInventoryFlowEntity::getInventoryFlowId, param.getInventoryFlowId())
                .eq(StorageInventoryFlowEntity::getShopId, param.getShopId()));
        // 将实体转换为视图对象
        StorageInventoryFlowVO vo = new StorageInventoryFlowVO();
        BeanUtils.copyProperties(entity, vo);
        // 设置库存名称
        setStorageName(ui, vo, storageInfoMapper);
        // 查询库存流水关联的商品列表
        List<StorageInventoryFlowGoodsVO> goodsList = storageInventoryFlowGoodsMapper.selectGoodsList(vo.getInventoryFlowId());
        // 为每个商品设置库存和库存变动信息
        goodsList.forEach(goods -> {
            // 准备商品库存信息查询参数
            StorageInventoryFlowGoodsDTO dto = new StorageInventoryFlowGoodsDTO(goods.getOutStorageId(), goods.getPutStorageId());
            storageInventoryFlowGoodsService.setStorageGoodsStock(dto, goods);
        });
        // 将商品列表设置到视图对象
        vo.setGoodsList(goodsList);
        // 返回查询结果
        return ResponseResult.success(vo);
    }


    /**
     * 导出入库存流水的列表
     *
     * @param param 包含仓储库存流水支出详细信息的请求体
     * @return 包含操作结果的响应对象
     */
    @Override
    public ResponseResult<String> putStorageInventoryFlowExp(StorageInventoryFlowDTO param) {
        // 定义报表标题列
        String[] title = {"入库编号", "入库类型", "入库仓库", "入库时间", "入库商品", "规格", "采购数量", "入库数量", "入库总数", "备注", "提交时间", "操作人"};
        // 创建用于存储报表数据的列表
        List<Map<String, String>> list = Lists.newArrayList();
        // 设置查询参数中的入库类型为入库
        param.setType(CommonType.inventoryFlowType.PUT_STORAGE.getCode());
        // 从token中获取店铺信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置查询参数中的店铺ID
        param.setShopId(Integer.valueOf(ui.getShopId()));
        // 查询入库流水明细数据
        List<StorageInventoryFlowExpVO> result = baseMapper.queryStorageInventoryFlowExp(param);
        // 遍历查询结果，补充缺失的信息并整理数据格式
        for (StorageInventoryFlowExpVO vo : result){
            // 如果入库仓库ID存在，则查询对应的入库仓库名称
            if(vo.getPutStorageId() != null){
                String storageName = storageInfoMapper.selectOne(new LambdaQueryWrapper<StorageInfoEntity>()
                        .select(StorageInfoEntity::getStorageName)
                        .eq(StorageInfoEntity::getStorageId, vo.getPutStorageId())).getStorageName();
                vo.setPutStorageName(storageName);
            }
            // 创建一个map来存储单行报表数据
            HashMap<String, String> map = new HashMap<>();
            // 将各个字段信息添加到map中
            map.put("入库编号", vo.getInventoryFlowId());
            map.put("入库类型", CommonType.inventoryFlowTypeDetail.getDescByCode(vo.getTypeDetails()));
            map.put("入库仓库", vo.getPutStorageName());
            map.put("入库时间", DateUtils.formateForSdfCn(vo.getStartTime()));
            map.put("入库商品", vo.getGoodsName());
            map.put("规格", vo.getGoodsSpec());
            map.put("采购数量", vo.getBuyNum() + "");
            map.put("入库数量", vo.getGoodsNum() + "");
            map.put("入库总数", vo.getInventoryFlowNum() + "");
            map.put("备注", vo.getNotes() == null ? "" : vo.getNotes());
            map.put("提交时间", DateUtils.formateForSdfCn(vo.getCreateTime()));
            map.put("操作人", vo.getOperatorName());
            // 将map添加到列表中
            list.add(map);
        }
        // 创建一个HashMap，用于存储键值对形式的数据集合
        Map<String, List<Map<String, String>>> map = Maps.newHashMap();
        // 将给定的数据列表添加到map中，键为"数据"
        map.put("数据", list);
        // 使用提供的标题、数据map和指定的列宽格式创建Excel，并返回Excel文件的下载URL
        String url = ExcelsUtil.createExcel(title, map, new int[]{0,1,2,3,9,10,11,12});
        // 返回一个成功的响应结果，将生成的Excel文件下载URL作为结果内容
        return ResponseResult.success(url);
    }

    /**
     * 出库库存流水导出方法
     *
     * @param param 库存流水查询参数
     * @return 返回导出库存流水的结果
     */
    @Override
    public ResponseResult<String> outStorageInventoryFlowExp(StorageInventoryFlowDTO param) {
        // 定义导出表格的标题行
        String[] title = {"出库编号", "出库类型", "出库仓库", "出库时间", "出库商品", "规格", "出库数量", "出库总数", "备注", "提交时间", "操作人"};
        // 初始化存储导出数据的列表
        List<Map<String, String>> list = Lists.newArrayList();
        // 设置查询条件为出库类型
        param.setType(CommonType.inventoryFlowType.OUT_STORAGE.getCode());
        // 从token中获取店铺信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置查询参数中的店铺ID
        param.setShopId(Integer.valueOf(ui.getShopId()));
        // 执行查询出库库存流水的操作
        List<StorageInventoryFlowExpVO> result = baseMapper.queryStorageInventoryFlowExp(param);
        // 遍历查询结果，组装导出数据
        for (StorageInventoryFlowExpVO vo : result){
            // 如果出库仓库ID存在，则查询对应的仓库名称
            if(vo.getOutStorageId() != null){
                String storageName = storageInfoMapper.selectOne(new LambdaQueryWrapper<StorageInfoEntity>()
                        .select(StorageInfoEntity::getStorageName)
                        .eq(StorageInfoEntity::getStorageId, vo.getOutStorageId())).getStorageName();
                vo.setOutStorageName(storageName);
            }
            // 创建一个map来存储单行导出数据
            HashMap<String, String> map = new HashMap<>();
            // 将各个字段信息添加到map中
            map.put("出库编号", vo.getInventoryFlowId());
            map.put("出库类型", CommonType.inventoryFlowTypeDetail.getDescByCode(vo.getTypeDetails()));
            map.put("出库仓库", vo.getOutStorageName());
            map.put("出库时间", DateUtils.formateForSdfCn(vo.getEndTime()));
            map.put("出库商品", vo.getGoodsName());
            map.put("规格", vo.getGoodsSpec());
            map.put("出库数量", vo.getGoodsNum() + "");
            map.put("出库总数", vo.getInventoryFlowNum() + "");
            map.put("备注", vo.getNotes() == null ? "" : vo.getNotes());
            map.put("提交时间", DateUtils.formateForSdfCn(vo.getCreateTime()));
            map.put("操作人", vo.getOperatorName());
            // 将map添加到列表中
            list.add(map);
        }
        // 创建一个HashMap，用于存储键值对形式的数据集合
        Map<String, List<Map<String, String>>> map = Maps.newHashMap();
        // 将给定的数据列表添加到map中，键为"数据"
        map.put("数据", list);
        // 使用提供的标题、数据map和指定的列宽格式创建Excel，并返回Excel文件的下载URL
        String url = ExcelsUtil.createExcel(title, map, new int[]{0,1,2,3,8,9,10,11});
        // 返回一个成功的响应结果，将生成的Excel文件下载URL作为结果内容
        return ResponseResult.success(url);
    }

    /**
     * 导出调拨库存流水的列表
     *
     * @param param 包含流转信息的参数对象，描述了库存的变化细节
     * @return 返回操作结果的响应对象，包含操作状态和可能的消息
     */
    @Override
    public ResponseResult<String> transferStorageInventoryFlowExp(StorageInventoryFlowDTO param) {
        // 定义Excel表格的列名
        String[] title = {"调拨编号", "调拨类型", "调入仓库", "调出仓库", "期望到货时间", "调拨申请时间", "调拨商品", "规格",
                "调出仓库当前库存", "调入仓库当前库存", "本次调拨数量", "共计调拨数量", "备注", "提交时间", "操作人"};
        // 初始化存储数据的列表
        List<Map<String, String>> list = Lists.newArrayList();
        // 设置调拨类型为“调拨订单”
        param.setType(CommonType.inventoryFlowType.TRANSFER_ORDER.getCode());
        // 从token中获取店铺信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置查询参数中的店铺ID
        param.setShopId(Integer.valueOf(ui.getShopId()));
        // 执行查询调拨库存流动的业务逻辑
        List<StorageInventoryFlowExpVO> result = baseMapper.queryStorageInventoryFlowExp(param);
        // 遍历查询结果，填充调拨详情信息
        for (StorageInventoryFlowExpVO vo : result){
            HashMap<String, String> map = new HashMap<>();
            int putStorageStock = 0;
            int outStorageStock = 0;
            // 查询并设置调入仓库的信息及库存
            if(vo.getPutStorageId() != null){
                String storageName = storageInfoMapper.selectOne(new LambdaQueryWrapper<StorageInfoEntity>()
                        .select(StorageInfoEntity::getStorageName)
                        .eq(StorageInfoEntity::getStorageId, vo.getPutStorageId())).getStorageName();
                vo.setPutStorageName(storageName);
                StorageGoodsBaseEntity putStorageGoods = storageGoodsBaseMapper.selectOne(new LambdaQueryWrapper<StorageGoodsBaseEntity>()
                        .select(StorageGoodsBaseEntity::getStorageStock)
                        .eq(StorageGoodsBaseEntity::getGoodsId, vo.getGoodsId())
                        .eq(StorageGoodsBaseEntity::getStorageId, vo.getPutStorageId()));
                putStorageStock = (putStorageGoods != null && putStorageGoods.getStorageStock() != null) ? putStorageGoods.getStorageStock() : 0;
            }
            // 查询并设置调出仓库的信息及库存
            if(vo.getOutStorageId() != null){
                String storageName = storageInfoMapper.selectOne(new LambdaQueryWrapper<StorageInfoEntity>()
                        .select(StorageInfoEntity::getStorageName)
                        .eq(StorageInfoEntity::getStorageId, vo.getOutStorageId())).getStorageName();
                vo.setOutStorageName(storageName);
                StorageGoodsBaseEntity outStorageGoods = storageGoodsBaseMapper.selectOne(new LambdaQueryWrapper<StorageGoodsBaseEntity>()
                        .select(StorageGoodsBaseEntity::getStorageStock)
                        .eq(StorageGoodsBaseEntity::getGoodsId, vo.getGoodsId())
                        .eq(StorageGoodsBaseEntity::getStorageId, vo.getOutStorageId()));
                outStorageStock = (outStorageGoods != null && outStorageGoods.getStorageStock() != null) ? outStorageGoods.getStorageStock() : 0;
            }
            // 将各个字段信息添加到map中
            map.put("调拨编号", vo.getInventoryFlowId());
            map.put("调拨类型", CommonType.inventoryFlowTypeDetail.getDescByCode(vo.getTypeDetails()));
            map.put("调入仓库", vo.getPutStorageName());
            map.put("调出仓库", vo.getOutStorageName());
            map.put("期望到货时间", DateUtils.formateForSdfCn(vo.getEndTime()));
            map.put("调拨申请时间", DateUtils.formateForSdfCn(vo.getStartTime()));
            map.put("调拨商品", vo.getGoodsName());
            map.put("规格", vo.getGoodsSpec());
            map.put("调出仓库当前库存", outStorageStock + "");
            map.put("调入仓库当前库存", putStorageStock + "");
            map.put("本次调拨数量", vo.getGoodsNum() + "");
            map.put("共计调拨数量", vo.getInventoryFlowNum() + "");
            map.put("备注", vo.getNotes());
            map.put("提交时间", DateUtils.formateForSdfCn(vo.getCreateTime()));
            map.put("操作人", vo.getOperatorName());
            // 将map添加到列表中
            list.add(map);
        }
        // 将数据封装进map，准备生成Excel
        Map<String, List<Map<String, String>>> map = Maps.newHashMap();
        map.put("数据", list);
        // 调用工具类生成Excel文件，并返回文件URL
        String url = ExcelsUtil.createExcel(title, map, new int[]{0,1,2,3,4,5,12,13,14,15});
        // 返回生成Excel文件的URL
        return ResponseResult.success(url);
    }
}
