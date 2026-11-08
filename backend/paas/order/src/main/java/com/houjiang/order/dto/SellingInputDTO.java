package com.houjiang.order.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class SellingInputDTO extends PageHelper {
	/**
	 *订单号
	 */
    private String orderId;

	/**
	 * 买家账号
	 */
    private String buyerUserName;

	/**
	 * 店铺名称
	 */
    private String shopName;

	/**
	 * 支付单号
	 */
    private String paymentNo;
    /**
     * 保存不在此购物清单中的商店ID。
     * 这个变量用于在查询或操作过程中，指定一个条件，即排除某些商店ID的影响。
     * 可能的用途包括但不限于：在统计销售数据时，忽略特定商店的销售情况；
     * 在发送促销信息时，不向这些商店发送等。
     */
    private String shopIdNotIn;


	/**
	 * 下单时间 开始时间
	 */
    private String startTime;

	/**
	 * 下单时间 结束时间
	 */
    private String endTime;

    /**
     * 标识是否为店铺ID的私有字段。
     * <p>
     * 该字段用于在业务逻辑中判断一个字符串是否代表一个店铺的ID。
     * 店铺ID在系统中具有特定的格式和意义，这个字段的存在是为了快速地进行非空和格式的校验。
     * </p>
     */
    private String isNotshopId;
	/**
	 * 省id
	 */
	private String provinceId;
	/**
	 *市Id
	 */
    private String districtCityId;
	/**
	 *市Id
	 */
    private String cityId;
	/**
	 *市Id
	 */
    private String districtId;
	/**
	 *区id
	 */
    private String areaId;
	/**
	 *1实物订单 2虚拟订单 3门店订单
	 */
    private Integer orderType; 
	/**
	 *实物 orderStatus 1待付款 2 待发货 4 待收货 6 确认收货 7 取消
/**
	 * 虚拟订单 orderStatus 1待付款 4 待核销  6 已核销   7 取消  12 已过期
	 * 自提订单 1待付款 4 待自提 6 已自提   7 取消
	 */
	 
    private Integer orderStatus;
	/**
	 * 0是无退货,1是退货中,2是退货完成，3退货不通过
	 */
    private Integer returnStatus; 
	/**
	 *0是无退款,1是退款中,2是退款完成,  3退款不通过
	 */
    private Integer refundStatus;  
	/**
	 *虚拟订单 0 是 1 否
	 */
	private Integer virtualGoods;
	/**
	 *门店自提订单 1是 2否
	 */
	private Integer orderIsShopTake;

    /** 订单类型，用于标识订单的种类，例如：普通订单、团购订单等 */
	private String type;

    /** 支付码，用于标识支付方式的代码，例如：支付宝、微信支付等 */
	private String payCode;

    /** 商店ID列表，以字符串形式存储，用于标识订单涉及的多个商店 */
	private String shopIds;

	/**
	 *1全部订单2合作商,3非合作
	 */
	private Integer status;
	/**
	 *1全部订单2合作商,3非合作
	 */
	private Integer otherOrderType;
}
