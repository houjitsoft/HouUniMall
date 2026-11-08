package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.entity.BaseExpressEntity;
import com.houjiang.base.vo.ManageExpressQueryVO;
import com.houjiang.commons.dto.base.BaseExpressDTO;
import com.houjiang.commons.vo.base.KdniaoExpressVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.base.entity.BaseExpress
 * @author Amos
 */
@Mapper
public interface BaseExpressMapper extends BaseMapper<BaseExpressEntity> {

    /**
     * 查询快递公司  feign
     * @return
     */
    List<ManageExpressQueryVO> queryNoTokenTwo();

    /**
     * 新增快递
     *
     * @param baseExpressReq
     */
    void addExpress(@Param("param") BaseExpressDTO baseExpressReq);

    /**
     * 修改快捷
     *
     * @param baseExpressReq
     */
    void updateExpress(@Param("param") BaseExpressDTO baseExpressReq);

    /**
     * 新增查询快递信息的方法
     * 通过快递单号查询相应的快递信息
     *
     * @param num 快递单号，用于查询快递信息
     * @return 返回查询到的快递信息对象KdniaoExpressVO
     */
    KdniaoExpressVO newQuerykdniaoexpress(@Param("id") Integer num);

}




