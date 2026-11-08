package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.base.DealerGoodsUserInfoDTO;
import com.houjiang.commons.dto.goods.*;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.dto.shop.ShopIndexDTO;
import com.houjiang.commons.dto.yiyatong.MsgCallBackDTO;
import com.houjiang.commons.dto.yiyatong.OneClickProductListingDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageHelper;
import com.houjiang.commons.vo.activity.ManjianzengzhePromotionContentQueryResultVO;
import com.houjiang.commons.vo.base.BaseWxLiveGoodsVO;
import com.houjiang.commons.vo.goods.*;
import com.houjiang.commons.vo.order.WapShippingQueryResultVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.dto.*;
import com.houjiang.goods.entity.GoodsCommonEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsCommonService
 * @Description 商品表
 * @Author Amos
 * @Date 2023-05-25 09:16:34
 * @Version 1.0
 * @author Amos
 */
public interface GoodsCommonService extends IService<GoodsCommonEntity> {
    /**
     * 根据商品id查询详情
     *
     * @param param
     * @return
     */
    ResponseResult queryGoodsDetail(GoodsDetailsDTO param);

    /**
     * 详情页的猜你喜欢
     *
     * @param param
     * @return
     */
    ResponseResult queryGuessLikeQuery(WepGuessLikeGoodsParamDTO param);

    /**
     * 根据commonid查询详情介绍
     *
     * @param commonsId
     * @return
     */
    ResponseResult queryGdsDetailDesc(Integer commonsId);

    /**
     * 推荐商品
     *
     * @param param
     * @return
     */
    ResponseResult queryRecommendGoods(WebIndexRecommendQueryParamDTO param);

    /**
     * 查询商品状态及库存
     *
     * @param commonId
     * @return
     */
    List<GdsBaseDetailResultVO> queryGoodsStartStock(Integer commonId);

    /**
     * 修改商品库存
     *
     * @param wapGood
     * @return
     */
    ResponseResult updateGoodsStock(List<WapGoodsSaleSumDTO> wapGood);

    /**
     * 修改商品销量
     *
     * @param wapGood
     * @return
     */
    ResponseResult updateGoodsSalenum(List<WapGoodsSaleSumDTO> wapGood);

    /**
     * 查询当前商品商品的 售卖区域规则
     *
     * @param goodsIds
     * @return
     */
    List<Integer> selectTransportAreaId(List<Integer> goodsIds);
    /**
     * 查询当前商品的 国外售卖区域规则
     * @param goodsIds
     * @return
     */
    List<Integer> selectInternationalTransportAreaId(List<Integer> goodsIds);

    /**
     * 更新商品收藏量
     *
     * @param paraMap
     * @return
     */
    ResponseResult updateGoodsCollect(Map<String, String> paraMap);

    /**
     * 根据goods_id查询积分商品的价格和所需的积分
     *
     * @param param
     * @return
     */
    ResponseResult queryIntegralGoodsBygoodsId(Map<String, Object> param);

    /**
     * 判断当前商品是否是是 plus 商品
     *
     * @param commonId
     * @return
     */
    Integer queryGoodsIsPlus(Integer commonId);

    /**
     * 判断当前商品是否是 虚拟商品
     *
     * @param param
     * @return
     */
    ResponseResult queryVirtualgoods(Map<String, String> param);

    /**
     * 更新商品销量和库存
     *
     * @param saleSum
     * @return
     */
    ResponseResult updateSaleAndStock(String saleSum);

    /**
     * 更新兑换券商品销量和库存
     * @param param
     * @return
     */
    ResponseResult updateProductVoucherGoodsStock(Map<String, Object> param);

    /**
     * 根据商品规格查询商品
     *
     * @param param
     * @return
     */
    ResponseResult queryGoodsSpec(GoodsDetailsDTO param);

    /**
     * 兑换券活动 根据商品规格查询商品
     * @param param
     * @return
     */
    ResponseResult productVoucherQueryGoodsSpec(GoodsDetailsDTO param);

