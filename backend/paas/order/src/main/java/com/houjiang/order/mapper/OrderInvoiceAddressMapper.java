package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.order.entity.OrderInvoiceAddressEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName OrderInvoiceAddressMapper
 * @Description  发票地址管理表
 * @Author Amos
 * @Date 2023-07-04 14:05:34
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrderInvoiceAddressMapper extends BaseMapper<OrderInvoiceAddressEntity> {

    /**
     * 查询收票人地址列表
     * @param page
     * @param param
     * @return
     */
    IPage<OrderInvoiceAddressEntity> list(IPage<OrderInvoiceAddressEntity> page, Map<String, Object> param);

    /**
     * 查询当前用户已经添加的模板数量
     * @param param
     * @return
     */
    Integer queryAddressTmpListCount(@Param("param")Map<String, Object> param);

    /**
     * 取消默认
     * @param param
     */
    void updateAddressDefault(@Param("param")Map<String, Object> param);

    /**
     * 插入地址模板
     * @param param
     */
    void insertAddressTmp(@Param("param")Map<String, Object> param);

    /**
     * 修改地址模板
     * @param param
     */
    void updateAddressTmp(@Param("param")Map<String, Object> param);
}
