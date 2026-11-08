package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class SpinwinGiftListVO {

	/**
	 * 礼品id
	 */
	private Integer giftId;

	/**
	 *礼品名称
	 */
	private String giftName; 

	/**
	 *红包模版编号
	 */
	private Integer redpacketTId; 

	/**
	 *红包模版编号
	 */
	private String redpacketTTitle; 

	/**
	 *礼品是否是红包1-是,2-否
	 */
	private Integer giftIsRedpacket; 

	/**
	 *礼品是否可用1-可用,2-不可用
	 */
	private Integer giftIsEnable; 

	/**
	 *更新人
	 */
	private String updateBy; 

	/**
	 *更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateDate; 

	/**
	 *礼品图片
	 */
	private String giftImage; 

	/**
	 *可发放红包总数
	 */
	private Integer redpacketTTotal;

	/**
	 * 奖项id
	 */
	private Integer prizeId;

	/**
	 * 奖项等级
	 */
	private String prizeGrade;

	/**
	 * 奖项名称
	 */
	private String prizeName;

	/**
	 * 是否中奖 1中奖2不中奖
	 */
	private Integer isWin;

}