    /**
     * 积分商品列表 pc
     *
     * @param param
     * @return
     */
    ResponseResult queryIntegralGoodsListFilter(ListPageQueryParamDTO param);

    /**
     * 查询规格值数据
     *
     * @param goodsId
     * @return
     */
    ResponseResult goodsOrderParam(String goodsId);

    /**
     * 查询店铺首页
     *
     * @param param
     * @return
     */
    ResponseResult queryShopIndex(ShopIndexDTO param);

    /**
     * 根据commonid获取商品详情
     *
     * @param commonId
     * @return
     */
    ResponseResult getDetailByCommonId(Integer commonId);


    /**
     * 根据商品id修改收藏数
     *
     * @param commonId
     * @return
     */
    ResponseResult updateCommonCollect(String commonId);

    /**
     * 取消下单时，增加库存
     *
     * @param param
     * @return
     */
    ResponseResult updateStockReduce(WapGoodsSaleSumDTO param);

    /**
     * 更新商品评论数
     *
     * @param map
     * @return
     */
    ResponseResult updateCommentCount(Map<String, Object> map);

    /**
     * 处理商品参与的活动
     *
     * @param goodsId      商品ID
     * @param commonId     活动ID
     * @param commonIsXian 商品参与的活动类型
     * @return 处理后的活动商品详情
     */
    ActivityGoodsDetailDTO dealActivity(Integer goodsId, Integer commonId, Integer commonIsXian);

    /**
     * 查询商品数据
     *
     * @param param
     * @return
     */
    ResponseResult queryByGoodsSalesum(UserTokenBeanDTO param);

    /**
     * 查询出售中的商品 goodsName不传，查询店铺所有
     *
     * @param param
     * @return
     */
    ResponseResult queryByGoodsName(ManageGdsNameQueryDTO param);

    /**
     * 根据commonId删除商品
     *
     * @param param
     * @return
     */
    ResponseResult deleteByCommonId(ManageCommonIdsDTO param);

    /**
     * 根据commonId查询商品锁状态
     *
     * @param param
     * @return
     */
    ResponseResult queryGoodsLockedStatus(GoodsDetailsDTO param);

    /**
     * plus 商品推荐
     *
     * @return
     */
    ResponseResult selectplusgoods();

    /**
     * 查看放入仓库的商品
     *
     * @param param
     * @return
     */
    ResponseResult queryStoreHouse(ManageGdsNameQueryDTO param) throws IllegalAccessException;

    /**
     * 查询等待审核的商品
     *
     * @param param
     * @return
     */
    ResponseResult queryVerifyGoods(ManageGdsNameQueryDTO param) throws IllegalAccessException;

    /**
     * 查询审核失败的商品
     *
     * @param param
     * @return
     */
    ResponseResult queryVerifyFailGoods(ManageGdsNameQueryDTO param) throws IllegalAccessException;

    /**
     * 违规的商品
     *
     * @param param
     * @return
     */
    ResponseResult queryViolationsGoods(ManageGdsNameQueryDTO param) throws IllegalAccessException;

    /**
     * 查询运费模板下所有商品
     *
     * @param param
     * @return
     */
    ResponseResult queryGoodsByTemplateId(TransportTemplateDetailDeleteDTO param) throws IllegalAccessException;

    /**
     * 查询售卖区域id下所有商品
     *
     * @param param
     * @return
     */
    ResponseResult queryGoodsByTransportAreaId(TransportTemplateDetailDeleteDTO param) throws IllegalAccessException;

    /**
     * 修改商品是否加入促销
     *
     * @param commonIdList
     */
    void updateCommonIsXian2(List<Integer> commonIdList);

    /**
     * 修改商品是否加入促销
     *
     * @param param
     */
    void updateCommonIsXian(Map<String, Object> param);

    /**
     * 查询非活动商品
     *
     * @param param
     * @return
     */
    ResponseResult queryGoodsList(GoodsListQueryDTO param);

    /**
     * 查询出售中的商品 goodsName不传，查询店铺所有   activityGoods
     *
     * @param param
     * @return
     */
    ResponseResult queryactivityGoods(ManageGdsNameQueryDTO param) throws IllegalAccessException;

