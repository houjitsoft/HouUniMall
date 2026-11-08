package com.houjiang.activity.controller;


import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivityVoucherBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.activity.dto.GetAvailableVcBaseDTO;
import com.houjiang.activity.dto.UserGetVoucherParamDTO;
import com.houjiang.activity.dto.UserVcBaseDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.VoucherBaseVO;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName ActivityVoucherBaseController
 * @Description 代金券表
 * @Author Amos
 * @Date 2023-05-25 16:55:50
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("voucherBase")
public class ActivityVoucherBaseController {
    @Autowired
    protected ActivityVoucherBaseService activityVoucherBaseService;

    /**
     * 根据店铺id查询代金券信息
     *
     * @return ReceiveOrNot 1领取 0未领取
     */
    @RequestMapping("/selectVctempByShopId")
    public ResponseResult selectVcTempbByShopId(@RequestBody GetAvailableVcBaseDTO param) {
        ResponseResult result = activityVoucherBaseService.selectVoucherTempByShopId(param);
        return result;

    }

    /**
     * 获取代金券信息(订单调用)
     *
     * @return
     */
    @PostMapping("/getvoucherdetail")
    public VoucherBaseVO getVoucherDetail(@RequestBody Map<String, Object> map) {
        VoucherBaseVO houVoucherBase = activityVoucherBaseService.getVoucherDetail(map);

        return houVoucherBase;
    }

    /**
     * 更新 红包优惠券状态
     * @param paraMap
     */
    @PostMapping("/salesprm/update")
    public ResponseResult updateSalesPrm(@RequestBody Map<String, Object> paraMap){
        ResponseResult result = activityVoucherBaseService.updateSalesPrm(paraMap);
        return result;
    }

    /**
     * 根据金额查询可领取代金券信息
     */
    @PostMapping("/selectavailablevcbase")
    public ResponseResult selectAvailableVcBase(@RequestBody Map<String, Object> paraMap){
        ResponseResult result = activityVoucherBaseService.selectAvailableVcBase(paraMap);
        return result;
    }

    /**
     * 用户领取优惠券
     * @param param
     * @return
     */
    @GlobalTransactional
    @PostMapping("/insertvcbase")
    public ResponseResult insertVcBase(@RequestBody UserGetVoucherParamDTO param){
        ResponseResult result = activityVoucherBaseService.insertVcBase(param);
        return result;
    }

    /**
     * 用户查询未使用代金券信息
     *
     * @param param
     * @return
     */
    @PostMapping("/selectvcbase")
    public ResponseResult selectVcBase(@RequestBody UserVcBaseDTO param) {
        ResponseResult result = activityVoucherBaseService.selectVcBase(param);
        return result;
    }
    /**
     * 查询 优惠券 列表
     *
     * @param param
     * @return
     */
    @PostMapping("/voucher/query/list")
    public ResponseResult queryFullReduceVcList(@RequestBody GetAvailableVcBaseDTO param) {
        ResponseResult result = activityVoucherBaseService.queryFullReduceVcList(param);
        return result;

    }
    /**
     * 查询优惠券信息
     *
     * @return
     */
    @PostMapping("/queryShopVoucherTempPort")
    public ResponseResult queryShopVoucherTempPort(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = activityVoucherBaseService.queryShopVoucherTempPort(paraMap);
        return result;
    }
    /**
     * 更新 兑换券状态
     * @param paraMap
     */
    @PostMapping("/updateProductVoucher")
    public ResponseResult updateProductVoucher(@RequestBody Map<String, Object> paraMap){
        ResponseResult result = activityVoucherBaseService.updateProductVoucher(paraMap);
        return result;
    }

    /**
     * 更新基础状态
     *
     * @param param
     */
    @PostMapping("/feign/updateVoucherBaseState")
    public void updateVoucherBaseState(@RequestBody Map<String, Object> param){
        activityVoucherBaseService.updateVoucherBaseState(param);
    }
}
