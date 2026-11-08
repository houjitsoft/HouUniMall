package com.houjiang.activity.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

import java.util.List;
/**
 * @ClassName UserAvailableRedpacketQueryDto
 * @Description  红包表
 * @Author Amos
 * @Date 2023-05-23 13:21:48
 * @Version 1.0
 * @author Amos
 */
@Data
public class UserAvailableRedpacketQueryDTO extends UserTokenBeanDTO {
	/**
	 * 红包所有者id
	 */
	private String redpacketOwnerId;
	/**
	 * 红包状态(1-未用,2-已用,3-过期)
	 */
	private Integer redpacketState;
	/**
	 * 红包模版状态(1-有效,2-失效)
	 */
	private Integer redpacketTState;
	/**
	 * 红包模板id
	 */
	private Integer redpacketTId;
	/**
	 * 红包模板id
	 */
	private List<Integer> redpacketTIdList;
	/**
	 * 商品id
	 */
	private Integer goodsId;
	/**
	 * 商品id
	 */
	private Integer commonId;
	/**
	 * 红包id
	 */
	private List<Integer> redpacketTIds;


}
