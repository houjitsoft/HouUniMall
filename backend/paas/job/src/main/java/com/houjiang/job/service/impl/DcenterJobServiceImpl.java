package com.houjiang.job.service.impl;


import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.entity.*;
import com.houjiang.job.mapper.*;
import com.houjiang.job.service.DcenterJobService;
import com.houjiang.job.vo.*;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Amos
 */
@Service
public class DcenterJobServiceImpl implements DcenterJobService {

    @Resource
    private DcenterOrderBaseMapper dcenterOrderBaseMapper;

    @Resource
    private DcenterOrderGoodsMapper dcenterOrderGoodsMapper;

    @Resource
    private DcenterOrderReturnMapper dcenterOrderReturnMapper;

    @Resource
    private DcenterOrderCancelReasonMapper dcenterOrderCancelReasonMapper;

    @Resource
    private DcenterUserInfoMapper dcenterUserInfoMapper;

    @Resource
    private DcenterUserInfoDetailMapper dcenterUserInfoDetailMapper;

    @Resource
    private DcenterLogActionMapper dcenterLogActionMapper;

    @Resource
    private DcenterUserAccessMapper dcenterUserAccessMapper;

    @Resource
    private DcenterShopBaseMapper dcenterShopBaseMapper;

    @Resource
    private DcenterShopJoinBaseMapper dcenterShopJoinBaseMapper;

    @Resource
    private DcenterGoodsBaseMapper dcenterGoodsBaseMapper;

    @Resource
    private DcenterGoodsCommonMapper dcenterGoodsCommonMapper;

    @Resource
    private DcenterUserFootprintMapper dcenterUserFootprintMapper;

    @Resource
    private DcenterGoodsCatMapper dcenterGoodsCatMapper;

    @Resource
    private DcenterUserCartMapper dcenterUserCartMapper;

    @Resource
    private DcenterSearchWordMapper dcenterSearchWordMapper;

    @Resource
    private DcenterConsumeDepositMapper dcenterConsumeDepositMapper;

    @Resource
    private DcenterUserResourceMapper dcenterUserResourceMapper;

    @Resource
    private DcenterSeckillBaseMapper dcenterSeckillBaseMapper;

    @Resource
    private DcenterReductionPolicyBaseMapper dcenterReductionPolicyBaseMapper;

    @Resource
    private DcenterDiscountBaseMapper dcenterDiscountBaseMapper;

    @Resource
    private DcenterPiecingTogetherMapper dcenterPiecingTogetherMapper;


    @Resource
    private DcenterShopJoinPayMapper dcenterShopJoinPayMapper;
    private static final Logger logger = LoggerFactory.getLogger(DcenterJobServiceImpl.class);

    @Override
    @Async
    public CompletableFuture<ResponseResult<?>> updateDcenterData() {

        List<CompletableFuture<Void>> futures = List.of(
                CompletableFuture.runAsync(() -> updateOrderBaseDataAsync()),
                CompletableFuture.runAsync(() -> updateOrderGoodsDataAsync()),
                CompletableFuture.runAsync(() ->   updateOrderReturnDataAsync()),
                CompletableFuture.runAsync(() ->  updateOrderCancelReasonDataAsync()),
                CompletableFuture.runAsync(() ->  updateUserInfoDataAsync()),
                CompletableFuture.runAsync(() ->  updateUserInfoDetailDataAsync()),
                CompletableFuture.runAsync(() ->  updateLogActionDataAsync()),
                CompletableFuture.runAsync(() ->  updateUserAccessDataAsync()),
                CompletableFuture.runAsync(() ->  updateShopBaseDataAsync()),
                CompletableFuture.runAsync(() ->  updateShopJoinBaseDataAsync()),
                CompletableFuture.runAsync(() ->  updateGoodsBaseDataAsync()),
                CompletableFuture.runAsync(() ->  updateGoodsCommonDataAsync()),


                updateUserFootprintDataAsync(),
                updateGoodsCatDataAsync(),
                updateUserCartDataAsync(),
                updateSearchWordDataAsync(),
                updateConsumeDepositDataAsync(),
                updateUserResourceDataAsync(),
                updateSeckillBaseDataAsync(),
                updateDiscountBaseDataAsync(),
                updateReductionPolicyBaseDataAsync(),
                updatePiecingTogetherDataAsync(),
                updateDcenterShopJoinPayDataAsync()

        );
        // 等待所有异步任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return CompletableFuture.completedFuture(ResponseResult.success());
    }

