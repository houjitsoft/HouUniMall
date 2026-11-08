package com.houjiang.activity.vo;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class CheckReceivedRpOrNotResultVO {

	/**
	 * 1-未用,2-已用,3-过期
	 */
	private Integer redpacketState;
	/**
	 * 红包模板id
	 */
	private Integer redpacketTId;


}
