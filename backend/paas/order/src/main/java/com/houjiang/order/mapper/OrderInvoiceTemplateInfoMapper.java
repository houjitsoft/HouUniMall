package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.order.entity.OrderInvoiceTemplateInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName OrderInvoiceTemplateInfoMapper
 * @Description  发票模版管理表
 * @Author Amos
 * @Date 2023-07-04 13:32:32
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrderInvoiceTemplateInfoMapper extends BaseMapper<OrderInvoiceTemplateInfoEntity> {

    /**
     * 查询发票信息列表
     * @param page
     * @param param
     * @return
     */
    IPage<OrderInvoiceTemplateInfoEntity> queryInvoiceInfoList(IPage<OrderInvoiceTemplateInfoEntity> page, @Param("param") Map<String,Object> param);

    /**
     * 查询当前用户已经添加的模板数量
     * @param param
     * @return
     */
    Integer queryInvoiceInfoListCount(@Param("param")Map<String, Object> param);

    /**
     * 取消默认
     * @param param
     */
    void updateDefault(@Param("param")Map<String, Object> param);

    /**
     * 插入发票模板
     * @param param
     */
    void insertInvoiceTmp(@Param("param")Map<String, Object> param);

    /**
     * 修改发票模板
     * @param param
     */
    void updateInvoiceTmp(@Param("param")Map<String, Object> param);

    /**
     * 新增发票
     *
     * @param param
     */
    void insertInvoiceTmpFeign(@Param("param") Map<String, Object> param);


    /**
     * 修改发票
     *
     * @param param
     */
    void updateInvoiceTmpFeign(@Param("param") Map<String, Object> param);
}
