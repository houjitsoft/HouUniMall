package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.dto.goods.*;
import com.houjiang.commons.dto.shop.ShopIndexDTO;
import com.houjiang.commons.vo.activity.HouRedpacketTemplateVO;
import com.houjiang.commons.vo.base.*;
import com.houjiang.commons.vo.goods.*;
import com.houjiang.commons.vo.order.WapShippingQueryResultVO;
import com.houjiang.commons.vo.shop.ListQueryResultVO;
import com.houjiang.commons.vo.shop.ShopGoodsCatVO;
import com.houjiang.commons.vo.shop.ShopIndexVO;
import com.houjiang.goods.dto.*;
import com.houjiang.goods.entity.GoodsCommonEntity;
import com.houjiang.goods.vo.VoucherMessageVO;
import com.houjiang.goods.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * (GoodsCommon)
 * 商品表
 *
 * @since 2023-05-19 13:52:01
 * @author Amos
 */
@Mapper
public interface GoodsCommonMapper extends BaseMapper<GoodsCommonEntity> {

    /**
     * 根据commid 查询店铺id
     *
     * @param commonId
     * @return
     */
    GoodsListQueryEsDTO selectShopIdByCommonId(Integer commonId);

    /**
     * 根据shopid 查询当前店铺的商品
     *
     * @param shopId
     * @return
     */
    List<GoodsListQueryEsDTO> selectByShopId3(@Param("shopId") Integer shopId);

    /**
     * 查询店铺的商品数量
     *
     * @param shopId
     * @return
     */
    Integer selectShopSum(@Param("shopId") Integer shopId);

    /**
     * 获取商品分类id
     * @param commonId
     * @return
     */
    String queryGoodsCatId(@Param("commonId")String commonId);

    /**
     * 查询店铺的商品数量
     *
     * @param shopId
     * @return
     */
    Integer selectShopSumFeign(@Param("shopId") Integer shopId);

    /**
     * 查询店铺商品信息
     *
     * @param shopListQueryDto
     * @return
     */
    List<Map<String, Object>> selectuniappQueryShopGoods(@Param("param") ShopListQueryDTO shopListQueryDto);

    /**
     * 查询商品详情
     *
     * @param goodsId
     * @return
     */
    GdsBaseDetailResultVO selectByGoodsId(@Param("goodsId") String goodsId);

    /**
     * 查询当前店铺商品数量
     *
     * @param shopId
     * @return
     */
    Integer selectByShopId(@Param("shopId") Integer shopId);

    /**
     * 查询商品公共信息
     *
     * @param commonId
     * @return
     */
    GdsCommonResultVO selectTransport(@Param("commonId") Integer commonId);

    /**
     * 满减增折活动 查询赠品活动信息
     *
     * @param commonId
     * @return
     */
    GdsDetailsCommonQueryResultVO selectByCommonId(@Param("commonId") Integer commonId);


    /**
     * 通过commonId查询
     *
     * @param commonId
     * @return
     */
    ManageGdsCommonVO selectByCommonIdInfo(@Param("commonId") Integer commonId);

    /**
     * 更新商品点击量
     *
     * @param commonId
     */
    void updateGoodsClick(@Param("commonId") Integer commonId);

    /**
     * 查询当前的商品所属的分类
     *
     * @param goodsId
     * @return
     */
    Map<String, Object> selectCatIdByGoodsId(@Param("goodsId") Integer goodsId);

    /**
     * 查询猜你喜欢商品
     *
     * @param param
     * @return
     */
    IPage<GdsListQueryResultVO> selectLikeGoodsByCatId(IPage<GdsListQueryResultVO> page, @Param("param") WepGuessLikeGoodsParamDTO param);

    /**
     * 查询商品属性
     *
     * @param commonsId
     * @return
     */
    CommonDetailsDescVO selectCommonDetailsDesc(@Param("commonId") Integer commonsId);

    /**
     * 推荐商品
     *
     * @param param
     * @return
     */
    List<GdsListQueryResultVO> queryRecommendGoods(@Param("param") WebIndexRecommendQueryParamDTO param);

    /**
     * 查询商品状态及库存
     *
     * @param commonId
     * @return
     */
    List<GdsBaseDetailResultVO> queryGoodsStartStock(@Param("commonId") Integer commonId);

    /**
     * 查询当前商品商品的 售卖区域规则
     *
     * @param goodsIds
     * @return
     */
    List<Integer> selectTransportAreaId(@Param("goodsIds") List<Integer> goodsIds);
    /**
     * 查询当前商品的 国外售卖区域规则
     * @param goodsIds
     * @return
     */
    List<Integer> selectInternationalTransportAreaId(@Param("goodsIds") List<Integer> goodsIds);

    /**
     * 更新商品收藏量
     *
     * @param paraMap
     */
    void updateGoodsCollect(@Param("param") Map<String, String> paraMap);

    /**
     * 根据goods_id查询积分商品的价格和所需的积分
     *
     * @param param
     * @return
     */
    Map<String, Object> queryIntegralGoodsBygoodsId(Map<String, Object> param);

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
     * @param goodsId
     * @return
     */
    Map<String, Object> queryVirtualgoods(String goodsId);

    /**
     * 更新商品库存
     *
     * @param wapGoodsSaleSumUpdate
     */
    void updateStock(@Param("param") WapGoodsSaleSumUpdateVO wapGoodsSaleSumUpdate);

    /**
     * 查询当前商品是否试供应商商品
     *
     * @param goodsId
     * @return
     */
    Map<String, Object> querydealerGoodsFlag(@Param("goodsId") Integer goodsId);

    /**
     * 查询供应商商品信息
     *
     * @param dealerGoods
     * @return
     */
    WapGoodsSaleSumUpdateVO querySupperGoodsInfo(@Param("dealerGoods") Map<String, Object> dealerGoods);

    /**
     * 查询足迹商品信息
     *
     * @param param
     * @return
     */
    List<GdsListQueryResultVO> selectUserFoot(@Param("param") CartGuessLikeGoodsQueryDTO param);

    /**
     * 查询商品列表
     *
     * @param page
     * @param param
     * @return
     */
    IPage<GdsListQueryResultVO> selectByWebFilter(IPage<GdsListQueryResultVO> page, @Param("param") ListPageQueryParamDTO param);

    /**
     * 查询热搜词
     *
     * @param commonName
     * @return
     */
    GdbMerchantSearchWordVO selectBySearchName(@Param("commonName") String commonName);

    /**
     * 修改热搜词
     *
     * @param searchWord
     */
    void updateByPrimaryKeySelective(GdbMerchantSearchWordVO searchWord);

