package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.dto.goods.*;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.dto.shop.ArticleDTO;
import com.houjiang.commons.dto.shop.GateStoreIndexGoodsDTO;
import com.houjiang.commons.vo.activity.ActivityCombinationGoodsVO;
import com.houjiang.commons.vo.activity.ManjianzengzhePromotionContentQueryResultVO;
import com.houjiang.commons.vo.base.BaseGoodsConfigVO;
import com.houjiang.commons.vo.goods.*;
import com.houjiang.commons.vo.order.OrderGoodsBarterVO;
import com.houjiang.commons.vo.order.WapShippingQueryResultVO;
import com.houjiang.commons.vo.shop.GdsShopDetailResultVO;
import com.houjiang.commons.vo.shop.InvoiceSwitchEntityVO;
import com.houjiang.goods.dto.*;
import com.houjiang.goods.entity.GoodsBaseEntity;
import com.houjiang.goods.entity.GoodsCommonEntity;
import com.houjiang.goods.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 商品表(GoodsBase)
 *
 * @since 2023-05-19 13:52:02
 * @author Amos
 */
@Mapper
public interface GoodsBaseMapper extends BaseMapper<GoodsBaseEntity> {

    /**
     * es 查询在售商品
     * @return
     */
    List<ArticleDTO> selectAll(@Param("page") Integer page, @Param("pageSize")Integer pageSize);
    /**
     * es 查询在售商品总数
     * @return
     */
    Integer selectAllCount();
    /**
     * es 查询在售商品
     * @return
     */
    ArticleDTO selectEsGoodsbyId(@Param("commonId") String commonId);

    /**
     * 根据CommonId 查询商品详情
     * @param commonId
     * @return
     */
    GoodsListQueryEsDTO selectByCommonIdEs(Integer commonId);

    /**
     * 根据goodsId 查询商品详情
     * @param goodsId
     * @return
     */
    GoodsListQueryEsDTO selectByGoodsIdEs(Integer goodsId);

    /**
     * 查询当前商品是否经销
     * @param shopId
     * @param supplierGoodsId
     * @return
     */
    Integer queryGoodsDealerInfo(@Param("shopId") String shopId, @Param("supplierGoodsId") Integer supplierGoodsId);

    /**
     * 查询经销商品配置信息
     * @param commonId
     * @return
     */
    BaseGoodsConfigVO queryDelayGoodsConfig(Integer commonId);

    /**
     * 更新商品点击量
     * @param goodsId
     */
    void updateGoodsClick(@Param("goodsId")String goodsId);

    /**
     * 更新商品收藏量
     * @param paraMap
     */
    void updateGoodsCollect(@Param("param")Map<String, String> paraMap);

    /**
     * 更新商品库存
     * @param wapGoodsSaleSumUpdate
     */
    void updateStock(@Param("param")WapGoodsSaleSumUpdateVO wapGoodsSaleSumUpdate);

    /**
     * 查询当前商品是否 参与秒杀活动
     * @param goodsId
     * @return
     */
    Integer querySeckillInfo(@Param("goodsId")Integer goodsId);

    /**
     * 查询商品信息
     * @param commonId
     * @return
     */
    List<ManageGdsBaseAddResultVO> selectByCommonId(@Param("commonId")Integer commonId);

    /**
     * 查询所有规格商品信息
     * @param commonId
     * @return
     */
    List<ManageGdsBaseAddResultVO> selectByCommonId2(@Param("commonId")Integer commonId);

    /**
     * @return 处理结果
     */
    List<ManageGdsBaseAddResultVO> selectSpecValueByGoodsId(@Param("goodsId")Integer goodsId);

    /**
     * 查询商品规格信息
     * @param goodsId
     * @return
     */
    GdsBaseDetailResultVO selectByGoodsId(@Param("goodsId")Integer goodsId);


    /**
     * 订单购物车中获取商品信息
     *
     * @param goodsId
     * @return
     */
    ReceiveGoodsDetailsDTO selectCartMessage(@Param("goodsId") Integer goodsId);

    /**
     * 删除goods_base商品
     * @param commonIds
     */
    void updateCommonDel(@Param("commonIds") List<Integer> commonIds);

