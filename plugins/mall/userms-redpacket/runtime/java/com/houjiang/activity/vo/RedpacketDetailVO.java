package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.excel.ExcelCell;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class RedpacketDetailVO implements Serializable {
	public static final long serialVersionUID = 1L;
	/**
	 * 红包编码
	 */
	@ExcelCell(index = 1)
	private String redpacketCode; 
	/**
	 *红包标题
	 */
	@ExcelCell(index = 2)
	private String redpacketTitle; 
	/**
	 *红包描述
	 */
	@ExcelCell(index = 3)
	private String redpacketDesc; 
	/**
	 *红包有效期开始时间
	 */
	@ExcelCell(index = 4)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date redpacketStartDate; 
	/**
	 *红包有效期结束时间
	 */
	@ExcelCell(index = 5)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date redpacketEndDate; 
	/**
	 *红包面额
	 */
	@ExcelCell(index = 6)
	private double redpacketPrice; 
	/**
	 * 红包状态
	 */
	@ExcelCell(index = 7)
	private String redpacketState;
	/**
	 *红包来源：1
	 */
	@ExcelCell(index = 8)
	private String redpacketFrom; 
	/**
	 *使用该红包的订单编号
	 */
	@ExcelCell(index = 9)
	private String redpacketOrderId; 
	/**
	 *红包所有者名称
	 */
	@ExcelCell(index = 10)
	private String redpacketOwnerName; 
	/**
	 *红包发放日期
	 */
	@ExcelCell(index = 11)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date redpacketActiveDate; 
	/**
	 *跳转名称
	 */
	private String actionName;
	/**
	 *红包编码
	 */
	private String redpacketId;
	/**
	 *前端是否显示1显示2不显示
	 */
	private Integer frontEndIsDisplay; 
	/**
	 *红包是否倒计时1-否2-是
	 */
	private Integer redpacketIsBooking; 


}
