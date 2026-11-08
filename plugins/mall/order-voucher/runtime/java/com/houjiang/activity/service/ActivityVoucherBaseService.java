package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.dto.GetAvailableVcBaseDTO;
import com.houjiang.activity.dto.UserGetVoucherParamDTO;
import com.houjiang.activity.dto.UserVcBaseDTO;
import com.houjiang.activity.entity.ActivityVoucherBaseEntity;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.VoucherBaseVO;

import java.util.Map;

/**
 * @ClassName ActivityVoucherBaseService
 * @Description  代金券表
 * @Author Amos
 * @Date 2023-05-25 16:55:50
 * @Version 1.0
 * @author Amos
 */
public interface ActivityVoucherBaseService extends IService<ActivityVoucherBaseEntity> {
    /**
     * 根据店铺id查询代金券信息
     * @param param
     * @return
     */
     ResponseResult selectVoucherTempByShopId(GetAvailableVcBaseDTO param);

    /**
     * 获取代金券信息
     * @param map
     * @return
     */
    VoucherBaseVO getVoucherDetail(Map<String, Object> map);

    /**
     * 更新 红包优惠券状态
     * @param paraMap
     * @return
     */
    ResponseResult updateSalesPrm(Map<String, Object> paraMap);

    /**
     * 根据金额查询可领取代金券信息
     * @param paraMap
     * @return
     */
    ResponseResult selectAvailableVcBase(Map<String, Object> paraMap);

    /**
     * 用户领取优惠券
     * @param param
     * @return
     */
    ResponseResult insertVcBase(UserGetVoucherParamDTO param);

    /**
     * 用户查询未使用代金券信息
     *
     * @param param
     * @return
     */
    ResponseResult selectVcBase(UserVcBaseDTO param);

    /**
     * 修改代金券详情
     *
     * @param voucherMap
     * @return
     */
    ResponseResult updateVoucherDetail(Map<String, Object> voucherMap);

    /**
     * 查询 优惠券 列表
     * @param param
     * @return
     */
    ResponseResult queryFullReduceVcList(GetAvailableVcBaseDTO param);

    /**
     * 查询优惠券信息
     * @param paraMap
     * @return
     */
    ResponseResult queryShopVoucherTempPort(Map<String, Object> paraMap);

    /**
     * 更新 兑换券状态
     * @param paraMap
     * @return
     */
    ResponseResult updateProductVoucher(Map<String, Object> paraMap);


    /**
     * 更新基础状态
     *
     * @param param
     */
    void updateVoucherBaseState(Map<String, Object> param);
}
