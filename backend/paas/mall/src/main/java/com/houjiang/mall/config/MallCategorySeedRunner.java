package com.houjiang.mall.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.entity.goods.MallGoodsCategory;
import com.houjiang.mall.mapper.goods.MallGoodsCategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BBC PC 首页默认分类种子。仅补齐缺失项，不覆盖后台已经维护的分类。
 */
@Slf4j
@Component
@Order(20)
@RequiredArgsConstructor
public class MallCategorySeedRunner implements ApplicationRunner {

    private final MallGoodsCategoryMapper categoryMapper;

    /** 一级频道：名称 → [图标, 二级名...] */
    private static final Map<String, String[]> DEFAULT_CATEGORIES = new LinkedHashMap<>();

    static {
        // 一级为频道名；二级为侧栏展示词（与截图「男装 | 箱包 | 女装 …」对齐）
        // 勿用已有一级类目名（如「男装」「美妆」）作 parent，避免把无关二级挂到旧树上
        DEFAULT_CATEGORIES.put("服饰鞋包", new String[]{"/uploads/mall-demo/cat-fashion.svg", "男装", "箱包", "女装", "配饰", "童装"});
        DEFAULT_CATEGORIES.put("美妆个护", new String[]{"/uploads/mall-demo/cat-care.svg", "品牌彩妆", "美食", "保养护肤", "洗护用品"});
        DEFAULT_CATEGORIES.put("家居生活", new String[]{"/uploads/mall-demo/cat-living.svg", "家居装饰", "床上用品", "厨具", "日用"});
        DEFAULT_CATEGORIES.put("图书音像", new String[]{"/uploads/mall-demo/cat-book.svg", "文学", "教材教辅", "人文社科", "科学"});
        DEFAULT_CATEGORIES.put("大家电", new String[]{"/uploads/mall-demo/cat-appliance.svg", "家用电器", "清洁电器", "厨房电器", "生活电器"});
        DEFAULT_CATEGORIES.put("数码配件", new String[]{"/uploads/mall-demo/cat-gadget.svg", "手机", "电脑", "耳机配件", "手表"});
        DEFAULT_CATEGORIES.put("食品饮料", new String[]{"/uploads/mall-demo/cat-food.svg", "休闲零食", "速食食品", "饮料冲调", "粮油调味"});
        DEFAULT_CATEGORIES.put("文具办公", new String[]{"/uploads/mall-demo/cat-office.svg", "文具用品", "书本用品", "办公设备"});
        DEFAULT_CATEGORIES.put("文娱票务", new String[]{"/uploads/mall-demo/cat-ticket.svg", "生活娱乐", "演出票务", "体育赛事", "影音会员"});
        DEFAULT_CATEGORIES.put("生鲜绿植", new String[]{"/uploads/mall-demo/cat-fresh.svg", "新鲜水果", "鲜花速递", "绿植园艺"});
        DEFAULT_CATEGORIES.put("到家服务", new String[]{"/uploads/mall-demo/cat-service.svg", "生活服务", "家电清洗", "家政服务"});
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            int sort = 10;
            for (Map.Entry<String, String[]> entry : DEFAULT_CATEGORIES.entrySet()) {
                String[] values = entry.getValue();
                String image = values.length > 0 ? values[0] : null;
                MallGoodsCategory parent = ensureCategory(entry.getKey(), 0, 1, sort++, image);
                for (int i = 1; i < values.length; i++) {
                    ensureCategory(values[i], parent.getId(), 2, sort++, "/uploads/mall-demo/goods-01.svg");
                }
            }
            log.info("PC 首页分类种子检查完成");
        } catch (Exception e) {
            // 分类表在部分旧环境可能尚未迁移，不能因此阻断商城服务启动。
            log.warn("PC 首页分类种子跳过：分类表不可用或数据结构不兼容", e);
        }
    }

    private MallGoodsCategory ensureCategory(String name, int pid, int level, int sort, String image) {
        MallGoodsCategory existing = categoryMapper.selectOne(new LambdaQueryWrapper<MallGoodsCategory>()
                .eq(MallGoodsCategory::getName, name)
                .eq(MallGoodsCategory::getPid, pid)
                .orderByDesc(MallGoodsCategory::getId)
                .last("LIMIT 1"));
        if (existing != null) {
            boolean changed = false;
            if (existing.getDeleteTime() != null && existing.getDeleteTime() != 0) {
                existing.setDeleteTime(0L);
                changed = true;
            }
            if (existing.getIsShow() == null || existing.getIsShow() != 1) {
                existing.setIsShow(1);
                changed = true;
            }
            if ((existing.getImage() == null || existing.getImage().isBlank())
                    && image != null && !image.isBlank()) {
                existing.setImage(image);
                changed = true;
            }
            if (changed) {
                existing.setUpdateTime(now());
                categoryMapper.updateById(existing);
            }
            return existing;
        }
        MallGoodsCategory created = new MallGoodsCategory();
        created.setName(name);
        created.setPid(pid);
        created.setLevel(level);
        created.setImage(image);
        created.setSort(sort);
        created.setIsShow(1);
        created.setDeleteTime(0L);
        created.setCreateTime(now());
        created.setUpdateTime(now());
        categoryMapper.insert(created);
        return created;
    }

    private static long now() {
        return System.currentTimeMillis() / 1000;
    }
}
