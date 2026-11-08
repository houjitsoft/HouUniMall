package com.houjiang.merchant.service.impl.area;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.Area;
import com.houjiang.merchant.common.mapper.AreaMapper;
import com.houjiang.merchant.common.util.HttpUtils;
import com.houjiang.merchant.common.util.ListUtils;
import com.houjiang.merchant.common.util.MapUtils;
import com.houjiang.merchant.service.area.IAreaService;
import com.houjiang.merchant.validate.area.AreaSearchValidate;
import com.houjiang.merchant.validate.area.RegionSearchValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.area.AreaListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 地区实现类
 * @author Amos
 */
@Service
public class AreaServiceImpl implements IAreaService {
        
    @Resource
    AreaMapper areaMapper;

    /**
     * 地区列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<AreaListedVo>
     */
    @Override
    public PageResult<AreaListedVo> list(PageValidate pageValidate, AreaSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<Area> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");

        areaMapper.setSearch(queryWrapper, searchValidate, new String[]{
            "=:parentId@parent_id:int",
            "=:level:int",
            "like:name:str",
            "=:short:str",
            "=:cityCode@city_code:str",
            "=:zipCode@zip_code:str",
            "=:gcj02Lng@gcj02_lng:str",
            "=:gcj02Lat@gcj02_lat:str",
            "=:db09Lng@db09_lng:str",
            "=:db09Lat@db09_lat:str",
            "=:remark1:str",
            "=:remark2:str",
        });

        IPage<Area> iPage = areaMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<AreaListedVo> list = new LinkedList<>();
        for(Area item : iPage.getRecords()) {
            AreaListedVo vo = new AreaListedVo();
            BeanUtils.copyProperties(item, vo);
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }


    @Override
    public JSONArray listAll() {
        List<Area> all = this.areaMapper.selectList(new QueryWrapper<Area>().between("level", 1, 3));
        JSONArray jsonArray = JSONArray.parseArray(JSONArray.toJSONString(all));
        return ListUtils.listToTree(jsonArray, "id", "parentId", "sub");
    }

    @Override
    public JSONObject regionSearch(RegionSearchValidate regionSearchValidate) {
        Map<String, String> query = new HashMap<>();
        query.put("keyword", regionSearchValidate.getTencent_keyword());
        query.put("boundary", regionSearchValidate.getTencent_boundary());
        query.put("key", regionSearchValidate.getTencent_key());
        String ret = HttpUtils.sendGet("https://apis.map.qq.com/ws/place/v1/search", HttpUtils.httpBuildQuery(query));
        return JSONObject.parseObject(ret);
    }
}
