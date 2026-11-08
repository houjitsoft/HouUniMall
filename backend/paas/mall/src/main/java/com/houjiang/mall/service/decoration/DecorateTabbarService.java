package com.houjiang.mall.service.decoration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.decoration.DecorateTabbar;
import com.houjiang.mall.mapper.decoration.DecorateTabbarMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 底部导航 Service
 *
 * @author Amos
 */
@Service
public class DecorateTabbarService extends ServiceImpl<DecorateTabbarMapper, DecorateTabbar> {

    /**
     * 获取底部导航详情（取第一条启用的配置）
     *
     * @param shopId 店铺ID，为null时获取平台级配置
     * @return 底部导航配置
     */
    public DecorateTabbar getTabbarDetail(Long shopId) {
        LambdaQueryWrapper<DecorateTabbar> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(shopId != null, DecorateTabbar::getShopId, shopId);
        if (shopId == null) {
            wrapper.isNull(DecorateTabbar::getShopId);
        }
        wrapper.eq(DecorateTabbar::getStatus, 1);
        wrapper.orderByDesc(DecorateTabbar::getCreateTime);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    /**
     * 保存底部导航配置（存在则更新，不存在则新增）
     *
     * @param tabbar 底部导航配置对象
     * @return 是否成功
     */
    public boolean saveTabbar(DecorateTabbar tabbar) {
        if (tabbar.getId() != null) {
            return this.updateById(tabbar);
        }
        // 查找是否已有同店铺配置
        LambdaQueryWrapper<DecorateTabbar> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(tabbar.getShopId() != null, DecorateTabbar::getShopId, tabbar.getShopId());
        if (tabbar.getShopId() == null) {
            wrapper.isNull(DecorateTabbar::getShopId);
        }
        DecorateTabbar exist = this.getOne(wrapper);
        if (exist != null) {
            exist.setContent(tabbar.getContent());
            exist.setStatus(tabbar.getStatus() != null ? tabbar.getStatus() : 1);
            return this.updateById(exist);
        }
        tabbar.setStatus(tabbar.getStatus() != null ? tabbar.getStatus() : 1);
        return this.save(tabbar);
    }
}
