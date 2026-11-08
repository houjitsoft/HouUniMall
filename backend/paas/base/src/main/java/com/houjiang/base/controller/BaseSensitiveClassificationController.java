package com.houjiang.base.controller;


import com.houjiang.base.dto.BaseSensitiveClassificationDTO;
import com.houjiang.commons.annnotation.AdminTokenLimit;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName BaseSensitiveClassificationController
 * @Description  敏感词分类
 * @Author Amos
 * @Date 2024-01-30 16:48:04
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("baseSensitiveClassification")
public class BaseSensitiveClassificationController extends BaseController {
    /**
     * 平台 查询敏感词分类
     * @param param
     * @return
     */
    @AdminTokenLimit
    @PostMapping("/query")
    public ResponseResult query(@RequestBody BaseSensitiveClassificationDTO param) {
        return baseSensitiveClassificationService.querySensitiveClassification(param);
    }

    /**
     * 查询所有敏感词分类
     * @return
     */
    @PostMapping("/queryAll")
    public ResponseResult queryAll() {
        return baseSensitiveClassificationService.querySensitiveClassificationAll();
    }

    /**
     * 平台添加或修改敏感词分类
     * @param param
     * @return
     */
    @AdminTokenLimit
    @PostMapping("/add")
    public ResponseResult add(@RequestBody BaseSensitiveClassificationDTO param) {
        return baseSensitiveClassificationService.add(param);
    }

    /**
     * 平台 删除敏感词分类
     * @param param
     * @return
     */
    @PostMapping("/delete")
    @AdminTokenLimit
    public ResponseResult delete(@RequestBody BaseSensitiveClassificationDTO param) {
        return baseSensitiveClassificationService.delete(param);
    }
}
