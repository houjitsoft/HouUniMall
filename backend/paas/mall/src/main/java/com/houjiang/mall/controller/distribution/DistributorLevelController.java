package com.houjiang.mall.controller.distribution;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.distribution.DistributorLevel;
import com.houjiang.mall.service.distribution.DistributorLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 分销等级控制器
 * 提供分销等级的列表、详情、创建、更新、删除等功能
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/distribution/level")
@RequiredArgsConstructor
public class DistributorLevelController {

    private final DistributorLevelService distributorLevelService;

    /**
     * 分页查询分销等级列表
     *
     * @param params 查询参数（可包含 shopId, page, size）
     * @return 分销等级列表
     */
    @PostMapping("/list")
    public ResponseResult<List<DistributorLevel>> list(@RequestBody Map<String, Object> params) {
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        return ResponseResult.success(distributorLevelService.listByShop(shopId));
    }

    /**
     * 获取所有分销等级（不分页）
     *
     * @param params 查询参数（可包含 shopId）
     * @return 分销等级列表
     */
    @PostMapping("/all")
    public ResponseResult<List<DistributorLevel>> all(@RequestBody Map<String, Object> params) {
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        return ResponseResult.success(distributorLevelService.listByShop(shopId));
    }

    /**
     * 获取分销等级详情
     *
     * @param params 参数（包含 id）
     * @return 分销等级详情
     */
    @PostMapping("/detail")
    public ResponseResult<DistributorLevel> detail(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return ResponseResult.success(distributorLevelService.getById(id));
    }

    /**
     * 创建分销等级
     *
     * @param level 分销等级信息
     * @return 是否成功
     */
    @PostMapping("/create")
    public ResponseResult<Boolean> create(@RequestBody DistributorLevel level) {
        return ResponseResult.success(distributorLevelService.createLevel(level));
    }

    /**
     * 更新分销等级
     *
     * @param level 分销等级信息
     * @return 是否成功
     */
    @PostMapping("/update")
    public ResponseResult<Boolean> update(@RequestBody DistributorLevel level) {
        return ResponseResult.success(distributorLevelService.updateById(level));
    }

    /**
     * 删除分销等级
     *
     * @param params 参数（包含 id）
     * @return 是否成功
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return ResponseResult.success(distributorLevelService.removeById(id));
    }

    /**
     * 获取默认分销等级
     *
     * @param params 参数（包含 shopId）
     * @return 默认分销等级
     */
    @PostMapping("/default")
    public ResponseResult<DistributorLevel> getDefault(@RequestBody Map<String, Object> params) {
        Long shopId = Long.parseLong(params.get("shopId").toString());
        return ResponseResult.success(distributorLevelService.getDefaultLevel(shopId));
    }
}
