package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.payment.dto.CardAmountDTO;
import com.houjiang.payment.dto.ReturnCardBaseInfoDTO;
import com.houjiang.payment.entity.PayCardBaseEntity;
import com.houjiang.payment.vo.CardAmountListVO;
import com.houjiang.payment.vo.CardIncomeListVO;
import com.houjiang.payment.vo.PayCardBaseInfoVO;
import com.houjiang.payment.vo.PayCardBaseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.payment.entity.PayCardBase
 * @author Amos
 */
@Mapper
public interface PayCardBaseMapper extends BaseMapper<PayCardBaseEntity> {

    /**
     * 分页查询卡信息
     *
     * @param page
     * @param returnCardBaseinfoParams
     * @return
     */
    IPage<PayCardBaseInfoVO> queryCardbaseInfoReport(IPage<PayCardBaseInfoVO> page, @Param("param") ReturnCardBaseInfoDTO returnCardBaseinfoParams);


    /**
     * 充值总额
     * @param param
     * @return
     */
    double queryNoRechargeMoney(@Param("param")ReturnCardBaseInfoDTO param);

    /**
     * 分页查询卡的端口
     *
     * @param page
     * @return
     */
    IPage<PayCardBaseVO> queryList(IPage<PayCardBaseVO> page, @Param("param") Map<String, Object> returnCardBaseinfoParams);

    /**
     * 充值卡余额
     * @return
     */
    double queryActivationNoRechargeMoney();

    /**
     * 查询卡消费excel
     *
     * @param param
     * @return
     */
    List<CardIncomeListVO> selectCardConsumeExcel(@Param("param") CardAmountDTO param);

    /**
     * 分页查询卡消费
     *
     * @param page
     * @param param
     * @return
     */
    IPage<CardIncomeListVO> selectCardConsume(IPage<CardIncomeListVO> page, @Param("param") CardAmountDTO param);

    /**
     * 查询卡消费金额
     *
     * @param param
     * @return
     */
    BigDecimal selectCardConsumeAmount(@Param("param") CardAmountDTO param);

    /**
     * 查询卡集合excel
     *
     * @param cardAmountQueryParam
     * @return
     */
    List<CardAmountListVO> queryCardAmountListExcel(@Param("param") CardAmountDTO cardAmountQueryParam);

    /**
     * 分页查询卡金额集合
     *
     * @param page
     * @param cardAmountQueryParam
     * @return
     */
    IPage<CardAmountListVO> queryCardAmountList(IPage<CardAmountListVO> page, @Param("param") CardAmountDTO cardAmountQueryParam);

    /**
     * 查询卡金额
     *
     * @param cardAmountQueryParam
     * @return
     */
    BigDecimal selectCardAmount(@Param("param") CardAmountDTO cardAmountQueryParam);


    /**
     * 添加充值卡基础信息
     *
     * @param baseResult
     * @return
     */
    Integer insetPayCardBase(PayCardBaseVO baseResult);
}




