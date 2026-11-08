package com.houjiang.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.yiyatong.*;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.vo.yiyatong.ProductSelectionCenteGoodsVO;
import com.houjiang.commons.vo.yiyatong.base.*;
import com.houjiang.goods.entity.SupplierGoodsYiyatongEntity;
import com.houjiang.goods.mapper.GoodsCommonMapper;
import com.houjiang.goods.mapper.SupplierGoodsYiyatongMapper;
import com.houjiang.goods.service.SupplierGoodsYiyatongService;
import com.houjiang.goods.service.YiYaTongService;
import com.houjiang.goods.util.HutoolHttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @ClassName YiYaTongServiceImpl
 * @Description
 * @Author Amos
 * @Date 2024/11/13 11:49
 * @Version 1.0
 * @author Amos
 */
@RefreshScope
@Service
public class YiYaTongServiceImpl implements YiYaTongService {

    private static final Logger LOGGER = LoggerFactory.getLogger(YiYaTongServiceImpl.class);


    /** sso appKey */
    @Value("${appKey}")
    private String appKey;

    @Value("${appSecret}")
    private String appSecret;

    /** 请求url */
    @Value("${sceUrl}")
    private String url;

    /** 获取省市区地址 */
    @Value("${getRegionByCodeOpenPath}")
    private String getRegionByCodeOpenPath;

    /** 获取分类信息地址 */
    @Value("${getCategorysPath}")
    private String getCategorysPath;

    /** 获取品牌列表 */
    @Value("${getBrandsPath}")
    private String getBrandsPath;

    /** 查询商品库SPUID列表 */
    @Value("${getSpuIdListPath}")
    private String getSpuIdListPath;

    /** 查询查询SPU商品详情 */
    @Value("${getSpuBySpuIdsPath}")
    private String getSpuBySpuIdsPath;

    /** 查询SKU规格信息 */
    @Value("${listSkuBySpuIdPath}")
    private String listSkuBySpuIdPath;

    /** 查询商品运费 */
    @Value("${calculatePath}")
    private String calculatePath;

    /** 查询SKU销售价 */
    @Value("${findSkuSalePricePath}")
    private String findSkuSalePricePath;

    /** 查询商品库存 */
    @Value("${findSkuStockPath}")
    private String findSkuStockPath;

    /** 修改订单收货地址 */
    @Value("${updateOrderReceiveAddressPath}")
    private String updateOrderReceiveAddressPath;

    /** 修改订单收货地址 */
    @Value("${checkSkuStockPath}")
    private String checkSkuStockPath;

    /** 创建订单 */
    @Value("${submitOrderPath}")
    private String submitOrderPath;

    /** 根据外部订单号查询订单详情列表 */
    @Value("${getOrderDetailListPath}")
    private String getOrderDetailListPath;

    /** 查询订单详情 */
    @Value("${getOrderDetailPath}")
    private String getOrderDetailPath;

    /** 查询订单详情 */
    @Value("${getOrderExpressListByOsPath}")
    private String getOrderExpressListByOsPath;


    /** 确认收货 */
    @Value("${confirmReceivePath}")
    private String confirmReceivePath;

    /** 售后申请 */
    @Value("${applyRefundPath}")
    private String applyRefundPath;

    /** 退货/换货邮寄卖家 */
    @Value("${sendReturnGoodsPath}")
    private String sendReturnGoodsPath;

    /** 换货确认收货 */
    @Value("${receiptRefundPath}")
    private String receiptRefundPath;

    /** 取消售后申请 */
    @Value("${cancelRefundPath}")
    private String cancelRefundPath;

    /** 查询售后单详细信息 */
    @Value("${orderReturnGoodsDetailPath}")
    private String orderReturnGoodsDetailPath;

    @Autowired
    private GoodsCommonMapper merchantGoodsCommonMapper;
    @Autowired
    private SupplierGoodsYiyatongService supplierGoodsYiyatongService;