    /**
     * 添加热搜词
     *
     * @param record
     */
    void insertSelective(GdbMerchantSearchWordVO record);

    /**
     * 查询商品规格信息
     *
     * @param goodsId
     * @return
     */
    Map<String, Object> goodsOrderParam(@Param("goodsId") String goodsId);


    /**
     * 分页查询足迹商品
     *
     * @param page
     * @param param
     * @return
     */
    IPage<WapCommonBaseUserFootVO> selectByCommonIds(IPage<WapCommonBaseUserFootVO> page, @Param("param") WapUserFootPrintDTO param);


    /**
     * 查询商品
     *
     * @param param
     * @return
     */
    List<ListQueryResultVO> selectByShopIndex(@Param("param") ShopIndexDTO param);


    /**
     * 查询审核过的商品
     *
     * @param shopId
     * @return
     */
    Integer selectVerify(@Param("shopId") Integer shopId);

    /**
     * 出售中商品数量
     *
     * @param shopId
     * @return
     */
    Integer selectSaleing(@Param("shopId") Integer shopId);

    /**
     * 待审核商品数量
     *
     * @param shopId
     * @return
     */
    Integer selectWaitVerify(@Param("shopId") Integer shopId);

    /**
     * 违规商品
     *
     * @param param
     * @return
     */
    Integer selectViolationsGoodsCount(@Param("param") ManageGdsNameQueryDTO param);

    /**
     * 已下架商品
     *
     * @param param
     * @return
     */
    Integer selectStoreGoodsCount(@Param("param") ManageGdsNameQueryDTO param);

    /**
     * 等待审核的商品
     *
     * @param param
     * @return
     */
    Integer selectVerifyGoodsCount(@Param("param") ManageGdsNameQueryDTO param);

    /**
     * 审核失败的商品
     *
     * @param param
     * @return
     */
    Integer selectVerifyFailGoodsCount(@Param("param") ManageGdsNameQueryDTO param);

    /**
     * 待上架经销商品
     *
     * @param param
     * @return
     */
    Integer queryIsPriceUpdateCount(@Param("param") ManageGdsNameQueryDTO param);

    /**
     * 违规下架商品
     *
     * @param shopId
     * @return
     */
    Integer selectIrregularities(@Param("shopId") Integer shopId);

    /**
     * 查询出售商品
     *
     * @param param
     * @return
     */
    IPage<ManageGdsNameQueryVO> selectByGdsName(IPage<ManageGdsNameQueryVO> iPage,@Param("param") ManageGdsNameQueryDTO param);

    /**
     * 查询代金卷模板id和名称
     *
     * @param commonId
     * @return
     */
    List<VoucherMessageVO> selectVoucherMes(@Param("commonId") Integer commonId);

    /**
     * 条件查总数
     *
     * @param param
     * @return
     */
    Integer selectByGdsNameCount(@Param("param") ManageGdsNameQueryDTO param);

    /**
     * 删除商品
     *
     * @param commonIds
     */
    void updateCommonDel(@Param("commonIds") List<Integer> commonIds);

    /**
     * 经销商品commonId
     *
     * @param commonId
     * @return
     */
    List<Integer> queryIsSupperGoods(@Param("commonId") Integer commonId);

    /**
     * 删除商品
     *
     * @param commonIdList
     */
    int cancelDealerGoods(@Param("commonIdList") List<Integer> commonIdList);

    /**
     * 根据commonId查询商品锁状态
     *
     * @param commonId
     * @return
     */
    Integer selectGoodsLockedStatus(@Param("commonId") String commonId);

    /**
     * 修改 newInsertGdsCommon 接口 为后面的数据库规范做准备
     *
     * @param param
     * @return
     */
    Integer newInsertGdsCommonObj(ManageGdsCommonPublishDTO param);

    /**
     * 更新商品规格
     *
     * @param param
     */
    void updateGdsSpec(@Param("param") UpdateGdsSpecParamDTO param);


    /**
     * 更新common表数据
     *
     * @param param
     */
    void updateGdsCommonObj(ManageGdsCommonUpdateDTO param);

    /**
     * 查询common信息
     *
     * @param commonId
     * @return
     */
    ManageGdsCommonVO selectByCommonId2(@Param("commonId") Integer commonId);

    /**
     * plus 商品推荐
     *
     * @return
     */
    List<PcConfigGoodsVO> pcConfigGoods();

    /**
     * 查看放入仓库的商品
     *
     * @param param
     * @return
     */
    IPage<ManageGdsNameQueryVO> selectStoreGoods(IPage<ManageGdsNameQueryVO> page , @Param("param") ManageGdsNameQueryDTO param);

    /**
     * 查询等待审核的商品
     *
     * @param param
     * @return
     */
    IPage<ManageGdsNameQueryVO> selectVerifyGoods(IPage<ManageGdsNameQueryVO> page , @Param("param") ManageGdsNameQueryDTO param);

    /**
     * 查询审核失败的商品
     *
     * @param param
     * @return
     */
    IPage<ManageGdsNameQueryVO> selectVerifyFailGoods(IPage<ManageGdsNameQueryVO> page , @Param("param") ManageGdsNameQueryDTO param);

    /**
     * 违规的商品
     *
     * @param param
     * @return
     */
    IPage<ManageGdsNameQueryVO> selectViolationsGoods(IPage<ManageGdsNameQueryVO> page , @Param("param") ManageGdsNameQueryDTO param);
    /**
     * 查询运费模板下所有商品
     *
     * @param param
     * @return
     */
    IPage<ManageTransportGoodsVO> selectByTemplateId(IPage<ManageTransportGoodsVO> page , @Param("param") TransportTemplateDetailDeleteDTO param);

    /**
     * 查询运费模板下所有商品的总数
     *
     * @param param
     * @return
     */
    Integer selectByTemplateIdCount(@Param("param") TransportTemplateDetailDeleteDTO param);

    /**
     * 查询售卖区域id下所有商品
     *
     * @param param
     * @return
     */
    IPage<ManageTransportGoodsVO> selectByTransportAreaId(IPage<ManageTransportGoodsVO> page , @Param("param") TransportTemplateDetailDeleteDTO param);


    /**
     * 查询售卖区域id下所有商品总数
     *
     * @param param
     * @return
     */
    Integer selectByTransportAreaIdCount(@Param("param") TransportTemplateDetailDeleteDTO param);

    /**
     * 修改商品是否加入促销
     *
     * @param commonIdList
     */
    void updateCommonIsXian2(@Param("commonIdList") List<Integer> commonIdList);

