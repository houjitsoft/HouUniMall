package com.houjiang.base.controller;


import com.houjiang.base.dto.BaseArticleGroupDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName BaseArticleGroupController
 * @Description 网站初始化内容分组表
 * @Author Amos
 * @Date 2023-06-07 14:49:59
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/articleGroup")
public class BaseArticleGroupController extends BaseController {

    /**
     * 查询文章分组
     *
     * @return
     */
    @GetMapping("/query")
    public ResponseResult queryArticleGroup() {

        return baseArticleGroupService.queryArticleGroup();
    }

    /**
     * 新增或修改 文章分组
     *
     * @param baseArticleGroupReq
     * @return
     */
    @PostMapping("/update")
    public ResponseResult updateArticleGroup(@RequestBody BaseArticleGroupDTO baseArticleGroupReq) {
        return baseArticleGroupService.updateArticleGroup(baseArticleGroupReq);
    }

    /**
     * 删除 文章分组
     *
     * @param baseArticleGroupReq
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult deleteArticleGroup(@RequestBody BaseArticleGroupDTO baseArticleGroupReq) {
        return baseArticleGroupService.deleteArticleGroup(baseArticleGroupReq);
    }

}
