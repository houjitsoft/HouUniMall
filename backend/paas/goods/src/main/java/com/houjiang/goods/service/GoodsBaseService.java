package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.goods.*;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.dto.order.OrderGoodsBarterDTO;
import com.houjiang.commons.dto.shop.GateStoreIndexGoodsDTO;
import com.houjiang.commons.dto.shop.WapTransportAreaQueryDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.ActivityCombinationGoodsVO;
import com.houjiang.commons.vo.goods.*;
import com.houjiang.commons.vo.order.OrderGoodsBarterVO;
import com.houjiang.commons.vo.order.WapShippingQueryResultVO;
import com.houjiang.goods.dto.*;
import com.houjiang.goods.entity.GoodsBaseEntity;
import com.houjiang.goods.vo.GoodsIdsQueryVO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsBaseService
 * @Description 商品表
 * @Author Amos
 * @Date 2023-05-25 09:16:34
 * @Version 1.0
 * @author Amos
 */
public interface GoodsBaseService extends IService<GoodsBaseEntity> {

    /**
     * 根据商品id修改收藏数
     *
     * @param goodId
     * @return
     */
    ResponseResult updateGoodsCollect(Integer goodId);

    /**
     * 订单购物车中获取商品信息，及校验信息
     *
     * @param goodsId
     * @return
     */
    ResponseResult getGoodsDetails(Integer goodsId);


    /**
     * 查询出售中的商品规格列表
     *
     * @param param
     * @return
     */
    ResponseResult queryByGoodsBaseList(ManageCommonIdsDTO param);

    /**
     * 查询出售中的商品规格列表 活动版
     *
     * @param param
     * @return
     */
    ResponseResult queryByGoodsBaseList3(ManageCommonIdsDTO param);


    /**
     * 查询编辑秒杀商品的规格列表
     *
     * @param param
     * @return
     */
    ResponseResult querySeckillGoodsList(ManageCommonIdsDTO param);

    /**
     * 根据goodsid 直接修改库存和库存最大值
     *
     * @param param
     * @return
     */
    ResponseResult updateGoodsBaseByCommonIdAndGoodsId(ManageGoodsBaseListResultDTO param);

    /**
     * 新增商品
     *
     * @param param
     * @return
     */
    ResponseResult newAddGoods(ManageGdsCommonPublishDTO param);

    /**
     * 更新商品
     *
     * @param param
     * @return
     */
    ResponseResult updateGoods(ManageGdsCommonUpdateDTO param);

    /**
     * 查看商品
     *
     * @param param
     * @return
     */
    ResponseResult queryGoods(ManageCommonIdsDTO param);

    /**
     * 查询所有商品 多个goodsID
     *
     * @param param
     * @return
     */
    ResponseResult queryAllGoodsInfo(PlatFormAllGoodsDTO param);

    /**
     * 商品搜索导出
     *
     * @param param
     * @param response
     * @return
     */
    ResponseResult selectCommodityDetailsExp(GoodsDetailsDTO param, HttpServletResponse response);


    /**
     * 查询商品规格等详情
     *
     * @param param
     * @return
     */

    /**
     * @param param 参数
     * @return 处理结果
     */
    ResponseResult selectCommodityDetails(GoodsDetailsDTO param) throws IllegalAccessException;

    /*
/**
     *查询红包下的商品列表
     */
    ResponseResult selectGoodsDetailByRct(Map<String, Object> paraMap);

    /**
     * 更新spu商品 除shopId无其他条件
     *
     * @param map
     * @return
     */
    ResponseResult updateGoodsBaseByMap(Map<String, Object> map);

    /**
     * 更新sku商品状态
     *
     * @param commonParam
     * @return
     */
    ResponseResult updateGoodsBaseState(CommonShopStateDTO commonParam);

    /**
     * 平台删除 积分商品
     *
     * @param param
     * @return
     */
    ResponseResult deleteIntegralGoods(ManageCommonIdsDTO param);

    /**
     * 平台查询 积分商品列表
     *
     * @param param
     * @return
     */
    ResponseResult queryIntegralGoodsAll(Map<String, Object> param);


    /**
     * @param commonId common主键
     * @return 处理结果
     */
    ResponseResult getGoodsCommonDetails(Integer commonId);

    /**
     * 查询出售中的商品规格列表
     *
     * @param param
     * @return
     */
    ResponseResult queryByGoodsBaseList2(ManageCommonIdsDTO param);

    /**
     * 批量调价
     *
     * @param param
     * @return
     */
    ResponseResult batchUpdateGoodsPrice(Map<String, Object> param);

