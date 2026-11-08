package com.houjiang.base.controller;


import com.houjiang.base.dto.ModularizationdDTO;
import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.util.ResponseResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName BaseModularizationdController
 * @Description  组件化 页面
 * @Author Amos
 * @Date 2023-07-04 17:58:51
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("baseModularizationd")
public class BaseModularizationdController extends BaseController{

    /*
/**
     * 添加或修改 组件化页面
     */
    @SensitiveCheck(field = {"pageName"})
    @PostMapping("/modularizationdAddOrUPdate")
    public ResponseResult ModularizationdAddOrUPdate(@RequestBody ModularizationdDTO param) {
        return  baseModularizationdService.ModularizationdAddOrUPdate(param);
    }

    /*
/**
     * 查询组件化页面 列表
     */
    @PostMapping("/queryModularizationdAll")
    public ResponseResult queryModularizationdAll(@RequestBody ModularizationdDTO param) {
        return  baseModularizationdService.queryModularizationdAll(param);
    }

    /*
/**
     * 查询组件化页面 列表
     */
    @PostMapping("/queryModularizationdById")
    public ResponseResult queryModularizationdById(@RequestBody ModularizationdDTO param) {
        return  baseModularizationdService.queryModularizationdById(param);
    }

    /*
/**
     * 删除   组件化页面
     */
    @PostMapping("/modularizationdDelete")
    public ResponseResult modularizationdDelete(@RequestBody ModularizationdDTO param) {
        return  baseModularizationdService.modularizationdDelete(param);
    }

    /**
     * 将页面设置是否使用
     */
    @PostMapping("/modularizationdUpdateStart")
    public ResponseResult modularizationdUpdateStart(@RequestBody ModularizationdDTO param) {
        return  baseModularizationdService.modularizationdUpdateStart(param);
    }

    /**
     * 根据id 查询系统默认页面
     * @return
     */
    @PostMapping("/modularizationdDefault")
    public ResponseResult ModularizationdDefaultByPageId(@RequestBody ModularizationdDTO param) {
        return  baseModularizationdService.ModularizationdDefaultByPageId(param);
    }


    /**
     * 二级页面使用
     */
    @PostMapping("/updateSecondLevelStart")
    public ResponseResult UpdateSecondLevelStart(@RequestBody ModularizationdDTO param) {
        ResponseResult result=baseModularizationdService.UpdateSecondLevelStart(param);
        return  result;
    }

    /**
     * 获取首页页面
     *
     * @return
     */
    @SysLog("移动端首页访客")
    @PostMapping("/modularizationdIndex")
    public ResponseResult modularizationdUpdateIndex() {
        ResponseResult result=baseModularizationdService.modularizationdUpdateIndex();
        return  result;
    }
    /**
     *获取导航列表
/**
     * @param param
     * @return
     */
    @PostMapping("/queryBottomNavigation")
    public ResponseResult querybottomNavigation(@RequestBody Map<String,Object> param) {
        ResponseResult result=baseModularizationdService.querybottomNavigation(param);
        return  result;
    }
    /**
     *修改底部导航栏配置
/**
     * @param param
     * @return
     */
    @PostMapping("/bottomNavigationUpdate")
    public ResponseResult bottomNavigation(@RequestBody ModularizationdDTO param) {
        ResponseResult result=baseModularizationdService.bottomNavigation(param);
        return  result;
    }

    /**
     * 添加
     * @param param
     * @return
     */
    @PostMapping("/fegin/add")
    public Integer add(@RequestBody ModularizationdDTO param){
        Integer result=baseModularizationdService.add(param);
        return result;
    }

    /**
     * 查询移动端页面模板库（后台「移动端装修 → 模板选择」用）。
     *
     * @return 模板列表
     */
    @PostMapping("/queryPageTemplates")
    public ResponseResult queryPageTemplates() {
        return baseModularizationdService.queryPageTemplates();
    }

    /**
     * 查询某页面当前使用的模板 code。
     *
     * @param param 含 pageId
     * @return 当前 templateCode，未设置时为空串
     */
    @PostMapping("/queryPageTemplateCode")
    public ResponseResult queryPageTemplateCode(@RequestBody ModularizationdDTO param) {
        return baseModularizationdService.queryPageTemplateCode(param);
    }

    /**
     * 为某页面选择移动端模板（按页面生效）。
     *
     * @param param 含 pageId 与 templateCode；templateCode 传空串表示取消套用
     * @return 是否成功
     */
    @PostMapping("/applyPageTemplate")
    public ResponseResult applyPageTemplate(@RequestBody ModularizationdDTO param) {
        return baseModularizationdService.applyPageTemplate(param);
    }
}
