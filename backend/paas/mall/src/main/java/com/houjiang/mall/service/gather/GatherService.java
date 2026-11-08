package com.houjiang.mall.service.gather;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.gather.Gather;
import com.houjiang.mall.mapper.gather.GatherMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 集采业务服务层
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GatherService extends ServiceImpl<GatherMapper, Gather> {

    /**
     * 获取集采列表
     * 
     * @param params 参数（goods_id，shop_id，page，size）
     * @return 集采列表
     */
    public List<Gather> list(Map<String, Object> params) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Gather>();
        
        // 可选：按商品 ID 筛选
        Long goodsId = getLong(params, "goods_id");
        if (goodsId != null) {
            wrapper.eq(Gather::getGoodsId, goodsId);
        }
        
        // 可选：按店铺 ID 筛选
        Long shopId = getLong(params, "shop_id");
        if (shopId != null) {
            wrapper.eq(Gather::getShopId, shopId);
        }
        
        return this.list(wrapper);
    }

    /**
     * 获取集采详情
     * 
     * @param id 主键 ID
     * @return 集采对象
     */
    public Gather getDetail(Long id) {
        return this.getById(id);
    }

    /**
     * 保存集采配置
     * 
     * @param gather 集采对象
     * @return 是否成功
     */
    public boolean saveConfig(Gather gather) {
        try {
            // 查询该商品该店铺是否已有集采配置
            var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Gather>();
            wrapper.eq(Gather::getGoodsId, gather.getGoodsId())
                   .eq(Gather::getShopId, gather.getShopId());
            
            Gather existGather = this.getOne(wrapper);
            
            if (existGather != null) {
                // 更新现有配置
                gather.setId(existGather.getId());
                return this.updateById(gather);
            } else {
                // 新增配置
                return this.save(gather);
            }
        } catch (Exception e) {
            log.error("保存集采配置失败", e);
            return false;
        }
    }

    /**
     * 删除集采配置
     * 
     * @param id 主键 ID
     * @return 是否成功
     */
    public boolean deleteConfig(Long id) {
        try {
            return this.removeById(id);
        } catch (Exception e) {
            log.error("删除集采配置失败", e);
            return false;
        }
    }

    /**
     * 从 Map 中提取 Long 值
     */
    private Long getLong(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }
}
