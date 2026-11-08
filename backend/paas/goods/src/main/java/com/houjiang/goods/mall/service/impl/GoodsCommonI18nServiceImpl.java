package com.houjiang.goods.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.goods.mall.dto.GoodsCommonI18nSaveDTO;
import com.houjiang.goods.mall.entity.GoodsCommonI18n;
import com.houjiang.goods.mall.mapper.GoodsCommonI18nMapper;
import com.houjiang.goods.mall.service.GoodsCommonI18nService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoodsCommonI18nServiceImpl implements GoodsCommonI18nService {

    @Autowired
    private GoodsCommonI18nMapper mapper;

    @Override
    public List<GoodsCommonI18n> listByCommonId(Integer commonId) {
        if (commonId == null) {
            return Collections.emptyList();
        }
        return mapper.selectList(new LambdaQueryWrapper<GoodsCommonI18n>()
                .eq(GoodsCommonI18n::getCommonId, commonId)
                .orderByAsc(GoodsCommonI18n::getLangCode));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(GoodsCommonI18nSaveDTO dto) {
        if (dto == null || dto.getCommonId() == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new RuntimeException("commonId 与 items 不能为空");
        }
        Date now = new Date();
        for (GoodsCommonI18nSaveDTO.Item item : dto.getItems()) {
            if (!StringUtils.hasText(item.getLangCode())) {
                continue;
            }
            String lang = item.getLangCode().trim().toLowerCase();
            GoodsCommonI18n exist = mapper.selectOne(new LambdaQueryWrapper<GoodsCommonI18n>()
                    .eq(GoodsCommonI18n::getCommonId, dto.getCommonId())
                    .eq(GoodsCommonI18n::getLangCode, lang)
                    .last("LIMIT 1"));
            if (exist == null) {
                exist = new GoodsCommonI18n();
                exist.setCommonId(dto.getCommonId());
                exist.setLangCode(lang);
                exist.setCreateTime(now);
            }
            if (item.getCommonName() != null) {
                exist.setCommonName(item.getCommonName());
            }
            if (item.getPromotionTips() != null) {
                exist.setPromotionTips(item.getPromotionTips());
            }
            if (item.getCommonBody() != null) {
                exist.setCommonBody(item.getCommonBody());
            }
            if (item.getSpecNameJson() != null) {
                exist.setSpecNameJson(item.getSpecNameJson());
            }
            if (item.getSpecValueJson() != null) {
                exist.setSpecValueJson(item.getSpecValueJson());
            }
            exist.setSource(StringUtils.hasText(item.getSource()) ? item.getSource() : "manual");
            exist.setStatus(StringUtils.hasText(item.getStatus()) ? item.getStatus() : "published");
            exist.setUpdateTime(now);
            if (exist.getId() == null) {
                mapper.insert(exist);
            } else {
                mapper.updateById(exist);
            }
        }
    }

    @Override
    public GoodsCommonI18n getPublished(Integer commonId, String langCode) {
        if (commonId == null || !StringUtils.hasText(langCode) || isCn(langCode)) {
            return null;
        }
        return mapper.selectOne(new LambdaQueryWrapper<GoodsCommonI18n>()
                .eq(GoodsCommonI18n::getCommonId, commonId)
                .eq(GoodsCommonI18n::getLangCode, langCode.trim().toLowerCase())
                .eq(GoodsCommonI18n::getStatus, "published")
                .last("LIMIT 1"));
    }

    @Override
    public Map<Integer, String> mapPublishedNames(List<Integer> commonIds, String langCode) {
        if (commonIds == null || commonIds.isEmpty() || !StringUtils.hasText(langCode) || isCn(langCode)) {
            return Collections.emptyMap();
        }
        List<GoodsCommonI18n> rows = mapper.selectList(new LambdaQueryWrapper<GoodsCommonI18n>()
                .in(GoodsCommonI18n::getCommonId, commonIds)
                .eq(GoodsCommonI18n::getLangCode, langCode.trim().toLowerCase())
                .eq(GoodsCommonI18n::getStatus, "published"));
        Map<Integer, String> map = new HashMap<>();
        for (GoodsCommonI18n row : rows) {
            if (StringUtils.hasText(row.getCommonName())) {
                map.put(row.getCommonId(), row.getCommonName());
            }
        }
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markStale(Integer commonId) {
        if (commonId == null) {
            return;
        }
        List<GoodsCommonI18n> rows = listByCommonId(commonId);
        Date now = new Date();
        for (GoodsCommonI18n row : rows) {
            if ("published".equals(row.getStatus()) || "draft".equals(row.getStatus())) {
                row.setStatus("stale");
                row.setUpdateTime(now);
                mapper.updateById(row);
            }
        }
    }

    private boolean isCn(String lang) {
        String l = lang.trim().toLowerCase();
        return "cn".equals(l) || "zh".equals(l) || "zh-cn".equals(l) || "zh_cn".equals(l);
    }
}
