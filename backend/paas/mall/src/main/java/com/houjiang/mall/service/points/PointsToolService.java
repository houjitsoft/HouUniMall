package com.houjiang.mall.service.points;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.points.PointsTool;
import com.houjiang.mall.mapper.points.PointsToolMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 积分商品服务
 * 提供积分商品的CRUD和上下架操作
 *
 * @author Amos
 */
@Slf4j
@Service
public class PointsToolService extends ServiceImpl<PointsToolMapper, PointsTool> {

    /**
     * 分页查询积分商品列表
     * 支持按名称、状态筛选
     *
     * @param params 查询参数：page, size, name, status
     * @return 分页结果
     */
    public IPage<PointsTool> pageList(Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;

        LambdaQueryWrapper<PointsTool> wrapper = new LambdaQueryWrapper<>();
        if (params.get("name") != null && StringUtils.hasText(params.get("name").toString())) {
            wrapper.like(PointsTool::getName, params.get("name").toString());
        }
        if (params.get("status") != null) {
            wrapper.eq(PointsTool::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        wrapper.orderByAsc(PointsTool::getSort).orderByDesc(PointsTool::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    /**
     * 切换积分商品上下架状态
     *
     * @param id 商品ID
     * @return 是否成功
     */
    public boolean toggleStatus(Long id) {
        PointsTool tool = this.getById(id);
        if (tool == null) {
            return false;
        }
        tool.setStatus(tool.getStatus() == 1 ? 0 : 1);
        return this.updateById(tool);
    }
}
