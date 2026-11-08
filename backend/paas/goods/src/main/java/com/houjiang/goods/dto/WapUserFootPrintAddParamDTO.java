package com.houjiang.goods.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class WapUserFootPrintAddParamDTO extends UserTokenBeanDTO {

	/**
	 * 用户id
	 */
	private String userId;
	/**
	 *商品id
	 */
	private Integer commonId;
	/**
	 *分类id
	 */
	private Integer catId;
	/**
	 * 时间
	 */
	private String footprintTime;
	/**
	 * 足迹
	 */
	private String footprintDate;



}