    @Autowired
    private SupplierGoodsYiyatongMapper supplierGoodsYiyatongMapper;
    @Autowired
    private GoodsCommonMapper goodsCommonMapper;
    @Override
    public ResponseResult synchronousAddress(RegionByCodeOpenDTO regionByCodeOpenDTO) {
        //省
        RegionByCodeOpenDTO regionByCodeOpenDTO1 = new RegionByCodeOpenDTO();
        List<RegionVO> list = getRegionByCodeOpenList(regionByCodeOpenDTO1);
        for (RegionVO vo : list) {
            String name = null;
            if (vo.getName().contains("市")) {
                name = vo.getName().replace("市", "");
            } else if (vo.getName().equals("台湾省")) {
                name = "中国台湾";
            } else if (vo.getName().equals("澳门特别行政区")) {
                name = "中国澳门";
            } else if (vo.getName().equals("香港特别行政区")) {
                name = "中国香港";
            } else {
                name = vo.getName();
            }
            String districtId = merchantGoodsCommonMapper.selectDistrictByName(name);
            if (null != districtId) {
                merchantGoodsCommonMapper.setDistrictCodeByDistrictId(districtId, vo.getRegionCode());
            }
            //市
            RegionByCodeOpenDTO regionByCodeOpenDTO2 = new RegionByCodeOpenDTO();
            regionByCodeOpenDTO2.setRegionCode(vo.getRegionCode());
            List<RegionVO> list2 = getRegionByCodeOpenList(regionByCodeOpenDTO2);
            for (RegionVO vo2 : list2) {
                if (vo2.getName().contains("城区")) {
                    RegionByCodeOpenDTO regionByCodeOpenDTO21 = new RegionByCodeOpenDTO();
                    regionByCodeOpenDTO21.setRegionCode(vo2.getRegionCode());
                    List<RegionVO> list21 = getRegionByCodeOpenList(regionByCodeOpenDTO21);
                    for (RegionVO vo21 : list21) {
                        String districtId2 = merchantGoodsCommonMapper.selectDistrictByName1(vo21.getName(), districtId);
                        if (null != districtId2) {
                            merchantGoodsCommonMapper.setDistrictCodeByDistrictId(districtId2, vo21.getRegionCode());
                        }
                        //县
                        RegionByCodeOpenDTO regionByCodeOpenDTO3 = new RegionByCodeOpenDTO();
                        regionByCodeOpenDTO3.setRegionCode(vo21.getRegionCode());
                        List<RegionVO> list3 = getRegionByCodeOpenList(regionByCodeOpenDTO3);
                        for (RegionVO vo3 : list3) {
                            String districtId3 = merchantGoodsCommonMapper.selectDistrictByName1(vo3.getName(), districtId2);
                            if (null != districtId3) {
                                merchantGoodsCommonMapper.setDistrictCodeByDistrictId(districtId3, vo3.getRegionCode());
                            }
                        }
                    }

                } else if (vo2.getName().contains("郊县")) {
                    RegionByCodeOpenDTO regionByCodeOpenDTO21 = new RegionByCodeOpenDTO();
                    regionByCodeOpenDTO21.setRegionCode(vo2.getRegionCode());
                    List<RegionVO> list21 = getRegionByCodeOpenList(regionByCodeOpenDTO21);
                    for (RegionVO vo21 : list21) {
                        String districtId2 = merchantGoodsCommonMapper.selectDistrictByName1(vo21.getName(), districtId);
                        if (null != districtId2) {
                            merchantGoodsCommonMapper.setDistrictCodeByDistrictId(districtId2, vo21.getRegionCode());
                        }
                        //县
                        RegionByCodeOpenDTO regionByCodeOpenDTO3 = new RegionByCodeOpenDTO();
                        regionByCodeOpenDTO3.setRegionCode(vo21.getRegionCode());
                        List<RegionVO> list3 = getRegionByCodeOpenList(regionByCodeOpenDTO3);
                        for (RegionVO vo3 : list3) {
                            String districtId3 = merchantGoodsCommonMapper.selectDistrictByName1(vo3.getName(), districtId2);
                            if (null != districtId3) {
                                merchantGoodsCommonMapper.setDistrictCodeByDistrictId(districtId3, vo3.getRegionCode());
                            }
                        }
                    }
                } else {
                    String districtId2 = merchantGoodsCommonMapper.selectDistrictByName1(vo2.getName(), districtId);
                    if (null != districtId2) {
                        merchantGoodsCommonMapper.setDistrictCodeByDistrictId(districtId2, vo2.getRegionCode());
                    }
                    //县
                    RegionByCodeOpenDTO regionByCodeOpenDTO3 = new RegionByCodeOpenDTO();
                    regionByCodeOpenDTO3.setRegionCode(vo2.getRegionCode());
                    List<RegionVO> list3 = getRegionByCodeOpenList(regionByCodeOpenDTO3);
                    for (RegionVO vo3 : list3) {
                        String districtId3 = merchantGoodsCommonMapper.selectDistrictByName1(vo3.getName(), districtId2);
                        if (null != districtId3) {
                            merchantGoodsCommonMapper.setDistrictCodeByDistrictId(districtId3, vo3.getRegionCode());
                        }
                    }
                }
            }
        }


        return ResponseResult.success();
    }

