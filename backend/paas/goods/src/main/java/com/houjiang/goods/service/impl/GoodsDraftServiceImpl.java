package com.houjiang.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.goods.*;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.goods.ManageGdsCommonResultVO;
import com.houjiang.commons.vo.goods.ManageShopBaseQueryResultVO;
import com.houjiang.commons.vo.goods.ManageTransportAreaResultVO;
import com.houjiang.commons.vo.goods.ManageTransportTemplateResultVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.entity.GoodsDraftEntity;
import com.houjiang.goods.mapper.GoodsDraftMapper;
import com.houjiang.goods.service.GoodsDraftService;
import com.houjiang.goods.vo.ManageGdsBaseAddResultVO;
import com.houjiang.goods.vo.ManageGdsCommonQueryResultDraftVO;
import com.houjiang.goods.vo.ManageGdsCommonQueryResultStrVO;
import com.houjiang.goods.vo.ManageGdsCommonQueryResultVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * @ClassName GoodsDraftServiceImpl
 * @Description 草稿箱
 * @Author Amos
 * @Date 2023/8/11 9:20
 * @Version 1.0
 * @author Amos
 */
@Service
public class GoodsDraftServiceImpl extends ServiceImpl<GoodsDraftMapper, GoodsDraftEntity>
        implements GoodsDraftService {

    /**
     * 根据给定的参数查询草稿信息。
     *
     * @param param 包含查询所需参数的映射，如token、shopId和catId。
     * @return 返回查询结果的响应对象。
     */
    @Override
    public ResponseResult queryDraft(Map<String, Object> param) {
        // 从参数中提取token
        String token = (String) param.get("token");
        // 根据token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 如果用户信息为空，则返回权限不足的错误结果
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        // 从参数中提取shopId和catId
        String shopId = (String) param.get("shopId");
        if(StringUtils.isBlank(shopId)){
            shopId = userInfo.getShopId();
        }
        Integer catId = (Integer) param.get("catId");

        // 初始化草稿列表
        List<ManageGdsCommonQueryResultDraftVO> drafts = new ArrayList<>();
        // 根据shopId和catId查询草稿内容
        List<ManageGdsCommonQueryResultStrVO> strList = this.baseMapper.queryDraft(shopId, catId);
        // 初始化一个映射用于存储最终结果
        Map<String, Object> map = new HashMap<>();
        // 如果查询结果不为空
        if (strList != null && !strList.isEmpty()) {
            // 遍历查询结果
            for (ManageGdsCommonQueryResultStrVO str : strList) {
                // 初始化一个草稿对象
                ManageGdsCommonQueryResultVO manageGdsCommonQueryResult = new ManageGdsCommonQueryResultVO();
                // 从查询结果中解析出草稿内容，并转换为相应的DTO
                ManageGdsCommonPublishDTO manageGdsCommonPublish = JSONObject.parseObject(str.getDraftContent(), ManageGdsCommonPublishDTO.class);
                // 初始化商品基础信息列表
                List<ManageGdsBaseAddResultVO> gdsBases = new ArrayList<>();
                // 如果草稿中包含商品基础信息
                if(!CollectionUtils.isEmpty( manageGdsCommonPublish.getGdsBases())){
                    // 遍历商品基础信息列表
                    for (ManageGdsBaseAddPublishDTO push : manageGdsCommonPublish.getGdsBases()) {
                        // 初始化一个商品基础信息对象
                        ManageGdsBaseAddResultVO result = new ManageGdsBaseAddResultVO();
                        result.setBrandId(push.getBrandId());
                        result.setCatId(push.getCatId());
                        result.setCommission(push.getCommission());
                        result.setCommodityBarTypeCode(push.getCommodityBarTypeCode());
                        result.setCommonId(push.getCommonId());
                        result.setDw(push.getDw());
                        result.setGoodsName(push.getGoodsName());
                        result.setGoodsAlarm(push.getGoodsAlarm());
                        result.setGoodsBabyPrice(push.getGoodsBabyPrice());
                        result.setGoodsBarcode(push.getGoodsBarcode());
                        result.setGoodsCode(push.getGoodsCode());
                        result.setGoodsCostPrice(push.getGoodsCostPrice());
                        result.setGoodsCubage(push.getGoodsCubage());
                        result.setGoodsImage(push.getGoodsImage());
                        result.setGoodsIsRecommend(push.getGoodsIsRecommend());
                        result.setGoodsIsShelves(push.getGoodsIsShelves());
                        result.setGoodsMarketPrice(push.getGoodsMarketPrice());
                        result.setGoodsMaxSale(push.getGoodsMaxSale());
                        result.setGoodsMinSale(push.getGoodsMinSale());
                        result.setGoodsPrice(push.getGoodsPrice());
                        result.setGoodsPromotionTips(push.getGoodsPromotionTips());
                        result.setGoodsSendBack(push.getGoodsSendBack());
                        result.setGoodsShopGateStock(push.getGoodsShopGateStock());
                        result.setGoodsSpec(push.getGoodsSpec());
                        result.setGoodsSpecValue(push.getGoodsSpecValue());
                        result.setGoodsStock(push.getGoodsStock());
                        if (StringUtils.isNotEmpty(push.getGoodsVolume())) {
                            BigDecimal goodsVolume = new BigDecimal(push.getGoodsVolume());
                            result.setGoodsVolume(goodsVolume);
                        }
                        if (push.getIsEnquiry() != null && push.getIsEnquiry() == true) {
                            result.setIsEnquiry(1);
                        } else {
                            result.setIsEnquiry(0);
                        }
                        result.setIsUse(push.getIsUse());
                        result.setMerchantCode(push.getMerchantCode());
                        result.setMoq(push.getMoq());
                        result.setSellingPoint(push.getSellingPoint());
                        result.setShopId(Integer.valueOf(shopId));
                        result.setShopName(push.getShopName());
                        result.setSpecId(push.getSpecId());
                        result.setSpecName(push.getSpecName());
                        result.setSpecValue(push.getGoodsSpecValue());
                        result.setTaxInclusiveAmount(push.getTaxInclusiveAmount());
                        result.setTaxRate(push.getTaxRate());
                        result.setTypeId(push.getTypeId());
                        gdsBases.add(result);
                    }
                }
                // 设置商品基础信息列表到草稿对象
                manageGdsCommonQueryResult.setGdsBases(gdsBases);

                // 初始化一个通用结果对象
                ManageGdsCommonResultVO commonResult = new ManageGdsCommonResultVO();
                commonResult.setAfterService(manageGdsCommonPublish.getAfterService());
                commonResult.setAfterServiceList(manageGdsCommonPublish.getAfterServiceList());
                commonResult.setBrandId(manageGdsCommonPublish.getBrandId());
                commonResult.setBrandName(manageGdsCommonPublish.getBrandName());
                commonResult.setCatId(manageGdsCommonPublish.getCatId());
                commonResult.setCatIds(manageGdsCommonPublish.getCatIds());
                commonResult.setCatName(manageGdsCommonPublish.getCatName());
                commonResult.setCommonAddTime(manageGdsCommonPublish.getCommonAddTime());
                commonResult.setCommonAlarm(manageGdsCommonPublish.getCommonAlarm());
                commonResult.setCommonBabyPrice(manageGdsCommonPublish.getCommonBabyPrice());
                commonResult.setCommonCostPrice(manageGdsCommonPublish.getCommonCostPrice());
                commonResult.setCommonCubage(manageGdsCommonPublish.getCommonCubage());
                commonResult.setCommonFormatidBottom(manageGdsCommonPublish.getCommonFormatidBottom());
                commonResult.setCommonFormatidTop(manageGdsCommonPublish.getCommonFormatidTop());
                commonResult.setCommonName(manageGdsCommonPublish.getCommonName());
                commonResult.setCommonImage(manageGdsCommonPublish.getCommonImage());
                commonResult.setCommonIsRecommend(manageGdsCommonPublish.getCommonIsRecommend());
                commonResult.setCommonIsShopTake(manageGdsCommonPublish.getCommonIsShopTake());
                commonResult.setCommonLimit(manageGdsCommonPublish.getCommonLimit());
                commonResult.setCommonMarketPrice(manageGdsCommonPublish.getCommonMarketPrice());
                commonResult.setCommonPackingList(manageGdsCommonPublish.getCommonPackingList());
                commonResult.setCommonPrice(manageGdsCommonPublish.getCommonPrice());
                commonResult.setCommonPromotionTips(manageGdsCommonPublish.getCommonPromotionTips());
                commonResult.setCommonProperty(manageGdsCommonPublish.getCommonProperty());
                commonResult.setCommonSellTime(manageGdsCommonPublish.getCommonSellTime());
                commonResult.setCommonService(manageGdsCommonPublish.getCommonService());
                commonResult.setCommonSpecName(manageGdsCommonPublish.getCommonSpecName());
                commonResult.setCommonSpecValue(manageGdsCommonPublish.getCommonSpecValue());
                commonResult.setCommonState(manageGdsCommonPublish.getCommonState());
                commonResult.setCommonStock(manageGdsCommonPublish.getCommonStock());
                commonResult.setCommonType(manageGdsCommonPublish.getCommonType());
                commonResult.setCommonVideo(manageGdsCommonPublish.getCommonVideo());
                commonResult.setCustomInformation(manageGdsCommonPublish.getCustomInformation());
                commonResult.setCustomInformationList(manageGdsCommonPublish.getCustomInformationList());
                commonResult.setDeliveryDate(manageGdsCommonPublish.getDeliveryDate());
                commonResult.setDw(manageGdsCommonPublish.getDw());
                commonResult.setGoodsOrderDate(manageGdsCommonPublish.getGoodsOrderDate());
                commonResult.setGoodsRecommendedMaxPrice(manageGdsCommonPublish.getGoodsRecommendedMaxPrice());
                commonResult.setGoodsRecommendedMinPrice(manageGdsCommonPublish.getGoodsRecommendedMinPrice());
                commonResult.setGoodsSendBack(manageGdsCommonPublish.getGoodsSendBack());
                commonResult.setGoodsType(manageGdsCommonPublish.getGoodsType());
                commonResult.setHotState(manageGdsCommonPublish.getHotState());
                commonResult.setPastDue(manageGdsCommonPublish.getPastDue());
                commonResult.setDeliveryTime(manageGdsCommonPublish.getDeliveryTime());
                if (!StringUtils.isEmpty(manageGdsCommonPublish.getPeriodOfValidity())) {
                    Date date = DateUtils.parseCNDate(manageGdsCommonPublish.getPeriodOfValidity());
                    commonResult.setPeriodOfValidity(date);
                }
                commonResult.setRestrictionHours(manageGdsCommonPublish.getRestrictionHours());
                commonResult.setRestrictionTimes(manageGdsCommonPublish.getRestrictionTimes());
                commonResult.setSellingPoint(manageGdsCommonPublish.getSellingPoint());
                commonResult.setShopCatId(manageGdsCommonPublish.getShopCatId());
                commonResult.setShopId(manageGdsCommonPublish.getShopId()==null?null:Integer.valueOf(manageGdsCommonPublish.getShopId()));
                commonResult.setShopName(manageGdsCommonPublish.getShopName());
                commonResult.setShopSelfSupport(manageGdsCommonPublish.getShopSelfSupport());
                commonResult.setShopStatus(manageGdsCommonPublish.getShopStatus());
                commonResult.setSpecParam(JSON.toJSONString(manageGdsCommonPublish.getSpecParam()));
                commonResult.setSpecParamList(manageGdsCommonPublish.getSpecParam());
                commonResult.setTransportAreaId(manageGdsCommonPublish.getTransportAreaId());
                if (manageGdsCommonPublish.getTransportAreaId() != null) {
                    ManageTransportAreaResultVO transportAreas = this.baseMapper.selectAreaById(manageGdsCommonPublish.getTransportAreaId());
                    if (transportAreas != null) {
                        commonResult.setTransportAreas(transportAreas);
                    }
                }
                commonResult.setTransportTemplate(manageGdsCommonPublish.getTransportTemplate());
                if (manageGdsCommonPublish.getTransportTemplate() != null) {
                    ManageTransportTemplateResultVO transportTemplate = this.baseMapper.selectTemplateById(manageGdsCommonPublish.getTransportTemplate());
                    if (transportTemplate != null) {
                        commonResult.setTransportTemplates(transportTemplate);
                    }
                }
                commonResult.setTransportTypeId(manageGdsCommonPublish.getTransportTypeId());
                commonResult.setType(manageGdsCommonPublish.getType());
                commonResult.setTypeId(manageGdsCommonPublish.getTypeId());
                commonResult.setVirtualGoods(manageGdsCommonPublish.getVirtualGoods());
                commonResult.setCategoryAttributesList(manageGdsCommonPublish.getCategoryAttributesList());
                manageGdsCommonQueryResult.setCommonResult(commonResult);

                manageGdsCommonQueryResult.setGoodsCommonDetail(manageGdsCommonPublish.getGoodsCommonDetail());
                List<GdbMerchantGuiGeGoodsImagesDTO> goodsImages = manageGdsCommonPublish.getGoodsImages();
                for (GdbMerchantGuiGeGoodsImagesDTO goodsImage : goodsImages) {
                    goodsImage.setImagesId(null);
                }
                manageGdsCommonQueryResult.setGoodsImages(goodsImages);
                manageGdsCommonQueryResult.setManageGdsGoodsConfig(manageGdsCommonPublish.getManageGdsGoodsConfig());
                if (!CollectionUtils.isEmpty(manageGdsCommonPublish.getGoodsCatIdList())) {
                    List<MerchantShopGoodsCatDTO> shopGoodsCatList = this.baseMapper.queryShopGoodsCatList(manageGdsCommonPublish.getGoodsCatIdList());
                    for (MerchantShopGoodsCatDTO merchantShopGoodsCat : shopGoodsCatList) {
                        if (merchantShopGoodsCat.getParentId() != 0) {
                            String catName = this.baseMapper.queryShopGoodsCatName(merchantShopGoodsCat.getParentId());
                            merchantShopGoodsCat.setShopGoodsCatName(catName + "/" + merchantShopGoodsCat.getShopGoodsCatName());
                        }
                    }
                    manageGdsCommonQueryResult.setShopGoodsCatList(shopGoodsCatList);
                }
                manageGdsCommonQueryResult.setGoodsCatList(manageGdsCommonPublish.getGoodsCatIdList());
                // 初始化一个草稿详情对象
                ManageGdsCommonQueryResultDraftVO draft = new ManageGdsCommonQueryResultDraftVO();
                // 设置草稿详情的基本属性
                draft.setManageGdsCommonQueryResult(manageGdsCommonQueryResult);
                draft.setDraftId(str.getDraftId());
                draft.setDraftName(str.getDraftName());
                draft.setCatId(str.getCatId());
                draft.setShopId(str.getShopId());
                draft.setCreateTime(str.getCreateTime());
                // 将草稿详情添加到草稿列表
                drafts.add(draft);
            }
            // 设置查询到的草稿列表和总数到结果映射
            map.put("list", drafts);
            map.put("totalCount", this.baseMapper.queryDraftCount(shopId, catId));
        } else {
            // 如果查询结果为空，则设置空列表和总数为0
            map.put("list", drafts);
            map.put("totalCount", 0);
        }

        // 返回查询成功的响应结果，包含草稿列表和总数
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL.getMsg(), map);
    }

    /**
     * 删除草稿方法
     *
     * @param param 包含删除草稿所需信息的参数映射，包括用户令牌（token）和草稿ID（draftId）。
     * @return 返回一个表示操作结果的ResponseResult对象。
     *         如果操作成功，返回包含成功信息和删除成功的业务码；
     *         如果操作失败，例如用户没有店铺权限，则返回包含错误信息和权限不足的业务码。
     */
    @Override
    public ResponseResult deleteDraft(Map<String, Object> param) {
        // 从参数映射中获取用户令牌
        String token = (String) param.get("token");
        // 通过用户令牌从Redis中获取用户信息
        // 根据code存储用户信息
        String userInfoStr = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
        // 将用户信息字符串反序列化为UserInfoDTO对象
        UserInfoDTO dto = JSON.parseObject(userInfoStr, UserInfoDTO.class);
        // 从UserInfoDTO中获取用户信息
        UserInfoVO userInfo = dto.getUser();
        // 检查用户是否有店铺权限，如果没有则返回权限不足的错误信息
        if (null == userInfo.getShopId()) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
        }
        // 从参数映射中获取草稿ID
        Integer draftId = (Integer) param.get("draftId");
        // 使用基类Mapper的deleteById方法删除指定ID的草稿
        this.baseMapper.deleteById(draftId);
        // 返回操作成功的信息和业务码
        return ResponseResult.success(BizCodeEnume.DELETE_SUCCESS);
    }

    /**
     * 将商品信息添加到草稿中。
     *
     * @param param 包含草稿相关信息的Map对象。
     * @return 成功添加返回成功结果，失败返回失败结果。
     */
    @Override
    @Transactional
    public ResponseResult addGoodsToDraft(Map<String, Object> param) {
        // 从参数中提取token、草稿名和草稿内容
        String token = (String) param.get("token");
        String draftName = (String) param.get("draftName");
        Object draftContent = param.get("draftContent");

        // 将草稿内容转换为ManageGdsCommonPublishDTO对象
        String str = JSONObject.toJSONString(draftContent);
        ManageGdsCommonPublishDTO param2 = JSONObject.parseObject(str, ManageGdsCommonPublishDTO.class);

        try {
            // 判断用户权限
            // 权限判断
            UserPermissionsResult userPermissionsResult = UserService.judgePermissions(token);
            if (!userPermissionsResult.getFlag()) {
                return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS);
            }

            // 设置草稿相关属性，如店铺ID、更新者、更新时间等
            String shopId = userPermissionsResult.getShopId();
            param2.setShopId(shopId);
            param2.setCommonUpdateBy(userPermissionsResult.getUserId());
            param2.setCommonUpdateTime(new Date());

            // 序列化自定义信息和售后服务列表
            //	序列化数组自定义信息
            String json = JSON.toJSONString(param2.getCustomInformationList());
            param2.setCustomInformation(json);
            //	序列化数组自定义信息
            String jsonString = JSON.toJSONString(param2.getAfterServiceList());
            param2.setAfterService(jsonString);

            // 初始化商品默认属性
            // 初始化默认值
            // 判断是否放入仓库
            if (param2.getCommonState() == null || param2.getCommonState() == 0) {
                param2.setCommonState(0);
            }
            param2.setCommonCubage(new BigDecimal(0));
            param2.setGoodsId("0");
            param2.setCommonVerify(10);
            param2.setCommonStock(0);
            param2.setCommonAddTime(new Date());
            param2.setCommonSellTime(DateUtils.stringToTime(param2.getCommonSellTimeStr()));

            // 设置商品主图
            if (!CollectionUtils.isEmpty(param2.getGoodsImages())) {
                if (!param2.getGoodsImages().get(0).getMerchantGoodsImages().isEmpty()) {
                    String image = param2.getGoodsImages().get(0).getMerchantGoodsImages().get(0).getImagesImage();
                    param2.setCommonImage(image);
                }
            }

            // 获取店铺基础信息，用于设置商品的区域ID、店铺名称等
            // 获取店铺区域id
            ManageShopBaseQueryResultVO shopBaseQueryResult = this.baseMapper.selectByShopId2(param2.getShopId());
            if(shopBaseQueryResult==null){
                shopBaseQueryResult = baseMapper.selectBySupplierShopId(param2.getShopId());
            }
            param2.setDistrictId(shopBaseQueryResult.getDistrictId());
            param2.setShopName(shopBaseQueryResult.getShopName());
            if (shopBaseQueryResult.getShopSelfSupport() == "false") {
                param2.setShopSelfSupport(0);
            } else {
                param2.setShopSelfSupport(1);
            }

            // 判断商品类型，设置商品的销售类型等属性
            // 判断主图类型
            if (StringUtils.isNotEmpty(param2.getCommonVideo())) {
                param2.setType(2);
            }
            param2.setShopStatus(shopBaseQueryResult.getShopStatus());
            param2.setCommonSpecValue("");

            // 处理商品的规格参数，并序列化保存
            // 将规格 和 规格值保存到数据库 规格跟着商品走
            if (null != param2.getSpecParam()) {
                param2.setSpecParamJson(JSON.toJSONString(param2.getSpecParam()));
            }

            if (null != userPermissionsResult.getShopType()) {
                if (userPermissionsResult.getShopType().equals(CommonType.ShopType.SUPPLY_SHOP.getCode())) {
                    param2.setGoodsType(2);
                }
            }
            if (null == param2.getVirtualGoods()) {
                param2.setVirtualGoods(1);
            }
            if (!StringUtils.isNotBlank(param2.getPeriodOfValidity())) {
                param2.setPeriodOfValidity(null);
            }

            // 处理商品的库存分配、规格等信息
            //获取传来的门店id数组
            List<Integer> storeId = param2.getStoreId();
            //转化为字符串存储到数据库
            String storeIds = StringUtils.join(storeId, ",");
            param2.setGoodsAssignmentStore(storeIds);

            // 处理商品基础信息，并设置相关属性
            // 获取goods_base,赋值commomid
            List<ManageGdsBaseAddPublishDTO> gdsBases;
            if (null == param2.getGdsBases()) {
                gdsBases = new ArrayList<>();
            } else {
                gdsBases = param2.getGdsBases();
            }
            for (int k = 0; k < gdsBases.size(); k++) {
                ManageGdsBaseAddPublishDTO gdsBase = gdsBases.get(k);
                // 设置商品的促销提示、是否推荐等属性
                gdsBase.setGoodsPromotionTips(param2.getCommonPromotionTips());
                gdsBase.setShopId(shopId);
                gdsBase.setShopName(shopBaseQueryResult.getShopName());
                gdsBase.setGoodsIsRecommend(param2.getCommonIsRecommend());

                // 设置商品图片等信息
                if (!param2.getGoodsImages().isEmpty() && k < param2.getGoodsImages().size()
                        && !param2.getGoodsImages().get(k).getMerchantGoodsImages().isEmpty()) {
                    gdsBase.setGoodsImage(param2.getGoodsImages().get(k).getMerchantGoodsImages().get(0).getImagesImage());
                } else {
                    gdsBase.setGoodsImage(null);
                }

                // 初始化商品的一些属性，如品牌ID、库存报警等
                if (gdsBase.getBrandId() == null) {
                    gdsBase.setBrandId(0);
                }
                if (gdsBase.getGoodsAlarm() == null) {
                    gdsBase.setGoodsAlarm(0);
                }
                if (gdsBase.getGoodsMaxSale() == null) {
                    gdsBase.setGoodsMaxSale(0);
                }

                ManageGdsBaseAddParamvDTO manageGdsBaseAddParam = new ManageGdsBaseAddParamvDTO();
                manageGdsBaseAddParam.setSpecId(gdsBase.getSpecId());
                manageGdsBaseAddParam.setTypeId(gdsBase.getTypeId());
                //商品规格
                List<GoodBaseSpecValuesJsonBeanDTO> specValues;
                if (null == gdsBase.getSpecValues()) {
                    specValues = new ArrayList<>();
                } else {
                    specValues = gdsBase.getSpecValues();
                }
                String goodsSpec = null;
                for (GoodBaseSpecValuesJsonBeanDTO specValue : specValues) {
                    if (goodsSpec == null) {
                        goodsSpec = specValue.getParamValue();
                        continue;
                    }
                    goodsSpec = goodsSpec + "+" + specValue.getParamValue();
                }
                manageGdsBaseAddParam.setGoodsSpec(goodsSpec);
                System.out.println(goodsSpec);
                if (StringUtils.isNotBlank(gdsBase.getGoodsSpec())) {
                    manageGdsBaseAddParam.setGoodsName(" " + param2.getBrandName() + " " + gdsBase.getGoodsName() + " " + gdsBase.getGoodsSpec() + " ");
                } else {
                    manageGdsBaseAddParam.setGoodsName(" " + param2.getBrandName() + " " + gdsBase.getGoodsName() + " " + goodsSpec + " ");
                }
                manageGdsBaseAddParam.setShopId(gdsBase.getShopId());
                manageGdsBaseAddParam.setShopName(gdsBase.getShopName());
                manageGdsBaseAddParam.setGoodsPromotionTips(gdsBase.getGoodsPromotionTips());
                manageGdsBaseAddParam.setCatId(gdsBase.getCatId());
                manageGdsBaseAddParam.setGoodsSpecValue(gdsBase.getGoodsSpecValue());
                manageGdsBaseAddParam.setGoodsPrice(gdsBase.getGoodsPrice());
                manageGdsBaseAddParam.setGoodsMarketPrice(gdsBase.getGoodsMarketPrice());
                manageGdsBaseAddParam.setGoodsStock(gdsBase.getGoodsStock());
                //添加门店库存总和最大值
                manageGdsBaseAddParam.setGoodsShopGateStock(gdsBase.getGoodsShopGateStock());
                manageGdsBaseAddParam.setGoodsAlarm(gdsBase.getGoodsAlarm());
                manageGdsBaseAddParam.setGoodsCode(gdsBase.getGoodsCode());
                manageGdsBaseAddParam.setGoodsBarcode(gdsBase.getGoodsBarcode());
                manageGdsBaseAddParam.setGoodsIsRecommend(gdsBase.getGoodsIsRecommend());
                manageGdsBaseAddParam.setGoodsImage(gdsBase.getGoodsImage());
                manageGdsBaseAddParam.setGoodsIsShelves(gdsBase.getGoodsIsShelves());
                manageGdsBaseAddParam.setBrandId(gdsBase.getBrandId());
                manageGdsBaseAddParam.setGoodsCubage(gdsBase.getGoodsCubage());
                manageGdsBaseAddParam.setGoodsBabyPrice(gdsBase.getGoodsBabyPrice());
                manageGdsBaseAddParam.setGoodsMinSale(gdsBase.getGoodsMinSale());
                //规格跟着商品走
                manageGdsBaseAddParam.setIsUse(gdsBase.getIsUse());
                // 商家编号
                manageGdsBaseAddParam.setMerchantCode(gdsBase.getMerchantCode());
                // 商品编码
                manageGdsBaseAddParam.setCommodityBarTypeCode(gdsBase.getCommodityBarTypeCode());
                // 规格
                if (null != gdsBase.getSpecValues()) {
                    manageGdsBaseAddParam.setSpecValue(JSON.toJSONString(gdsBase.getSpecValues()));
                }
                //指导价
                manageGdsBaseAddParam.setHotPrice(gdsBase.getHotPrice());
                // 体积
                if (StringUtils.isNotEmpty(gdsBase.getGoodsVolume())) {
                    BigDecimal goodsVolume = new BigDecimal(gdsBase.getGoodsVolume());
                    manageGdsBaseAddParam.setGoodsVolume(goodsVolume);
                }
                // 商品成本价
                if (null != param2.getCommonCostPrice()) {
                    manageGdsBaseAddParam.setGoodsCostPrice(param2.getCommonCostPrice());
                }
                // 直播卖点
                if (StringUtils.isNotEmpty(param2.getSellingPoint())) {
                    manageGdsBaseAddParam.setSellingPoint(param2.getSellingPoint());
                }
                // 是否寄样
                if (null != gdsBase.getGoodsSendBack()) {
                    manageGdsBaseAddParam.setGoodsSendBack(gdsBase.getGoodsSendBack());
                }
                //限购不设置 则默认最多购买数量是商品的库存
                manageGdsBaseAddParam.setGoodsMaxSale(gdsBase.getGoodsMaxSale());
                if (null == gdsBase.getGoodsMaxSale() || 0 == gdsBase.getGoodsMaxSale()) {
                    manageGdsBaseAddParam.setGoodsMaxSale(gdsBase.getGoodsStock());
                }
                if (null != gdsBase.getCommission()) {
                    manageGdsBaseAddParam.setCommission(gdsBase.getCommission());
                }
                manageGdsBaseAddParam.setMoq(gdsBase.getMoq());
                manageGdsBaseAddParam.setTaxInclusiveAmount(gdsBase.getTaxInclusiveAmount());
                if (gdsBase.getTaxRate() != null) {
                    manageGdsBaseAddParam.setTaxRate(gdsBase.getTaxRate() * 0.01);
                }
                manageGdsBaseAddParam.setDw(param2.getDw());

                if (gdsBase.getIsEnquiry() != null && gdsBase.getIsEnquiry() == true) {
                    manageGdsBaseAddParam.setEnquiry(1);
                    manageGdsBaseAddParam.setIsEnquiry(true);
                } else {
                    manageGdsBaseAddParam.setEnquiry(0);
                    manageGdsBaseAddParam.setIsEnquiry(false);
                }

            }
            String paramString = JSON.toJSONString(param.get("draftContent"));
            if (StringUtils.isEmpty(draftName)) {
                draftName = "草稿_类目_" + param2.getCatName();
            }
            this.baseMapper.insertDraft(shopId, param2.getCatId(), draftName, paramString);

            // 返回成功结果
            return ResponseResult.success(1,BizCodeEnume.OPERATION_SUCCESSFUL.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            // 返回失败结果
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        }

    }
}
