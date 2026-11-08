package com.houjiang.goods.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.goods.mall.dto.MallGoodsAuditDTO;
import com.houjiang.goods.mall.dto.MallGoodsSearchDTO;
import com.houjiang.goods.mall.dto.MallGoodsUpdateDTO;
import com.houjiang.goods.mall.entity.GoodsCommonI18n;
import com.houjiang.goods.mall.entity.MallGoods;
import com.houjiang.goods.mall.entity.MallGoodsCategoryIndex;
import com.houjiang.goods.mall.entity.MallGoodsImage;
import com.houjiang.goods.mall.entity.MallGoodsSku;
import com.houjiang.goods.mall.entity.MallGoodsSkuName;
import com.houjiang.goods.mall.mapper.MallGoodsCategoryIndexMapper;
import com.houjiang.goods.mall.mapper.MallGoodsImageMapper;
import com.houjiang.goods.mall.mapper.MallGoodsMapper;
import com.houjiang.goods.mall.mapper.MallGoodsSkuMapper;
import com.houjiang.goods.mall.mapper.MallGoodsSkuNameMapper;
import com.houjiang.goods.mall.service.GoodsCommonI18nService;
import com.houjiang.goods.mall.service.MallGoodsService;
import com.houjiang.goods.mall.vo.MallGoodsDetailVO;
import com.houjiang.goods.mall.vo.MallGoodsListedVO;
import com.houjiang.goods.mall.vo.PageVO;
import com.houjiang.commons.dto.mall.MallSkuSnapshot;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品 Service 实现
 * 设计参考 bbc MallGoodsServiceImpl，剔除 MPJ 多表 join，使用 MyBatis-Plus + 子表分次查询
 * @author Amos
 */
@Service
public class MallGoodsServiceImpl implements MallGoodsService {

    @Override
    public MallSkuSnapshot requireSaleableSku(Long skuId) {
        if (skuId == null) {
            throw new IllegalArgumentException("SKU不能为空");
        }
        MallGoodsSku sku = skuMapper.selectById(skuId);
        MallSkuSnapshot snapshot = new MallSkuSnapshot();
        snapshot.setSkuId(skuId);
        if (sku == null || (sku.getDeleteTime() != null && sku.getDeleteTime() != 0)) {
            snapshot.setSaleable(false);
            snapshot.setInvalidReason("SKU不存在或已删除");
            return snapshot;
        }
        MallGoods goods = goodsMapper.selectById(sku.getGoodsId());
        snapshot.setGoodsId(Long.valueOf(sku.getGoodsId()));
        snapshot.setShopId(sku.getShopId() == null ? null : Long.valueOf(sku.getShopId()));
        snapshot.setSkuSpec(sku.getSkuValueArr());
        snapshot.setSalePrice(sku.getPrice());
        snapshot.setSaleableStock(sku.getStock() == null ? 0 : sku.getStock());
        if (goods != null) {
            snapshot.setGoodsName(goods.getName());
            snapshot.setGoodsImage(sku.getImage() != null && !sku.getImage().isBlank()
                    ? sku.getImage() : goods.getImage());
        }
        String invalidReason = saleInvalidReason(goods, sku);
        snapshot.setSaleable(invalidReason == null);
        snapshot.setInvalidReason(invalidReason);
        return snapshot;
    }

    private String saleInvalidReason(MallGoods goods, MallGoodsSku sku) {
        if (goods == null || (goods.getDeleteTime() != null && goods.getDeleteTime() != 0)
                || Integer.valueOf(1).equals(goods.getIsRecycle())) {
            return "商品不存在或已删除";
        }
        if (!Integer.valueOf(1).equals(goods.getStatus())) {
            return "商品已下架";
        }
        if (!Integer.valueOf(1).equals(goods.getAuditStatus())) {
            return "商品尚未审核通过";
        }
        if (sku.getPrice() == null || sku.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return "SKU售价无效";
        }
        if (sku.getStock() == null || sku.getStock() <= 0) {
            return "SKU库存不足";
        }
        return null;
    }

