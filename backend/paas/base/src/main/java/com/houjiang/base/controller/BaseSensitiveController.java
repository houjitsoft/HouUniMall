package com.houjiang.base.controller;


import com.houjiang.commons.annnotation.AdminTokenLimit;
import com.houjiang.commons.dto.sensitive.BaseSensitiveDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.sensitive.BaseSensitiveVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName BaseSensitiveController
 * @Description  敏感词词库
 * @Author Amos
 * @Date 2024-01-30 16:45:07
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("baseSensitive")
public class BaseSensitiveController extends BaseController {
    /**
     * 平台查询敏感词
     *
     * @param param
     * @return
     */
    @AdminTokenLimit
    @PostMapping("/query")
    public ResponseResult query(@RequestBody BaseSensitiveDTO param) {
        return baseSensitiveService.querySensitiveConfig(param);
    }

    /**
     * 平台 新增或修改 敏感词
     *
     * @param param
     * @return
     */
    @AdminTokenLimit
    @PostMapping("/add")
    public ResponseResult add(@RequestBody BaseSensitiveDTO param) {
        return baseSensitiveService.add(param);
    }

    /**
     * 平台 删除 敏感词
     *
     * @param param
     * @return
     */
    @AdminTokenLimit
    @PostMapping("delete")
    public ResponseResult delete(@RequestBody BaseSensitiveDTO param) {
        return baseSensitiveService.delete(param);
    }


    /**
     * 平台 批量添加 敏感词
     *
     * @param param
     * @return
     */
    @AdminTokenLimit
    @PostMapping("/addList")
    public ResponseResult addList(@RequestBody BaseSensitiveDTO param) {
        return baseSensitiveService.addList(param);
    }
    /**
     * 查询所有敏感词 feign
     *
     * @return
     */
    @PostMapping("/selectAllSensitive")
    public List<BaseSensitiveVO> selectAllSensitive() {
        return baseSensitiveService.selectAllSensitive();
    }
}
