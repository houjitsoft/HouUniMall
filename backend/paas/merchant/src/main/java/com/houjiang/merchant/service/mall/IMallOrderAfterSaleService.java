package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallGoodsComment;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallGoodsCommentSearchValidate;
import com.houjiang.merchant.validate.mall.MallGoodsCommentSetIsShowValidate;
import com.houjiang.merchant.validate.mall.MallGoodsCommentSetReplyValidate;
import com.houjiang.merchant.validate.mallorder.OrderAfterAuditValidate;
import com.houjiang.merchant.validate.mallorder.OrderAfterGoodsReceiveValidate;
import com.houjiang.merchant.validate.mallorder.OrderAfterSearchValidate;
import com.houjiang.merchant.vo.mall.HasAfterSaleWaitDealVo;
import com.houjiang.merchant.vo.mall.MallGoodsCommentListedVo;
import com.houjiang.merchant.vo.mallorder.OrderAfterDetailVo;
import com.houjiang.merchant.vo.mallorder.OrderAfterListVo;

/**
 * 订单售后接口类
 * @author Amos
 */
public interface IMallOrderAfterSaleService {
    // 商家发货 是否有待处理中的售后
    /**
     * TODO 补充方法说明
     * @param orderId TODO
     * @return TODO
     */
    HasAfterSaleWaitDealVo orderSendHasWait(Integer orderId);

    /**
     * 获取订单售后列表
     * @param pageValidate
     * @param searchValidate
     * @return
     */
    PageResult<OrderAfterListVo> list(PageValidate pageValidate, OrderAfterSearchValidate searchValidate);

    /**
     * @notes 获取订单售后详情
     * @param $params
     * @return array
     * @author Amos
     * @date 2022/11/14 17:32
     */
    OrderAfterDetailVo detail(Integer id);

    /**
     * 审核
     * @param validate
     */
    void audit(OrderAfterAuditValidate validate);

    /**
     * @notes 商家收货
     * @author Amos
     */
    void goodsReceive(OrderAfterGoodsReceiveValidate validate);

    /**
     * 确认退款
     * @param id
     */
    void refund(Integer id);

    /**
     * 商家处理
     * @param id
     */
    void deal(Integer id);
}
