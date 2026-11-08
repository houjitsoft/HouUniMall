package com.houjiang.activity.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class ManageSeckillBaseQueryDTO extends UserTokenBeanDTO {

	/**
	 * 秒杀id
	 */
	private Integer seckillId;

	/**
	 * 店铺id
	 */
	private String shopId;



}