    /**
     * 查询装修空间 所有商品
     **/
    /**
     * @param param 参数
     * @return 处理结果
     */
    ResponseResult queryManageSpaceAllGoods(PlatFormAllGoodsQueryDTO param);

    /**
     * 查询商品数量
     *
     * @param map
     * @return
     */
    ResponseResult goodsCount(Map<String, Object> map);

    /**
     * 查询今日商品数量
     *
     * @param map
     * @return
     */
    ResponseResult goodsTodayNumber(Map<String, Object> map);

    /**
     * 商品按月分组统计
     */
    ResponseResult queryByMonth(Map<String, Object> map);

    /**
     * 查询所有商品
     *
     * @param param
     * @return
     */
    ResponseResult queryAllGoods(PlatFormAllGoodsDTO param) throws IllegalAccessException;

    /**
     * 商品一键上架 同步商品
     *
     * @param goodsUserInfo
     * @return
     */
    ResponseResult putAway(DealerGoodsUserInfoDTO goodsUserInfo);

    /**
     * 供应商商品立即购买
     *
     * @param map
     * @return
     */
    ResponseResult dealer(Map<String, String> map);

    /**
     * 查询经销商品
     *
     * @param param
     * @return
     */
    ResponseResult queryDealerGoods(ManageGdsNameQueryDTO param) throws IllegalAccessException;


    /**
     * 积分商品编辑
     *
     * @param param
     * @return
     */
    ResponseResult updateIntegralGoods(IntegralCommonDTO param);

    /**
     * 查询商品详情
     *
     * @param map
     * @return
     */
    ResponseResult goodsQueryDetail(Map<String, Object> map);

    /**
     * 积分商品添加
     *
     * @param param
     * @return
     */
    ResponseResult addIntegralGoods(ManageGdsCommonPublishDTO param);

    /**
     * 优惠券商品列表
     *
     * @param param
     * @return
     */
    ResponseResult queryCouponList(UserVcBaseQueryDTO param);

    /**
     * 红包商品列表
     *
     * @param param
     * @return
     */
    ResponseResult queryRedPacketList(UserAvailableRedpacketQueryDTO param);

    /**
     * 查询商品、店铺审核数
     *
     * @param
     * @return
     */
    ResponseResult queryVerifyNum(Map<String, Object> map);

    /**
     * 更新spu商品
     *
     * @param map
     * @return
     */
    ResponseResult updateGoodsCommonByMap(Map<String, Object> map);

    /**
     * 根据店铺id查找所有commonId
     *
     * @param shopId
     * @return
     */
    List<Integer> queryCommonIdByShopId(Integer shopId);

    /**
     * 更新spu商品状态
     *
     * @param commonParam
     * @return
     */
    ResponseResult updateCommonState(CommonShopStateDTO commonParam);

    /**
     * 查询所有的积分商品
     *
     * @param param
     * @return
     */
    ResponseResult queryIntegralAll(ListPageQueryParamDTO param);

    /**
     * 平台商品批量 锁定
     *
     * @param param
     * @return
     */
    ResponseResult platformLockUpdate(PlatFormGoodsStateDTO param);

    /**
     * 平台商品批量 屏蔽
     *
     * @param param
     * @return
     */
    ResponseResult platformScreenUpdate(PlatFormGoodsStateDTO param);

    /**
     * 平台商品批量上、下架  审核
     *
     * @param param
     * @return
     */
    ResponseResult platformViolationsUpdate(PlatFormGoodsStateDTO param);

    /**
     * 平台查询商品详情
     *
     * @param param
     * @return
     */
    ResponseResult platformQueryDetail(ManageGdsNameQueryParamDTO param);

    /**
     * 获取门店列表库存信息
     *
     * @param param
     * @return
     */
    ResponseResult queryGateCommonsList(GateQueryCommonsDTO param);

