package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.dto.GdbMerchantAgreementDTO;
import com.houjiang.base.entity.BaseMerchantAgreementEntity;
import com.houjiang.base.vo.GdbMerchantAgreementVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName BaseMerchantAgreementMapper
 * @Description 入驻协议
 * @Author Amos
 * @Date 2023/6/21 14:04
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BaseMerchantAgreementMapper extends BaseMapper<BaseMerchantAgreementEntity> {

    /**
     * 选择协议信息
     *
     * 该方法用于根据GdbMerchantAgreementDTO对象中的属性值查询并返回相应的协议信息
     * 主要用于merchantAgreement表中的数据查询
     *
     * @param gdbMerchantAgreement 用于查询的GdbMerchantAgreementDTO对象，包含查询条件
     * @return 返回查询到的GdbMerchantAgreementVO对象，如果未找到则返回null
     */
    GdbMerchantAgreementVO selectAgreement(@Param("gdbMerchantAgreement") GdbMerchantAgreementDTO gdbMerchantAgreement);

    /**
     * 新增协定
     *
     * @param baseMerchantAgreementEntity
     */
    void addAgreement(BaseMerchantAgreementEntity baseMerchantAgreementEntity);
}
