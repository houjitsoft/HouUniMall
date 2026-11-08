package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayAuthorizationAppletInformationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName PayAuthorizationAppletInformationMapper
 * @Description  授权小程序信息表
 * @Author Amos
 * @Date 2023-07-20 09:39:49
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface PayAuthorizationAppletInformationMapper extends BaseMapper<PayAuthorizationAppletInformationEntity> {

    /**
     * 查询品牌店铺小程序授权配置
     * @param shopId
     * @return
     */
    Map<String, String> queryShopAppletConfigFeign(@Param("shopId") String shopId);

    /**
     * 更新小程序信息
     * @param map
     * @return
     */
    Integer updatesecret(@Param("param")Map<String,Object> map);
}