    /**
     * 获取商品下规格的详情
     *
     * @param param
     * @return
     */
    ResponseResult queryGoodsSpecList(ManageCommonIdsDTO param);

    /**
     * 门店设置库存
     *
     * @param param
     * @return
     */
    ResponseResult updateGateShopStock(GateUpShopGoodsStockDTO param);

    /**
     * 商家经销审核
     *
     * @param dto
     * @return
     */
    ResponseResult checkDealer(DealerGoodsUserInfoDTO dto);

    /**
     * 查询商家是否是指定供应商的经销商
     *
     * @param dto
     * @return
     */
    ResponseResult queryGoodsUser(DealerGoodsUserInfoDTO dto);

    /**
     * 商家中心-商品信息查询
     *
     * @param dto
     * @return
     */
    ResponseResult queryGoodsDetailByBusiness(ManageCommonIdsDTO dto);

    /**
     * 供应商商品列表页
     *
     * @param dto
     * @return
     */
    ResponseResult queryGoodsListByBusiness(WebGoodsListQueryDTO dto) throws IllegalAccessException;

    /**
     * 获取店铺所有商品
     *
     * @param param
     * @return
     */
    ResponseResult livequeryByGoodsName(ManageGdsNameQueryDTO param);

    /**
     * 判断商品是否可以添加
     *
     * @param param
     * @return
     */
    ResponseResult seckillCheckGoods(ManageSeckillBaseUpdateDTO param);

    /**
     * 检验商品名称是否重复
     *
     * @param param
     * @return
     */
    ResponseResult checkGoodsName(CheckNameQueryParamDTO param);

    /**
     * 添加plus商品
     *
     * @param map
     * @return
     */
    ResponseResult updatePlusGoods(Map<String, Object> map);


    /**
     * 查询商品信息
     *
     * @param map
     * @return
     */
    ResponseResult selecPlustGoodsByPage(Map<String, Object> map);


    /**
     * 删除plus商品
     *
     * @param map
     * @return
     */
    ResponseResult deletePlusGoods(Map<String, Object> map);

    /**
     * 查询可以添加兑换券活动的商品
     * @param map
     * @return
     */
    ResponseResult selecProductVoucherGoodsByPage(Map<String, Object> map);
    /**
     *  查询 兑换券商品信息
     * @param productVoucherGoodsVOS
     * @return
     */
    List<ProductVoucherGoodsVO> selecProductVoucherGoods(List<ProductVoucherGoodsVO> productVoucherGoodsVOS);


    /**
     * 查询赠品
     *
     * @param param
     * @return
     */
    ResponseResult queryGiveawayList(ManjianzengzheListQueryDTO param) throws IllegalAccessException;


    /**
     * 判断商品是否可以添加
     *
     * @param param
     * @return
     */
    ResponseResult discountCheckGoods(DiscountBaseAddParamDTO param);


    /**
     * 查询商品列表 暂不用
     *
     * @param param
     * @return
     */
    ResponseResult queryGoodsListByPintuan(Map<String, Object> param);


    /**
     * 查询商家同步供应商的商品 但未修改过价格的
     */
    ResponseResult queryIsPriceUpdate(ManageGdsNameQueryDTO param) throws IllegalAccessException;

    /**
     * 待发布的商品
     *
     * @param param
     * @return
     */
    ResponseResult queryWaitRelease(ManageGdsNameQueryDTO param) throws IllegalAccessException;

    /**
     * 商品上架
     *
     * @param param
     * @return
     */
    ResponseResult setGoodsOnline(ManageCommonIdsDTO param);


    /**
     * 校验店铺规格下是否有商品
     *
     * @param param
     * @return
     */
    ResponseResult checkGoodsBySpecId(Map<String, Object> param);



    /**
     * 商品下架
     *
     * @param param
     * @return
     */
    ResponseResult setGoodsOffline(ManageCommonIdsDTO param);

    /**
     * 商品搜索列表
     *
     * @param param
     * @return
     */
    ResponseResult shopListQueryByShopFilter(ListPageQueryParamDTO param);

