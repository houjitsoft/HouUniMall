package com.houjiang.merchant.common.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.Area;
import com.houjiang.merchant.common.mapper.AreaMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 地区工具类
 */
public class RegionUtils {

    /**
     * 地区详情
     *
     * @author Amos
     * @param addressId List<Integer>
     * @return Map<String, Object>
     */
    public static Map<String, Object> getDetail(List<Integer> addressId) {

        AreaMapper areaMapper = SpringUtils.getBean(AreaMapper.class);
        List<Area> regionList = areaMapper.selectList(new QueryWrapper<Area>()
                .in("id", addressId));

        HashMap<String, Object> regionMap = new HashMap<>();
        for (Area devRegion : regionList) {
            if (1 == devRegion.getLevel()) {
                regionMap.put("provinceId", devRegion.getId());
                regionMap.put("provinceName", devRegion.getName());
            }
            if (2 == devRegion.getLevel()) {
                regionMap.put("cityId", devRegion.getId());
                regionMap.put("cityName", devRegion.getName());
            }
            if (3 == devRegion.getLevel()) {
                regionMap.put("districtId", devRegion.getId());
                regionMap.put("districtName", devRegion.getName());
            }
        }

        return regionMap;
    }
}