    @Autowired
    private MallGoodsMapper goodsMapper;
    @Autowired
    private MallGoodsImageMapper imageMapper;
    @Autowired
    private MallGoodsSkuMapper skuMapper;
    @Autowired
    private MallGoodsSkuNameMapper skuNameMapper;
    @Autowired
    private MallGoodsCategoryIndexMapper categoryIndexMapper;
    @Autowired
    private GoodsCommonI18nService goodsCommonI18nService;

    @Override
    public PageVO<MallGoodsListedVO> list(MallGoodsSearchDTO s) {
        MallGoodsSearchDTO search = s != null ? s : new MallGoodsSearchDTO();
        Map<String, Object> extend = buildTabExtend(search);

        LambdaQueryWrapper<MallGoods> qw = buildBaseQuery(search);
        applyTypeFilter(qw, search.getStatus() != null ? search.getStatus() : search.getType());
        qw.orderByDesc(MallGoods::getSort).orderByDesc(MallGoods::getId);

        Page<MallGoods> page = new Page<>(
                search.getPageNo() != null ? search.getPageNo() : 1,
                search.getPageSize() != null ? search.getPageSize() : 15);
        Page<MallGoods> res = goodsMapper.selectPage(page, qw);

        List<Integer> ids = res.getRecords().stream().map(MallGoods::getId).collect(Collectors.toList());
        Map<Integer, String> i18nNames = goodsCommonI18nService.mapPublishedNames(ids, search.getLang());

        List<MallGoodsListedVO> rows = new ArrayList<>();
        for (MallGoods g : res.getRecords()) {
            MallGoodsListedVO vo = new MallGoodsListedVO();
            BeanUtils.copyProperties(g, vo);
            String localized = i18nNames.get(g.getId());
            if (localized != null && !localized.isBlank()) {
                vo.setName(localized);
            }
            vo.setSkuTypeText(g.getSkuType() != null && g.getSkuType() == 1 ? "多规格" : "单规格");
            vo.setStatusText(g.getStatus() != null && g.getStatus() == 1 ? "已上架" : "仓库中");
            String audit = "待审核";
            if (g.getAuditStatus() != null) {
                if (g.getAuditStatus() == 1) audit = "已通过";
                else if (g.getAuditStatus() == 2) audit = "已拒绝";
            }
            vo.setAuditStatusText(audit);
            BigDecimal min = g.getMinPrice() == null ? BigDecimal.ZERO : g.getMinPrice();
            BigDecimal max = g.getMaxPrice() == null ? BigDecimal.ZERO : g.getMaxPrice();
            vo.setPriceText(min.compareTo(max) == 0 ? min.toPlainString() : min + "~" + max);
            vo.setAllSaleNum((g.getSaleNum() == null ? 0 : g.getSaleNum())
                    + (g.getVirtualSaleNum() == null ? 0 : g.getVirtualSaleNum()));
            if (g.getCategoryIds() != null && !g.getCategoryIds().isEmpty()) {
                vo.setCategoryIdsArr(Arrays.stream(g.getCategoryIds().split(","))
                        .filter(x -> !x.isEmpty()).map(Integer::valueOf).collect(Collectors.toList()));
            } else {
                vo.setCategoryIdsArr(Collections.emptyList());
            }
            // 操作按钮可用性
            vo.setCanSale(g.getAuditStatus() != null && g.getAuditStatus() == 1 ? 1 : 0);
            vo.setCanAudit(g.getAuditStatus() != null && g.getAuditStatus() == 0 ? 1 : 0);
            vo.setCanReAudit(g.getAuditStatus() != null && g.getAuditStatus() == 2 ? 1 : 0);
            vo.setCanDelete(1);
            vo.setCanRecycle(g.getIsRecycle() != null && g.getIsRecycle() == 0 ? 1 : 0);
            rows.add(vo);
        }
        return new PageVO<>(res.getCurrent(), res.getSize(), res.getTotal(), rows, extend);
    }