    /**
     * 删除goods_base商品
     * @param commonIdList
     * @return
     */
    int cancelDealerGoodsBase(@Param("commonIdList") List<Integer> commonIdList);

    /**
     * 查询出售中的商品规格列表
     * @param commonId
     * @return
     */
    List<ManageGoodsBaseListVO> selectByGoodsList(@Param("commonId") Integer commonId);

    /**
     * 查询出售中的商品规格列表 活动版
     * @param commonId
     * @return
     */
    List<ManageGoodsBaseListVO> selectByGoodsList3(@Param("commonId") Integer commonId);

    /**
     * 查询编辑秒杀商品的规格列表
     *
     * @param param
     * @return
     */
    List<ManageGoodsBaseListVO> querySeckillGoodsList(ManageCommonIdsDTO param);


    /**
     * 查询当前商品库存
     * @param goodId
     * @return
     */
    Integer queryGoodsStock(@Param("goodsId")Integer goodId);


    /**
     * 修改common库存
     * @param commonId
     * @param gateShopStock
     * @return
     */
    Integer updateCommonStock(@Param("commonId")Integer commonId,@Param("gateShopStock")Integer gateShopStock);


    /**
     * 修改common库存
     * @param commonId
     * @param gateShopStock
     * @return
     */
    Integer updateCommonStock2(@Param("commonId")Integer commonId,@Param("gateShopStock")Integer gateShopStock);


    /**
     * 修改base库存
     * @param param
     * @return
     */
    Integer updateGoodsBaseByCommonIdAndGoodsId(@Param("param") ManageGoodsBaseListResultDTO param);

    /**
     * 查询店铺信息
     * @param shopId
     * @return
     */
    GdbMerchantShopBaseVO selectByPrimaryKey(Integer shopId);

    /**
     * insertGdsBases 接口升级后续需要使用
     * @param gdsBase
     */
    void insertGdsBasesObj(ManageGdsBaseAddParamvDTO gdsBase);

    /**
     * 查询最后插入的goodsid值
     * @return
     */
    int getByGoodsId();

    /**
     * 更新商品状态
     * @param goodsIds
     * @param commonId
     */
    void updateGoodsIsShelves2(@Param("goodsIds") List<Integer> goodsIds, @Param("commonId") Integer commonId);


    /**
     * 修改商品
     * @param gdsBases
     */
    void updateGdsBasesBatch2Obj(ManageGdsBaseAddParamvDTO gdsBases);

    /**
     * 查询商品
     * @param commonId
     * @return
     */
    List<ManageGdsBaseAddResultVO> selectGoodsSpecBygoodsIsShelves(@Param("commonId") Integer commonId,@Param("isVersion") Integer isVersion);

    /**
     * 判断是否是自己的商品
     * @param shopId
     * @param commonId
     * @return
     */
    Integer isMeGoods(@Param("shopId")String shopId,@Param("commonId")Integer commonId);

    /**
     * 分页查询所有商品信息
     * @param param
     * @return
     */
    IPage<PlatFormAllGoodsVO> selectByAllGoods(IPage<PlatFormAllGoodsVO> page , @Param("param") PlatFormAllGoodsDTO param);


    /**
     * 查询当前商品不同规格下的库存
     * @param commonId
     * @return
     */
    List<GoodsStockVO> selectStocks(@Param("commonId") Integer commonId);

    /**
     * 查询商品信息
     * @param commonId
     * @return
     */
    List<GoodsBaseVO> queryGoodsCommonId(@Param("commonId")Integer commonId,@Param("isVersion") Integer isVersion);

    /**
     * 添加商品详情
     * @param goodsBaseList
     */
    void insertGoodsBase(@Param("item") GoodsBaseVO goodsBase);

    /**
     * 根据分类和类型查询商品信息
     * @param catIdList
     * @param goodsType
     * @return
     */
    List<GoodsIdsQueryVO> selectByCatIds(@Param("catIdList") List<Integer> catIdList, @Param("goodsType") Integer goodsType);

