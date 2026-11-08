package com.houjiang.job.mapper;

import com.houjiang.commons.vo.order.StatisticsMonthVO;
import com.houjiang.job.vo.UpdateALiWxOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName OrderbaseMapper
 * @Description
 * @Author Amos
 * @Date 2023/9/5 11:33
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrderbaseMapper {

    /**
     * 查询未支付的订单。
     *
     * @return 返回未支付订单的列表。
     */
    List<UpdateALiWxOrderVO> queryOrderNoPay();

    /**
     * 查询Plus会员未支付的订单。
     *
     * @return 返回Plus会员未支付订单的列表。
     */
    List<UpdateALiWxOrderVO> queryPlusOrderNoPay();

}
