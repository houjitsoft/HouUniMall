package com.houjiang.mall.controller.home;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.home.Banner;
import com.houjiang.mall.service.home.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Banner轮播图控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/home/banner")
public class BannerController {
    
    @Autowired
    private BannerService bannerService;
    
    /**
     * 获取首页Banner列表（前台接口）
     */
    @GetMapping("/list")
    public ResponseResult getHomeBanners(
            @RequestParam(required = false, defaultValue = "home") String position,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        return bannerService.getHomeBanners(position, limit);
    }
    
    /**
     * 分页查询Banner列表（后台管理）
     */
    @PostMapping("/page")
    public ResponseResult queryPage(@RequestBody Map<String, Object> params) {
        return bannerService.queryPage(params);
    }
    
    /**
     * 根据ID查询Banner
     */
    @GetMapping("/{id}")
    public ResponseResult getById(@PathVariable Long id) {
        return bannerService.getById(id);
    }
    
    /**
     * 新增Banner
     */
    @PostMapping("/add")
    public ResponseResult add(@RequestBody Banner banner) {
        return bannerService.add(banner);
    }
    
    /**
     * 更新Banner
     */
    @PostMapping("/update")
    public ResponseResult update(@RequestBody Banner banner) {
        return bannerService.update(banner);
    }
    
    /**
     * 删除Banner
     */
    @PostMapping("/delete/{id}")
    public ResponseResult delete(@PathVariable Long id) {
        return bannerService.delete(id);
    }
}
