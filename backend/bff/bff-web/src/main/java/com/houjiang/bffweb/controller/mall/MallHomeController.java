package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Mall商城首页接口控制器
/**
 * 提供首页数据、Banner、公告、推荐商品等接口
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/home")
public class MallHomeController {
    
    @Autowired
    private MallServiceFeign mallServiceFeign;
    
    // ================= 首页聚合数据 =================
    
    /**
     * 获取首页聚合数据（前台接口）
/**
     * 包含：轮播图、快报、推荐商品等
     */
    @GetMapping("/data")
    public ResponseResult getHomeData() {
        return mallServiceFeign.getHomeData();
    }
    
    // ================= Banner轮播图 =================
    
    /**
     * 获取首页Banner列表（前台接口）
     */
    @GetMapping("/banner/list")
    public ResponseResult getHomeBanners(
            @RequestParam(required = false, defaultValue = "home") String position,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        return mallServiceFeign.getHomeBanners(position, limit);
    }
    
    /**
     * 分页查询Banner列表（后台管理）
     */
    @PostMapping("/banner/page")
    public ResponseResult getBannerPage(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getBannerPage(param);
    }
    
    /**
     * 新增Banner
     */
    @PostMapping("/banner/add")
    public ResponseResult addBanner(@RequestBody Map<String, Object> banner) {
        return mallServiceFeign.addBanner(banner);
    }
    
    /**
     * 更新Banner
     */
    @PostMapping("/banner/update")
    public ResponseResult updateBanner(@RequestBody Map<String, Object> banner) {
        return mallServiceFeign.updateBanner(banner);
    }
    
    /**
     * 删除Banner
     */
    @PostMapping("/banner/delete/{id}")
    public ResponseResult deleteBanner(@PathVariable Long id) {
        return mallServiceFeign.deleteBanner(id);
    }
    
    // ================= 公告快报 =================
    
    /**
     * 获取首页公告列表（前台接口）
     */
    @GetMapping("/notice/list")
    public ResponseResult getHomeNotices(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        return mallServiceFeign.getHomeNotices(type, limit);
    }
    
    /**
     * 分页查询公告列表（后台管理）
     */
    @PostMapping("/notice/page")
    public ResponseResult getNoticePage(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getNoticePage(param);
    }
    
    /**
     * 新增公告
     */
    @PostMapping("/notice/add")
    public ResponseResult addNotice(@RequestBody Map<String, Object> notice) {
        return mallServiceFeign.addNotice(notice);
    }
    
    /**
     * 更新公告
     */
    @PostMapping("/notice/update")
    public ResponseResult updateNotice(@RequestBody Map<String, Object> notice) {
        return mallServiceFeign.updateNotice(notice);
    }
    
    /**
     * 删除公告
     */
    @PostMapping("/notice/delete/{id}")
    public ResponseResult deleteNotice(@PathVariable Long id) {
        return mallServiceFeign.deleteNotice(id);
    }
    
    // ================= 推荐商品 =================
    
    /**
     * 获取首页推荐商品列表（前台接口）
     */
    @GetMapping("/recommend/list")
    public ResponseResult getHomeRecommendGoods(
            @RequestParam(required = false, defaultValue = "1") Integer type,
            @RequestParam(required = false, defaultValue = "home") String position,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return mallServiceFeign.getHomeRecommendGoods(type, position, limit);
    }
    
    /**
     * 分页查询推荐商品列表（后台管理）
     */
    @PostMapping("/recommend/page")
    public ResponseResult getRecommendGoodsPage(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getRecommendGoodsPage(param);
    }
    
    /**
     * 根据ID获取推荐商品详情
     */
    @GetMapping("/recommend/{id}")
    public ResponseResult getRecommendGoodsById(@PathVariable Long id) {
        return mallServiceFeign.getRecommendGoodsById(id);
    }
    
    /**
     * 新增推荐商品
     */
    @PostMapping("/recommend/add")
    public ResponseResult addRecommendGoods(@RequestBody Map<String, Object> goods) {
        return mallServiceFeign.addRecommendGoods(goods);
    }
    
    /**
     * 更新推荐商品
     */
    @PostMapping("/recommend/update")
    public ResponseResult updateRecommendGoods(@RequestBody Map<String, Object> goods) {
        return mallServiceFeign.updateRecommendGoods(goods);
    }
    
    /**
     * 删除推荐商品
     */
    @PostMapping("/recommend/delete/{id}")
    public ResponseResult deleteRecommendGoods(@PathVariable Long id) {
        return mallServiceFeign.deleteRecommendGoods(id);
    }
    
    /**
     * 批量删除推荐商品
     */
    @PostMapping("/recommend/batch/delete")
    public ResponseResult batchDeleteRecommendGoods(@RequestBody List<Long> ids) {
        return mallServiceFeign.batchDeleteRecommendGoods(ids);
    }
    
    // ================= 首页楼层配置 =================
    
    /**
     * 获取启用的楼层配置（前台接口）
     */
    @GetMapping("/floor/enabled")
    public ResponseResult getEnabledFloors(@RequestParam(required = false) Long shopId) {
        return mallServiceFeign.getEnabledFloors(shopId);
    }
    
    /**
     * 根据类型获取楼层配置
     */
    @GetMapping("/floor/type/{floorType}")
    public ResponseResult getFloorsByType(@PathVariable String floorType,
                                          @RequestParam(required = false) Long shopId) {
        return mallServiceFeign.getFloorsByType(floorType, shopId);
    }
    
    /**
     * 分页查询楼层配置（后台管理）
     */
    @PostMapping("/floor/page")
    public ResponseResult getFloorPage(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getFloorPage(param);
    }
    
    /**
     * 获取楼层详情
     */
    @GetMapping("/floor/{id}")
    public ResponseResult getFloorById(@PathVariable Long id) {
        return mallServiceFeign.getFloorById(id);
    }
    
    /**
     * 新增楼层配置
     */
    @PostMapping("/floor/add")
    public ResponseResult addFloor(@RequestBody Map<String, Object> floor) {
        return mallServiceFeign.addFloor(floor);
    }
    
    /**
     * 更新楼层配置
     */
    @PostMapping("/floor/update")
    public ResponseResult updateFloor(@RequestBody Map<String, Object> floor) {
        return mallServiceFeign.updateFloor(floor);
    }
    
    /**
     * 删除楼层配置
     */
    @PostMapping("/floor/delete/{id}")
    public ResponseResult deleteFloor(@PathVariable Long id) {
        return mallServiceFeign.deleteFloor(id);
    }
    
    /**
     * 更新楼层排序
     */
    @PostMapping("/floor/sort")
    public ResponseResult updateFloorSort(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.updateFloorSort(param);
    }
    
    /**
     * 更新楼层状态
     */
    @PostMapping("/floor/status")
    public ResponseResult updateFloorStatus(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.updateFloorStatus(param);
    }
}
