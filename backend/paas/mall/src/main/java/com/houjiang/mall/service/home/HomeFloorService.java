package com.houjiang.mall.service.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.home.HomeFloor;
import com.houjiang.mall.mapper.home.HomeFloorMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 首页楼层配置服务
 * 
 * @author Amos
 */
@Service
public class HomeFloorService extends ServiceImpl<HomeFloorMapper, HomeFloor> {

    /**
     * 分页查询楼层配置
     * 
     * @param param 查询参数
     * @return 分页结果
     */
    public Page<HomeFloor> queryPage(Map<String, Object> param) {
        int pageNum = param.get("pageNum") != null ? Integer.parseInt(param.get("pageNum").toString()) : 1;
        int pageSize = param.get("pageSize") != null ? Integer.parseInt(param.get("pageSize").toString()) : 10;
        
        LambdaQueryWrapper<HomeFloor> wrapper = new LambdaQueryWrapper<>();
        
        // 店铺ID
        if (param.get("shopId") != null) {
            wrapper.eq(HomeFloor::getShopId, Long.parseLong(param.get("shopId").toString()));
        }
        
        // 楼层类型
        if (StringUtils.hasText((String) param.get("floorType"))) {
            wrapper.eq(HomeFloor::getFloorType, param.get("floorType"));
        }
        
        // 状态
        if (param.get("status") != null) {
            wrapper.eq(HomeFloor::getStatus, param.get("status"));
        }
        
        wrapper.orderByDesc(HomeFloor::getSort);
        
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 获取启用的楼层配置
     * 
     * @param shopId 店铺ID（可选）
     * @return 楼层列表
     */
    public List<HomeFloor> getEnabledFloors(Long shopId) {
        LambdaQueryWrapper<HomeFloor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeFloor::getStatus, 1);
        
        if (shopId != null) {
            wrapper.eq(HomeFloor::getShopId, shopId);
        } else {
            wrapper.isNull(HomeFloor::getShopId);
        }
        
        wrapper.orderByDesc(HomeFloor::getSort);
        
        return this.list(wrapper);
    }

    /**
     * 根据类型获取楼层配置
     * 
     * @param floorType 楼层类型
     * @param shopId 店铺ID（可选）
     * @return 楼层列表
     */
    public List<HomeFloor> getFloorsByType(String floorType, Long shopId) {
        LambdaQueryWrapper<HomeFloor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeFloor::getFloorType, floorType);
        wrapper.eq(HomeFloor::getStatus, 1);
        
        if (shopId != null) {
            wrapper.eq(HomeFloor::getShopId, shopId);
        } else {
            wrapper.isNull(HomeFloor::getShopId);
        }
        
        wrapper.orderByDesc(HomeFloor::getSort);
        
        return this.list(wrapper);
    }

    /**
     * 更新楼层排序
     * 
     * @param id 楼层ID
     * @param sort 排序值
     * @return 是否成功
     */
    public boolean updateSort(Long id, Integer sort) {
        HomeFloor floor = new HomeFloor();
        floor.setId(id);
        floor.setSort(sort);
        return this.updateById(floor);
    }

    /**
     * 更新楼层状态
     * 
     * @param id 楼层ID
     * @param status 状态
     * @return 是否成功
     */
    public boolean updateStatus(Long id, Integer status) {
        HomeFloor floor = new HomeFloor();
        floor.setId(id);
        floor.setStatus(status);
        return this.updateById(floor);
    }
}