    /**
     * 根据typeId查询商品发布相关信息
     *
     * @param param
     * @return
     */
    ResponseResult queryByTypeId(ManageByTypeIdParamInfoDTO param);


    /**
     * 上传图片到oss服务器
     *
     * @param param
     * @return
     */
    ResponseResult insertOssPhotoPort(OssPhotoInsertDTO param);


    /**
     * 根据商品id查询详情
     */
    ResponseResult queryGdsDetail(WapGoodsDetailQueryDTO param);

    /**
     * h5首页猜你喜欢
     *
     * @return
     */
    ResponseResult queryFootPrintH5(GuessYouLikeQueryDTO param);


    /**
     * 获取 移动端 商品分享页海报
     *
     * @param param
     * @return
     */
    ResponseResult getGoodsPoster(Map<String, Object> param);


    /**
     * 查询活动专区商品
     *
     * @param param
     * @return
     */

    /**
     * @param param 参数
     * @return 处理结果
     */
    ResponseResult queryNoneGoods(NoneGoodsQueryDTO param) throws IllegalAccessException;


    /**
     * 上传图片或文档
     *
     * @param param
     * @return
     */
    ResponseResult uploadToOss(OssUploadUpdateParamDTO param);


    /**
     * 查询兑换券下单 商品信息 feign
     *
     * @param goodsId
     * @return
     */
    WapShippingQueryResultVO queryProductVoucherGoods(Integer goodsId);

    /**
     * 查询可售区域
     *
     * @param param
     * @return
     */
    ResponseResult queryTransportArea(WapTransportAreaQueryDTO param);


    /**
     * 根据Id获取商品
     * @param goodsId 商品Id
     * @return 商品
     */
    ActivityCombinationGoodsVO selectGoodsById(Integer goodsId);

    /**
     * 商品是否是经销商品
     * @param goodsId
     * @return
     */
    GoodsIdsQueryResultVO selectGoodsIdList(Integer goodsId);

    /**
     * 查询商品commonId
     *
     * @param goodsId
     * @return
     */
    Integer selectCommonId(Integer goodsId);


    /**
     * 更新商品commonId
     * @param shopId
     */
    void updateGoodsBase(String shopId);


    /**
     * 更新商品库存
     * @param param
     * @return
     */
    Integer updateBaseStock(Map<String, Object> param);


    /**
     * 查询商品信息
     * @param gateStoreIndexGoodsDTO
     * @return
     */
    List<GateStoreIndexGoodsVO> queryGoodsBaseInfo(GateStoreIndexGoodsDTO gateStoreIndexGoodsDTO);


    /**
     * 查询模板信息
     * @param supplierGoodsId
     * @return
     */
    Map<String, Object> queryByGoodsId(Integer supplierGoodsId);


    /**
     * 查询商品信息
     * @param modularizationdGoodsData
     * @return
     */
    ModularizationdGoodsDataDTO queryGoodsInfo(ModularizationdGoodsDataDTO modularizationdGoodsData);

    /**
     * 查询商品信息
     * @param goodsId
     * @return
     */
    ModularizationdGoodsVO queryModularizationdGoodsInfo(String goodsId);

    /**
     * 查询商品信息
     * @param goodsId
     * @return
     */
    ModularizationdGoodsVO queryModularizationdGoodsInfo2(String goodsId);


    /**
     * 查询商品信息
     * @param goodsId
     * @return
     */
    GoodsIdsVO newSelectWebFloorMobConfig(Integer goodsId);

    /**
     * 查询分类信息
     * @param
     * @param catIdsDTO
     * @return
     */
    List<GoodsIdsQueryVO> selectByCatIds(CatIdsDTO catIdsDTO);

    /**
     * 根据dify传输过来的数据查询商品信息
     */
    ResponseResult queryGoodsByDify(String data);

    /**
     * 查看申请换货商品
     */
    ResponseResult<OrderGoodsBarterVO> queryGoodsToBarter(OrderGoodsBarterDTO param);

    /**
     * 查询换货商品信息
     * @param goodsId 换货商品id
     */
    ResponseResult<OrderGoodsBarterVO> selectBarterGoods(Integer goodsId);

    /**
     * 根据规格值查看换货商品规格信息
     */
    ResponseResult<BaseDetailVO> queryGoodsSpecToBarter(OrderGoodsBarterDTO param);

    /**
     * @param goodsNumMap 商品nummap
     */
    void updateGoodsSaleNum(Map<String, Object> goodsNumMap);
}

