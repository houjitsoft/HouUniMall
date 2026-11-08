package com.houjiang.goods.controller;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.annnotation.AdminTokenLimit;
import com.houjiang.commons.annnotation.Log;
import com.houjiang.commons.dto.base.DealerGoodsUserInfoDTO;
import com.houjiang.commons.dto.goods.*;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.dto.shop.ShopIndexDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.group.Update;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageHelper;
import com.houjiang.commons.vo.base.BaseWxLiveGoodsVO;
import com.houjiang.commons.vo.goods.*;
import com.houjiang.commons.vo.order.WapShippingQueryResultVO;
import com.houjiang.goods.dto.*;
import com.houjiang.goods.entity.GoodsCommonEntity;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsCommonController
 * @Description 商品表
 * @Author Amos
 * @Date 2023-05-25 09:16:26
 * @Version 1.0
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/goods/common")
public class GoodsCommonController extends BaseController {

    /**
     * 根据商品id查询详情
     *
     * @param param
     * @return
     */
    @PostMapping("/query/detail")
    public ResponseResult queryGoodsDetail(@RequestBody GoodsDetailsDTO param) {
        ResponseResult result = goodsCommonService.queryGoodsDetail(param);
        return result;
    }

    /**
     * 详情页的猜你喜欢
     *
     * @param param
     * @return
     */
    @PostMapping("/guess/like/query")
    public ResponseResult queryGuessLikeQuery(@RequestBody WepGuessLikeGoodsParamDTO param) {
        ResponseResult result = goodsCommonService.queryGuessLikeQuery(param);
        return result;
    }

    /**
     * 根据commonid查询详情介绍
     *
     * @param commonsId
     * @return
     */
    @GetMapping("/desc/query")
    public ResponseResult queryGdsDetailDesc(@RequestParam("commonsId") Integer commonsId) {
        ResponseResult result = goodsCommonService.queryGdsDetailDesc(commonsId);
        return result;
    }

    /**
     * 推荐商品
     *
     * @param param
     * @return
     */
    @PostMapping("/recommend/goods/query")
    public ResponseResult queryRecommendGoods(@RequestBody WebIndexRecommendQueryParamDTO param) {
        ResponseResult result = goodsCommonService.queryRecommendGoods(param);
        return result;
    }


    /**
     * 查询商品状态及库存
     *
     * @param commonId
     * @return
     */
    @PostMapping("/queryGoodsStartStock")
    public List<GdsBaseDetailResultVO> queryGoodsStartStock(@RequestParam("commonId") Integer commonId) {
        List<GdsBaseDetailResultVO> gdsBaseDetailResultVOS = goodsCommonService.queryGoodsStartStock(commonId);
        return gdsBaseDetailResultVOS;
    }

    /**
     * 修改商品库存
     *
     * @param wapGood
     * @return
     */
    @PostMapping("/update/goodsStock")
    public ResponseResult updateGoodsStock(@RequestBody List<WapGoodsSaleSumDTO> wapGood) {
        ResponseResult result = goodsCommonService.updateGoodsStock(wapGood);
        return result;
    }

    /**
     * 修改商品销量
     *
     * @param wapGood
     * @return
     */
    @PostMapping("/update/goodsSalenum")
    public ResponseResult updateGoodsSalenum(@RequestBody List<WapGoodsSaleSumDTO> wapGood) {
        ResponseResult result = goodsCommonService.updateGoodsSalenum(wapGood);
        return result;
    }

    /**
     * 查询当前商品商品的 售卖区域规则
     *
     * @param goodsIds
     * @return
     */
    @PostMapping("/selectTransportAreaId")
    public List<Integer> selectTransportAreaId(@RequestBody List<Integer> goodsIds) {
        List<Integer> integers = goodsCommonService.selectTransportAreaId(goodsIds);
        return integers;
    }

    /**
     * 查询当前商品的 国外售卖区域规则
     *
     * @param goodsIds
     * @return
     */
    @PostMapping("selectInternationalTransportAreaId")
    public List<Integer> selectInternationalTransportAreaId(@RequestBody List<Integer> goodsIds) {
        List<Integer> integers = goodsCommonService.selectInternationalTransportAreaId(goodsIds);
        return integers;
    }

    /**
     * 更新商品收藏量  feign
     */
    @PostMapping("/update/goods/collect")
    public ResponseResult updateGoodsCollect(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = goodsCommonService.updateGoodsCollect(paraMap);
        return result;
    }

    /**
     * 根据goods_id查询积分商品的价格和所需的积分
     */
    @PostMapping("/integralGoods/query")
    public ResponseResult queryIntegralGoodsBygoodsId(@RequestBody Map<String, Object> param) {
        ResponseResult result = goodsCommonService.queryIntegralGoodsBygoodsId(param);
        return result;
    }

    /**
     * 判断当前商品是否是是 plus 商品 feign
     */
    @PostMapping("/queryGoodsIsPlus")
    public Integer queryGoodsIsPlus(@RequestParam Integer commonId) {
        Integer integer = goodsCommonService.queryGoodsIsPlus(commonId);
        return integer;
    }

    /**
     * 判断当前商品是否是 虚拟商品
     */
    @PostMapping("/queryVirtualgoods")
    ResponseResult queryVirtualgoods(@RequestBody Map<String, String> param) {
        ResponseResult result = goodsCommonService.queryVirtualgoods(param);
        return result;
    }

    /**
     * 更新商品销量和库存
     *
     * @return
     */
    @PostMapping("/update/salesumStock")
    public ResponseResult updateSaleSum(@RequestBody Map<String, String> param) {
        ResponseResult result = goodsCommonService.updateSaleAndStock(param.get("saleSum"));
        return result;
    }

    /**
     * 更新兑换券商品销量和库存
     *
     * @return
     */
    @PostMapping("/update/ProductVoucherGoodsStock")
    public ResponseResult updateProductVoucherGoodsStock(@RequestBody Map<String, Object> param) {
        ResponseResult result = goodsCommonService.updateProductVoucherGoodsStock(param);
        return result;
    }

    /**
     * 根据商品规格查询商品
     */
    @PostMapping("/spec/queryNew")
    public ResponseResult queryGoodsSpec(@RequestBody GoodsDetailsDTO param) {
        ResponseResult result = goodsCommonService.queryGoodsSpec(param);
        return result;
    }

