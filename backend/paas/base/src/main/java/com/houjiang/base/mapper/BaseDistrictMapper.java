package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.entity.BaseDistrictEntity;
import com.houjiang.commons.vo.base.DealerSetVO;
import com.houjiang.commons.vo.shop.ShopAllBaseDistrictVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName BaseDistrictMapper
 * @Description 地区表
 * @Author Amos
 * @Date 2023/6/14 15:04
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BaseDistrictMapper extends BaseMapper<BaseDistrictEntity> {
    /**
     * 根据地区id查询名称
     * @param provinceId
     * @return
     */
    String queryProvinceport(@Param("provinceId")String provinceId);

    /**
     * 查询所有地址
     * @return
     */
    List<ShopAllBaseDistrictVO> selectAllDistrict();

    /**
     * 查询地区名称
     * @param districtIds
     * @return
     */
    List<String> selectByIdList( @Param("districtIds")List<Integer> districtIds);

    /**
     * 查询地区表所对应的名称
     *
     * @param districtMap
     * @return
     */
    List<String> queryRegionName(Map<String, Integer> districtMap);

    /**
     * 根据地区id得到地区名称
     *
     * @param provinceId
     * @return
     */
    String queryProvincePort(String provinceId);

    /**
     * 查询所有地址
     *
     * @return
     */
    List<ShopAllBaseDistrictVO> selectAllDistrict2();

    /**
     * 根据地区id得到地区行政编码
     *
     * @param provinceId
     * @return
     */
    String queryDistrictCode(String provinceId);


    /**
     * 查询供应商经销配置
     *
     * @param name
     * @return
     */
    DealerSetVO queryDealerSet(@Param("key") String name);
}
