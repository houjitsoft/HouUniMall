package com.houjiang.mall.controller.home;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.home.RecommendGoods;
import com.houjiang.mall.service.home.RecommendGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 推荐商品控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/home/recommend")
public class RecommendGoodsController {
    
    @Autowired
    private RecommendGoodsService recommendGoodsService;
    
    /**
     * 获取首页推荐商品列表（前台接口）
     * 
     * @param type 推荐类型：1-热门推荐 2-新品推荐 3-猜你喜欢 4-人气榜单
     * @param position 位置：home-首页 category-分类页 detail-详情页
     * @param limit 限制数量
     * @return 推荐商品列表
     */
    @GetMapping("/list")
    public ResponseResult getHomeRecommendGoods(
            @RequestParam(required = false, defaultValue = "1") Integer type,
            @RequestParam(required = false, defaultValue = "home") String position,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return recommendGoodsService.getHomeRecommendGoods(type, position, limit);
    }
    
    /**
     * 分页查询推荐商品列表（后台管理）
     * 
     * @param params 查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public ResponseResult pageList(@RequestBody Map<String, Object> params) {
        return recommendGoodsService.pageList(params);
    }
    
    /**
     * 根据ID获取推荐商品详情
     * 
     * @param id 推荐商品ID
     * @return 推荐商品详情
     */
    @GetMapping("/{id}")
    public ResponseResult getById(@PathVariable Long id) {
        return recommendGoodsService.getById(id);
    }
    
    /**
     * 新增推荐商品
     * 
     * @param recommendGoods 推荐商品
     * @return 结果
     */
    @PostMapping("/add")
    public ResponseResult add(@RequestBody RecommendGoods recommendGoods) {
        return recommendGoodsService.add(recommendGoods);
    }
    
    /**
     * 更新推荐商品
     * 
     * @param recommendGoods 推荐商品
     * @return 结果
     */
    @PostMapping("/update")
    public ResponseResult update(@RequestBody RecommendGoods recommendGoods) {
        return recommendGoodsService.update(recommendGoods);
    }
    
    /**
     * 删除推荐商品
     * 
     * @param id 推荐商品ID
     * @return 结果
     */
    @PostMapping("/delete/{id}")
    public ResponseResult delete(@PathVariable Long id) {
        return recommendGoodsService.delete(id);
    }
    
    /**
     * 批量删除推荐商品
     * 
     * @param ids ID列表
     * @return 结果
     */
    @PostMapping("/batch/delete")
    public ResponseResult batchDelete(@RequestBody List<Long> ids) {
        return recommendGoodsService.batchDelete(ids);
    }
}
