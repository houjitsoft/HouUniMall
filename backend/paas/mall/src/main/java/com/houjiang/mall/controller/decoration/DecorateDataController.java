package com.houjiang.mall.controller.decoration;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.content.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 装修数据 Controller
 * 提供装修系统所需的外部数据（文章、商品等）
 * 路径：/mall/decoration/data/*
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/decoration/data")
@RequiredArgsConstructor
public class DecorateDataController {

    private final ContentService contentService;

    /**
     * 获取文章数据（用于装修组件选择文章）
     * POST /mall/decoration/data/article
     *
     * @param params 查询参数（可选：page, size, keyword等）
     * @return 文章数据列表
     */
    @PostMapping("/article")
    public ResponseResult getArticleData(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取装修用文章数据，参数：{}", params);
            // 复用 content 选题数据作为文章来源
            return ResponseResult.success(contentService.selectionPageList(params));
        } catch (Exception e) {
            log.error("获取文章数据失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }
}
