package com.houjiang.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.activity.dto.PintuanBaseListQueryDTO;
import com.houjiang.activity.entity.ActivityPiecingTogetherTempEntity;
import com.houjiang.activity.vo.PintuanMarkMessageVO;
import com.houjiang.commons.vo.activity.ModularGoodsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ActivityPiecingTogetherTempMapper
 * @Description  拼团信息表 参与者所属团 及拼团类型
 * @Author Amos
 * @Date 2023-06-01 11:59:15
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface ActivityPiecingTogetherTempMapper extends BaseMapper<ActivityPiecingTogetherTempEntity> {

     /**
      * 插入团信息
      * @param markMessage
      */
     void insertPintuanTemp(@Param("param")PintuanMarkMessageVO markMessage);

    /**
     * 查询团长信息
     * @param markId
     * @return
     */
    List<String> selectByMarkId(@Param("markId")String markId);

    /**
     * 查询该订单是否是拼团订单
     */
    String queryPintByOrder(@Param("orderId") String orderId);

    /**
     * 修改拼团的支付状态
     */
    Integer updatePintByOrder(@Param("orderId")String orderId);

    /**
     * 标记拼团订单已退款（幂等：仅当已付款 pay_start 1->2）
     */
    Integer markTempRefundedByOrder(@Param("orderId") String orderId);

    /**
     * 统计某团已付款人数
     */
    Integer countPaidTempByMarkId(@Param("markId") String markId);

    /**
     * 查询某团已付款订单号列表
     */
    List<String> selectPaidOrderIdsByMarkId(@Param("markId") String markId);

    /**
     * 组件化查询拼团活动列表
     **/
    /**
     * TODO 补充方法说明
     * @param page TODO
     * @return TODO
     */
    IPage<Map<String,Object>> selectModularizationPintuanBaseList(IPage<Map<String,Object>> page,@Param("param") PintuanBaseListQueryDTO param);

    /**
     *查询活动商品价格
     */
    ModularGoodsVO queryGoodsPrice(@Param("goodsId") Integer goodsId);


    /**
     * 根据过期标记ID列表选择数据
     * 此方法用于批量查询，通过提供一个或多个过期标记ID，以列表的形式返回相应的数据
     * 主要用于处理批量查询请求，提高查询效率
     *
     * @param expireMarkIdList 过期标记ID列表，用于指定查询的依据条件
     * @return 返回一个字符串列表，包含根据过期标记ID查询到的数据
     */
    List<String> selectByMarkId2(@Param("expireMarkIdList")List<String> expireMarkIdList);
}