    /**
     * 活动
     *
     * @param goodsId
     * @param goodsMaxSale
     * @param commonId
     * @param commonIsXian
     * @param userId
     * @param sessionTime
     * @return
     */
    ActivityGoodsDetailDTO dealActivity2(Integer goodsId, Integer goodsStock, Integer goodsMaxSale, Integer commonId, Integer commonIsXian, String userId, Date sessionTime);

    /**
     * 生成活动内容
     *
     * @param r
     */
    void dealPromotionContent(ManjianzengzhePromotionContentQueryResultVO r);

    /**
     * 添加足迹
     *
     * @param userinfo
     * @param commonId
     * @param catId
     */
    void addUserFoot(UserInfoVO userinfo, Integer commonId, Integer catId);

    /**
     * 查询商品信息
     *
     * @param param
     * @return
     */
    ResponseResult queryGoods(Map<String, Object> param);

    /**
     * 全部宝贝下面分类
     *
     * @param param
     * @return
     */
    ResponseResult queryByFilter(ShopIndexQueryParamDTO param) throws IllegalAccessException;

    /**
     * 店铺列表
     *
     * @param param
     * @return
     */
    ResponseResult queryShopIndexquery(ShopIndexQueryParamDTO param) throws IllegalAccessException;


    /**
     * 商家端-图片导入传入zip包
     *
     * @param sourceZipFileBytes
     * @return
     */
    ResponseResult zipLocalDiskImage(byte[] sourceZipFileBytes, List<String> commonList) throws IOException;


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
    ResponseResult insertGoodsBatch(MultipartFile file, String token, Integer transportAreaId, Integer transportTemplate) throws Exception;


    /**
     * 导入
     *
     * @param dataParam
     * @param param
     * @return
     */
    Map<String, Object> importExcelData(Map<String, Object> dataParam, GoodsBatchCommonVO param);

    /**
     * 满赠减少赠品库存增加商品销量
     *
     * @param commonIdList
     * @return
     */
    void updateGiveawayStock(List<Integer> commonIdList);

    /**
     * 查询待发布的商品
     *
     * @param param
     * @return
     */
    ResponseResult queryToBeReleased(ManageGdsNameQueryParamDTO param);

    /**
     * 定时发布商品--立即发布
     *
     * @param param
     * @return
     */
    ResponseResult fixedTimeGoodsPublishNow(ManageCommonIdsDTO param);

    /**
     * 放入仓库商品--立即审核
     *
     * @param param
     * @return
     */
    ResponseResult putIntoWarehouseGoodsReviewingNow(ManageCommonIdsDTO param);

    /**
     * 查询平台礼包商品
     *
     * @param param
     * @return
     */
    ResponseResult selectPlatformGiftGoods(GiftGoodsDTO param);


    /**
     * 添加自营店铺商品
     *
     * @param param
     * @return
     */
    ResponseResult addPlatformGiftGoods(GiftGoodsDTO param);

    /**
     * 移除自营店铺商品
     *
     * @param param
     * @return
     */
    ResponseResult removePlatformGiftGoods(GiftGoodsDTO param);

    /**
     * 查询商品规格列表
     *
     * @param param
     * @return
     */
    ResponseResult queryPlatformGoodsSpecList(ManageCommonIdsDTO param);

    /**
     * 查询兑换券下单 商品信息 feign
     * @param commonId
     * @return
     */
    GdbMerchantGoodsCommonVO queryProductVoucherGoods(Integer commonId);

    /**
     * 根据商品名称模糊查询商品id feign
     * @param commonName
     * @return
     */
    List<Integer> queryLikeGoodsName(String commonName);

    /**
     * 批量修改商品物流信息
     *
     * @param param
     * @return
     */
    ResponseResult updateGoodsTransportInfo(UpdateGoodsTransportInfoDTO param);

    /**
     * 一键代发
     */
    ResponseResult oneClickDropShipping(OneClickProductListingDTO oneClickProductListingDTO);

    /**
     * 一键代发
     */
    ResponseResult msgCallBack(MsgCallBackDTO msgCallBackDTO);

