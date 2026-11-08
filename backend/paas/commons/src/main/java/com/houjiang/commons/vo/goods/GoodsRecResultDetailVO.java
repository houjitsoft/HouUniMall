package com.houjiang.commons.vo.goods;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.goods.ActivityGoodsDetailDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class GoodsRecResultDetailVO {

	private Integer id;
	private Integer commonId;
	@Excel(name = "权重", orderNum = "14")
	private Integer displayOrder;
	private String goodsId;
	@Excel(name = "商品名称", orderNum = "0")
	/** 商品名称 */
	private String commonName;
	@Excel(name = "商品主图", orderNum = "1")
	/** 商品主图 */
	private String commonImage;
	@Excel(name = "商品销量", orderNum = "2")
	private Integer commonSalenum;
	@Excel(name = "商品评论数", orderNum = "3")
	/** 商品评论数 */
	private Integer commonEvaluate;
	@Excel(name = "商品链接", orderNum = "4")
	/** 商品地址 */
	private String comUrl;
	@Excel(name = "锁定状态", replace = { "未锁定_1", "锁定_2" }, orderNum = "5")
	/** 商品是否被锁定，默认为1,不锁定；2锁定' */
	private Integer commonIsLocked;
	@Excel(name = "商品状态", replace = { "下架_0", "正常_1", "违规_10" }, orderNum = "6")
	/** 商品状态 0下架（放入仓库），1正常，10违规（禁售） */
	private Integer commonState;
	@Excel(name = "商品审核", replace = { "未通过_0", "通过_1", "审核中_10" }, orderNum = "7")
	/** 商品审核 1通过，0未通过，10审核中 */
	private Integer commonVerify;
	@Excel(name = "更新时间", exportFormat = "yyyy-MM-dd HH:mm:ss", orderNum = "8")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateDate;
	@Excel(name = "专区审核状态", replace = { "未通过_0", "通过_1", "审核中_10" }, orderNum = "9")
	/** 专区审核状态 1通过，0未通过，10审核中 */
	private Integer activityVerify;
	@Excel(name = "专区审核备注", orderNum = "10")
	/** 审核备注 */
	private String activityVerifyRemark;

	private Integer shopId;
	@Excel(name = "店铺名称", orderNum = "11")
	private String shopName;
	@Excel(name = "抓取时长", orderNum = "12")
	/** 时长 */
	private Long duration;
	@Excel(name = "商品点击量", orderNum = "13")
	/** 商品点击量 */
	private String commonClick;

	/** 商品库存 */
	private String commonStock;

	/** 角标地址 */
	private String cornerUrl;

	/** 参与的跨店满减活动 */
	private List<VoucherMessageVO> voucherMes;

	@Excel(name = "店铺优惠", orderNum = "14")
	/** 参与的跨店满减活动 */
	private String voucherList;

	/** 专区名称 */
	private String noneName;

	/** 专区类型 */
	private String noneType;



	/** 商品价格 */
	private BigDecimal commonPrice;

	private BigDecimal commonMarketPrice;


	/** 商品广告词 */
	private String commonPromotionTips;

	private Integer commonIsXian;

	/** 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣 */
	private Integer activityStatus;

	/** 活动详情 */
	private ActivityGoodsDetailDTO activityGoodsDetail;

	private String commonSpecValue;





}
