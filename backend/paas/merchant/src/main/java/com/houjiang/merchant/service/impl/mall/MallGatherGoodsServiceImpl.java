package com.houjiang.merchant.service.impl.mall;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallGather;
import com.houjiang.merchant.common.entity.mall.MallGatherGoods;
import com.houjiang.merchant.common.entity.mall.MallGoods;
import com.houjiang.merchant.common.entity.mall.MallShopJob;
import com.houjiang.merchant.common.enums.AlbumEnum;
import com.houjiang.merchant.common.enums.FileEnum;
import com.houjiang.merchant.common.enums.GatherEnum;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallGatherGoodsMapper;
import com.houjiang.merchant.common.mapper.mall.MallGatherMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.ToolUtils;
import com.houjiang.merchant.common.util.YmlUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.IFileService;
import com.houjiang.merchant.service.mall.IMallGatherGoodsService;
import com.houjiang.merchant.service.mall.IMallGoodsService;
import com.houjiang.merchant.validate.gather.MallGatherEditGoodsValidate;
import com.houjiang.merchant.validate.gather.MallGatherSaveGoodsValidate;
import com.houjiang.merchant.validate.mall.MallGoodsCreateValidate;
import com.houjiang.merchant.vo.gather.MallGatherGoodsDetailVo;
import com.houjiang.merchant.vo.gather.MallGatherGoodsListedVo;
import com.houjiang.merchant.vo.mall.MallGoodsSkuVo;
import com.houjiang.merchant.vo.mall.MallShopJobDetailVo;
import com.houjiang.merchant.vo.mall.SkuNameListVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品采集实现类
 * @author Amos
 */
@Service
public class MallGatherGoodsServiceImpl implements IMallGatherGoodsService {
    @Resource
    MallGatherGoodsMapper mallGatherGoodsMapper;
    @Resource
    IMallGoodsService iMallGoodsService;
    @Resource
    IFileService fileService;
    @Override
    public List<MallGatherGoodsListedVo> goodsDealNo(Integer gatherId) {
        List<MallGatherGoods> list = mallGatherGoodsMapper.selectList(new QueryWrapper<MallGatherGoods>().eq("gather_id", gatherId).eq("delete_time", 0).eq("deal", YesNoEnum.NO.getCode()));
        List<MallGatherGoodsListedVo> ret = new ArrayList<MallGatherGoodsListedVo>();
        if (list.size() > 0) {
            for (MallGatherGoods item : list) {
                MallGatherGoodsListedVo vo = new MallGatherGoodsListedVo();
                BeanUtils.copyProperties(item, vo);
                vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
                vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
                vo.setGather(JSON.parse(item.getGather()));
                vo.setContent(JSON.parse(item.getContent()));
                vo.setChannelName(GatherEnum.getMsgByCode(item.getChannel()));
                ret.add(vo);
            }
        }
        return ret;
    }

    @Override
    public List<MallGatherGoodsListedVo> goodsDealYes(Integer gatherId) {
        List<MallGatherGoods> list = mallGatherGoodsMapper.selectList(new QueryWrapper<MallGatherGoods>().eq("gather_id", gatherId).eq("delete_time", 0).eq("deal", YesNoEnum.YES.getCode()));
        List<MallGatherGoodsListedVo> ret = new ArrayList<MallGatherGoodsListedVo>();
        if (list.size() > 0) {
            for (MallGatherGoods item : list) {
                MallGatherGoodsListedVo vo = new MallGatherGoodsListedVo();
                BeanUtils.copyProperties(item, vo);
                vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
                vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
                vo.setGather(JSON.parse(item.getGather()));
                vo.setContent(JSON.parse(item.getContent()));
                vo.setChannelName(GatherEnum.getMsgByCode(item.getChannel()));
                ret.add(vo);
            }
        }
        return ret;
    }

    @Override
    public MallGatherGoodsDetailVo detail(Integer id) {
        MallGatherGoods model = mallGatherGoodsMapper.selectOne(
                new QueryWrapper<MallGatherGoods>()
                        .eq("id", id).eq("delete_time", 0)
                        .last("limit 1"));

        Assert.notNull(model, "数据不存在");

        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能查看其他店铺的店铺采集信息");
        }

        MallGatherGoodsDetailVo vo = new MallGatherGoodsDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setCreateTime(TimeUtils.timestampToDate(model.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(model.getUpdateTime()));
        vo.setGather(JSONObject.parseObject(model.getGather()));
        vo.setContent(JSONObject.parseObject(model.getContent()));
        vo.setChannelName(GatherEnum.getMsgByCode(model.getChannel()));
        return vo;
    }

    @Override
    @Transactional
    public void editGoods(MallGatherEditGoodsValidate editGoodsValidate) {
        MallGatherGoods model = mallGatherGoodsMapper.selectOne(
                new QueryWrapper<MallGatherGoods>()
                        .eq("id", editGoodsValidate.getId()).eq("delete_time", 0)
                        .last("limit 1"));

        Assert.notNull(model, "数据不存在");

        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能编辑其他店铺的店铺采集信息");
        }