    /**
     * 兑换券活动 根据商品规格查询商品
     */
    @PostMapping("/productVoucher/queryGoodsSpec")
    public ResponseResult productVoucherQueryGoodsSpec(@RequestBody GoodsDetailsDTO param) {
        ResponseResult result = goodsCommonService.productVoucherQueryGoodsSpec(param);
        return result;
    }

    /**
     * 积分商品列表 pc
     *
     * @param param
     * @return
     */
    @PostMapping("integral/query/filter")
    public ResponseResult queryIntegralGoodsListFilter(@RequestBody(required = false) ListPageQueryParamDTO param) {
        ResponseResult result = goodsCommonService.queryIntegralGoodsListFilter(param);
        return result;
    }

    /**
     * 查询规格值数据
     */
    @PostMapping("/shop/goodsOrderParam/{goodsId}")
    public ResponseResult goodsOrderParam(@PathVariable("goodsId") String goodsId) {
        ResponseResult result = goodsCommonService.goodsOrderParam(goodsId);
        return result;
    }

    /**
     * 查询店铺首页
     *
     * @param param
     * @return
     */
    @PostMapping("/shopIndex/query")
    public ResponseResult queryShopIndex(@RequestBody ShopIndexDTO param) {
        ResponseResult result = goodsCommonService.queryShopIndex(param);
        return result;
    }

    /**
     * 根据commonid获取商品详情
     *
     * @param commonId
     * @return
     */
    @GetMapping("/getDetailByCommonId")
    public ResponseResult getDetailByCommonId(@RequestParam("commonId") Integer commonId) {
        ResponseResult result = goodsCommonService.getDetailByCommonId(commonId);
        return result;
    }


    /**
     * 根据商品id修改收藏数
     *
     * @param commonId
     * @return
     */
    @GetMapping("/updateCommonCollect")
    public ResponseResult updateCommonCollect(@RequestParam("goodId") String commonId) {
        ResponseResult result = goodsCommonService.updateCommonCollect(commonId);
        return result;
    }

    /**
     * 取消下单时，增加库存
     *
     * @param param
     * @return
     */
    @PostMapping("/update/stock/reduce")
    public ResponseResult updateStockReduce(@RequestBody WapGoodsSaleSumDTO param) {
        ResponseResult result = goodsCommonService.updateStockReduce(param);
        return result;
    }

    /**
     * 更新商品评论数
     *
     * @param map
     * @return
     */
    @PostMapping("/updateCommentCount")
    public ResponseResult updateCommentCount(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsCommonService.updateCommentCount(map);
        return result;
    }

    /**
     * 查询商品数据
     *
     * @param param
     * @return
     */
    @PostMapping("/salesum/query")
    public ResponseResult queryByGoodsSalesum(@RequestBody UserTokenBeanDTO param) {
        ResponseResult result = goodsCommonService.queryByGoodsSalesum(param);
        return result;
    }

    /**
     * 查询出售中的商品 goodsName不传，查询店铺所有   goods
     *
     * @param param
     * @return
     */
    @PostMapping("/gdsname/query")
    public ResponseResult queryByGoodsName(@RequestBody ManageGdsNameQueryDTO param) {
        ResponseResult result = goodsCommonService.queryByGoodsName(param);
        return result;
    }

    /**
     * 根据commonId删除商品
     *
     * @param param
     * @return
     */
    @PostMapping("/commonId/delete")
    @Log("根据commonId删除商品")
    public ResponseResult deleteByCommonId(@RequestBody ManageCommonIdsDTO param) {
        ResponseResult result = goodsCommonService.deleteByCommonId(param);
        return result;
    }


    /**
     * 根据commonId查询商品锁状态
     *
     * @param param
     * @return
     */
    @PostMapping("/query/lock/status")
    public ResponseResult queryGoodsLockedStatus(@RequestBody GoodsDetailsDTO param) {
        ResponseResult result = goodsCommonService.queryGoodsLockedStatus(param);
        return result;
    }

    /**
     * plus 商品推荐
     */
    @RequestMapping("/selectPlusGoods")
    public ResponseResult selectPlusgoods() {
        ResponseResult selectplusgoods = goodsCommonService.selectplusgoods();
        return selectplusgoods;
    }

    /**
     * 查看放入仓库的商品
     *
     * @param param
     * @return
     */
    @PostMapping("/storeHouse/query")
    public ResponseResult queryStoreHouse(@RequestBody ManageGdsNameQueryDTO param) throws IllegalAccessException {
        ResponseResult result = goodsCommonService.queryStoreHouse(param);
        return result;
    }

    /**
     * 查询等待审核的商品
     *
     * @param param
     * @return
     */
    @PostMapping("/verify/query")
    public ResponseResult queryVerifyGoods(@RequestBody ManageGdsNameQueryDTO param) throws IllegalAccessException {
        ResponseResult result = goodsCommonService.queryVerifyGoods(param);
        return result;
    }

    /**
     * 查询审核失败的商品
     *
     * @param param
     * @return
     */
    @PostMapping("/verify/fail/query")
    public ResponseResult queryVerifyFailGoods(@RequestBody ManageGdsNameQueryDTO param) throws IllegalAccessException {
        ResponseResult result = goodsCommonService.queryVerifyFailGoods(param);
        return result;
    }


    /**
     * 违规的商品
     *
     * @param param
     * @return
     */
    @PostMapping("/violations/query")
    public ResponseResult queryViolationsGoods(@RequestBody ManageGdsNameQueryDTO param) throws IllegalAccessException {
        ResponseResult result = goodsCommonService.queryViolationsGoods(param);
        return result;
    }


    /**
     * 查询运费模板下所有商品
     *
     * @param param
     * @return
     */
    @PostMapping("/by/template/id")
    public ResponseResult queryGoodsByTemplateId(@RequestBody TransportTemplateDetailDeleteDTO param) throws IllegalAccessException {
        ResponseResult result = goodsCommonService.queryGoodsByTemplateId(param);
        return result;
    }

    /**
     * 查询售卖区域id下所有商品
     *
     * @param param
     * @return
     */
    @PostMapping("/by/transport/area/id")
    public ResponseResult queryGoodsByTransportAreaId(@RequestBody TransportTemplateDetailDeleteDTO param) throws IllegalAccessException {
        ResponseResult result = goodsCommonService.queryGoodsByTransportAreaId(param);
        return result;
    }