    private LambdaQueryWrapper<MallGoods> buildBaseQuery(MallGoodsSearchDTO s) {
        LambdaQueryWrapper<MallGoods> qw = new LambdaQueryWrapper<>();
        qw.eq(MallGoods::getDeleteTime, 0);
        qw.eq(MallGoods::getIsRecycle, 0);
        if (s.getShopId() != null) {
            qw.eq(MallGoods::getShopId, s.getShopId());
        }
        if (s.getShopCategoryId() != null) {
            qw.eq(MallGoods::getShopCategoryId, s.getShopCategoryId());
        }
        if (s.getName() != null && !s.getName().trim().isEmpty()) {
            String keyword = s.getName().trim();
            // 名称或简介命中即可（前台搜索「口红」等关键词）
            qw.and(w -> w.like(MallGoods::getName, keyword)
                    .or()
                    .like(MallGoods::getDescript, keyword));
        }
        if (s.getIsDistribution() != null) {
            qw.eq(MallGoods::getIsDistribution, s.getIsDistribution());
        }
        if (s.getCategoryId() != null && !s.getCategoryId().isEmpty()) {
            qw.apply("FIND_IN_SET ('" + s.getCategoryId().replace("'", "") + "', category_ids)");
        }
        return qw;
    }

    private void applyTypeFilter(LambdaQueryWrapper<MallGoods> qw, String state) {
        if (state == null || state.isEmpty()) {
            return;
        }
        if ("sale".equals(state)) {
            qw.eq(MallGoods::getStatus, 1).eq(MallGoods::getAuditStatus, 1);
        } else if ("not_sale".equals(state)) {
            qw.eq(MallGoods::getStatus, 0);
        } else if ("audit".equals(state)) {
            qw.eq(MallGoods::getAuditStatus, 0);
        } else if ("audit_refuse".equals(state)) {
            qw.eq(MallGoods::getAuditStatus, 2);
        } else if ("stock_warning".equals(state)) {
            qw.eq(MallGoods::getStatus, 1)
                    .gt(MallGoods::getStockWarning, 0)
                    .gt(MallGoods::getTotalStock, 0)
                    .apply("total_stock < stock_warning");
        } else if ("recycle".equals(state)) {
            qw.clear();
            qw.eq(MallGoods::getDeleteTime, 0).eq(MallGoods::getIsRecycle, 1);
        }
    }

    private Map<String, Object> buildTabExtend(MallGoodsSearchDTO search) {
        Map<String, Object> extend = new LinkedHashMap<>();
        extend.put("count_sale", countByType(search, "sale"));
        extend.put("count_not_sale", countByType(search, "not_sale"));
        extend.put("count_audit", countByType(search, "audit"));
        extend.put("count_audit_refuse", countByType(search, "audit_refuse"));
        extend.put("count_stock_warning", countByType(search, "stock_warning"));
        extend.put("count_recycle", countByType(search, "recycle"));
        return extend;
    }

    private long countByType(MallGoodsSearchDTO search, String type) {
        LambdaQueryWrapper<MallGoods> qw = buildBaseQuery(search);
        applyTypeFilter(qw, type);
        return goodsMapper.selectCount(qw);
    }

    @Override
    public MallGoodsDetailVO detail(Integer id) {
        return detail(id, null);
    }

    @Override
    public MallGoodsDetailVO detail(Integer id, String lang) {
        MallGoods g = goodsMapper.selectById(id);
        if (g == null || (g.getDeleteTime() != null && g.getDeleteTime() != 0)) {
            throw new RuntimeException("商品不存在");
        }
        applyI18nOverlay(g, lang);
        MallGoodsDetailVO vo = new MallGoodsDetailVO();
        vo.setGoods(g);
        if (g.getCategoryIds() != null && !g.getCategoryIds().isEmpty()) {
            vo.setCategoryIds(Arrays.stream(g.getCategoryIds().split(","))
                    .filter(x -> !x.isEmpty()).map(Integer::valueOf).collect(Collectors.toList()));
        } else {
            vo.setCategoryIds(Collections.emptyList());
        }
        // 轮播图
        List<MallGoodsImage> imgs = imageMapper.selectList(
                new LambdaQueryWrapper<MallGoodsImage>().eq(MallGoodsImage::getGoodsId, id));
        vo.setImageList(imgs.stream().map(MallGoodsImage::getImage).collect(Collectors.toList()));
        // SKU
        List<MallGoodsSku> skus = skuMapper.selectList(
                new LambdaQueryWrapper<MallGoodsSku>().eq(MallGoodsSku::getGoodsId, id));
        vo.setSkuList(skus);
        // SKU 规格名
        List<MallGoodsSkuName> names = skuNameMapper.selectList(
                new LambdaQueryWrapper<MallGoodsSkuName>().eq(MallGoodsSkuName::getGoodsId, id));
        vo.setSkuNameList(names.stream().map(MallGoodsSkuName::getName).collect(Collectors.toList()));
        return vo;
    }

