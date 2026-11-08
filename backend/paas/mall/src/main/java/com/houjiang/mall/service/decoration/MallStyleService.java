package com.houjiang.mall.service.decoration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.decoration.MallStyle;
import com.houjiang.mall.mapper.decoration.MallStyleMapper;
import org.springframework.stereotype.Service;

/**
 * 商城风格 Service
 *
 * @author Amos
 */
@Service
public class MallStyleService extends ServiceImpl<MallStyleMapper, MallStyle> {

    /**
     * 获取商城风格配置
     *
     * @param shopId 店铺ID，为null时获取平台级配置
     * @return 商城风格配置
     */
    public MallStyle getMallStyle(Long shopId) {
        LambdaQueryWrapper<MallStyle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(shopId != null, MallStyle::getShopId, shopId);
        if (shopId == null) {
            wrapper.isNull(MallStyle::getShopId);
        }
        wrapper.orderByDesc(MallStyle::getCreateTime);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    /**
     * 保存商城风格配置（存在则更新，不存在则新增）
     *
     * @param mallStyle 风格配置对象
     * @return 是否成功
     */
    public boolean saveMallStyle(MallStyle mallStyle) {
        if (mallStyle.getId() != null) {
            return this.updateById(mallStyle);
        }
        // 查找是否已有同店铺配置
        LambdaQueryWrapper<MallStyle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(mallStyle.getShopId() != null, MallStyle::getShopId, mallStyle.getShopId());
        if (mallStyle.getShopId() == null) {
            wrapper.isNull(MallStyle::getShopId);
        }
        MallStyle exist = this.getOne(wrapper);
        if (exist != null) {
            if (mallStyle.getThemeColor() != null) {
                exist.setThemeColor(mallStyle.getThemeColor());
            }
            if (mallStyle.getStyleConfig() != null) {
                exist.setStyleConfig(mallStyle.getStyleConfig());
            }
            return this.updateById(exist);
        }
        return this.save(mallStyle);
    }
}
