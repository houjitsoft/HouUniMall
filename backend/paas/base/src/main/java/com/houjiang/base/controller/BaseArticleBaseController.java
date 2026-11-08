package com.houjiang.base.controller;


import com.houjiang.base.dto.BaseArticleBaseDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName BaseArticleBaseController
 * @Description  网站初始化内容设置
 * @Author Amos
 * @Date 2023-06-07 15:17:52
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/articleBase")
public class BaseArticleBaseController extends BaseController {


    /**
     * 根据文章id查询分组列表
     *
     * @param articleGroupId
     * @return
     */
    @GetMapping(value = "/group/detail/query")
    public ResponseResult queryArticleGroupDetail(@RequestParam("articleGroupId") Integer articleGroupId) {
        return baseArticleBaseService.queryArticleGroupDetail(articleGroupId);
    }

    /**
     * 查询文章信息
     *
     * @param param
     * @return
     */
    @PostMapping("/query/detail")
    private ResponseResult articleQueryDetail(@RequestBody BaseArticleBaseDTO param) {
        return baseArticleBaseService.articleQueryDetail(param);
    }

    /**
     * 删除文章
     *
     * @param param
     * @return
     */
    @PostMapping("/delete")
    private ResponseResult articleDelete(@RequestBody BaseArticleBaseDTO param) {
        return baseArticleBaseService.articleDelete(param);
    }

    /**
     * 更新或添加文章
     *
     * @param param
     * @return
     */
    @PostMapping("/update")
    private ResponseResult articleUpdate(@RequestBody BaseArticleBaseDTO param) {
        return baseArticleBaseService.articleUpdate(param);
    }


    /**
     * 查询所有的文章
     *
     * @param param
     * @return
     */
    @PostMapping("/queryAll")
    private ResponseResult queryArticleBase(@RequestBody BaseArticleBaseDTO param) {
        return baseArticleBaseService.queryArticleBase(param);
    }
}
