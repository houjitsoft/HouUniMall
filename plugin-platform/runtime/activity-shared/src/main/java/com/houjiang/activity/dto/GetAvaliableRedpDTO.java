package com.houjiang.activity.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class GetAvaliableRedpDTO extends UserTokenBeanDTO {

	/**
	 * 店铺信息
	 */
	private List<GetShopInfoDTO> shopList;

	/**
	 * 红包id
	 */
	private Integer redpacketTId;

	/**
	 * 代金券编号
	 */
	private Integer voucherTId;

	/**
	 * 订单实付价
	 */
	private Double totalPrice;



}
