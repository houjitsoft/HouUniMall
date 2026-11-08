package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.entity.BaseMerchantPlusThaliEntity;
import com.houjiang.commons.vo.base.PlusEquityEntityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.base.entity.BaseMerchantPlusThali
 * @author Amos
 */
@Mapper
public interface BaseMerchantPlusThaliMapper extends BaseMapper<BaseMerchantPlusThaliEntity> {


    /**
     * TODO 补充方法说明
     * @return TODO
     */
    BaseMerchantPlusThaliEntity selectthalitype1(@Param("param")String plusOrder);

    /**
     *plus套餐配置
/**
     * @param map
     * @return
     */
    Integer insertplusthali(@Param("param") Map map);

    /**
     *plus套餐更新
/**
     * @param map
     * @return
     */
    Integer updateplusthali(@Param("param")Map map);

}