    /**
     * 查询商品规格等详情
     * @param param
     * @return
     */
    List<GoodsDetailsDTO> selectCommodityDetails(@Param("param") GoodsDetailsDTO param);


    /**
     * 分页查询商品规格等详情
     * @param param
     * @return
     */
    IPage<GoodsDetailsDTO> selectCommodityDetailsByPage(IPage<GoodsDetailsDTO> page , @Param("param") GoodsDetailsDTO param);


    /*
/**
     *查询红包下的商品列表
     */
    IPage<GoodsBaseEntity> selectGoodsDetailByRct(IPage<GoodsBaseEntity> page , @Param("param") Map<String, Object> param);

    /**
     * 查询商品信息
     * @param goodsCatLimitList
     * @return
     */
    List<WapHallGoodsQueryVO> selectByGoodsIds(@Param("goodsIds")List<Integer> goodsCatLimitList);

    /**
     * 更新基础商品
     *
     * @param map
     * @return
     */
    Integer updateGoodsBaseByMap(@Param("param") Map<String, Object> map);

    /**
     * 更新基础商品状态
     *
     * @param commonParam
     * @return
     */
    Integer updateGoodsBaseState(CommonShopStateDTO commonParam);

    /**
     * 分页查询积分商品
     *
     * @param page
     * @param param
     * @return
     */
    IPage<IntegraMallGoodsVO> queryIntegralGoodsAll(IPage<IntegraMallGoodsVO> page, @Param("param") Map<String, Object> param);


    /**
     * 更新基础商品状态集合
     *
     * @param param
     */
    void updateGoodsBaseStateList(@Param("param") PlatFormGoodsStateDTO param);


    /**
     * 根据commonId查询商品信息
     * @param commonId
     * @return
     */
    List<ReceiveGoodsDetailsVO> selectCartMessageByCommonId(@Param("commonId") Integer commonId);

    /**
     * 根据commonId查goodid
     * @param commonId
     * @return
     */
    List<Integer> selectGoodsId(@Param("commonId")Integer commonId);

    /**
     * 查询出售中的商品规格列表
     *
     * @param commonIds
     * @return
     */
    List<ManageGoodsBaseListResultDTO> selectByGoodsList2(@Param("commonIds") List<Integer> commonIds);


    /**
     * 覆盖调价
     * @param param
     */
    void updateGoodsPriceUni1(@Param("param") Map<String, Object> param);

    /**
     * 修改商品价格
     * @param param
     */
    void updateGoodsPriceSep(@Param("param") Map<String, Object> param);

    /**
     * 分别调价
     * @param map
     */
    void updateGoodsPriceSep2(@Param("param") Map<String, Object> map);

    /**
     * 修改goodsBase 价格
     * @param supperGoodsPrice
     */
    void updateGoodsPrice(@Param("param")SupperGoodsPriceDTO supperGoodsPrice);

    /**
     * 修改商品上下架状态
     * @param commonIds
     */
    void updateOnline(@Param("commonIds") List<Integer> commonIds);

    /**
     * 新增报表
     * @param map
     * @return
     */
    int insertReport(Map<String, Object> map);


    /**
     * 查询店铺信息
     * @param shopId
     * @return
     */
    GdsShopDetailResultVO selectShopDetailByShopId(Integer shopId);

    /**
     * 查询发票状态
     * @param param
     * @return
     */
    InvoiceSwitchEntityVO queryInvoiceSwitch(@Param("param") Map<Object, Object> param);


    /**
     * 查询满减赠折活动展示活动列表
     * @param commonId
     * @return
     */
    List<ManjianzengzhePromotionContentQueryResultVO> selectManjianzengzhePromotionContent(Integer commonId);


    /**
     * @param param
     * @return
     */
    List<WapCommonBaseUserFootVO> selectAppGuessLikeH5(@Param("param") GuessYouLikeQueryDTO param);

    /**
     * 查询店铺自定义商品分类
     *
     * @param userId
     * @return
     */
    List<Integer> selectCartGuessLikeGoods2(@Param("userId") String userId);



    /**
     * 查询商品海报
     *
     * @param
     * @return
     */
    GoodsPosterVO getGoodsPoster(@Param("goodsId") Integer goodsId);


