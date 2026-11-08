package com.houjiang.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class PromotionIndexQueryParamDTO extends PageUserTokenBeanDTO {

	/**
	 * 商品名
	 */
	private String commonName;
	/**
	 *订单类型  1 实物 2 虚拟 3 门店
	 */
	private Integer orderType;
	/**
	 *1降序，2升序
	 */
	private Integer priceOrder;
	/**
	 *评论排序 1降序，2升序
	 */
	private Integer commentOrder;
	/**
	 *店铺id
	 */
	private String shopId;
	/**
	 *开始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date goodsStartTime;


}
