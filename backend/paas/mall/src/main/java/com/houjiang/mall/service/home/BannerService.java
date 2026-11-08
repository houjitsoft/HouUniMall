package com.houjiang.mall.service.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.home.Banner;
import com.houjiang.mall.mapper.home.BannerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Banner轮播图服务类
 * 
 * @author Amos
 */
@Service
public class BannerService {
    
    @Autowired
    private BannerMapper bannerMapper;
    
    /**
     * 获取首页Banner列表（前台接口）
     * 
     * @param position 位置：home-首页 category-分类页
     * @param limit 限制数量
     * @return Banner列表
     */
    public ResponseResult getHomeBanners(String position, Integer limit) {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getStatus, 1)
               .eq(Banner::getPosition, position != null ? position : "home")
               .and(w -> w.isNull(Banner::getStartTime)
                       .or().le(Banner::getStartTime, LocalDateTime.now()))
               .and(w -> w.isNull(Banner::getEndTime)
                       .or().ge(Banner::getEndTime, LocalDateTime.now()))
               .orderByDesc(Banner::getSort)
               .orderByDesc(Banner::getCreateTime);
        
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        
        List<Banner> banners = bannerMapper.selectList(wrapper);
        return ResponseResult.success(banners);
    }
    
    /**
     * 分页查询Banner列表（后台管理）
     * 
     * @param params 查询参数
     * @return 分页结果
     */
    public ResponseResult queryPage(Map<String, Object> params) {
        int pageNum = params.get("pageNum") != null ? (Integer) params.get("pageNum") : 1;
        int pageSize = params.get("pageSize") != null ? (Integer) params.get("pageSize") : 10;
        
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        
        if (params.get("status") != null) {
            wrapper.eq(Banner::getStatus, params.get("status"));
        }
        if (params.get("position") != null) {
            wrapper.eq(Banner::getPosition, params.get("position"));
        }
        if (params.get("shopId") != null) {
            wrapper.eq(Banner::getShopId, params.get("shopId"));
        }
        
        wrapper.orderByDesc(Banner::getSort).orderByDesc(Banner::getCreateTime);
        
        Page<Banner> page = bannerMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return ResponseResult.success(page);
    }
    
    /**
     * 根据ID查询Banner
     * 
     * @param id Banner ID
     * @return Banner信息
     */
    public ResponseResult getById(Long id) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            return ResponseResult.fail(404, "Banner不存在");
        }
        return ResponseResult.success(banner);
    }
    
    /**
     * 新增Banner
     * 
     * @param banner Banner实体
     * @return 操作结果
     */
    public ResponseResult add(Banner banner) {
        banner.setCreateTime(LocalDateTime.now());
        banner.setUpdateTime(LocalDateTime.now());
        if (banner.getSort() == null) {
            banner.setSort(0);
        }
        bannerMapper.insert(banner);
        return ResponseResult.success(banner.getId());
    }
    
    /**
     * 更新Banner
     * 
     * @param banner Banner实体
     * @return 操作结果
     */
    public ResponseResult update(Banner banner) {
        if (banner.getId() == null) {
            return ResponseResult.fail(400, "ID不能为空");
        }
        banner.setUpdateTime(LocalDateTime.now());
        bannerMapper.updateById(banner);
        return ResponseResult.success();
    }
    
    /**
     * 删除Banner
     * 
     * @param id Banner ID
     * @return 操作结果
     */
    public ResponseResult delete(Long id) {
        bannerMapper.deleteById(id);
        return ResponseResult.success();
    }
}
