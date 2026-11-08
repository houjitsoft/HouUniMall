package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.FloorListDTO;
import com.houjiang.base.dto.GdbPcIndexInfoDTO;
import com.houjiang.base.entity.BaseFloorAdpageEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * @author Amos
 */
public interface BaseFloorAdpageService extends IService<BaseFloorAdpageEntity> {

    /**
     * 修改 pc 首页 天天特价 特色专题 为你推荐 楼层装修 顺序
     */

    /**
     * TODO 补充方法说明
     * @param dto TODO
     * @return TODO
     */
    ResponseResult updateQueryPcIndexSort(GdbPcIndexInfoDTO dto);


    /**
     * 修改 首页楼层
     *
     * @param param
     * @return
     */
    ResponseResult updateAdpageFloor(FloorListDTO param);


    /**
     * 查询楼层
     * @param floorAdpageId
     * @return
     */

    /**
     * TODO 补充方法说明
     * @param floorAdpageId TODO
     * @return TODO
     */
    ResponseResult queryWebIndexFloor(Integer floorAdpageId);

    /**
     * 查询web首页楼层列表
     *
     * @param
     * @return
     */
    ResponseResult queryWebIndexFloorList();



}