    /**
     * 修改商品是否加入促销
     *
     * @param commonIsXian
     * @param commonId
     */
    void updateCommonIsXian(@Param("commonIsXian") Integer commonIsXian, @Param("commonId") Integer commonId);

    /**
     * 查询非活动商品
     *
     * @param param
     * @return
     */
    List<PromotionGoodsListVO> selectPromotionGoodsList(@Param("param") GoodsListQueryDTO param);

    /**
     * 查询非活动商品数量
     *
     * @param param
     * @return
     */
    Integer selectPromotionGoodsListCount(@Param("param") GoodsListQueryDTO param);

    /**
     * 查询出售中的商品 goodsName不传，查询店铺所有   activityGoods
     *
     * @param param
     * @return
     */
    IPage<ManageGdsNameQueryVO> selectactivityGoods(IPage<ManageGdsNameQueryVO> page , @Param("param") ManageGdsNameQueryDTO param);

    /**
     * 查询出售中的商品总数 goodsName不传，查询店铺所有   activityGoods
     *
     * @param param
     * @return
     */
    Integer selectactivityGoodsCount(@Param("param") ManageGdsNameQueryDTO param);


    /**
     * 查询装修空间 所有商品
     **/
    /**
     * @param page 分页
     * @return 处理结果
     */
    IPage<PlatFormAllGoodsVO> selectByAllGoodsToSpace(IPage<PlatFormAllGoodsVO> page, @Param("param") PlatFormAllGoodsQueryDTO param);

    /**
     * 查询专区名称
     */
    List<String> selectNoneName(Integer commonId);

    /**
     * 查询全部商品数量
     *
     * @return
     */
    Integer queryAllGoodsCount();

    /**
     * 查询每日商品数量
     *
     * @return
     */
    Integer queryTodayGoodsCount();

    /**
     * 查询每周商品数量
     *
     * @return
     */
    Integer queryWeekGoodsCount();

    /**
     * 查询每月商品数量
     *
     * @return
     */
    Integer queryMonthGoodsCount();

    /**
     * 查询每年商品数量
     *
     * @return
     */
    Integer queryYearGoodsCount();


    /**
     * 查询装修空间 所有商品
     * @param page
     * @param param
     * @return
     */
    IPage<PlatFormAllGoodsVO> selectByAllGoods(IPage<PlatFormAllGoodsVO> page, @Param("param") PlatFormAllGoodsDTO param);

    /**
     * 查询当前商品不同规格下的库存
     * @param commonId
     * @return
     */
    List<GoodsStockVO> selectStocks(@Param("commonId") Integer commonId);

    /**
     * 商品按月分组统计
     */
    Integer queryGoodsGrouping2(@Param("param") Map<String, String> stringStringMap);

    /**
     * 查询商品信息
     *
     * @param commonId
     * @return
     */
    ManageGdsCommonResultVO selectPutawayGoodsInfo(@Param("commonId") Integer commonId);

    /**
     * 查询店铺信息
     *
     * @param shopId
     * @return
     */
    ManageShopBaseQueryResultVO selectByShopId2(@Param("shopId") String shopId);

    /**
     * 经销商品配置信息
     *
     * @param commonId
     * @return
     */
    BaseGoodsConfigVO selectDelayGoodsConfig(Integer commonId);

    /**
     * 批量查询商品详情
     *
     * @param commonIdList
     * @return
     */
    List<ManageGdsCommonResultVO> queryCommonInfoList(@Param("commonIdList") List<Integer> commonIdList);

    /**
     * 查询商品信息
     *
     * @param commonId
     * @return
     */
    GdbMerchantGoodsCommonVO selectsupplyGoodsCommonInfo(Integer commonId);

    /**
     * 商品添加
     *
     * @param goodsCommon
     */
    void insertGoodsCommon(GdbMerchantGoodsCommonVO goodsCommon);

    /**
     * 修改商品规格
     *
     * @param updateGdsSpecParam
     */
    void updateGdsSpec2(@Param("param") UpdateGdsSpecParamVO updateGdsSpecParam);

    /**
     * 插入商品同步记录
     *
     * @param record
     */
    void insertputawayRecord(DealerPutawayRecordVO record);

    /**
     * 查询经销商品
     *
     * @param param
     * @return
     */
    IPage<ManageGdsNameQueryVO> queryDealerGoods(IPage<ManageGdsNameQueryVO> page, @Param("param") ManageGdsNameQueryDTO param);



    /**
     * 更新common数据
     *
     * @param param
     */
    void updateGdsCommon(IntegralCommonDTO param);

    /**
     * 更新商品
     *
     * @param goodsIds
     * @param commonId
     */
    void updateGoodsIsShelves(@Param("goodsIds") List<Integer> goodsIds, @Param("commonId") Integer commonId);


    /**
     * 更新基础
     *
     * @param record
     */
    void updateGdsBasesBatch2Obj(BaseAddParamDTO record);


    /**
     * 通过common查询详情
     *
     * @param commonId
     * @return
     */
    GoodsCommonDetailDTO selectCommonDetailByPrimaryKey(Integer commonId);


    /**
     * 更新主键
     *
     * @param goodsCommonDetail
     */
    void updateByPrimaryKey(GoodsCommonDetailDTO goodsCommonDetail);


    /**
     * 新增选择性
     *
     * @param goodsCommonDetail
     */
    void insertSelective2(GoodsCommonDetailDTO goodsCommonDetail);

    /**
     * 新增选择性
     *
     * @param goodsCommonDetail
     */
    void insertSelective3(GdbMerchantGoodsCommonDetailDTO goodsCommonDetail);

    /**
     * 通过店铺id删除
     *
     * @param commonId
     */
    void deleteByShopId(@Param("commonId") Integer commonId);
    /**
     * 通过图片id删除
     *
     * @param removeGoodsImageIds
     * @param commonId
     */
    void deleteByGoodsImageIds(@Param("removeGoodsImageIds") List<Integer> removeGoodsImageIds, @Param("commonId") Integer commonId);


    /**
     * 修改批次
     *
     * @param goodsImages
     */
    void updateBatch(@Param("goodsImages") List<GoodsImagesDTO> goodsImages);

    /**
     * 通过commonId删除
     *
     * @param commonId
     */
    void deleteByCommonId2(@Param("commonId") Integer commonId);


    /**
     * 新增商品图片
     *
     * @param addImages
     */
    void insertGoodsImage(@Param("goodsImages") List<GoodsImagesDTO> addImages);


    /**
     * 查询运输模板
     *
     * @param transportTemplate
     * @return
     */
    ManageTransportTemplateVO selectTransportTemplateById(@Param("transportTemplate") Integer transportTemplate);