    /**
     * 修改商品是否加入促销 feign
     *
     * @param commonIdList
     */
    @PostMapping("/update/is/xian")
    public void updateCommonIsXian2(@RequestBody List<Integer> commonIdList) {
        goodsCommonService.updateCommonIsXian2(commonIdList);
    }

    /**
     * 修改商品是否加入促销 feign
     *
     * @param param
     */
    @PostMapping("/update/is/xian/byCommonId")
    public void updateCommonIsXian(@RequestBody Map<String, Object> param) {
        goodsCommonService.updateCommonIsXian(param);
    }

    /**
     * 查询非活动商品
     *
     * @param param
     * @return
     */
    @PostMapping("/goods/query/list")
    public ResponseResult queryGoodsList(@RequestBody GoodsListQueryDTO param) {
        ResponseResult result = goodsCommonService.queryGoodsList(param);
        return result;
    }

    /**
     * 查询出售中的商品 goodsName不传，查询店铺所有   activityGoods
     *
     * @param param
     * @return
     */
    @PostMapping("/activityGoods/query")
    public ResponseResult queryactivityGoods(@RequestBody ManageGdsNameQueryDTO param) throws IllegalAccessException {
        ResponseResult result = goodsCommonService.queryactivityGoods(param);
        return result;
    }

    /**
     * 查询装修空间 所有商品
     *
     * @param param
     * @return
     */
    @PostMapping("/modularization/queryGoodsAll")
    public ResponseResult queryManageSpaceAllGoods(@RequestBody PlatFormAllGoodsQueryDTO param) {
        ResponseResult result = goodsCommonService.queryManageSpaceAllGoods(param);
        return result;
    }

    /**
     * 查询商品数量
     *
     * @param map
     * @return
     */
    @PostMapping("/query/goodsCount")
    public ResponseResult goodsCount(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsCommonService.goodsCount(map);
        return result;
    }

    /**
     * 查询今日商品数量
     *
     * @param map
     * @return
     */
    @PostMapping("/query/todayNumber")
    public ResponseResult goodsTodayNumber(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsCommonService.goodsTodayNumber(map);
        return result;
    }

    /**
     * 查询所有商品
     *
     * @param param
     * @return
     */
    @PostMapping("/all/goods")
    public ResponseResult queryAllGoods(@RequestBody PlatFormAllGoodsDTO param) throws IllegalAccessException {
        ResponseResult result = goodsCommonService.queryAllGoods(param);
        return result;
    }

    /**
     * 商品按月分组统计
     */
    @RequestMapping("/query/byMonth")
    public ResponseResult queryByMonth(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsCommonService.queryByMonth(map);
        return result;
    }

    /**
     * 商品一键上架 同步商品
     *
     * @param goodsUserInfo
     * @return
     */
    @RequestMapping("/putAway")
    public ResponseResult putAway(@RequestBody DealerGoodsUserInfoDTO goodsUserInfo) {
        ResponseResult result = goodsCommonService.putAway(goodsUserInfo);
        return result;
    }

    /**
     * 供应商商品立即购买
     *
     * @param map
     * @return
     */
    @RequestMapping("/dealer")
    public ResponseResult dealer(@RequestBody Map<String, String> map) {
        ResponseResult result = goodsCommonService.dealer(map);
        return result;
    }

    /**
     * 查询经销商品
     *
     * @param param
     * @return
     */
    @PostMapping("/query/dealerGoods")
    public ResponseResult queryDealerGoods(@RequestBody ManageGdsNameQueryDTO param) throws IllegalAccessException {
        ResponseResult result = goodsCommonService.queryDealerGoods(param);
        return result;
    }

    /**
     * 积分商品编辑
     *
     * @param param
     * @return
     */
    @PostMapping("/updateIntegralGoods")
    public ResponseResult updateIntegralGoods(@RequestBody IntegralCommonDTO param) {
        ResponseResult result = goodsCommonService.updateIntegralGoods(param);
        return result;
    }

    /**
     * 查询商品详情
     *
     * @param map
     * @return
     */
    @PostMapping("/goodsQueryDetail")
    public ResponseResult goodsQueryDetail(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsCommonService.goodsQueryDetail(map);
        return result;
    }


    /**
     * 积分商品添加
     *
     * @param param
     * @return
     */
    @PostMapping("/addIntegralGoods")
    public ResponseResult addIntegralGoods(@RequestBody ManageGdsCommonPublishDTO param) {
        ResponseResult result = goodsCommonService.addIntegralGoods(param);
        return result;
    }

    /**
     * 优惠券商品列表
     */
    @PostMapping("/query/couponList")
    public ResponseResult queryCouponList(@RequestBody UserVcBaseQueryDTO param) {
        ResponseResult result = goodsCommonService.queryCouponList(param);
        return result;
    }

    /**
     * 红包商品列表
     *
     * @param param
     * @return
     */
    @PostMapping("/query/redPacketList")
    public ResponseResult queryRedPacketList(@RequestBody UserAvailableRedpacketQueryDTO param) {
        ResponseResult result = goodsCommonService.queryRedPacketList(param);
        return result;
    }


    /**
     * 查询商品、店铺审核数
     *
     * @param
     * @return
     */
    @PostMapping("/query/verify/num")
    public ResponseResult queryVerifyNum(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsCommonService.queryVerifyNum(map);
        return result;
    }

    /**
     * 更新spu商品 除shopId无其他条件
     *
     * @param map
     * @return
     */
    @PostMapping("/update")
    public ResponseResult updateGoodsCommonByMap(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsCommonService.updateGoodsCommonByMap(map);
        return result;
    }

    /**
     * 根据店铺id查找所有commonId
     *
     * @param shopId
     * @return
     */
    @GetMapping("/query/commonIdListByShopId")
    public List<Integer> queryCommonIdList(@RequestParam("shopId") Integer shopId) {
        List<Integer> result = goodsCommonService.queryCommonIdByShopId(shopId);
        return result;
    }

    /**
     * 更新spu商品状态
     *
     * @param commonParam
     * @return
     */
    @PostMapping("/updateGoodsCommonState")
    public ResponseResult updateCommonState(@RequestBody CommonShopStateDTO commonParam) {
        ResponseResult result = goodsCommonService.updateCommonState(commonParam);
        return result;
    }

    /**
     * 查询所有的积分商品
     *
     * @param param
     * @return
     */
    @PostMapping("/queryIntegralAll")
    public ResponseResult queryIntegralAll(@RequestBody(required = false) ListPageQueryParamDTO param) {
        ResponseResult result = goodsCommonService.queryIntegralAll(param);
        return result;
    }


