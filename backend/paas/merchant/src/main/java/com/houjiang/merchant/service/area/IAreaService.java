package com.houjiang.merchant.service.area;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.validate.area.AreaSearchValidate;
import com.houjiang.merchant.validate.area.RegionSearchValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.area.AreaListedVo;

/**
 * 地区服务接口类
 * @author Amos
 */
public interface IAreaService {

    /**
     * 地区列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<AreaListedVo>
     */
    PageResult<AreaListedVo> list(PageValidate pageValidate, AreaSearchValidate searchValidate);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    JSONArray listAll();

    /**
     * @notes 腾讯地图区域搜索
     * @param $params
     * @return mixed
     * @author Amos
     */
    JSONObject regionSearch(RegionSearchValidate regionSearchValidate);
}
