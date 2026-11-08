package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.commons.dto.yiyatong.GoodsDTO;
import com.houjiang.commons.vo.yiyatong.base.SubmitOrderVO;
import com.houjiang.payment.entity.PayUserBaseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.payment.entity.PayUserBase
 * @author Amos
 */
@Mapper
public interface PayUserBaseMapper extends BaseMapper<PayUserBaseEntity> {

    /**
     * 密码
     *
     * @param fromUserId
     * @return
     */
    String isPasswd(@Param("fromUserId") String fromUserId);

    /**
     * 校验支付密码是否存在
     * @param paraMap
     * @return
     */
    Integer checkPayPassword(Map<String, String> paraMap);

    /**
     * 修改支付密码
     * @param userId
     * @param passwd
     * @return
     */
    boolean modifyPasswd(@Param("userId")String userId, @Param("passwd")String passwd);

    /**
     * 验证密码是否正确
     * @param userId
     * @param passwd
     * @return
     */
    Integer plusMemberPayPasswd(@Param("userId")String userId, @Param("passwd")String passwd);

    /**
     * 根据订单ID查询订单详情映射列表
     * 该方法用于查询与特定订单ID关联的所有订单详情信息，并以键值对的形式返回
     * 主要用于需要以映射形式处理订单详情数据的场景
     *
     * @param orderId 订单ID，用于查询特定订单的详情信息
     * @return 包含订单详情的映射列表，每个映射代表一个订单详情项
     */
    List<Map<String,Object>> selectOrderMap(@Param("orderId") String orderId);

    /**
     * 根据订单ID查询订单商品列表
     * 该方法用于获取特定订单下的所有商品信息，返回一个商品数据传输对象列表
     * 主要用于需要获取订单下商品详细信息的场景，便于进一步处理或展示商品数据
     *
     * @param orderId 订单ID，用于查询特定订单下的商品信息
     * @return 订单商品列表，每个元素是一个商品DTO，包含商品详细信息
     */
    List<GoodsDTO> selectOrderGoodsList(@Param("orderId") String orderId);


}




