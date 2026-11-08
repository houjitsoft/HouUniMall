package com.houjiang.goods.mapper;

import com.houjiang.commons.vo.goods.ManageTransportTemplateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName ShopTransportTemplateMapper
 * @Description 运费模板表
 * @Author Amos
 * @Date 2023-05-29 13:55:18
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ShopTransportTemplateMapper {


    /**
     *  查询模板
     * @param transportTemplate
     * @return
     */
    ManageTransportTemplateVO selectById(@Param("transportTemplate")Integer transportTemplate);
}
