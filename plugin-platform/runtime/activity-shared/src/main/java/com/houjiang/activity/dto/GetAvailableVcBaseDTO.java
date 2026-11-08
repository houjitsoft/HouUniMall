package com.houjiang.activity.dto;


import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

import java.util.List;
/**
 * @ClassName
 * @Description
 * @Author Amos
 * @Date 2023-06-13 09:05:20
 * @Version 1.0
 * @author Amos
 */
@Data
public class GetAvailableVcBaseDTO extends UserTokenBeanDTO {

	/**
	 *代金券所有者id
	 */
	private String voucherOwnerId;
	/**
	 *代金券模版编号
	 */
	private Integer voucherTId;
	/**
	 *代金券编号
	 */
	private Integer voucherId;
	/**
	 *店铺id
	 */
	private String shopId;
	/**
	 * 平台代金券id
	 */
	private Integer voucherPId;
	/**
	 *商品id
	 */
	private Integer commonId;
	/**
	 *商品id
	 */
	private Integer goodsId;
	/**
	 *代金券模版编号集合
	 */
	private List<Integer> voucherTIds;


}