        model.setContent(JSONObject.toJSONString(editGoodsValidate.getContent()));
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        mallGatherGoodsMapper.updateById(model);
    }

    @Override
    @Transactional
    public void saveGoods(MallGatherSaveGoodsValidate saveValidate) throws IOException {
        if (saveValidate.getIds().size() > 0) {
            String path = "image/" + TimeUtils.timestampToDate(System.currentTimeMillis() / 1000, "yyyyMMdd") + "/";
            String fullPath = YmlUtils.get("merchant.upload-directory") + "/" + path;
            for (Integer id : saveValidate.getIds()) {
                MallGatherGoods model = mallGatherGoodsMapper.selectOne(
                        new QueryWrapper<MallGatherGoods>()
                                .eq("id", id).eq("delete_time", 0)
                                .last("limit 1"));

                Assert.notNull(model, "数据不存在");

                if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
                    throw new OperateException("不能编辑其他店铺的店铺采集信息");
                }

                if (model.getDeal().equals(YesNoEnum.YES.getCode())) {
                    throw new OperateException("已处理，无需重复操作");
                }

                JSONObject content = JSONObject.parseObject(model.getContent());
                content.put("status", saveValidate.getGoodsStatus());
                MallGoodsCreateValidate createMallGoods = new MallGoodsCreateValidate();
                createMallGoods.setName(content.getString("name"));
                createMallGoods.setCode(content.getString("code"));
                createMallGoods.setDescript(content.getString("descript"));
                createMallGoods.setShopId(saveValidate.getShopId());
                createMallGoods.setCategoryIds(content.getList("category_ids", Integer.class));
                createMallGoods.setShopCategoryId(content.getInteger("shop_category_id"));
                createMallGoods.setVideoIs(content.getInteger("video_is"));
                createMallGoods.setSkuType(content.getInteger("sku_type"));
                createMallGoods.setExpressType(content.getInteger("express_type"));
                createMallGoods.setStockWarning(content.getInteger("stock_warning"));
                createMallGoods.setSort(content.getInteger("sort"));
                createMallGoods.setStatus(content.getInteger("status"));
                createMallGoods.setSkuNameList(content.getList("sku_name_list", SkuNameListVo.class));
                createMallGoods.setContent(content.getString("content"));
                createMallGoods.setSku(content.getList("sku", MallGoodsSkuVo.class));
                createMallGoods.setGoodsImage(content.getList("goods_image", String.class));
                if (createMallGoods.getSku().size() == 0) {
                    List<MallGoodsSkuVo> skuVos = new ArrayList<MallGoodsSkuVo>();
                    skuVos.add(new MallGoodsSkuVo());
                    createMallGoods.setSku(skuVos);
                }
                //处理轮播图
                if (createMallGoods.getGoodsImage().size() > 0) {
                    List<String> goodsImage = new ArrayList<String>();
                    for (String val : createMallGoods.getGoodsImage()) {
                        if (StringUtils.isNotEmpty(val)) {
                            val = ToolUtils.checkHttp(val);
                            String imageName = ToolUtils.makeMd5(val) + ".jpg";
                            ToolUtils.downloadImage(imageName, val, fullPath);
                            Map<String, String> album = new LinkedHashMap<>();
                            album.put("aid", String.valueOf(MerchantThreadLocal.getAdminId()));
                            album.put("cid", "0");
                            album.put("type", String.valueOf(AlbumEnum.IMAGE.getCode()));
                            album.put("url", path + imageName);
                            album.put("name", imageName);
                            album.put("source", String.valueOf(FileEnum.SOURCE_SHOP.getCode()));
                            album.put("sourceId", String.valueOf(MerchantThreadLocal.getShopId()));
                            Integer fileId = fileService.fileAdd(album);
                            val = path + imageName;
                        }
                        goodsImage.add(val);
                    }
                    createMallGoods.setGoodsImage(goodsImage);
                }
                //处理详情图片
                List<String> contentImages = ToolUtils.getContentImage(createMallGoods.getContent());
                if (contentImages.size() > 0) {
                    String goodsContent = createMallGoods.getContent();
                    for (String img : contentImages) {
                        if (StringUtils.isNotEmpty(img)) {
                            String contentImage = ToolUtils.checkHttp(img);
                            String contentImageName = ToolUtils.makeMd5(contentImage) + ".jpg";
                            ToolUtils.downloadImage(contentImageName, contentImage, fullPath);
                            goodsContent = goodsContent.replace(img, "##domain##" + path + contentImageName);
                        }
                    }
                    createMallGoods.setContent(goodsContent);
                }
                //处理规格项图片
                if (createMallGoods.getSku().size() > 0) {
                    List<MallGoodsSkuVo> goodsSku = new ArrayList<MallGoodsSkuVo>();
                    for (MallGoodsSkuVo skuVo : createMallGoods.getSku()) {
                         if (StringUtils.isNotEmpty(skuVo.getImage())) {
                             String image = ToolUtils.checkHttp(skuVo.getImage());
                             String imageName = ToolUtils.makeMd5(image) + ".jpg";
                             ToolUtils.downloadImage(imageName, image, fullPath);
                             skuVo.setImage(path + imageName);
                         }
                        goodsSku.add(skuVo);
                    }
                    createMallGoods.setSku(goodsSku);
                }
                iMallGoodsService.add(createMallGoods);

                model.setDeal(YesNoEnum.YES.getCode());
                model.setUpdateTime(System.currentTimeMillis() / 1000);
                mallGatherGoodsMapper.updateById(model);
            }
        } else {
            throw new OperateException("请提交id");
        }
    }
}
