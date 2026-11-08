package com.houjiang.activity.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * @ClassName UserGetVoucherParamDTO
 * @Description 用户领取优惠券
 * @Author Amos
 * @Date 2023-06-13 09:05:20
 * @Version 1.0
 * @author Amos
 */
@Data
public class UserGetVoucherParamDTO extends UserTokenBeanDTO {

	/**
	 * 领取人id
	 */
	private String voucherOwnerId; 

	/**
	 * 模板id
	 */
	private Integer voucherTId; 

	/**
	 * 代金券id
	 */
	private Integer voucherId;



}