    /**
     * 平台商品批量 锁定
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/lock/update")
    public ResponseResult platformLockUpdate(@RequestBody PlatFormGoodsStateDTO param) {
        ResponseResult result = goodsCommonService.platformLockUpdate(param);
        return result;
    }


    /**
     * 平台商品批量 屏蔽
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/screen/update")
    public ResponseResult platformScreenUpdate(@RequestBody PlatFormGoodsStateDTO param) {
        ResponseResult result = goodsCommonService.platformScreenUpdate(param);
        return result;
    }


    /**
     * 平台商品批量上、下架  审核
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/violations/update")
    public ResponseResult platformViolationsUpdate(@RequestBody PlatFormGoodsStateDTO param) {
        ResponseResult result = goodsCommonService.platformViolationsUpdate(param);
        return result;
    }


    /**
     * 平台查询商品详情
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/query/detail")
    public ResponseResult platformQueryDetail(@RequestBody ManageGdsNameQueryParamDTO param) {
        ResponseResult result = goodsCommonService.platformQueryDetail(param);
        return result;
    }

    /**
     * 获取门店列表库存信息
     *
     * @param param
     * @return
     */
    @PostMapping("/query/gateCommons")
    public ResponseResult queryGateCommonsList(@RequestBody GateQueryCommonsDTO param) {
        ResponseResult result = goodsCommonService.queryGateCommonsList(param);
        return result;
    }

    /**
     * 查询出售中的商品规格列表
     *
     * @param param
     * @return
     */
    @PostMapping("/query/goodsSpec")
    public ResponseResult queryGoodsSpecList(@RequestBody ManageCommonIdsDTO param) {
        ResponseResult result = goodsCommonService.queryGoodsSpecList(param);
        return result;
    }

    /**
     * 门店设置库存
     *
     * @param param
     * @return
     */
    @PostMapping("/update/inventory")
    public ResponseResult updateGateShopStock(@RequestBody GateUpShopGoodsStockDTO param) {
        ResponseResult result = goodsCommonService.updateGateShopStock(param);
        return result;
    }


    /**
     * 商家经销审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/check/dealer")
    public ResponseResult checkDealer(@RequestBody DealerGoodsUserInfoDTO dto) {
        ResponseResult result = goodsCommonService.checkDealer(dto);
        return result;
    }

    /**
     * 查询商家是否是指定供应商的经销商
     *
     * @param dto
     * @return
     */
    @PostMapping("/queryGoodsUser")
    public ResponseResult queryGoodsUser(@RequestBody DealerGoodsUserInfoDTO dto) {
        ResponseResult result = goodsCommonService.queryGoodsUser(dto);
        return result;
    }

    /**
     * 商家中心-商品信息查询
     *
     * @param dto
     * @return
     */
    @PostMapping("/business/queryGoodsDetail")
    public ResponseResult queryGoodsDetailByBusiness(@RequestBody ManageCommonIdsDTO dto) {
        ResponseResult result = goodsCommonService.queryGoodsDetailByBusiness(dto);
        return result;
    }

    /**
     * 商家中心-供应商商品列表页
     *
     * @param dto
     * @return
     */
    @PostMapping("/business/queryGoodsList")
    public ResponseResult queryGoodsListByBusiness(@RequestBody WebGoodsListQueryDTO dto) throws IllegalAccessException {
        ResponseResult result = goodsCommonService.queryGoodsListByBusiness(dto);
        return result;
    }

    /**
     * 查询出售中的商品 goodsName不传，查询店铺所有
     *
     * @param
     * @return
     */
    @PostMapping("/live/gdsname/query")
    public ResponseResult livequeryByGoodsName(@RequestBody ManageGdsNameQueryDTO param) {
        ResponseResult result = goodsCommonService.livequeryByGoodsName(param);
        return result;
    }

    /**
     * 判断商品是否可以添加
     *
     * @param param
     * @return
     */
    @PostMapping("/seckill/check/goods")
    public ResponseResult seckillCheckGoods(@RequestBody ManageSeckillBaseUpdateDTO param) {
        ResponseResult result = goodsCommonService.seckillCheckGoods(param);
        return result;
    }

    /**
     * 商品搜索列表
     *
     * @param param
     * @return
     */
    @PostMapping("/query/filter")
    public ResponseResult shopListQueryByShopFilter(@RequestBody(required = false) ListPageQueryParamDTO param) {
        ResponseResult result = goodsCommonService.shopListQueryByShopFilter(param);
        return result;
    }

    /**
     * 添加plus商品
     *
     * @param map
     * @return
     */
    @PostMapping("/updatePlusGoods")
    public ResponseResult updatePlusGoods(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsCommonService.updatePlusGoods(map);
        return result;
    }


    /**
     * 查询plus商品信息
     *
     * @param map
     * @return
     */
    @PostMapping("/selecPlustGoodsByPage")
    public ResponseResult selecPlustGoodsByPage(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsCommonService.selecPlustGoodsByPage(map);
        return result;
    }

    /**
     * 删除plus商品
     *
     * @param map
     * @return
     */
    @PostMapping("/deletePlusGoods")
    public ResponseResult deletePlusGoods(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsCommonService.deletePlusGoods(map);
        return result;
    }

    /**
     * 查询可以添加兑换券活动的商品
     *
     * @param map
     * @return
     */
    @PostMapping("/selecProductVoucherGoodsByPage")
    public ResponseResult selecProductVoucherGoodsByPage(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsCommonService.selecProductVoucherGoodsByPage(map);
        return result;
    }

    /**
     * 查询 兑换券商品信息
     *
     * @param productVoucherGoodsVOS
     * @return
     */
    @PostMapping("/selecProductVoucherGoods")
    public List<ProductVoucherGoodsVO> selecProductVoucherGoods(@RequestBody List<ProductVoucherGoodsVO> productVoucherGoodsVOS) {
        List<ProductVoucherGoodsVO> result = goodsCommonService.selecProductVoucherGoods(productVoucherGoodsVOS);
        return result;
    }

    /**
     * 查询赠品
     *
     * @param param
     * @return
     */
    @PostMapping("/query/giftList")
    public ResponseResult queryGiveawayList(@RequestBody ManjianzengzheListQueryDTO param) throws IllegalAccessException {
        ResponseResult result = goodsCommonService.queryGiveawayList(param);
        return result;
    }

