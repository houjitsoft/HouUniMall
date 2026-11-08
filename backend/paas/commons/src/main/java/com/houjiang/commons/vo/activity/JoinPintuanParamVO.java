package com.houjiang.commons.vo.activity;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class JoinPintuanParamVO {
	private String token;

	private String markId;

	private String orderId;

	/** 团长ID */
	private String userId;

	private Integer commonId;

	/** 0拼团，1单独购买 */
	private Integer type;

	/** 1 团长 2 不是 */
	private Integer isMark;

	private Integer goodsId;


}
