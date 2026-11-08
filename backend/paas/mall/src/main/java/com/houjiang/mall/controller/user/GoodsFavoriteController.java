package com.houjiang.mall.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.user.GoodsFavorite;
import com.houjiang.mall.service.user.GoodsFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品收藏Controller
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/favorite")
@RequiredArgsConstructor
public class GoodsFavoriteController {

    private final GoodsFavoriteService goodsFavoriteService;

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public ResponseResult<Boolean> addFavorite(@RequestBody GoodsFavorite favorite) {
        if (favorite.getUserId() == null || favorite.getGoodsId() == null) {
            return ResponseResult.error("参数不完整");
        }
        if (favorite.getFavoriteType() == null) {
            // 默认商品收藏
            favorite.setFavoriteType(1);
        }
        boolean result = goodsFavoriteService.addFavorite(favorite);
        if (!result) {
            return ResponseResult.error("已收藏该商品");
        }
        return ResponseResult.success(true);
    }

    /**
     * 取消收藏
     */
    @PostMapping("/remove")
    public ResponseResult<Boolean> removeFavorite(@RequestBody Map<String, Object> param) {
        Long id = Long.valueOf(param.get("id").toString());
        Long userId = Long.valueOf(param.get("userId").toString());
        boolean result = goodsFavoriteService.removeFavorite(id, userId);
        return ResponseResult.success(result);
    }

    /**
     * 根据商品ID取消收藏
     */
    @PostMapping("/remove-by-goods")
    public ResponseResult<Boolean> removeFavoriteByGoodsId(@RequestBody Map<String, Object> param) {
        Long goodsId = Long.valueOf(param.get("goodsId").toString());
        Long userId = Long.valueOf(param.get("userId").toString());
        boolean result = goodsFavoriteService.removeFavoriteByGoodsId(goodsId, userId);
        return ResponseResult.success(result);
    }

    /**
     * 批量取消收藏
     */
    @PostMapping("/batch-remove")
    public ResponseResult<Boolean> batchRemove(@RequestBody Map<String, Object> param) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) param.get("ids");
        Long userId = Long.valueOf(param.get("userId").toString());
        boolean result = goodsFavoriteService.batchRemove(ids, userId);
        return ResponseResult.success(result);
    }

    /**
     * 检查是否已收藏
     */
    @PostMapping("/is-favorited")
    public ResponseResult<Boolean> isFavorited(@RequestBody Map<String, Object> param) {
        Long goodsId = Long.valueOf(param.get("goodsId").toString());
        Long userId = Long.valueOf(param.get("userId").toString());
        boolean result = goodsFavoriteService.isFavorited(goodsId, userId);
        return ResponseResult.success(result);
    }

    /**
     * 获取收藏列表(分页)
     */
    @PostMapping("/list")
    public ResponseResult<IPage<GoodsFavorite>> getFavoriteList(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        Integer page = (Integer) param.getOrDefault("page", 1);
        Integer size = (Integer) param.getOrDefault("size", 10);
        IPage<GoodsFavorite> list = goodsFavoriteService.getFavoriteList(userId, page, size);
        return ResponseResult.success(list);
    }

    /**
     * 获取收藏数量
     */
    @PostMapping("/count")
    public ResponseResult<Long> getFavoriteCount(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        long count = goodsFavoriteService.getFavoriteCount(userId);
        return ResponseResult.success(count);
    }

    /**
     * 按类型获取收藏列表
     */
    @PostMapping("/list-by-type")
    public ResponseResult<List<GoodsFavorite>> getFavoriteByType(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        Integer favoriteType = Integer.valueOf(param.get("favoriteType").toString());
        List<GoodsFavorite> list = goodsFavoriteService.getFavoriteByType(userId, favoriteType);
        return ResponseResult.success(list);
    }
}
