package com.houjiang.mall.service.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.home.RecommendGoods;
import com.houjiang.mall.mapper.home.RecommendGoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 推荐商品服务类
 * 
 * @author Amos
 */
@Service
public class RecommendGoodsService {
    
    @Autowired
    private RecommendGoodsMapper recommendGoodsMapper;
    
    /**
     * 获取首页推荐商品列表（前台接口）
     * 
     * @param recommendType 推荐类型：1-热门推荐 2-新品推荐 3-猜你喜欢 4-人气榜单
     * @param position 位置：home-首页 category-分类页 detail-详情页
     * @param limit 限制数量
     * @return 推荐商品列表
     */
    public ResponseResult getHomeRecommendGoods(Integer recommendType, String position, Integer limit) {
        LambdaQueryWrapper<RecommendGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecommendGoods::getStatus, 1);
        if (recommendType != null) {
            wrapper.eq(RecommendGoods::getRecommendType, recommendType);
        }
        if (position != null && !position.isEmpty()) {
            wrapper.eq(RecommendGoods::getPosition, position);
        }
        LocalDateTime now = LocalDateTime.now();
        wrapper.and(w -> w.isNull(RecommendGoods::getStartTime).or().le(RecommendGoods::getStartTime, now));
        wrapper.and(w -> w.isNull(RecommendGoods::getEndTime).or().ge(RecommendGoods::getEndTime, now));
        wrapper.orderByAsc(RecommendGoods::getSort);
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        List<RecommendGoods> list = recommendGoodsMapper.selectList(wrapper);
        return ResponseResult.success(list);
    }
    
    /**
     * 分页查询推荐商品（后台管理）
     * 
     * @param params 查询参数（page, size, recommendType, position, status, keyword）
     * @return 分页结果
     */
    public ResponseResult pageList(Map<String, Object> params) {
        int page = params.get("page") != null ? (int) params.get("page") : 1;
        int size = params.get("size") != null ? (int) params.get("size") : 10;
        
        LambdaQueryWrapper<RecommendGoods> wrapper = new LambdaQueryWrapper<>();
        
        if (params.get("recommendType") != null) {
            wrapper.eq(RecommendGoods::getRecommendType, params.get("recommendType"));
        }
        if (params.get("position") != null && !params.get("position").toString().isEmpty()) {
            wrapper.eq(RecommendGoods::getPosition, params.get("position"));
        }
        if (params.get("status") != null) {
            wrapper.eq(RecommendGoods::getStatus, params.get("status"));
        }
        if (params.get("keyword") != null && !params.get("keyword").toString().isEmpty()) {
            wrapper.like(RecommendGoods::getGoodsName, params.get("keyword"));
        }
        
        wrapper.orderByAsc(RecommendGoods::getSort);
        wrapper.orderByDesc(RecommendGoods::getCreateTime);
        
        Page<RecommendGoods> pageResult = recommendGoodsMapper.selectPage(new Page<>(page, size), wrapper);
        return ResponseResult.success(pageResult);
    }
    
    /**
     * 新增推荐商品
     * 
     * @param recommendGoods 推荐商品
     * @return 结果
     */
    public ResponseResult add(RecommendGoods recommendGoods) {
        recommendGoods.setCreateTime(LocalDateTime.now());
        recommendGoods.setUpdateTime(LocalDateTime.now());
        if (recommendGoods.getSort() == null) {
            recommendGoods.setSort(0);
        }
        if (recommendGoods.getStatus() == null) {
            recommendGoods.setStatus(1);
        }
        recommendGoodsMapper.insert(recommendGoods);
        return ResponseResult.success(recommendGoods.getId());
    }
    
    /**
     * 更新推荐商品
     * 
     * @param recommendGoods 推荐商品
     * @return 结果
     */
    public ResponseResult update(RecommendGoods recommendGoods) {
        if (recommendGoods.getId() == null) {
            return ResponseResult.fail("ID不能为空");
        }
        recommendGoods.setUpdateTime(LocalDateTime.now());
        recommendGoodsMapper.updateById(recommendGoods);
        return ResponseResult.success();
    }
    
    /**
     * 删除推荐商品
     * 
     * @param id 推荐商品ID
     * @return 结果
     */
    public ResponseResult delete(Long id) {
        if (id == null) {
            return ResponseResult.fail("ID不能为空");
        }
        recommendGoodsMapper.deleteById(id);
        return ResponseResult.success();
    }
    
    /**
     * 批量删除推荐商品
     * 
     * @param ids ID列表
     * @return 结果
     */
    public ResponseResult batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseResult.fail("ID列表不能为空");
        }
        recommendGoodsMapper.deleteBatchIds(ids);
        return ResponseResult.success();
    }
    
    /**
     * 根据ID查询推荐商品详情
     * 
     * @param id 推荐商品ID
     * @return 推荐商品详情
     */
    public ResponseResult getById(Long id) {
        if (id == null) {
            return ResponseResult.fail("ID不能为空");
        }
        RecommendGoods recommendGoods = recommendGoodsMapper.selectById(id);
        return ResponseResult.success(recommendGoods);
    }
}
