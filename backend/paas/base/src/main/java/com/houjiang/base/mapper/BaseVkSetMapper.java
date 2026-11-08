package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.dto.CommissionSettingVO;
import com.houjiang.base.entity.BaseVkSetEntity;
import com.houjiang.commons.vo.fx.GoodsVkSetVO;
import com.houjiang.commons.vo.order.OrderCommissionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName BaseVkSetMapper
 * @Description  微客配置表
 * @Author Amos
 * @Date 2023-06-05 10:48:39
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BaseVkSetMapper extends BaseMapper<BaseVkSetEntity> {

     /**
      * 查询微客配置
      * @param vkSetId
      * @return
      */
     GoodsVkSetVO selectByPrimaryKey(Integer vkSetId);


    /**
     *插入分佣设置
     */
    Integer insertCommissionSetting(@Param("param") CommissionSettingVO param);

    /**
     * 修改分佣设置
     */
    Integer updateCommissionSetting(@Param("param") CommissionSettingVO param);



}