    private void applyI18nOverlay(MallGoods g, String lang) {
        if (g == null || g.getId() == null) {
            return;
        }
        GoodsCommonI18n i18n = goodsCommonI18nService.getPublished(g.getId(), lang);
        if (i18n == null) {
            return;
        }
        if (i18n.getCommonName() != null && !i18n.getCommonName().isBlank()) {
            g.setName(i18n.getCommonName());
        }
        if (i18n.getPromotionTips() != null && !i18n.getPromotionTips().isBlank()) {
            g.setDescript(i18n.getPromotionTips());
        }
        if (i18n.getCommonBody() != null && !i18n.getCommonBody().isBlank()) {
            g.setContent(i18n.getCommonBody());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer set(MallGoodsUpdateDTO dto) {
        int now = (int) (System.currentTimeMillis() / 1000);
        MallGoods g;
        boolean isNew = dto.getId() == null || dto.getId() == 0;
        if (isNew) {
            g = new MallGoods();
            g.setCreateTime(now);
        } else {
            g = goodsMapper.selectById(dto.getId());
            if (g == null) throw new RuntimeException("商品不存在");
        }
        String oldName = isNew ? null : g.getName();
        // 基础字段
        BeanUtils.copyProperties(dto, g, "id", "categoryIds", "imageList", "skuList", "skuNameList");
        g.setUpdateTime(now);
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            g.setCategoryIds(dto.getCategoryIds().stream().map(String::valueOf)
                    .collect(Collectors.joining(",")));
        }
        // 价格 & 库存聚合
        if (dto.getSkuList() != null && !dto.getSkuList().isEmpty()) {
            BigDecimal min = dto.getSkuList().stream().map(MallGoodsUpdateDTO.SkuItem::getPrice)
                    .filter(java.util.Objects::nonNull).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal max = dto.getSkuList().stream().map(MallGoodsUpdateDTO.SkuItem::getPrice)
                    .filter(java.util.Objects::nonNull).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            int totalStock = dto.getSkuList().stream()
                    .mapToInt(it -> it.getStock() == null ? 0 : it.getStock()).sum();
            g.setMinPrice(min);
            g.setMaxPrice(max);
            g.setTotalStock(totalStock);
        }
        // 新建商品默认待审
        if (isNew) {
            g.setAuditStatus(0);
            goodsMapper.insert(g);
        } else {
            goodsMapper.updateById(g);
            if (dto.getName() != null && oldName != null && !dto.getName().equals(oldName)) {
                goodsCommonI18nService.markStale(g.getId());
            }
        }
        Integer goodsId = g.getId();
        // 同步子表
        syncImages(goodsId, now, dto.getImageList());
        syncCategoryIndex(goodsId, now, dto.getCategoryIds());
        syncSkuNames(goodsId, now, dto.getSkuNameList());
        syncSkus(g.getShopId(), goodsId, now, dto.getSkuList());
        return goodsId;
    }

    private void syncImages(Integer goodsId, int now, List<String> images) {
        imageMapper.delete(new LambdaQueryWrapper<MallGoodsImage>().eq(MallGoodsImage::getGoodsId, goodsId));
        if (images == null) return;
        for (String img : images) {
            MallGoodsImage e = new MallGoodsImage();
            e.setGoodsId(goodsId);
            e.setImage(img);
            e.setCreateTime(now);
            e.setUpdateTime(now);
            imageMapper.insert(e);
        }
    }

    private void syncCategoryIndex(Integer goodsId, int now, List<Integer> cats) {
        categoryIndexMapper.delete(new LambdaQueryWrapper<MallGoodsCategoryIndex>()
                .eq(MallGoodsCategoryIndex::getGoodsId, goodsId));
        if (cats == null) return;
        for (Integer cid : cats) {
            MallGoodsCategoryIndex e = new MallGoodsCategoryIndex();
            e.setGoodsId(goodsId);
            e.setCategoryId(cid);
            e.setCreateTime(now);
            e.setUpdateTime(now);
            categoryIndexMapper.insert(e);
        }
    }

    private void syncSkuNames(Integer goodsId, int now, List<String> names) {
        skuNameMapper.delete(new LambdaQueryWrapper<MallGoodsSkuName>().eq(MallGoodsSkuName::getGoodsId, goodsId));
        if (names == null) return;
        for (String name : names) {
            MallGoodsSkuName e = new MallGoodsSkuName();
            e.setGoodsId(goodsId);
            e.setName(name);
            e.setCreateTime(now);
            e.setUpdateTime(now);
            skuNameMapper.insert(e);
        }
    }

    private void syncSkus(Integer shopId, Integer goodsId, int now, List<MallGoodsUpdateDTO.SkuItem> list) {
        skuMapper.delete(new LambdaQueryWrapper<MallGoodsSku>().eq(MallGoodsSku::getGoodsId, goodsId));
        if (list == null) return;
        for (MallGoodsUpdateDTO.SkuItem it : list) {
            MallGoodsSku e = new MallGoodsSku();
            BeanUtils.copyProperties(it, e, "id");
            e.setShopId(shopId);
            e.setGoodsId(goodsId);
            e.setCreateTime(now);
            e.setUpdateTime(now);
            skuMapper.insert(e);
        }
    }

    @Override
    public void audit(MallGoodsAuditDTO dto) {
        MallGoods g = goodsMapper.selectById(dto.getId());
        if (g == null) throw new RuntimeException("商品不存在");
        int now = (int) (System.currentTimeMillis() / 1000);
        LambdaUpdateWrapper<MallGoods> uw = new LambdaUpdateWrapper<MallGoods>()
                .eq(MallGoods::getId, dto.getId())
                .set(MallGoods::getAuditStatus, dto.getAuditStatus())
                .set(MallGoods::getAuditContent, dto.getAuditContent() == null ? "" : dto.getAuditContent())
                .set(MallGoods::getUpdateTime, now);
        // 审核通过自动上架
        if (dto.getAuditStatus() != null && dto.getAuditStatus() == 1) {
            uw.set(MallGoods::getStatus, 1);
        }
        goodsMapper.update(null, uw);
    }

    @Override
    public void reAudit(MallGoodsAuditDTO dto) {
        int now = (int) (System.currentTimeMillis() / 1000);
        goodsMapper.update(null, new LambdaUpdateWrapper<MallGoods>()
                .eq(MallGoods::getId, dto.getId())
                .set(MallGoods::getAuditStatus, 0)
                .set(MallGoods::getAuditContent, "")
                .set(MallGoods::getUpdateTime, now));
    }

    @Override
    public void del(Integer id) {
        int now = (int) (System.currentTimeMillis() / 1000);
        goodsMapper.update(null, new LambdaUpdateWrapper<MallGoods>()
                .eq(MallGoods::getId, id)
                .set(MallGoods::getDeleteTime, now)
                .set(MallGoods::getUpdateTime, now));
    }

    @Override
    public void changeStatus(Integer id, Integer status) {
        int now = (int) (System.currentTimeMillis() / 1000);
        goodsMapper.update(null, new LambdaUpdateWrapper<MallGoods>()
                .eq(MallGoods::getId, id)
                .set(MallGoods::getStatus, status)
                .set(MallGoods::getUpdateTime, now));
    }

    @Override
    public void changeRecommend(Integer id, Integer shopRecommend) {
        int now = (int) (System.currentTimeMillis() / 1000);
        goodsMapper.update(null, new LambdaUpdateWrapper<MallGoods>()
                .eq(MallGoods::getId, id)
                .set(MallGoods::getShopRecommend, shopRecommend)
                .set(MallGoods::getUpdateTime, now));
    }
}
