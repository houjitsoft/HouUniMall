package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.CommissionSettingVO;
import com.houjiang.base.entity.BaseVkSetEntity;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.fx.GoodsVkSetVO;

/**
 * @ClassName BaseVkSetService
 * @Description  微客配置表
 * @Author Amos
 * @Date 2023-06-05 10:48:39
 * @Version 1.0
 * @author Amos
 */
public interface BaseVkSetService extends IService<BaseVkSetEntity> {
    /**
     * 查询微客配置
     * @return
     */
     GoodsVkSetVO queryGoodsVkSet();

    /**
     * 添加或者修改分佣设置
     */
    ResponseResult addOrUpdateCommissionSetting(CommissionSettingVO param);


    /**
     * 查询分佣设置
     */
    ResponseResult commissionSetting();

    /**
     * 分佣订单售后时间更新
     * @return
     */
    ResponseResult updateOrderAfterSale(String orderId);
}