    /**
     * 查询运输区域
     *
     * @param transportAreaId
     * @return
     */
    ManageTransportAreaVO selectTransportAreaByIdArea(@Param("transportAreaId") Integer transportAreaId);
    /**
     * 通过commonId查询
     *
     * @param commonId
     * @return
     */
    List<ManageGdsBaseInfoDTO> selectByCommonId3(@Param("commonId") Integer commonId);

    /**
     * 通过commonId查询
     *
     * @param commonId
     * @param shopId
     * @return
     */
    List<GoodsImagesDTO> selectByCommonId4(@Param("commonId") Integer commonId, @Param("shopId") Integer shopId);


    /**
     * 新增基础
     *
     * @param addGoodsBase
     */
    void insertGdsBasesObj(BaseAddParamDTO addGoodsBase);

    /**
     * 获取商品id
     *
     * @return
     */
    Integer getByGoodsId();


    /**
     * 新增商品图片
     *
     * @param record
     */
    void insertGoodsImages(GdbMerchantGuiGeGoodsImagesDTO record);

    /**
     * 查询优惠券信息
     *
     * @param param
     * @return
     */
    List<Integer> queryCouponList(@Param("param") UserVcBaseQueryDTO param);

    /**
     * 查询商品信息
     *
     * @param goodsCatLimitList
     * @return
     */
    List<WapCommonBaseUserFootVO> selectByCommonIds4(@Param("commonIds") List<Integer> goodsCatLimitList);

    /**
     * 查询模板详情
     *
     * @param param
     * @return
     */
    HouRedpacketTemplateVO selectByRedpacketId(@Param("param") UserAvailableRedpacketQueryDTO param);

    /**
     * 查询审核中和审核未通过的商品
     *
     * @return
     */
    Integer selectByGoodsCount();

    /**
     * 店铺审核数
     *
     * @return
     */
    Integer selectByShopCount();


    /**
     * 更新商品
     *
     * @param map
     * @return
     */
    Integer updateGoodsCommonByMap(@Param("map") Map<String, Object> map);

    /**
     * 查询commonId
     *
     * @param shopId
     * @return
     */
    List<Integer> queryCommonIdByShopId(@Param("shopId") Integer shopId);

    /**
     * 更新状态
     *
     * @param commonParam
     * @return
     */
    Integer updateCommonState(CommonShopStateDTO commonParam);

    /**
     * 查询所有积分商品
     *
     * @param page
     * @param param
     * @return
     */
    IPage<IntegralGoodsResultVO> queryIntegralAll(IPage<IntegralGoodsResultVO> page, @Param("param") ListPageQueryParamDTO param);


    /**
     * 锁定商品
     *
     * @param param
     */
    void updateGoodsLock(@Param("param") PlatFormGoodsStateDTO param);


    /**
     * 更新商品屏蔽
     *
     * @param param
     */
    void updateGoodsScreen(@Param("param") PlatFormGoodsStateDTO param);


    /**
     * 根据commonId找到所有goodsId
     *
     * @param commonIdList
     * @return
     */
    List<Integer> queryAllGoodsIdByCommonId(List<Integer> commonIdList);


    /**
     * 更新商品数量
     *
     * @param param
     */
    void updateGoodsState(@Param("param") PlatFormGoodsStateDTO param);

    /**
     * 处理店铺自定义分类
     * @param shopCatIds
     * @return
     */
    List<ManageGoodsSelfCatVO> selectByShopCatId(@Param("shopCatIds") List<Integer> shopCatIds);
    /**
     * 查询门店id
     *
     * @param shopGateId
     * @return
     */
    Integer queryShopIdByGateId(@Param("shopGateId") String shopGateId);

    /**
     * 根据shop id 查询该店铺所有字体商品信息
     *
     * @param page
     * @param param
     * @return
     */
    IPage<GateQueryCommonsVO> queryGateCommonsList(Page<GateQueryCommonsVO> page, @Param("param") GateQueryCommonsDTO param);

    /**
     * 根据门店id 和 commonid 查询门店规格商品 查询该店铺所有字体商品信息
     *
     * @param shopGateId
     * @param commonId
     * @return
     */
    List<GateGoodsBaseVO> queryGateGoodsBasePoList(@Param("shopGateId") String shopGateId, @Param("commonId") Integer commonId);

    /**
     * 查询商品规格集合
     *
     * @param commonId
     * @return
     */
    List<ManageGoodsBaseListVO> queryGoodsSpecList(@Param("commonId") Integer commonId);

    /**
     * 查询商品库存
     *
     * @param param
     * @return
     */
    Integer queryGateGoodsStock(@Param("param") GateUpShopGoodsStockDTO param);

    /**
     * 查询商品数量
     *
     * @param param
     * @return
     */
    Integer queryGateGoodsCount(@Param("param") GateGoodsBaseVO param);

    /**
     * 查询店铺库存
     *
     * @param param
     * @return
     */
    Integer queryGateShopStock(@Param("param") GateGoodsBaseVO param);

    /**
     * 更新基础商品
     *
     * @param param
     * @return
     */
    Integer updateGateGoodsBase(@Param("param") GateGoodsBaseVO param);

    /**
     * 更新商品库存
     *
     * @param integer
     * @param goodsId
     * @return
     */
    Integer updateGoodsStock(@Param("integer") Integer integer, @Param("goodsId") Integer goodsId);

    /**
     * 新增基础商品
     *
     * @param param
     * @return
     */
    Integer insertGateGoodsBase(@Param("param") GateGoodsBaseVO param);

    /**
     * 更新基础商品库存
     *
     * @param integer
     * @param goodsId
     * @return
     */
    Integer updateGoodsBaseStock(@Param("integer") Integer integer, @Param("goodsId") Integer goodsId);


    /**
     * 查询商品所属分类
     * @param commonId
     * @return
     */
    List<ShopGoodsCatVO> queryByCommonId(Integer commonId);

    /**
     * 校验商品是否已经销
     * @param record
     * @return
     */
    DealerPutawayRecordVO queryPutawayRecord(DealerPutawayRecordVO record);

    /**
     * 根据commonid查询出对应供应商的goodsid和shopid
     * @param commonId
     * @return
     */
    List<Integer> selectShopIdAndGoodsId(@Param("commonId")Integer commonId);


    /**
     * 运费规则
     * @param id
     * @return
     */
    ManageTransportAreaVO selectByPrimaryKey(Integer id);


    /**
     * 查询商品列表
     * @param page
     * @param param
     * @return
     */
    IPage<GdsListQueryResultVO> selectSupplyWebFilter(IPage<GdsListQueryResultVO> page , @Param("param") WebGoodsListQueryDTO param);

