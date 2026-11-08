package com.houjiang.mall.controller.points;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.points.PointsTool;
import com.houjiang.mall.service.points.PointsToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 积分商城Controller
 * 提供积分商品的CRUD和上下架操作
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/points-tool")
@RequiredArgsConstructor
public class PointsToolController {

    private final PointsToolService pointsToolService;

    /**
     * 分页查询积分商品列表
     *
     * @param params 查询参数：page, size, name, status
     * @return ResponseResult 分页数据
     */
    @PostMapping("/list")
    public ResponseResult list(@RequestBody Map<String, Object> params) {
        try {
            return ResponseResult.success(pointsToolService.pageList(params));
        } catch (Exception e) {
            log.error("查询积分商品列表失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取积分商品详情
     *
     * @param params 包含id字段
     * @return ResponseResult 商品详情
     */
    @PostMapping("/detail")
    public ResponseResult detail(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            PointsTool tool = pointsToolService.getById(id);
            return tool != null ? ResponseResult.success(tool) : ResponseResult.fail(404, "商品不存在");
        } catch (Exception e) {
            log.error("查询积分商品详情失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建积分商品
     *
     * @param tool 商品实体
     * @return ResponseResult 操作结果
     */
    @PostMapping("/create")
    public ResponseResult create(@RequestBody PointsTool tool) {
        try {
            return pointsToolService.save(tool) ? ResponseResult.success("创建成功") : ResponseResult.fail(500, "创建失败");
        } catch (Exception e) {
            log.error("创建积分商品失败", e);
            return ResponseResult.fail(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新积分商品
     *
     * @param tool 商品实体
     * @return ResponseResult 操作结果
     */
    @PostMapping("/update")
    public ResponseResult update(@RequestBody PointsTool tool) {
        try {
            return pointsToolService.updateById(tool) ? ResponseResult.success("更新成功") : ResponseResult.fail(500, "更新失败");
        } catch (Exception e) {
            log.error("更新积分商品失败", e);
            return ResponseResult.fail(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除积分商品
     *
     * @param params 包含id字段
     * @return ResponseResult 操作结果
     */
    @PostMapping("/delete")
    public ResponseResult delete(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            return pointsToolService.removeById(id) ? ResponseResult.success("删除成功") : ResponseResult.fail(500, "删除失败");
        } catch (Exception e) {
            log.error("删除积分商品失败", e);
            return ResponseResult.fail(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 切换积分商品上下架状态
     *
     * @param params 包含id字段
     * @return ResponseResult 操作结果
     */
    @PostMapping("/toggle-status")
    public ResponseResult toggleStatus(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            return pointsToolService.toggleStatus(id) ? ResponseResult.success("操作成功") : ResponseResult.fail(500, "操作失败");
        } catch (Exception e) {
            log.error("切换积分商品状态失败", e);
            return ResponseResult.fail(500, "操作失败: " + e.getMessage());
        }
    }
}
