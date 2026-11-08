package com.houjiang.mall.controller.home;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.home.BannerService;
import com.houjiang.mall.service.home.NoticeService;
import com.houjiang.mall.service.home.RecommendGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页聚合数据控制器
 * 提供首页所有楼层数据的聚合接口
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/home")
public class HomeController {

    /** 首页聚合短缓存：同一时刻多入口并发打爆 Hikari 时，合并为一次装载。 */
    private static final long HOME_DATA_CACHE_TTL_MS = 30_000L;

    @Autowired
    private BannerService bannerService;
    
    @Autowired
    private NoticeService noticeService;
    
    @Autowired
    private RecommendGoodsService recommendGoodsService;

    private final Object homeDataLock = new Object();
    private volatile Map<String, Object> homeDataCache;
    private volatile long homeDataCachedAtMs;
    
    /**
     * 获取首页聚合数据
     * 包含：轮播图、快报、推荐商品、服务保障、快捷入口等
     */
    @GetMapping("/data")
    public ResponseResult getHomeData() {
        long now = System.currentTimeMillis();
        Map<String, Object> cached = homeDataCache;
        if (cached != null && now - homeDataCachedAtMs < HOME_DATA_CACHE_TTL_MS) {
            return ResponseResult.success(cached);
        }

        synchronized (homeDataLock) {
            now = System.currentTimeMillis();
            cached = homeDataCache;
            if (cached != null && now - homeDataCachedAtMs < HOME_DATA_CACHE_TTL_MS) {
                return ResponseResult.success(cached);
            }

            Map<String, Object> data = loadHomeData();
            homeDataCache = data;
            homeDataCachedAtMs = System.currentTimeMillis();
            return ResponseResult.success(data);
        }
    }

    private Map<String, Object> loadHomeData() {
        Map<String, Object> data = new HashMap<>();

        // 楼层1: 轮播图Banner
        ResponseResult bannersResult = bannerService.getHomeBanners("home", 5);
        data.put("banners", bannersResult.getData());

        // 楼层2: 商城快报/公告（类型1为快报）
        ResponseResult noticesResult = noticeService.getHomeNotices(1, 5);
        data.put("notices", noticesResult.getData());

        // 楼层3: 热门推荐商品（类型1为热门推荐）
        ResponseResult hotGoodsResult = recommendGoodsService.getHomeRecommendGoods(1, "home", 10);
        data.put("hotGoods", hotGoodsResult.getData());

        // 楼层4: 新品推荐商品（类型2为新品推荐）
        ResponseResult newGoodsResult = recommendGoodsService.getHomeRecommendGoods(2, "home", 10);
        data.put("newGoods", newGoodsResult.getData());

        // 楼层5: 猜你喜欢商品（类型3为猜你喜欢）
        ResponseResult guessGoodsResult = recommendGoodsService.getHomeRecommendGoods(3, "home", 10);
        data.put("guessGoods", guessGoodsResult.getData());

        // 楼层6: 人气榜单商品（类型4为人气榜单）
        ResponseResult popularGoodsResult = recommendGoodsService.getHomeRecommendGoods(4, "home", 10);
        data.put("popularGoods", popularGoodsResult.getData());

        // 服务保障（静态数据）
        data.put("services", getServiceList());

        // 快捷入口（静态数据）
        data.put("quickEntries", getQuickEntries());

        // 楼层标题配置
        data.put("floorTitles", getFloorTitles());

        return data;
    }
    
    /**
     * 获取楼层标题配置
     * 
     * @return 楼层标题列表
     */
    private List<Map<String, String>> getFloorTitles() {
        return List.of(
            Map.of("key", "seckill", "title", "限时秒杀", "subtitle", "低价抢购 限时特惠"),
            Map.of("key", "hotGoods", "title", "热门推荐", "subtitle", "精选好货 品质之选"),
            Map.of("key", "newGoods", "title", "新品上市", "subtitle", "潮流新品 抢先体验"),
            Map.of("key", "brands", "title", "品牌专区", "subtitle", "大牌正品 品质保障"),
            Map.of("key", "guessGoods", "title", "猜你喜欢", "subtitle", "为你推荐 专属好物"),
            Map.of("key", "popularGoods", "title", "人气榜单", "subtitle", "热销爆款 人气之选")
        );
    }
    
    /**
     * 获取服务保障列表（静态配置）
     * 
     * @return 服务保障列表
     */
    private List<Map<String, String>> getServiceList() {
        return List.of(
            Map.of("title", "正品保障", "desc", "100%正品行货", "icon", "CircleCheck", "color", "linear-gradient(135deg, #ff6b6b, #ee5a52)"),
            Map.of("title", "极速发货", "desc", "24小时内发货", "icon", "Van", "color", "linear-gradient(135deg, #4ecdc4, #44a08d)"),
            Map.of("title", "7天退换", "desc", "无忧退换货", "icon", "RefreshRight", "color", "linear-gradient(135deg, #a18cd1, #fbc2eb)"),
            Map.of("title", "贴心服务", "desc", "专业客服团队", "icon", "Service", "color", "linear-gradient(135deg, #ffecd2, #fcb69f)")
        );
    }
    
    /**
     * 获取快捷入口列表（静态配置）
     * 
     * @return 快捷入口列表
     */
    private List<Map<String, String>> getQuickEntries() {
        return List.of(
            Map.of("name", "我的浏览", "icon", "View"),
            Map.of("name", "我的收藏", "icon", "Star"),
            Map.of("name", "我的订单", "icon", "Document"),
            Map.of("name", "账号安全", "icon", "Lock"),
            Map.of("name", "售后服务", "icon", "Headset"),
            Map.of("name", "商家入驻", "icon", "Shop")
        );
    }
}