    /**
     * 查询微信直播商品
     * @param shopId
     * @return
     */
    List<Integer> queryLiveCommons2(@Param("shopId") String shopId);

    /**
     * 查询出售中的商品
     * @param page
     * @param param
     * @param integers
     * @return
     */
    IPage<ManageGdsNameQueryVO> selectByGdsName2(IPage<ManageGdsNameQueryVO> page,@Param("param") ManageGdsNameQueryDTO param,@Param("integers")List<Integer> integers);


    /**
     * 查看商品是否参加其他活动
     * @param commonId
     * @return
     */
    Integer selectCommonIsXian(@Param("commonId")Integer commonId);

    /**
     * 查看参加同类型活动商品是否冲突
     * @param goodsId
     * @param startTime
     * @param seckillId
     * @return
     */
    List<AppSeckillGoodsVO> selectSeckillGoodsId1(@Param("goodsId")Integer goodsId,@Param("startTime")String startTime,@Param("seckillId")Integer seckillId);

    /**
     * 查看参加同类型活动商品
     * @param goodsId
     * @param startTime
     * @return
     */
    List<AppSeckillGoodsVO> selectSeckillGoodsId(@Param("goodsId")Integer goodsId,@Param("startTime")String startTime);

    /**
     * 查询商品名
     * @param commonId
     * @return
     */
    String queryCommonName(@Param("commonId")Integer commonId);

    /**
     * 查询商品id
     * @param param
     * @return
     */
    Integer selectCommonName(@Param("param") CheckNameQueryParamDTO param);
    /**
     * /添加/删除plus商品
     * @param map
     * @return
     */
    Integer updatePlusGoods(@Param("param") Map map);


    /**
     * 查询平台最低会员折扣
     * @return
     */
    String selectmemberdiscount();


    /**
     * 获取全部套餐
     * @return
     */
    List<PlusThaliEntityVO> selectthaliList();


    /**
     * 查询会员权益
     * @param id
     * @return
     */
    List<PlusEquityEntityVO> selectplusequitys(@Param("list") List<Integer> id);


    /**
     * 查询最小库存和价格
     * @param commonId
     * @return
     */
    Map<String,Object> selectByGdsNamemin(@Param("param") Integer commonId);


    /**
     * 查询店铺类型
     * @param shopId
     * @return
     */
    Integer selectshoptype(@Param("shopId") Integer shopId);


    /**
     * 查询赠品
     *
     * @param param
     * @return
     */
    IPage<GoodsCommonEntity> queryGiveawayList(IPage<GoodsCommonEntity> page , @Param("param") ManjianzengzheListQueryDTO param);

    /**
     * 查询店铺商品销量相关信息
     * @param page
     * @param param
     * @return
     */
    IPage<GdsListQueryResultVO> selectByFilter(IPage<GdsListQueryResultVO> page, @Param("param") ListPageQueryParamDTO param);

    /**
     * 查询商品信息
     * @param page
     * @param param
     * @return
     */
    IPage<GdsListQueryResultVO> selectShopByFilter(IPage<GdsListQueryResultVO> page, @Param("param")ListPageQueryParamDTO param);

    /**
     * 查询商品活动信息
     * @param goodsId
     * @param startTime
     * @param discontId
     * @return
     */
    List<DiscountGoodsListVO> selectDiscountGoodsId1(@Param("goodsId")Integer goodsId,
                                                         @Param("startTime")String startTime, @Param("discontId")Integer discontId);

    /**
     * 查询当前时间活动商品
     * @param goodsId
     * @param startTime
     * @return
     */
    List<DiscountGoodsListVO> selectDiscountGoodsId(@Param("goodsId")Integer goodsId,@Param("startTime")String startTime);

    /**
     * 查询商品列表 暂不用
     * @param page
     * @param param
     * @return
     */
    IPage<GoodsCommonEntity> selectPromotionGoodsList2(IPage<GoodsCommonEntity> page , @Param("param") Map<String, Object> param);


    /**
     * 覆盖调价
     * @param param
     */
    void updateCommonPriceUni1(@Param("param") Map<String, Object> param);

    /**
     * 浮动调价
     * @param map
     */
    void updateCommonPriceSep(@Param("param") Map<String, Object> map);

    /**
     * 分别调价
     * @param map
     */
    void updateCommonPriceSep2(@Param("param") Map<String, Object> map);

    /**
     * 修改goodsCommon价格
     * @param param
     */
    void updateCommonPrice(@Param("param") SupperGoodsPriceDTO param);

    /**
     * 修改商品是否编辑过价格
      * @param supperGoodsPrice
     */
    void updateIsUpdatePrice(@Param("param")SupperGoodsPriceDTO supperGoodsPrice);

    /**
     * 根据common 查询供应商商品的商品规格价格
     * @param commonId
     * @return
     */
    List<Map<String, Object>> querySupperGoodsPrice(@Param("commonId")String commonId);

    /**
     * 查询商家同步供应商的商品 但未修改过价格的
     * @param page
     * @param param
     * @return
     */
    IPage<ManageGdsNameQueryVO> queryIsPriceUpdate(IPage<ManageGdsNameQueryVO> page , @Param("param") ManageGdsNameQueryDTO param);

    /**
     * 待发布的商品
     * @param page
     * @param param
     * @return
     */
    IPage<GoodsCommonEntity> selectWaitRelease(IPage<GoodsCommonEntity> page , @Param("param") ManageGdsNameQueryDTO param);

    /**
     * 商品上架
     * @param commonIds
     */
    void updateOnline(@Param("commonIds") List<Integer> commonIds);

    /**
     * 查询经销商品信息
     * @param page
     * @param map
     * @return
     */
    IPage<ManageGdsNameQueryVO> selectByGdsNameByPlus(IPage<ManageGdsNameQueryVO> page,@Param("param") Map<String, Object> map);

    /**
     * 查询可以添加兑换券活动的商品
     * @param page
     * @param map
     * @return
     */
    IPage<ManageGdsNameQueryVO> selecProductVoucherGoodsByPage(IPage<ManageGdsNameQueryVO> page,@Param("param") Map<String, Object> map);

    /**
     * 查询店铺规格下商品信息
     * @param page
     * @param param
     * @return
     */
    IPage<GoodsCommonEntity> selectGoodsBySpecId(IPage<GoodsCommonEntity> page , @Param("param") Map<String, Object> param);

    /**
     * 查询 兑换券商品信息
     * @param productVoucherGoodsVOS
     * @return
     */
    List<ProductVoucherGoodsVO> selecProductVoucherGoods(@Param("param") List<ProductVoucherGoodsVO> productVoucherGoodsVOS);


