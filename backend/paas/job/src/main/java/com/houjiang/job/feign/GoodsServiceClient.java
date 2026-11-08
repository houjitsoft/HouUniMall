package com.houjiang.job.feign;

import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @ClassName GoodsServiceClient
 * @Description
 * @Author Amos
 * @Date 2023/9/5 9:20
 * @Version 1.0
 * @author Amos
 */
@FeignClient("goods")
public interface GoodsServiceClient {

    /**
     * 修改商品库存
     *
     * @param wapGood
     * @return
     */
    @PostMapping("/goods/common/update/goodsStock")
    ResponseResult updateGoodsStock(@RequestBody List<WapGoodsSaleSumDTO> wapGood);

    /**
     * 导入商品数据
     *
     * @return
     */
    @GetMapping("/es/importGoodsData")
    ResponseResult importData();


    /**
     * 导入店铺信息
     *
     * @return
     */
    @GetMapping("/es/importShopData")
    ResponseResult importShopData();

    /**
     * 导入敏感词信息
     *
     * @return
     */
    @GetMapping("/es/importSensitiveData")
    ResponseResult importSensitiveData();
    /**
     * 京东定时任务 修改商品信息
     * @return
     */
    @GetMapping("/jingDong/jingDongGoodsInfopdateJob")
    ResponseResult jingDongGoodsInfopdateJob();
    /**
     * 触发京东定时任务，处理新增或删除商品的操作
     * 此接口与京东VOP服务交互，执行商品的新增或删除同步任务
     *
     * @return ResponseResult 包含操作结果的响应对象
     */
    @GetMapping("/jingDong/jingDongAddOrDeleteGoodsJob")
    ResponseResult jingDongAddOrDeleteGoodsJob();

    /**
     * 触发京东定时任务，更新商品价格
     * 此接口与京东VOP服务交互，执行商品价格的更新任务
     *
     * @return ResponseResult 包含操作结果的响应对象
     */
    @GetMapping("/jingDong/jingDongUpdatePriceJob")
    ResponseResult jingDongUpdatePriceJob();

    /**
     * 触发京东定时任务，更新商品SKU的销售状态（上下架）
     * 此接口与京东VOP服务交互，执行商品SKU销售状态的同步任务
     *
     * @return ResponseResult 包含操作结果的响应对象
     */
    @GetMapping("/jingDong/jingDongGoodsSKUSaleStatusJob")
    ResponseResult jingDongGoodsSKUSaleStatusJob();

    /**
     * 触发京东定时任务，处理取消订单的操作
     * 此接口与京东VOP服务交互，执行订单取消的同步任务
     *
     * @return ResponseResult 包含操作结果的响应对象
     */
    @GetMapping("/jingDong/jingDongCancelOrderJob")
    ResponseResult jingDongCancelOrderJob();

    /**
     * 触发京东定时任务，处理订单发货操作
     * 此接口与京东VOP服务交互，执行订单发货的同步任务
     *
     * @return ResponseResult 包含操作结果的响应对象
     */
    @GetMapping("/jingDong/jingDongShipmentsJob")
    ResponseResult jingDongShipmentsJob();

    /**
     * 触发京东定时任务，处理订单拆单操作
     * 此接口与京东VOP服务交互，执行订单拆分的同步任务
     *
     * @return ResponseResult 包含操作结果的响应对象
     */
    @GetMapping("/jingDong/jingDongSplitOrderJob")
    ResponseResult jingDongSplitOrderJob();

    /**
     * 触发京东定时任务，确认订单
     * 此接口与京东VOP服务交互，执行订单确认的同步任务
     *
     * @return ResponseResult 包含操作结果的响应对象
     */
    @GetMapping("/jingDong/jingDongConfirmOrderJob")
    ResponseResult jingDongConfirmOrderJob();

    /**
     * 触发京东定时任务，售后单修改
     * 此接口与京东VOP服务交互，执行订单确认的同步任务
     *
     * @return ResponseResult 包含操作结果的响应对象
     */
    @GetMapping("/jingDong/jingDongReurenOrderJob")
    void jingDongReurenOrderJob();

}