    /**
     * 判断商品是否可以添加
     *
     * @param param
     * @return
     */
    @PostMapping("/discount/check/goods")
    public ResponseResult discountCheckGoods(@RequestBody DiscountBaseAddParamDTO param) {
        ResponseResult result = goodsCommonService.discountCheckGoods(param);
        return result;
    }


    /**
     * 查询商品列表 暂不用
     *
     * @param param
     * @return
     */
    @PostMapping("/pingtuan/goods/query/list")
    public ResponseResult queryGoodsList(@RequestBody Map<String, Object> param) {
        ResponseResult result = goodsCommonService.queryGoodsListByPintuan(param);
        return result;
    }

    /**
     * 查询商家同步供应商的商品 但未修改过价格的
     */
    @PostMapping("/queryIsPriceUpdate")
    public ResponseResult queryIsPriceUpdate(@RequestBody ManageGdsNameQueryDTO param) throws IllegalAccessException {
        ResponseResult result = goodsCommonService.queryIsPriceUpdate(param);
        return result;
    }

    /**
     * 待发布的商品
     *
     * @param param
     * @return
     */
    @PostMapping("/wait/release/query")
    public ResponseResult queryWaitRelease(@RequestBody ManageGdsNameQueryDTO param) throws IllegalAccessException {
        ResponseResult result = goodsCommonService.queryWaitRelease(param);
        return result;
    }


    /**
     * 商品上架
     *
     * @param param
     * @return
     */
    @PostMapping("/online")
    public ResponseResult setGoodsOnline(@RequestBody ManageCommonIdsDTO param) {
        ResponseResult result = goodsCommonService.setGoodsOnline(param);
        return result;
    }

    /**
     * 校验商品名称是否重复
     *
     * @param param
     * @return
     */
    @PostMapping("/check/goods/name")
    public ResponseResult checkGoodsName(@RequestBody CheckNameQueryParamDTO param) {
        ResponseResult result = goodsCommonService.checkGoodsName(param);
        return result;
    }


    /**
     * 校验店铺规格下是否有商品
     *
     * @param param
     * @return
     */
    @PostMapping("/spec/query")
    public ResponseResult checkGoodsBySpecId(@RequestBody Map<String, Object> param) {
        ResponseResult result = goodsCommonService.checkGoodsBySpecId(param);
        return result;
    }


    /**
     * 商品下架
     *
     * @param param
     * @return
     */
    @PostMapping("/offline")
    public ResponseResult setGoodsOffline(@RequestBody ManageCommonIdsDTO param) {
        ResponseResult result = goodsCommonService.setGoodsOffline(param);
        return result;
    }

    /**
     * 查询门店商品
     *
     * @param param
     * @return
     */
    @PostMapping("/h5/queryGoods")
    public ResponseResult queryGoods(@RequestBody Map<String, Object> param) {
        ResponseResult result = goodsCommonService.queryGoods(param);
        return result;
    }

    /**
     * 根据条件筛选
     *
     * @param param
     * @return
     */
    @PostMapping("/h5/filter/query")
    public ResponseResult queryByFilter(@RequestBody ShopIndexQueryParamDTO param) throws IllegalAccessException {
        ResponseResult result = goodsCommonService.queryByFilter(param);
        return result;
    }

    /**
     * 查询店铺首页
     *
     * @param param
     * @return
     */
    @PostMapping("/h5/shopindex/query")
    public ResponseResult queryShopIndexquery(@RequestBody ShopIndexQueryParamDTO param) throws IllegalAccessException {
        ResponseResult result = goodsCommonService.queryShopIndexquery(param);
        return result;
    }

    /**
     * 商家端-图片导入传入zip包
     *
     * @param file
     * @return
     */
    @PostMapping("/zipLocalDisk")
    public ResponseResult zipLocalDiskImage(@RequestParam("file") MultipartFile file, @RequestParam("commonList") List<String> commonList) throws IOException {

        byte[] sourceZipFileBytes = null;
        //将zip转换为二进制流
        sourceZipFileBytes = file.getBytes();
        if (sourceZipFileBytes == null) {
            return ResponseResult.fail(0, "导入文件为空！");
        } else {
            ResponseResult result = goodsCommonService.zipLocalDiskImage(sourceZipFileBytes, commonList);
            return result;
        }

    }


    /**
     * 淘宝导入
     *
     * @param file
     * @param token
     * @param transportAreaId
     * @param transportTemplate
     * @return
     * @throws Exception
     */
    @PostMapping("/importGoodsData/file")
    public ResponseResult insertGoodsBatch(@RequestParam("file") MultipartFile file, @RequestParam("token") String token,
                                           @RequestParam("transportAreaId") Integer transportAreaId,
                                           @RequestParam("transportTemplate") Integer transportTemplate) throws Exception {
        ResponseResult result = goodsCommonService.insertGoodsBatch(file, token, transportAreaId, transportTemplate);
        return result;
    }

    /**
     * 满赠减少赠品库存增加商品销量
     *
     * @param commonIdList
     * @return
     */
    @PostMapping("/updateGiveawayStock")
    public void updateGiveawayStock(@RequestBody List<Integer> commonIdList) {
        goodsCommonService.updateGiveawayStock(commonIdList);
    }

