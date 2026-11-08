package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 积分商城工具Controller
/**
 * 通过MallServiceFeign调用mall服务积分商品接口
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/mall/points-tool")
@RequiredArgsConstructor
public class MallPointsToolController {

    private final MallServiceFeign mallServiceFeign;

    /**
     * 积分商品列表
/**
     *
     * @param params 查询参数：page, size, name, status
     * @return ResponseResult 分页数据
     */
    @PostMapping("/list")
    public ResponseResult list(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getPointsToolList(params);
    }

    /**
     * 积分商品详情
/**
     *
     * @param params 包含id字段
     * @return ResponseResult 商品详情
     */
    @PostMapping("/detail")
    public ResponseResult detail(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getPointsToolDetail(params);
    }

    /**
     * 创建积分商品
/**
     *
     * @param params 商品数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/create")
    public ResponseResult create(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.createPointsTool(params);
    }

    /**
     * 更新积分商品
/**
     *
     * @param params 商品数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/update")
    public ResponseResult update(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.updatePointsTool(params);
    }

    /**
     * 删除积分商品
/**
     *
     * @param params 包含id字段
     * @return ResponseResult 操作结果
     */
    @PostMapping("/delete")
    public ResponseResult delete(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.deletePointsTool(params);
    }

    /**
     * 切换积分商品上下架状态
/**
     *
     * @param params 包含id字段
     * @return ResponseResult 操作结果
     */
    @PostMapping("/toggle-status")
    public ResponseResult toggleStatus(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.togglePointsToolStatus(params);
    }
}
