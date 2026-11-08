package com.houjiang.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.dto.goods.GdbMerchantGuiGeGoodsImagesDTO;
import com.houjiang.commons.dto.goods.MerchantShopGoodsCatDTO;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.vo.goods.GdbMerchantGoodsCommonDetailVO;
import com.houjiang.goods.dto.ManageCommonIdsDTO;
import com.houjiang.goods.entity.*;
import com.houjiang.goods.feign.UserServiceClient;
import com.houjiang.goods.mapper.*;
import com.houjiang.goods.service.GoodsBaseService;
import com.houjiang.goods.vo.ManageGdsBaseAddResultVO;
import com.houjiang.goods.vo.ManageGdsCommonQueryVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Amos
 */
@Slf4j
@Service
public class AsyncVersionServiceImpl {

    @Autowired
    private TaskScheduler taskScheduler;

    @Resource
    private GoodsBaseService goodsBaseService;

    @Resource
    private GoodsVersionMapper goodsVersionMapper;

    @Resource
    private GoodsBaseVersionMapper goodsBaseVersionMapper;

    @Resource
    private GoodsImageVersionMapper goodsImageVersionMapper;

    @Resource
    private GoodsDetailsVersionMapper goodsDetailsVersionMapper;

    @Resource
    private GoodsShopCatVersionMapper goodsShopCatVersionMapper;

    @Resource
    private UserServiceClient userServiceClient;

    /**
     * 添加商品版本信息的方法
     */
    @Async("versionTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addGoodsVersion(ManageCommonIdsDTO dto) {
        // 延迟 2 秒后执行实际逻辑
        taskScheduler.schedule(() ->
                doAddGoodsVersion(dto), Instant.now().plusMillis(2000)
        );
    }

    /**
     * 添加商品版本信息的方法
     */
    @Async("versionTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addGoodsVersionToList(List<Integer> commonIds, String token) {
        // 处理每个commonId
        commonIds.forEach(commonId -> doAddGoodsVersion(new ManageCommonIdsDTO(commonId, 1, token)));
    }

    private void doAddGoodsVersion(ManageCommonIdsDTO dto) {
        // 设置用户信息
        log.info("开始执行异步方法");

        // 初始化商品版本实体
        GoodsVersionEntity entity = new GoodsVersionEntity();

        // 查询商品基础信息
        ResponseResult<ManageGdsCommonQueryVO> rr = goodsBaseService.queryGoods(dto);
        log.info("查询商品公共信息结果：{}", rr.getCode());
        // 如果查询不成功，则直接返回
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return;
        }

        // 获取查询结果数据
        ManageGdsCommonQueryVO data = rr.getData();

        // 查询最新版本的商品版本实体
        GoodsVersionEntity versionEntity = goodsVersionMapper.selectOne(new LambdaQueryWrapper<GoodsVersionEntity>()
                .eq(GoodsVersionEntity::getCommonId, dto.getCommonId())
                .orderByDesc(GoodsVersionEntity::getOperateTime)
                .last("limit 1 FOR UPDATE"));

        if(versionEntity != null && versionEntity.getVerBody().equals(JSON.toJSONString(data))){
            log.info("商品版本信息未更新，commonId为{}", dto.getCommonId());
            return;
        }

        // 计算新的版本号
        String versionNo = versionEntity != null ? Double.parseDouble(versionEntity.getVersionNo()) + 1 + "" : "1.0";

        // 打印异步方法执行的日志，包括商品公共ID和最新版本号
        log.info("异步方法执行，commonId为{}的最新版本号：{}", dto.getCommonId(), versionNo);
        String commonName = data.getCommonResult().getCommonName();
        String userId = data.getCommonResult().getCommonUpdateBy();
        String userName = userServiceClient.queryUserNameByUserId(userId);
        if(StringUtils.isEmpty(userName)){
            userName = "admin";
            userId = "1";
        }
        Integer shopId = data.getCommonResult().getShopId();
        Integer commonId = dto.getCommonId();
        // 设置商品版本实体的属性
        entity.setCommonId(commonId);
        entity.setCommonName(commonName);
        entity.setVerBody(JSON.toJSONString(data.getCommonResult()));
        entity.setShopId(shopId);
        entity.setOperateUserId(userId);
        entity.setOperateUserName(userName);
        entity.setOperateTime(DateUtils.getNowDateToDf());
        entity.setVersionNo(versionNo);

        // 插入新的商品版本记录
        goodsVersionMapper.insert(entity);
        Integer verId = entity.getVerId();
        // 插入新的商品sku版本记录
        List<ManageGdsBaseAddResultVO> goodsBases = data.getGdsBases();
        if(goodsBases !=null && !goodsBases.isEmpty()) {
            List<GoodsBaseVersionEntity> goodsBaseVersionEntityList = new ArrayList<>();
            for (ManageGdsBaseAddResultVO goodsBase : goodsBases) {
                GoodsBaseVersionEntity baseEntity = new GoodsBaseVersionEntity(verId, commonId, commonName, JSON.toJSONString(goodsBase), shopId, userId, userName, versionNo);
                goodsBaseVersionEntityList.add(baseEntity);
            }
            goodsBaseVersionMapper.insertBatch(goodsBaseVersionEntityList);
        }
        // 插入新的商品图片版本记录
        List<GdbMerchantGuiGeGoodsImagesDTO> goodsImages = data.getGoodsImages();
        if(goodsImages != null && !goodsImages.isEmpty()) {
            List<GoodsImageVersionEntity> goodsImageVersionEntityList = new ArrayList<>();
            for (GdbMerchantGuiGeGoodsImagesDTO goodsImage : goodsImages) {
                GoodsImageVersionEntity imageEntity = new GoodsImageVersionEntity(verId, commonId, commonName, JSON.toJSONString(goodsImage), shopId, userId, userName, versionNo);
                goodsImageVersionEntityList.add(imageEntity);
            }
            goodsImageVersionMapper.insertBatch(goodsImageVersionEntityList);
        }
        // 插入新的商品店铺分类版本记录
        List<MerchantShopGoodsCatDTO> goodsShopCats = data.getShopGoodsCatList();
        if(goodsShopCats != null && !goodsShopCats.isEmpty()) {
            List<GoodsShopCatVersionEntity> goodsShopCatVersionEntityList = new ArrayList<>();
            for (MerchantShopGoodsCatDTO goodsShopCat : goodsShopCats) {
                GoodsShopCatVersionEntity shopCatEntity = new GoodsShopCatVersionEntity(verId, commonId, commonName, JSON.toJSONString(goodsShopCat), shopId, userId, userName, versionNo);
                goodsShopCatVersionEntityList.add(shopCatEntity);
            }
            goodsShopCatVersionMapper.insertBatch(goodsShopCatVersionEntityList);
        }
        // 插入新的商品详情版本记录
        GdbMerchantGoodsCommonDetailVO goodsCommonDetail = data.getGoodsCommonDetail();
        if(goodsCommonDetail != null) {
            GoodsDetailsVersionEntity commonDetailEntity = new GoodsDetailsVersionEntity(verId, commonId, commonName, JSON.toJSONString(goodsCommonDetail), shopId, userId, userName, versionNo);
            goodsDetailsVersionMapper.insert(commonDetailEntity);
        }
    }

}