    /**
     * 查询待发布的商品
     *
     * @param param
     * @return
     */
    @PostMapping("/query/toBeReleased")
    public ResponseResult queryToBeReleased(@RequestBody ManageGdsNameQueryParamDTO param) {
        if (param.getToken() == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_NULL);
        } else {
            return goodsCommonService.queryToBeReleased(param);
        }
    }

    /**
     * 定时发布商品--立即发布
     *
     * @param param
     * @return
     */
    @PostMapping("/fixedTimeGoodsPublishNow")
    public ResponseResult fixedTimeGoodsPublishNow(@RequestBody ManageCommonIdsDTO param) {
        return goodsCommonService.fixedTimeGoodsPublishNow(param);
    }

    /**
     * 放入仓库商品--立即审核
     *
     * @param param
     * @return
     */
    @PostMapping("/putIntoWarehouseGoodsReviewingNow")
    public ResponseResult putIntoWarehouseGoodsReviewingNow(@RequestBody ManageCommonIdsDTO param) {
        return goodsCommonService.putIntoWarehouseGoodsReviewingNow(param);
    }

    /**
     * 查询平台礼包商品
     *
     * @param param
     * @return
     */
    @PostMapping("/selectPlatformGiftGoods")
    public ResponseResult selectPlatformGiftGoods(@RequestBody GiftGoodsDTO param) {
        return goodsCommonService.selectPlatformGiftGoods(param);
    }

    /**
     * 添加自营店铺商品
     *
     * @param param
     * @return
     */
    @PostMapping("/addPlatformGiftGoods")
    public ResponseResult addPlatformGiftGoods(@RequestBody GiftGoodsDTO param) {
        return goodsCommonService.addPlatformGiftGoods(param);
    }

    /**
     * 移除自营店铺商品
     *
     * @param param
     * @return
     */
    @PostMapping("/removePlatformGiftGoods")
    public ResponseResult removePlatformGiftGoods(@RequestBody @Validated({Update.class}) GiftGoodsDTO param) {
        return goodsCommonService.removePlatformGiftGoods(param);
    }

    /**
     * 查询商品规格列表
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/queryGoodsSpecList")
    public ResponseResult queryPlatformGoodsSpecList(@RequestBody ManageCommonIdsDTO param) {
        ResponseResult result = goodsCommonService.queryPlatformGoodsSpecList(param);
        return result;
    }

    /**
     * 查询兑换券下单 商品信息 feign
     *
     * @param commonId
     * @return
     */
    @PostMapping("/queryProductVoucherGoodsCommon")
    public GdbMerchantGoodsCommonVO queryProductVoucherGoodsCommon(@RequestParam("commonId") Integer commonId) {
        GdbMerchantGoodsCommonVO result = goodsCommonService.queryProductVoucherGoods(commonId);
        return result;
    }

    /**
     * 根据商品名称模糊查询商品id feign
     *
     * @param commonName
     * @return
     */
    @PostMapping("/queryLikeGoodsName")
    public List<Integer> queryLikeGoodsName(@RequestParam("commonName") String commonName) {
        List<Integer> result = goodsCommonService.queryLikeGoodsName(commonName);
        return result;
    }

    /**
     * 批量修改商品物流信息
     *
     * @param param
     * @return
     */
    @PostMapping("/updateGoodsTransportInfo")
    public ResponseResult updateGoodsTransportInfo(@RequestBody UpdateGoodsTransportInfoDTO param) {
        return goodsCommonService.updateGoodsTransportInfo(param);
    }

    /**
     * 平台查询供应商商品库
     *
     * @param platFormAllGoodsQuery
     * @return
     */
    @PostMapping("all/suppliergoods/query")
    public ResponseResult queryAllSuppliergoods(@RequestBody PlatFormAllGoodsDTO platFormAllGoodsQuery) {
        return goodsCommonService.queryAllSupplierGoods(platFormAllGoodsQuery);
    }

    /**
     * 查询待上架的经销商品
     *
     * @param param
     * @return
     */
    @PostMapping("/dealerProducts/query")
    public ResponseResult queryDealerProducts(@RequestBody ManageGdsNameQueryParamDTO param) {
        return goodsCommonService.queryDealerProducts(param);
    }


    /**
     * 查询待上架的怡亚通经销商品
     *
     * @param param
     * @return
     */
    @PostMapping("/yiyatong/dealerProducts")
    public ResponseResult queryDealerProductsYiyatong(@RequestBody ManageGdsNameQueryParamDTO param) {
        return goodsCommonService.queryDealerProductsYiyatong(param);
    }

    /**
     * 查询待上架的怡亚通经销商品规格
     *
     * @param param
     * @return
     */
    @PostMapping("/yiyatong/queryGoodsSpec")
    public ResponseResult queryGoodsSpecListByYiYaTong(@RequestBody ManageGdsNameQueryParamDTO param) {
        return goodsCommonService.queryGoodsSpecListByYiYaTong(param);
    }

    /**
     * 批量调价怡亚通经销商品
     *
     * @param param
     * @return
     */
    @PostMapping("/yiyatong/batchPriceAdjustment")
    public ResponseResult batchPriceAdjustment(@RequestBody BatchPriceAdjustmentDTO param) {
        return goodsCommonService.batchPriceAdjustment(param);
    }

    /**
     * 批量编辑怡亚通经销商品
     *
     * @param param
     * @return
     */
    @PostMapping("/yiyatong/batchEdit")
    public ResponseResult batchEdit(@RequestBody BatchPriceAdjustmentDTO param) {
        return goodsCommonService.batchEdit(param);
    }

    /**
     * 待上架的经销商品  查询商品价格
     *
     * @param param
     * @return
     */
    @PostMapping("/querySupperGoodsPrice")
    public ResponseResult querySupperGoodsPrice(@RequestBody Map<String, Object> param) {
        ResponseResult result = goodsCommonService.querySupperGoodsPrice(param);
        return result;
    }

    /**
     * 待上架的经销商品  怡亚通
     *
     * @param param
     * @return
     */
    @PostMapping("/yiyatong/querySupperGoodsPrice")
    public ResponseResult queryYiYaTongGoodsPrice(@RequestBody Map<String, Object> param) {
        ResponseResult result = goodsCommonService.queryYiYaTongGoodsPrice(param);
        return result;
    }

    /**
     * 待上架的经销商品  修改商品价格
     */
    @PostMapping("/updateSupperGoodsPrice")
    public ResponseResult updateSupperGoodsPrice(@RequestBody UpdateSupperGoodsPriceDTO param) {
        ResponseResult result = goodsCommonService.updateSupperGoodsPrice(param);
        return result;
    }

    /**
     * 待上架的经销商品  修改商品价格分类--怡亚通
     */
    @PostMapping("/yiyatong/updateSupperGoodsPrice")
    public ResponseResult updateYiyatongGoodsPrice(@RequestBody UpdateSupperGoodsPriceDTO param) {
        ResponseResult result = goodsCommonService.updateYiyatongGoodsPrice(param);
        return result;
    }

    /**
     * 待上架的经销商品  批量上架
     */
    @PostMapping("/updateSupperGoodsStart")
    public ResponseResult updateSupperGoodsStart(@RequestBody Map<String, Object> param) {
        ResponseResult result = goodsCommonService.updateSupperGoodsStart(param);
        return result;
    }


    /**
     * 查询商品信息
     *
     * @param commonId
     * @return
     */
    @GetMapping("/feign/SelfSupportIndexGoodsVO")
    public SelfSupportIndexGoodsVO selectByCommonId(@RequestParam("commonId") Integer commonId) {
        SelfSupportIndexGoodsVO result = goodsCommonService.selectByCommonId(commonId);
        return result;
    }

    /**
     * 修改商品活动状态
     *
     * @param param
     */
    @PostMapping("/feign/updateCommonIsXian3")
    public void updateCommonIsXian3(@RequestBody CommonIsXianUpdateDTO param) {
        goodsCommonService.updateCommonIsXian3(param);
    }

    /**
     * 查询商品信息
     *
     * @param commonId
     * @return
     */
    @GetMapping("/feign/selectManjianzengzheMessage")
    public Map<String, Object> selectManjianzengzheMessage(@RequestParam("commonId") Integer commonId) {
        Map<String, Object> result = goodsCommonService.selectManjianzengzheMessage(commonId);
        return result;
    }

    /**
     * 查询商品信息
     *
     * @param commonId
     * @return
     */
    @GetMapping("/feign/selectByCommonId")
    public GdsDetailsCommonQueryResultVO selectByCommonIdFeign(@RequestParam("commonId") Integer commonId) {
        GdsDetailsCommonQueryResultVO result = goodsCommonService.selectByCommonIdFeign(commonId);
        return result;
    }

    /**
     * 查询商品首页数量
     *
     * @param recommendParam
     * @return
     */
    @PostMapping("/feign/selectWebShopIndexCount")
    public Integer selectWebShopIndexCount(@RequestBody WebIndexRecommendQueryParamVO recommendParam) {
        Integer result = goodsCommonService.selectWebShopIndexCount(recommendParam);
        return result;
    }

    /**
     * 查询是否是自己商品
     *
     * @param param
     * @return
     */
    @PostMapping("/feign/checkShopSelf")
    public Integer checkShopSelf(@RequestBody Map<String, Object> param) {
        Integer result = goodsCommonService.checkShopSelf(param);
        return result;
    }

    /**
     * 修改商品状态
     *
     * @param param
     */
    @PostMapping("/feign/updateShopState")
    public void updateShopState(@RequestBody CommonShopStateDTO param) {
        goodsCommonService.updateShopState(param);
    }

    /**
     * 修改商品店铺状态
     *
     * @param shopId
     */
    @GetMapping("/feign/updateShopState1")
    public void updateShopState1(@RequestParam("shopId") Integer shopId) {
        goodsCommonService.updateShopState1(shopId);
    }


    /**
     * 查询店铺 三件在售商品
     *
     * @param record
     * @return
     */
    @PostMapping("/feign/selectUniappQueryShopGoods")
    public List<Map<String, Object>> selectUniappQueryShopGoods(@RequestBody ShopListQueryResultVO record) {
        List<Map<String, Object>> result = goodsCommonService.selectUniappQueryShopGoods(record);
        return result;
    }

    /**
     * 查询店铺 在售商品
     *
     * @param shopId
     * @return
     */
    @GetMapping("/feign/selectUniappQueryShopGoods")
    public List<GdsListQueryResultVO> selectByShopId3(@RequestParam("shopId") Integer shopId) {
        List<GdsListQueryResultVO> result = goodsCommonService.selectByShopId3(shopId);
        return result;
    }

    /**
     * 查询店铺 在售商品
     *
     * @param shopId
     * @return
     */
    @GetMapping("/feign/selectShopSum")
    public Integer selectShopSum(@RequestParam("shopId") Integer shopId) {
        Integer result = goodsCommonService.selectShopSum(shopId);
        return result;
    }

    /**
     * 获取商品分类id
     *
     * @param commonId
     * @return
     */
    @GetMapping("/feign/queryGoodsCatId")
    public String queryGoodsCatId(@RequestParam("commonId") String commonId) {
        String result = goodsCommonService.queryGoodsCatId(commonId);
        return result;
    }


    /**
     * 修改商品分类id
     *
     * @param shopId
     */
    @GetMapping("/feign/updateGoodsCommon")
    public void updateGoodsCommon(@RequestParam("shopId") String shopId) {
        goodsCommonService.updateGoodsCommon(shopId);

    }


    /**
     * 修改分销商品状态
     *
     * @param distributorFlag 商品分销状态 0-不参与分销 1-参与分销
     * @param commonId        商品id
     */
    @GetMapping("/feign/updateGoodsFxStatus")
    public void updateGoodsFxStatus(@RequestParam("distributorFlag") Integer distributorFlag, @RequestParam("commonId") Integer commonId) {
        goodsCommonService.updateGoodsFxStatus(distributorFlag, commonId);
    }

    /**
     * 查询店铺商品数量
     *
     * @param shopId
     * @return
     */
    @GetMapping("/feign/selectShopGoodsCount")
    public Integer selectShopGoodsCount(@RequestParam("shopId") Integer shopId) {
        Integer result = goodsCommonService.selectShopGoodsCount(shopId);
        return result;
    }

    /**
     * 查询店铺在售商品数量
     *
     * @param shopId
     * @return
     */
    @GetMapping("/feign/selectShopSellGoodsCount")
    public Integer selectShopSellGoodsCount(@RequestParam("shopId") Integer shopId) {
        Integer result = goodsCommonService.selectShopSellGoodsCount(shopId);
        return result;
    }

    /**
     * 查询商品使用的 售卖区域模板
     *
     * @param commonId
     * @return
     */
    @GetMapping("/feign/selectTransportAreaId")
    public Integer selectTransportAreaId(@RequestParam("commonId") Integer commonId) {
        Integer result = goodsCommonService.selectTransportAreaIdFeign(commonId);
        return result;
    }

    /**
     * 查询商品使用的 售国际卖区域模板
     *
     * @param commonId
     * @return
     */
    @GetMapping("/feign/selectInternationalTransportAreaId")
    public Integer selectInternationalTransportAreaId(@RequestParam("commonId") Integer commonId) {
        Integer result = goodsCommonService.selectInternationalTransportAreaIdFeign(commonId);
        return result;
    }

    /**
     * 查询商品信息
     *
     * @param goodsIdList
     * @return
     */
    @PostMapping("/feign/selectShipping2")
    public List<WapShippingQueryResultVO> selectShipping2(@RequestBody List<Integer> goodsIdList) {
        List<WapShippingQueryResultVO> result = goodsCommonService.selectShipping2(goodsIdList);
        log.info("查询商品信息:{}", result);
        return result;
    }


    /**
     * 根据商品goodsId集合查询所有商品
     * @param goodsIdList
     * @return
     */
    @PostMapping("/feign/selectGoodsByIds")
    public List<GoodsCommonEntity> selectGoodsByIds(@RequestBody List<Integer> goodsIdList) {
        log.info("查询商品信息:{}", goodsIdList);
        if (CollUtil.isEmpty(goodsIdList)) {
            return new ArrayList<>();
        }
        List<GoodsCommonEntity> result = goodsCommonService.list(new LambdaQueryWrapper<GoodsCommonEntity>()
                .in(GoodsCommonEntity::getGoodsId, goodsIdList));
        log.info("查询商品信息:{}", result);
        return result;
    }

    /**
     * 查询运费
     *
     * @param goodsId
     * @return
     */
    @GetMapping("/feign/selectShippingInfo")
    public ShippingInfoDTO selectShippingInfo(@RequestParam("goodsId") Integer goodsId) {
        ShippingInfoDTO result = goodsCommonService.selectShippingInfo(goodsId);
        return result;
    }


    /**
     * 查询商品goodId
     *
     * @param commonId
     * @return
     */
    @GetMapping("/feign/queryFxGuigeGoodsId")
    public Integer queryFxGuigeGoodsId(@RequestParam("commonId") Integer commonId) {
        Integer result = goodsCommonService.queryFxGuigeGoodsId(commonId);
        return result;
    }


    /**
     * 查询当前商品是否是虚拟商品
     *
     * @param goodsId
     * @return
     */
    @GetMapping("/feign/queryVirtualGoods")
    public Integer queryVirtualGoods(@RequestParam("goodsId") Integer goodsId) {
        Integer result = goodsCommonService.queryVirtualGoods(goodsId);
        return result;
    }

    /**
     * 根据通用ID查询通用名称
     *
     * @param commonId 通用ID
     * @return 通用名称字符串
     */
    @GetMapping("/feign/selectCommonName")
    public String selectCommonName(@RequestParam("commonId") Integer commonId) {
        String result = goodsCommonService.selectCommonName(commonId);
        return result;
    }

    /**
     * 查询商品详情
     *
     * @param commonId
     * @return
     */
    @GetMapping("/feign/queryCommonDetail")
    public BaseWxLiveGoodsVO queryCommonDetail(@RequestParam("commonId") Integer commonId) {
        BaseWxLiveGoodsVO result = goodsCommonService.queryCommonDetail(commonId);
        return result;
    }

    /**
     * 根据商品名称查询分类
     *
     * @param commonName
     * @return
     */
    @GetMapping("/feign/selectCatIdByCommonName")
    public Integer selectCatIdByCommonName(@RequestParam("commonName") String commonName) {
        Integer result = goodsCommonService.selectCatIdByCommonName(commonName);
        return result;
    }

    /**
     * 查询自营店铺商品
     *
     * @param param
     * @return
     */
    @AdminTokenLimit
    @PostMapping("/selectSelfSupportShopGoods")
    public ResponseResult selectSelfSupportShopGoods(@RequestBody GiftGoodsDTO param) {
        return goodsCommonService.selectSelfSupportShopGoods(param);
    }

    /**
     * 商品智能推荐
     *
     * @param pageHelper
     * @return
     */
    @PostMapping("/intelligentRecommendationGoods")
    public ResponseResult intelligentRecommendationGoods(@RequestBody PageHelper pageHelper) {
        ResponseResult result = goodsCommonService.intelligentRecommendationGoods(pageHelper);
        return result;
    }

    /**
     * 减少商品库存增加商品销量
     *
     * @param wapGood
     * @return
     */
    @PostMapping("/updateSubStockAndAddSaleNum")
    public ResponseResult updateSubStockAndAddSaleNum(@RequestBody String wapGood) {

        return goodsCommonService.updateSubStockAndAddSaleNum(wapGood);
    }

    /**
     * 批量导入商品
     *
     * @param file              文件
     * @param transportAreaId   运费区域id
     * @param transportTemplate 运费模板id
     * @return 结果
     */
    @GlobalTransactional
    @PostMapping("/importGoodsBatch")
    public ResponseResult importGoodsBatch(@RequestParam("file") MultipartFile file, @RequestParam("token") String token,
                                           @RequestParam("transportAreaId") Integer transportAreaId,
                                           @RequestParam("transportTemplate") Integer transportTemplate) throws Exception {
        return goodsCommonService.importGoodsBatch(file, token, transportAreaId, transportTemplate);
    }

    /**
     * 查询商品下所有规格
     *
     * @param commonId
     * @return
     */
    @GetMapping("queryGoodsSpecStr")
    public String queryGoodsSpecStr(@RequestParam("commonId") Integer commonId) {
        return goodsCommonService.queryGoodsSpecStr(commonId);
    }
    /**
     * 查询平台代发商品
     * @param platFormAllGoodsDTO
     * @return
     */
    @PostMapping("/platFormQueryDealerGoods")
    public ResponseResult platFormQueryDealerGoods(@RequestBody PlatFormAllGoodsDTO  platFormAllGoodsDTO) {
        return goodsCommonService.platFormQueryDealerGoods(platFormAllGoodsDTO);
    }
    /**
     * 查询平台代发商品的店铺信息
     * @param platFormAllGoodsDTO
     * @return
     */
    @PostMapping("/platFormQueryDealerShop")
    public ResponseResult platFormQueryDealerShop(@RequestBody PlatFormAllGoodsDTO  platFormAllGoodsDTO) {
        return goodsCommonService.platFormQueryDealerShop(platFormAllGoodsDTO);
    }
    
    /**
     * 获取商品统计数据
     * @return
     */
    @PostMapping("/statistics")
    public ResponseResult getGoodsStatistics() {
        try {
            java.util.Map<String, Object> statistics = new java.util.HashMap<>();
            
            // 统计数据源尚未接入（StatisticsService 目前同样是占位实现）。
            // 这里显式返回 mock=true，前端据此提示「数据未接入」，别把 0 当成真实指标
            statistics.put("mock", true);
            statistics.put("totalGoods", 0);
            statistics.put("onShelfGoods", 0);
            statistics.put("offShelfGoods", 0);
            statistics.put("draftGoods", 0);
            statistics.put("stockWarning", 0);
            statistics.put("pendingAudit", 0);
            
            return ResponseResult.success(statistics);
        } catch (Exception e) {
            return ResponseResult.error("获取商品统计数据失败: " + e.getMessage());
        }
    }
}

