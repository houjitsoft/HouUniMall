package com.houjiang.goods.dto;

import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class UserVcBaseQueryDTO extends PageUserTokenBeanDTO {

	/**
	 *代金券状态(1-未用,2-已用,3-过期,4-收回)
	 */
	private Integer voucherState;
	/**
	 * 领取人id
	 */
	private String voucherOwnerId;
	/**
	 * 代金券模版编号
	 */
	private Integer voucherTId;

}
