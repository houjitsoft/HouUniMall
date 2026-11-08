package com.houjiang.base.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.base.DealerSetVO;
import com.houjiang.commons.vo.shop.ShopAllBaseDistrictVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @ClassName BaseDistrictController
 * @Description 地区
 * @Author Amos
 * @Date 2023/6/19 16:14
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/base/district")
public class BaseDistrictController extends BaseController {


    /**
     * 根据父id查询省市区
     *
     * @param districtParentId
     * @return
     */
    @GetMapping("/query")
    public ResponseResult queryDistinct(Integer districtParentId) {

        return baseDistrictService.queryDistinct(districtParentId);
    }

    /**
     * 查询地区 feign
     *
     * @param provinceId
     * @return
     */
    @PostMapping("/queryProvinceport")
    public String queryProvinceport(@RequestParam("provinceId") String provinceId) {
        return baseDistrictService.queryProvinceport(provinceId);
    }


    /**
     * 得到地址信息
     *
     * @param provinceId
     * @param cityId
     * @param districtId
     * @return
     */
    @GetMapping("/getAddressString")
    public ResponseResult getAddressString(@RequestParam("province") Integer provinceId,
                                           @RequestParam("city") Integer cityId,
                                           @RequestParam("county") Integer districtId) {
        ResponseResult address = baseDistrictService.getAddressString(provinceId, cityId, districtId);

        return address;
    }

    /**
     * 得到地址信息
     *
     * @param provinceId
     * @param cityId
     * @param districtId
     * @return
     */
    @GetMapping("/getAddrStr")
    public String getAddrStr(@RequestParam("provinceId") Integer provinceId,
                                           @RequestParam("cityId") Integer cityId,
                                           @RequestParam("districtId") Integer districtId) {
        ResponseResult address = baseDistrictService.getAddressString(provinceId, cityId, districtId);
        return address.getData().toString();
    }

    /**
     * 查询所有的省市区
     *
     * @return
     */
    @PostMapping("/queryAll")
    public ResponseResult queryAllDistrict() {
        return baseDistrictService.queryAllDistrict();
    }


    /**
     * 查询地区名称 feign
     *
     * @return
     */
    @PostMapping(value = "/query/district/name")
    public List<String> queryDistrictName(@RequestBody List<Integer> districtIds) {
        return baseDistrictService.selectByIdList(districtIds);
    }

    /**
     * 查询地区表所对应的名称
     *
     * @param districtMap
     * @return
     */
    @PostMapping("/queryRegionName")
    public List<String> queryRegionName(@RequestBody Map<String, Integer> districtMap) {
        return baseDistrictService.queryRegionName(districtMap);
    }

    /**
     * 得到地址信息
     *
     * @param districtId
     * @return
     */
    @Cacheable(value = "district", key = "#root.methodName + #root.args")
    @GetMapping("/getAddressById")
    public String getAddressById(@RequestParam("districtId") Integer districtId) {
        return baseDistrictService.getAddressById(districtId);
    }

    /**
     * 查询所有的省市区
     *
     * @return
     */
    @Cacheable(value = "allDistrict", key = "#root.methodName + #root.args")
    @PostMapping("/all/district/query")
    public ResponseResult queryAllDistrictList() {
        return baseDistrictService.queryAllDistrictList();
    }

    /**
     * 根据地址名得到地址Id
     *
     * @param addressName 地址名
     * @return 地址Id
     */
    @GetMapping("/getAddressIdByName")
    public Integer getAddressIdByName(@RequestParam("addressName") String addressName,
                                      @RequestParam("level") Integer level){
        return baseDistrictService.getAddressIdByName(addressName,level);
    }

    /**
     * 根据地区Id查找地区行政编码
     *
     * @param districtId
     * @return
     */
    @GetMapping("/queryDistrictCode")
    public String queryDistrictCode(@RequestParam("districtId") String districtId) {
        return baseDistrictService.queryDistrictCode(districtId);
    }

    /**
     * 查询所有的省市区
     * @return
     */
    @GetMapping("/feign/selectAllDistrict2")
    public List<ShopAllBaseDistrictVO> selectAllDistrict2(){
        List<ShopAllBaseDistrictVO> result = baseDistrictService.selectAllDistrict2();
        return result;
    }

    /**
     * 根据地区id查询
     * @param districtIds
     * @return
     */
    @PostMapping("/feign/selectByIdList")
    public List<String> selectByIdList( @RequestBody List<Integer> districtIds){
        List<String> result = baseDistrictService.selectByIdList(districtIds);
        return result;
    }

    /**
     * 查询供应商经销配置
     *
     * @param name
     * @return
     */
    @GetMapping("/feign/queryDealerSet")
    public DealerSetVO queryDealerSet(@RequestParam("name") String name){
        DealerSetVO result = baseDistrictService.queryDealerSet(name);
        return result;
    }
}