    /**
     * 查询待上架经销商品
     *
     * @param param
     * @return
     */
    ResponseResult queryDealerProducts(ManageGdsNameQueryParamDTO param);

    /**
     * 查询待上架怡亚通经销商品
     *
     * @param param
     * @return
     */
    ResponseResult queryDealerProductsYiyatong(ManageGdsNameQueryParamDTO param);

    /**
     * 查询待上架怡亚通经销商品规格
     *
     * @param param
     * @return
     */
    ResponseResult queryGoodsSpecListByYiYaTong(ManageGdsNameQueryParamDTO param);

    /**
     * 批量调价
     *
     * @param param
     * @return
     */
    ResponseResult batchPriceAdjustment(BatchPriceAdjustmentDTO param);

    /**
     * 批量调价
     *
     * @param param
     * @return
     */
    ResponseResult batchEdit(BatchPriceAdjustmentDTO param);

    /**
     * 待上架的经销商品  查询商品价格
     *
     * @param param
     * @return
     */
    ResponseResult querySupperGoodsPrice(Map<String, Object> param);

    /**
     * 待上架的经销商品  查询商品价格
     *
     * @param param
     * @return
     */
    ResponseResult queryYiYaTongGoodsPrice(Map<String, Object> param);

    /**
     * 待上架的经销商品  修改商品价格
     *
     * @param param
     * @return
     */
    ResponseResult updateSupperGoodsPrice(UpdateSupperGoodsPriceDTO param);

    /**
     * 待上架的经销商品  修改商品价格---yiyatong
     *
     * @param param
     * @return
     */
    ResponseResult updateYiyatongGoodsPrice(UpdateSupperGoodsPriceDTO param);


    /**
     * 待上架的经销商品  批量上架
     *
     * @param param
     * @return
     */
    ResponseResult updateSupperGoodsStart(Map<String, Object> param);
    /**
     * 根据入住分类查询商品分类
     *
     * @param param
     * @return
     */
    ResponseResult supplierQueryByJoin(ManageItemCatQueryDTO param);

    /**
     * 平台查询供应商商品库
     *
     * @param platFormAllGoodsQuery
     * @return
     */
    ResponseResult queryAllSupplierGoods(PlatFormAllGoodsDTO platFormAllGoodsQuery);

    /**
     * 根据商品ID列表选择供应商运输区域ID
     *
     * 此方法用于根据一组商品ID来查询与这些商品关联的供应商运输区域ID
     * 主要用于物流或供应链管理系统中，以便确定商品的配送范围或物流成本
     *
     * @param goodsIds 商品ID列表，用于查询供应商运输区域ID
     * @return 返回与商品ID关联的供应商运输区域ID列表
     */
    List<Integer> selectSupplierTransportAreaId(List<Integer> goodsIds);


    /**
     * 查询商品信息
     * @param commonId
     * @return
     */
    SelfSupportIndexGoodsVO selectByCommonId(Integer commonId);


    /**
     * 修改商品活动状态
     * @param param
     */
    void updateCommonIsXian3(CommonIsXianUpdateDTO param);



    /**
     * 查询商品信息
     * @param commonId
     * @return
     */
    Map<String, Object> selectManjianzengzheMessage(Integer commonId);


    /**
     * 查询商品信息
     * @param commonId
     * @return
     */
    GdsDetailsCommonQueryResultVO selectByCommonIdFeign(Integer commonId);


    /**
     * 查询商品首页数量
     *
     * @param recommendParam
     * @return
     */
    Integer selectWebShopIndexCount(WebIndexRecommendQueryParamVO recommendParam);


    /**
     * 查询是否是自己商品
     *
     * @param param
     * @return
     */
    Integer checkShopSelf(Map<String, Object> param);


    /**
     * @param param
     */
    void updateShopState(CommonShopStateDTO param);


    /**
     * 修改商品状态
     * @param shopId
     */
    void updateShopState1(Integer shopId);

