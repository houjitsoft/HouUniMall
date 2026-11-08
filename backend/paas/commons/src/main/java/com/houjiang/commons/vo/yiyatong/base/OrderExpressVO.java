package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

import java.util.List;

/**
 * @ClassName OrderExpressVO
 * @Description
 * @Author Amos
 * @Date 2024/11/14 9:33
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderExpressVO {

    /**
     * 订单配送地址ID
     */
    private String deliveryAddrId;

    /**
     * 更新标志，用于标记订单的更新状态
     */
    private String updateFlag;

    /**
     * 发货人姓名
     */
    private String sendName;

    /**
     * 订单编号，唯一标识一个订单
     */
    private String orderSn;

    /**
     * 发货地址
     */
    private String sendAddr;

    /**
     * 发货时间
     */
    private String sendTime;

    /**
     * 快递公司名称
     */
    private String expressName;

    /**
     * 配送公司客服电话
     */
    private String deliveryCorpPhone;

    /**
     * 配送公司内部序列号
     */
    private String deliveryCorpSn;

    /**
     * 快递单号
     */
    private String deliverySn;

    /**
     * 发货人联系电话
     */
    private String sendMobile;

    /**
     * 操作员ID，记录最后一次操作的用户ID
     */
    private String operationId;

    /**
     * 物流商品信息明细
     */
    private List<OrderExpressItemVO> orderExpressItemVOList;

    /**
     * 物流更新集合
     */
    private List<DeliveryUpdateVO> deliveryUpdateVOList;

}
