package com.houjiang.mall.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.user.GoodsFavorite;
import com.houjiang.mall.mapper.user.GoodsFavoriteMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品收藏服务实现类
 * 
 * @author Amos
 */
@Slf4j
@Service
public class GoodsFavoriteService extends ServiceImpl<GoodsFavoriteMapper, GoodsFavorite> {

    /**
     * 添加收藏
     */
    public boolean addFavorite(GoodsFavorite favorite) {
        // 检查是否已收藏
        LambdaQueryWrapper<GoodsFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsFavorite::getUserId, favorite.getUserId())
                .eq(GoodsFavorite::getGoodsId, favorite.getGoodsId())
                .eq(GoodsFavorite::getFavoriteType, favorite.getFavoriteType());
        
        GoodsFavorite existing = this.getOne(wrapper);
        if (existing != null) {
            // 已收藏,返回false
            return false;
        }
        
        favorite.setIsValid(1);
        return this.save(favorite);
    }

    /**
     * 取消收藏
     */
    public boolean removeFavorite(Long id, Long userId) {
        LambdaQueryWrapper<GoodsFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsFavorite::getId, id)
                .eq(GoodsFavorite::getUserId, userId);
        return this.remove(wrapper);
    }

    /**
     * 根据商品ID取消收藏
     */
    public boolean removeFavoriteByGoodsId(Long goodsId, Long userId) {
        LambdaQueryWrapper<GoodsFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsFavorite::getGoodsId, goodsId)
                .eq(GoodsFavorite::getUserId, userId);
        return this.remove(wrapper);
    }

    /**
     * 批量取消收藏
     */
    public boolean batchRemove(List<Long> ids, Long userId) {
        LambdaQueryWrapper<GoodsFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(GoodsFavorite::getId, ids)
                .eq(GoodsFavorite::getUserId, userId);
        return this.remove(wrapper);
    }

    /**
     * 检查是否已收藏
     */
    public boolean isFavorited(Long goodsId, Long userId) {
        LambdaQueryWrapper<GoodsFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsFavorite::getGoodsId, goodsId)
                .eq(GoodsFavorite::getUserId, userId);
        return this.count(wrapper) > 0;
    }

    /**
     * 获取收藏列表
     */
    public IPage<GoodsFavorite> getFavoriteList(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<GoodsFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsFavorite::getUserId, userId)
                .orderByDesc(GoodsFavorite::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    /**
     * 获取收藏数量
     */
    public long getFavoriteCount(Long userId) {
        LambdaQueryWrapper<GoodsFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsFavorite::getUserId, userId);
        return this.count(wrapper);
    }

    /**
     * 按类型获取收藏列表
     */
    public List<GoodsFavorite> getFavoriteByType(Long userId, Integer favoriteType) {
        LambdaQueryWrapper<GoodsFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsFavorite::getUserId, userId)
                .eq(GoodsFavorite::getFavoriteType, favoriteType)
                .orderByDesc(GoodsFavorite::getCreateTime);
        return this.list(wrapper);
    }
}
