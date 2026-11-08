package com.houjiang.goods.mall.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.mall.dto.GoodsCommonI18nSaveDTO;
import com.houjiang.goods.mall.service.GoodsCommonI18nService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商品多语言内部接口：/mall/goods/i18n
 */
@RestController
@RequestMapping("/mall/goods/i18n")
public class GoodsCommonI18nController {

    @Autowired
    private GoodsCommonI18nService service;

    /** 查询某商品全部语种 */
    @GetMapping("/{commonId}")
    public ResponseResult list(@PathVariable("commonId") Integer commonId) {
        return ResponseResult.success(service.listByCommonId(commonId));
    }

    /** 批量保存 */
    @PutMapping("/{commonId}")
    public ResponseResult save(@PathVariable("commonId") Integer commonId,
                               @RequestBody GoodsCommonI18nSaveDTO dto) {
        if (dto == null) {
            dto = new GoodsCommonI18nSaveDTO();
        }
        dto.setCommonId(commonId);
        service.save(dto);
        return ResponseResult.success(service.listByCommonId(commonId));
    }

    /** 已发布单语种 */
    @GetMapping("/{commonId}/published")
    public ResponseResult published(@PathVariable("commonId") Integer commonId,
                                    @RequestParam("lang") String lang) {
        return ResponseResult.success(service.getPublished(commonId, lang));
    }

    /** 中文变更后标记 stale */
    @PostMapping("/{commonId}/mark-stale")
    public ResponseResult markStale(@PathVariable("commonId") Integer commonId) {
        service.markStale(commonId);
        return ResponseResult.success();
    }

    /** 兼容 POST body 保存 */
    @PostMapping("/save")
    public ResponseResult savePost(@RequestBody GoodsCommonI18nSaveDTO dto) {
        service.save(dto);
        return ResponseResult.success(service.listByCommonId(dto.getCommonId()));
    }
}
