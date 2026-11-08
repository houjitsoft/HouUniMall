package com.houjiang.base.controller;

import com.houjiang.base.dto.ModularizationdDTO;
import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName BaseShopModularizationdController
 * @Description
 * @Author Amos
 * @Date 2023/8/3 15:39
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/shopModularizationd")
public class BaseShopModularizationdController extends BaseController{

    /**
     *底部导航栏配置
/**
     * @param param
     * @return
     */
    @PostMapping("/bottomNavigationUpdate")
    public ResponseResult bottomNavigation(@RequestBody ModularizationdDTO param) {
        ResponseResult result=baseShopModularizationdService.bottomNavigation(param);
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
        ResponseResult result=baseShopModularizationdService.querybottomNavigation(param);
        return  result;
    }
    /**
     *移动端首页获取导航
/**
     * @param param
     * @return
     */
    @PostMapping("/indexQueryBottomNavigation")
    public ResponseResult indexQueryBottomNavigation(@RequestBody Map<String,Object> param) {
        ResponseResult result=baseShopModularizationdService.indexQueryBottomNavigation(param);
        return  result;
    }
    /**
     * 获取首页页面
     * @return
     */
    @PostMapping("/h5/modularizationdShopIndex")
    public ResponseResult modularizationdUpdateIndex(@RequestBody Map<String,Object> map) {
        return baseShopModularizationdService.modularizationdUpdateIndex(map);
    }


    /**
     * 添加或修改 组件化页面
     * @param param
     * @return
     */
    @SensitiveCheck(field = {"pageName"})
    @PostMapping("/shopAddOrUpdate")
    public ResponseResult ModularizationdAddOrUPdate(@RequestBody ModularizationdDTO param) {
        ResponseResult result=baseShopModularizationdService.ModularizationdAddOrUPdate(param);
        return  result;
    }


    /**
     * 查询组件化页面 列表
     * @param param
     * @return
     */
    @PostMapping("/queryShopAll")
    public ResponseResult queryModularizationdAll(@RequestBody ModularizationdDTO param) {
        ResponseResult result=baseShopModularizationdService.queryModularizationdAll(param);
        return  result;
    }

    /**
     * 查询组件化页面 id
     * @param param
     * @return
     */
    @PostMapping("/queryShopById")
    public ResponseResult queryModularizationdShopById(@RequestBody ModularizationdDTO param) {
        ResponseResult result=baseShopModularizationdService.queryModularizationdShopById(param);
        return  result;
    }


    /**
     * 删除   组件化页面
     * @param param
     * @return
     */
    @PostMapping("/shopDelete")
    public ResponseResult modularizationdDelete(@RequestBody ModularizationdDTO param) {
        ResponseResult result=baseShopModularizationdService.modularizationdDelete(param);
        return  result;
    }


    /**
     * 修改页面 是否使用
     */
    @PostMapping("/shopUpdateStart")
    public ResponseResult modularizationdUpdateStart(@RequestBody ModularizationdDTO param) {
        ResponseResult result=baseShopModularizationdService.modularizationdUpdateStart(param);
        return  result;
    }
}
