package com.houjiang.commons.vo.activity;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class ExpireMarkIdUpdateParamVO {

	private List<String> expireMarkIdList;

	/** 0,等待成团。1为已成团，2为成团失败，3为已完成退款 */
	private Integer status;

	private List<String> orderIdList;


}
