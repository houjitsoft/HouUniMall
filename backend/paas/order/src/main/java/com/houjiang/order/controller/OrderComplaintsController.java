package com.houjiang.order.controller;


import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.order.OrderComplaintsVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderComplaintsController
 * @Description  用户投诉表
 * @Author Amos
 * @Date 2023-08-23 09:38:14
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("order/complaints")
public class OrderComplaintsController extends BaseController {

    /**
     * 用户新增投诉单
     */
    @PostMapping("/add")
    public ResponseResult addcomplaints(@RequestBody Map<String,Object> map){
        ResponseResult addcomplaints = orderComplaintsService.addcomplaints(map);
        return addcomplaints;
    }

    /**
     * 判断订单是否在发货周期
     */
    @PostMapping("/query")
    public ResponseResult query(@RequestBody Map<String,Object> map){
        ResponseResult query = orderComplaintsService.query(map);
        return query;
    }

    /**
     * 查询订单最后售后时间
     */
    @PostMapping("/isAfterSales")
    public ResponseResult isAfterSales(@RequestBody Map<String,Object> map){
        ResponseResult afterSales = orderComplaintsService.isAfterSales(map);
        return afterSales;
    }
    /**
     * 查询订单是否投诉过
     */
    @PostMapping("/isComplaints")
    public ResponseResult isComplaints(@RequestBody Map<String,Object> map){
        ResponseResult complaints = orderComplaintsService.isComplaints(map);
        return complaints;
    }
    /**
     * 查询投诉单列表
     */
    @PostMapping("/queryComplaints")
    public ResponseResult queryComplaints(@RequestBody Map<String,Object> map){
        ResponseResult result = orderComplaintsService.queryComplaints(map);
        return result;
    }

    /**
     * 查询投诉单详情
     */
    @PostMapping("/complaintsDetail")
    public ResponseResult complaintsDetail(@RequestBody Map<String,Object> map){
        ResponseResult result = orderComplaintsService.complaintsDetail(map);
        return result;
    }

    /**
     * 撤销投诉单
     */
    @PostMapping("/revokeComplaints")
    public ResponseResult revokeComplaints(@RequestBody Map<String,Object> map){
        ResponseResult result = orderComplaintsService.revokeComplaints(map);
        return result;
    }


    /**
     * 平台-查询投诉列表
     */
    @PostMapping("/platform/queryComplaints")
    public ResponseResult queryComplaintsByPlatform(@RequestBody Map<String,Object> map){
        ResponseResult result = orderComplaintsService.queryComplaintsByPlatform(map);
        return result;
    }

    /**
     * 平台-查询投诉详情
     */
    @PostMapping("/platform/queryComplaintsDetail")
    public ResponseResult queryComplaintsDetailByPlatform(@RequestBody Map<String,Object> map){
        ResponseResult result = orderComplaintsService.queryComplaintsDetailByPlatform(map);
        return result;
    }


    /**
     * 平台-处理投诉
     */
    @PostMapping("/platform/updateComplaint")
    public ResponseResult updateComplaint(@RequestBody Map<String,Object> map){
        ResponseResult result = orderComplaintsService.updateComplaint(map);
        return result;
    }

    /**
     * 平台-查询月fine
     * @param map
     * @return
     */
    @PostMapping("/feign/querymonthFine")
    public String querymonthFine(@RequestBody Map<String, Object> map){
        String result = orderComplaintsService.querymonthFine(map);
        return result;
    }

    /**
     * 平台-新增fine
     * @param map
     * @return
     */
    @PostMapping("/feign/addComplaints")
    public Integer addComplaints(@RequestBody Map<String,Object> map){
        Integer result = orderComplaintsService.addComplaints(map);
        return result;
    }

    /**
     * 修改投诉单赔付状态
     * @param id
     */
    @GetMapping("/feign/updateispay")
    public  void updateispay(@RequestParam("id")Integer id){
        orderComplaintsService.updateispay(id);
    }

    /**
     * 平台-处理投诉
     * @param map
     * @return
     */
    @PostMapping("/feign/handlingComplaints")
    public Integer handlingComplaints(@RequestBody Map<String, Object> map){
        Integer result = orderComplaintsService.handlingComplaints(map);
        return result;
     }


    /**
     * 查询未赔付投诉单
     * @param shopId
     * @return
     */
    @GetMapping("/feign/unpaidcomplaints")
    public List<OrderComplaintsVO> unpaidcomplaints(@RequestParam("shopId")String shopId){
        List<OrderComplaintsVO> result = orderComplaintsService.unpaidcomplaints(shopId);
        return result;
    }

    /**
     * 查询待处理投诉单
     * @param shopId
     * @param complaintsStatus
     * @return
     */
    @GetMapping("/feign/selectretentiontotal")
    public Integer selectretentiontotal(@RequestParam("shopId") String shopId,@RequestParam("complaintsStatus")Integer complaintsStatus){
        Integer result = orderComplaintsService.selectretentiontotal(shopId, complaintsStatus);
        return result;
    }

    /**
     * 查询投诉单详情
     * @param complaintsId
     * @return
     */
    @GetMapping("/feign/queryComplaintsId")
    public OrderComplaintsVO queryComplaintsId(@RequestParam("complaintsId") String complaintsId){
        OrderComplaintsVO result = orderComplaintsService.queryComplaintsId(complaintsId);
        return result;
    }

    /**
     * 查询投诉单详情
     * @param complaintsId
     * @return
     */
    @GetMapping("/feign/queryComplaintsId1")
    public OrderComplaintsVO queryComplaintsId1(@RequestParam("complaintsId") Integer complaintsId){
        OrderComplaintsVO result = orderComplaintsService.queryComplaintsId1(complaintsId);
        return result;
    }


    /**
     * 查询投诉单详情
     * @param complaintsId
     * @return
     */
    @GetMapping("/feign/selectOrderId")
    public  Map<String, Object> selectOrderId(@RequestParam("complaintsId") String complaintsId){
        Map<String, Object> result = orderComplaintsService.selectOrderId(complaintsId);
        return result;
    }

    /**
     * 查询投诉单详情
     * @param shopId
     * @return
     */
    @GetMapping("/feign/queryAllFine")
    public  String queryAllFine(@RequestParam("shopId") String shopId){
        String result = orderComplaintsService.queryAllFine(shopId);
        return result;
    }
}