    /**
     * 删除商品供应记录
     * @param commonIds
     * @return
     */
    int deleteRecordStateBatchAll(@Param("commonIds") List<Integer> commonIds);

    /**
     * 查询商品信息
     * @param page
     * @param param
     * @return
     */
    IPage<GoodsInfoDTO> queryGoodsInfo2(IPage<GoodsInfoDTO> page , @Param("param") Map<String, Object> param);

    /**
     * 全部宝贝下面分类
     * @param page
     * @param param
     * @return
     */
    IPage<ListQueryResultVO> selectByShopFilter(IPage<ListQueryResultVO> page , @Param("param") ShopIndexQueryParamDTO param);

    /**
     * 根据shopId查询所有商品
     * @param page
     * @param param
     * @return
     */
    IPage<ListQueryResultVO> selectByShopIndex2(IPage<ListQueryResultVO> page , @Param("param") ShopIndexQueryParamDTO param);

    /**
     * 查询首页信息
     * @param shopId
     * @return
     */
    ShopIndexVO selectByParam(@Param("shopId") Integer shopId);

    /**
     * 查询店铺名
     * @param shopId
     * @return
     */
    String selectShopName(@Param("shopId") Integer shopId);

    /**
     * 新的 Excel 导入数据
     * @param common
     * @return
     */
    Integer importDataGoodsCommon(GdbMerchantGoodsCommonVO common);


    /**
     * 更新限时秒杀商品秒杀库存数量
     * @param paramMap
     */
    void updateSaleForSeckill2(@Param("param") Map<String, Object> paramMap);

    /**
     * 查询代发商品 所同步的供应商商品是否在售
     * @param commonId
     * @return
     */
    Integer querySupplierGoodsStart(@Param("commonId") Integer commonId);

    /**
     * 批量上架同步供应商商品
     * @param commonIds
     */
    void updateSupperGoodsStart(@Param("commonIds")List<Integer> commonIds);

    /**
     * 判断当前用户是否是微客
     * @param userId
     * @return
     */
    Integer queryUserVkStart(@Param("userId")String userId);

    /**
     * 判断当前商品是否是 分销商品
     * @param commonId
     * @return
     */
    Integer queryvkGoodsStart(@Param("commonId")Integer commonId);

    /**
     * 当前商品是供应商商品 所有商家商品下架
     * @param dealerCommonIds
     */
    void updateOffline(@Param("commonIds")List<Integer> dealerCommonIds);

    /**
     * 商品经销记录
     * @param dealerGoods
     * @return
     */
    List<DealerPutawayRecordVO> queryBuyRecordList(@Param("param")DealerGoodsVO dealerGoods);

    /**
     * 更新商品状态
     * @param updateGoods
     */
    void updateDealerGoods(GdbMerchantGoodsCommonVO updateGoods);

    /**
     * 查询当前商品 goodsCommon 规格信息
     * @param dealerCommonId
     * @return
     */
    Map<String, String> queryCommonSpecByCommonId(@Param("commonId")Integer dealerCommonId);

    /**
     * 查询当前商品商品的 售卖区域规则
     * @param transportAreaId
     * @return
     */
    GdbMerchantTransportAreaVO selectTransportArea(@Param("transportAreaId") Integer transportAreaId);

    /**
     * 新增售卖区域
     * @param transportArea
     */
    void insertTransportArea(GdbMerchantTransportAreaVO transportArea);

    /**
     * 设置运费和售卖区域
     * @param transportTemplate
     * @return
     */
    GdbMerchantTransportTemplateVO selectTransportTemplate(Integer transportTemplate);

    /**
     * 新增运费模板
     * @param transportTemplate
     */
    void insertTransportTemplate(GdbMerchantTransportTemplateVO transportTemplate);

    /**
     * 运费规则
     * @param id
     * @return
     */
    List<GdbMerchantTransportRuleVO> selectTransportRule(@Param("transportTemplateId")Integer id);

    /**
     * 设置运费规则
     * @param transportRules
     */
    void insertTransportRule(@Param("transportRules")List<GdbMerchantTransportRuleVO> transportRules);

    /**
     * @param shopId 店铺主键
     * @return 处理结果
     */
    Map<String, BigDecimal> selectShopEvaluation(Integer shopId);

    /**
     * 增加库存
     * @param supperGoods
     */
    void updateAddStock(@Param("param") WapGoodsSaleSumUpdateVO supperGoods);

    /**
     * 查询等待发布的商品
     *
     * @param page
     * @param param
     * @return
     */
    IPage<ManageGdsNameVO> queryToBeReleased(IPage<ManageGdsNameVO> page, @Param("param") ManageGdsNameQueryParamDTO param);

    /**
     * 查找门店商品库存
     * @param goodsId
     * @param shopId
     * @return
     */
    Integer queryGoodsGateStock(@Param("goodsId") Integer goodsId, @Param("shopId") String shopId);

    /**
     * 插入用户访问记录
     */
    void addUserAccessData(@Param("param") UserAccess userAccess);

    /**
     * 查询平台礼包商品
     *
     * @param param
     * @return
     */
    IPage<GiftGoodsVO> selectPlatformGiftGoods(Page<GiftGoodsVO> page,@Param("param") GiftGoodsDTO param);

    void updatePlatformGiftGoods(@Param("param") GiftGoodsDTO param);

    /**
     * 判断spu商品状态是否上架
     * @param commonId
     * @return
     */
    Integer querySpuStartNum(@Param("commonId")Integer commonId);

    /**
     * 根据商品名称模糊查询商品id
     * @param commonName
     * @return
     */
    List<Integer> queryLikeGoodsName(@Param("commonName") String commonName);
    /**
     * 查询用户足迹信息
     * 该方法用于查询用户在移动端的足迹信息，通过用户的足迹数据进行商品推荐或数据展示
     *
     * @param page 分页对象，包含了当前页码、每页记录数等分页信息
     * @param param 用户足迹信息的封装对象，包含了查询所需的用户ID和其他查询条件
     * @return 返回一个分页对象，其中包含查询到的用户足迹信息
     *
     * 该方法是一个接口方法，通常由具体的Mapper实现类来实现用于处理用户足迹信息的查询请求，
     * 并返回分页后的足迹数据
     */
    IPage<ImUserFootVO> imQueryFootPrint(IPage page, @Param("param") WapUserFootPrintDTO param);

    /**
     * 更新称重商品库存
     * @param param
     */
    void updateWeighingGoodsCommonStock(@Param("param") ManageGoodsBaseListResultDTO param);

    /**
     * @return 处理结果
     */
    List<Map<String,Object>> selectYiYaTongGoodsId(@Param("commonId") Integer commonId,@Param("shopId") Integer shopId);

