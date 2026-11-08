package com.houjiang.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 奖品 DTO
 * @author Amos
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftDTO {

	private Integer prizeId;
	/**
	 * 奖品等级
	 */
	private String prizeGrade;
	/**
	 * 奖品名称
	 */
	private String prizeName;
	/**
	 * 中奖概率
	 */
	private Double probability;
	/**
	 * 礼品是否是红包1-是,2-否
	 */
	private Integer giftIsRedpacket;

	/**
	 * 红包模板id
	 */
	private Integer redpacketTId;

	/**
	 * 是否中奖1中奖2谢谢参与
	 */
	private Integer isWin;

	public GiftDTO(Integer prizeId, String prizeGrade, String prizeName, Double probability) {
		super();
		this.prizeId = prizeId;
		this.prizeGrade = prizeGrade;
		this.prizeName = prizeName;
		this.probability = probability;
	}
}
