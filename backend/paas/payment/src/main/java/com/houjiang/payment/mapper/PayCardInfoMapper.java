package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayCardInfoEntity;
import com.houjiang.payment.vo.PayCardBaseInfoVO;
import com.houjiang.payment.vo.PayCardInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.payment.entity.PayCardInfo
 * @author Amos
 */
@Mapper
public interface PayCardInfoMapper extends BaseMapper<PayCardInfoEntity> {

    /**
     * 更新卡
     *
     * @param mapCard
     * @return
     */
    boolean updateRechargeCard(Map<String, Object> mapCard);


    /**
     * 导出已完成办卡
     * @param param
     * @return
     */
    List<PayCardBaseInfoVO> execl(@Param(value="param")Map<String, Object> param);

    /**
     * 更新支付卡信息
     *
     * @param payCardInfoResult
     */
    void updatePayCardInfoBase(@Param("payCardInfoResult") PayCardInfoVO payCardInfoResult);


    /**
     * 新增支付卡信息
     *
     * @param cardInfo2
     * @param id
     */
    void insetPayCardInfoBase(@Param("list") List<PayCardInfoVO> cardInfo2, @Param("id") Integer id);
}