    /**
     * 查询店铺 三件在售商品
     *
     * @param record
     * @return
     */
    List<Map<String, Object>> selectUniappQueryShopGoods(ShopListQueryResultVO record);

    /**
     * 查询店铺 在售商品
     * @param shopId
     * @return
     */
    List<GdsListQueryResultVO> selectByShopId3(Integer shopId);

    /**
     * 查询店铺 在售商品
     * @param shopId
     * @return
     */
    Integer selectShopSum(Integer shopId);


    /**
     * 获取商品分类id
     * @param commonId
     * @return
     */
    String queryGoodsCatId(String commonId);

    /**
     * 修改商品分类id
     * @param shopId
     */
    void updateGoodsCommon(String shopId);


    /**
     * 修改分销商品状态
     * @param distributorFlag 商品分销状态 0-不参与分销 1-参与分销
     * @param commonId 商品id
     */
    void updateGoodsFxStatus(Integer distributorFlag,Integer commonId);


    /**
     * 查询店铺商品数量
     * @param shopId
     * @return
     */
    Integer selectShopGoodsCount(Integer shopId);


    /**
     * 查询店铺在售商品数量
     * @param shopId
     * @return
     */
    Integer selectShopSellGoodsCount(Integer shopId);


    /**
     * 查询商品使用的 售卖区域模板
     * @param commonId
     * @return
     */
    Integer selectTransportAreaIdFeign(Integer commonId);
    /**
     * 查询商品使用的 售国际卖区域模板
     * @param commonId
     * @return
     */
    Integer selectInternationalTransportAreaIdFeign(Integer commonId);

    /**
     * 查询商品信息
     * @param goodsIdList
     * @return
     */
    List<WapShippingQueryResultVO> selectShipping2(List<Integer> goodsIdList);


    /**
     * 查询运费
     *
     * @param goodsId
     * @return
     */
    ShippingInfoDTO selectShippingInfo(Integer goodsId);


    /**
     * 查询商品goodId
     * @param commonId
     * @return
     */
    Integer queryFxGuigeGoodsId(Integer commonId);



    /**
     * 查询当前商品是否是虚拟商品
     * @param goodsId
     * @return
     */
    Integer queryVirtualGoods(Integer goodsId);

    /**
     * 根据通用ID查询通用名称
     *
     * @param commonId 通用ID
     * @return 通用名称字符串
     */
    String selectCommonName(Integer commonId);

    /**
     * 查询商品详情
     * @param commonId
     * @return
     */
    BaseWxLiveGoodsVO queryCommonDetail(Integer commonId);

    /**
     * 根据商品名称查询分类
     * @param commonName
     * @return
     */
    Integer selectCatIdByCommonName(String commonName);
    /**
     * 查询自营店铺商品
     *
     * @param param
     * @return
     */
    ResponseResult selectSelfSupportShopGoods(GiftGoodsDTO param);

    /**
     * 商品智能推荐
     * @param pageHelper
     * @return
     */
    ResponseResult intelligentRecommendationGoods(PageHelper pageHelper);

    /**
     * 减少商品库存增加商品销量
     *
     * @param wapGood
     * @return
     */
    ResponseResult updateSubStockAndAddSaleNum(String wapGood);

    /**
     * 批量导入商品
     * @param file 文件
     * @param transportAreaId 运费区域id
     * @param transportTemplate 运费模板id
     * @return 结果
     */
    ResponseResult importGoodsBatch(MultipartFile file, String token, Integer transportAreaId, Integer transportTemplate);

    /**
     * 查询商品下所有规格
     * @param commonId
     * @return
     */
    String queryGoodsSpecStr(Integer commonId);

    /**
     * 查询平台代发商品
     * @param platFormAllGoodsDTO
     * @return
     */
    ResponseResult platFormQueryDealerGoods(PlatFormAllGoodsDTO platFormAllGoodsDTO);

    /**
     * 查询平台代发商品的店铺信息
     * @param platFormAllGoodsDTO
     * @return
     */
    ResponseResult  platFormQueryDealerShop(PlatFormAllGoodsDTO platFormAllGoodsDTO);
}

