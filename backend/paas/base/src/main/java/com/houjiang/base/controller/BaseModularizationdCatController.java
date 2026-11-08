package com.houjiang.base.controller;

import com.houjiang.base.dto.ModularizationdCatDTO;
import com.houjiang.base.dto.ModularizationdCatDataDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName BaseModularizationdCatController
 * @Description
 * @Author Amos
 * @Date 2023/8/30 15:07
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/modularizationd/cat")
public class BaseModularizationdCatController extends BaseController{

    /**
     * 分类页 查询所有
     */
    @PostMapping("/selectAll")
    public ResponseResult selectAllModularizationdCat(@RequestBody ModularizationdCatDTO ModularizationdCatDTO) throws IllegalAccessException {
        ResponseResult result=baseModularizationdCatService.selectAll(ModularizationdCatDTO);
        return result;
    }


    /**
     * 分类页 添加
     */
    @PostMapping("/add")
    public ResponseResult addModularizationdCat(@RequestBody ModularizationdCatDTO ModularizationdCatDTO){
        ResponseResult result=baseModularizationdCatService.addModularizationdCat(ModularizationdCatDTO);
        return result;
    }

    /**
     * 分类页 修改
     */
    @PostMapping("/update")
    public ResponseResult updateModularizationdCat(@RequestBody ModularizationdCatDTO ModularizationdCatDTO){
        ResponseResult result=baseModularizationdCatService.updateModularizationdCat(ModularizationdCatDTO);
        return result;
    }

    /**
     * 分类页 删除
     */
    @PostMapping("/delete")
    public ResponseResult deleteModularizationdCat(@RequestBody ModularizationdCatDTO ModularizationdCatDTO){
        ResponseResult result=baseModularizationdCatService.deleteModularizationdCat(ModularizationdCatDTO);
        return result;
    }

    /**
     * 使用页面
     */
    @PostMapping("/updatePageApply")
    public ResponseResult updatePageApply(@RequestBody ModularizationdCatDTO ModularizationdCatDTO){
        ResponseResult result=baseModularizationdCatService.updatePageApply(ModularizationdCatDTO);
        return result;
    }

    /**
     * 查询分类页详情
     */
    @PostMapping("/querPageDataById")
    public ResponseResult querPageDataById(@RequestBody ModularizationdCatDTO ModularizationdCatDTO){
        ResponseResult result=baseModularizationdCatService.querPageDataById(ModularizationdCatDTO);
        return result;
    }

    /**
     * 解析分类页 数据
     */
    @PostMapping("/analysisPageData")
    public ResponseResult analysisPageData(@RequestBody ModularizationdCatDataDTO ModularizationdCatDTO){
        ResponseResult result=baseModularizationdCatService.analysisPageData(ModularizationdCatDTO);
        return result;
    }

    /**
     * 查询使用中的页面
     * @return
     */
    @PostMapping("/queryApplyPage")
    public ResponseResult queryApplyPage(){
        return baseModularizationdCatService.queryApplyPage();
    }
}
