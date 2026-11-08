package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.entity.BaseDistrictEntity;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.base.DealerSetVO;
import com.houjiang.commons.vo.shop.ShopAllBaseDistrictVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
public interface BaseDistrictService extends IService<BaseDistrictEntity> {

    /**
     * 根据父id查询省市区
     *
     * @param districtParentId
     * @return
     */
    ResponseResult queryDistinct(Integer districtParentId);

    /**
     * 查询地区名称
     *
     * @param provinceId
     * @return
     */
    String queryProvinceport(@Param("provinceId") String provinceId);


    /**
     * 得到地址信息
     *
     * @param provinceId
     * @param cityId
     * @param districtId
     * @return
     */
    ResponseResult getAddressString(Integer provinceId, Integer cityId, Integer districtId);

    /**
     * 查询所有的省市区
     *
     * @return
     */
    ResponseResult queryAllDistrict();

    /**
     * 查询地区名称
     *
     * @param districtIds
     * @return
     */
    List<String> selectByIdList(List<Integer> districtIds);

    /**
     * 查询地区表所对应的名称
     *
     * @param districtMap
     * @return
     */
    List<String> queryRegionName(Map<String, Integer> districtMap);

    /**
     * 得到地址信息
     *
     * @param districtId
     * @return
     */
    String getAddressById(Integer districtId);

    /**
     * 查询所有的省市区
     *
     * @return
     */
    ResponseResult queryAllDistrictList();

    /**
     * 根据地址名得到地址Id
     *
     * @param addressName 地址名
     * @return 地址Id
     */
    Integer getAddressIdByName(String addressName, Integer level);

    /**
     * 根据地区Id查找地区行政编码
     *
     * @param districtId
     * @return
     */
    String queryDistrictCode(String districtId);


    /**
     * 查询所有的省市区
     * @return
     */
    List<ShopAllBaseDistrictVO> selectAllDistrict2();


    /**
     * 查询供应商经销配置
     *
     * @param name
     * @return
     */
    DealerSetVO queryDealerSet(String name);
}
