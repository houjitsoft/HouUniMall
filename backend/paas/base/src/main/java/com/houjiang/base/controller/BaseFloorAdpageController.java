package com.houjiang.base.controller;

import com.houjiang.base.dto.FloorListDTO;
import com.houjiang.base.dto.GdbPcIndexInfoDTO;
import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName BaseFloorAdpageController
 * @Description
 * @Author Amos
 * @Date 2023/7/5 9:09
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/base/floorAdpage")
public class BaseFloorAdpageController extends BaseController{


    /**
     * 修改 pc 首页 天天特价 特色专题 为你推荐 楼层装修 顺序
     */
    @PostMapping("/newUpdatePcIndexSort")
    public ResponseResult updateQueryPcIndexSort(@RequestBody GdbPcIndexInfoDTO dto){
        ResponseResult result = baseFloorAdpageService.updateQueryPcIndexSort(dto);
        return result;
    }


    /**
     * 修改 首页楼层
     *
     * @param param
     * @return
     */
    @SensitiveCheck(field = {"floorUpdateList.floorName.catName","floorUpdateList.leftAdpageNavList.leftAdpageNavName"})
    @PostMapping("/index/floor/update")
    public ResponseResult updateAdpageFloor(@RequestBody FloorListDTO param) {
        ResponseResult resultResp = baseFloorAdpageService.updateAdpageFloor(param);
        return resultResp;
    }

    /**
     * 查询楼层
     * @param floorAdpageId
     * @return
     */
    @GetMapping("/web/index/floor/query")
    public ResponseResult queryWebIndexFloor(@RequestParam("shopId") Integer floorAdpageId) {
        ResponseResult result = baseFloorAdpageService.queryWebIndexFloor(floorAdpageId);
        return result;
    }

    /**
     * 查询web首页楼层列表
     *
     * @param
     * @return
     */
    @PostMapping("/floor/query/list")
    public ResponseResult queryWebIndexFloorList() {
        ResponseResult result = baseFloorAdpageService.queryWebIndexFloorList();
        return result;
    }
}
