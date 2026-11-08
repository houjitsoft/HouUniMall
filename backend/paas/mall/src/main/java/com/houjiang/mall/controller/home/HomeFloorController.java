package com.houjiang.mall.controller.home;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.home.HomeFloor;
import com.houjiang.mall.service.home.HomeFloorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 首页楼层配置Controller
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/home/floor")
public class HomeFloorController {

    @Autowired
    private HomeFloorService homeFloorService;

    /**
     * 分页查询楼层配置
     * 
     * @param param 查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public ResponseResult page(@RequestBody Map<String, Object> param) {
        Page<HomeFloor> page = homeFloorService.queryPage(param);
        return ResponseResult.success(page);
    }

    /**
     * 获取启用的楼层配置
     * 
     * @param shopId 店铺ID（可选）
     * @return 楼层列表
     */
    @GetMapping("/enabled")
    public ResponseResult getEnabledFloors(@RequestParam(required = false) Long shopId) {
        List<HomeFloor> floors = homeFloorService.getEnabledFloors(shopId);
        return ResponseResult.success(floors);
    }

    /**
     * 根据类型获取楼层配置
     * 
     * @param floorType 楼层类型
     * @param shopId 店铺ID（可选）
     * @return 楼层列表
     */
    @GetMapping("/type/{floorType}")
    public ResponseResult getFloorsByType(@PathVariable String floorType,
                                          @RequestParam(required = false) Long shopId) {
        List<HomeFloor> floors = homeFloorService.getFloorsByType(floorType, shopId);
        return ResponseResult.success(floors);
    }

    /**
     * 获取楼层详情
     * 
     * @param id 楼层ID
     * @return 楼层详情
     */
    @GetMapping("/{id}")
    public ResponseResult getById(@PathVariable Long id) {
        HomeFloor floor = homeFloorService.getById(id);
        return ResponseResult.success(floor);
    }

    /**
     * 新增楼层配置
     * 
     * @param floor 楼层信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public ResponseResult add(@RequestBody HomeFloor floor) {
        if (floor.getSort() == null) {
            floor.setSort(0);
        }
        if (floor.getStatus() == null) {
            floor.setStatus(1);
        }
        boolean success = homeFloorService.save(floor);
        return success ? ResponseResult.success("添加成功") : ResponseResult.error("添加失败");
    }

    /**
     * 更新楼层配置
     * 
     * @param floor 楼层信息
     * @return 操作结果
     */
    @PostMapping("/update")
    public ResponseResult update(@RequestBody HomeFloor floor) {
        boolean success = homeFloorService.updateById(floor);
        return success ? ResponseResult.success("更新成功") : ResponseResult.error("更新失败");
    }

    /**
     * 删除楼层配置
     * 
     * @param id 楼层ID
     * @return 操作结果
     */
    @PostMapping("/delete/{id}")
    public ResponseResult delete(@PathVariable Long id) {
        boolean success = homeFloorService.removeById(id);
        return success ? ResponseResult.success("删除成功") : ResponseResult.error("删除失败");
    }

    /**
     * 更新楼层排序
     * 
     * @param param 包含id和sort
     * @return 操作结果
     */
    @PostMapping("/sort")
    public ResponseResult updateSort(@RequestBody Map<String, Object> param) {
        Long id = Long.parseLong(param.get("id").toString());
        Integer sort = Integer.parseInt(param.get("sort").toString());
        boolean success = homeFloorService.updateSort(id, sort);
        return success ? ResponseResult.success("更新成功") : ResponseResult.error("更新失败");
    }

    /**
     * 更新楼层状态
     * 
     * @param param 包含id和status
     * @return 操作结果
     */
    @PostMapping("/status")
    public ResponseResult updateStatus(@RequestBody Map<String, Object> param) {
        Long id = Long.parseLong(param.get("id").toString());
        Integer status = Integer.parseInt(param.get("status").toString());
        boolean success = homeFloorService.updateStatus(id, status);
        return success ? ResponseResult.success("更新成功") : ResponseResult.error("更新失败");
    }
}