    /**
     * 新增商品信息
     * @param record
     * @return
     */
    int insertSelective(GdbMerchantGoodsBaseVO record);

    /**
     * 分页查询活动专区商品
     *
     * @param param
     * @return
     */
    IPage<GoodsRecResultDetailVO> selectNoneGoods(IPage<GoodsRecResultDetailVO> page , @Param("param") NoneGoodsQueryDTO param);

    /**
     * 更新商品库存
     * @param gdsBaseAddParam
     */
    void updateDealerGoodsBase(@Param("gdsBases")ManageGdsBaseAddParamvDTO gdsBaseAddParam);

    /**
     * 更新商品库存
     * @param gdsBaseAddParam
     */
    void updateDealerGoodsStock(@Param("gdsBases")ManageGdsBaseAddParamvDTO gdsBaseAddParam);

    /**
     * 查询经销商品规格信息
     * @param goodsId
     * @return
     */
    GoodsBaseVO queryByGoodsId(@Param("goodsId")Integer goodsId);

    /**
     * 新增商品信息
     * @param goodsBase
     * @return
     */
    void insertDealerGdsBases(GoodsBaseVO goodsBase);

    /**
     * 增加库存
     * @param supperGoods
     */
    void updateAddStock(@Param("param") WapGoodsSaleSumUpdateVO supperGoods);

    /**
     * @return 处理结果
     */
    WapShippingQueryResultVO queryProductVoucherGoods(@Param("goodsId") Integer goodsId);

    /**
     * 查询可以继续库存流水更改商品列表
     *
     * @param page  分页对象
     * @param param 筛选条件
     * @return 商品列表
     */
    IPage<StorageInventoryFlowGoodsVO> queryStorageInventoryFlowGoods(Page<StorageInventoryFlowGoodsVO> page,
                                                                      @Param("param") StorageInventoryFlowGoodsDTO param);

    /**
     * 根据商品id查询商品信息
     *
     * @param goodsId 商品id
     * @return 商品信息
     */
    StorageInventoryFlowGoodsVO selectGoodsInfo(@Param("goodsId") Integer goodsId);

    /**
     * 根据商品名称查询商品id列表
     *
     * @param goodsName 商品名称
     * @param shopId    店铺id
     * @return 商品id列表
     */
    List<Integer> selectGoodsIdListByGoodsName(String goodsName, Integer shopId);

    /**
     * 查询spuid
     * @param commonIds
     * @return
     */
    List<Integer> queryGoodsByCommonIds(@Param("commonIds") List<Integer> commonIds);

    /**
     * 更新称重商品库存
     * @param param
     */
    void updateWeighingGoodsBaseStock(@Param("param") ManageGoodsBaseListResultDTO param);

    /**
     * @return 处理结果
     */
    List<GoodsBaseVO> selectByOffOrOnline(@Param("goodsIsShelves") Integer goodsIsShelves, @Param("commonId") Integer commonId);

    void updateSkuIsShelves(@Param("goodsIsShelves") Integer goodsIsShelves, @Param("goodsId") Integer goodsId);

    /**
     * @param commonId common主键
     * @return 处理结果
     */
    Integer selectCommonIsShelves(Integer commonId);

    /**
     * @return 处理结果
     */
    Integer selectGoodsIsShelves(@Param("goodsIsShelves") Integer goodsIsShelves, @Param("commonId") Integer commonId);

    /**
     * 修改商品状态
     *
     * @param commonIds
     */
    void updateSupperGoodsBaseStart(@Param("commonIds") List<Integer> commonIds);

    /**
     * 根据commonId查询
     *
     * @param commonId
     * @return
     */
    List<BaseDetailVO> selectByCommonIdZ(@Param("commonId") Integer commonId);

    /**
     * 根据commonId查询
     *
     * @param commonId
     * @return
     */
    List<BaseDetailVO> selectSupplierByCommonIdZ(@Param("commonId") Integer commonId);

    /**
     * 更新基础商品状态集合
     *
     * @param param
     */
    void updateSpplierGoodsBaseStateList(@Param("param") PlatFormGoodsStateDTO param);

