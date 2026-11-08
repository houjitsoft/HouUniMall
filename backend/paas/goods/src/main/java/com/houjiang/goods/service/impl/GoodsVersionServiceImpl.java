package com.houjiang.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.goods.GdbMerchantGuiGeGoodsImagesDTO;
import com.houjiang.commons.dto.goods.MerchantShopGoodsCatDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.vo.goods.GdbMerchantGoodsCommonDetailVO;
import com.houjiang.commons.vo.goods.GdbMerchantGoodsImagesVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.dto.GoodsVersionDTO;
import com.houjiang.goods.dto.ManageCommonIdsDTO;
import com.houjiang.goods.entity.*;
import com.houjiang.goods.mapper.*;
import com.houjiang.goods.service.GoodsVersionService;
import com.houjiang.goods.vo.ManageGdsBaseAddResultVO;
import com.houjiang.goods.vo.ManageGdsCommonQueryVO;
import com.houjiang.goods.vo.ManageGdsCommonVO;
import jakarta.annotation.Resource;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品版本(GoodsVersion)表服务实现类
 *
 * @author Amos
 * @since 2025-04-29 13:25:21
 */
@Service
public class GoodsVersionServiceImpl extends ServiceImpl<GoodsVersionMapper, GoodsVersionEntity>
    implements GoodsVersionService {

    @Resource
    private GoodsCommonMapper goodsCommonMapper;

    @Resource
    private GoodsBaseMapper goodsBaseMapper;

    @Resource
    private GoodsImagesMapper goodsImagesMapper;

    @Resource
    private GoodsCommonDetailMapper goodsCommonDetailMapper;

    @Resource
    private ShopGoodsCatMapper shopGoodsCatMapper;

    @Lazy
    @Resource
    private AsyncVersionServiceImpl asyncVersionService;

    @Resource
    private GoodsStockMapper merchantGoodsStockMapper;

    @Resource
    private GoodsBaseVersionMapper goodsBaseVersionMapper;

    @Resource
    private GoodsImageVersionMapper goodsImageVersionMapper;

    @Resource
    private GoodsDetailsVersionMapper goodsDetailsVersionMapper;

    @Resource
    private GoodsShopCatVersionMapper goodsShopCatVersionMapper;

    /**
     * 添加商品版本信息
     * <p>
     * 本方法用于添加商品的版本信息，通过查询商品公共ID获取其最新版本信息，并将这些信息保存到数据库中
     */
    @Override
    public void addGoodsVersion(ManageCommonIdsDTO param) {
        // 调用异步服务进行商品版本添加
        asyncVersionService.addGoodsVersion(param);
    }

    /**
     * 同步商品版本信息
     * <p>
     * 本方法用于同步商品的版本信息，通过查询特定条件下的商品公共ID列表，
     * 然后针对每个商品公共ID获取其最新版本信息，并将这些信息保存到数据库中
     */
    @Override
    public void syncGoodsVersion(ManageCommonIdsDTO dto) {
        // 查询所有符合特定条件的商品公共ID列表
        List<Integer> commonIdList = goodsCommonMapper.selectList(new LambdaQueryWrapper<GoodsCommonEntity>()
                .select(GoodsCommonEntity::getCommonId)
                .eq(GoodsCommonEntity::getIntegralGoods, 1).eq(GoodsCommonEntity::getCommonState, 1)
                .ne(GoodsCommonEntity::getSupplierSource, 2)).stream().map(GoodsCommonEntity::getCommonId).toList();
        asyncVersionService.addGoodsVersionToList(commonIdList, dto.getToken());
    }

    /**
     * 查询商品版本详情
     *
     * @param dto 商品版本查询DTO，包含分页信息和商品通用ID
     * @return 返回包含商品版本信息的分页响应结果
     */
    @Override
    public ResponseResult<PageUtils<ManageGdsCommonQueryVO>> queryGoodsVersionDetails(GoodsVersionDTO dto) {
        // 获取当前用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(dto.getToken());
        dto.setShopId(Integer.valueOf(ui.getShopId()));

        UserTokenBeanDTO userTokenBeanDTO = new UserTokenBeanDTO();
        userTokenBeanDTO.setToken(dto.getToken());
        // 初始化分页对象
        Page<GoodsVersionEntity> page = new Page<>(dto.getPage(), dto.getLimit());

        // 查询商品版本信息，只选择版本体信息，并根据操作时间降序排序
        IPage<GoodsVersionEntity> resultPage = baseMapper.selectPage(page, new LambdaQueryWrapper<GoodsVersionEntity>()
                .select(GoodsVersionEntity::getVerBody,GoodsVersionEntity::getVersionNo,
                        GoodsVersionEntity::getOperateTime,GoodsVersionEntity::getVerId)
                .eq(GoodsVersionEntity::getCommonId, dto.getCommonId())
                .eq(GoodsVersionEntity::getShopId, ui.getShopId())
                .orderByDesc(GoodsVersionEntity::getOperateTime));

        // 将查询结果转换为目标VO类型
        IPage<ManageGdsCommonQueryVO> result = resultPage.convert(entity -> {
            // 解析版本体信息为指定的VO类型
            ManageGdsCommonQueryVO vo = new ManageGdsCommonQueryVO();
            ManageGdsCommonVO commonVerBody = JSON.parseObject(entity.getVerBody(), ManageGdsCommonVO.class);
            commonVerBody.setVersionNo(entity.getVersionNo());
            commonVerBody.setOperateTime(entity.getOperateTime());
            vo.setCommonResult(commonVerBody);

            List<String> goodsBasesVerBody = goodsBaseVersionMapper.selectList(new LambdaQueryWrapper<GoodsBaseVersionEntity>()
                    .select(GoodsBaseVersionEntity::getVerBody)
                    .eq(GoodsBaseVersionEntity::getVerId, entity.getVerId())).stream().map(GoodsBaseVersionEntity::getVerBody).toList();
            List<ManageGdsBaseAddResultVO> gdsBases = new ArrayList<>();
            List<Integer> goodsIds = new ArrayList<>();
            for (String goodsBaseVerBody : goodsBasesVerBody) {
                ManageGdsBaseAddResultVO gdsBase = JSON.parseObject(goodsBaseVerBody, ManageGdsBaseAddResultVO.class);
                goodsIds.add(gdsBase.getGoodsId());
                gdsBases.add(gdsBase);
            }
            vo.setGdsBases(gdsBases);
            // 获取商品列表并提取商品ID
            List<ManageGdsBaseAddResultVO> goodsList = vo.getGdsBases();

            // 批量查询所有 ManageGdsBaseAddResultVO
            Map<Integer, ManageGdsBaseAddResultVO> goodsBaseMap = goodsIds.isEmpty() ?
                    Collections.emptyMap() : goodsList.stream().collect(Collectors.toMap(ManageGdsBaseAddResultVO::getGoodsId, Function.identity()));

            // 为每个商品基础信息设置规格版本状态
            vo.getGdsBases().forEach(goodsBase -> {
                // 规格版本状态设置逻辑
                goodsBase.setSpecVersionStatus(
                        resolveSpecVersionStatus(goodsBaseMap.get(goodsBase.getGoodsId()))
                );
            });

            // 返回转换后的VO对象
            return vo;
        });

        // 返回包含分页信息的响应结果
        return ResponseResult.success(PageUtils.getPage(result));
    }

    /**
     * 根据商品基础信息实体解析商品规格版本的状态
     * <p>
     * 本方法旨在提供一个统一的逻辑判断入口，通过商品的基础信息判断其规格版本的状态
     * 主要判断依据包括：实体是否为空、商品是否被删除、商品是否上架
     *
     * @param entity 商品基础信息实体，用于判断商品状态
     * @return 返回商品规格版本的状态码，可能的值包括：已删除、正常、已下架
     */
    private Integer resolveSpecVersionStatus(ManageGdsBaseAddResultVO entity) {
        // 如果传入的实体为空，则认为商品状态为已删除
        if (entity == null) {
            return CommonType.SpecVersionStatus.DELETED.getCode();
        }
        // 如果商品标记为已删除，则返回已删除状态码
        if (Objects.equals(entity.getIsDelete(), CommonType.commonStatus.YES.getCode())) {
            return CommonType.SpecVersionStatus.DELETED.getCode();
        }
        // 根据商品是否上架，返回对应的状态码
        return Objects.equals(entity.getGoodsIsShelves(), CommonType.goodsIsShelves.GOODS_ON_THE_SHELF.getCode()) ?
                CommonType.SpecVersionStatus.NORMAL.getCode() :
                CommonType.SpecVersionStatus.OFF_SHELF.getCode();
    }

    /**
     * 查询商品版本列表
     *
     * @param dto 商品版本查询条件DTO，包含分页信息和查询条件
     * @return 包含商品版本实体的分页工具类
     *
     * 该方法用于查询特定商品的版本列表根据传入的查询条件和分页信息
     * 它首先获取当前用户信息，然后初始化分页对象，最后调用Mapper方法进行查询
     */
    @Override
    public ResponseResult<PageUtils<GoodsVersionEntity>> queryGoodsVersionList(GoodsVersionDTO dto) {
        // 获取当前用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(dto.getToken());
        dto.setShopId(Integer.valueOf(ui.getShopId()));

        // 初始化分页对象
        Page<GoodsVersionEntity> page = new Page<>(dto.getPage(), dto.getLimit());

        // 查询商品版本信息，只选择版本体信息，并根据操作时间降序排序
        IPage<GoodsVersionEntity> resultPage = baseMapper.queryGoodsVersionList(page, dto);

        return ResponseResult.success(PageUtils.getPage(resultPage));
    }

    /**
     * 回滚商品版本
     *
     * @param dto 商品版本回滚条件DTO，包含版本号和商品通用ID
     * @return 返回响应结果
     */
    @Override
    @Transactional
    public ResponseResult<T> rollBackGoodsVersion(GoodsVersionDTO dto) {
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(dto.getToken());
        Integer shopId = Integer.valueOf(ui.getShopId());

        // 1. 查询版本信息（复用查询条件）
        GoodsVersionEntity entity = baseMapper.selectOne(new LambdaQueryWrapper<GoodsVersionEntity>()
                .eq(GoodsVersionEntity::getVersionNo, dto.getVersionNo())
                .eq(GoodsVersionEntity::getCommonId, dto.getCommonId())
                .eq(GoodsVersionEntity::getShopId, shopId));
        if (entity == null) {
            throw new DefinedException(BizCodeEnume.VERSION_NOT_EXIST);
        }

        // 2. 创建返回结果对象
        ManageGdsCommonQueryVO vo = new ManageGdsCommonQueryVO();

        // 3. 更新公共信息（合并对象操作）
        ManageGdsCommonVO commonInfo = JSON.parseObject(entity.getVerBody(), ManageGdsCommonVO.class);
        updateCommonInfo(dto, ui, commonInfo);
        GoodsDetailsVersionEntity commonDetail = goodsDetailsVersionMapper.selectOne(new LambdaQueryWrapper<GoodsDetailsVersionEntity>()
                .select(GoodsDetailsVersionEntity::getVerBody)
                .eq(GoodsDetailsVersionEntity::getVerId, entity.getVerId()));
        if  (commonDetail != null) {
            GdbMerchantGoodsCommonDetailVO commonDetailVO = JSON.parseObject(commonDetail.getVerBody(), GdbMerchantGoodsCommonDetailVO.class);
            vo.setGoodsCommonDetail(commonDetailVO);
            updateCommonDetail(dto, commonDetailVO);
        }

        // 4. 处理分类关系（批量删除+插入）
        List<String> goodsShopCatsVerBody = goodsShopCatVersionMapper.selectList(new LambdaQueryWrapper<GoodsShopCatVersionEntity>()
                .select(GoodsShopCatVersionEntity::getVerBody)
                .eq(GoodsShopCatVersionEntity::getVerId, entity.getVerId())).stream().map(GoodsShopCatVersionEntity::getVerBody).toList();
        List<MerchantShopGoodsCatDTO> shopCats = new ArrayList<>();
        for (String goodsShopCatVerBody : goodsShopCatsVerBody) {
            MerchantShopGoodsCatDTO shopCat = JSON.parseObject(goodsShopCatVerBody, MerchantShopGoodsCatDTO.class);
            shopCats.add(shopCat);
        }
        vo.setShopGoodsCatList(shopCats);
        processShopGoodsCat(dto, vo);

        // 5. 批量处理商品数据（商品sku和商品图片）
        List<String> goodsBasesVerBody = goodsBaseVersionMapper.selectList(new LambdaQueryWrapper<GoodsBaseVersionEntity>()
                .select(GoodsBaseVersionEntity::getVerBody)
                .eq(GoodsBaseVersionEntity::getVerId, entity.getVerId())).stream().map(GoodsBaseVersionEntity::getVerBody).toList();
        List<ManageGdsBaseAddResultVO> gdsBases = new ArrayList<>();
        for (String goodsBaseVerBody : goodsBasesVerBody) {
            ManageGdsBaseAddResultVO gdsBase = JSON.parseObject(goodsBaseVerBody, ManageGdsBaseAddResultVO.class);
            gdsBases.add(gdsBase);
        }
        vo.setGdsBases(gdsBases);
        List<String> goodsImagesVerBody = goodsImageVersionMapper.selectList(new LambdaQueryWrapper<GoodsImageVersionEntity>()
                .select(GoodsImageVersionEntity::getVerBody)
                .eq(GoodsImageVersionEntity::getVerId, entity.getVerId())).stream().map(GoodsImageVersionEntity::getVerBody).toList();
        List<GdbMerchantGuiGeGoodsImagesDTO> goodsImages = new ArrayList<>();
        for (String goodsImageVerBody : goodsImagesVerBody) {
            GdbMerchantGuiGeGoodsImagesDTO goodsImage = JSON.parseObject(goodsImageVerBody, GdbMerchantGuiGeGoodsImagesDTO.class);
            goodsImages.add(goodsImage);
        }
        vo.setGoodsImages(goodsImages);
        processGoodsData(dto, shopId, vo);

        // 6. 新增版本号
        asyncVersionService.addGoodsVersion(new ManageCommonIdsDTO(dto.getCommonId(), CommonType.commonStatus.YES.getCode(), dto.getToken()));

        return ResponseResult.success();
    }

    // ---------------------- 辅助方法 ----------------------
    /**
     * 更新商品公共信息
     *
     * @param dto 商品版本DTO，包含商品的公共ID等信息
     * @param ui 用户信息VO，包含当前操作用户的店铺ID和用户名等信息
     * @param commonInfo 商品公共信息VO，包含需要更新的商品公共信息
     */
    private void updateCommonInfo(GoodsVersionDTO dto, UserInfoVO ui, ManageGdsCommonVO commonInfo) {
        // 创建一个商户商品公共实体对象，并设置其基本属性
        GoodsCommonEntity commonEntity = new GoodsCommonEntity()
                // 设置店铺ID
                .setShopId(Integer.valueOf(ui.getShopId()))
                // 设置商品公共ID
                .setCommonId(dto.getCommonId())
                // 设置商品状态为正常
                .setCommonState(CommonType.commonSpuState.GOODS_NORMAL.getCode())
                // 设置更新者为当前用户名
                .setCommonUpdateBy(ui.getUserName())
                // 设置更新时间为当前时间
                .setCommonUpdateTime(DateUtils.getNowDateToDf());

        // 将商品公共信息VO中的属性复制到商品公共实体对象中
        BeanUtils.copyProperties(commonInfo, commonEntity);

        // 根据ID更新商品公共信息
        goodsCommonMapper.updateById(commonEntity);
    }

    /**
     * 更新商品通用详情信息
     * <p>
     * 该方法用于将界面传入的商品通用详情信息（GoodsVersionDTO）和管理商品通用信息（ManageGdsCommonQueryVO）
     * 结合起来，更新到数据库中对应的MerchantGoodsCommonDetail表中
     *
     * @param dto 商品版本DTO，包含了需要更新的商品的通用ID等信息
     * @param commonDetail 管理商品通用信息的视图对象，包含了商品通用详情信息
     */
    private void updateCommonDetail(GoodsVersionDTO dto, GdbMerchantGoodsCommonDetailVO commonDetail) {
        // 获取商品通用详情信息
        GoodsCommonDetailEntity dtoEntity = goodsCommonDetailMapper.selectById(dto.getCommonId());
        if  (dtoEntity == null) {
            dtoEntity = new GoodsCommonDetailEntity();
            BeanUtils.copyProperties(commonDetail, dtoEntity);
            goodsCommonDetailMapper.insert(dtoEntity);
        }else {
            // 更新数据库中的商品通用详情记录
            goodsCommonDetailMapper.updateById(new GoodsCommonDetailEntity()
                    .setCommonId(dto.getCommonId())
                    .setCommonBody(commonDetail.getCommonBody())
                    .setTs(DateUtils.getNowDateToDf()));
        }
    }

    /**
     * 处理店铺商品分类数据
     *
     * @param dto 商品版本DTO，包含商品的公共ID等信息
     * @param vo 商品信息VO，包含店铺商品分类列表等信息
     */
    private void processShopGoodsCat(GoodsVersionDTO dto, ManageGdsCommonQueryVO vo) {
        // 删除与商品公共ID关联的所有店铺商品分类记录
        shopGoodsCatMapper.deleteByCommonId(dto.getCommonId());

        // 如果店铺商品分类列表不为空，则批量插入新的店铺商品分类记录
        if (!vo.getShopGoodsCatList().isEmpty()) {
            shopGoodsCatMapper.insertSelectiveBatch(vo.getShopGoodsCatList());
        }
    }

    /**
     * 处理商品数据
     * 该方法负责根据提供的商品信息DTO和店铺ID，处理商品基础信息和商品图片的批量更新或插入
     * 同时，它还会标记需要下架的商品
     *
     * @param dto 商品版本DTO，包含通用的字段如commonId
     * @param shopId 店铺ID，用于关联商品和店铺
     * @param vo 包含商品基础信息和商品图片信息的容器
     */
    private void processGoodsData(GoodsVersionDTO dto, Integer shopId, ManageGdsCommonQueryVO vo) {
        List<ManageGdsBaseAddResultVO> goodsBases = vo.getGdsBases();
        List<GdbMerchantGuiGeGoodsImagesDTO> goodsImages = vo.getGoodsImages();

        // 1. 批量检查goodsId存在性
        List<Integer> goodsIdsToCheck = goodsBases.stream()
                .map(ManageGdsBaseAddResultVO::getGoodsId)
                .collect(Collectors.toList());
        Set<Integer> existingGoodsIds = goodsIdsToCheck.isEmpty() ?
                Collections.emptySet() :
                goodsBaseMapper.selectList(new LambdaQueryWrapper<GoodsBaseEntity>()
                                .select(GoodsBaseEntity::getGoodsId)
                                .in(GoodsBaseEntity::getGoodsId, goodsIdsToCheck))
                        .stream()
                        .map(GoodsBaseEntity::getGoodsId)
                        .collect(Collectors.toSet());

        // 2. 批量收集需要下架的商品ID
        List<Integer> goodsIdsToDisable = new ArrayList<>(goodsBases.size());

        // 3. 批量收集图片操作
        List<GdbMerchantGoodsImagesVO> imagesToInsert = new ArrayList<>();

        // 4. 优化循环处理
        Iterator<GdbMerchantGuiGeGoodsImagesDTO> imageIterator = goodsImages.iterator();
        for (ManageGdsBaseAddResultVO goodsBase : goodsBases) {
            if (!imageIterator.hasNext()) break;
            GdbMerchantGuiGeGoodsImagesDTO goodsImage = imageIterator.next();

            boolean exists = existingGoodsIds.contains(goodsBase.getGoodsId());
            if (!exists) {
                goodsIdsToDisable.add(goodsBase.getGoodsId());
            }

            // 处理商品基信息
            processGoodsBase(dto, shopId, goodsBase, exists);

            // 处理商品图片（批量收集）
            List<GdbMerchantGoodsImagesVO> images = goodsImage.getMerchantGoodsImages();
            images.forEach(img -> {
                    img.setCommonId(dto.getCommonId());
                    img.setGoodsId(goodsBase.getGoodsId());
                    imagesToInsert.addAll(images);
            });
        }

        // 5. 批量图片操作
        if (!imagesToInsert.isEmpty()) {
            goodsImagesMapper.insertBatch(imagesToInsert);
        }

        // 6. 批量下架商品
        if (!goodsIdsToDisable.isEmpty()) {
            goodsBaseMapper.update(null, new LambdaUpdateWrapper<GoodsBaseEntity>()
                    .set(GoodsBaseEntity::getGoodsIsShelves, CommonType.goodsIsShelves.GOODS_OFF_THE_SHELF.getCode())
                    .set(GoodsBaseEntity::getIsDelete, CommonType.commonStatus.YES.getCode())
                    .in(GoodsBaseEntity::getGoodsId, goodsIdsToDisable));
        }
    }

    /**
     * 处理商品基础信息
     *
     * @param dto 商品版本DTO，包含商品通用ID等信息
     * @param shopId 店铺ID，用于关联商品和店铺
     * @param goodsBase 管理员商品基础信息DTO，包含需要保存或更新的商品基础数据
     * @param exists 标志变量，表示商品是否已存在true表示存在，需要更新；false表示新商品，需要插入
     */
    private void processGoodsBase(GoodsVersionDTO dto, Integer shopId, ManageGdsBaseAddResultVO goodsBase, boolean exists) {
        // 创建商户商品基础实体对象并设置基本属性
        GoodsBaseEntity baseEntity = new GoodsBaseEntity()
                .setCommonId(dto.getCommonId())
                .setShopId(shopId)
                .setGoodsIsShelves(CommonType.goodsIsShelves.GOODS_ON_THE_SHELF.getCode())
                .setIsDelete(CommonType.commonStatus.NO.getCode());

        // 将商品基础信息DTO的属性复制到实体对象中
        BeanUtils.copyProperties(goodsBase, baseEntity);
        // 根据商品是否存在，执行不同的数据库操作
        if (exists) {
            // 如果商品已存在，则更新商品信息
            goodsBaseMapper.updateById(baseEntity);
        } else {
            // 如果是新商品，则插入商品信息并让数据库自动生成商品ID
            baseEntity.setGoodsId(null);
            goodsBaseMapper.insert(baseEntity);
            // 设置商品库存
            GoodsStockEntity goodsStockEntity = new GoodsStockEntity();
            goodsStockEntity.setGoodsId(baseEntity.getGoodsId());
            goodsStockEntity.setCommonId(baseEntity.getCommonId());
            goodsStockEntity.setGoodsStock(0);
            goodsStockEntity.setGoodsAlarm(0);
            goodsStockEntity.setUpdateTime(new Date());
            goodsStockEntity.setUpdateUser(ParseTokenUtils.getUserInfoByToken(dto.getToken()).getUserId());
            merchantGoodsStockMapper.insert(goodsStockEntity);
            // 插入后，获取数据库生成的商品ID，并回填到商品基础信息DTO中，供后续使用
            goodsBase.setGoodsId(baseEntity.getGoodsId());
        }
    }
}
