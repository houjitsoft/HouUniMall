package com.houjiang.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class SellingInputNewDTO extends PageHelper {
	/**
	 * 订单id
	 */
	private String orderId;

	/**
	 * 买家用户名
	 */
	private String buyerUserName;

	/**
	 * 商店名称
	 */
	private String shopName;

	/**
	 * 支付单号
	 */
	private String paymentNo;

	/**
	 * 开始时间
	 */
	private Date startTime;

	/**
	 * 结束时间
	 */
	private Date endTime;
	/**
	 * 省id
	 */
	private String provinceId;
	/**
	 * 市Id
	 */
    private String districtCityId;
	/**
	 * 区id
	 */
    private String areaId;
	/**
	 *  1实物订单 2虚拟订单 3门店订单
	 */
    private Integer orderType;

	/**
	 * 实物 orderStatus 1待付款 2 待发货 4 待收货 6 确认收货 7 取消
	 * 虚拟订单 orderStatus 1待付款 4 待核销  6 已核销   7 取消  12 已过期
	 * 自提订单 1待付款 4 待自提 6 已自提   7 取消
	 */
    private Integer orderStatus;
	/**
	 * 0是无退货,1是退货中,2是退货完成，3退货不通过
	 */
    private Integer returnStatus;
	/**
	 * 0是无退款,1是退款中,2是退款完成,  3退款不通过
	 */
    private Integer refundStatus;
	/**
	 * 虚拟订单 0 是 1 否
	 */
	private Integer virtualGoods;
	/**
	 * 门店自提订单 1是 2否
	 */
	private Integer orderIsShopTake;
	/**
	 * 省id
	 */
	private String type;
	/**
	 * 是否同城配送2-否 1-是
	 */
	private Integer isDelivery;
	/**
	 * 0待取货 1已取货，待配送
	 */
	private Integer pickup;

	/**
	 * 开始时间
	 * @return
	 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getStartTime() {
        return startTime;
    }

	/**
	 * 结束时间
	 * @return
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getEndTime() {
        return endTime;
    }

	/**
	 * 支付方式1-余额支付，2-支付宝支付，3-微信支付，4-通联支付,5-健康账户余额支付,6-购物卡支付，7-充值卡+支付宝，8-充值卡+微信,9-蜂币支付，10-蜂币+支付宝，11-蜂币+微信'
	 */
	private Integer payCode;

	/**
	 * 1.线上支付  2.线下支付 3.货到付款 4.账期支付
	 */
	private Integer paymentCode;
	/**
	 * 订单来源 1:代发平台供应链订单,2:代发怡亚通供应链订单 3 京东VOP
	 */
	private Integer orderSource;


}
