package com.houjiang.goods.controller;


import com.houjiang.commons.annnotation.Log;
import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.constant.CommonStatusEnum;
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
import com.houjiang.goods.vo.GoodsIdsQueryVO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsBaseController
 * @Description 商品表
 * @Author Amos
 * @Date 2023-05-25 09:16:26
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/goods/base")
public class GoodsBaseController extends BaseController {

    /**
     * 根据商品id修改收藏数
     *
     * @param goodId
     * @return
     */
    @GetMapping("/updateGoodsCollect")
    public ResponseResult updateGoodsCollect(@RequestParam("goodId") Integer goodId) {
        ResponseResult result = goodsBaseService.updateGoodsCollect(goodId);
        return result;
    }

    /**
     * 订单购物车中获取商品信息，及校验信息
     *
     * @param goodsId
     * @return
     */
    @GetMapping("/get/cart/getGoodsDetails")
    public ResponseResult getGoodsDetails(@RequestParam Integer goodsId) {
        ResponseResult goodsDetails = goodsBaseService.getGoodsDetails(goodsId);
        return goodsDetails;
    }

    /**
     * 查询出售中的商品规格列表
     *
     * @param param
     * @return
     */
    @PostMapping("/goods/list/query")
    public ResponseResult queryByGoodsBaseList(@RequestBody ManageCommonIdsDTO param) {
        ResponseResult result = goodsBaseService.queryByGoodsBaseList(param);
        return result;
    }

    /**
     * 查询出售中的商品规格列表 活动版
     *
     * @param param
     * @return
     */
    @PostMapping("/goods/list/query3")
    public ResponseResult queryByGoodsBaseList3(@RequestBody ManageCommonIdsDTO param) {
        ResponseResult result = goodsBaseService.queryByGoodsBaseList3(param);
        return result;
    }

    /**
     * 查询编辑秒杀商品的规格列表
     *
     * @param param
     * @return
     */
    @PostMapping("/querySeckillGoodsList")
    public ResponseResult querySeckillGoodsList(@RequestBody ManageCommonIdsDTO param) {
        ResponseResult result = goodsBaseService.querySeckillGoodsList(param);
        return result;
    }

    /**
     * 根据goodsid 直接修改库存和库存最大值
     *
     * @param param
     * @return
     */
    @PostMapping("/goods/list/updateById")
    public ResponseResult queryGoodsBaseByCommonIdAndGoodsId(@RequestBody ManageGoodsBaseListResultDTO param) {
        ResponseResult result = goodsBaseService.updateGoodsBaseByCommonIdAndGoodsId(param);
        return result;
    }

