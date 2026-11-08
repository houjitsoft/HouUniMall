package com.houjiang.mall.mapper.market;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 插件分类表（hou_addon_cate）只读查询 —— 插件市场侧。
 *
 * <p>与 plugin-center / permission 里同名 mapper 读的是同一张表，只是分属不同 Maven 模块，
 * 各自声明，避免为一张表跨模块拉依赖。
 *
 * <p>hou_addon_cate 原只有 id / name / create_time / update_time 四列，2026-09-19 补了
 * {@code sort}（见 scripts/sql/migrations/20260919-plugin-cate-orthogonal-split.sql），
 * 顺序改为 sort 升序、同 sort 按 id 升序，后台可调分类先后。
 *
 * <p>仍<b>不加 pid</b>：市场分类刻意保持扁平，加层级会把「市场标签」重新绑回
 * 「导航层级树」，退回两套分类互相拷贝的老问题。
 */
@Mapper
public interface MarketAddonCateMapper {

    /** 全量分类（sort 升序，同 sort 按 id；sort 缺失时按 0 处理）。 */
    @Select("SELECT id, name, COALESCE(sort, 0) AS sort FROM hou_addon_cate ORDER BY sort ASC, id ASC")
    List<Map<String, Object>> selectAll();
}