    public List<RegionVO> getRegionByCodeOpenList(RegionByCodeOpenDTO regionByCodeOpenDTO) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", getRegionByCodeOpenPath);
        Map<String, String> param = Maps.newHashMapWithExpectedSize(3);
        param.put("regionCode", regionByCodeOpenDTO.getRegionCode());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);

        JSONObject jsonObject = JSONObject.parseObject(execute);
        LOGGER.warn(jsonObject.toJSONString());
        JSONArray jsonOrgData = JSONObject.parseArray(jsonObject.getString("data"));

        //将区域集合数据转换为List集合
        List<RegionVO> list = jsonOrgData.toJavaList(RegionVO.class);
        return list;
    }


    @Override
    public ResponseResult getRegionByCodeOpen(RegionByCodeOpenDTO regionByCodeOpenDTO) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", getRegionByCodeOpenPath);
        Map<String, String> param = Maps.newHashMapWithExpectedSize(3);
        param.put("regionCode", regionByCodeOpenDTO.getRegionCode());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        LOGGER.warn(jsonObject.toJSONString());
        JSONArray jsonOrgData = JSONObject.parseArray(jsonObject.getString("data"));

        //将区域集合数据转换为List集合
        List<RegionVO> list = jsonOrgData.toJavaList(RegionVO.class);
        return ResponseResult.success(list);
    }

    @Override
    public ResponseResult getCategorys(CategoryDTO categoryDTO) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", getCategorysPath);
        Map<String, String> param = Maps.newHashMapWithExpectedSize(3);
        param.put("id", categoryDTO.getId().toString());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        LOGGER.warn("getCategorys:--jsonObject--" + jsonObject.toJSONString());
        JSONArray jsonOrgData = JSONObject.parseArray(jsonObject.getString("data"));
        return ResponseResult.success(jsonOrgData);
    }

    @Override
    public ResponseResult getBrands(BrandsDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", getBrandsPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("dto", dto);
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);

        JSONObject jsonOrgData = JSONObject.parseObject(jsonObject.getString("data"));
        BrandVO vo = JSON.parseObject(jsonOrgData.toString(), BrandVO.class);
        //封装分页参数
        IPage<BrandInfoVO> page = new Page<>();
        page.setRecords(vo.getRecords());
        page.setTotal(vo.getTotal());
        page.setPages(vo.getPages());
        page.setSize(vo.getSize());
        page.setCurrent(vo.getCurrent());


        return ResponseResult.success(PageUtils.getPage(page));
    }

    @Override
    public ResponseResult getSpuIdList(SpuPageQueryDTO spuPageQueryDTO) {
        ResponseResult rr = ParseTokenUtils.getShopUserInfo(spuPageQueryDTO.getToken());
        // 如果获取用户信息失败，则直接返回错误信息
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        UserInfoVO ui = JSON.parseObject(JSON.toJSONString(rr.getData()), UserInfoVO.class);
        Integer shopId = Integer.valueOf(ui.getShopId());
        //spuPageQueryDTO.setPageIndex(spuPageQueryDTO.getPage().toString());
        //spuPageQueryDTO.setPageSize(spuPageQueryDTO.getLimit().toString());
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", getSpuIdListPath);
        try {
            Map<String, Object> param = ParseMapUtils.beanToMap(spuPageQueryDTO);
            mapParam.put("param", param);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);

        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        JSONObject jsonData = JSONObject.parseObject(jsonObject.getString("data"));
        LOGGER.error("---jsonObject--" + jsonObject);
        SpuPageVO dto = JSON.parseObject(jsonData.toString(), SpuPageVO.class);
        //商品集合
        List<ProductSelectionCenteGoodsVO> goodsVOList = new ArrayList<>();
        if (!dto.getSpuIdList().isEmpty()) {
            //spuId列表集合
            LOGGER.warn("SPUID::" + dto.getSpuIdList());
            for (String goodsId : dto.getSpuIdList()) {
                //封装查询SPU商品详情请求参数
                List<String> goodsInfo = new ArrayList<>();
                //spuId
                goodsInfo.add(goodsId);
                OpenSpuInfoQueryDTO goodsDto = new OpenSpuInfoQueryDTO();
                goodsDto.setSpuIdList(goodsInfo);
                //根据spuId查询商品详情即Spu的商品信息
                ResponseResult result = getSpuBySpuIdsGoodsList(goodsDto);
                //转换为VO
                ProductSelectionCenteGoodsVO vo = JSON.parseObject(JSON.toJSONString(result.getData()), ProductSelectionCenteGoodsVO.class);

                LambdaQueryWrapper<SupplierGoodsYiyatongEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(SupplierGoodsYiyatongEntity::getSpuId, goodsId);
                lambdaQueryWrapper.eq(SupplierGoodsYiyatongEntity::getIsDelete, 0);
                lambdaQueryWrapper.eq(SupplierGoodsYiyatongEntity::getShopId, shopId);
                List<SupplierGoodsYiyatongEntity> commonEntities = supplierGoodsYiyatongService.list(lambdaQueryWrapper);
                if (!commonEntities.isEmpty()) {
                    vo.setDropShippingStart(true);
                }
                if (null != spuPageQueryDTO.getDropShipping()) {
                    //未代发
                    if (spuPageQueryDTO.getDropShipping() == 0 && commonEntities.isEmpty()) {
                        goodsVOList.add(vo);

                    }
                    //已代发
                    if (spuPageQueryDTO.getDropShipping() == 1 && !commonEntities.isEmpty()) {
                        goodsVOList.add(vo);

                    }
                } else {
                    goodsVOList.add(vo);
                }
            }
        }

        if (!goodsVOList.isEmpty()) {
            if (spuPageQueryDTO.getProfitMarginSorting() != null) {
                if (spuPageQueryDTO.getProfitMarginSorting() == 1) {
                    goodsVOList.sort(Comparator.comparing(o -> new BigDecimal(o.getProfitMargin())));

                }
                if (spuPageQueryDTO.getProfitMarginSorting() == 2) {
                    goodsVOList.sort((o1, o2) -> new BigDecimal(o2.getProfitMargin()).compareTo(new BigDecimal(o1.getProfitMargin())));

                }
            }
            if (spuPageQueryDTO.getAgreementPriceSorting() != null) {
                if (spuPageQueryDTO.getAgreementPriceSorting() == 1) {
                    goodsVOList.sort(Comparator.comparing(ProductSelectionCenteGoodsVO::getAgreementPrice));

                }
                if (spuPageQueryDTO.getAgreementPriceSorting() == 2) {
                    goodsVOList.sort((o1, o2) -> o2.getAgreementPrice().compareTo(o1.getAgreementPrice()));

                }
            }
        }
        Integer dropShippingNum = goodsCommonMapper.queryDropShippingNum(shopId, CommonType.SupplierSource.YI_YA_TONG.getCode());
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("totalCount", dto.getTotal());
        stringObjectMap.put("list", goodsVOList);
        stringObjectMap.put("dropShippingNum", dropShippingNum);
        return ResponseResult.success(stringObjectMap);
    }

    /**
     * 查询SPU商品详情并封装
     *
     * @param dto
     */
    @Override
    public ResponseResult getSpuBySpuIdsGoodsList(OpenSpuInfoQueryDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", getSpuBySpuIdsPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("spuIdList", dto.getSpuIdList());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        JSONArray jsonOrgData = JSONObject.parseArray(jsonObject.getString("data"));
        //将SPU商品详情集合数据转换为List集合
        List<OpenSpuInfoQueryVO> list = jsonOrgData.toJavaList(OpenSpuInfoQueryVO.class);
        //获取第一个
        OpenSpuInfoQueryVO vo = list.get(0);
        //封装商品信息
        ProductSelectionCenteGoodsVO goodsVO = new ProductSelectionCenteGoodsVO();
        //spuId
        goodsVO.setSupplierCommonId(vo.getSpuId());
        //商品名
        goodsVO.setGoodsName(vo.getName());
        //商品图片
        goodsVO.setGoodsImage(vo.getDetailImgList().get(0));
        //类目属性/属性值
        goodsVO.setPropertys(vo.getPropertys());

        StringBuilder sb = new StringBuilder();
        for (String str : vo.getDetailImgList()) {
            String header = "<p><img class=\"wscnph\" style=\"max-width: 100%;\" src=\"";
            header = header + str;
            String bottom = "\" /></p>";
            header = header + bottom;
            sb.append(header);
        }
        goodsVO.setCommonBody(sb.toString());
        //分类id
        if (null != vo.getCategoryId3()) {
            goodsVO.setCatId(vo.getCategoryId3());
        } else if (null == vo.getCategoryId3() && null != vo.getCategoryId2()) {
            goodsVO.setCatId(vo.getCategoryId2());
        } else if (null == vo.getCategoryId2() && null != vo.getCategoryId1()) {
            goodsVO.setCatId(vo.getCategoryId1());
        }
        //商品重量
        if (null != vo.getWeight()) {
            goodsVO.setCommonCubage(new BigDecimal(vo.getWeight()));
        } else {
            goodsVO.setCommonCubage(new BigDecimal("1.00"));
        }

        //封装查询SKU规格信息请求参数
        OpenSkuInfoBySpuIdDTO spuIdDTO = new OpenSkuInfoBySpuIdDTO();
        spuIdDTO.setSpuId(vo.getSpuId());
        //请求查询SKU规格信息解决
        ResponseResult result = getSkuBySpuId(spuIdDTO);
        OpenSkuInfoBySpuIdVO skuVo = JSON.parseObject(JSON.toJSONString(result.getData()), OpenSkuInfoBySpuIdVO.class);
        //skuId
        goodsVO.setSupplierGoodsId(skuVo.getSkuId());
        //协议价/经销价
        goodsVO.setAgreementPrice(new BigDecimal(skuVo.getBasePrice()));
        //品牌名
        goodsVO.setBrandName(vo.getBrandName());
        //SKU起订量
        goodsVO.setMoq(skuVo.getBuyStartQty());
        //sku库存
        goodsVO.setMinOrder(Integer.valueOf(skuVo.getStock()));
        //SPU总库存
        goodsVO.setGoodsStock(vo.getInventory());
        //建议零售价-采购价
        BigDecimal a = new BigDecimal(skuVo.getSuggestPrice()).subtract(new BigDecimal(skuVo.getBasePrice()));
        //（建议零售价-采购价）/采购价*100%
        a = a.divide(new BigDecimal(skuVo.getSuggestPrice()).multiply(new BigDecimal("0.01")), 2, RoundingMode.HALF_UP);
        goodsVO.setProfitMargin(a.toString());

        //规格
        if (skuVo.getSkuPropertyList().size() > 0) {
            goodsVO.setGoodsSpec(skuVo.getSkuPropertyList().get(0).getSpecValueName());
        }

        return ResponseResult.success(goodsVO);
    }


    /**
     * 查询SPU商品详情
     *
     * @param dto
     */
    @Override
    public ResponseResult getSpuBySpuIdsPath(OpenSpuInfoQueryDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", getSpuBySpuIdsPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("spuIdList", dto.getSpuIdList());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        JSONArray jsonOrgData = JSONObject.parseArray(jsonObject.getString("data"));
        //将SPU商品详情集合数据转换为List集合
        List<OpenSpuInfoQueryVO> list = jsonOrgData.toJavaList(OpenSpuInfoQueryVO.class);
        for (OpenSpuInfoQueryVO vo : list) {
            //封装查询SKU规格信息请求参数
            OpenSkuInfoBySpuIdDTO spuIdDTO = new OpenSkuInfoBySpuIdDTO();
            spuIdDTO.setSpuId(vo.getSpuId());
            //请求查询SKU规格信息解决
            ResponseResult skuResult = listAllSkuBySpuId(spuIdDTO);

            List<OpenSkuInfoBySpuIdVO> skuList = JSON.parseArray(JSON.toJSONString(skuResult.getData()), OpenSkuInfoBySpuIdVO.class);
            vo.setSkuList(skuList);
        }


        return ResponseResult.success(list);
    }

    /**
     * 查询SKU规格信息
     *
     * @param dto
     */
    @Override
    public ResponseResult getSkuBySpuId(OpenSkuInfoBySpuIdDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", listSkuBySpuIdPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("spuId", dto.getSpuId());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        JSONArray jsonOrgData = JSONObject.parseArray(jsonObject.getString("data"));

        //将SKU规格信息集合数据转换为List集合
        List<OpenSkuInfoBySpuIdVO> list = jsonOrgData.toJavaList(OpenSkuInfoBySpuIdVO.class);
        //获取第一个
        OpenSkuInfoBySpuIdVO skuVo = list.get(0);

        return ResponseResult.success(skuVo);
    }

    /**
     * 查询SKU规格信息
     *
     * @param dto
     */
    @Override
    public ResponseResult listSkuBySpuId(OpenSkuInfoBySpuIdDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", listSkuBySpuIdPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("spuId", dto.getSpuId());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        JSONArray jsonOrgData = JSONObject.parseArray(jsonObject.getString("data"));

        //将SKU规格信息集合数据转换为List集合
        List<OpenSkuInfoBySpuIdVO> list = jsonOrgData.toJavaList(OpenSkuInfoBySpuIdVO.class);


        return ResponseResult.success(list);
    }

    @Override
    public ResponseResult calculate(FreightCalculateDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", calculatePath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("spuInfoList", dto.getSpuInfoList());
        param.put("cityId", dto.getCityId());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        String data = jsonObject.getString("data");

        return ResponseResult.success(data);
    }

    @Override
    public ResponseResult findSkuSalePrice(Map<String, Object> map) {
        String skuIds = map.get("skuIds").toString();
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", findSkuSalePricePath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("skuIds", skuIds);
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        JSONArray jsonOrgData = JSONObject.parseArray(jsonObject.getString("data"));
        //
        List<OfficialDistriPriceVO> list = jsonOrgData.toJavaList(OfficialDistriPriceVO.class);


        return ResponseResult.success(list);
    }

    @Override
    public ResponseResult findSkuStock(OpenFindSkuStockDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", findSkuStockPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("distriId", dto.getDistriId());
        param.put("skuIdList", dto.getSkuIdList());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        JSONArray jsonOrgData = JSONObject.parseArray(jsonObject.getString("data"));
        //
        List<OpenFindSkuStockVO> list = jsonOrgData.toJavaList(OpenFindSkuStockVO.class);


        return ResponseResult.success(list);
    }

    @Override
    public ResponseResult updateOrderReceiveAddress(UpdateOrderReceiveAddressDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", updateOrderReceiveAddressPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("shipMobile", dto.getShipMobile());
        param.put("orderSn", dto.getOrderSn());
        param.put("shipName", dto.getShipName());
        param.put("shipZipCode", dto.getShipZipCode());
        param.put("shipAddress", dto.getShipAddress());
        param.put("shipAreaCode", dto.getShipAreaCode());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);

        return ResponseResult.success(jsonObject.getString("data"));
    }

    @Override
    public ResponseResult checkSkuStock(StockQueryDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", checkSkuStockPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("itemDTOList", dto.getItemDTOList());
        param.put("areaRegionCode", dto.getAreaRegionCode());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);

        SkuStockVO vo = JSONObject.parseObject(jsonObject.getString("data"), SkuStockVO.class);


        return ResponseResult.success(vo);
    }

    @Override
    public ResponseResult submitOrder(SubmitOrderDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", submitOrderPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("orderSource", dto.getOrderSource());
        param.put("goodsList", dto.getGoodsList());
        param.put("buyerName", dto.getBuyerName());
        param.put("shipName", dto.getShipName());
        param.put("shipAddress", dto.getShipAddress());
        param.put("shipPhone", dto.getShipPhone());
        param.put("shipMobile", dto.getShipMobile());
        param.put("taxPayer", dto.getTaxPayer());
        param.put("outOrderSn", dto.getOutOrderSn());
        param.put("invoiceType", dto.getInvoiceType());
        param.put("invoice", dto.getInvoice());
        param.put("shipZipCode", dto.getShipZipCode());
        param.put("buyerMemo", dto.getBuyerMemo());
        param.put("shipAreaCode", dto.getShipAreaCode());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);

        String success = jsonObject.getString("success");
        if (success.equals("false")) {
            String msg = jsonObject.getString("msg");
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), msg);
        }
        JSONObject dataObject = jsonObject.getJSONObject("data");
        // 获取 orderList 数组
        JSONArray orderListArray = dataObject.getJSONArray("orderList");
        //返回订单列表集合数据转换为List集合
        List<SubmitOrderVO> list = orderListArray.toJavaList(SubmitOrderVO.class);


        return ResponseResult.success(list);
    }

    @Override
    public ResponseResult getOrderDetailList(OrderDetailListDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", getOrderDetailListPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("thirdOrderSn", dto.getThirdOrderSn());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        JSONArray jsonOrgData = JSONObject.parseArray(jsonObject.getString("data"));
        //返回订单详情列表集合数据转换为List集合
        List<ResellerOrderDetailVO> list = jsonOrgData.toJavaList(ResellerOrderDetailVO.class);


        return ResponseResult.success(list);
    }

    @Override
    public ResponseResult getOrderDetail(OrderDetailDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", getOrderDetailPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("orderSn", dto.getOrderSn());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        ResellerOrderDetailVO vo = JSONObject.parseObject(jsonObject.getString("data"), ResellerOrderDetailVO.class);

        return ResponseResult.success(vo);
    }

    @Override
    public ResponseResult getOrderExpressListByOs(OrderExpressListByOsDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", getOrderExpressListByOsPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("orderSn", dto.getOrderSn());
        param.put("deliverySn", dto.getDeliverySn());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        JSONArray jsonOrgData = JSONObject.parseArray(jsonObject.getString("data"));
        //返回订单详情列表集合数据转换为List集合
        List<OrderExpressVO> list = jsonOrgData.toJavaList(OrderExpressVO.class);

        return ResponseResult.success(list);
    }

    @Override
    public ResponseResult confirmReceive(ConfirmReceiveDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", confirmReceivePath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("orderSn", dto.getOrderSn());
        param.put("operator", dto.getOperator());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);


        return ResponseResult.success(jsonObject.getString("data"));
    }

    @Override
    public ResponseResult applyRefund(ApplyRefundDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", applyRefundPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("reason", dto.getOrderSn());
        param.put("applyWay", dto.getApplyWay());
        param.put("orderSn", dto.getOrderSn());
        param.put("refundReasonId", dto.getRefundReasonId());
        param.put("goodsList", dto.getGoodsList());
        param.put("memo", dto.getMemo());
        param.put("pictureurlList", dto.getPictureurlList());
        param.put("returnType", dto.getReturnType());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        String success = jsonObject.getString("success");
        if (success.equals("false")) {
            String msg = jsonObject.getString("msg");
            String status = jsonObject.getString("status");
            if (status.equals("513") && msg.equals("已有一个正在售后的订单")) {
                String returnTime = merchantGoodsCommonMapper.selectReturnTime(dto.getOrderSn());
                if (null != returnTime) {
                    String time = DateUtils.getDatePoor(DateUtils.addHours(DateUtils.parseCNDate(returnTime), 24), new Date());
                    return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "售后申请不可连续提交，请在" + time + "后提交");
                } else {
                    return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "售后申请不可连续提交，请在1小时2分钟后再次提交");
                }

            } else {
                return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), msg);
            }

        }
        OrderReturnGoodsVO vo = JSONObject.parseObject(jsonObject.getString("data"), OrderReturnGoodsVO.class);


        return ResponseResult.success(vo);
    }

    @Override
    public ResponseResult sendReturnGoods(SendReturnGoodsDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", sendReturnGoodsPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("deliveryCorpSn", dto.getDeliveryCorpSn());
        param.put("deliverySn", dto.getDeliverySn());
        param.put("deliveryCorpName", dto.getDeliveryCorpName());
        param.put("returnSn", dto.getReturnSn());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        String success = jsonObject.getString("success");
        if (success.equals("false")) {
            String msg = jsonObject.getString("msg");
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), msg);
        }
        OrderReturnGoodsVO vo = JSONObject.parseObject(jsonObject.getString("data"), OrderReturnGoodsVO.class);
        return ResponseResult.success(vo);
    }

    @Override
    public ResponseResult receiptRefund(ReceiptRefundDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", receiptRefundPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("returnSn", dto.getReturnSn());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        OrderReturnGoodsVO vo = JSONObject.parseObject(jsonObject.getString("data"), OrderReturnGoodsVO.class);
        return ResponseResult.success(vo);
    }

    @Override
    public ResponseResult cancelRefund(CancelRefundDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", cancelRefundPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("returnSn", dto.getReturnSn());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        String success = jsonObject.getString("success");
        if (success.equals("false")) {
            String msg = jsonObject.getString("msg");
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), msg);
        }
        OrderReturnGoodsVO vo = JSONObject.parseObject(jsonObject.getString("data"), OrderReturnGoodsVO.class);
        return ResponseResult.success(vo);
    }

    @Override
    public ResponseResult orderReturnGoodsDetail(CancelRefundDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", orderReturnGoodsDetailPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("returnSn", dto.getReturnSn());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        String success = jsonObject.getString("success");
        if (success.equals("false")) {
            String msg = jsonObject.getString("msg");
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), msg);
        }
        OrderReturnGoodsVO vo = JSONObject.parseObject(jsonObject.getString("data"), OrderReturnGoodsVO.class);
        return ResponseResult.success(vo);
    }

    @Override
    public ResponseResult listAllSkuBySpuId(OpenSkuInfoBySpuIdDTO dto) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("url", url);
        mapParam.put("method", listSkuBySpuIdPath);
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("spuId", dto.getSpuId());
        mapParam.put("param", param);
        mapParam.put("appKey", appKey);
        mapParam.put("appSecret", appSecret);
        //获取接口响应参数
        String execute = HutoolHttpUtils.httpPost(mapParam);
        if (execute == null) {
            return ResponseResult.fail();
        }
        JSONObject jsonObject = JSONObject.parseObject(execute);
        JSONArray jsonOrgData = JSONObject.parseArray(jsonObject.getString("data"));

        //将SKU规格信息集合数据转换为List集合
        List<OpenSkuInfoBySpuIdVO> list = jsonOrgData.toJavaList(OpenSkuInfoBySpuIdVO.class);

        return ResponseResult.success(list);
    }

    /**
     * 查询怡亚通商品skuId
     * @param goodsId
     * @return
     */
    @Override
    public String queryYiYaTongSkuId(Integer goodsId) {
        String skuId = supplierGoodsYiyatongMapper.queryYiYaTongSkuId(goodsId);
        return skuId;
    }
}
