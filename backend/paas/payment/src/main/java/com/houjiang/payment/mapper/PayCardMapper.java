package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.dto.CheckRechargeCardDTO;
import com.houjiang.payment.entity.PayCardEntity;
import com.houjiang.payment.vo.PayCardInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.payment.entity.PayCard
 * @author Amos
 */
@Mapper
public interface PayCardMapper extends BaseMapper<PayCardEntity> {

    /**
     * 新增支付卡
     *
     * @param pay
     */
    void addPayCardtew(@Param("list") List<PayCardEntity> pay);

    /**
     * 更新卡端口
     *
     * @param map
     * @return
     */
    Integer updateRechargeCardPort(Map<Object, Object> map);

    /**
     * 查询卡参数
     *
     * @param map
     * @return
     */
    List<PayCardInfoVO> queryPayBaseParam(Map<Object, Object> map);


    /**
     * 查询激活数据
     * @param param
     * @return
     */
    List<PayCardEntity> query(@Param("param") CheckRechargeCardDTO param);
}




