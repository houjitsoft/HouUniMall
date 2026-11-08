package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.order.OrderComplaintsVO;
import com.houjiang.order.entity.OrderComplaintsEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderComplaintsService
 * @Description  用户投诉表
 * @Author Amos
 * @Date 2023-08-23 09:38:15
 * @Version 1.0
 * @author Amos
 */
public interface OrderComplaintsService extends IService<OrderComplaintsEntity> {
    /**
     * 用户新增投诉单
     * @param map
     * @return
     */
    ResponseResult addcomplaints(Map<String, Object> map);

    /**
     * 判断订单是否在发货周期
     * @param map
     * @return
     */
    ResponseResult query(Map<String, Object> map);

    /**
     * 查询订单最后售后时间
     * @param map
     * @return
     */
    ResponseResult isAfterSales(Map<String, Object> map);

    /**
     * 查询订单是否投诉过
     * @param map
     * @return
     */
    ResponseResult isComplaints(Map<String, Object> map);

    /**
     * 查询投诉单列表
     * @param map
     * @return
     */
    ResponseResult queryComplaints(Map<String, Object> map);

    /**
     * 查询投诉单详情
     * @param map
     * @return
     */
    ResponseResult complaintsDetail(Map<String, Object> map);

    /**
     * 撤销投诉单
     * @param map
     * @return
     */
    ResponseResult revokeComplaints(Map<String, Object> map);



    /**
     * 查询投诉列表
     */
    ResponseResult queryComplaintsByPlatform(Map<String,Object> map);


    /**
     * 平台-查询投诉详情
     */
    ResponseResult queryComplaintsDetailByPlatform(Map<String,Object> map);

    /**
     * 平台-处理投诉
     */
    ResponseResult updateComplaint(Map<String,Object> map);


    /**
     * 查询投诉单金额
     * @param map
     * @return
     */
    String querymonthFine(Map<String, Object> map);


    /**
     * 添加投诉单
     * @param map
     * @return
     */

    /**
     * TODO 补充方法说明
     * @param map TODO
     * @return TODO
     */
    Integer addComplaints(Map<String,Object> map);

    /**
     * 修改投诉单赔付状态
     * @param id
     */
    void updateispay(Integer id);


    /**
     * 处理投诉单
     * @param map
     * @return
     */
    Integer handlingComplaints( Map<String, Object> map);


    /**
     * 查询未赔付投诉单
     * @param shopId
     * @return
     */
    List<OrderComplaintsVO> unpaidcomplaints(String shopId);


    /**
     * 查询投诉单总数
     * @param shopId
     * @param complaintsStatus
     * @return
     */
    Integer selectretentiontotal(String shopId,Integer complaintsStatus);


    /**
     * 查询投诉单详情
     * @param complaintsId
     * @return
     */
    OrderComplaintsVO queryComplaintsId(String complaintsId);


    /**
     * 查询投诉单详情
     * @param complaintsId
     * @return
     */
    OrderComplaintsVO queryComplaintsId1(Integer complaintsId);


    /**
     * 查询投诉
     * @param complaintsId
     * @return
     */
    Map<String, Object> selectOrderId(String complaintsId);


    /**
     * 查询投诉金额
     * @param shopId
     * @return
     */
    String queryAllFine(String shopId);

}