    /**
     * @return 处理结果
     */
    List<Map<String,Object>> selectJingDongGoodsId(@Param("commonId") Integer commonId,@Param("shopId") Integer shopId);


    /**
     * @return 处理结果
     */
    List<Map<String,Object>> selectYiYaTongGoodsIdByRatio(@Param("commonId") Integer commonId,@Param("ratio") BigDecimal ratio,@Param("shopId") Integer shopId);

    /**
     * @return 处理结果
     */
    List<Map<String,Object>> selectJingDongGoodsIdByRatio(@Param("commonId") Integer commonId,@Param("ratio") BigDecimal ratio,@Param("shopId") Integer shopId);

    /**
     * @return 处理结果
     */
    String selectDistrictByName(@Param("name") String name);

    /**
     * @return 处理结果
     */
    String selectDistrictByName1(@Param("name") String name,@Param("parentId") String parentId);

    /**
     * @return 处理结果
     */
    Integer setDistrictCodeByDistrictId(@Param("districtId") String districtId,@Param("districtCode") String districtCode);

    /**
     * @return 处理结果
     */
    List<SupplierGoodsSpecVO> queryGoodsSpecListBySupplier(@Param("commonId") Integer commonId);

    /**
     * @return 处理结果
     */
    String selectReturnTime(@Param("orderSn") String orderSn);

    /**
     * @return 处理结果
     */
    String selectShopNameByShopId(@Param("shopId") Integer shopId);

    /**
     * 查询待上架的经销商品
     *
     * @param page
     * @param param
     * @return
     */
    IPage<ManageGdsNameVO> queryDealerProducts(IPage<ManageGdsNameVO> page, @Param("param") ManageGdsNameQueryParamDTO param);

    /**
     * 查询商品的供应商品状态
     *
     * @param commonId
     * @return
     */
    Integer querySupplierGoodsNum(@Param("commonId") Integer commonId);

    /**
     * 获取商品全部库存
     *
     * @param commonId
     * @return
     */
    Integer selectAllGooodsStock(@Param("commonId") Integer commonId);

    /**
     * 查询待上架的经销商品
     *
     * @param page
     * @param param
     * @return
     */
    IPage<ManageGdsNameVO> queryDealerProductsYiyatong(IPage<ManageGdsNameVO> page, @Param("param") ManageGdsNameQueryParamDTO param);


    /**
     * @return 处理结果
     */
    List<SupplierGoodsSpecVO> queryGoodsSpecListByYiYaTong(@Param("commonId") Integer commonId);

    /**
     * 查询待上架的京东经销商品规格
     * @param commonId
     * @return
     */
    List<SupplierGoodsSpecVO> queryGoodsSpecListByJingDong(@Param("commonId") Integer commonId);

    /**
     * 根据commonid 查询供应商商品的商品规格价格
     *
     * @param commonId
     * @return
     */
    List<Map<String, Object>> queryYiYaTongGoodsPrice(@Param("commonId") String commonId);

    /**
     * 查询待上架的京东经销商品
     * @param page
     * @param param
     * @return
     */
    IPage<ManageGdsNameVO> queryDealerProductsJingDong(IPage<ManageGdsNameVO> page, @Param("param") ManageGdsNameQueryParamDTO param);
    /**
     * 分页查询所有商品
     *
     * @param page
     * @param platFormAllGoodsQuery
     * @return
     */
    IPage<PlatFormAllGoodsVO> selectByAllSupplierGoods(IPage<PlatFormAllGoodsVO> page, @Param("param") PlatFormAllGoodsDTO platFormAllGoodsQuery);

    /**
     * 查询库存
     *
     * @param commonId
     * @return
     */
    List<GoodsStockVO> selectSupplierStocks(@Param("commonId") Integer commonId);

    /**
     * @return 处理结果
     */
    List<Integer> selectSupplierTransportAreaId(@Param("goodsIds") List<Integer> goodsIds);

    /**
     * 删除 经销同步记录
     *
     * @param commonIds
     */
    void deleteDealerPutawayRecord(@Param("commonIds") List<Integer> commonIds);

    /**
     * 更新供应商商品状态
     * 该方法用于根据提供的参数更新平台商品的状态
     * 主要用于当商品状态发生变化时，同步更新数据库中的商品状态信息
     *
     * @param param 包含更新信息的数据传输对象，包括需要更新的商品ID和新的状态信息
     */
    void updateSupplierGoodsState(@Param("param") PlatFormGoodsStateDTO param);

    /**
     * 查询供应商商品基础信息
     * 通过供应商商品ID查询商品的基础信息
     * 这包括但不限于商品名称、描述、类别等基本信息
     * 该方法主要用于获取供应商商品的公共基础信息，以便在系统中共享和使用
     *
     * @param supplierCommonId 供应商商品ID，用于标识特定的商品记录
     * @return 返回包含商品基础信息的实体对象如果找不到对应的商品信息，则返回null
     */
    GoodsCommonEntity querySupplierCommonBaseInfo(@Param("supplierCommonId") Integer supplierCommonId);


    /**
     * 查询商品信息
     * @param commonId
     * @return
     */
    SelfSupportIndexGoodsVO selectByCommonIdFeign(Integer commonId);


    /**
     * 修改商品活动状态
     * @param param
     */
    void updateCommonIsXian3(@Param("param") CommonIsXianUpdateDTO param);

    /**
     * 查同步供应商商品的经销商商品id
     *
     * @param supplierCommonId
     * @return
     */
    List<SupplierDealerPutawayRecordVO> queryDealerCommonIdList(@Param("supplierCommonId") Integer supplierCommonId);

    /**
     * 更新经销商品状态
     *
     * @param dealerGoodsId
     */
    void updateDealerGoodsState(@Param("dealerGoodsId") Integer dealerGoodsId);

    /**
     * 更新经销商品商品信息
     *
     * @param dealerCommonIdList
     * @param merchantGoodsCommon
     */
    void updateDealerGoodsCommon(@Param("dealerCommonIdList") Set<Integer> dealerCommonIdList, @Param("param") GoodsCommonEntity merchantGoodsCommon);

    /**
     * 更新经销商商品详情
     *
     * @param dealerCommonIdList
     * @param commonBody
     */
    void updateDealerCommonBody(@Param("dealerCommonIdList") Set<Integer> dealerCommonIdList, @Param("commonBody") String commonBody);

    /**
     * 更新商品价格信息
     * 该方法通过接收一个SupperGoodsPriceDTO对象作为参数，来更新商品的价格信息
     * 主要用于在商品价格发生变化时，对数据库中的商品价格信息进行更新
     *
     * @param supperGoodsPrice 包含更新后的商品价格信息的对象
     */
    void updateGoodsPrice(@Param("param")SupperGoodsPriceDTO supperGoodsPrice);