    /**
     * 新增商品
     *
     * @param param
     * @return
     */
    @Log("新增商品")
    @SensitiveCheck(field = {"commonName", "commonPromotionTips", "goodsCommonDetail.commonBody"})
    @PostMapping("/newAddGoods")
    public ResponseResult newAddGoods(@RequestBody ManageGdsCommonPublishDTO param) {

        if (CollectionUtils.isEmpty(param.getGoodsImages())) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "请上传商品主图");
        }
        if (null == param.getSpecParam() || param.getSpecParam().isEmpty()) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "规格不能为空");
        }
        if (param.getSpecParam().size() > 3) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "规格数量不能超过3个");
        }

        ResponseResult result = goodsBaseService.newAddGoods(param);
        return result;
    }

    /**
     * 更新商品
     *
     * @param param
     * @return
     */
    @SensitiveCheck(field = {"commonName", "commonPromotionTips", "goodsCommonDetail.commonBody"})
    @PostMapping("/update")
    @Log("更新商品")
    public ResponseResult updateGoods(@RequestBody ManageGdsCommonUpdateDTO param) {

        if (null == param.getSpecParam() || param.getSpecParam().isEmpty()){
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "规格不能为空");
        }
        if (param.getSpecParam().size() > 3){
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "规格数量不能超过3个");
        }
        ResponseResult result = goodsBaseService.updateGoods(param);
        return result;
    }

    /**
     * 查看商品
     *
     * @param param
     * @return
     */
    @PostMapping("/query")
    public ResponseResult queryGoods(@RequestBody ManageCommonIdsDTO param) {
        ResponseResult result = goodsBaseService.queryGoods(param);
        return result;
    }

    /**
     * 查询所有商品 多个goodsID
     *
     * @param param
     * @return
     */
    @PostMapping("/all/goods/querygoods")
    public ResponseResult queryAllGoodsInfo(@RequestBody PlatFormAllGoodsDTO param) {
        ResponseResult result = goodsBaseService.queryAllGoodsInfo(param);
        return result;

    }

    /**
     * 商品搜索导出
     *
     * @param param
     * @param response
     * @return
     */
    @PostMapping("/goods/selectDetailsExp")
    public ResponseResult selectCommodityDetailsExp(@RequestBody GoodsDetailsDTO param, HttpServletResponse response) {
        ResponseResult result = goodsBaseService.selectCommodityDetailsExp(param, response);
        return result;
    }

    /**
     * 查询商品规格等详情
     *
     * @param param
     * @return
     */
    @PostMapping("/goods/selectDetails")
    public ResponseResult selectCommodityDetails(@RequestBody GoodsDetailsDTO param) throws IllegalAccessException {
        ResponseResult result = goodsBaseService.selectCommodityDetails(param);
        return result;
    }

    /*
/**
     *查询红包下的商品列表
     */
    @PostMapping("/selectGoodsDetailByRct")
    public ResponseResult selectGoodsDetailByRct(@RequestBody(required = false) Map<String, Object> paraMap) {
        ResponseResult result = goodsBaseService.selectGoodsDetailByRct(paraMap);
        return result;
    }


    /**
     * 更新sku商品 除shopId无其他条件
     *
     * @param map
     * @return
     */
    @PostMapping("/updateGoodsBase")
    public ResponseResult updateGoodsBaseByMap(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsBaseService.updateGoodsBaseByMap(map);
        return result;
    }


    /**
     * 更新sku商品状态
     *
     * @param commonParam
     * @return
     */
    @PostMapping("/updateGoodsBaseState")
    public ResponseResult updateGoodsBaseState(@RequestBody CommonShopStateDTO commonParam) {
        ResponseResult result = goodsBaseService.updateGoodsBaseState(commonParam);
        return result;
    }

    /**
     * 平台删除 积分商品
     *
     * @param param
     * @return
     */
    @PostMapping("/deleteIntegralGoods")
    public ResponseResult deleteIntegralGoods(@RequestBody ManageCommonIdsDTO param) {
        ResponseResult result = goodsBaseService.deleteIntegralGoods(param);
        return result;
    }

    /**
     * 平台查询 积分商品列表
     *
     * @param param
     * @return
     */
    @PostMapping("/queryIntegralGoodsAll")
    public ResponseResult queryIntegralGoodsAll(@RequestBody Map<String, Object> param) {
        ResponseResult result = goodsBaseService.queryIntegralGoodsAll(param);
        return result;
    }

    /**
     * 查询商品信息
     *
     * @param commonId
     * @return
     */
    @GetMapping("/get/cart/getGoodsCommonDetails")
    public ResponseResult getGoodsCommonDetails(@RequestParam Integer commonId) {
        ResponseResult result = goodsBaseService.getGoodsCommonDetails(commonId);
        return result;
    }


    /**
     * 查询出售中的商品规格列表
     *
     * @param param
     * @return
     */
    @PostMapping("/goods/list/query2")
    public ResponseResult queryByGoodsBaseList2(@RequestBody ManageCommonIdsDTO param) {
        ResponseResult result = goodsBaseService.queryByGoodsBaseList2(param);
        return result;
    }

    /**
     * 批量调价
     *
     * @param param
     * @return
     */
    @PostMapping("/price/batchUpdate")
    public ResponseResult batchUpdateGoodsPrice(@RequestBody Map<String, Object> param) {
        ResponseResult result = goodsBaseService.batchUpdateGoodsPrice(param);
        return result;
    }

    /**
     * 根据typeId查询商品发布相关信息
     *
     * @param param
     * @return
     */
    @PostMapping("/typeid/query")
    public ResponseResult queryByTypeId(@RequestBody ManageByTypeIdParamInfoDTO param) {
        ResponseResult result = goodsBaseService.queryByTypeId(param);
        return result;
    }


    /**
     * 上传图片到oss服务器
     *
     * @param param
     * @return
     */
    @PostMapping("/OSS/insertOssPhotoPort")
    public ResponseResult insertOssPhotoPort(@RequestBody OssPhotoInsertDTO param) {
        ResponseResult result = goodsBaseService.insertOssPhotoPort(param);
        return result;
    }

    /**
     * 根据商品id查询详情
     */
    @PostMapping("/goodsid/query")
    public ResponseResult queryGdsDetail(@RequestBody WapGoodsDetailQueryDTO param) {
        ResponseResult result = goodsBaseService.queryGdsDetail(param);
        return result;
    }

    /**
     * h5首页猜你喜欢
     *
     * @return
     */
    @PostMapping("/h5Userfoot/query")
    public ResponseResult queryFootPrintH5(@RequestBody GuessYouLikeQueryDTO param) {
        ResponseResult result = goodsBaseService.queryFootPrintH5(param);
        return result;
    }

    /**
     * 获取 移动端 商品分享页海报
     *
     * @param param
     * @return
     */
    @PostMapping("/h5/goods/getGoodsPoster")
    public ResponseResult getGoodsPoster(@RequestBody Map<String, Object> param) {
        ResponseResult result = goodsBaseService.getGoodsPoster(param);
        return result;
    }


    /**
     * 查询活动专区商品
     *
     * @param param
     * @return
     */
    @PostMapping("/goods/query")
    public ResponseResult queryNoneGoods(@RequestBody NoneGoodsQueryDTO param) throws IllegalAccessException {
        ResponseResult result = goodsBaseService.queryNoneGoods(param);
        return result;
    }


    /**
     * 上传图片或文档
     *
     * @param param
     * @return
     */
    @PostMapping("/oss/upload")
    public ResponseResult uploadToOss(@RequestBody OssUploadUpdateParamDTO param) {
        ResponseResult result = goodsBaseService.uploadToOss(param);
        return result;
    }

    /**
     * 查询兑换券下单 商品信息 feign
     *
     * @param goodsId
     * @return
     */
    @PostMapping("/queryProductVoucherGoods")
    public WapShippingQueryResultVO queryProductVoucherGoods(@RequestParam("goodsId") Integer goodsId) {
        WapShippingQueryResultVO result = goodsBaseService.queryProductVoucherGoods(goodsId);
        return result;
    }

    /**
     * 查询可售区域
     *
     * @param param
     * @return
     */
    @PostMapping("/transport/area/query")
    public ResponseResult queryTransportArea(@RequestBody WapTransportAreaQueryDTO param) {

        return goodsBaseService.queryTransportArea(param);
    }

    /**
     * 根据Id获取商品
     * @param goodsId 商品Id
     * @return 商品
     */
    @GetMapping("/feign/selectGoodsById")
    public ActivityCombinationGoodsVO selectGoodsById(@RequestParam("goodsId") Integer goodsId){
        ActivityCombinationGoodsVO goods = goodsBaseService.selectGoodsById(goodsId);
        return goods;

    }

    /**
     * 商品是否是经销商品
     * @param goodsId
     * @return
     */
    @GetMapping("/feign/selectGoodsIdList")
    public GoodsIdsQueryResultVO selectGoodsIdList(@RequestParam("goodsId")Integer goodsId){
        GoodsIdsQueryResultVO goods = goodsBaseService.selectGoodsIdList(goodsId);
        return goods;
    }

    /**
     * 查询商品commonId
     *
     * @param goodsId
     * @return
     */
    @GetMapping("/feign/selectCommonId")
    public   Integer selectCommonId(@RequestParam("goodsId") Integer goodsId){
        Integer commonId = goodsBaseService.selectCommonId(goodsId);
        return commonId;
    }

    /**
     * 修改商品状态
     * @param shopId
     */
    @GetMapping("/feign/updateGoodsBase")
    void updateGoodsBase(@RequestParam("shopId") String shopId){
        goodsBaseService.updateGoodsBase(shopId);
    }


    /**
     * 修改商品库存
     * @param param
     * @return
     */
    @PostMapping("/feign/updateBaseStock")
    public Integer updateBaseStock(@RequestBody Map<String, Object> param){
        Integer result = goodsBaseService.updateBaseStock(param);
        return result;
    }

    /**
     * 查询商品信息
     * @param gateStoreIndexGoodsDTO
     * @return
     */
    @PostMapping("/feign/queryGoodsBaseInfo")
    public List<GateStoreIndexGoodsVO> queryGoodsBaseInfo(@RequestBody GateStoreIndexGoodsDTO gateStoreIndexGoodsDTO){
        List<GateStoreIndexGoodsVO> result = goodsBaseService.queryGoodsBaseInfo(gateStoreIndexGoodsDTO);
        return result;
    }

    /**
     * 查询模板信息
     * @param supplierGoodsId
     * @return
     */
    @GetMapping("/feign/queryByGoodsId")
    public Map<String, Object> queryByGoodsId(@RequestParam("supplierGoodsId")Integer supplierGoodsId){
        Map<String, Object> result = goodsBaseService.queryByGoodsId(supplierGoodsId);
        return result;
    }

    /**
     * 查询商品信息
     * @param modularizationdGoodsData
     * @return
     */
    @PostMapping("/feign/queryGoodsInfo")
    public ModularizationdGoodsDataDTO queryGoodsInfo(@RequestBody ModularizationdGoodsDataDTO modularizationdGoodsData){
        ModularizationdGoodsDataDTO result = goodsBaseService.queryGoodsInfo(modularizationdGoodsData);
        return result;
    }

    /**
     * 查询商品信息
     * @param goodsId
     * @return
     */
    @GetMapping("/feign/queryModularizationdGoodsInfo")
    public ModularizationdGoodsVO queryModularizationdGoodsInfo(@RequestParam("goodsId") String goodsId){
        ModularizationdGoodsVO result = goodsBaseService.queryModularizationdGoodsInfo(goodsId);
        return result;
    }

    /**
     * 查询商品信息
     * @param goodsId
     * @return
     */
    @GetMapping("/feign/queryModularizationdGoodsInfo2")
    public ModularizationdGoodsVO queryModularizationdGoodsInfo2(@RequestParam("goodsId") String goodsId){
        ModularizationdGoodsVO result = goodsBaseService.queryModularizationdGoodsInfo2(goodsId);
        return result;
    }

    /**
     * 查询商品信息
     * @param goodsId
     * @return
     */
    @GetMapping("/feign/newSelectWebFloorMobConfig")
    public GoodsIdsVO newSelectWebFloorMobConfig(@RequestParam("goodsId")Integer goodsId){
        GoodsIdsVO result = goodsBaseService.newSelectWebFloorMobConfig(goodsId);
        return result;
    }

    /**
     * 查询分类信息
     * @param
     * @param catIdsDTO
     * @return
     */
    @PostMapping("/feign/selectByCatIds")
    public List<GoodsIdsQueryVO> selectByCatIds(@RequestBody CatIdsDTO catIdsDTO){
        List<GoodsIdsQueryVO> result = goodsBaseService.selectByCatIds(catIdsDTO);
        return result;
    }

    /**
     * 根据dify传输过来的数据查询商品信息
     */
    @PostMapping("/queryGoodsByDify")
    public ResponseResult queryGoodsByDify(@RequestParam("data") String data){
        return goodsBaseService.queryGoodsByDify(data);
    }

    /**
     * 查看申请换货商品
     */
    @PostMapping("/queryGoodsToBarter")
    public ResponseResult<OrderGoodsBarterVO> queryGoodsToBarter(@RequestBody OrderGoodsBarterDTO param) {
        return goodsBaseService.queryGoodsToBarter(param);
    }

    /**
     * 查询换货商品信息
     * @param goodsId 换货商品id
     */
    @GetMapping("/feign/selectBarterGoods")
    public ResponseResult<OrderGoodsBarterVO> selectBarterGoods(@RequestParam("goodsId") Integer goodsId){
        return goodsBaseService.selectBarterGoods(goodsId);
    }

    /**
     * 根据规格值查看换货商品规格信息
     */
    @PostMapping("/feign/queryGoodsSpecToBarter")
    public ResponseResult<BaseDetailVO> queryGoodsSpecToBarter(@RequestBody OrderGoodsBarterDTO param) {
        return goodsBaseService.queryGoodsSpecToBarter(param);
    }

    /**
     * 更新商品销量
     *
     * @param goodsNumMap
     * @return
     */
    @PostMapping("/updateGoodsSaleNum")
    public void updateGoodsSaleNum(@RequestBody Map<String, Object> goodsNumMap) {
        goodsBaseService.updateGoodsSaleNum(goodsNumMap);
    }
}