    /**
     * @param dealerGoodsId 经销商商品主键
     */
    void updateDealerGoodsBaseStateList(Integer dealerGoodsId);

    /**
     * 查询商品信息
     *
     * @param supplierCommonId
     * @return
     */
    GoodsCommonEntity querySupplierCommonBaseInfo(@Param("supplierCommonId") Integer supplierCommonId);

    /**
     * 查询供应商商品信息
     *
     * @param supplierCommonId
     * @return
     */
    List<GoodsBaseEntity> querySupplierGoodsBaseInfo(@Param("supplierCommonId") Integer supplierCommonId);

    /**
     * 查询供应商商品详情
     *
     * @param supplierCommonId
     * @return
     */
    String querySupplierCommonBodyInfo(@Param("supplierCommonId") Integer supplierCommonId);

    /**
     * 更新商品信息
     *
     * @param dealerGoodsId
     * @param GoodsBaseEntity
     */
    void updateDealerGoods(@Param("dealerGoodsId") Integer dealerGoodsId, @Param("param") GoodsBaseEntity GoodsBaseEntity);



    /**
     * 根据Id获取商品
     * @param goodsId 商品Id
     * @return 商品
     */
    ActivityCombinationGoodsVO selectGoodsById(@Param("goodsId") Integer goodsId);


    /**
     * 商品是否是经销商品
     * @param goodsId
     * @return
     */
    GoodsIdsQueryResultVO selectGoodsIdList(@Param("goodsId")Integer goodsId);

    /**
     * 查询商品commonId
     *
     * @param goodsId
     * @return
     */
    Integer selectCommonId(@Param("goodsId") Integer goodsId);


    /**
     * 更新商品库存
     * @param shopId
     */
    void updateGoodsBase(@Param("shopId") String shopId);


    /**
     * 更新商品库存
     * @param param
     * @return
     */
    Integer updateBaseStock(@Param("param")Map<String, Object> param);


    /**
     * 查询商品库存
     * @param gateStoreIndexGoodsDTO
     * @return
     */
    List<GateStoreIndexGoodsVO> queryGoodsBaseInfo(@Param("param") GateStoreIndexGoodsDTO gateStoreIndexGoodsDTO);



    /**
     * 查询模板信息
     * @param supplierGoodsId
     * @return
     */
    Map<String, Object> queryByGoodsIdFeign(@Param("goodsId")Integer supplierGoodsId);


    /**
     *  查询商品信息
     * @param modularizationdGoodsData
     * @return
     */
    ModularizationdGoodsDataDTO queryGoodsInfo(@Param("param")ModularizationdGoodsDataDTO modularizationdGoodsData);


    /*
/**
     * 查询组件化页面 商品信息
     */
    ModularizationdGoodsVO queryModularizationdGoodsInfo(@Param("goodsId") String goodsId);

    /*
/**
     * 查询组件化页面 商品信息
     */
    ModularizationdGoodsVO queryModularizationdGoodsInfo2(@Param("goodsId") String goodsId);

    /**
     * 查询商品信息
     * @param goodsId
     * @return
     */
    GoodsIdsVO newSelectWebFloorMobConfig(@Param("goodsId")Integer goodsId);

    /**
     * 查询商品信息
     * @param goodsIds
     * @return
     */
    List<WapHallGoodsQueryResultVO> selectFoodExhibitionHallByGoodsIds(@Param("goodsIds")List<Integer> goodsIds);

    /**
     * 查询商品可换货信息
     */
    OrderGoodsBarterVO queryGoodsToBarter(Integer goodsId);

    /**
     * @param commonId common主键
     * @return 处理结果
     */
    List<Map<String, Object>> getGoodsSpecNameList(Integer commonId);

    void lessGoodsBySales(@Param("param") WapGoodsSaleSumDTO goods);

    /**
     * 查询当前商品下 有效的规格
     * @param commonId
     * @return
     */
    List<Integer> selectGoodsListByCommonId(@Param("commonId")Integer commonId);

    /**
     * 查询商品规格
     * @param commonId
     * @return
     */
    List<String> queryGoodsSpecStr(@Param("commonId")Integer commonId);
}