    @Async
    public CompletableFuture<Void> updateOrderBaseDataAsync() {
        logger.info("====开始更新order_base表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;
        while (true) {
            List<DecenterOrderBaseVO> decenterOrderBaseVOList = dcenterOrderBaseMapper.getDecenterOrderBaseVOList((pageNum - 1) * pageSize, pageSize);
            if (decenterOrderBaseVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DecenterOrderBaseVO decenterOrderBaseVO : decenterOrderBaseVOList) {
                DcenterOrderBaseEntity dcenterOrderBaseEntity = dcenterOrderBaseMapper.selectById(decenterOrderBaseVO.getOrderId());
                if (null != dcenterOrderBaseEntity) {
                    BeanUtils.copyProperties(decenterOrderBaseVO, dcenterOrderBaseEntity);
                    dcenterOrderBaseMapper.updateById(dcenterOrderBaseEntity);
                } else {
                    dcenterOrderBaseEntity = new DcenterOrderBaseEntity();
                    BeanUtils.copyProperties(decenterOrderBaseVO, dcenterOrderBaseEntity);
                    dcenterOrderBaseMapper.insert(dcenterOrderBaseEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateOrderGoodsDataAsync() {
        logger.info("====开始更新order_goods表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;
        while (true) {
            List<DcenterOrderGoodsVO> dcenterOrderGoodsVOList = dcenterOrderGoodsMapper.getDecenterOrderGoodsVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterOrderGoodsVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterOrderGoodsVO dcenterOrderGoodsVO : dcenterOrderGoodsVOList) {
                DcenterOrderGoodsEntity dcenterOrderGoodsEntity = dcenterOrderGoodsMapper.selectById(dcenterOrderGoodsVO.getOrderId());
                if (null != dcenterOrderGoodsEntity) {
                    BeanUtils.copyProperties(dcenterOrderGoodsVO, dcenterOrderGoodsEntity);
                    dcenterOrderGoodsMapper.updateById(dcenterOrderGoodsEntity);
                } else {
                    dcenterOrderGoodsEntity = new DcenterOrderGoodsEntity();
                    BeanUtils.copyProperties(dcenterOrderGoodsVO, dcenterOrderGoodsEntity);
                    dcenterOrderGoodsMapper.insert(dcenterOrderGoodsEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateOrderReturnDataAsync() {
        logger.info("====开始更新order_return表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterOrderReturnVO> dcenterOrderReturnVOList = dcenterOrderReturnMapper.getDecenterOrderReturnVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterOrderReturnVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterOrderReturnVO dcenterOrderReturnVO : dcenterOrderReturnVOList) {
                DcenterOrderReturnEntity dcenterOrderReturnEntity = dcenterOrderReturnMapper.selectById(dcenterOrderReturnVO.getOrderId());
                if (null != dcenterOrderReturnEntity) {
                    BeanUtils.copyProperties(dcenterOrderReturnVO, dcenterOrderReturnEntity);
                    dcenterOrderReturnMapper.updateById(dcenterOrderReturnEntity);
                } else {
                    dcenterOrderReturnEntity = new DcenterOrderReturnEntity();
                    BeanUtils.copyProperties(dcenterOrderReturnVO, dcenterOrderReturnEntity);
                    dcenterOrderReturnMapper.insert(dcenterOrderReturnEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateOrderCancelReasonDataAsync() {
        logger.info("====开始更新order_cancel_reason表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterOrderCancelReasonVO> dcenterOrderCancelReasonVOList = dcenterOrderCancelReasonMapper.getDecenterOrderCancelReasonVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterOrderCancelReasonVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterOrderCancelReasonVO dcenterOrderCancelReasonVO : dcenterOrderCancelReasonVOList) {
                DcenterOrderCancelReasonEntity dcenterOrderCancelReasonEntity = dcenterOrderCancelReasonMapper.selectById(dcenterOrderCancelReasonVO.getCancelReasonId());
                if (null != dcenterOrderCancelReasonEntity) {
                    BeanUtils.copyProperties(dcenterOrderCancelReasonVO, dcenterOrderCancelReasonEntity);
                    dcenterOrderCancelReasonMapper.updateById(dcenterOrderCancelReasonEntity);
                } else {
                    dcenterOrderCancelReasonEntity = new DcenterOrderCancelReasonEntity();
                    BeanUtils.copyProperties(dcenterOrderCancelReasonVO, dcenterOrderCancelReasonEntity);
                    dcenterOrderCancelReasonMapper.insert(dcenterOrderCancelReasonEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateUserInfoDataAsync() {
        logger.info("====开始更新user_info表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterUserInfoVO> dcenterUserInfoVOList = dcenterUserInfoMapper.getDecenterUserInfoVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterUserInfoVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterUserInfoVO dcenterUserInfoVO : dcenterUserInfoVOList) {
                DcenterUserInfoEntity dcenterUserInfoEntity = dcenterUserInfoMapper.selectById(dcenterUserInfoVO.getUserId());
                if (null != dcenterUserInfoEntity) {
                    BeanUtils.copyProperties(dcenterUserInfoVO, dcenterUserInfoEntity);
                    dcenterUserInfoMapper.updateById(dcenterUserInfoEntity);
                } else {
                    dcenterUserInfoEntity = new DcenterUserInfoEntity();
                    BeanUtils.copyProperties(dcenterUserInfoVO, dcenterUserInfoEntity);
                    dcenterUserInfoMapper.insert(dcenterUserInfoEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateUserInfoDetailDataAsync() {
        logger.info("====开始更新user_info_detail表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterUserInfoDetailVO> dcenterUserInfoDetailVOList = dcenterUserInfoDetailMapper.getDecenterUserInfoDetailVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterUserInfoDetailVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterUserInfoDetailVO dcenterUserInfoDetailVO : dcenterUserInfoDetailVOList) {
                DcenterUserInfoDetailEntity dcenterUserInfoDetailEntity = dcenterUserInfoDetailMapper.selectById(dcenterUserInfoDetailVO.getUserId());
                if (null != dcenterUserInfoDetailEntity) {
                    BeanUtils.copyProperties(dcenterUserInfoDetailVO, dcenterUserInfoDetailEntity);
                    dcenterUserInfoDetailMapper.updateById(dcenterUserInfoDetailEntity);
                } else {
                    dcenterUserInfoDetailEntity = new DcenterUserInfoDetailEntity();
                    BeanUtils.copyProperties(dcenterUserInfoDetailVO, dcenterUserInfoDetailEntity);
                    dcenterUserInfoDetailMapper.insert(dcenterUserInfoDetailEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateLogActionDataAsync() {
        logger.info("====开始更新user_log_action表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterLogActionVO> dcenterLogActionVOList = dcenterLogActionMapper.getDecenterLogActionVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterLogActionVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterLogActionVO dcenterLogActionVO : dcenterLogActionVOList) {
                DcenterLogActionEntity dcenterLogActionEntity = dcenterLogActionMapper.selectById(dcenterLogActionVO.getUserId());
                if (null != dcenterLogActionEntity) {
                    BeanUtils.copyProperties(dcenterLogActionVO, dcenterLogActionEntity);
                    dcenterLogActionMapper.updateById(dcenterLogActionEntity);
                } else {
                    dcenterLogActionEntity = new DcenterLogActionEntity();
                    BeanUtils.copyProperties(dcenterLogActionVO, dcenterLogActionEntity);
                    dcenterLogActionMapper.insert(dcenterLogActionEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateUserAccessDataAsync() {
        logger.info("====开始更新user_access表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterUserAccessVO> dcenterUserAccessVOList = dcenterUserAccessMapper.getDecenterUserAccessVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterUserAccessVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterUserAccessVO dcenterUserAccessVO : dcenterUserAccessVOList) {
                DcenterUserAccessEntity dcenterUserAccessEntity = dcenterUserAccessMapper.selectById(dcenterUserAccessVO.getId());
                if (null != dcenterUserAccessEntity) {
                    BeanUtils.copyProperties(dcenterUserAccessVO, dcenterUserAccessEntity);
                    dcenterUserAccessMapper.updateById(dcenterUserAccessEntity);
                } else {
                    dcenterUserAccessEntity = new DcenterUserAccessEntity();
                    BeanUtils.copyProperties(dcenterUserAccessVO, dcenterUserAccessEntity);
                    dcenterUserAccessMapper.insert(dcenterUserAccessEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateShopBaseDataAsync() {
        logger.info("====开始更新shop_base表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterShopBaseVO> dcenterShopBaseVOList = dcenterShopBaseMapper.getDecenterShopBaseVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterShopBaseVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterShopBaseVO dcenterShopBaseVO : dcenterShopBaseVOList) {
                DcenterShopBaseEntity dcenterShopBaseEntity = dcenterShopBaseMapper.selectById(dcenterShopBaseVO.getShopId());
                if (null != dcenterShopBaseEntity) {
                    BeanUtils.copyProperties(dcenterShopBaseVO, dcenterShopBaseEntity);
                    dcenterShopBaseMapper.updateById(dcenterShopBaseEntity);
                } else {
                    dcenterShopBaseEntity = new DcenterShopBaseEntity();
                    BeanUtils.copyProperties(dcenterShopBaseVO, dcenterShopBaseEntity);
                    dcenterShopBaseMapper.insert(dcenterShopBaseEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateShopJoinBaseDataAsync() {
        logger.info("====开始更新shop_join_base表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterShopJoinBaseVO> dcenterShopJoinBaseVOList = dcenterShopJoinBaseMapper.getDecenterShopJoinBaseVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterShopJoinBaseVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterShopJoinBaseVO dcenterShopJoinBaseVO : dcenterShopJoinBaseVOList) {
                DcenterShopJoinBaseEntity dcenterShopJoinBaseEntity = dcenterShopJoinBaseMapper.selectById(dcenterShopJoinBaseVO.getJoinId());
                if (null != dcenterShopJoinBaseEntity) {
                    BeanUtils.copyProperties(dcenterShopJoinBaseVO, dcenterShopJoinBaseEntity);
                    dcenterShopJoinBaseMapper.updateById(dcenterShopJoinBaseEntity);
                } else {
                    dcenterShopJoinBaseEntity = new DcenterShopJoinBaseEntity();
                    BeanUtils.copyProperties(dcenterShopJoinBaseVO, dcenterShopJoinBaseEntity);
                    dcenterShopJoinBaseMapper.insert(dcenterShopJoinBaseEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateGoodsBaseDataAsync() {
        logger.info("====开始更新goods_base表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterGoodsBaseVO> dcenterGoodsBaseVOList = dcenterGoodsBaseMapper.getDecenterGoodsBaseVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterGoodsBaseVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterGoodsBaseVO dcenterGoodsBaseVO : dcenterGoodsBaseVOList) {
                DcenterGoodsBaseEntity dcenterGoodsBaseEntity = dcenterGoodsBaseMapper.selectById(dcenterGoodsBaseVO.getGoodsId());
                if (null != dcenterGoodsBaseEntity) {
                    BeanUtils.copyProperties(dcenterGoodsBaseVO, dcenterGoodsBaseEntity);
                    dcenterGoodsBaseMapper.updateById(dcenterGoodsBaseEntity);
                } else {
                    dcenterGoodsBaseEntity = new DcenterGoodsBaseEntity();
                    BeanUtils.copyProperties(dcenterGoodsBaseVO, dcenterGoodsBaseEntity);
                    dcenterGoodsBaseMapper.insert(dcenterGoodsBaseEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateGoodsCommonDataAsync() {
        logger.info("====开始更新goods_common表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterGoodsCommonVO> dcenterGoodsCommonVOList = dcenterGoodsCommonMapper.getDecenterGoodsCommonVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterGoodsCommonVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterGoodsCommonVO dcenterGoodsCommonVO : dcenterGoodsCommonVOList) {
                DcenterGoodsCommonEntity dcenterGoodsCommonEntity = dcenterGoodsCommonMapper.selectById(dcenterGoodsCommonVO.getCommonId());
                if (null != dcenterGoodsCommonEntity) {
                    BeanUtils.copyProperties(dcenterGoodsCommonVO, dcenterGoodsCommonEntity);
                    dcenterGoodsCommonMapper.updateById(dcenterGoodsCommonEntity);
                } else {
                    dcenterGoodsCommonEntity = new DcenterGoodsCommonEntity();
                    BeanUtils.copyProperties(dcenterGoodsCommonVO, dcenterGoodsCommonEntity);
                    dcenterGoodsCommonMapper.insert(dcenterGoodsCommonEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateUserFootprintDataAsync() {
        logger.info("====开始更新user_footprint表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterUserFootprintVO> dcenterUserFootprintVOList = dcenterUserFootprintMapper.getDecenterUserFootprintVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterUserFootprintVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterUserFootprintVO dcenterUserFootprintVO : dcenterUserFootprintVOList) {
                DcenterUserFootprintEntity dcenterUserFootprintEntity = dcenterUserFootprintMapper.selectById(dcenterUserFootprintVO.getFootprintId());
                if (null != dcenterUserFootprintEntity) {
                    BeanUtils.copyProperties(dcenterUserFootprintVO, dcenterUserFootprintEntity);
                    dcenterUserFootprintMapper.updateById(dcenterUserFootprintEntity);
                } else {
                    dcenterUserFootprintEntity = new DcenterUserFootprintEntity();
                    BeanUtils.copyProperties(dcenterUserFootprintVO, dcenterUserFootprintEntity);
                    dcenterUserFootprintMapper.insert(dcenterUserFootprintEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateGoodsCatDataAsync() {
        logger.info("====开始更新goods_cat表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterGoodsCatVO> dcenterGoodsCatVOList = dcenterGoodsCatMapper.getDecenterGoodsCatVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterGoodsCatVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterGoodsCatVO dcenterGoodsCatVO : dcenterGoodsCatVOList) {
                DcenterGoodsCatEntity dcenterGoodsCatEntity = dcenterGoodsCatMapper.selectById(dcenterGoodsCatVO.getCatId());
                if (null != dcenterGoodsCatEntity) {
                    BeanUtils.copyProperties(dcenterGoodsCatVO, dcenterGoodsCatEntity);
                    dcenterGoodsCatMapper.updateById(dcenterGoodsCatEntity);
                } else {
                    dcenterGoodsCatEntity = new DcenterGoodsCatEntity();
                    BeanUtils.copyProperties(dcenterGoodsCatVO, dcenterGoodsCatEntity);
                    dcenterGoodsCatMapper.insert(dcenterGoodsCatEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateUserCartDataAsync() {
        //获取所有user_cart表数据并更新
        logger.info("====开始更新user_cart表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterUserCartVO> dcenterUserCartVOList = dcenterUserCartMapper.getDecenterUserCartVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterUserCartVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterUserCartVO dcenterUserCartVO : dcenterUserCartVOList) {
                DcenterUserCartEntity dcenterUserCartEntity= dcenterUserCartMapper.selectById(dcenterUserCartVO.getCartId());
                if(null!=dcenterUserCartEntity){
                    BeanUtils.copyProperties(dcenterUserCartVO, dcenterUserCartEntity);
                    dcenterUserCartMapper.updateById(dcenterUserCartEntity);
                } else{
                    dcenterUserCartEntity=new DcenterUserCartEntity();
                    BeanUtils.copyProperties(dcenterUserCartVO, dcenterUserCartEntity);
                    dcenterUserCartMapper.insert(dcenterUserCartEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateSearchWordDataAsync() {
        //获取所有search_word表数据并更新
        logger.info("====开始更新search_word表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterSearchWordVO> dcenterSearchWordVOList = dcenterSearchWordMapper.getDecenterSearchWordVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterSearchWordVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterSearchWordVO dcenterSearchWordVO : dcenterSearchWordVOList) {
                DcenterSearchWordEntity dcenterSearchWordEntity= dcenterSearchWordMapper.selectById(dcenterSearchWordVO.getSearchId());
                if(null!=dcenterSearchWordEntity){
                    BeanUtils.copyProperties(dcenterSearchWordVO, dcenterSearchWordEntity);
                    dcenterSearchWordMapper.updateById(dcenterSearchWordEntity);
                } else{
                    dcenterSearchWordEntity=new DcenterSearchWordEntity();
                    BeanUtils.copyProperties(dcenterSearchWordVO, dcenterSearchWordEntity);
                    dcenterSearchWordMapper.insert(dcenterSearchWordEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateConsumeDepositDataAsync() {
        //获取所有consume_deposit表数据并更新
        logger.info("====开始更新consume_deposit表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterConsumeDepositVO> dcenterConsumeDepositVOList = dcenterConsumeDepositMapper.getDecenterConsumeDepositVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterConsumeDepositVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterConsumeDepositVO dcenterConsumeDepositVO : dcenterConsumeDepositVOList) {
                DcenterConsumeDepositEntity dcenterConsumeDepositEntity= dcenterConsumeDepositMapper.selectById(dcenterConsumeDepositVO.getId());
                if(null!=dcenterConsumeDepositEntity){
                    BeanUtils.copyProperties(dcenterConsumeDepositVO, dcenterConsumeDepositEntity);
                    dcenterConsumeDepositMapper.updateById(dcenterConsumeDepositEntity);
                } else{
                    dcenterConsumeDepositEntity=new DcenterConsumeDepositEntity();
                    BeanUtils.copyProperties(dcenterConsumeDepositVO, dcenterConsumeDepositEntity);
                    dcenterConsumeDepositMapper.insert(dcenterConsumeDepositEntity);
                }

            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateUserResourceDataAsync() {
        //获取所有user_resource表数据并更新
        logger.info("====开始更新user_resource表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterUserResourceVO> DcenterUserResourceVOList= dcenterUserResourceMapper.getDecenterUserResourceVOList((pageNum - 1) * pageSize, pageSize);
            if (DcenterUserResourceVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterUserResourceVO dcenterUserResourceVO : DcenterUserResourceVOList) {
                DcenterUserResourceEntity dcenterUserResourceEntity= dcenterUserResourceMapper.selectById(dcenterUserResourceVO.getUserId());
                if(null!=dcenterUserResourceEntity){
                    BeanUtils.copyProperties(dcenterUserResourceVO, dcenterUserResourceEntity);
                    dcenterUserResourceMapper.updateById(dcenterUserResourceEntity);
                } else{
                    dcenterUserResourceEntity=new DcenterUserResourceEntity();
                    BeanUtils.copyProperties(dcenterUserResourceVO, dcenterUserResourceEntity);
                    dcenterUserResourceMapper.insert(dcenterUserResourceEntity);


                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateSeckillBaseDataAsync() {
        //获取所有seckill_base表数据并更新
        logger.info("====开始更新seckill_base表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterSeckillBaseVO> dcenterSeckillBaseVOList = dcenterSeckillBaseMapper.getDecenterSeckillBaseVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterSeckillBaseVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterSeckillBaseVO dcenterSeckillBaseVO : dcenterSeckillBaseVOList) {
                DcenterSeckillBaseEntity dcenterSeckillBaseEntity= dcenterSeckillBaseMapper.selectById(dcenterSeckillBaseVO.getSeckillId());
                if(null!=dcenterSeckillBaseEntity){
                    BeanUtils.copyProperties(dcenterSeckillBaseVO, dcenterSeckillBaseEntity);
                    dcenterSeckillBaseMapper.updateById(dcenterSeckillBaseEntity);
                } else{
                    dcenterSeckillBaseEntity=new DcenterSeckillBaseEntity();
                    BeanUtils.copyProperties(dcenterSeckillBaseVO, dcenterSeckillBaseEntity);
                    dcenterSeckillBaseMapper.insert(dcenterSeckillBaseEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateDiscountBaseDataAsync() {
        //获取所有discount_base表数据并更新
        logger.info("====开始更新discount_base表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterDiscountBaseVO> dcenterDiscountBaseVOList = dcenterDiscountBaseMapper.getDecenterDiscountBaseVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterDiscountBaseVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterDiscountBaseVO dcenterDiscountBaseVO : dcenterDiscountBaseVOList) {
                DcenterDiscountBaseEntity dcenterDiscountBaseEntity= dcenterDiscountBaseMapper.selectById(dcenterDiscountBaseVO.getDiscountId());
                if(null!=dcenterDiscountBaseEntity){
                    BeanUtils.copyProperties(dcenterDiscountBaseVO, dcenterDiscountBaseEntity);
                    dcenterDiscountBaseMapper.updateById(dcenterDiscountBaseEntity);
                } else{
                    dcenterDiscountBaseEntity=new DcenterDiscountBaseEntity();
                    BeanUtils.copyProperties(dcenterDiscountBaseVO, dcenterDiscountBaseEntity);
                    dcenterDiscountBaseMapper.insert(dcenterDiscountBaseEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateReductionPolicyBaseDataAsync() {
        //获取所有reduction_policy_base表数据并更新
        logger.info("====开始更新reduction_policy_base表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterReductionPolicyBaseVO> dcenterReductionPolicyBaseVOList = dcenterReductionPolicyBaseMapper.getDecenterReductionPolicyBaseVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterReductionPolicyBaseVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterReductionPolicyBaseVO dcenterReductionPolicyBaseVO : dcenterReductionPolicyBaseVOList) {
                DcenterReductionPolicyBaseEntity dcenterReductionPolicyBaseEntity= dcenterReductionPolicyBaseMapper.selectById(dcenterReductionPolicyBaseVO.getPromotionId());
                if(null!=dcenterReductionPolicyBaseEntity){
                    BeanUtils.copyProperties(dcenterReductionPolicyBaseVO, dcenterReductionPolicyBaseEntity);
                    dcenterReductionPolicyBaseMapper.updateById(dcenterReductionPolicyBaseEntity);
                } else{
                    dcenterReductionPolicyBaseEntity=new DcenterReductionPolicyBaseEntity();
                    BeanUtils.copyProperties(dcenterReductionPolicyBaseVO, dcenterReductionPolicyBaseEntity);
                    dcenterReductionPolicyBaseMapper.insert(dcenterReductionPolicyBaseEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updatePiecingTogetherDataAsync() {
        //获取所有activity_piecing_together表数据并更新
        logger.info("====开始更新${database.prefix.datav}hou_datav_piecing_together表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterPiecingTogetherVO> dcenterPiecingTogetherVOList = dcenterPiecingTogetherMapper.getDecenterPiecingTogetherVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterPiecingTogetherVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterPiecingTogetherVO dcenterPiecingTogetherVO : dcenterPiecingTogetherVOList) {
                DcenterPiecingTogetherEntity dcenterPiecingTogetherEntity= dcenterPiecingTogetherMapper.selectById(dcenterPiecingTogetherVO.getPintuanId());
                if(null!=dcenterPiecingTogetherEntity){
                    BeanUtils.copyProperties(dcenterPiecingTogetherVO, dcenterPiecingTogetherEntity);
                    dcenterPiecingTogetherMapper.updateById(dcenterPiecingTogetherEntity);
                } else{
                    dcenterPiecingTogetherEntity=new DcenterPiecingTogetherEntity();
                    BeanUtils.copyProperties(dcenterPiecingTogetherVO, dcenterPiecingTogetherEntity);
                    dcenterPiecingTogetherMapper.insert(dcenterPiecingTogetherEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> updateDcenterShopJoinPayDataAsync() {
        //获取所有${database.prefix.datav}hou_datav_shop_join_pay表数据并更新
        logger.info("====开始更新${database.prefix.datav}hou_datav_shop_join_pay表数据=====");
        // 每页大小
        Integer pageSize = 2000;
        // 起始页码
        Integer pageNum = 1;

        while (true) {
            List<DcenterShopJoinPayVO> dcenterShopJoinPayVOList = dcenterShopJoinPayMapper.getDecenterShopJoinPayVOList((pageNum - 1) * pageSize, pageSize);
            if (dcenterShopJoinPayVOList.isEmpty()) {
                // 如果没有数据，退出循环
                break;
            }
            for (DcenterShopJoinPayVO dcenterShopJoinPayVO : dcenterShopJoinPayVOList) {
                DcenterShopJoinPayEntity dcenterShopJoinPayEntity= dcenterShopJoinPayMapper.selectById(dcenterShopJoinPayVO.getPayId());
                if(null!=dcenterShopJoinPayEntity){
                    BeanUtils.copyProperties(dcenterShopJoinPayVO, dcenterShopJoinPayEntity);
                    dcenterShopJoinPayMapper.updateById(dcenterShopJoinPayEntity);
                } else{
                    dcenterShopJoinPayEntity=new DcenterShopJoinPayEntity();
                    BeanUtils.copyProperties(dcenterShopJoinPayVO, dcenterShopJoinPayEntity);
                    dcenterShopJoinPayMapper.insert(dcenterShopJoinPayEntity);
                }
            }
            // 处理下一页
            pageNum++;
        }
        return CompletableFuture.completedFuture(null);
    }



}