    /**
     * 查询商品信息
     * @param commonId
     * @return
     */
    Map<String, Object> selectManjianzengzheMessage(@Param("commonId") Integer commonId);


    /**
     * 查询商品信息
     * @param commonId
     * @return
     */
    GdsDetailsCommonQueryResultVO selectByCommonIdFeign2(@Param("commonId") Integer commonId);


    /**
     * 查询商品首页数量
     *
     * @param recommendParam
     * @return
     */
    Integer selectWebShopIndexCount(@Param("param") WebIndexRecommendQueryParamVO recommendParam);

    /**
     * 查询是否是自己商品
     *
     * @param param
     * @return
     */
    Integer checkShopSelf(@Param("param") Map<String, Object> param);

    /**
     * 更新店铺营业状态
     * 此方法通过接收一个包含店铺营业状态信息的DTO对象来更新店铺的营业状态
     * 它主要用于应对店铺开闭店的需求，通过修改数据库中的状态信息来实现
     *
     * @param param 包含店铺营业状态信息的DTO对象，用于更新店铺状态
     */
    void updateShopState(@Param("param") CommonShopStateDTO param);


    /**
     * 更新店铺状态
     * 此方法通过店铺ID来更新店铺的状态
     * 它是一个只修改状态的操作，不返回任何值
     *
     * @param shopId 店铺的唯一标识符，用于指定需要更新状态的店铺
     */
    void updateShopState1(@Param("shopId") Integer shopId);


    /**
     * 查询店铺 三件在售商品
     *
     * @param record
     * @return
     */
    List<Map<String, Object>> selectUniappQueryShopGoods(@Param("param") ShopListQueryResultVO record);


    /**
     * 查询店铺商品信息
     * @param shopId
     * @return
     */
    List<GdsListQueryResultVO> selectByShopIdFeign(@Param("shopId") Integer shopId);


    /**
     * 更新商品信息
     * @param shopId
     */
    void updateGoodsCommon(@Param("shopId") String shopId);


    /**
     * 修改分销商品状态
     * @param distributorFlag 商品分销状态 0-不参与分销 1-参与分销
     * @param commonId 商品id
     */
    void updateGoodsFxStatus(@Param("distributorFlag") Integer distributorFlag,@Param("commonId") Integer commonId);


    /**
     * 查询店铺商品数量
     * @param shopId
     * @return
     */
    Integer selectShopGoodsCount(@Param("shopId") Integer shopId);


    /**
     *  查询店铺在售商品数量
     * @param shopId
     * @return
     */
    Integer selectShopSellGoodsCount(@Param("shopId") Integer shopId);

    /**
     * 查询商品使用的 售卖区域模板
     * @param commonId
     * @return
     */
    Integer selectTransportAreaIdFeign(@Param("commonId")Integer commonId);
    /**
     * 查询商品使用的 售国际卖区域模板
     * @param commonId
     * @return
     */
    Integer selectInternationalTransportAreaIdFeign(@Param("commonId")Integer commonId);



    /**
     * 查询商品信息
     * @param goodsIdList
     * @return
     */
    List<WapShippingQueryResultVO> selectShipping2(@Param("goodsList")List<Integer> goodsIdList);


    /**
     * 查询运费
     *
     * @param goodsId
     * @return
     */
    ShippingInfoDTO selectShippingInfo(@Param("goodsId") Integer goodsId);


    /**
     * 查询商品goodId
     * @param commonId
     * @return
     */
    Integer queryFxGuigeGoodsId(@Param("commonId")Integer commonId);


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
    String selectCommonNameFeign(Integer commonId);

    /**
     * 查询商品详情
     * @param commonId
     * @return
     */
    BaseWxLiveGoodsVO queryCommonDetail(@Param("commonId") Integer commonId);

    /**
     * 根据商品名称查询分类
     * @param commonName
     * @return
     */
    Integer selectCatIdByCommonName(@Param("commonName")String commonName);

    /**
     * 查询自营店铺商品
     *
     * @param param
     * @return
     */
    IPage<GiftGoodsVO> selectSelfSupportShopGoods(Page<GiftGoodsVO> page,@Param("param") GiftGoodsDTO param);

    /**
     * 查询智能商品推荐商品信息
     * @param commonId
     * @return
     */
    WapCommonBaseUserFootVO queryintelligentRecommendationGoodsById(@Param("commonId")Integer commonId);

    /**
     * 查询智能商品推荐商品信息
     * @param page
     * @return
     */
    Page<WapCommonBaseUserFootVO> queryIntelligentRecommendationAllGoods(Page<WapCommonBaseUserFootVO> page,@Param("resultList")List<WapCommonBaseUserFootVO> resultList);

    /**
     * 根据 历史下单商品、游览、收藏、加购、商品所属分类查询商品
     * @param userId
     * @return
     */
    List<IntelligentRecommendationGoodsCatVO> queryIntelligentRecommendationGoodsCat(@Param("userId")String userId);

    /**
     * 根据商品类目查询指定数量的商品
     * @param recommendationGoodsCatVO
     * @return
     */
    List<WapCommonBaseUserFootVO> queryCatQueryGoodsNum(@Param("param")IntelligentRecommendationGoodsCatVO recommendationGoodsCatVO);

    /**
     * 修改 goodsCommon Stock
     * @param commonId
     * @param goodsStock
     */
    void updateGoodsCommonStock(@Param("commonId")Integer commonId, @Param("goodsStock")Integer goodsStock);

    /**
     * 更新商品规格
     * @param commonSpecValuesStr
     * @param specParamStr
     * @param commonId
     */
    void updateGoodsSpec(@Param("commonSpecValuesStr") String commonSpecValuesStr, @Param("specParamStr")String specParamStr, @Param("commonId")Integer commonId);

    /**
     * 更新商品
     *
     * @param commonId
     * @param orderGoodsReturnNum
     * @return
     */
    Integer updateGoodsSalenum(@Param("commonId") Integer commonId, @Param("orderGoodsReturnNum") Integer orderGoodsReturnNum);

    /**
     * 判断当前商品是否是京东VOP商品
     * @param commonId
     * @return
     */
    Integer queryGoodsJingDongIsVop(@Param("commonId") Integer commonId);
    /**
     * 查询商品代发数量
     * @param shopId
     * @param supplierSource
     * @return
     */
    Integer queryDropShippingNum(@Param("shopId") Integer shopId, @Param("supplierSource") Integer supplierSource);
}

