package com.houjiang.merchant.service.distribution;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.distribution.DistributionOrderSearchValidate;
import com.houjiang.merchant.vo.distribution.DistributionOrderListedVo;

/**
 * 分销订单服务接口类
 */
public interface IDistributionOrderService {
    /**
     * @notes 设置分销订单失效
     * @param $order_id
     * @return void
     * @author Amos
     */
    void distributionFailedByOrder(Integer orderId);

    /**
     * @notes 设置分销订单商品失效
     * @param $order_goods_id
     * @return void
     * @author Amos
     */
    void distributionFailedByOrderGoods(Integer orderGoodsId);

    /**
     * 分销订单列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<DistributionOrderListedVo>
     */
    PageResult<DistributionOrderListedVo> list(PageValidate pageValidate, DistributionOrderSearchValidate searchValidate);
}
